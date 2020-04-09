package query;

import play.*;
import utils.Json;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public class Layer_Integer extends Layer_Base
{
    private static final Logger logger = LoggerFactory.getLogger("app");
	
	// Count at which we switch to using a hashset vs. a standard array list...
	private static final int RAW_BREAK_EVEN_COUNT = 10;
	
	public enum EType {
		EPreShiftedIndex,		// data imported is already shifted, do shift compares
		ELoadShiftedIndex,		// data is shifted at load time
		EQueryShiftedIndex,		// data is shifted for query testing only
		ERaw					// data is loaded raw and unmodified
	}	
	
	public class KeyItem {
		public String mName, mHexColor;
		KeyItem(String nm, String hex) {
			mName = nm; mHexColor = hex;
		}
	};
	// Internal helper class to store color key information...
	//--------------------------------------------------------------------------
	protected class Layer_Key {

		protected class ReclassifiedKey implements Comparable<ReclassifiedKey> {
			public int mCode;
			public String mName, mHexColor;
			public List<String> mOriginalNames = new ArrayList<String>(); 
			
			// originalNames can be null
			public ReclassifiedKey(int code, String name, String hexColor, String [] originalNames) {
				mCode = code;
				mName = name;
				mHexColor = hexColor;
				if (originalNames != null) {
					mOriginalNames.addAll(Arrays.asList(originalNames));
				}
			}
			public JsonNode toJson() {
				
				if (mHexColor == null) {
					return null;
				}
				
				// TODO: support sending back the original names if needed.
				return Json.pack("code", mCode,
						"name", mName,
						"color", mHexColor);
			}
		    @Override
		    public int compareTo(ReclassifiedKey o) {
		        return this.mCode - o.mCode;
		    }			
		}

		// Only keeping some of this around in case it's useful runtime?
		public Map<String,Integer> 	mOriginalKeyByString 	= new HashMap<String,Integer>();
		public Map<Integer,String> 	mOriginalKeyByCode 		= new HashMap<Integer,String>();
		public Map<String,ReclassifiedKey> mKeys 			= new HashMap<String,ReclassifiedKey>();
		
		// Original Code to New Code
		public Map<Integer,Integer> mTranslationMap 		= new HashMap<Integer,Integer>();
		
		public boolean tryLoad(String layerName) {
			File keyFile = new File("./layerData/" + layerName + ".key");
			if (!keyFile.exists()) {
				logger.warn(" Layer_Integer: tryLoad: no key file found");
				return false;
			}
			
			boolean success = false;
			//logger.error("  Attempting to read color and name key file.");
			try (BufferedReader br = new BufferedReader(new FileReader(keyFile))) {
	
				boolean inReclassify = false;
				while (br.ready()) {
					String line = br.readLine().trim();
					
					if (line.startsWith(";") || line.startsWith("#") || line.length() <= 0) {
						continue;
					}
					else if (line.toLowerCase().equals("reclassify:")) {
						//logger.error("  -- Reclassify section...");
						inReclassify = true;
						continue;
					}
					
					String split[] = line.split(",");
				
					if (inReclassify) { //---------------------------------------
						if (split.length != 3) {
							logger.error("  Parse error reading reclassifcation section of /layerData/" + layerName + ".key");
							logger.error("  Error: <read>" + line);
							logger.error("    Expected Line Format: Classification Name, NewClassificationCode, HexColor");
							throw new Exception();
						}
						else {
							String name = split[0].trim().toLowerCase();
							int code = Integer.parseInt(split[1].trim());
							String color = split[2].trim();

							//logger.error("  Read: <" + name + "> - <" + code + "> - <" + color + ">");
							
							String hasNames[] = name.split(":");
							if (hasNames.length == 1) { // no subnames
								mKeys.put(name, new ReclassifiedKey(code,name,color, null));
							}
							else { // split subnames
								String subNames[] = hasNames[1].split(";");
								//logger.error("  processing subname for: " + hasNames[0]);
								for (int idx = 0; idx < subNames.length; idx++) {
									subNames[idx] = subNames[idx].trim();
									//logger.error("  subname is: " + subNames[idx]);
								}
								mKeys.put(hasNames[0].trim(), new ReclassifiedKey(code,name,color,subNames));
							}
						}
					}
					else { //-------------------------------------------------------
						if (split.length != 2) {
							logger.error("  Parse error reading base classification of /layerData/" + layerName + ".key");
							logger.error("  Error: <read>" + line);
							logger.error("    Expected Line Format: Classification Code, Classification Name");
							throw new Exception();
						}
						else {
							Integer code = Integer.parseInt(split[0].trim());
							String name = split[1].trim().toLowerCase();
							
							//logger.error("  Read: <" + code + "> - <" + name + ">");
							
							// Bi-directional mapping
							mOriginalKeyByString.put(name, code);
							mOriginalKeyByCode.put(code,name);
						}
					}
				}
				success = true;
			}
			catch (Exception e) {
				logger.error("Keyfile was invalid, key entries discared");
				logger.error(" > " + e.toString());
				// TODO: verify this is a good idea....
				mOriginalKeyByString = null;
				mOriginalKeyByCode = null;
			}
			
			//logger.error(mKeys.toString());
			// Build translation map
			//logger.error("  Build Translation Map --------");
			if (success) {
				for(Integer originalKey: mOriginalKeyByCode.keySet()) {
					String originalName = mOriginalKeyByCode.get(originalKey);
					//logger.error("  Read: <" + originalKey + "> - <" + originalName + ">");
					
					ReclassifiedKey rk = mKeys.get(originalName);
					if (rk != null) {
						//logger.error("put a key... <" + originalKey + "> <" +rk.mCode + ">");
						mTranslationMap.put(originalKey,  rk.mCode);
					}
					else {
						boolean found = false;
						for (Entry<String, ReclassifiedKey> es: mKeys.entrySet()) {
							if (es.getValue().mOriginalNames.indexOf(originalName) >= 0) {
								//logger.error("put a key... <" + originalKey + "> <" + es.getValue().mCode + ">");
								mTranslationMap.put(originalKey, es.getValue().mCode);
								found = true;
								break;
							}
						}
						if (!found) logger.error("Mismatched key");
					}
				}
				
				//logger.error(mTranslationMap.toString());
			}
			return success;
		}

		public int translateKey(int originalCode, int translatedCode) {
			return 0;
		}
		
		public int getIndex(String indexName) {
//			logger.error("   Looking up: " + indexName);
			if (mKeys == null) return 0;
			ReclassifiedKey ki = mKeys.get(indexName.toLowerCase());
			if (ki == null) return 0;
			return ki.mCode;
		}
		
		public JsonNode toJson() {
			
			if (mKeys == null) return null;
			
			ArrayNode ret = JsonNodeFactory.instance.arrayNode();
			mKeys.forEach((k,v) -> {
				if (v.toJson() != null) ret.add(v.toJson());
			});

			return ret;
		}
		public List<KeyItem> toKeyItem() {
			List<KeyItem> ki = new ArrayList<>();
			
			List<ReclassifiedKey> rkl = new ArrayList<>(mKeys.values());
			Collections.sort(rkl);
			for (ReclassifiedKey rk: rkl) {
				ki.add(new KeyItem(rk.mName, rk.mHexColor));
			}
			return ki;
		}
	}
	
	private Layer_Key mLayerKey = new Layer_Key();
	protected int[][] mIntData;
	protected int mNoDataValue = -9999;// TODO: load from file...
	protected int mConvertedNoDataValue;
	protected EType mLayerDataFormat;
	protected boolean mbInitedMinMaxCache;
	protected int mMin, mMax;
	
	// Pass true to have the data shifted for mask type comparisons.
	//--------------------------------------------------------------------------
	public Layer_Integer(String name, Layer_Integer.EType layerType) {
		
		super(name);
		
		// if raw, don't convert the no-data value...otherwise it isn't really raw anymore...
		if (layerType == EType.ERaw) {
			mConvertedNoDataValue = mNoDataValue;
		}
		else {
			mConvertedNoDataValue = 0; // default to turning -9999 into a zero value...
		}
		mLayerDataFormat = layerType;
	}
	
	//--------------------------------------------------------------------------
	public Layer_Integer(String name) {
		
		this(name, EType.ELoadShiftedIndex); // default to a pre-shifted (load time) index
	}

	// Call after constructor...But before Layer.init...if default conversion of -9999 to 0
	//	is not ok.	
	//--------------------------------------------------------------------------
	public Layer_Integer setNoDataConversion(int newConversionValue) {
		
		mConvertedNoDataValue = newConversionValue;
		return this;
	}
	
	//--------------------------------------------------------------------------
	public List<KeyItem> getKey() {
		return mLayerKey.toKeyItem();
	}
	
	//--------------------------------------------------------------------------
	protected void beforeLoad() {
		mLayerKey.tryLoad(mName);
	}
	
	//--------------------------------------------------------------------------
	public int[][] getIntData() {
		
		return mIntData;
	}
	
	//--------------------------------------------------------------------------
	protected void allocMemory() {
		
		logger.debug("  Allocating INT work array");
		mIntData = new int[mHeight][mWidth];
	}
	
	// Copies a file read bytebuffer into the internal native int array...
	//--------------------------------------------------------------------------
	protected void readCopy(ByteBuffer dataBuffer, int width, int atY) {
		
		for (int x = 0; x < width; x++) {
			mIntData[atY][x] = dataBuffer.getInt();
			cacheMinMax(mIntData[atY][x]); 
		}
	}

	// Copies the native int data into a bytebuffer that is set up to recieve it (by the caller)
	//--------------------------------------------------------------------------
	protected void writeCopy(ByteBuffer dataBuffer, int width, int atY) {
		
		for (int x = 0; x < width; x++) {
			dataBuffer.putInt(mIntData[atY][x]);
		} 
	}
	
	//--------------------------------------------------------------------------
	protected void processASC_Line(int y, String lineElementsArray[]) {
		
		boolean erred = false;
		for (int x = 0; x < lineElementsArray.length; x++) {
			Integer val = Integer.parseInt(lineElementsArray[x]);
			if (val <= 0) {// mNoDataValue) {
				val = mConvertedNoDataValue;
			}
			else {
				// FIXME: TODO: support translation only when needed, otherwise null pointer exceptions happen...
				try {
					val = mLayerKey.mTranslationMap.get(val);
				}
				catch(Exception e) {
					logger.error(" val:" + val);
				}
				cacheMinMax(val);
				// Optionally convert to a bit style value for fast/simultaneous compares
				if (mLayerDataFormat == EType.ELoadShiftedIndex) {
					if (val <= 31) {
						val = indexToMask(val);
					}
					else if (!erred) {
						erred = true;
						logger.error("  BAD value - indexed values can only be 1-31. Was: " 
							+ Integer.toString(val));
					}
				}
			}
			mIntData[y][x] = val;
		}
	}

	//--------------------------------------------------------------------------
	final private void cacheMinMax(int value) {
		
		if (value != mNoDataValue) {
			if (!mbInitedMinMaxCache) {
				mbInitedMinMaxCache = true;
				mMin = value;
				mMax = value;
			}
		
			if (value > mMax) {
				mMax = value;
			}
			else if (value < mMin) {
				mMin = value;
			}
		}
	}
	
	//--------------------------------------------------------------------------
	protected void onLoadEnd() {
		
		logger.info("  Value range is: " + Integer.toString(mMin) + 
						" to " + Integer.toString(mMax));
	}
	
	//--------------------------------------------------------------------------
	protected JsonNode getParameterInternal(JsonNode clientRequest) throws Exception {

		// Set this as a default - call super first so subclass can override a return result
		//	for the same parameter request type. Unsure we need that functionality but...
		JsonNode ret = super.getParameterInternal(clientRequest);

		String type = clientRequest.get("type").textValue();
		if (type.equals("colorKey")) {
			ret = mLayerKey.toJson();
		}
		
		return ret;
	}
	
	// Takes an index on an indexed raster and converts it to the appropriate
	//	bit position. Index must be 1 based and not more than 31 (ie, 1-31)
	//--------------------------------------------------------------------------
	public static int indexToMask(int index) {
		
		if (index <= 0 || index > 31) {
			logger.warn("Bad index in convertIndexToMask: " + Integer.toString(index));
			return 0;
		}
		
		return (1 << (index-1));
	}

	// Takes a variable number of integer arguments...can be called like these: 
	//	int mask1 = Layer_Indexed.convertIndicesToMask(1,5,8,11,15);
	//	int mask2 = Layer_Indexed.convertIndicesToMask(2,3,7);
	//--------------------------------------------------------------------------
	public static int indexToMask(int... indicesList) {
		
		int result = 0;
		for (int i=0; i < indicesList.length; i++) {
			result |= indexToMask(indicesList[i]);
		}
		
		return result;
	}
	
	//--------------------------------------------------------------------------
	public int getIndexForString(String indexName) {
		
		return mLayerKey.getIndex(indexName);
	}
	
	// Takes a variable number of String arguments...can be called like these: 
	//	int mask1 = Layer_Indexed.convertStringsToMask("corn","soy","woodland");
	//	int mask2 = Layer_Indexed.convertStringsToMask("corn");
	//--------------------------------------------------------------------------
	public int stringToMask(String... nameList) {
		
		int result = 0;
		for (int i=0; i < nameList.length; i++) {
			result |= indexToMask(getIndexForString(nameList[i]));
		}
		
		return result;
	}

	//--------------------------------------------------------------------------
	private int getCompareBitMask(JsonNode matchValuesArray) {
		
		int queryMask = 0;
		
		ArrayNode arNode = (ArrayNode)matchValuesArray;
		if (arNode != null) {
			int count = arNode.size();
			detailedLog("Query Index Array count: " + Integer.toString(count));
			StringBuffer debug = new StringBuffer();
			debug.append("Query Indices: ");
			for (int i = 0; i < count; i++) {
				JsonNode node = arNode.get(i);
				
				int val = node.intValue(); // FIXME: default value?
				debug.append(Integer.toString(val));
				if (i < count - 1) {
					debug.append(", ");
				}
				queryMask |= indexToMask(val);
			}
			
			detailedLog(debug.toString());
			detailedLog("Final Query Mask: " + Integer.toString(queryMask));
			return queryMask;
		}
		
		return 1;
	}

	//--------------------------------------------------------------------------
	private Set<Integer> getCompareSet(JsonNode matchValuesArray) {
		
		ArrayNode arNode = (ArrayNode)matchValuesArray;
		if (arNode != null) {
			int count = arNode.size();
			if (count < RAW_BREAK_EVEN_COUNT) return null; // Break even point?
				
			Set<Integer> set = new HashSet<Integer>(count);
			for (int i = 0; i < count; i++) {
				JsonNode node = arNode.get(i);
				
				int val = node.intValue(); // FIXME: default value?
				set.add(val);
			}
			logger.info(set.toString());
			return set;
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	private int[] getCompareArray(JsonNode matchValuesArray) {
		
		ArrayNode arNode = (ArrayNode)matchValuesArray;
		if (arNode != null) {
			int count = arNode.size();
			int array[] = new int [count];
			detailedLog("Query Index Array count: " + Integer.toString(count));
			StringBuffer debug = new StringBuffer();
			debug.append("Query Indices: ");
			for (int i = 0; i < count; i++) {
				JsonNode node = arNode.get(i);
				
				int val = node.intValue(); // FIXME: default value?
				debug.append(Integer.toString(val));
				if (i < count - 1) {
					debug.append(", ");
				}
				array[i] = val;
			}
			
			detailedLog(debug.toString());
			return array;
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	private boolean doRawQuery(JsonNode queryNode) {
		
		Set<Integer> set = getCompareSet(queryNode);
		
		// We'll get a set back if we're near the supposed break-even point....
		if (set != null) {
			for (int y = 0; y < mHeight; y++) {
				for (int x = 0; x < mWidth; x++) {
					boolean found = false;
					// Only check values that ARE NOT noData and where Selection is 1
					if (mIntData[y][x] >= 0 /*&& selection.mRasterData[y][x] > 0*/) {
						if (set.contains(mIntData[y][x])) {
							found = true;
						}
					}
					//selection.mRasterData[y][x] &= (found ? 1 : 0);
				}
			}
			
			return false;//selection;
		}
		
		// Else....Doing a slower per-array-element test...
		int array[] = getCompareArray(queryNode);
		if (array != null) {
			for (int y = 0; y < mHeight; y++) {
				for (int x = 0; x < mWidth; x++) {
					boolean found = false;
					// Only check values that ARE NOT noData and where Selection is 1
					if (mIntData[y][x] >= 0/* && selection.mRasterData[y][x] > 0*/) {
						for (int i = 0; i < array.length; i++) {
							if (mIntData[y][x] == array[i]) {
								found = true;
								break;
							}
						}
					}
					//selection.mRasterData[y][x] &= (found ? 1 : 0);
				}
			}
		}
		else {
			logger.warn("Tried to get a match array but it failed!");
		}
		
		return false;//selection;
	}
	
	//--------------------------------------------------------------------------
	protected boolean query(JsonNode queryNode) {//, Selection selection) {

		detailedLog("Running indexed query");
		JsonNode queryValues = queryNode.get("matchValues");

		if (mLayerDataFormat == EType.ERaw) {
			//selection = doRawQuery(queryValues, selection);
		}
		else {
			// Doing the faster bit-mask check...
			int test_mask = getCompareBitMask(queryValues);
			if (mLayerDataFormat == EType.ELoadShiftedIndex || mLayerDataFormat == EType.EPreShiftedIndex) {
				// Doing the fastest already-shifted test...
				for (int y = 0; y < mHeight; y++) {
					for (int x = 0; x < mWidth; x++) {
					//	selection.mRasterData[y][x] &= ((mIntData[y][x] & test_mask) > 0 ? 1 : 0);
					}
				}
			}
			else if (mLayerDataFormat == EType.EQueryShiftedIndex) {
				// doing the slightly less fast shift-at-each-pixel test...
				for (int y = 0; y < mHeight; y++) {
					for (int x = 0; x < mWidth; x++) {
						int shifted = (1 << (mIntData[y][x]-1));
					//	selection.mRasterData[y][x] &= ((shifted & test_mask) > 0 ? 1 : 0);
					}
				}
			}
			else {
				logger.error("Unhandled and known integer layer type!");
			}
		}
		
		return false;//selection;
	}
}

