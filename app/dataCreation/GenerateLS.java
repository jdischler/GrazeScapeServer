package dataCreation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileHandling.Asc_Writer;
import query.Layer_Base;

public class GenerateLS {
	
    private static final Logger logger = LoggerFactory.getLogger("app");
	
	public static void go() {
	
		Asc_Writer.quickDump(makeRaster());
	}
	
	private static float[][] makeRaster() {
		
		Layer_Base slopeL = Layer_Base.getLayer("slope");
		float [][] slopeR = slopeL.getFloatData();

		Layer_Base slopeLenL = Layer_Base.getLayer("slope_length");
		float [][] slopeLenR = slopeLenL.getFloatData();
		
		int width = slopeL.getWidth();
		int height = slopeL.getHeight();
		
		float [][] lsR = new float[height][width];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				float slope = slopeR[y][x];
				float factor = 0.2f; // slopes < 1
				if (slope > 3.0 && slope <= 4) {
					factor = 0.4f;
				}
				else if (slope >= 1 && slope <= 3) {
					factor = 0.3f;
				} 
				else if (slope >= 4) {
					factor = 0.5f;
				}

				float slopeLen = slopeLenR[y][x];
				float ls = (float)(((slope / Math.pow(10000.0 + slope * slope, 0.5) * 4.56) + Math.pow(slope/Math.pow(10000.0+ slope * slope,0.5), 2.0) * 65.41 + 0.065) * 
						Math.pow(slopeLen * 3.3 / 72.6,factor));
				int intLS = (int)(ls * 100.0 + 0.5);
				lsR[y][x] = intLS / 100.0f;
			}
		}
		
		return lsR;
	}
	
	private static class Slope_SlopeLen {
		Float slope, slopeLen;
		public Slope_SlopeLen(Float slp, Float slpLen) {
			slope = slp;
			slopeLen = slpLen;
		}
	}
	
	public static void validateLS_Function() {
		
		
		Slope_SlopeLen [] tests = {
			new Slope_SlopeLen( 1.5f, 250.0f),	// 0.21
			new Slope_SlopeLen( 2.5f, 200.0f),	// 0.3
			new Slope_SlopeLen( 4.0f, 175.0f),	// 0.5	
			new Slope_SlopeLen( 9.0f, 200.0f),	// 1.66
			new Slope_SlopeLen(12.0f, 150.0f),	// 2.21
			new Slope_SlopeLen(18.0f, 150.0f),	// 4.21
			new Slope_SlopeLen(18.5f, 80.0f),	// 3.21
			new Slope_SlopeLen(22.0f, 80.0f),	// 4.27
			new Slope_SlopeLen(30.0f, 125.0f),	// 8.89
			new Slope_SlopeLen(42.5f, 60.0f),	// 10.78
			new Slope_SlopeLen(60.0f, 60.0f),	// 17.93
		};
		
		for (Slope_SlopeLen sslp: tests) {
			float slope = sslp.slope;
			float factor = 0.2f; // slopes < 1
			if (slope > 3.0 && slope <= 4) {
				factor = 0.4f;
			}
			else if (slope >= 1 && slope <= 3) {
				factor = 0.3f;
			} 
			else if (slope >= 4) {
				factor = 0.5f;
			}

			float slopeLen = sslp.slopeLen;
			float ls = (float)(((slope / Math.pow(10000.0 + slope * slope, 0.5) * 4.56) + Math.pow(slope/Math.pow(10000.0+ slope * slope,0.5), 2.0) * 65.41 + 0.065) * 
					Math.pow(slopeLen/* * 3.3 *// 72.6,factor));
			int intLS = (int)(ls * 100.0 + 0.5);
			Float res = intLS / 100.0f;
			
			logger.info(String.format("{%.1f , %.1f} = %.2f", slope, slopeLen, res));
		}
		
	}
}