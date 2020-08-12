package models;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import analysis.AreaStats;
import analysis.RasterizeFeatures;
import analysis.Stats;
import db.CropYear;
import db.Field;
import db.Landcover;
import db.Scenario;
import io.ebean.Ebean;
import io.ebean.SqlRow;
import query.Layer_CDL;
import query.Layer_Float;
import query.Layer_Integer;
import raster.Extents;
import utils.Json;
import utils.ServerStartup;

//---------------------------------------------------------------------
public class DryMatter implements RasterModel {

    private static final Logger logger = LoggerFactory.getLogger("app");

    // Data holder for the landcover bit - to - relevant data to process that type of bit
    // Example: a given crop bit (such as Corn Grain) will map to the model definition, instance, and 
    //	lookup of the ratios to apply to yields of this type for a given field.
    private class CropData {
    	// TODO: these only cover the dominant Crop....
    	public Landcover		crop;
    	public LinearModel		modelInstance;
    	public Map<Long,Float>	perFieldRatio = new HashMap<>();
    	public AreaStats		stats;
    };
    
	@Override
	public RasterResult compute(Extents ext, JsonNode options) throws Exception {

		Long farmId = 1L;
		
		db.Farm farm = db.Farm.find.byId(farmId);
		if (farm == null) {
			logger.error("Farm was not found");
			return null;
		}
		if (farm.scenarios.size() <= 0) {
			logger.error("No scenarios found");
			return null;
		}
		// TODO: support arbitrary scenario
		Scenario scen = farm.scenarios.get(0);
		
		List<JsonNode> features = new ArrayList<>();
		Map<Integer, CropData> cropMap = new HashMap<>();
		
		for (Field f: scen.mFields) {
			// Get the total landcover bit pattern so we can render that composite value out
			//	Also note the unique yield models we'll need to allocate
			Integer landcoverBitCode = 0;
			if (f.geometry == null) continue;
			Long f_id = f.geometry.id;
			for (CropYear cy: f.cropYears) {
				logger.info("CropYearId: " + cy.id);
				if (cy.dominantCrop == null) {
					logger.error("dominant crop was nullllllll");
				}
				else {
					logger.info("Crop would be: " + cy.dominantCrop.dbValue);
				}
				if (cy.dominantCrop.yieldModel == null) continue;
				Integer bits = cy.dominantCrop.bitEncoding;
				landcoverBitCode |= bits;
				CropData cd = cropMap.get(bits);
				if (cd == null) {
					cd = new CropData();
					cd.crop = cy.dominantCrop;
					cropMap.put(bits, cd);
				}
				cd.perFieldRatio.put(f_id, cy.dominantRatio);
			}
			SqlRow sw = Ebean.createSqlQuery("SELECT ST_AsGeoJson(ST_Transform(ST_GeomFromText( ?, 3857 ),3071)) as gjson")
				.setParameter(1, f.geometry.geom)
				.findOne();
		
			JsonNode res = play.libs.Json.parse(sw.getString("gjson"));
			features.add(Json.pack("type", "Feature",
					"geometry", res,
					"properties", Json.pack(
							"f_id", f_id, 
							"bitcode", landcoverBitCode)
				));
		}
		
		// Create needed model instances
		for (CropData cd: cropMap.values()) {
			LinearModel lm = null;
			try {
				String modelPath = ServerStartup.getApplicationRoot() + cd.crop.yieldModel;
				logger.debug("DryMatter:test -> loading model: " + modelPath);
				cd.modelInstance = new LinearModel().init(modelPath);
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
		
		int[][] cropBitKey = RasterizeFeatures.withIntProperty(features, "bitcode");
		int[][] fieldIdKey = RasterizeFeatures.withIntProperty(features, "f_id");
		
		Layer_Integer wl = Layer_CDL.get();
		int width = wl.getWidth();
		int height = wl.getHeight();
		
		float[][] dmOut = new float[height][width];
		for (float[] row: dmOut) {
			Arrays.fill(row, -9999.0f);
		}
		
		for (CropData cd: cropMap.values()) {
			if (cd == null || cd.modelInstance == null) {
				continue;
			}
			float[][] dmWork = new float[height][width];
			
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					float value = -9999.0f;
					if (fieldIdKey[y][x] > 0 && (cropBitKey[y][x] & cd.crop.bitEncoding) > 0) {
						Long key = Long.valueOf(fieldIdKey[y][x]);
						Float ratio = cd.perFieldRatio.get(key);
						// TODO: FIXME: why is ratio null?
						if (ratio == null) ratio =  1.0f;
						value = cd.modelInstance.calculate(x, y) * ratio;
					}
					dmWork[y][x] = value;
				}
			}
			// capture stats for this pass of yield calculation
			cd.stats = new AreaStats(dmWork).forExtents(ext).forRasterizedFields(fieldIdKey).compute();
			
			// and then accumulate the pass
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					// if OUT is not initialized, set with work (which can also be uninitialized)
					if (Layer_Float.isNoDataValue(dmOut[y][x])) {
						dmOut[y][x] = dmWork[y][x];
					}
					else if (!Layer_Float.isNoDataValue(dmWork[y][x])) {
						dmOut[y][x] += dmWork[y][x];
					}
				}
			}
		}
		
		ObjectNode results = Json.newPack();
		
		// TODO: figure out how to extract the stats
		// CropData should be a list of computed crops...
		//	Inside of each should be a stats object.
		//	That stats object can be queried PER field to get the field response for each crop
		//		OR just make something on the Stats object that can export a representation of what it's
		//			holding, most likely with some sort of configuration which specifies which 
		//			data to get out and how...
		ObjectNode cropOuts = Json.newPack();
		for (CropData cd: cropMap.values()) {
			logger.info("------------ Crop <<" + cd.crop.dbValue + ">> -------------------");
			Set<Long> fieldKeys = cd.stats.getFieldKeys();
			ObjectNode fieldOuts = Json.newPack();
			for (Long fk: fieldKeys) {
				Stats stats = cd.stats.getFieldStats(fk);
				if (fk > 0L && stats.hasStatistics()) {
					Json.addToPack(fieldOuts, 
							fk, stats.toJson(true));
				}
			//	stats.debug(true);
			}
			Json.addToPack(fieldOuts, 
					"totals", cd.stats.getAreaStats().toJson(true));
			Json.addToPack(cropOuts, 
					cd.crop.dbValue, fieldOuts);
		}

		AreaStats total = new AreaStats(dmOut).forExtents(ext).compute();
		Json.addToPack(results, 
				"totals", total.getAreaStats().toJson(true),
				"crops", cropOuts);
		
		return new RasterResult(dmOut).addClientData(results); 
	}
}