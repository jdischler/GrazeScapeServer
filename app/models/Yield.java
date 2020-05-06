package models;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import query.Layer_CDL;
import query.Layer_Integer;
import raster.Extents;
import utils.Json;
import utils.ServerStartup;

//---------------------------------------------------------------------
public class Yield implements RasterModel {

    private static final Logger logger = LoggerFactory.getLogger("app");

	@Override
	public RasterResult compute(Extents ext, JsonNode options) throws Exception {
		
		logger.error(options.toString());
		String cropModel = Json.safeGetOptionalString(options, "crop", "corn");
		LinearModel lm = null;
		
		try {
//			String modelPath = mApplicationRoot + "/conf/modelDefs/yield/bluegrassWhiteClover.csv";
			String modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/yield/" + cropModel + ".csv";
	    //    String definition = new String( Files.readAllBytes( Paths.get(modelPath) ));
			lm = new LinearModel().init(modelPath);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Layer_Integer wl = Layer_CDL.get();
		int width = wl.getWidth();
		int height = wl.getHeight();
		
		float[][] bwcYield = new float[height][width];
		
		for (int y = ext.y2(); y < ext.y1(); y++) {
			for (int x = ext.x1(); x < ext.x2(); x++) {
				bwcYield[y][x] = lm.calculate(x, y).floatValue();
			}
		}
		
		return new RasterResult(bwcYield); 
	}	
}
