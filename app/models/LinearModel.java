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
		public float measureGetRanged(float alpha);
		public String debug();
    }
    
	//------------------------------------------------------------
    public final class DataConstant implements DataSource {
    	public Float mValue = 1.0f;
    	public Boolean canComputeValue(int rasterX, int rasterY) {
    		return true;
    	}
    	public float getData(int rasterX, int rasterY) {
    		return mValue;
    	}
		public float measureGetRanged(float alpha) {
			return mValue;
		}
		public String debug() {
			return " DataConstant: " +  mValue;
		}
    }
    
	//------------------------------------------------------------
    public final class DataLayer implements DataSource {
		public Layer_Float mDataLayer;
		public float mDataSource[][];
		public ValidRange mValidRange = null;  
		public List<Transform> mTransforms = new ArrayList<>();
    	public Boolean canComputeValue(int rasterX, int rasterY) {
			float data = mDataSource[rasterY][rasterX];
			if (Layer_Float.isNoDataValue(data)) {
				return false;
			}
			else if (mValidRange == null) {
				return true;
			}
			return mValidRange.isValid(data);
    	}
    	public float getData(int rasterX, int rasterY) {
			Float data = mDataSource[rasterY][rasterX];
			for (Transform t: mTransforms) {
				data = t.apply(data);
			}
			return data.floatValue();
    	}
		public float measureGetRanged(float alpha) {
			Float value = (mDataLayer.getMin() + (mDataLayer.getMax() - mDataLayer.getMin()) * alpha);
			for (Transform t: mTransforms) {
				value = t.apply(value);
			}
			return value.floatValue();
		}
		public String debug() {
			return " DataLayer: " + mDataLayer.getName() + " ...more details soon";
		}
    }
    
	//------------------------------------------------------------
	private final class InputData {
		
		public DataSource mDataSource;
		public List<Position> mAt = new ArrayList<>();
		
		public final Boolean canComputeValue(int rasterX, int rasterY) {
			return mDataSource.canComputeValue(rasterX, rasterY);
		}
		
		public float getData(int rasterX, int rasterY) {
			return mDataSource.getData(rasterX, rasterY);
		}
		public String debugData(int rasterX, int rasterY) {
			String result = mDataSource.debug() + ":" + getData(rasterX, rasterY);
			return result;
		}
		public String debug() {
			if (mDataSource == null) {
				logger.warn(" mDataSource is null");
			}
			String result = "Data Item <" + mDataSource.debug() + ">:\n";
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
			return mDataSource.measureGetRanged(alpha);
		}
	}
	
	//------------------------------------------------------------
	private final class Element {
		public Float mValue[] = {1.0f,1.0f};
		public Float mCoefficient;
		public Boolean mbInteracting = false;
		public Element(Float coefficient, Boolean interacting) {
			mCoefficient = coefficient; 
			mbInteracting = interacting;
		}
		public final Float apply() {
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
	
	private Float mNoDataValue = -9999.0f;
	private Float mIntercept = 0.0f;
	private Map<String,Float> mVariableIntercept = new HashMap<>();
	
	private List<InputData> mData = new ArrayList<>();
	private Map<String,DataConstant> mMappedConstants = new HashMap<>();
	
	private List<Element> mElements = new ArrayList<>();
	private List<Transform> mResultTransforms = new ArrayList<>();
	
	private final Integer VARIABLE = 0;
	private final Integer COEFFICIENT = 1;
	
	//------------------------------------------------------------
	public final void setConstant(String constant, Float value) throws Exception {
		DataConstant dc = mMappedConstants.get(constant);
		if (dc == null) {
			throw new Exception(" LinearModel: mapped constant not found: " + constant);
		}
		dc.mValue = value;
	}
	//------------------------------------------------------------
	public final void setIntercept(String interceptVariable) throws Exception {
		Float intercept = mVariableIntercept.get(interceptVariable);
		if (intercept == null) {
			throw new Exception(" LinearModel: mapped intercept not found: " + interceptVariable);
		}
		mIntercept = intercept;
	}
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
				Float coeff = Float.valueOf(el[COEFFICIENT]);
				this.mIntercept = coeff;
				String ex[] = el[VARIABLE].replace(")", "").split("=");
				if (ex.length > 1) {
					String interceptVar = ex[1].trim().toLowerCase();
					mVariableIntercept.put(interceptVar,  coeff);
				}
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
			
			Float coeff = Float.valueOf(el[COEFFICIENT]);
			
			// look for interaction specifiers, expected to have a ":" between but likely also enclosed in "`"
			String variable = el[VARIABLE].replace("`", "");
			String interactingVariable []= variable.split(":");
			int idx = mElements.size();
			
			// Is an interacting input. Unit conversion and range handling are not currently applicable here -- these features only			
			if (interactingVariable.length > 1) {
				String iv0  = interactingVariable[0], iv1 = interactingVariable[1];
				if (iv0.startsWith("@")) {
					iv0 = iv0.substring(1);
				}
				else {
					iv0 = Layer_Base.resolveName(iv0);
				}
				InputData d = dataMap.get(iv0);
				
				// TODO: d may be null
				d.mAt.add(new Position(idx, 0));
				if (iv1.startsWith("@")) {
					iv1 = iv1.substring(1);
				}
				else {
					iv1 = Layer_Base.resolveName(iv1);
				}
				d = dataMap.get(iv1);
				d.mAt.add(new Position(idx, 1));
				
				mElements.add(new Element(coeff, true));
				continue;
			}
			
			// "Solo" input, which wires up the data access-or plus extra bits that can be created from a 
			//	TransformFactory...
			// Any of these transforms are processed first and the result is passed along as a "solo" input or to the interacting inputs
			InputData d = new InputData();
			if (variable.startsWith("@")) {
				// Strip the '@' and put the variable in a map for easy run-time access to change the consts
				variable = variable.substring(1);
				DataConstant constant = new DataConstant();
				mMappedConstants.put(variable, constant);
				d.mDataSource = constant;
			}
			else {
				// layer name "synonyms" exist so resolve those back to the real name for simplicity
				variable = Layer_Base.resolveName(variable);
				DataLayer src = new DataLayer();
				src.mDataLayer = (Layer_Float)Layer_Base.getLayer(variable);
				src.mDataSource = src.mDataLayer.getFloatData();
				d.mDataSource = src;
				
				// DataLayer source may have other bits
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
							src.mValidRange = new ValidRange(ex[1]);
						}
						else {
							logger.warn("---------------------------------------");
							logger.warn(" Unhandled extra bit: " + extra);
							logger.warn("---------------------------------------");
						}
					}
					else {
						src.mTransforms.add(trx);
					}
				}
				
				// TODO: OPTIMIZE: inspect any adjacent transforms to see if they can be collapsed
				//	Example: two multiplies can turn into a single one
			}
			d.mAt.add(new Position(idx, 0));
			dataMap.put(variable, d);
			
			mData.add(d);
			
			mElements.add(new Element(coeff, false));
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
	public final Float calculate(int rasterX, int rasterY) {
		
		// populate data
		for(InputData d: mData) {
			if (!d.canComputeValue(rasterX, rasterY)) {
				return mNoDataValue;
			}
			// This data may have been transformed
			final float val = d.getData(rasterX, rasterY);
			for(Position p: d.mAt) {
				// put the final data result into all needed element slots...
				final Element e = mElements.get(p.mIndex);
				e.mValue[p.mSlot] = val;
			}
		}
		// Compute all elements
		Float result = mIntercept;
		for(Element e: mElements) {
			result += e.apply();
		}
		// Do any transforms requested on the final result
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
					e.mValue[p.mSlot] = val;
				}
			}
			// Compute all elements
			Float result = mIntercept;
			for(Element e: mElements) {
				result += e.apply();
			}
			// Apply final transforms
			for (Transform t: mResultTransforms) {
				result = t.apply(result.floatValue());
			}
			
			float val = result.floatValue();
			stats.record(val);
		}
		
		stats.debug();
		
		return this;
	}
}
