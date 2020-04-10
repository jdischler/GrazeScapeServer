package raster;

public class RasterInt {
	
	// TODO: These are better here than being in Extents (probably) but still should really 
	//	be in some kind of configuration/settings object? Maybe?
	//-------------------------------------------------------------------
	final public static	Integer mAreaExtents[] = {
		440000, 314000,
		455000, 340000
	};
	
	final public Integer mWidth = 1500, mHeight = 2600;
	//-------------------------------------------------------------------
	
	private Integer mNoData = -9999;
	
	private Extents mSubset;
	private int[][] mData;
	
	
	public static Extents toRasterSpace(Extents ext) {
		Double mExtent[] = {0.0,1.0,2.0,3.0};
		
//		logger.debug(String.format("XY1: x1<%.2f> y1<%.2f>", mExtent[0], mExtent[1]));
//		logger.debug(String.format("XY2: x2<%.2f> y2<%.2f>", mExtent[2], mExtent[3]));
		Integer x1, y1, x2, y2;
		
		// Align to 10m units
		x1 = (int) (Math.round(mExtent[0] / 10.0) * 10);
		y1 = (int) (Math.round(mExtent[1] / 10.0) * 10);
		x2 = (int) (Math.round(mExtent[2] / 10.0) * 10);
		y2 = (int) (Math.round(mExtent[3] / 10.0) * 10);
		
		// Clip to raster-supported extents
		if (x1 < mAreaExtents[0]) 		x1 = mAreaExtents[0];
		else if (x1 > mAreaExtents[2]) 	x1 = mAreaExtents[2];
		
		if (x2 < mAreaExtents[0]) 		x2 = mAreaExtents[0];
		else if (x2 > mAreaExtents[2]) 	x2 = mAreaExtents[2];
	
		if (y1 < mAreaExtents[1]) 		y1 = mAreaExtents[1];
		else if (y1 > mAreaExtents[3]) 	y1 = mAreaExtents[3];
		
		if (y2 < mAreaExtents[1]) 		y2 = mAreaExtents[1];
		else if (y2 > mAreaExtents[3]) 	y2 = mAreaExtents[3];

		mExtent[0] = x1.doubleValue();
		mExtent[1] = y1.doubleValue();
		mExtent[2] = x2.doubleValue();
		mExtent[3] = y2.doubleValue();
		
		// re-index
		x1 = (x1 - mAreaExtents[0]) / 10;
		y1 = -(y1 - mAreaExtents[3]) / 10;
		
		x2 = (x2 - mAreaExtents[0]) / 10;
		y2 = -(y2 - mAreaExtents[3]) / 10;
		
		Extents newExt = new Extents(x1, y1, x2, y2);
		return newExt;
	}
}