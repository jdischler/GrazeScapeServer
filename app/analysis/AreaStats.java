package analysis;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//------------------------------------------------------------------------------
public class AreaStats
{
//    private static final Logger logger = LoggerFactory.getLogger(FieldStats.class);


	//---------------------------------------------
    public class Histogram {
	//---------------------------------------------
    	
    	public class Entry {
    		public Entry(float min, float max) {
    			mCount = 0;
    			mMin = min; mMax = max;
    		}
    		float mMin, mMax;
    		int mCount;
    		
    		final void increment() {
    			mCount++;
    		}
    	}
    	
    	int mBins;
    	List<Entry> mHistogram = new ArrayList<>();
    	
    	//---------------------------------------------
    	public Histogram(int bins) {
    		mBins = bins;
    	}
    	
    	//---------------------------------------------
    	public Histogram compute(List<Float> forValues, float min, float max) {
    		
    		float range = max - min;
    		float binWidth = range / mBins;
    		
    		for (int idx = 0; idx < mBins; idx++) {
    			mHistogram.add(new Entry(min + binWidth * idx, min + binWidth * (idx +1)));
    		}
    		
    		for (Float val: forValues) {
    			int bin = (int)((val - min) / binWidth - 1);
    			if (bin <  0) bin = 0;
    			else if (bin >= mBins - 1) bin = mBins - 1; // TODO: was hoping to perfectly fit these values without clamping?
    			
    			mHistogram.get(bin).increment();
    		}
    		return this;
    	}
    	
    	//---------------------------------------------
    	public String toString() {
    		
    		final String chartSpacer = "  "; 
    		int highCt = 0, vHeight = 30;
    		float bestBinMin = Float.POSITIVE_INFINITY, bestBinMax = Float.NEGATIVE_INFINITY;
    		StringBuilder sb = new StringBuilder();
    		for (Entry e: mHistogram) {
    			if (e.mCount > highCt) {
    				highCt = e.mCount;
    				bestBinMin = e.mMin;
    				bestBinMax = e.mMax;
    			}
    		}
    		
    		for (int y = 0; y < vHeight; y += 2) {
    			String ch[] = new String[mBins];
    			int it = 0;
    			
        		for (Entry e: mHistogram) {
        			int ht = Math.round(e.mCount / ((float)highCt) * vHeight);
        			if (ht >= (vHeight - y) ) {
        				ch[it] = "█";
        			}
        			else if (ht+1 >= (vHeight - y) ) {
        				ch[it] = "▄";
        			}
        			else if (y + 2 >= vHeight && e.mCount > 0 ) {
        				ch[it] = "_";
        			}
        			else {
        				ch[it] = " ";
        			}
        			it++;
        		}
        		
        		sb.append(chartSpacer + "║");
        		for (String s: ch) {
        			sb.append(s);
        		}
        		sb.append("║\n");
    		}
    		sb.append(chartSpacer + "╚");
    		for (int ct = 0; ct < mBins; ct++) {
    			sb.append("═");
    		}
    		sb.append("╝\n");
    		
    		sb.append(String.format("     Highest bin: [%.2f -> %.2f]  %.2f(ac)\n", bestBinMin, bestBinMax, (highCt * 100.0f / 4047.0f)));
    		
    		return sb.toString();
    	}
    }
    
	//---------------------------------------------
    public class Stats {
	//---------------------------------------------
    	
    	int mConsideredCells = 0;
    	int mCellsCounted = 0;   
    	int mNoDataCount = 0;
    	
    	float mMin = Float.POSITIVE_INFINITY, mMax = Float.NEGATIVE_INFINITY;
    	double mSum = 0.0;
    	
    	//FIXME: this always seems broken...
    	float mNoDataValue = -9999.0f;
    	
    	boolean mbForFullStats = false; // such as ability to histogram at the end of data collection
    	List<Float> mValues = null; 

    	//---------------------------------------------
    	public Stats(boolean forFullStats) {
    		if (forFullStats) {
    			mbForFullStats = forFullStats;
    			// TODO: tune initial size but this comfortably holds a 10acre field at 10m res data
    			mValues = new ArrayList<>(500);
    		}
    	}

    	//---------------------------------------------
    	public void record(float element) {
    		
    		mConsideredCells++;
    		if (Math.abs(element - mNoDataValue) < 0.001f) {
    			mNoDataCount++;
    			return;
    		}
    		
    		mCellsCounted++;
    		mSum += element;
    		if (element > mMax) mMax = element;
    		if (element < mMin) mMin = element;
    		if (mbForFullStats) {
    			mValues.add(element);
    		}
    	}
    	
    	//---------------------------------------------
    	public int getNoDataCount() {
    		return mNoDataCount;
    	}
    	//---------------------------------------------
    	public float getFractionNoData() throws Exception {
    		if (mConsideredCells <= 0) throw new Exception("Stats getFractionNoData failed with divide by zero");
    		return mNoDataCount / ((float)mConsideredCells);
    	}
    	//---------------------------------------------
    	public boolean hasStatistics() {
    		return mCellsCounted > 0;
    	}
    	//---------------------------------------------
    	public int getCounted() {
    		return mCellsCounted;
    	} 
    	//---------------------------------------------
    	public double getSum() {
    		return mSum;
    	}
    	//---------------------------------------------
    	public float getMin() {
    		return mMin;
    	}
    	//---------------------------------------------
    	public float getMax() {
    		return mMax;
    	}
    	//---------------------------------------------
    	public float getMean() throws Exception {
    		if (mCellsCounted <= 0) throw new Exception("Stats getMean failed with divide by zero");
    		return (float)(mSum / mCellsCounted);
    	}
    	//---------------------------------------------
    	public float getMedian() throws Exception {
    		if (!mbForFullStats) throw new Exception("Stats object was only created with simple analysis features");
    		
			Collections.sort(mValues);			
			if (mCellsCounted % 2 > 0) { // odd, return the middle
				return mValues.get(mCellsCounted / 2);
			}
			else { // even, average the two middle values
			    return  (mValues.get(mCellsCounted / 2) + mValues.get(mCellsCounted / 2 - 1)) / 2.0f;
			}
    	}
    	//---------------------------------------------
    	public Histogram getHistogram(int bins) throws Exception {
    		if (!mbForFullStats) throw new Exception("Stats object was only created with simple analysis features");
    		
    		return new Histogram(bins).compute(mValues, mMin, mMax);
    	}
    	// FIXME: TODO: overriding min and max will require the histogram engine to add extra safety checking (which is not currently there)
    	//---------------------------------------------
    	public Histogram getHistogram(int bins, float min, float max) throws Exception {
    		if (!mbForFullStats) throw new Exception("Stats object was only created with simple analysis features");
    		
    		return new Histogram(bins).compute(mValues, min, max);
    	}
    }
    
    //------------------------------------------------------------
    private Map<Long,Stats> mFieldStats = null;
    
    // TODO: wrap these to reduce sending around raw 2D arrays?
    private int[][] 	mFieldIDs = null;
    private float[][]	mFieldData = null;
    
    
    //----------------------------------------------------------------
    public AreaStats(float[][] data) {
    	mFieldStats = new HashMap<>();
    	mFieldData = data;
    	
    	// set default analysis extents
    }
    
    //----------------------------------------------------------------
    public AreaStats forRasterizedFields(int[][] fieldIDs) {
    	mFieldIDs = fieldIDs;
    	return this;
    }
    
    //----------------------------------------------------------------
    public AreaStats forExtents(int x, int y, int width, int height) {
    	// TODO: this.set up analysis extents
    	return this;
    }
    
    //----------------------------------------------------------------
    public AreaStats compute() {

		int rasterWidth = 1500, rasterHeight = 2600;
		
		if (mFieldIDs == null) {
			
			Stats s = new Stats(true);
			mFieldStats.put(0L, s);
			
	    	for (int y = 0; y < rasterHeight; y++) {
	        	for (int x = 0; x < rasterWidth; x++) {
	    			s.record(mFieldData[y][x]);
	        	}
	    	}
		}
		else {
	    	for (int y = 0; y < rasterHeight; y++) {
	        	for (int x = 0; x < rasterWidth; x++) {
	        		
	        		int f_id = mFieldIDs[y][x];
	        		if (f_id <= 0) continue;
	        		
	    			Stats s = mFieldStats.get((long)f_id);
	    			if (s == null) {
	    				s = new Stats(true);
	    				mFieldStats.put((long) f_id, s);
	    			}
	        		
	    			s.record(mFieldData[y][x]);
	        	}
	    	}
		}
    	return this;
    }
    
    //----------------------------------------------------------------
    public Stats getFieldStats(Long fs_idx) throws Exception {
		
		if (mFieldIDs == null) throw new Exception("AreaStats: was not configured for field level compution");
		return mFieldStats.get(fs_idx);
    }
    
    // TODO: area stats may also be wanted?
    //----------------------------------------------------------------
    public Stats getAreaStats() throws Exception {
		if (mFieldIDs != null) throw new Exception("AreaStats: was configured for field level compution so total area stats are not available");
		return mFieldStats.get(0L);
    }
    
    // 
	//--------------------------------------------------------------------------
    @Deprecated // I have no idea what this is....
	public static void huh_fetch_image() {
		
		// topLeftX, topLeftY, bottomRightX, bottomRightY
		Integer areaExtents[] = {
			-10128000, 5392000,
			-10109000, 5358000
		};
	
		// Align selection to 10m grid
		// The openLayers extent has the Y values reversed from the convention I prefer
		Integer selExtents[] = {
//			Math.round(extent.get(0).floatValue() / 10.0f) * 10, Math.round(extent.get(3).floatValue() / 10.0f) * 10,
//			Math.round(extent.get(2).floatValue() / 10.0f) * 10, Math.round(extent.get(1).floatValue() / 10.0f) * 10
		};
		
		// Clip Selection to area
		if (selExtents[0] < areaExtents[0]) 		selExtents[0] = areaExtents[0];
		else if (selExtents[0] > areaExtents[2]) 	selExtents[0] = areaExtents[2];
		
		if (selExtents[2] < areaExtents[0]) 		selExtents[2] = areaExtents[0];
		else if (selExtents[2] > areaExtents[2]) 	selExtents[2] = areaExtents[2];

		if (selExtents[1] > areaExtents[1]) 		selExtents[1] = areaExtents[1];
		else if (selExtents[1] < areaExtents[3]) 	selExtents[1] = areaExtents[3];
		
		if (selExtents[3] > areaExtents[1]) 		selExtents[3] = areaExtents[1];
		else if (selExtents[3] < areaExtents[3]) 	selExtents[3] = areaExtents[3];
//		logger.error("clipped [" + selExtents[0] + "," + selExtents[1] + "][" + selExtents[2] + "," + selExtents[3] + "]");

		int rasterWidth = 1900, rasterHeight = 3400;
		
		// re-index
		Integer indexX = (selExtents[0] - areaExtents[0]) / 10;
		Integer indexY = -(selExtents[1] - areaExtents[1]) / 10;
		
		Integer indexX2 = (selExtents[2] - areaExtents[0]) / 10;
		Integer indexY2 = -(selExtents[3] - areaExtents[1]) / 10;
		
		Integer ww = indexX2 - indexX;
		Integer hh = indexY2 - indexY;

/*		Map<Integer,Float> mTotalSlope = new HashMap<>();
		Map<Integer,Integer> mCellCount = new HashMap<>();
		
		float slp[][] = Layer_Base.getLayer("Slope").getFloatData();
		for (int y = 0; y < rasterHeight; y++) {
			for (int x = 0; x < rasterWidth; x++) {
				int f_id = layer[y][x]; 
				if ( f_id > 0) {
					if (!mCellCount.containsKey(f_id)) {
						mCellCount.put(f_id, 0);
						mTotalSlope.put(f_id, 0.0f);
					}
					mCellCount.put(f_id, mCellCount.get(f_id) + 1);
					mTotalSlope.put(f_id, mTotalSlope.get(f_id) + cornYield[y][x]);	
				}
			}
		}

		for (Entry<Integer, Integer> es: mCellCount.entrySet()) {
			mTotalSlope.put(es.getKey(), mTotalSlope.get(es.getKey()) / es.getValue());
		}
*/
	}
	
}
