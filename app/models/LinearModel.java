package models;

import java.nio.file.Files;
import java.nio.file.Paths;
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
import utils.ServerStartup;

//------------------------------------------------------------
public class LinearModel {

	private static final Logger logger = LoggerFactory.getLogger("app");
	
	//------------------------------------------------------------
    public abstract class DataSource {
		public ValidRange mValidRange = null;  
		public List<Transform> mTransforms = new ArrayList<>();
	
		// Computation needs to avoid No_Data raster cells and may optionally need to avoid
		//	computing models if there is a valid_range specification for a given input variable
    	public final Boolean canComputeValue(int rasterX, int rasterY) {
			float data = fetchData(rasterX, rasterY);
			if (Layer_Float.isCustomNoDataValue(mNoDataValue, data)) {
				return false;
			}
			else if (mValidRange == null) {
				return true;
			}
			return mValidRange.isValid(data);
    	};
    	public final float computeValue(int rasterX, int rasterY) {
			Float data = fetchData(rasterX, rasterY);
			for (Transform t: mTransforms) {
				data = t.apply(data);
			}
			return data.floatValue();
    		
    	}
    	protected abstract float fetchData(int rasterX, int rasterY);
		public abstract String debug();
    }
    
    // Simple constant data source
	//------------------------------------------------------------
    public final class DataConstant extends DataSource {
    	
    	public Float mValue = 1.0f;
    	protected final float fetchData(int rasterX, int rasterY) {
    		return mValue;
    	}
		public String debug() {
			return " DataConstant: " +  mValue;
		}
    }
    
    // Data Source that originates from a raster data set 
	//------------------------------------------------------------
    public final class DataLayer extends DataSource {
    	
		public Layer_Float mDataLayer; 	// can be null, only currently needed for debug information
		public float mDataSource[][];	// should not be null
    	protected final float fetchData(int rasterX, int rasterY) {
			return mDataSource[rasterY][rasterX];
    	}
		public String debug() {
			if (mDataLayer != null) {
				return " DataLayer: " + mDataLayer.getName() + " ...more details soon";
			}
			else  {
				return " DataLayer: unnamed data source, probably transient raster data?";
			}
		}
    }
    
    // InputData has a source (which can be constant, raster) and then given the model definition,
    //	a list of positions (variables places) where that data should be injected
	//------------------------------------------------------------
	private final class InputData {
		
		public DataSource mDataSource;
		public List<Position> mAt = new ArrayList<>();
		
		public final Boolean canComputeValue(int rasterX, int rasterY) {
			return mDataSource.canComputeValue(rasterX, rasterY);
		}
		
		public float getData(int rasterX, int rasterY) {
			return mDataSource.computeValue(rasterX, rasterY);
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
	}
	
	// Elements have a coefficient and at least one variable. For interactions, there may be two variables.
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
	
	// Basically a tuple to map the data to the right element and the right "slot" in that element.
	//	The Slot specifically is used for model lines that specify interacting variables, otherwise
	//	the slot is always 1
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
	
	private Boolean mbInitialized = false;
	private Float mNoDataValue = Layer_Float.getNoDataValue();
	private Float mIntercept = 0.0f;
	private Map<String,Float> mVariableIntercept = new HashMap<>();

	// Normally, data is bound in init based on whether it links nicely to the normal GrazeScape data layers
	//	.. or whether it has the '@' constant tag notation. Alternately, prior to calling init(),
	//	a temporary raster can be bound: bindRaster. This latter approach is meant to be used when 
	//	custom field results are needed as raster inputs to a model. After the computation is done,
	//	(and no other model has a dependency on that temporary raster) those custom rasters can be discarded...
	//	
	private Map<String,InputData> mDataMap = new HashMap<>();

	private List<InputData> mData = new ArrayList<>();
	private Map<String,DataConstant> mMappedConstants = new HashMap<>();
	
	private List<Element> mElements = new ArrayList<>();
	private List<Transform> mResultTransforms = new ArrayList<>();
	
	private final Integer VARIABLE = 0;
	private final Integer COEFFICIENT = 1;
	
	// Mapped names should start with '@'
	//------------------------------------------------------------
	public final LinearModel bindRaster(String mappedName, float [][] rasterData) throws Exception {
		
		if (mbInitialized) {
			logger.error("LinearModel: must call bindRaster for <" + mappedName + "> prior to finalizing the model comutation instance");
			throw new Exception(" LinearModel: initialization process already finalized - bindRaster will not work as expected");
		}
		else if (mappedName.startsWith("@") == false) {
			logger.error("LinearModel: mappedName <" + mappedName + "> in bindRaster should begin with '@'");
			throw new Exception(" LinearModel: illegal mappedName in bindRaster");
		}
		DataLayer dl = new DataLayer();
		dl.mDataSource = rasterData;
		InputData id = new InputData();
		id.mDataSource = dl;
		mDataMap.put(mappedName, id);
		
		return this;
	}

	// TODO: fixme...in some cases we need a fixed constant...in others, the input variable could really be a raster layer...
	// Constants can be "bound" to the model with the '@' notation in the model file and then
	//	specifying a value run-time. If bindRaster() is called first (before init) and the constant
	//	name maps to a boundRaster, then that takes priority.
	// These mapped constant names should start with '@'
	//------------------------------------------------------------
	public final void setConstant(String mappedConstant, Float value) throws Exception {
		
/*		if (mappedConstant.startsWith("@") == false) {
			logger.error("LinearModel: mappedConstant <" + mappedConstant + "> in setConstant should begin with '@'");
			throw new Exception(" LinearModel: illegal mappedConstant in setConstant");
		}
*/		
		DataConstant dc = mMappedConstants.get(mappedConstant);
		if (dc == null) {
			throw new Exception(" LinearModel: mapped constant not found: " + mappedConstant);
		}
		dc.mValue = value;
	}
	
	// Certain types of model files had multiple intercepts that could be chosen by name.
	//------------------------------------------------------------
	public final void setIntercept(String interceptVariable) throws Exception {
		
		Float intercept = mVariableIntercept.get(interceptVariable);
		if (intercept == null) {
			throw new Exception(" LinearModel: mapped intercept not found: " + interceptVariable);
		}
		mIntercept = intercept;
	}
	
	//------------------------------------------------------------
	public final LinearModel init(String modelPath) throws Exception {
		
        String csv = new String( Files.readAllBytes( Paths.get(modelPath) ));
		
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
				//	logger.info("Trying for: " + extra);
					mResultTransforms.add(TransformFactory.create(extra));
				}
				continue;
			}
			
			Float coeff = Float.valueOf(el[COEFFICIENT]);
			
			// look for interaction specifiers, expected to have a ":" between potentially also enclosed in "`"
			String variable = el[VARIABLE].replace("`", "");
			String interactingVariable []= variable.split(":");
			int idx = mElements.size();
			
			// Is an interacting input. Unit conversion and range handling are not currently applicable here -- these features only			
			if (interactingVariable.length > 1) {
//				logger.debug("LinearModel: setting up interacting variables");
				String iv0  = interactingVariable[0], iv1 = interactingVariable[1];
				
				iv0 = Layer_Base.resolveName(iv0, false);
				InputData d = mDataMap.get(iv0);
				if (d == null) {
					// may be a flex-bind variable, which could be a constant or a bound raster
					d = mDataMap.get("@" + iv0);
					if (d == null) {
						throw new Exception("LinearModel: <" + iv0 + "> was not mapped to an InputData");
					}
				}
				d.mAt.add(new Position(idx, 0));
				
				iv1 = Layer_Base.resolveName(iv1, false);
				d = mDataMap.get(iv1);
				if (d == null) {
					// may be a flex-bind variable, which could be a constant or a bound raster
					d = mDataMap.get("@" + iv1);
					if (d == null) {
						throw new Exception("LinearModel: <" + iv1 + "> was not mapped to an InputData");
					}
				}
				d.mAt.add(new Position(idx, 1));
				
				mElements.add(new Element(coeff, true));
				continue;
			}
			
			// "Solo" input, which wires up the data access-or plus extra bits that can be created from a 
			//	TransformFactory...
			// Any of these transforms are processed first and the result is passed along as a "solo" input or to the interacting inputs
			logger.debug("LinearModel: 'solo' type variable");
			// boundRasters will already have one of these allocated.. Other types will have to allocate one
			InputData id = null; 
			if (variable.startsWith("@")) {
				// In the case of a flex-bind variable, it has to be pre-bound to a raster before the model init step
				//	if pre-bind is done, then this variable will already exist in the map below.
				//	If not, then the @variable is treated as a constant variable that has to be programmatically changed
				//		as needed
				id = mDataMap.get(variable);
				if (id == null) {
					id = new InputData();					
					DataConstant constant = new DataConstant();
					mMappedConstants.put(variable, constant);
					id.mDataSource = constant;
				}
			}
			else {
				id = new InputData();					
				// layer name "synonyms" exist so resolve those back to the real name for simplicity
				variable = Layer_Base.resolveName(variable);
				DataLayer src = new DataLayer();
				src.mDataLayer = (Layer_Float)Layer_Base.getLayer(variable);
				src.mDataSource = src.mDataLayer.getFloatData();
				id.mDataSource = src;
			}

			DataSource dSrc = id.mDataSource; 
			
			// DataSources may have validation settings, etc
			// examples: "unit-convert=feet-to-meters", "input-clamp=?/56", "legal-range=?/32"
			for (int extras = 2; extras < el.length; extras++) {
				String extra = el[extras].trim().toLowerCase();
				
				logger.debug("Wanting to create: <" + extra + ">");
				Transform trx = TransformFactory.create(extra);
				// Might be a ValidRange type which is not classified as a Transform
				if (trx == null) {
					// Split and minimally sanitize
					String ex[] = extra.split("=");
					for (int i = 0; i < ex.length; i++) {
						ex[i] = ex[i].trim().toLowerCase();
					}
					if (ex[0].equalsIgnoreCase("valid-range")) {
						dSrc.mValidRange = new ValidRange(ex[1]);
					}
					else {
						logger.warn("---------------------------------------");
						logger.warn(" Unhandled extra bit: " + extra);
						logger.warn("---------------------------------------");
					}
				}
				else {
					dSrc.mTransforms.add(trx);
				}
				
				// TODO: OPTIMIZE: inspect any adjacent transforms to see if they can be collapsed
				//	Example: two multiplies next to each other could safely be pre-multiplied and thus combined into a single step 
			}
			id.mAt.add(new Position(idx, 0));
			logger.debug("LinearModel: adding variable <" + variable + "> to map");
			mDataMap.put(variable, id);
			
			mData.add(id);
			
			mElements.add(new Element(coeff, false));
		}//);
		
		mbInitialized = true;		
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
		
		// populate data from the bound data sources
		for(InputData d: mData) {
			if (!d.canComputeValue(rasterX, rasterY)) {
				return mNoDataValue;
			}
			
			// Get the data, which may have been transformed as per the model-spec
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
	
}
