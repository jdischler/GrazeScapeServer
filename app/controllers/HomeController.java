package controllers;

import javax.inject.Inject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import play.mvc.*;
import play.mvc.Http.Session;
import play.twirl.api.Content;
import play.api.cache.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import analysis.*;
import analysis.windowing.*;
import query.*;
import raster.Extents;
import data_types.Farm;
import db.FieldGeometry;
import io.ebean.Ebean;
import io.ebean.SqlRow;
import models.LinearModel;
import models.Yield;
import models.transform.*;
import utils.Json;
import utils.RandomString;
import utils.ServerStartup;

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

    private Cache<String,session.Session> mSessions = null;
    
	//------------------------------------------------------------------
	@Inject
	public HomeController(ServerStartup startup) {
		mSessions = Caffeine.newBuilder().expireAfterAccess(8, TimeUnit.DAYS) .build();
	}
	
	//------------------------------------------------------------------
	public Result app(Http.Request request) {

		Session session = request.session();
		if (session != null) {
			Optional<String> user = session.get("user");
			if (user.isEmpty()) {
				String userID = RandomString.get(12);
				session = session.adding("user", userID);
				mSessions.put(userID, new session.Session());
			}
			else {
				logger.info("User is: " + user.get());
			}
		}
		return ok((Content) views.html.app.render()).withSession(session);
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
		
//		logger.info("Got a request for farm: " + farmId);
		db.Farm f = db.Farm.find.byId(farmId);
		if (f != null) {
//			logger.info(String.format("And that farm was found...<%s> <%s> <%s>", f.farmName, f.farmOwner, f.farmAddress));
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
		String mode = Json.safeGetOptionalString(node, "model", "bird-habitat");
		
		ModelComputation mc = null;
		if (mode.equalsIgnoreCase("bird-habitat")) mc = new BirdModel();
		else if (mode.equalsIgnoreCase("crop-yield")) mc = new YieldModel();
		else if (mode.equalsIgnoreCase("p-loss")) mc = new PLossModel();
		else if (mode.equalsIgnoreCase("p-loss-real")) mc = new AwePLossModel();
		
		else if (mode.equalsIgnoreCase("slope")) mc = new RasterInspection().dataLayer("slope");//.setTransform(new SlopePercentToAngle());
		else if (mode.equalsIgnoreCase("soil-depth")) mc = new RasterInspection().dataLayer("soil_depth");
		else if (mode.equalsIgnoreCase("dem")) mc = new RasterInspection().dataLayer("dem");
		else if (mode.equalsIgnoreCase("dist-water")) mc = new RasterInspection().dataLayer("distance_to_water");
		else if (mode.equalsIgnoreCase("perc-sand")) mc = new RasterInspection().dataLayer("sand_perc");
		else if (mode.equalsIgnoreCase("perc-silt")) mc = new RasterInspection().dataLayer("silt_perc");
		else if (mode.equalsIgnoreCase("perc-clay")) mc = new RasterInspection().dataLayer("clay_perc");
		else if (mode.equalsIgnoreCase("om")) mc = new RasterInspection().dataLayer("om");
		else if (mode.equalsIgnoreCase("k")) mc = new RasterInspection().dataLayer("k");
		else if (mode.equalsIgnoreCase("ls")) mc = new RasterInspection().dataLayer("ls");
		else if (mode.equalsIgnoreCase("ksat")) mc = new RasterInspection().dataLayer("ksat");
		else if (mode.equalsIgnoreCase("cec")) mc = new RasterInspection().dataLayer("cec");
		else if (mode.equalsIgnoreCase("ph")) mc = new RasterInspection().dataLayer("ph");
		else if (mode.equalsIgnoreCase("slope-length")) mc = new RasterInspection().dataLayer("slope_length");
		else if (mode.equalsIgnoreCase("ssurgo-slope")) mc = new RasterInspection().dataLayer("ssurgo_slope");//.setTransform(new SlopePercentToAngle());
		else if (mode.equalsIgnoreCase("t")) mc = new RasterInspection().dataLayer("t");
		else if (mode.equalsIgnoreCase("cdl")) {
			return processCategorical(request);
		}
		
		return computeModel(request, mc);
	}

	//-------------------------------------------------------
	public Result processCategorical(Http.Request request) {
		Layer_Integer cdl = Layer_CDL.get();
		int[][] cdlData = cdl.getIntData();
		
		JsonNode node = request.body().asJson();
		Extents ext = new Extents().fromJson((ArrayNode)node.get("extent")).toRasterSpace();
		ObjectNode result = null;
		File fp = new File(FileService.getDirectory() + "yes" + pngCounter + ".png");
		result  = (ObjectNode)RasterToPNG.saveClassified(cdlData, cdl.getKey(), ext.width(), ext.height(), fp);
		
		Json.addToPack(result, "url", "renders/yes" + pngCounter + ".png", 
						"extent", ext.toJson());	
		
		pngCounter++;
		return ok(result);
	}
	
	//-------------------------------------------------------
	// Model interface
	//-------------------------------------------------------
	private interface ModelComputation {
		public abstract float[][] compute(Extents computationArea, JsonNode options) throws Exception; 
	};

	//-------------------------------------------------------
	public Result computeModel(Http.Request request, ModelComputation modelFunction) {
	
		JsonNode node = request.body().asJson();

		JsonNode options = node.get("options");
		if (options != null) {
			logger.info("Has model options:" + options.toString());
		}
		// Extent array node can be missing, in which case we get a clipper that extracts the entire area
		Extents ext = new Extents().fromJson((ArrayNode)node.get("extent")).toRasterSpace();
		
		final Integer rasterHeight = 2600, rasterWidth = 1500;
		ObjectNode result = null;
		
		float[][] modelResults = null;
		try {
			modelResults = modelFunction.compute(ext, options);
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1.toString());
		}
		
		Long farmId = utils.Json.safeGetOptionalLong(node, "farm_id");
		int mapMode = utils.Json.safeGetOptionalInteger(node, "mode", 1);  // 0, 1, or 2

		Boolean restrictToRowCrops = utils.Json.safeGetOptionalBoolean(node, "row_crops", false);
		Boolean restrictToGrasses = utils.Json.safeGetOptionalBoolean(node, "grasses", false);
		if (restrictToRowCrops || restrictToGrasses) {
			modelResults = FloatFilters.restrictToAgriculture(modelResults, ext, 
				restrictToRowCrops, restrictToGrasses);
		}
		
		AreaStats fs = null;
		ObjectNode fieldStats = null;
		if (farmId != null) {
			db.Farm f = db.Farm.find.byId(farmId);
			if (f != null) {
				List<JsonNode> features = new ArrayList<>();
				
				for (FieldGeometry fd: f.fieldGeometry) {
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
				
				fs = new AreaStats(modelResults).forRasterizedFields(layer).compute();
				
				try {
					fieldStats = JsonNodeFactory.instance.objectNode();
					
					for (FieldGeometry fd: f.fieldGeometry) {
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
							Integer histogramCt = 40;
							Histogram hs = stats.getHistogram(histogramCt, stats.getMin(), stats.getMax()); 
							Integer ct = stats.getCounted();
							Float area = ct * 100.0f;
							Double sum = stats.getSum();
							Float min = stats.getMin();
							Float max = stats.getMax();
							Float mid = (min + max) * 0.5f;
							Float mean = stats.getMean();
							Float median = stats.getMedian();
							results += " »FIELD CELLS: " + ct + "\n";
							results += String.format("  Area: %.2f(ac)   %.2f(km2) \n", area / 4047.0f, area / 1000000.0f);
							results += " »YIELD STATS \n";
							results += String.format("  Total Yield: %.2f\n", sum);
							results += String.format("  Min: %.2f  Mid: %.2f  Max: %.2f \n", min, mid, max);
							results += String.format("  Mean: %.2f\n", mean);
							results += String.format("  Median: %.2f\n", median);
							results += " »HISTOGRAM (" + histogramCt + " bins) \n";
							results += hs.toString();
							
							Json.addToPack(fieldStats, fs_idx.toString(), Json.pack("area", area, "mean", mean));
						}
						results += "─────────────────────────────────────────────────────\n";
						
					//	logger.info(results);
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
						logger.error(e.toString());
					}
				}
			}
		}
		else { // no farm 
			
			fs = new AreaStats(modelResults).forExtents(ext.x1(), ext.y2(), ext.width(), ext.height());
			fs.compute();
			Stats stats;
			try {
				stats = fs.getAreaStats();
				stats.debug(true);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.toString());
			}
		}
		
		// TODO: may want to skip clipping process if raster modelResults is the full raster...
		float clipped[][] = Extract.now(modelResults, ext.x1(), ext.y2(), ext.width(), ext.height());

		// Downsampler...
/*		int maxSize = 400;
		// restrict output size and resample
		if (ext.mWidth > maxSize || ext.mHeight > maxSize) {
			int newW = ext.mWidth, newH = ext.mHeight;
			if (ext.mWidth > ext.mHeight) {
				newW = maxSize;
				newH = Math.round((ext.mHeight / (float)ext.mWidth) * maxSize);
			}
			else if (ext.mWidth < ext.mHeight) {
				newW = Math.round((ext.mWidth / (float)ext.mHeight) * maxSize);
				newH = maxSize;
			}
			else {
				newW = newH = maxSize;
			}
			
			logger.info("wh [" + ext.mWidth + "," + ext.mHeight + "] new wh [" + newW + "," + newH + "]");
			try {
				clipped = Downsampler.max(clipped, ext.mWidth, ext.mHeight, newW, newH);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			ext.mHeight = newH;
			ext.mWidth = newW;
		}
*/				
		File fp = new File(FileService.getDirectory() + "yes" + pngCounter + ".png");
		result  = (ObjectNode)RasterToPNG.save(clipped, ext.width(), ext.height(), fp);
		
		Json.addToPack(result, "url", "renders/yes" + pngCounter + ".png", 
						"extent", ext.toJson());	
		
		if (fieldStats != null) {
			Json.addToPack(result, "fields", fieldStats);
		}
		
		pngCounter++;
		return ok(result);
	}
	
	// Simple fake model - just copy a layer into a returnable dataset
	//-------------------------------------------------------------------------------------
	public class RasterInspection implements ModelComputation {

		private Layer_Base mLayer;
		private Transform mDataTransform = new PassThrough();
		
		public RasterInspection dataLayer(String dataLayer) {
			mLayer = Layer_Base.getLayer(dataLayer);
			return this;
		}
		public RasterInspection setTransform(Transform dataTransform) {
			mDataTransform = dataTransform;
			return this;
		}
		
		// ext is the computation extent
		// options can contain a filter object. That filter object (if exists) can contain
		//	compare: (greater-than, less-than) and value: #
		public float[][] compute(Extents ext, JsonNode options) throws Exception  { 
			
			Boolean filterEnabled = false;
			Boolean lessThan = true; // if false, then is in greater-than mode
			Float filterValue = 0.0f;
			if (options != null) {
				JsonNode filter = options.get("filter");
				if (filter != null) {
					filterEnabled = true;
					lessThan = Json.safeGetOptionalString(filter, "compare", "less-than").equalsIgnoreCase("less-than");
					filterValue = (float)Json.safeGetOptionalInteger(filter, "value", 0);
				}
			}
			
			if (mLayer == null) throw new Exception("Did not configure a data layer");
			float [][] dataIn = mLayer.getFloatData();
			float [][] dataOut = new float[mLayer.getHeight()][mLayer.getWidth()];

			if (filterEnabled) {
				logger.info("Using filtered view...");
				for (int y = ext.y2(); y < ext.y1(); y++) {
					for (int x = ext.x1(); x < ext.x2(); x++) {
						Float data = mDataTransform.apply(dataIn[y][x]);
						if (lessThan && data < filterValue) {
							dataOut[y][x] = data;
						}
						else if (!lessThan && data > filterValue) { // in greater than mode
							dataOut[y][x] = data;
						}
						else { // filter it out
							dataOut[y][x] = -9999.0f;
						}
					}
				}
			}
			else { // straight copy
				for (int y = ext.y2(); y < ext.y1(); y++) {
					for (int x = ext.x1(); x < ext.x2(); x++) {
						float data = mDataTransform.apply(dataIn[y][x]);
						dataOut[y][x] = data;
					}
				}
			}
			
			return dataOut;
		}
	}
	
	public enum Crop {
		ECornGrain,
		ECornSilage,
		ESoybeans,
		ESoylage,
		EAlfalfa
	};
	
	//-------------------------------------------------------------------------------------
	public class YieldModel implements ModelComputation {
		
		public float[][] compute(Extents ext, JsonNode options) throws Exception { 
			
			logger.error(options.toString());
			String cropModel = Json.safeGetOptionalString(options, "crop", "corn");
			Yield test = new Yield();
			return test.compute(cropModel);
			
/*			Boolean filterEnabled = false;
			Boolean lessThan = true; // if false, then is in greater-than mode
			Float filterValue = 0.0f;
			
			Crop cropCode = Crop.ECornGrain;
			
			if (options != null) {
				JsonNode filter = options.get("filter");
				if (filter != null) {
					filterEnabled = true;
					lessThan = Json.safeGetOptionalString(filter, "compare", "less-than").equalsIgnoreCase("less-than");
					filterValue = (float)Json.safeGetOptionalInteger(filter, "value", 0);
				}
				// Extract crop....
				String crop = Json.safeGetOptionalString(options, "crop", "corn-grain");
				if (crop.equalsIgnoreCase("corn-grain")) cropCode = Crop.ECornGrain;
				else if (crop.equalsIgnoreCase("corn-silage")) cropCode = Crop.ECornSilage;
				else if (crop.equalsIgnoreCase("soybeans")) cropCode = Crop.ESoybeans;
				else if (crop.equalsIgnoreCase("soylage")) cropCode = Crop.ESoylage;
				else if (crop.equalsIgnoreCase("alfalfa")) cropCode = Crop.EAlfalfa;
				
				// Oats + grasses incoming
			}
			
			
			Layer_Base slope = Layer_Base.getLayer("slope");
			float slopeData[][] = slope.getFloatData();
			float silt[][] = Layer_Base.getLayer("silt_perc").getFloatData();
			float depth[][] = Layer_Base.getLayer("soil_depth").getFloatData();
			float cec[][] = Layer_Base.getLayer("cec").getFloatData();
			float [][] yield = new float[slope.getHeight()][slope.getWidth()];
			
			final float soyCoefficient = 1.2f		// Correct for techno advances
					* 0.0585f;						// conversion to Mg per Ha
			
			final float maxCropSlope = (float)(Math.tan(15 * Math.PI / 180.0f) * 100.0f);
			
			for (int y = ext.mY1; y < ext.mY2; y++) {
				for (int x = ext.mX1; x < ext.mX2; x++) {
					
					float value = 0;
					float _slope = slopeData[y][x], _depth = depth[y][x], _silt = silt[y][x], _cec = cec[y][x];

					// Corn
					//----------------------------------------------------------
					if (cropCode == Crop.ECornGrain || cropCode == Crop.ECornSilage) {
						
						if (_slope > maxCropSlope) {
							value = -9999.0f;
						}
						else {
							// Corn roots don't exceed much below 150cm
							if (_depth > 150) _depth = 150;
							
							// Yield
	//						value =  22.0f - 1.05f * _slope + 0.19f * _depth + (0.817f / 100.0f) * _silt + 1.32f * _cec
							value =  3.08f - 0.11f * _slope + 0.012f * _depth + 0.07f * _silt + 0.03f * _cec; 
							value *= 1.3f; // techno advances
	//								* cornCoefficient;
							
							if (value < 1.5f) value = 1.5f;
							else if (value > 20) value = 20;
							
							// contribution of stover doubles yield
							if (cropCode == Crop.ECornSilage) value *= 2.0f;
						}
					}
					//----------------------------------------------------------
					else if (cropCode == Crop.ESoybeans || cropCode == Crop.ESoylage) {
						
						// soy roots don't exceed much below 150cm
						if (_depth > 150) _depth = 150;
						
						// Yield
						value = 6.37f - 0.34f * _slope + 0.065f * _depth + (0.278f / 100.0f) * _silt + 0.437f * _cec 
								* soyCoefficient;
					
						// TODO: sensible clamps?
						if (value < 0.5) value = 0.5f;
						else if (value > 18) value = 18;
						
						// soy plant residue contributes 2.5x biomass
						if (cropCode == Crop.ESoylage) value *= 2.5f;
					}
					
					if (filterEnabled) {
						if ((lessThan && value >= filterValue) ||
								(!lessThan && value <= filterValue)) {
							value = -9999.0f;
						}
					}
					yield[y][x] = value;
				}
			}
			
			return yield;*/
		}
	}
	
	// Moving window example
	//-------------------------------------------------------------------------------------
	public class BirdModel implements ModelComputation {
		
		public float[][] compute(Extents ext, JsonNode options) throws Exception { 
			
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
		
						if (habitatIndex < 0.1f) {
							habitatIndex = -9999.0f;
						}
				
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
	
	//-------------------------------------------------------------------------------------
	public class PLossModel implements ModelComputation {
		
		public float[][] compute(Extents ext, JsonNode options) throws Exception { 
			
			String landcoverCode = "cc";
			String coverCropCode = "cc";
			String tillageCode = "nt";
			
			Boolean applySnapPlusTransmission = false;
			float initialP = 32.0f;
			float percManure = 50.0f;
			float percSynth = 50.0f;
			
			if (options != null) {
				landcoverCode 	= Json.safeGetOptionalString(options, "landcover", 	"cc"); // cc, cg, dr
				coverCropCode 	= Json.safeGetOptionalString(options, "cover-crop", "cc"); // cc or nc
				tillageCode 	= Json.safeGetOptionalString(options, "tillage", 	"nt"); // nt, s_cd, f_cd, s_mp, f_mp
				initialP 		= Json.safeGetOptionalFloat(options, "soil-p", 		initialP);
				percManure 		= Json.safeGetOptionalFloat(options, "perc-manure", percManure);
				percSynth 		= Json.safeGetOptionalFloat(options, "perc-fertilizer", percSynth);
				applySnapPlusTransmission = Json.safeGetOptionalBoolean(options, "snap-plus-transmission", applySnapPlusTransmission);
			}
		
			float intercept 	= 0;
			float c_ManureApp 	= 0;
			float c_SyntheticApp= 0;
			float c_initialP 	= 0;
			float c_Slope 		= 0;
			float c_SlopeLength = 0;
			float c_LS 			= 0;
			float c_K 			= 0;
			float c_sandtotal_r = 0;
			float c_silttotal_r = 0;
			float c_OM 			= 0;
			Layer_Base s_layer = Layer_Base.getLayer("slope");
			
			// Continuous Corn
			//-------------------------------------------------------------
			if (landcoverCode.equalsIgnoreCase("cc") || landcoverCode.equalsIgnoreCase("cg")) {
				LinearModel lm = null;
				
				try {
					String model = landcoverCode.equalsIgnoreCase("cc") ? 
							String.format("continuous_corn.csv") :
							String.format("cash_grain.csv");
					String modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss/" + model;
			        String definition = new String( Files.readAllBytes( Paths.get(modelPath) ));
					lm = new LinearModel().init(definition);

//					lm.debug();
//					lm.measureResponse();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				lm.setConstant("manure_app", percManure);
				lm.setConstant("synthetic_app", percSynth);
				lm.setConstant("initial_p", initialP);
				lm.setIntercept(coverCropCode + "_" + tillageCode);
				
				float[][] ploss = new float[s_layer.getHeight()][s_layer.getWidth()];
				
				for (int y = ext.y2(); y < ext.y1(); y++) {
					for (int x = ext.x1(); x < ext.x2(); x++) {
						ploss[y][x] = lm.calculate(x, y).floatValue();
					}
				}
				
				return ploss; 
			}				
			// Dairy Rotation
			//-------------------------------------------------------------
			else if (landcoverCode.equalsIgnoreCase("dr")) {
				if (coverCropCode.equalsIgnoreCase("cc")) {
					switch(tillageCode) {
					case "fc":
						intercept = 0.302634741679f;
						break;
					case "sc":
						intercept = 0.072659495219f;
						break;
					case "fm":
						intercept = 0.337371095760f;
						break;
					case "sm":
						intercept = 0.399588747324f;
						break;
					case "nt":
						intercept = -0.992183005201f;
						break;
					default:
						logger.error("Unhandled tillage code:" + tillageCode);
					}
				}
				else if (coverCropCode.equalsIgnoreCase("nc")) {
					switch(tillageCode) {
					case "fc":
						intercept = 0.605307890508f;
						break;
					case "sc":
						intercept = 0.375332644048f;
						break;
					case "fm":
						intercept = 0.640044244590f;
						break;
					case "sm":
						intercept = 0.702261896153f;
						break;
					case "nt":
						intercept = -0.689509856372f;
						break;
					default:
						logger.error("Unhandled tillage code:" + tillageCode);
					}
				}
				else {
					logger.error("Unhandled cover crop code:" + coverCropCode);
				}
				
				c_ManureApp 	= 0.002952260183f; 
				c_SyntheticApp 	= 0.002324369178f; 
				c_initialP 		= 0.003523159931f; 
				c_Slope 		= 0.058817887648f;  
				c_SlopeLength 	= -0.004091007642f; 
				c_LS 			= -0.102274335208f; 
				c_K 			= 2.303621117181f; 
				c_sandtotal_r 	= -0.003640751209f; 
				c_silttotal_r 	= 0.003903691381f; 
				c_OM 			= 0.088916995709f; 	
			}
			// Corn Soy Oats
			//-------------------------------------------------------------
			else if (landcoverCode.equalsIgnoreCase("cso")) {
				if (coverCropCode.equalsIgnoreCase("cc")) {
					switch(tillageCode) {
					case "fc":
						intercept = 0.304055046729f;
						break;
					case "sc":
						intercept = 0.186830609034f;
						break;
					case "fm":
						intercept = 0.555635922028f;
						break;
					case "sm":
						intercept = 0.590269197502f;
						break;
					case "nt":
						intercept = -1.260135314598f;
						break;
					default:
						logger.error("Unhandled tillage code:" + tillageCode);
					}
				}
				else if (coverCropCode.equalsIgnoreCase("nc")) {
					switch(tillageCode) {
					case "fc":
						intercept = 0.556564164589f;
						break;
					case "sc":
						intercept = 0.439339726894f;
						break;
					case "fm":
						intercept = 0.808145039888f;
						break;
					case "sm":
						intercept = 0.842778315362f;
						break;
					case "nt":
						intercept = -1.007626196738f;
						break;
					default:
						logger.error("Unhandled tillage code:" + tillageCode);
					}
				}
				else {
					logger.error("Unhandled cover crop code:" + coverCropCode);
				}
				
				c_ManureApp 	= 0.003055080991f;
				c_SyntheticApp 	= 0.002707272611f;
				c_initialP 		= 0.003382967272f;
				c_Slope 		= 0.055077232960f;
				c_SlopeLength 	= -0.004251675881f;
				c_LS 			= -0.090935063767f;
				c_K 			= 2.252541965754f;
				c_sandtotal_r 	= 0.000942354835f;
				c_silttotal_r 	= 0.008457536411f;
				c_OM 			= 0.084705451215f;	
			}
			// Pasture
			//-------------------------------------------------------------
			else if (landcoverCode.startsWith("p")) {
				switch(landcoverCode) {
				case "pr": // pasture, rotational
					intercept = -3.387384002923f;
					break;
				case "pl": // pasture, continuous, low density
					intercept = -2.803379129937f;
					break;
				case "ph": // pasture, continuous, high density
					intercept = -1.261009446664f;
					break;
				default:
					logger.error("Unhandled tillage code:" + tillageCode);
				}
				
				c_ManureApp 	= 0.002814638921f;
				c_SyntheticApp 	= 0.001230465139f;
				c_initialP 		= 0.007878286120f;
				c_Slope 		= 0.041020426882f;
				c_SlopeLength 	= -0.001120477836f;
				c_LS 			= -0.088859139728f;
				c_K 			= 2.065562020684f;
				c_sandtotal_r 	= 0.000161475720f;
				c_silttotal_r 	= 0.005943307442f;
				c_OM 			= 0.110005660671f;
			}
			else {
				logger.error("Unhandled landcover code:" + landcoverCode);
			}
			
			float slope[][] = s_layer.getFloatData();
			float silt[][] = Layer_Base.getLayer("silt_perc").getFloatData();
			float sand[][] = Layer_Base.getLayer("sand_perc").getFloatData();
			float om[][] = Layer_Base.getLayer("om").getFloatData();
			float k[][] = Layer_Base.getLayer("k").getFloatData();
			float ls[][] = Layer_Base.getLayer("ls").getFloatData();
			float slp_len[][] = Layer_Base.getLayer("slope_length").getFloatData();
			
			float distanceToWater[][] = Layer_Base.getLayer("distance_to_water").getFloatData();
			
			float [][] ploss = new float[s_layer.getHeight()][s_layer.getWidth()];
			
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					
					float value = 0;
					float _slope =slope[y][x];
					if (_slope > 40 || sand[y][x] > 65) value = -9999.0f;
					else {
						value = intercept	+
							percManure 	* c_ManureApp +
							percSynth 	* c_SyntheticApp + 
							initialP 	* c_initialP + 
							_slope 		* c_Slope +
							k[y][x] 	* c_K +
							ls[y][x] 	* c_LS +
							sand[y][x] 	* c_sandtotal_r +
							silt[y][x] 	* c_silttotal_r +
							om[y][x] 	* c_OM +
							slp_len[y][x] * c_SlopeLength;
						
						value = (float)Math.exp(value);// * 0.024f; // convert to pounds per 100m2
						
						if (applySnapPlusTransmission) {
							double fixedSlope = 1;
							double distance = distanceToWater[y][x] * 3.281; // convert meters to feet
							double factor = 0.9415f + 
									0.02122 * fixedSlope + -0.001345 * fixedSlope * fixedSlope + 
									-0.00003511 * distance + 0.0000000007114 * distance * distance;
							
							if (factor > 1) factor = 1.0;
							else if (factor < 0.4f) factor = 0.4f;
							
							value *= factor;
						}
					}
					ploss[y][x] = value;
				}
			}
			
			return ploss;
		}
	}
	
	//-------------------------------------------------------------------------------------
	public class AwePLossModel implements ModelComputation {
		
		public float[][] compute(Extents ext, JsonNode options) throws Exception { 
			
			Layer_Integer wl = Layer_CDL.get();
			int [][] wl_data = wl.getIntData();
			
			int cg = wl.stringToMask("Cash Grain");
			int cc = wl.stringToMask("Continuous Corn");
			int dr = wl.stringToMask("Dairy Rotation");
			int ps = wl.stringToMask("Pasture","Cool-season Grass","Warm-season Grass");
			
			int totalMask = cg | cc | dr | ps;
			
			float initialP = 32.0f;
			float percManure = 20.0f;
			float percSynth = 70.0f;
		
			float intercept 	= 0;
			float c_ManureApp 	= 0;
			float c_SyntheticApp= 0;
			float c_initialP 	= 0;
			float c_Slope 		= 0;
			float c_SlopeLength = 0;
			float c_LS 			= 0;
			float c_K 			= 0;
			float c_sandtotal_r = 0;
			float c_silttotal_r = 0;
			float c_OM 			= 0;
			
			Layer_Base s_layer = Layer_Base.getLayer("slope");
			
			float slope[][] = Layer_Base.getLayer("slope").getFloatData();
			float silt[][] = Layer_Base.getLayer("silt_perc").getFloatData();
			float sand[][] = Layer_Base.getLayer("sand_perc").getFloatData();
			float om[][] = Layer_Base.getLayer("om").getFloatData();
			float k[][] = Layer_Base.getLayer("k").getFloatData();
			float ls[][] = Layer_Base.getLayer("ls").getFloatData();
			float slp_len[][] = Layer_Base.getLayer("slope_length").getFloatData();
			float distanceToWater[][] = Layer_Base.getLayer("distance_to_water").getFloatData();
			
			float [][] ploss = new float[s_layer.getHeight()][s_layer.getWidth()];
			
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					
					float value = 0;
					float _slope =slope[y][x];
					
					if ((wl_data[y][x] & totalMask) <= 0) {
						ploss[y][x] = -9999.0f;
						continue;
					}
					else if (_slope > 40 || sand[y][x] > 65) {
						ploss[y][x] = -9999.0f;
						continue;
					}
					
					//-------------------------------------------------------------
					if ((wl_data[y][x] & cc) > 0) {
						intercept 		= -0.633812975746179f;// -1.78349735992302f;
						c_ManureApp 	= 0.00359206803416f;
						c_SyntheticApp 	= 0.00252837564114f;
						c_initialP 		= 0.00337386285029f;
						c_Slope 		= 0.06857203548177f;
						c_SlopeLength 	= -0.0034862913967f;
						c_LS 			= -0.1297218451209f;
						c_K 			= 2.25186015396712f;
						c_sandtotal_r 	= 0.00388356367312f;
						c_silttotal_r 	= 0.01238149285957f;
						c_OM 			= 0.07928443256857f;	
					}
					//-------------------------------------------------------------
					else if ((wl_data[y][x] & cg) > 0) {
						intercept 		= -0.031632721834f;//-1.600321633430f;
						c_ManureApp 	= 0.00277537745450f;
						c_SyntheticApp 	= 0.00210478873069f;
						c_initialP 		= 0.00328978161121f;
						c_Slope 		= 0.07268108586729f; 
						c_SlopeLength 	= -0.00374287052429f;
						c_LS 			= -0.14212765910246f;
						c_K 			= 2.43583905849407f;
						c_sandtotal_r 	= 0.00588279213064f;
						c_silttotal_r 	= 0.01441431827988f;
						c_OM 			= 0.08113536990160f;	
					}
					// Dairy Rotation
					//-------------------------------------------------------------
					else if ((wl_data[y][x] & dr) > 0) {
						intercept 		= 0.375332644048f;// -0.992183005201f;
						c_ManureApp 	= 0.002952260183f; 
						c_SyntheticApp 	= 0.002324369178f; 
						c_initialP 		= 0.003523159931f; 
						c_Slope 		= 0.058817887648f;  
						c_SlopeLength 	= -0.004091007642f; 
						c_LS 			= -0.102274335208f; 
						c_K 			= 2.303621117181f; 
						c_sandtotal_r 	= -0.003640751209f; 
						c_silttotal_r 	= 0.003903691381f; 
						c_OM 			= 0.088916995709f; 	
					}
					// Pasture
					//-------------------------------------------------------------
					else if ((wl_data[y][x] & ps) > 0) {
						intercept 		= -1.261009446664f;//-2.803379129937f;
						c_ManureApp 	= 0.002814638921f;
						c_SyntheticApp 	= 0.001230465139f;
						c_initialP 		= 0.007878286120f;
						c_Slope 		= 0.041020426882f;
						c_SlopeLength 	= -0.001120477836f;
						c_LS 			= -0.088859139728f;
						c_K 			= 2.065562020684f;
						c_sandtotal_r 	= 0.000161475720f;
						c_silttotal_r 	= 0.005943307442f;
						c_OM 			= 0.110005660671f;
					}
					
					value = intercept	+
						percManure 	* c_ManureApp +
						percSynth 	* c_SyntheticApp + 
						initialP 	* c_initialP + 
						_slope 		* c_Slope +
						k[y][x] 	* c_K +
						ls[y][x] 	* c_LS +
						sand[y][x] 	* c_sandtotal_r +
						silt[y][x] 	* c_silttotal_r +
						om[y][x] 	* c_OM +
						slp_len[y][x] * c_SlopeLength;
					
					value = (float)Math.exp(value) * 0.024f; // convert to pounds per 100m2
					
					double fixedSlope = 1;
					double distance = distanceToWater[y][x] * 3.281; // convert meters to feet
					double factor = 0.9415f + 
							0.02122 * fixedSlope + -0.001345 * fixedSlope * fixedSlope + 
							-0.00003511 * distance + 0.0000000007114 * distance * distance;
					
					if (factor > 1) factor = 1.0;
					else if (factor < 0) factor = 0.0;
					
					value *= factor;
					if (value < 0.05f) value = -9999.0f;
					else if (value > 2) value = 2.0f;
					ploss[y][x] = value;  
				}
			}
			// slope:
			//	< 1: factor = 0.2
			//	1-3: factor = 0.3
			//	3-4: factor = 0.4
			// else (>4): factor = 0.5
			
			// LS = (((slope/((10000+(slope^2))^0.5))*4.56)+(slope/(10000+(slope^2))^0.5)^2*(65.41)+0.065)*(slopelength(in meters)/72.6)^(factor)
			
			// a = slope / ((10000 + slope * slope)^0.5) * 4.56
			// b = slope / (10000 + slope * slope)^0.5
			//		b = b * b * 65.41 + 0.065
			//		b = b * Math.pow(slopelength/72.6, factor);
			// LS = a + b;
			
			/*double slp = 3.0;
			double factor = 0.5;
			double slpLen = 76;
			double a = Math.pow(10000.0 + slp * slp, 0.5);
			double b = Math.pow(Math.pow(slp/(10000.0 + slp * slp), 0.5), 2.0);
			double lls = ((slp / a * 4.56) + b * 65.41+0.065) * Math.pow(slpLen / 72.6, 0.5);
			*/
			return ploss;
		}
	}

}
