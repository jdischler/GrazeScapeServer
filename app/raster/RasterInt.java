package raster;

import query.Layer_Integer;

// Doesn't seem to be used....Some of the code looks broken anyway
@Deprecated 
public class RasterInt {
	
	// FIXME: TODO: These are better here than being in Extents (probably) but still should really
	//	be in some kind of configuration/settings object? Maybe?
	//-------------------------------------------------------------------
	final public static	Integer mAreaExtents[] = {
		440000, 314000,
		455000, 340000
	};
	
	// FIXME: TODO: need some system to where these are defined 
	final public Integer mWidth = 1500, mHeight = 2600;
	//-------------------------------------------------------------------
	
	private Integer mNoDataValue = Layer_Integer.getIntNoDataValue();
	
	private Extents mSubset;
	private int[][] mData;
	
	
	public static Extents toRasterSpace(Extents ext) {
		
		Integer x1, y1, x2, y2;
		
		// Align to 10m units
		x1 = (int) (Math.round(ext.x1() / 10.0) * 10);
		y1 = (int) (Math.round(ext.y1() / 10.0) * 10);
		x2 = (int) (Math.round(ext.x2() / 10.0) * 10);
		y2 = (int) (Math.round(ext.y2() / 10.0) * 10);
		
		// Clip to raster-supported extents
		if (x1 < mAreaExtents[0]) 		x1 = mAreaExtents[0];
		else if (x1 > mAreaExtents[2]) 	x1 = mAreaExtents[2];
		
		if (x2 < mAreaExtents[0]) 		x2 = mAreaExtents[0];
		else if (x2 > mAreaExtents[2]) 	x2 = mAreaExtents[2];
	
		if (y1 < mAreaExtents[1]) 		y1 = mAreaExtents[1];
		else if (y1 > mAreaExtents[3]) 	y1 = mAreaExtents[3];
		
		if (y2 < mAreaExtents[1]) 		y2 = mAreaExtents[1];
		else if (y2 > mAreaExtents[3]) 	y2 = mAreaExtents[3];

		// re-index
		x1 = (x1 - mAreaExtents[0]) / 10;
		y1 = -(y1 - mAreaExtents[3]) / 10;
		
		x2 = (x2 - mAreaExtents[0]) / 10;
		y2 = -(y2 - mAreaExtents[3]) / 10;
		
		Extents newExt = new Extents(x1, y1, x2, y2);
		return newExt;
	}
}