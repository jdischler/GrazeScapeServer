package analysis.windowing;

import query.Layer_CDL;
import query.Layer_Integer;

// This base class for CDL based rectangular moving window analysis
//	It calculates proportion of Ag, forest, grass, water, and developed with the user specified rectangle buffer
//
// Inputs are location of cell, window size
// Output proportion of Ag, Forest, Grass, Developed, Water (inc. wetlands)

// It uses a cool idea by Amin to zigzag across the cells, subtracting only the old cells
//	that fall out of the moving window...and adding in the new cells at the leading edge
//	of the moving window.

// Version 08/20/2013

// Simple usage example
/*
Moving_CDL_Window win = new Moving_CDL_Window_Z(windowSize, rasterWidth, rasterHeight).initialize();

boolean moreCells = true;
while (!moreCells) {
	
	Moving_Window.WindowPoint point = win.getPoint();
	if (win.canGetProportions()) {
		float agProp = win.getProportionAg();
		someDataArray[point.mY][point.mX] = agProp;
	}
	
	moreCells = win.advance();
}
*/

//------------------------------------------------------------------------------
public abstract class Moving_CDL_Window extends Moving_Window
{
	public Moving_CDL_Window(int win_sz, int raster_w, int raster_h) {
		super(win_sz, raster_w, raster_h);
	}

	protected int[][] mRasterData;
	
	protected int mCountAg, mAgMask;
	protected int mCountForest, mForestMask;
	protected int mCountGrass, mGrassMask;
	protected int mCountDeveloped, mDevelopedMask;
	protected int mCountWater, mWaterMask; // NOTE: includes wetlands
	
	//--------------------------------------------------------------------------
	public Moving_Window initialize() {
		
		super.initialize();
		
		Layer_Integer cdl = Layer_CDL.get();
		
		mGrassMask = cdl.stringToMask("Hay","Pasture","Reed Canary Grass","Cool-Season Grass","Warm-Season Grass");	
		mForestMask = cdl.stringToMask("Deciduous/Coniferous Forest","Conifers","Deciduous","Forested Wetland");
		mAgMask = cdl.stringToMask("Cash Grain","Continuous Corn","Dairy Rotation","Other Crops");
		mDevelopedMask = cdl.stringToMask("Developed High Intensity", "Developed Low Intensity");
		mWaterMask = cdl.stringToMask("Open Water");

		mRasterData = cdl.getIntData();
		
		initCounts();
		
		return this;
	}
	
	// Called internally off the constructor
	//--------------------------------------------------------------------------
	protected void initCounts() {
		
		mTotal = 0;	
		
		for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
			for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
				int cellValue = mRasterData[y][x]; 
				if (cellValue != 0) {
					mTotal++;
					
					// Calculate count of land cover in the given moving window
					if ((cellValue & mAgMask) > 0) {
						mCountAg++;	
					}
					else if ((cellValue & mGrassMask) > 0) {
						mCountGrass++;
					}
					else if ((cellValue & mForestMask) > 0) {
						mCountForest++;
					}
					else if ((cellValue & mDevelopedMask) > 0) {
						mCountDeveloped++;
					}
					else if ((cellValue & mWaterMask) > 0) {
						mCountWater++;
					}
				}
			}
		}
	}
	
	//--------------------------------------------------------------------------
	public final int getWindowCenterValue() {
		return mRasterData[mAt_Y][mAt_X];
		
	}
	// Each call to run advances one cell in the direction the Z-win is moving in...
	//	since it uses a somewhat irregular pattern to move, this function will
	// 	return a class with the X, Y coordinates for where the Z_Window is at...
	// Returns FALSE the Z_Window is finished processing all cells in the raster...
	//--------------------------------------------------------------------------
	public abstract boolean advance();
	
	//--------------------------------------------------------------------------
	public final float getProportionAg() {
		return (float)mCountAg / mTotal;
	}
	
	//--------------------------------------------------------------------------
	public final float getProportionForest() {
		return (float)mCountForest / mTotal;
	}

	//--------------------------------------------------------------------------
	public final float getProportionGrass() {
		return (float)mCountGrass / mTotal;
	}
	
	//--------------------------------------------------------------------------
	public final float getProportionDeveloped() {
		return (float)mCountDeveloped / mTotal;
	}
	
	// NOTE: includes wetland
	//--------------------------------------------------------------------------
	public final float getProportionWater() {
		return (float)mCountWater / mTotal;
	}
}

