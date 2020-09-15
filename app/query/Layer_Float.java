package query;

import play.*;
import utils.Json;

import java.nio.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.*;

import analysis.AreaStats;
import analysis.Histogram;
import analysis.Stats;

//------------------------------------------------------------------------------
public class Layer_Float extends Layer_Base
{
    private static final Logger logger = LoggerFactory.getLogger("app");
	
	protected boolean mbInitedMinMaxCache;
	protected float	mMin, mMax;
	protected float[][] mFloatData;
	protected int mCountNoDataCells;
	public static final Float EPSILON = (float) 1e-3;
	public static final Float NO_DATA_VALUE = -9999.0f;
	
	//--------------------------------------------------------------------------
	public Layer_Float(String name) {

		this(name, false); // not temporary
	}
	
	//--------------------------------------------------------------------------
	public Layer_Float(String name, boolean temporary) {

		super(name, temporary);

		mbInitedMinMaxCache = false;
		mCountNoDataCells = 0;
	}
	
	// Min / Max
	//--------------------------------------------------------------------------
	public float getMin() {
		
		return mMin;
	}
	public float getMax() {
		
		return mMax;
	}
	
	//--------------------------------------------------------------------------
	public float[][] getFloatData() {
		
		return mFloatData;
	}

	// Erf, certain systems and data imports may need to handle No Data values which are
	//	different than the SmartScape / GrazeScape defaults...
	//--------------------------------------------------------------------------
	public static final Boolean isCustomNoDataValue(float customNoDataValue, float testValue) {
		return (Math.abs(testValue - customNoDataValue) < EPSILON);
	}
	
	//--------------------------------------------------------------------------
	public static final Boolean isNoDataValue(float value) {
		return (Math.abs(value - getNoDataValue()) < EPSILON);
	}
	
	//--------------------------------------------------------------------------
	public static final Float getNoDataValue() {
		return NO_DATA_VALUE;
	}
	
	//--------------------------------------------------------------------------
	protected void allocMemory() {
		
		logger.debug("  Allocating FLOAT work array");
		mFloatData = new float[mHeight][mWidth];
	}
	
	// Copies a file read bytebuffer into the internal native float array...
	//--------------------------------------------------------------------------
	protected void readCopy(ByteBuffer dataBuffer, int width, int atY) {
		
		for (int x = 0; x < width; x++) {
			mFloatData[atY][x] = dataBuffer.getFloat();
		}
	}

	// Copies the native float data into a bytebuffer that is set up to recieve it (by the caller)
	//--------------------------------------------------------------------------
	protected void writeCopy(ByteBuffer dataBuffer, int width, int atY) {
		
		for (int x = 0; x < width; x++) {
			dataBuffer.putFloat(mFloatData[atY][x]);
		}
	}
	
	//--------------------------------------------------------------------------
	final private void cacheMinMax(float value) {
		
		if (isNoDataValue(value)) {
			mCountNoDataCells++;
		}
		else {
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
	protected void processASC_Line(int y, String lineElementsArray[]) {
		
		for (int x = 0; x < lineElementsArray.length; x++) {
			
			float val = Float.parseFloat(lineElementsArray[x]);
			mFloatData[y][x] = val;
		}
	}

	//--------------------------------------------------------------------------
	protected void onLoadEnd() {
		
		if (SHOW_LAYER_LOAD_STATS) {
			try {
				AreaStats fs = new AreaStats(mFloatData).compute();
				Stats stats = fs.getAreaStats();
				Integer noDataCt = stats.getNoDataCount();
				Float noDataPerc = stats.getFractionNoData();
		
				String results = "\n" + //─────────────────────────────────────────────────────\n" +
						"¤STATISTICS\n" +
						"  NoDataCells: " + noDataCt + "\n" +
						"  NoData%:     " + String.format("%.1f%%", noDataPerc * 100) + "\n";
		
				
				if (stats.hasStatistics()) {
					Integer histogramCt = 40;
					Histogram hs = stats.getHistogram(histogramCt, stats.getMin(), stats.getMax()); 
					mMin = stats.getMin();
					mMax = stats.getMax();
					float mid = (mMin + mMax) * 0.5f;
					Float mean = stats.getMean();
					Float median = stats.getMedian();
					results += " »Value STATS \n";
					results += String.format("  Min: %.2f  Mid: %.2f  Max: %.2f \n", mMin, mid, mMax);
					results += String.format("  Mean: %.2f\n", mean);
					results += String.format("  Median: %.2f\n", median);
					results += " »HISTOGRAM (" + histogramCt + " bins) \n";
					results += hs.toString();
				}
				results += "─────────────────────────────────────────────────────\n";
				
				logger.info(results);
			}
			catch(Exception e) {
				logger.error(e.toString());
				e.printStackTrace();
			}
		}
	}
	
	//--------------------------------------------------------------------------
	protected JsonNode getParameterInternal(JsonNode clientRequest) throws Exception {

		// Set this as a default - call super first so subclass can override a return result
		//	for the same parameter request type. Unsure we need that functionality but...
		JsonNode ret = super.getParameterInternal(clientRequest);

		String type = clientRequest.get("type").textValue();
		if (type.equals("layerRange")) {
			ret = Json.pack("layerMin", getMin(),
					"layerMax", getMax());
		}
		
		return ret;
	}
	
	//--------------------------------------------------------------------------
	protected boolean query(JsonNode queryNode) {//, Selection selection) {

		detailedLog("Running continuous query");

		String lessTest = queryNode.get("lessThanTest").textValue();
		String gtrTest = queryNode.get("greaterThanTest").textValue();
		
		JsonNode gtrValNode = queryNode.get("greaterThanValue");
		JsonNode lessValNode = queryNode.get("lessThanValue");
		
		float minVal = 0, maxVal = 0;
		boolean isGreaterThan = false, isGreaterThanEqual = false;
		boolean isLessThan = false, isLessThanEqual = false;
		
		if (gtrValNode != null) {
			if (gtrValNode.isNumber()) {
				isGreaterThan = (gtrTest.compareTo(">") == 0);
				isGreaterThanEqual = !isGreaterThan;
				minVal = gtrValNode.numberValue().floatValue();
			}
		}
		if (lessValNode != null) {
			if (lessValNode.isNumber()) {
				isLessThan = (lessTest.compareTo("<") == 0);
				isLessThanEqual = !isLessThan;
				maxVal = lessValNode.numberValue().floatValue();
			}
		}
		
		detailedLog("Min value:" + Float.toString(minVal));
		detailedLog("Max value:" + Float.toString(maxVal));
		int x,y;
		
		// Split out to remove the conditional tests from the inner loops. Because not taking the
		//	chance that the JVM is smart enough to realize how to properly optimize this.
		if (isGreaterThan) {
			if (isLessThan) {
				// >  <
				detailedLog("> <");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
					/*	selection.mRasterData[y][x] &= 
							((mFloatData[y][x] > minVal && mFloatData[y][x] < maxVal) 
							? 1 : 0);*/
					}
				}
			}
			else if (isLessThanEqual) {
				// >  <=
				detailedLog("> <=");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						/*selection.mRasterData[y][x] &= 
							((mFloatData[y][x] > minVal && mFloatData[y][x] <= maxVal) 
							? 1 : 0);*/
					}
				}
			}
			else
			{ // >
				detailedLog(">");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						/*selection.mRasterData[y][x] &= 
							(mFloatData[y][x] > minVal 
							? 1 : 0);*/
					}
				}
			}
		}
		else if (isGreaterThanEqual) {
			if (isLessThan) {
				// >= <
				detailedLog(">= <");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						/*selection.mRasterData[y][x] &= 
							((mFloatData[y][x] >= minVal && mFloatData[y][x] < maxVal) 
							? 1 : 0);*/
					}
				}
			}
			else if (isLessThanEqual) {
				// >=  <=
				detailedLog(">= <=");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						/*selection.mRasterData[y][x] &= 
							((mFloatData[y][x] >= minVal && mFloatData[y][x] <= maxVal) 
							? 1 : 0);*/
					}
				}
			}
			else
			{ // >=
				detailedLog(">=");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						/*selection.mRasterData[y][x] &= 
							(mFloatData[y][x] >= minVal 
							? 1 : 0);*/
					}
				}
			}
		}
		else if (isLessThan) {
			// <
			detailedLog("<");
			for (y = 0; y < mHeight; y++) {
				for (x = 0; x < mWidth; x++) {
					/*selection.mRasterData[y][x] &= 
						(mFloatData[y][x] < maxVal 
						? 1 : 0);*/
				}
			}
		}
		else if (isLessThanEqual) {
			// <=
			detailedLog("<=");
			for (y = 0; y < mHeight; y++) {
				for (x = 0; x < mWidth; x++) {
					/*selection.mRasterData[y][x] &= 
						(mFloatData[y][x] <= maxVal 
						? 1 : 0);*/
				}
			}
		}

		detailedLog("Continuous query done!");
		return false;//selection;
	}
}

