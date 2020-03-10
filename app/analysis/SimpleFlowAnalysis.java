package analysis;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import query.Layer_Base;

//------------------------------------------------------------------------------
public class SimpleFlowAnalysis
{
    private static final Logger logger = LoggerFactory.getLogger("app");

    private int			mWidth = 1500, mHeight = 2600;
    
    //----------------------------------------------------------------
    public SimpleFlowAnalysis() {
    }
    
    public class Dist {
    	public float mDistance;
    	public int atX, atY;
    	public Dist(float d, int x, int y) {
    		mDistance = d;
    		atX = x; atY = y;
    	}
    }
    //----------------------------------------------------------------
    public float [][] go() {

    	float data[][] = new float[mHeight][mWidth];
		float dem[][] = Layer_Base.getLayer("dem").getFloatData();

		float d2w[][] = Layer_Base.getLayer("distance_to_water").getFloatData();
		List<Dist> mDistances = new ArrayList<>();

		//initialize
		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				data[y][x] = -9999.0f;
				mDistances.add(new Dist(d2w[y][x], x, y));
			}
		}
    	
		
    	return data;
    	
    }
}
