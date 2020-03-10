package analysis;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//------------------------------------------------------------------------------
public class AreaStats
{
    private static final Logger logger = LoggerFactory.getLogger("app");


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
    private int 		mAtX = 0, mAtY = 0;
    private int			mWidth = 1500, mHeight = 2600;
    
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
    	mAtX = x; mAtY = y; mWidth = width; mHeight = height;
    	return this;
    }
    
    //----------------------------------------------------------------
    public AreaStats compute() {

		if (mFieldIDs == null) {
			
			Stats s = new Stats(true);
			mFieldStats.put(0L, s);
			
	    	for (int y = 0; y < mHeight; y++) {
	        	for (int x = 0; x < mWidth; x++) {
	    			s.record(mFieldData[y+mAtY][x+mAtX]);
	        	}
	    	}
		}
		else {
	    	for (int y = 0; y < mHeight; y++) {
	        	for (int x = 0; x < mWidth; x++) {
	        		
	        		int f_id = mFieldIDs[y+mAtY][x+mAtX];
	        		if (f_id <= 0) continue;
	        		
	    			Stats s = mFieldStats.get((long)f_id);
	    			if (s == null) {
	    				s = new Stats(true);
	    				mFieldStats.put((long) f_id, s);
	    			}
	        		
	    			s.record(mFieldData[y+mAtY][x+mAtX]);
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
    
    //----------------------------------------------------------------
    public Stats getAreaStats() throws Exception {
		if (mFieldIDs != null) throw new Exception("AreaStats: was configured for field level compution so total area stats are not available");
		return mFieldStats.get(0L);
    }
    
}
