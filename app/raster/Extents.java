package raster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import utils.Json;

// Supports extents in Double format for those use-cases but primarily exists to clip those
//	extents to the supported raster extents, and more importantly, get those extents into aligned
//	raster coordinates
//--------------------------------------------------------------------------------------------------
public class Extents {
	
    private static final Logger logger = LoggerFactory.getLogger("app");
	
	// Extents in OpenLayers order, corresponds to mX1, mY1, mX2, mY2 order below
	private Double mExtent[] = {0.0, 0.0, 0.0, 0.0};
	
	private Integer mX1, mY1, mX2, mY2;
	private Integer mWidth, mHeight;
	
	private Boolean mbRasterSafe = false;
	
	// These are stored as Integers to match the exact numbers used for raster space, 
	//	order is OpenLayers
	final public static	Integer areaExtents[] = {
		440000, 314000,
		455000, 340000
	};

	public final Integer x1() 		{ return mX1; 		}
	public final Integer y1() 		{ return mY1; 		}
	public final Integer x2() 		{ return mX2; 		}
	public final Integer y2() 		{ return mY2; 		}
	
	public final Integer width() 	{ return mWidth;	}
	public final Integer height() 	{ return mHeight;	}
	
	// Raster operations wanting to use these extents should check
	public final Boolean isRasterSafe()	{return mbRasterSafe;	}
	
	// Node is expected to be an array node populated with OpenLayers ordered values
	//	If it's null, the default area extents is returned instead.
	//-------------------------------------------------------------------------------------
	public Extents fromJson(ArrayNode extentNode) {
		
		if (extentNode != null) {
			mExtent[0] = extentNode.get(0).doubleValue();
			mExtent[1] = extentNode.get(1).doubleValue();
			mExtent[2] = extentNode.get(2).doubleValue();
			mExtent[3] = extentNode.get(3).doubleValue();
		}
		else {
			mExtent[0] = areaExtents[0].doubleValue();
			mExtent[1] = areaExtents[1].doubleValue();
			mExtent[2] = areaExtents[2].doubleValue();
			mExtent[3] = areaExtents[3].doubleValue();
		}
		
		return this;
	}
	
	public Extents() {}
	
	//-------------------------------------------------------------------------------------
	public Extents(int x1, int y1, int x2, int y2) {
		
		mX1 = x1;	mY1 = y1;
		mX2 = x2;	mY2 = y2;
		
		mWidth = x2 - x1;
		mHeight = y1 - y2;
	}
	
	//-------------------------------------------------------------------------------------
	public JsonNode toJson() {
		
		return Json.array(mExtent[0], mExtent[1],
				mExtent[2], mExtent[3]);
	}
	
	// Align extents to nearest 10m (but does not put them into raster space)
	//-------------------------------------------------------------------------------------
	public Extents align() {
		mExtent[0] = (double)(Math.round(mExtent[0] / 10.0) * 10);
		mExtent[1] = (double)(Math.round(mExtent[1] / 10.0) * 10);
		mExtent[2] = (double)(Math.round(mExtent[2] / 10.0) * 10);
		mExtent[3] = (double)(Math.round(mExtent[3] / 10.0) * 10);
		
		return this;
	}

	//-------------------------------------------------------------------------------------
	public Extents buffer(Double inMeters) {

		return this;
	}
	
	// Optional extents can be null to clip against the raster supported extents
	//-------------------------------------------------------------------------------------
	public Extents restrictTo(Extents optionalExtents) {

		if (optionalExtents == null) {
			// Clip to default area
			if (mExtent[0] < areaExtents[0]) 		mExtent[0] = areaExtents[0].doubleValue();
			else if (mExtent[0] > areaExtents[2]) 	mExtent[0] = areaExtents[2].doubleValue();
			
			if (mExtent[2] < areaExtents[0]) 		mExtent[2] = areaExtents[0].doubleValue();
			else if (mExtent[2] > areaExtents[2]) 	mExtent[2] = areaExtents[2].doubleValue();
		
			if (mExtent[1] > areaExtents[1]) 		mExtent[1] = areaExtents[1].doubleValue();
			else if (mExtent[1] < areaExtents[3]) 	mExtent[1] = areaExtents[3].doubleValue();
			
			if (mExtent[3] > areaExtents[1]) 		mExtent[3] = areaExtents[1].doubleValue();
			else if (mExtent[3] < areaExtents[3]) 	mExtent[3] = areaExtents[3].doubleValue();
		}
		else {
			Double[] otherExtent = optionalExtents.mExtent;
			if (mExtent[0] < otherExtent[0]) 		mExtent[0] = otherExtent[0];
			else if (mExtent[0] > otherExtent[2]) 	mExtent[0] = otherExtent[2];
			
			if (mExtent[2] < otherExtent[0]) 		mExtent[2] = otherExtent[0];
			else if (mExtent[2] > otherExtent[2]) 	mExtent[2] = otherExtent[2];
		
			if (mExtent[1] > otherExtent[1]) 		mExtent[1] = otherExtent[1];
			else if (mExtent[1] < otherExtent[3]) 	mExtent[1] = otherExtent[3];
			
			if (mExtent[3] > otherExtent[1]) 		mExtent[3] = otherExtent[1];
			else if (mExtent[3] < otherExtent[3]) 	mExtent[3] = otherExtent[3];
		}
		
		return this;
	}
	
	// Clips extents to raster supported extents and creates raster-safe access information 
	//-------------------------------------------------------------------------------------
	public Extents toRasterSpace() {
		
//		logger.debug(String.format("XY1: x1<%.2f> y1<%.2f>", mExtent[0], mExtent[1]));
//		logger.debug(String.format("XY2: x2<%.2f> y2<%.2f>", mExtent[2], mExtent[3]));

		// Align to 10m units
		mX1 = (int) (Math.round(mExtent[0] / 10.0) * 10);
		mY1 = (int) (Math.round(mExtent[1] / 10.0) * 10);
		mX2 = (int) (Math.round(mExtent[2] / 10.0) * 10);
		mY2 = (int) (Math.round(mExtent[3] / 10.0) * 10);
		
		// Clip to raster-supported extents
		if (mX1 < areaExtents[0]) 		mX1 = areaExtents[0];
		else if (mX1 > areaExtents[2]) 	mX1 = areaExtents[2];
		
		if (mX2 < areaExtents[0]) 		mX2 = areaExtents[0];
		else if (mX2 > areaExtents[2]) 	mX2 = areaExtents[2];
	
		if (mY1 < areaExtents[1]) 		mY1 = areaExtents[1];
		else if (mY1 > areaExtents[3]) 	mY1 = areaExtents[3];
		
		if (mY2 < areaExtents[1]) 		mY2 = areaExtents[1];
		else if (mY2 > areaExtents[3]) 	mY2 = areaExtents[3];

		mExtent[0] = mX1.doubleValue();
		mExtent[1] = mY1.doubleValue();
		mExtent[2] = mX2.doubleValue();
		mExtent[3] = mY2.doubleValue();
		
		// re-index
		mX1 = (mX1 - areaExtents[0]) / 10;
		mY1 = -(mY1 - areaExtents[3]) / 10;
		
		mX2 = (mX2 - areaExtents[0]) / 10;
		mY2 = -(mY2 - areaExtents[3]) / 10;
		
		mWidth = mX2 - mX1;
		mHeight = mY1 - mY2;
		
//		logger.debug(String.format("XY1: x1<%d> y1<%d>", mX1, mY1));
//		logger.debug(String.format("XY2: x2<%d> y2<%d>", mX2, mY2));
//		logger.debug(String.format("Width/Height: <%d, %d>", mWidth, mHeight));
		mbRasterSafe = true;
		return this;
	}

};
