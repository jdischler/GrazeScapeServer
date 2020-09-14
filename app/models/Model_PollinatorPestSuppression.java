package models;

import query.Layer_CDL;
import query.Layer_Float;
import query.Layer_Integer;
import utils.PerformanceTimer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import analysis.windowing.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses landscape proportion to calculate pollinator visitation index (Grass and Forest) 
// using 1500m rectangle buffers 
// Visitation Index can vary from 0 to 18, after normalization it goes 0 to 1
// This model is from unpublished work by Ashley Bennett from Michigan State University
// Inputs are proportion of land cover particularly grass and forest, selected cells in the raster map and crop rotation layer 
// ASCII map of visitation index
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_PollinatorPestSuppression extends Model_Base {

    private static final Logger logger = LoggerFactory.getLogger("app");
	
	private static final boolean SELF_DEBUG_LOGGING = false;

	private static final int mWindowSizeMeters = 1500;
	private static final int mWindowSizeInCells = mWindowSizeMeters / 30; // Number of Cells in Raster Map
	
	//--------------------------------------------------------------------------
	public void run() {

		int[][] rotationData = null;
		
		debugLog(">>> Computing Model Pest/ Pollinator");
		PerformanceTimer timer = new PerformanceTimer();
		
		Layer_Integer cdl = Layer_CDL.get();
		int width = cdl.getWidth(), height = cdl.getHeight();
		
		int grassMask = cdl.stringToMask("hay","pasture","cool-season grass","warm-season grass");	
		int totalMask = cdl.stringToMask("hay","pasture","cool-season grass","warm-season grass",	
				"continuous corn","cash grain","dairy rotation","other crops");
		
		// full raster save process...
		float [][] pestData = new float[height][width];
		float [][] pollinatorData = new float[height][width];
		
		Moving_CDL_Window win = (Moving_CDL_Window) new Moving_CDL_Window_N(mWindowSizeInCells).initialize();
		Moving_Window.WindowPoint point;

		// derived from pollinatorIndex formula. 
		float max = (float)Math.pow(2.8f, 2.0f);
		
		boolean moreCells = true;
		while (moreCells) {
			
			point = win.getPoint();
			if ((rotationData[point.mY][point.mX] & totalMask) > 0 && win.canGetProportions()) {
				
				float proportionForest = win.getProportionForest();
				float proportionGrass = win.getProportionGrass();
				
				// Calculate visitation index and normalize value by max
				float pollinatorIndex = (float)Math.pow((proportionForest * proportionGrass) * 3.0f 
							+ (2.5f * proportionForest) 
							+ (proportionGrass), 2.0f);
				
				pollinatorData[point.mY][point.mX] = pollinatorIndex / max;
				
				// Crop type is zero for Ag, Crop type is 1 for grass
				float cropType = 0.0f;
				if ((rotationData[point.mY][point.mX] & grassMask) > 0) {
					cropType = 1.0f;
				}
					
				// Pest suppression calculation
				float pestSuppression = 0.24f + (0.18f * cropType) + (0.58f * proportionGrass);
	
				pestData[point.mY][point.mX] = pestSuppression;
			}
			else {
				pollinatorData[point.mY][point.mX] = Layer_Float.getNoDataValue();
				pestData[point.mY][point.mX] = Layer_Float.getNoDataValue();
			}

			
			moreCells = win.advance();
		}	
	
		debugLog(">>> Model_PollinatorPestSuppression_New is finished - timing (ms): " + timer.stringMilliseconds(2));

		return;
	}	
	
	//-------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private static final void debugLog(String conditionalLog) {
		
		if (ALL_MODELS_DEBUG_LOGGING || SELF_DEBUG_LOGGING) {
			logger.debug(conditionalLog);
		}
	}
}
