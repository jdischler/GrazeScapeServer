package models;

import play.*;
import query.Layer_CDL;
import query.Layer_Integer;
import utils.PerformanceTimer;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import analysis.windowing.*;

//------------------------------------------------------------------------------
public class Model_HabitatIndex extends Model_Base
{
    private static final Logger logger = LoggerFactory.getLogger("app");
	
	private static final boolean SELF_DEBUG_LOGGING = false;
	
	private static final int mWindowSizeMeters = 390;
	private static final int mWindowSizeInCells = mWindowSizeMeters / 30;
	
	
	// Define habitat index function
	//--------------------------------------------------------------------------
	public void run() {

		int[][] rotationData = null;//scenario.mNewRotation;
//		int width = scenario.getWidth(), height = scenario.getHeight();
		int width = 1250, height = 2370;
		
		debugLog(">>> Computing Model Habitat Index");
		PerformanceTimer timer = new PerformanceTimer();
		
		float [][] habitatData = new float[height][width];
		debugLog("  > Allocated memory for Habitat Index");
		
		Layer_Integer cdl = Layer_CDL.get();
		
		int totalMask = cdl.stringToMask("hay","pasture","cool-season grass","warm-season grass",	
				"continuous corn","cash grain","dairy rotation","other crops");

		// --- Model specific code starts here
		Moving_CDL_Window win = new Moving_CDL_Window_N(mWindowSizeInCells, rotationData, width, height);
		Moving_Window.WindowPoint point;
		
		boolean moreCells = true;
		while (moreCells) {
			point = win.getPoint();
			
			// If proportions are zero, don't try to get them because we'd divide by zero in doing that.
			if ((rotationData[point.mY][point.mX] & totalMask) > 0 && win.canGetProportions()) {
				float proportionAg = win.getProportionAg();
				float proportionGrass = win.getProportionGrass();
				
				// Habitat Index
				float lambda = -4.47f + (2.95f * proportionAg) + (5.17f * proportionGrass); 
				float habitatIndex = (float)((1.0f / (1.0f / Math.exp(lambda) + 1.0f )) / 0.67f);

				habitatData[point.mY][point.mX] = habitatIndex;
			}
			else {
				habitatData[point.mY][point.mX] = -9999.0f; // NO DATA
			}
			
			moreCells = win.advance();
		}		
		
		debugLog(">>> Model_Habitat_Index is finished - timing (ms): " + timer.stringMilliseconds(2));
	}	
	
	//-------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private static final void debugLog(String conditionalLog) {
		
		if (ALL_MODELS_DEBUG_LOGGING || SELF_DEBUG_LOGGING) {
			logger.debug(conditionalLog);
		}
	}
}
