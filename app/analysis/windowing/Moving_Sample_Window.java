package analysis.windowing;

import query.Layer_CDL;
import query.Layer_Float;
import query.Layer_Integer;

// FIXME: TODO: There seem to be a few issues with this so it would need to be better vetted before using for real
//	Primary problems are "Infinity" "NAN" results coming through
//  Also the top and left edges seem to be counting out-of-bounds cells, possibly zeroes, which 
//		causes incorrect edges for subset work.
//------------------------------------------------------------------------------
public abstract class Moving_Sample_Window extends Moving_Window
{
	public Moving_Sample_Window(int win_sz, float [][] rasterData) {
		super(win_sz);
		
		// TODO: fixme
		Layer_Integer cdl = Layer_CDL.get();
		mRasterWidth = mSubsetMaxX = cdl.getWidth();
		mRasterHeight = mSubsetMaxY = cdl.getHeight();
		
		mRasterData = rasterData;
	}

	protected float [][] mRasterData;
	protected Double mInternalSum;
	protected Integer mTotal;
	
	//--------------------------------------------------------------------------
	public Moving_Window initialize() {
		
		super.initialize();
		
		initCounts();
		
		return this;
	}
	
	// Called internally only
	//--------------------------------------------------------------------------
	protected void initCounts() {
		
		mTotal = 0;
		mInternalSum = 0.0;
		
		for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
			for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
				float cellValue = mRasterData[y][x];
				if (!Layer_Float.isNoDataValue(cellValue)) {
					mInternalSum +=  cellValue; 
					mTotal++;
				}
			}
		}
	}
	
	//--------------------------------------------------------------------------
	@Override
	protected void updateBoundsMoving_X() {
		
		mUpLeft_X = mAt_X - mHalfWindowSize;
		mLowRight_X = mAt_X + mHalfWindowSize;
		
		if (mUpLeft_X < mSubsetMinX) {
			mUpLeft_X = mSubsetMinX;
		}
		if (mLowRight_X > mSubsetMaxX - 1) {
			mLowRight_X = mSubsetMaxX - 1;
		}
	}

	//--------------------------------------------------------------------------
	@Override
	protected void updateBoundsMoving_Y() {
		
		mUpLeft_Y = mAt_Y - mHalfWindowSize;
		mLowRight_Y = mAt_Y + mHalfWindowSize;
		
		if (mUpLeft_Y < mSubsetMinY) {
			mUpLeft_Y = mSubsetMinY;
		}
		if (mLowRight_Y > mSubsetMaxY - 1) {
			mLowRight_Y = mSubsetMaxY - 1;
		}
	}	
	// Each call to run advances one cell in the direction the Z-win is moving in...
	//	since it uses a somewhat irregular pattern to move, this function will
	// 	return a class with the X, Y coordinates for where the Z_Window is at...
	// Returns FALSE the Z_Window is finished processing all cells in the raster...
	//--------------------------------------------------------------------------
	public abstract boolean advance();
	
	//--------------------------------------------------------------------------
	public final float getResampled() {
		return ((Double)(mInternalSum / mTotal.doubleValue())).floatValue();
	}
}

