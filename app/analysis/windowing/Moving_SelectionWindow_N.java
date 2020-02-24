package analysis.windowing;

//This class calculate proportion of selected cells in a selection 
//Inputs are location of cell, window size
//Output is proportion of selection

//Since the z window manages the irregular movement process, it returns the coordinates of
//	where it is at to the caller. The caller must only issue a call to advance to move the window

//Simple usage example
/*
Moving_SelectionWindow win = new Moving_N_SelectionWindow(windowSize, selectionData, selWidth, selHeight);

boolean moreCells = true;
while (!moreCells) {
	
	WindowPoint point = win.getPoint();
	if (win.canGetProportion()) {
		float selectionProp = win.getProportion();
		someDataArray[point.mY][point.mX] = selectionProp;
	}
	
	moreCells = win.advance();
}
*/

//------------------------------------------------------------------------------
public final class Moving_SelectionWindow_N extends Moving_SelectionWindow
{
	private boolean mbMovingUp; // set when the window should be moving UP
	private boolean mbShouldAdvance_X; // set when the next move should be a RIGHT movement
	
	// Define moving N window - it assumes starting at (x,y) = (0,0) - but that could be changed if needed
	//--------------------------------------------------------------------------
	public Moving_SelectionWindow_N(int win_sz, byte [][] rasterData, int raster_w, int raster_h) {
		
		super(win_sz, rasterData, raster_w, raster_h);

		mbMovingUp = false; // window moves down first...
		mbShouldAdvance_X = false; // only gets set once per line once an edge is hit
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
					byte cellValue = mRasterData[windowReal_Y][x];
					mTotal--;
					if (cellValue > 0) {
						mCountSelection--;
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
					byte cellValue = mRasterData[y][windowRealTop_X]; 
					mTotal--;
					if (cellValue > 0) {
						mCountSelection--;
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
					byte cellValue = mRasterData[windowReal_Y][x];
					mTotal++;
					if (cellValue > 0) {
						mCountSelection++;
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
					byte cellValue = mRasterData[y][windowRealBottom_X];
					mTotal++;
					if (cellValue > 0) {
						mCountSelection++;
					}
				}
			}
			updateBoundsMoving_X();
		}
		
		return true;
	}	
}

