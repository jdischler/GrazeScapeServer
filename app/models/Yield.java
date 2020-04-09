package models;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import query.Layer_CDL;
import query.Layer_Integer;
import utils.ServerStartup;

//---------------------------------------------------------------------
public class Yield {

    private static final Logger logger = LoggerFactory.getLogger("app");

    //---------------------------------------------------------------------
	public float[][] compute(String cropModel) {
		
		LinearModel lm = null;
		
		try {
//			String modelPath = mApplicationRoot + "/conf/modelDefs/yield/bluegrassWhiteClover.csv";
			String modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/yield/" + cropModel + ".csv";
	        String definition = new String( Files.readAllBytes( Paths.get(modelPath) ));
			lm = new LinearModel().init(definition);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Layer_Integer wl = Layer_CDL.get();
		int width = wl.getWidth();
		int height = wl.getHeight();
		
		float[][] bwcYield = new float[height][width];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				bwcYield[y][x] = lm.calculate(x, y).floatValue();
			}
		}
		
		return bwcYield; 
	}
	
}