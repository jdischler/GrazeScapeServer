package models;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import analysis.windowing.Moving_CDL_Window;
import analysis.windowing.Moving_CDL_Window_Z;
import analysis.windowing.Moving_Window;
import query.Layer_CDL;
import query.Layer_Float;
import query.Layer_Integer;
import raster.Extents;

// Moving window example
//---------------------------------------------------------------------
public class BirdHabitat implements RasterModel {
//-------------------------------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger("app");

	@Override
	public RasterResult compute(Extents ext, JsonNode options) throws Exception { 
		
		Layer_Integer wl = Layer_CDL.get();
		float [][] habitatData = new float[wl.getHeight()][wl.getWidth()];
		
		Moving_CDL_Window win = (Moving_CDL_Window)new Moving_CDL_Window_Z(400/10).
				restrict(ext.x1(), ext.y2(),
				ext.x2(), ext.y1()).initialize();
		
		Moving_Window.WindowPoint point = win.getPoint();
		
		try {
			boolean moreCells = true;
			while (moreCells) {
				point = win.getPoint();
				
				if (win.canGetProportions()) {
					float proportionAg = win.getProportionAg();
					float proportionGrass = win.getProportionGrass();
					
					// Habitat Index
					float lambda = -4.47f + (2.95f * proportionAg) + (5.17f * proportionGrass); 
					float habitatIndex = (float)((1.0f / (1.0f / Math.exp(lambda) + 1.0f )) / 0.67f);
	
					// TODO: Verify that turning very low values into No Data is useful...
					if (habitatIndex < 0.1f) {
						habitatIndex = Layer_Float.getNoDataValue();
					}
			
					habitatData[point.mY][point.mX] = habitatIndex;
				}
				else {
					habitatData[point.mY][point.mX] = Layer_Float.getNoDataValue();
				}
				
				moreCells = win.advance();
			}	
		}
		catch(Exception e) {
			logger.error(e.toString());
			logger.error(" mx: " + ((Integer)point.mX) + "   my: " + ((Integer)point.mY) );
		}
		return new RasterResult(habitatData);
	}
}
