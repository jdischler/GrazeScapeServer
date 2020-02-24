package analysis.windowing;

// This class calculate proportion of selected cells in a selection 
// Inputs are location of cell, window size
// Output is proportion of selection

// Since the z window manages the irregular movement process, it returns the coordinates of
//	where it is at to the caller. The caller must only issue a call to advance to move the window

// Simple usage example
/*
Moving_Z_SelectionWindow zWin = new Moving_Z_SelectionWindow(windowSize, selectionData, selWidth, selHeight);

boolean moreCells = true;
while (!moreCells) {
	
	WindowPoint point = zWin.getPoint();
	if (zWin.canGetProportion()) {
		float selectionProp = zWin.getProportion();
		someDataArray[point.mY][point.mX] = selectionProp;
	}
	
	moreCells = zWin.advance();
}
*/

//------------------------------------------------------------------------------
public abstract class Moving_SelectionWindow extends Moving_Window
{
	protected byte[][] mRasterData;
	protected int mCountSelection;
	
	public Moving_SelectionWindow(int win_sz, byte [][] rasterData, int raster_w, int raster_h) {
		super(win_sz, raster_w, raster_h);
		
		mRasterData = rasterData;
		
		initCounts();
	}
	
	// Called internally off the constructor
	//--------------------------------------------------------------------------
	protected void initCounts() {
		
		mTotal = 0;	
		
		for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
			for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
				byte cellValue = mRasterData[y][x]; 
				mTotal++;
				
				if (cellValue > 0) {
					mCountSelection++;
				}
			}
		}
	}
	
	//--------------------------------------------------------------------------
	public final byte getWindowCenterValue() {
		return mRasterData[mAt_Y][mAt_X];
		
	}
	// Each call to run advances one cell in the direction the Z-win is moving in...
	//	since it uses a somewhat irregular pattern to move, this function will
	// 	return a class with the X, Y coordinates for where the Z_Window is at...
	// Returns FALSE the Z_Window is finished processing all cells in the raster...
	//--------------------------------------------------------------------------
	public abstract boolean advance();
	
	//--------------------------------------------------------------------------
	public final float getProportion() {
		return (float)mCountSelection / mTotal;
	}
}

