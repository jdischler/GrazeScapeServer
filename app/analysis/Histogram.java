package analysis;

import java.util.*;

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
    		
    		sb.append(chartSpacer + "║ ");
    		for (String s: ch) {
    			sb.append(s);
    		}
    		sb.append(" ║\n");
		}
		if (mBins >= 20) {
			sb.append(chartSpacer + "║ ");
			for (int i = 0; i < mBins; i++) {
				if (i % 10 == 0) {
					sb.append("▏");
				}
				else {
					sb.append(" ");
				}
			}
			sb.append("▏║\n");
//			sb.append("|║\n");
//			sb.append("║\n");
		}
		
		sb.append(chartSpacer + "╚");
		for (int ct = 0; ct < mBins+2; ct++) {
			sb.append("═");
		}
		sb.append("╝\n");
		
		sb.append(String.format("     Highest bin: [%.2f -> %.2f]  %.2f(ac)\n", bestBinMin, bestBinMax, (highCt * 100.0f / 4047.0f)));
		
		return sb.toString();
	}
}
