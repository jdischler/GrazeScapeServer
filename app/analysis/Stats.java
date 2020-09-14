package analysis;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import analysis.Histogram;
import models.transform.UnitConvert;
import query.Layer_Float;
import utils.Json;

//---------------------------------------------
public class Stats {
//---------------------------------------------
	
    private static final Logger logger = LoggerFactory.getLogger("app");

	int mConsideredCells = 0;
	int mCellsCounted = 0;   
	int mNoDataCount = 0;
	
	float mMin = Float.POSITIVE_INFINITY, mMax = Float.NEGATIVE_INFINITY;
	double mSum = 0.0;
	
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
		if (Layer_Float.isNoDataValue(element)) {
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
	
	//---------------------------------------------
	public void debug() {
		debug(null);
	}
	
	//---------------------------------------------
	public void debug(Boolean includeArea) {
		Boolean computeArea = includeArea == null ? true : includeArea;

		if (hasStatistics()) {
			String results = "";
			try {
				Integer noDataCt = getNoDataCount();
				Float noDataPerc = getFractionNoData();

				results += "\n─────────────────────────────────────────────────────\n" +
					"¤STATISTICS for Data: \n" +
					"  NoDataCells: " + noDataCt + "\n" +
					"  NoData%:     " + String.format("%.1f%%", noDataPerc * 100) + "\n";
			
				Integer histogramCt = 40;
				Histogram hs = getHistogram(histogramCt, getMin(), getMax()); 
				Double sum = getSum();
				Float mmin = getMin();
				Float mmax = getMax();
				Float mmid = (mmin + mmax) * 0.5f;
				Float mean = getMean();
				Float median = getMedian();
				if (computeArea) {
					UnitConvert msq2ac = new UnitConvert("meters-squared-to-acres");
					UnitConvert msq2ha = new UnitConvert("meters-squared-to-hectares");
					Integer ct = getCounted();
					Float area = ct * 100.0f; // turn into meters sqr
					results += " »Area CELLS: " + ct + "\n";
					results += String.format("  Area: %.2f(ac)   %.2f(ha) \n", msq2ac.apply(area), msq2ha.apply(area));
				}
				
				results += " »Value STATS \n";
				results += String.format("  Total Value: %.2f\n", sum);
				results += String.format("  Min: %.2f  Mid: %.2f  Max: %.2f \n", mmin, mmid, mmax);
				results += String.format("  Mean: %.2f\n", mean);
				results += String.format("  Median: %.2f\n", median);
				results += " »HISTOGRAM (" + histogramCt + " bins) \n";
				results += hs.toString();
				results += "─────────────────────────────────────────────────────\n";
				
				logger.info(results);
			}
			catch(Exception e) {
				results += "....exception encountered:";
				results += e.toString();
				logger.error(results);
			}
		}
		else {
			logger.warn(" Stats: no statistics available");
		}
	}
	
	//---------------------------------------------
	public JsonNode toJson(Boolean includeArea) {
		Boolean computeArea = includeArea == null ? true : includeArea;

		ObjectNode stats = Json.newPack();
		
		if (hasStatistics()) {
			try {
				Histogram hs = getHistogram(20, getMin(), getMax()); 
				Json.addToPack(stats, 
						"no-data-ct", getNoDataCount(), 
						"no-data-perc", getFractionNoData(),
						"min", getMin(),
						"max", getMax(),
						"sum", getSum(),
						"mean", getMean(),
						"median", getMedian(),
						"histogram", hs.toJson());
			
				if (computeArea) {
					UnitConvert msq2ha = new UnitConvert("meters-squared-to-hectares");
					Float area = getCounted() * 100.0f; // turn into meters sqr
					Json.addToPack(stats, "area", msq2ha.apply(area));
				}
			}
			catch(Exception e) {
				logger.error("Stats:toJson exception");
				logger.error(e.toString());
			}
		}
		else {
			logger.warn(" Stats: no statistics available");
		}
		return stats;
	}	
}
