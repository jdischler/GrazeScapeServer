package query;

import raster.Extents;

public class Filters {

	// FLOAT filters
	//--------------------------------------------------------------------------------------------
	
	// TO AGRICULTURE
	
	// NOTE: DATA is modified so it should be a copy....
	//--------------------------------------------------------------------------------------------
	public static final float[][] restrictToAgriculture(float [][]data, Extents ext, Boolean restrictToRowCrops, 
			Boolean restrictToGrasses) {
		
		Layer_Integer wl = Layer_CDL.get();
		final int wl_data[][] = wl.getIntData();
		final int road_mask[][] = Layer_Base.getLayer("road_mask").getIntData();
		final int water_mask[][] = Layer_Base.getLayer("water_mask").getIntData();
		
		int mask = 0;
		if (restrictToRowCrops) {
			mask |= wl.stringToMask("Cash Grain","Continuous Corn","Dairy Rotation","Other Crops");
		}
		if (restrictToGrasses) {
			mask |= wl.stringToMask("Hay","Pasture","Reed Canary Grass","Cool-Season Grass","Warm-Season Grass");
		}
		
		for (int y = ext.y2(); y < ext.y1(); y++) {
			for (int x = ext.x1(); x < ext.x2(); x++) {
				if ((wl_data[y][x] & mask) <= 0) data[y][x] = Layer_Float.getNoDataValue();
				else if (road_mask[y][x] > 0 || water_mask[y][x] > 0) data[y][x] = Layer_Float.getNoDataValue();
			}
		}
		
		return data;
	}

	// NOTE: when compareAsLessThan = False, comparison is greaterThan 
	// NOTE: DATA is modified so it should be a copy....
	//--------------------------------------------------------------------------------------------
	public static final float[][] restrictToSlope(float [][]data, Extents ext, Boolean compareAsLessThan, Float slopeValue) {
		
		float slope[][] = Layer_Base.getFloatData("slope");
		
		for (int y = ext.y2(); y < ext.y1(); y++) {
			for (int x = ext.x1(); x < ext.x2(); x++) {
				if (compareAsLessThan && slope[y][x] >= slopeValue) {
					data[y][x] = Layer_Float.getNoDataValue();
				}
				else if (!compareAsLessThan && slope[y][x] <= slopeValue) {
					data[y][x] = Layer_Float.getNoDataValue();
				}
			}
		}
		
		return data;
	}

	// NOTE: when compareAsLessThan = False, comparison is greaterThan
	// NOTE: when clamped, exceptional values are turned into NoData
	// NOTE: DATA is modified so it should be a copy....
	//--------------------------------------------------------------------------------------------
	public static final float[][] restrictToValue(float [][] data, Extents ext, Boolean compareAsLessThan, Float value, Boolean clamped) {
		if (clamped) {
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					if (compareAsLessThan && data[y][x] >= value) {
						data[y][x] = value;
					}
					else if (!compareAsLessThan && data[y][x] <= value) {
						data[y][x] = value;
					}
				}
			}
		}
		else {
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					if (compareAsLessThan && data[y][x] >= value) {
						data[y][x] = Layer_Float.getNoDataValue();
					}
					else if (!compareAsLessThan && data[y][x] <= value) {
						data[y][x] = Layer_Float.getNoDataValue();
					}
				}
			}
		}
		return data;
	}
	
	
	// BYTE filters
	//--------------------------------------------------------------------------------------------
	

	// TO AGRICULTURE
	
	// NOTE: DATA is modified so it should be a copy....
	//--------------------------------------------------------------------------------------------
	public static final byte[][] restrictToAgriculture(byte [][]data, Extents ext, Boolean restrictToRowCrops, 
			Boolean restrictToGrasses) {

		// NOTE: y1 and y2 are flipped because of OpenLayers conventions
		return restrictToAgriculture(data, ext.x1(), ext.x2(), ext.y2(), ext.y1(), restrictToRowCrops, restrictToGrasses);
	}

	// NOTE: DATA is modified so it should be a copy....
	//--------------------------------------------------------------------------------------------
	public static final byte[][] restrictToAgriculture(byte [][]data, Integer width, Integer height, Boolean restrictToRowCrops, 
			Boolean restrictToGrasses) {
		
		return restrictToAgriculture(data, 0, width, 0, height, restrictToRowCrops, restrictToGrasses);
	}
	
	// NOTE: DATA is modified so it should be a copy....
	//--------------------------------------------------------------------------------------------
	public static final byte[][] restrictToAgriculture(byte [][]data, Integer x1, Integer x2, Integer y1, Integer y2, 
			Boolean restrictToRowCrops, Boolean restrictToGrasses) {
		
		Layer_Integer wl = Layer_CDL.get();
		final int wl_data[][] = wl.getIntData();
		final int road_mask[][] = Layer_Base.getLayer("road_mask").getIntData();
		final int water_mask[][] = Layer_Base.getLayer("water_mask").getIntData();
		
		int mask = 0;
		if (restrictToRowCrops) {
			mask |= wl.stringToMask("Cash Grain","Continuous Corn","Dairy Rotation","Other Crops");
		}
		if (restrictToGrasses) {
			mask |= wl.stringToMask("Hay","Pasture","Reed Canary Grass","Cool-Season Grass","Warm-Season Grass");
		}
		
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				if ((wl_data[y][x] & mask) <= 0) data[y][x] = 0;
				else if (road_mask[y][x] > 0 || water_mask[y][x] > 0) data[y][x] = 0;
			}
		}
		
		return data;
	}

	// NOTE: when compareAsLessThan = False, comparison is greaterThan 
	// NOTE: DATA is modified so it should be a copy....
	//--------------------------------------------------------------------------------------------
	public static final byte[][] restrictToSlope(byte [][]data, Extents ext, Boolean compareAsLessThan, Float slopeValue) {
		
		return restrictToSlope(data, ext.x1(), ext.x2(), ext.y2(), ext.y1(), compareAsLessThan, slopeValue);
	}

	// NOTE: when compareAsLessThan = False, comparison is greaterThan 
	// NOTE: DATA is modified so it should be a copy....
	//--------------------------------------------------------------------------------------------
	public static final byte[][] restrictToSlope(byte [][]data, Integer width, Integer height, 
			Boolean compareAsLessThan, Float slopeValue) {
		
		// NOTE: y1 and y2 are flipped because of OpenLayers conventions
		return restrictToSlope(data, 0, width, 0, height, compareAsLessThan, slopeValue);
	}
	
	// NOTE: when compareAsLessThan = False, comparison is greaterThan 
	// NOTE: DATA is modified so it should be a copy....
	//--------------------------------------------------------------------------------------------
	public static final byte[][] restrictToSlope(byte [][]data, Integer x1, Integer x2, Integer y1, Integer y2,
			Boolean compareAsLessThan, Float slopeValue) {
		
		float slope[][] = Layer_Base.getFloatData("slope");
		
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				if (compareAsLessThan && slope[y][x] >= slopeValue) {
					data[y][x] = Layer_Integer.getByteNoDataValue();
				}
				else if (!compareAsLessThan && slope[y][x] <= slopeValue) {
					data[y][x] = Layer_Integer.getByteNoDataValue();
				}
			}
		}
		
		return data;
	}

}