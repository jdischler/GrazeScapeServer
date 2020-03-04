package analysis.windowing;

// This class calculate proportion of Ag, forest and grass with the user specified rectangle buffer 
// Inputs are location of cell, window size
// Output proportion of Ag, Forest, Grass

// It uses a cool idea by Amin to zigzag across the cells, subtracting only the old cells
//	that fall out of the moving window...and adding in the new cells at the leading edge
//	of the moving window.

// Since the n window manages the irregular movement process, it returns the coordinates of
//	where it is at to the caller. The caller must only issue a call to advance to move the window
// Version 08/20/2013

// Simple usage example
/*
Moving_CDL_Window win = new Moving_Z_CDL_Window(windowSize, rasterWidth, rasterHeight).initialize();

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
public final class Moving_CDL_Window_N extends Moving_CDL_Window
{
	private boolean mbMovingUp = false; 		// set when the window should be moving UP
	private boolean mbShouldAdvance_X = false; 	// set when the next move should be a RIGHT movement
	
	//--------------------------------------------------------------------------
	public Moving_CDL_Window_N(int win_sz, int raster_w, int raster_h) {
		super(win_sz, raster_w, raster_h);
	}
	
	// Each call to run advances one cell in the direction the Z-win is moving in...
	//	since it uses a somewhat irregular pattern to move, this function will
	// 	return a class with the X, Y coordinates for where the Z_Window is at...
	// Returns FALSE the Z_Window is finished processing all cells in the raster...
	//--------------------------------------------------------------------------
	public final boolean advance() {
		
		//-----------
		// SUBTRACT
		//-----------
		// subtracts OLD cells in the proper direction based on our movement
		if (!mbShouldAdvance_X) { // moving up/down
			int windowReal_Y;
			if (mbMovingUp) {
				// we are moving UP, so we would subtract off the BOTTOM edge if needed
				windowReal_Y = mAt_Y + mHalfWindowSize;
			}
			else {
				// we are moving DOWN, so we need to subtract off the TOP edge if needed
				windowReal_Y = mAt_Y - mHalfWindowSize;
			}
			// if the real (non clipped) Y value is in a valid array location, subtract...
			if (windowReal_Y >= 0 && windowReal_Y < mRasterHeight) {
				for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
					int cellValue = mRasterData[windowReal_Y][x];
					if (cellValue != 0) {
						mTotal--;
						
						// Calculate count of land cover in the given moving window
						if ((cellValue & mAgMask) > 0) {
							mCountAg--;	
						}
						else if ((cellValue & mGrassMask) > 0) {
							mCountGrass--;
						}
						else if ((cellValue & mForestMask) > 0) {
							mCountForest--;
						}
						else if ((cellValue & mDevelopedMask) > 0) {
							mCountDeveloped--;
						}
						else if ((cellValue & mWaterMask) > 0) {
							mCountWater--;
						}
					}
				}
			}			
		}
		else {
			// we are moving RIGHT - remove old cells off the LEFT of the window if needed
			//	this is needed when the REAL left of the window is validly IN the raster array (vs. CLIPPED off)
			int windowRealTop_X = mAt_X - mHalfWindowSize;
			if (windowRealTop_X >= 0) {
				for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
					int cellValue = mRasterData[y][windowRealTop_X]; 
					if (cellValue != 0) {
						mTotal--;
						
						// Calculate count of land cover in the given moving window
						if ((cellValue & mAgMask) > 0) {
							mCountAg--;	
						}
						else if ((cellValue & mGrassMask) > 0) {
							mCountGrass--;
						}
						else if ((cellValue & mForestMask) > 0) {
							mCountForest--;
						}
						else if ((cellValue & mDevelopedMask) > 0) {
							mCountDeveloped--;
						}
						else if ((cellValue & mWaterMask) > 0) {
							mCountWater--;
						}
					}
				}
			}
		}

		//------
		// ADD
		//------
		// adds NEW cells in the proper direction based on our movement
		if (!mbShouldAdvance_X) {
			if (mbMovingUp) {
				mAt_Y--;
				if (mAt_Y <= 0) { // check for need to switch direction...or move in current direction
					mbShouldAdvance_X = true;
				}
			}
			else {
				mAt_Y++;
				if (mAt_Y >= mRasterHeight - 1) { // check for need to switch direction
					mbShouldAdvance_X = true;
				}
			}
			
			int windowReal_Y;
			if (mbMovingUp) {
				// we are moving UP, so we would ADD on the DOWN edge if needed
				windowReal_Y = mAt_Y - mHalfWindowSize;
			}
			else {
				// we are moving DOWN, so we need to add on the UP edge if needed
				windowReal_Y = mAt_Y + mHalfWindowSize;
			}
			// if the real (non clipped) Y value is in a valid array location, subtract...
			if (windowReal_Y >= 0 && windowReal_Y < mRasterHeight) {
				for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
					int cellValue = mRasterData[windowReal_Y][x];
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
			updateBoundsMoving_Y();
		}
		else {
			mbShouldAdvance_X = false; // we only ever move down once per line, turn off move down flag
			mbMovingUp = !mbMovingUp; // also change direction for the next advance call
		
			mAt_X++;
			if (mAt_X >= mRasterWidth) {
				// process is done...signal back to caller that there is no valid point
				return false;
			}
			
			// we are moving DOWN - ADD new cells on the BOTTOM of the window if needed
			//	this is needed when the REAL bottom of the window is validly IN the raster array (vs. CLIPPED off)
			int windowRealBottom_X = mAt_X + mHalfWindowSize;
			if (windowRealBottom_X < mRasterWidth) {
//				for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
				// NOTE: Going in reverse order seems to be moderately more cache friendly.
				for (int y = mLowRight_Y; y >= mUpLeft_Y; y--) {
					int cellValue = mRasterData[y][windowRealBottom_X];
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
			updateBoundsMoving_X();
		}
		
		return true;
	}	
}

