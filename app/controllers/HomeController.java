package controllers;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import analysis.AreaStats;
import analysis.Downsampler;
import analysis.Extract;
import analysis.RasterToPNG;
import analysis.RasterizeFeature;
import analysis.AreaStats.Stats;
import analysis.windowing.Moving_CDL_Window;
import analysis.windowing.Moving_CDL_Window_Z;
import analysis.windowing.Moving_Window;
import play.api.libs.Files.TemporaryFile;
import play.mvc.*;
import utils.Json;
import utils.ServerStartup;
import query.Layer_Base;
import query.Layer_CDL;
import query.Layer_Integer;
import data_types.Farm;
import db.FieldGeometry;
import io.ebean.Ebean;
import io.ebean.SqlRow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//• On connect:
//	• Create new user id if needed (stored in cookies)

//• Assign any short-term scenario resources:
//	• Main one might be a clone of the farm data set which the user can then modify
//	• Secondary one might be the current scenario itself so the user has the option of refreshing the browser but still
//		keeping their scenario setup
//	• Another might be a copy of the assumptions, again so they could be cached for a browser refresh?

//	• Client changes:
//	• Alert user about cookie policy (cookies required). Specifically, cookies are used to link your scenarios to the
//		computing resources that will process the scenario. Cookies may be used for research purposes 
//		(linking multiple scenario runs...ie, tracking what types of things users are doing)

//	• Render assigned farm data set points
//	• When farm data set changes, need to send new to client and redraw
//	• Scenario setups could be sent more regularly and stored in the session cache?

// 	• dynamic masking may be doable with: https://github.com/come/csg2d.js/

//	• general design changes: store things like job keys, scenario keys, and similar in cookies?

/*SmartScape 2.0 Design Considerations

Performance / Resource Management:
	•	Unbounded access to CPU and MEM can't be allowed in the future
	•	Dealing with disk asset cleanup
	•	Overall reduction of disk footprint for temporary-ish results
	•	Efficiently managing models that can communicate with other models and/or rely on another model for results before doing its own work
	•	Area of Interest (AOI) complicates this. Unload Areas when they are not referenced for some time?

User / Website Related:
	•	ADA accessibility considerations (visually impaired) and color blind
	•	Cookie transparency, cookie user agreements, general cookie policy
	•	Google analytics tracking concerns
	•	Registered user features complications. Requires tracking email addresses, storing passwords, risk of data breach
	•	Mobile vs. desktop layout considerations. Mobile design likely needs to be hugely different and overall simplified
	•	Lack of level-of-detail improvements to selection/heatmap displays during map zoom
	•	Satellite map layer requirements. Most likely paid unless we host Open Street Maps data here. Which then opens up the requirement to periodically update that to maintain accuracy. Plus additional server resources (map server) and setup complications
	•	Reliance on ExtJs libraries and potential for licensing requirements

Area of Interest / subsetting / Selection / Queries:
	•	requirement for full datasets for each area of interest (AOI)
	•	ease of defining new areas of interests, potentially outside of Wisconsin
	•	Inter-screen communication between Portal and Application
	•	Where do selection transforms go? Such as procedural fraction, buffering, critical max?
	•	Selection transforms are order dependent. Subset most likely should be after Critical Max but other than that...Is there a fixed and generally most-useful ordering we should do? 
	•	Layers are currently looked up by name, not by a composite AOI and name...or some other suitable scheme

Models:
	•	Effort to develop new models.
	•	General complexity of model modules communicating and sharing results.
	•	Do we need to support the potential for one or more models to not be relevant or available based on area and/or lack of input data
	•	Provide feedback on overall model run progress
	•	Potentially have a model run queue and feedback to the end-user where they are in the run queue
	
Farms:
	•	Most likely need a master set of farms and then a way to filter it down to a (large?) radius buffered selection from the watershed / county AOI for the user.
	 	For a large buffering radius, this won't be super fast. However, the results could be stored as just the set of ID's with the idea that
	 	a working set of user modifiable Farms can be created as needed without the need to repeat the selection.
	 	
*/

//------------------------------------------------------------------
public class HomeController extends Controller {

    private static final Logger logger = LoggerFactory.getLogger("app");
    private static Integer pngCounter = 1;

	//------------------------------------------------------------------
	@Inject
	public HomeController(ServerStartup startup) {
	}
	
	//------------------------------------------------------------------
	public Result app() {

		return ok(views.html.app.render());
	}

	//------------------------------------------------------------------
	public Result createOperation(Http.Request request) {
		
		JsonNode dat = request.body().asJson();
		logger.error(dat.toString());
		String operation = Json.safeGetString(dat, "operation");
		String owner = Json.safeGetString(dat, "owner");
		String address = Json.safeGetOptionalString(dat, "address", "");
		Float x = Float.parseFloat(Json.safeGetString(dat, "location_x"));
		Float y = Float.parseFloat(Json.safeGetString(dat, "location_y"));
		
		/*Map<String,String[]> parms = request.body().asFormUrlEncoded();
		
		String location_x[] = parms.get("location_x");
		String location_y[] = parms.get("location_y");*/

		db.Farm f = new db.Farm().
				name(operation).
				owner(owner).
				address(address).
				location(x, y);
		
		f.save();
		
		// Must return "success": true for extjs form submission framework...
		return ok(Json.pack("success",true, "farm", Json.pack("id", f.id)));
	}
	
	//------------------------------------------------------------------
	public Result getDataFromClickPoint(Http.Request request) {
		
/*		float slope[][] = Layer_Base.getLayer("slope").getFloatData();
		float silt[][] 	= Layer_Base.getLayer("silt_perc").getFloatData();
		float sand[][] 	= Layer_Base.getLayer("sand_perc").getFloatData();
		float clay[][] 	= Layer_Base.getLayer("clay_perc").getFloatData();
		float depth[][] = Layer_Base.getLayer("soil_depth").getFloatData();
		float cec[][] 	= Layer_Base.getLayer("cec").getFloatData();
		float distToWater[][] = Layer_Base.getLayer("dist_to_water").getFloatData();

		JsonNode node = request.body().asJson();
		logger.info(node.toString());
		String c_x = Json.safeGetString(node, "x");
		String c_y = Json.safeGetString(node, "y");
		
		Float click_x = Float.valueOf(c_x);
		Float click_y = Float.valueOf(c_y);
		
		int rasterWidth = 1900, rasterHeight = 3400;
		Integer areaExtents[] = {
				-10128000, 5358000,
			};
		
			// Align selection to 10m grid
			Integer selExtents[] = {
				Math.round(click_x / 10.0f) * 10, Math.round(click_y / 10.0f) * 10,
			};
			
			// re-index
			Integer x = (selExtents[0] - areaExtents[0]) / 10;
			Integer y = rasterHeight - ((selExtents[1] - areaExtents[1]) / 10);

			Boolean x_clipped = false, y_clipped = false;
			if (x < 0) {
				x_clipped = true;
				x = 0;
			}
			else if (x >= rasterWidth - 1) {
				x_clipped = true;
				x = rasterWidth - 1;
			}
			
			if (y < 0) {
				y_clipped = true;
				y = 0;
			}
			else if (y >= rasterHeight - 1) {
				y_clipped = true;
				y = rasterHeight - 1;
			}
		
			if (x_clipped || y_clipped) {
				logger.error("Click Results....well, there was some cllippage that happened: (x=" + x_clipped + "; y=" + y_clipped);
			}
			
			String res = "Click data results at location are:\n";
			
			res += "  Slope: " + (Float)slope[y][x] + "\n";
			res += "  Silt: " + (Float)silt[y][x] + "\n";
			res += "  Sand: " + (Float)sand[y][x] + "\n";
			res += "  Clay: " + (Float)clay[y][x] + "\n";
			res += "  Depth: " + (Float)depth[y][x] + "\n";
			res += "  CEC: " + (Float)cec[y][x] + "\n";
			res += "  DistToWater: " + (Float)distToWater[y][x] + "\n";
			logger.info(res);*/
		return ok();
	}
	
/*	//------------------------------------------------------------------
	public Result uploadFileTest(Http.Request request) {
	    Http.MultipartFormData<TemporaryFile> body = request.body().asMultipartFormData();
	    Http.MultipartFormData.FilePart<TemporaryFile> picture = body.getFile("picture");
	    if (picture != null) {
	      String fileName = picture.getFilename();
	      long fileSize = picture.getFileSize();
	      String contentType = picture.getContentType();
	      TemporaryFile file = picture.getRef();
	      file.copyTo(Paths.get("/tmp/picture/destination.jpg"), true);
	      return ok("File uploaded");
	    } else {
	      return badRequest().flashing("error", "Missing file");
	    }	}
*/	
	public Result getFarms() {
		return ok(db.Farm.getAllAsGeoJson());
	}
	
	public Result getFields(Long farmId) {
		
		logger.info("Got a request for farm: " + farmId);
		db.Farm f = db.Farm.find.byId(farmId);
		if (f != null) {
			logger.info(String.format("And that farm was found...<%s> <%s> <%s>", f.farmName, f.farmOwner, f.farmAddress));
			return ok(f.getFieldShapesAsGeoJson());
		}
		return ok("Farm did not exist");
	}
	
	public Result testFetch(Http.Request request) {
		return ok();
	}

	public Result addField(Http.Request request) {
		JsonNode res = db.Farm.addField(request);
		return ok(res);
	}

	public Result modifyFields(Http.Request request) {
		JsonNode res = db.FieldGeometry.modifyFields(request);
		return ok(res);
	}

	public Result fetchImage(Http.Request request) {
		
		JsonNode node = request.body().asJson();
		String model = Json.safeGetOptionalString(node, "model", "bird");
		
		ModelComputation mc = null;
		if (model.equalsIgnoreCase("bird")) mc = new BirdModel();
		else if (model.equalsIgnoreCase("corn")) mc = new CornModel();
		else if (model.equalsIgnoreCase("slope")) mc = new SlopeModel();
		
		return computeModel(request, mc);
//		return ok(Layer_Base.fetch_image(request));
	}

	//-----------------------------------------------------------------------------
	public Result computeSlope(Http.Request request) {
		return computeModel(request, new SlopeModel());
	}	

	//-----------------------------------------------------------------------------
	public Result computeCornModel(Http.Request request) {
		return computeModel(request, new CornModel());
	}	
	
	//-------------------------------------------------------
	public Result computeBirdModel(Http.Request request) {
		return computeModel(request, new BirdModel());
	}
	
	
	//-------------------------------------------------------
	// Model interface
	//-------------------------------------------------------
	private interface ModelComputation {
		public float[][] compute(Clipper computationArea); 
	};

	public class Clipper {
		Integer mExtent[] = {0,0,0,0};
		Integer mX1, mY1, mX2, mY2;
		Integer mWidth, mHeight;

		// Node should be an arrayNode of OL extent values, but it can be a NULL node in which case the area extents becomes the clip extent
		public Clipper initFromJson(JsonNode node) {
			
			ArrayNode extent = (ArrayNode)node;
			final Integer areaExtents[] = {
				440000, 340000,
				455000, 314000
			};
			
			// Align selection to 10m grid
			// The openLayers extent has the Y values reversed from the convention I prefer
			if (extent != null) {
//				logger.info(" Clipper has an extent node...." + extent.toString());
				mExtent[0] = Math.round(extent.get(0).floatValue() / 10.0f) * 10;
				mExtent[1] = Math.round(extent.get(3).floatValue() / 10.0f) * 10;
				mExtent[2] = Math.round(extent.get(2).floatValue() / 10.0f) * 10;
				mExtent[3] = Math.round(extent.get(1).floatValue() / 10.0f) * 10;
			
				// Clip Selection to area
				if (mExtent[0] < areaExtents[0]) 		mExtent[0] = areaExtents[0];
				else if (mExtent[0] > areaExtents[2]) 	mExtent[0] = areaExtents[2];
				
				if (mExtent[2] < areaExtents[0]) 		mExtent[2] = areaExtents[0];
				else if (mExtent[2] > areaExtents[2]) 	mExtent[2] = areaExtents[2];
			
				if (mExtent[1] > areaExtents[1]) 		mExtent[1] = areaExtents[1];
				else if (mExtent[1] < areaExtents[3]) 	mExtent[1] = areaExtents[3];
				
				if (mExtent[3] > areaExtents[1]) 		mExtent[3] = areaExtents[1];
				else if (mExtent[3] < areaExtents[3]) 	mExtent[3] = areaExtents[3];
			}
			else {
				mExtent[0] = areaExtents[0];
				mExtent[1] = areaExtents[1];
				mExtent[2] = areaExtents[2];
				mExtent[3] = areaExtents[3];
			}
//			logger.error(" clipped extents are [" + mExtent[0] + "," + mExtent[1] + "][" + mExtent[2] + "," + mExtent[3] + "]");
		
			// re-index
			mX1 = (mExtent[0] - areaExtents[0]) / 10;
			mY1 = -(mExtent[1] - areaExtents[1]) / 10;
			
			mX2 = (mExtent[2] - areaExtents[0]) / 10;
			mY2 = -(mExtent[3] - areaExtents[1]) / 10;
			
			mWidth = mX2 - mX1;
			mHeight = mY2 - mY1;
			
			return this;
		}
	}

	
	//-------------------------------------------------------
	public Result computeModel(Http.Request request, ModelComputation modelFunction) {
	
		JsonNode node = request.body().asJson();
		
		// Extent array node can be missing, in which case we get a clipper that extracts the entire area
		Clipper ext = new Clipper().initFromJson(node.get("extent"));
		
		final Integer rasterHeight = 2600, rasterWidth = 1500;
		final Integer areaExtents[] = {
			440000, 340000,
			455000, 314000
		};
		ObjectNode result = null;
		
		float [][] modelResults = modelFunction.compute(ext);
		
		Long farmId = utils.Json.safeGetOptionalLong(node, "farm_id");
		int mapMode = utils.Json.safeGetOptionalInteger(node, "mode", 1);  // 0, 1, or 2

		Boolean restrictionToRowCrops = utils.Json.safeGetOptionalBoolean(node, "row_crops", false);
		Boolean restrictionToGrasses = utils.Json.safeGetOptionalBoolean(node, "grasses", false);

		if (restrictionToRowCrops || restrictionToGrasses) {
			Layer_Integer wl = Layer_CDL.get();
			int wl_data[][] = wl.getIntData();
			int road_mask[][] = Layer_Base.getLayer("road_mask").getIntData();
			int water_mask[][] = Layer_Base.getLayer("water_mask").getIntData();
			
			int mask = 0;
			if (restrictionToRowCrops) {
				mask |= wl.stringToMask("Cash Grain","Continuous Corn","Dairy Rotation","Other Crops");
			}
			if (restrictionToGrasses) {
				mask |= wl.stringToMask("Hay","Pasture","Reed Canary Grass","Cool-Season Grass","Warm-Season Grass");
			}
			
			for (int y = 0; y < rasterHeight; y++) {
				for (int x = 0; x < rasterWidth; x++) {
					if ((wl_data[y][x] & mask) <= 0) modelResults[y][x] = -9999.0f;
					else if (road_mask[y][x] > 0 || water_mask[y][x] > 0) modelResults[y][x] = -9999.0f;
				}
			}
		}
		
		if (farmId != null) {
			db.Farm f = db.Farm.find.byId(farmId);
			if (f != null) {
				List<JsonNode> features = new ArrayList<>();
				
				for (FieldGeometry fd: f.mFieldGeometry) {
					SqlRow sw = Ebean.createSqlQuery("SELECT ST_AsGeoJson(ST_Transform(ST_GeomFromText( ?, 3857 ),3071)) as gjson")
						.setParameter(1, fd.geom)
						.findOne();
				
					JsonNode res = play.libs.Json.parse(sw.getString("gjson"));
					features.add(Json.pack("type", "Feature",
							"geometry", res,
							"properties", Json.pack("f_id", fd.id)));
				}
				
				int layer[][] = RasterizeFeature.testToInt(features);
			
				for (int y = 0; y < rasterHeight; y++) {
					for (int x = 0; x < rasterWidth; x++) {
						if (layer[y][x] <= 0) modelResults[y][x] = -9999.0f;
					}
				}
				
				AreaStats fs = new AreaStats(modelResults).forRasterizedFields(layer).compute();
				
				try {
					for (FieldGeometry fd: f.mFieldGeometry) {
						Long fs_idx = fd.id;
						Stats stats = fs.getFieldStats(fs_idx);
						
						if (stats == null) {
							logger.error("Stats are null for field_id: " + fs_idx);
							continue;
						}
						Integer noDataCt = stats.getNoDataCount();
						Float noDataPerc = stats.getFractionNoData();
	
						String results = "\n─────────────────────────────────────────────────────\n" +
								"¤STATISTICS for FieldID: " + fs_idx + "\n" +
								"  NoDataCells: " + noDataCt + "\n" +
								"  NoData%:     " + String.format("%.1f%%", noDataPerc * 100) + "\n";
	
						
						if (stats.hasStatistics()) {
							Integer histogramCt = 20;
							AreaStats.Histogram hs = stats.getHistogram(histogramCt, stats.getMin(), stats.getMax()); 
							Integer ct = stats.getCounted();
							Float area = ct * 100.0f;
							Double sum = stats.getSum();
							Float min = stats.getMin();
							Float max = stats.getMax();
							Float mean = stats.getMean();
							Float median = stats.getMedian();
							results += " »FIELD CELLS: " + ct + "\n";
							results += String.format("  Area: %.2f(ac)   %.2f(km2) \n", area / 4047.0f, area / 1000000.0f);
							results += " »YIELD STATS \n";
							results += String.format("  Total Yield: %.2f\n", sum);
							results += String.format("  Min: %.2f    Max: %.2f \n", min, max);
							results += String.format("  Mean: %.2f\n", mean);
							results += String.format("  Median: %.2f\n", median);
							results += " »HISTOGRAM (" + histogramCt + " bins) \n";
							results += hs.toString();
						}
						results += "─────────────────────────────────────────────────────\n";
						
						logger.info(results);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					logger.error(e.toString());
				}
				if (mapMode == 2) {
					try {
						for (int y = 0; y < rasterHeight; y++) {
							for (int x = 0; x < rasterWidth; x++) {
								if (layer[y][x] > 0) {
									Stats stats = fs.getFieldStats((long)layer[y][x]);
									if (stats.hasStatistics()) {
										modelResults[y][x] = stats.getMean();
									}
									else {
										modelResults[y][x] = 0;
									}
								}
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		// TODO: may want to skip clipping process if raster modelResults is the full raster...
		float clipped[][] = Extract.now(modelResults, ext.mX1, ext.mY1, ext.mWidth, ext.mHeight);

		// Downsampler...
/*		int maxSize = 1600;
		// restrict output size and resample
		if (ww > maxSize || hh > maxSize) {
			int newW = ww, newH = hh;
			if (ww > hh) {
				newW = maxSize;
				newH = Math.round((hh / (float)ww) * maxSize);
			}
			else if (ww < hh) {
				newW = Math.round((ww / (float)hh) * maxSize);
				newH = maxSize;
			}
			else {
				newW = newH = maxSize;
			}
			
			logger.info("wh [" + ww + "," + hh + "] new wh [" + newW + "," + newH + "]");
			try {
				clipped = Downsampler.mean(clipped, ww, hh, newW, newH);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			hh = newH;
			ww = newW;
		}
		*/		
		File fp = new File("./public/dynamicFiles/yes" + pngCounter + ".png");
		result  = (ObjectNode)RasterToPNG.save(clipped, ext.mWidth, ext.mHeight, fp);
		
		// Reverse Y ordering for openlayers
		Json.addToPack(result, "url", "renders/yes" + pngCounter + ".png", 
						"extent", Json.array(ext.mExtent[0],ext.mExtent[3],
								ext.mExtent[2],ext.mExtent[1]));	
		
		pngCounter++;
		return ok(result);
	}
	
	// Simple fake model - just copy a layer into a returnable dataset
	//-------------------------------------------------------------------------------------
	public class SlopeModel implements ModelComputation {
		
		// ext is the computation extent
		public float[][] compute(Clipper ext)  { 
			
			Layer_Base slope = Layer_Base.getLayer("slope");
			float slopeData[][] = slope.getFloatData();
			float [][] slopeOut = new float[slope.getHeight()][slope.getWidth()];
			
			for (int y = ext.mY1; y < ext.mY2; y++) {
				for (int x = ext.mX1; x < ext.mX2; x++) {
					
					slopeOut[y][x] = slopeData[y][x];
				}
			}
			
			return slopeOut;
		}
	}
	
	//-------------------------------------------------------------------------------------
	public class CornModel implements ModelComputation {
		
		public float[][] compute(Clipper ext)  { 
			
			Layer_Base slope = Layer_Base.getLayer("slope");
			float slopeData[][] = slope.getFloatData();
			float silt[][] = Layer_Base.getLayer("silt_perc").getFloatData();
//			float depth[][] = Layer_Base.getLayer("soil_depth").getFloatData();
			float cec[][] = Layer_Base.getLayer("cec").getFloatData();
			float [][] cornYield = new float[slope.getHeight()][slope.getWidth()];
			
			float cornCoefficient = 1.30f 	// correction for technological advances 
					* 0.053f; 				// conversion to Mg per Ha 
			
			float soyCoefficient = 1.2f		// Correct for techno advances
					* 0.0585f;				// conversion to Mg per Ha
			
			for (int y = ext.mY1; y < ext.mY2; y++) {
				for (int x = ext.mX1; x < ext.mX2; x++) {
					
					float _slope = slopeData[y][x], /*_depth = depth[y][x],*/ _silt = silt[y][x], _cec = cec[y][x];
					
//					if (_depth > 60) _depth = 60;
					float cornY =  22.0f - 1.05f * _slope + /*0.19f * _depth +*/ (0.817f / 100.0f) * _silt + 1.32f * _cec
							* cornCoefficient ;
					
//					float soyY = 6.37f - 0.34f * _slope + 0.065f * _depth + (0.278f / 100.0f) * _silt + 0.437f * _cec 
//							* soyCoefficient;
					
					if (cornY < 1) cornY = 1;//-9999.0f;
					else if (cornY > 36) cornY = 36;
					
//					if (soyY < 1) soyY = 1;//-9999.0f;
//					else if (soyY > 36) soyY = 36;
					
					cornYield[y][x] = cornY;
				}
			}
			
			return cornYield;
		}
	}
	
	// Moving window example
	//-------------------------------------------------------------------------------------
	public class BirdModel implements ModelComputation {
		
		public float[][] compute(Clipper ext)  { 
			
			Layer_Integer wl = Layer_CDL.get();
			int wl_data[][] = wl.getIntData();
			float [][] habitatData = new float[wl.getHeight()][wl.getWidth()];
			
			Moving_CDL_Window win = (Moving_CDL_Window)new Moving_CDL_Window_Z(400/10).
					restrict(ext.mX1, ext.mY1,
					ext.mX2, ext.mY2).initialize();
			
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
		
						if (habitatIndex < 0.1f) 
							habitatIndex = -9999.0f;
				
						habitatData[point.mY][point.mX] = habitatIndex;
					}
					else {
						habitatData[point.mY][point.mX] = -9999.0f; // NO DATA
					}
					
					moreCells = win.advance();
				}	
			}
			catch(Exception e) {
				logger.error(e.toString());
				logger.error(" mx: " + ((Integer)point.mX) + "   my: " + ((Integer)point.mY) );
			}
			return habitatData;
		}
	}
	
	

}
