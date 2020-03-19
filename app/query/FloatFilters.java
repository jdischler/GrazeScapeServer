package query;

import raster.Extents;

public class FloatFilters {
	
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
				if ((wl_data[y][x] & mask) <= 0) data[y][x] = -9999.0f;
				else if (road_mask[y][x] > 0 || water_mask[y][x] > 0) data[y][x] = -9999.0f;
			}
		}
		
		return data;
	}
}