package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import analysis.Stats;
import models.transform.Transform;
import models.transform.TransformFactory;
import models.transform.ValidRange;
import query.Layer_Base;
import query.Layer_Float;

//------------------------------------------------------------
public class LinearModel {

    private static final Logger logger = LoggerFactory.getLogger("app");

	//------------------------------------------------------------
    public interface DataSource {
    	public Boolean canComputeValue(int rasterX, int rasterY);
    	public float getData(int rasterX, int rasterY);
    }
    
    public final class DataConstant implements DataSource {
    	public float mValue;
    	public Boolean canComputeValue(int rasterX, int rasterY) {
    		return true;
    	}
    	public float getData(int rasterX, int rasterY) {
    		return mValue;
    	}
    }
    public final class DataLayer implements DataSource {
		public Layer_Float mDataLayer;
		public float mDataSource[][];
		public ValidRange mValidRange = null;  
		public List<Transform> mTransforms = new ArrayList<>();
    	public Boolean canComputeValue(int rasterX, int rasterY) {
    		return true;
    	}
    	public float getData(int rasterX, int rasterY) {
    	}
    }
    
	//------------------------------------------------------------
	private final class InputData {
		
		// TODO: encapsulate these into a DataSource object that can also abstract constant model inputs
		//	vs. raster inputs
		public Layer_Float mDataLayer;
		public float mDataSource[][];
		
		public ValidRange mValidRange = null;  
		public List<Transform> mTransforms = new ArrayList<>();
		public List<Position> mAt = new ArrayList<>();
		
		public Boolean canComputeValue(int rasterX, int rasterY) {
			if (mValidRange == null) return true;
			
			float data = mDataSource[rasterY][rasterX];
			 return mValidRange.isValid((double) data);
		}
		public float getData(int rasterX, int rasterY) {
			Double data = (double) mDataSource[rasterY][rasterX];
			for (Transform t: mTransforms) {
				data = t.apply(data);
			}
			return data.floatValue();
		}
		public String debugData(int rasterX, int rasterY) {
			String result = mDataLayer.getName() + ":" + getData(rasterX, rasterY);
			return result;
		}
		public String debug() {
			String result = "Data Item <" + mDataLayer.getName() + ">:\n";
			for (Position p: mAt) {
				result += p.debug() + "\n";
			}
			return result;
		}
		
		// Debug helper 
		// queries underlying datasouce and linearly interpolates min/max by alpha.
		//	an alpha of 0 returns min, an alpha of 1 returns max, an alpha of 0.5 = the mean
		// The unit conversion is applied however any range checking is skipped
		public float measureGetRanged(float alpha) {
			Double value = (double) (mDataLayer.getMin() + (mDataLayer.getMax() - mDataLayer.getMin()) * alpha);
			for (Transform t: mTransforms) {
				value = t.apply(value);
			}
			return value.floatValue();
		}
	}
	
	//------------------------------------------------------------
	private final class Element {
		public Double mValue[] = {1.0,1.0};
		public Double mCoefficient;
		public Boolean mbInteracting = false;
		public Element(Double coefficient, Boolean interacting) {
			mCoefficient = coefficient; 
			mbInteracting = interacting;
		}
		public final Double apply() {
			if (mbInteracting) {
				return mValue[0] * mValue[1] * mCoefficient;
			}
			else {
				return mValue[0] * mCoefficient;
			}
		}
		public String debug() {
			return " > Coef: " + mCoefficient + "  interacts:" + mbInteracting;
		}
	}
	//------------------------------------------------------------
	private final class Position {
		public Integer mIndex;
		public Integer mSlot;
		public Position(int index, int slot) {
			mIndex = index; mSlot = slot;
		}
		public String debug() {
			return " > index: " + mIndex + "  slot:" + mSlot;
		}
	}
	
	private Double mNoDataValue = -9999.0;
	private Double mIntercept;
	private List<InputData> mData = new ArrayList<>();
	private List<Element> mElements = new ArrayList<>();
	private List<Transform> mResultTransforms = new ArrayList<>();
	
	private final Integer VARIABLE = 0;
	private final Integer COEFFICIENT = 1;
	
	//------------------------------------------------------------
	public final LinearModel init(String csv) throws Exception {
		
		Map<String,InputData> dataMap = new HashMap<>();
		
//		Stream<String> lines = csv.lines();
//		lines.forEachOrdered(s -> {
		String lines [] = csv.split("\n");
		for (int t = 0; t < lines.length; t++) {
			// ditch extra whitespace and then skip empty and comment lines
			String s = lines[t].trim();
			if (s.startsWith(";") || s.startsWith("#") || s.length() <= 0) {
				continue;
			}
			
			logger.debug(" Line: <" + s + ">");
			
			// Split and minimally sanitize
			String el[] = s.split(",");
			for (int i = 0; i < el.length; i++) {
				el[i] = el[i].trim().toLowerCase();
			}
			
			if (el[VARIABLE].contains("intercept")) {
				Double coeff = Double.valueOf(el[COEFFICIENT]);
				this.mIntercept = coeff;
				continue;
			}
			else if (el[VARIABLE].contains("result-transform")) {
				// Example: "(result-transform), power=2, clamp=?/150"
				for (int extras = 1; extras < el.length; extras++) {
					String extra = el[extras].trim().toLowerCase();
					logger.info("Trying for: " + extra);
					mResultTransforms.add(TransformFactory.create(extra));
				}
				continue;
			}
			
			Double coeff = Double.valueOf(el[COEFFICIENT]);
			
			// look for interaction specifiers, expected to have a ":" between but likely also enclosed in "`"
			String variable = el[VARIABLE].replace("`", "");
			String interactingVariable []= variable.split(":");
			int idx = mElements.size();
			
			// Is an interacting input. Unit conversion and range handling are not currently applicable here -- these features only			
			if (interactingVariable.length > 1) { 
				//	apply to "solo" input transformation
				InputData d = dataMap.get(interactingVariable[0]);
				
				// TODO: d may be null
				d.mAt.add(new Position(idx, 0));
				d = dataMap.get(interactingVariable[1]);
				d.mAt.add(new Position(idx, 1));
				
				mElements.add(new Element(coeff, true));
				continue;
			}
			
			// "Solo" input, which wires up the data access-or plus extra bits, such as:
			//	-accepts unit conversion arguments
			//	-range clamping
			//	-out-of-range handling
			// Any of these traits are processed first and the result is passed along as a "solo" input or to the interacting inputs
			InputData d = new InputData();
			// layer name "synonyms" exist so resolve those back to the real name for simplicity
			variable = Layer_Base.resolveName(variable);
			d.mDataLayer = (Layer_Float)Layer_Base.getLayer(variable);
			d.mDataSource = d.mDataLayer.getFloatData();
			d.mAt.add(new Position(idx, 0));
			dataMap.put(variable, d);
			
			mData.add(d);
			
			mElements.add(new Element(coeff, false));
			
			// may have other bits
			// examples: "unit-convert=feet-to-meters", "input-clamp=?/56", "legal-range=?/32"
			for (int extras = 2; extras < el.length; extras++) {
				String extra = el[extras].trim().toLowerCase();
				
				Transform trx = TransformFactory.create(extra);
				if (trx == null) {
					// Split and minimally sanitize
					String ex[] = extra.split("=");
					for (int i = 0; i < ex.length; i++) {
						ex[i] = ex[i].trim().toLowerCase();
					}
					if (ex[0].equalsIgnoreCase("valid-range")) {
						d.mValidRange = new ValidRange(ex[1]);
					}
					else {
						logger.warn("---------------------------------------");
						logger.warn(" Unhandled extra bit: " + extra);
						logger.warn("---------------------------------------");
					}
				}
				else {
					d.mTransforms.add(trx);
				}
			}
		}//);
		
		return this;
	}
	
	//------------------------------------------------------------
	public final String debugInputs(String reason, int rasterX, int rasterY) {
		
		String debug = "";
		for(InputData d: mData) {
			debug += d.debugData(rasterX, rasterY) + " | ";
		}
		
		return reason + ": " + debug;
	}
	
	//------------------------------------------------------------
	public final Double calculate(int rasterX, int rasterY) {
		
		// populate data
		for(InputData d: mData) {
			if (!d.canComputeValue(rasterX, rasterY)) {
			//	logger.warn(debugInputs("No Data", rasterX, rasterY));
				return mNoDataValue;
			}
			
			final float val = d.getData(rasterX, rasterY);
			for(Position p: d.mAt) {
				final Element e = mElements.get(p.mIndex);
				e.mValue[p.mSlot] = (double) val;
			}
		}
		// Compute all elements
		Double result = mIntercept;
		for(Element e: mElements) {
			result += e.apply();
		}
		
		for (Transform t: mResultTransforms) {
			result = t.apply(result);
		}
		
		return result;
	}
	
	//------------------------------------------------------------
	public final LinearModel debug() {
		
		logger.debug("Intercept: " + mIntercept);
		logger.debug("Element definition:");
		Integer idx = 0;
		for (Element e: mElements) {
			logger.debug("Index: " + idx + "  " +e.debug()); idx++;
		}
		logger.debug("Data mapping definition:");
		for (InputData d: mData) {
			logger.debug(d.debug());
		}
		logger.debug("Final results transformations:");
		for (Transform trx: mResultTransforms) {
			logger.debug(trx.debug());
		}
		return this;
	}

	// FIXME: TODO: certain variables may not (should not) occur in any combination. Example: the sum of silt
	//	and sand should never exceed 100...so testing a sand value of 80 and silt
	//------------------------------------------------------------
	public final LinearModel measureResponse() {
	
		int numTests = 3; // tests alpha: 0.0, 0.3333, 0.66667, 1.0

		// Create first base....
		List<List<Float>> tests = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			List<Float> testValues = new ArrayList<>();
			testValues.add(i * 0.1f); // magic slope testing
			for(int t = 1; t < mData.size(); t++) { // already added first, so skip
				testValues.add(0.0f);
			}
			tests.add(testValues);
		}
		// Clone the entire set this many times....
		for (int i = 1; i < mData.size(); i++) {
			int sz = tests.size();
			for (int t = 1; t < numTests; t++) {
				for (int cp = 0; cp < sz; cp++) {
					List<Float> copy = new ArrayList<>();
					List<Float> orig = tests.get(cp);
					for (int deep = 0; deep < orig.size(); deep++) {
						copy.add(orig.get(deep).floatValue());
					}
					copy.set(i, (t % numTests) / (float)(numTests-1.0f));
					tests.add(copy);
				}
			}
		}
		
		Stats stats = new Stats(true);
		
		logger.info("Computed " + tests.size() + " test premutations");
		
		for (List<Float> aTest: tests) {
			
			int idx = 0;
			for(InputData d: mData) {
				Float alpha = aTest.get(idx); idx++;
				Float val = d.measureGetRanged(alpha);
				for(Position p: d.mAt) {
					final Element e = mElements.get(p.mIndex);
					e.mValue[p.mSlot] = (double) val;
				}
			}
			// Compute all elements
			Double result = mIntercept;
			for(Element e: mElements) {
				result += e.apply();
			}
			// Apply final transforms
			for (Transform t: mResultTransforms) {
				result = t.apply(result);
			}
			
			float val = result.floatValue();
			stats.record(val);
		}
		
		stats.debug();
		
		return this;
	}
}
