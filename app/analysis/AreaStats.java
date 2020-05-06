package analysis;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import raster.Extents;

//------------------------------------------------------------------------------
public class AreaStats
{
    private static final Logger logger = LoggerFactory.getLogger("app");
    
    //------------------------------------------------------------
    private Map<Long,Stats> mFieldStats = null;
    
    // TODO: wrap these to reduce sending around raw 2D arrays?
    private int[][] 	mFieldIDs = null;
    private float[][]	mData = null;
    private int 		mAtX = 0, mAtY = 0;
    private int			mWidth = 1500, mHeight = 2600;
    
    //----------------------------------------------------------------
    public AreaStats(float[][] data) {
    	mFieldStats = new HashMap<>();
    	mData = data;
    	
    	// set default analysis extents
    }
    
    //----------------------------------------------------------------
    public AreaStats forRasterizedFields(int[][] fieldIDs) {
    	mFieldIDs = fieldIDs;
    	return this;
    }
    
    //----------------------------------------------------------------
    public AreaStats forExtents(int x, int y, int width, int height) {
    	mAtX = x; mAtY = y; mWidth = width; mHeight = height;
    	return this;
    }
    
    //----------------------------------------------------------------
    public AreaStats forExtents(Extents ext) {
    	mAtX = ext.x1();
    	mAtY = ext.y2();
    	mWidth = ext.width();
    	mHeight = ext.height();	
    	return this;
    }
    
    //----------------------------------------------------------------
    public AreaStats compute() {

    	// Always compute total stats, which can be accessed via get AreaStats
		Stats as = new Stats(true);
		mFieldStats.put(0L, as);
		
    	for (int y = 0; y < mHeight; y++) {
        	for (int x = 0; x < mWidth; x++) {
    			as.record(mData[y+mAtY][x+mAtX]);
        	}
    	}
    	
    	// Additionally get field stats when configured to do so...
		if (mFieldIDs != null) {
	    	for (int y = 0; y < mHeight; y++) {
	        	for (int x = 0; x < mWidth; x++) {
	        		
	        		int f_id = mFieldIDs[y+mAtY][x+mAtX];
	        		if (f_id <= 0) continue;
	        		
	    			Stats s = mFieldStats.get((long)f_id);
	    			if (s == null) {
	    				s = new Stats(true);
	    				mFieldStats.put((long) f_id, s);
	    			}
	        		
	    			s.record(mData[y+mAtY][x+mAtX]);
	        	}
	    	}
		}
    	return this;
    }
    
    //----------------------------------------------------------------
    public Set<Long> getFieldKeys() {
    	return mFieldStats.keySet();
    }
    
    //----------------------------------------------------------------
    public Stats getFieldStats(Long fs_idx) throws Exception {
		
		if (mFieldIDs == null) throw new Exception("AreaStats: was not configured for field level compution");
		return mFieldStats.get(fs_idx);
    }
    
    //----------------------------------------------------------------
    public Stats getAreaStats() throws Exception {
	//	if (mFieldIDs != null) throw new Exception("AreaStats: was configured for field level compution so total area stats are not available");
		return mFieldStats.get(0L);
    }
    
}
