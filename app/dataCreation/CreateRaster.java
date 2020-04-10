package dataCreation;

import java.util.Map;

import query.Layer_Float;
import raster.Extents;

//---------------------------------------------------------------
public class CreateRaster {
	
	private Extents mExtents;
	private int mHeight = 2600, mWidth = 1500;
	private int [][] mFieldIDs = null;
	private Map<Integer,Float> mFieldDataMap = null;
	private Float mDefaultValue = null;
	private float [][] mDefaultRaster = null;
	private Float mNoDataValue = -9999.0f;
	
	public CreateRaster(Extents ext) {
		mExtents = ext;
	}
	
	//---------------------------------------------------------------
	public CreateRaster bindFields(int [][] fieldIDs) {
		mFieldIDs = fieldIDs; return this;
	}

	//---------------------------------------------------------------
	public CreateRaster bindFieldValues(Map<Integer,Float> fieldDataMap) {
		mFieldDataMap = fieldDataMap; return this;
	}
		
	//---------------------------------------------------------------
	public CreateRaster setDefault(Float defaultValue) {
		mDefaultValue = defaultValue; return this;
	}
	
	// When exists, trumps defaultFloatValue. If raster has noData for a cell, defaultFloatValue would be used
	//---------------------------------------------------------------
	public CreateRaster setDefault(float [][] defaultRaster) {
		mDefaultRaster = defaultRaster; return this;
	}
	
	//---------------------------------------------------------------
	public CreateRaster setNoDataValue(float noDataValue) {
		mNoDataValue = noDataValue; return this;
	}
	
	//---------------------------------------------------------------
	public float [][] process() {
		
		float [][] raster = new float[mHeight][mWidth];
		
		// Ensure we can always use default value, even if it still ends up being a no data value
		if (mDefaultValue == null) mDefaultValue = mNoDataValue;
		
		if (mFieldIDs == null || mFieldDataMap == null) {
			
			// Basically CopyRaster mode (if there is one)
			for (int y = 0; y < mHeight; y++) {
				for (int x = 0; x < mWidth; x++) {
					// If there's no default raster or it has no-data, try a default value
					if (mDefaultRaster == null || Layer_Float.isNoDataValue(mDefaultRaster[y][x])) {
						raster[y][x] = mDefaultValue;
						continue;
					}
					raster[y][x] = mDefaultRaster[y][x];
				}
			}
		}
		else {
			
			// Pull in fieldMapped data as needed. When 
			for (int y = 0; y < mHeight; y++) {
				for (int x = 0; x < mWidth; x++) {
					int fid = mFieldIDs[y][x];
					// if field id is no data or the field map doesn't contain a lookup value, should look up for backup data
					if (fid == -9999 || !mFieldDataMap.containsKey(fid)) {
						// If there's no default raster or it has no-data, try a default value
						if (mDefaultRaster == null || Layer_Float.isNoDataValue(mDefaultRaster[y][x])) {
							raster[y][x] = mDefaultValue;
							continue;
						}
						raster[y][x] = mDefaultRaster[y][x];
						continue;
					}
					raster[y][x] = mFieldDataMap.get(fid);
				}
			}
		}
		return raster;
	}
}