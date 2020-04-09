package analysis;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import query.Layer_Base;

//------------------------------------------------------------------------------
public class SimpleFlowAnalysis
{
    private static final Logger logger = LoggerFactory.getLogger("app");

    
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

    	Layer_Base dem = Layer_Base.getLayer("dem"); 
    	int width = dem.getWidth(), height = dem.getHeight();
		float demData[][] = dem.getFloatData();
    	float data[][] = new float[height][width];

		float d2w[][] = Layer_Base.getLayer("distance_to_water").getFloatData();
		List<Dist> mDistances = new ArrayList<>();

		//initialize
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				data[y][x] = -9999.0f;
				mDistances.add(new Dist(d2w[y][x], x, y));
			}
		}
    	
		
    	return data;
    	
    }
}
