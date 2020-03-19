package models;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import query.Layer_CDL;
import query.Layer_Integer;

public class Yield {

	private static String mApplicationRoot;
    private static final Logger logger = LoggerFactory.getLogger("app");
	
	public static void setModelPath(File applicationRoot) {
		mApplicationRoot = applicationRoot.getPath();
	}
	
	public float[][] compute() {
		
		LinearModel lm = null;
		
		try {
//			String modelPath = mApplicationRoot + "/conf/modelDefs/bluegrassWhiteClover.csv";
			String modelPath = mApplicationRoot + "/conf/modelDefs/cornYield.csv";
	        String definition = new String( Files.readAllBytes( Paths.get(modelPath) ));
			lm = new LinearModel().init(definition);

			lm.debug();
			lm.measureResponse();
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