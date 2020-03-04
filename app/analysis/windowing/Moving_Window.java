package analysis.windowing;

// The base class for rectangular moving window analysis

//------------------------------------------------------------------------------
public abstract class Moving_Window
{
	// small helper class.....
	public final class WindowPoint
	{
		public int mX, mY;
		
		public WindowPoint(int x, int y) {
			mX = x;
			mY = y;
		}
	}
	
	//
	protected int mRasterWidth, mRasterHeight;
	protected int mHalfWindowSize; // half the size, in cells
	
	// Define working variables
	protected int mUpLeft_X, mUpLeft_Y;
	protected int mLowRight_X, mLowRight_Y;
	
	protected int mTotal; // total data cells in window. NoData cells are NOT counted
	
	protected int mAt_X, mAt_Y;
	// Same as raster width/height when doing a full layer sweep. Exists to allow computing a subset
	protected int mFinalX, mFinalY; 
	protected WindowPoint mPoint;

	// Defaults to full layer processing...chain restrict() to limit the extents
	//--------------------------------------------------------------------------
	public Moving_Window(int win_sz, int raster_w, int raster_h) {
		
		mRasterWidth = mFinalX = raster_w;
		mRasterHeight = mFinalY = raster_h;
		mHalfWindowSize = win_sz / 2;
		
		mAt_X = 0;
		mAt_Y = 0;	
	}
	
	// chain this on after the constructor to restrict processing to a subset
	//--------------------------------------------------------------------------
	public Moving_Window restrict(int startX, int startY, int finalX, int finalY) {
		
		mAt_X = startX;
		mAt_Y = startY;	
		mFinalX = finalX;
		mFinalY = finalY;
		
		return this;
	}

	// must be called last...
	//--------------------------------------------------------------------------
	public Moving_Window initialize() {
		
		mPoint = new WindowPoint(mAt_X, mAt_Y);
		calcWindowBounds();
		
		return this;
	}
	
	//--------------------------------------------------------------------------
	protected void calcWindowBounds() {
		
		updateBoundsMoving_X();
		updateBoundsMoving_Y();
	}
	
	//--------------------------------------------------------------------------
	protected final void updateBoundsMoving_X() {
		
		mUpLeft_X = mAt_X - mHalfWindowSize;
		mLowRight_X = mAt_X + mHalfWindowSize;
		
		if (mUpLeft_X < 0) {
			mUpLeft_X = 0;
		}
		if (mLowRight_X > mRasterWidth - 1) {
			mLowRight_X = mRasterWidth - 1;
		}
	}

	//--------------------------------------------------------------------------
	protected final void updateBoundsMoving_Y() {
		
		mUpLeft_Y = mAt_Y - mHalfWindowSize;
		mLowRight_Y = mAt_Y + mHalfWindowSize;
		
		if (mUpLeft_Y < 0) {
			mUpLeft_Y = 0;
		}
		if (mLowRight_Y > mRasterHeight - 1) {
			mLowRight_Y = mRasterHeight - 1;
		}
	}
	
	//--------------------------------------------------------------------------
	public final WindowPoint getPoint() {
		
		mPoint.mX = mAt_X;
		mPoint.mY = mAt_Y;
		return mPoint;
	}
	
	// Each call to run advances one cell in the direction the Z-win is moving in...
	//	since it uses a somewhat irregular pattern to move, this function will
	// 	return a class with the X, Y coordinates for where the Z_Window is at...
	// Returns FALSE the Z_Window is finished processing all cells in the raster...
	//--------------------------------------------------------------------------
	public abstract boolean advance();
	
	// If total cells is zero, there is no reasonable proportion we can calculate,
	//	we should check this before trying to get proportions. If false, should probably
	//	put NoData in resulting cell...
	//--------------------------------------------------------------------------
	public final boolean canGetProportions() {
		return (mTotal > 0);
	}
}

