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
			if (!user.isPresent()) {//user. .isEmpty()) {
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

	
/*	
 	// Restriction options
 	restrict_by_value: 		{ compare: '<' || '>', 	value: # 			}
	restrict_to_fields: 	{ aggregate: t/f 							}
	restrict_to_landcover: 	{ row_crops: t/f,		grass_pasture: t/f	}

*/
	
	public Result fetchImage(Http.Request request) {
		
		JsonNode node = request.body().asJson();
		String mode = Json.safeGetOptionalString(node, "model", "bird-habitat");
		JsonNode options = node.get("options");
		
		ModelComputation mc = null;
		if (mode.equalsIgnoreCase("bird-habitat")) mc = new BirdModel();
		else if (mode.equalsIgnoreCase("crop-yield")) mc = new YieldModel();
		else if (mode.equalsIgnoreCase("p-loss")) mc = new PLossModel();
		else if (mode.equalsIgnoreCase("p-loss-real")) mc = new AwePLossModel();
		
		else if (mode.equalsIgnoreCase("slope")) {
			mc = new RasterInspection().dataLayer("slope");//.setTransform(new SlopePercentToAngle());
		}
		else if (mode.equalsIgnoreCase("ssurgo-slope")) {
			mc = new RasterInspection().dataLayer("ssurgo_slope");
		}
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

		JsonNode restrictions = node.get("restrictions");
		JsonNode options = node.get("options");
		if (restrictions != null) {
			logger.info("Has model restrictions:" + restrictions.toString());
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
		
		JsonNode valueRestriction = restrictions.get("restrict_by_value");
		if (valueRestriction != null) {
			Float value = utils.Json.safeGetOptionalFloat(valueRestriction, "value", 10.0f);
			Boolean lessThan = utils.Json.safeGetOptionalString(valueRestriction, "compare", "less-than").equalsIgnoreCase("less-than");
			Boolean clamped = utils.Json.safeGetOptionalBoolean(valueRestriction, "clamped", false);
			if (clamped) {
				for (int y = ext.y2(); y < ext.y1(); y++) {
					for (int x = ext.x1(); x < ext.x2(); x++) {
						if (lessThan && modelResults[y][x] >= value) {
							modelResults[y][x] = value;
						}
						else if (!lessThan && modelResults[y][x] <= value) {
							modelResults[y][x] = value;
						}
					}
				}
			}
			else {
				for (int y = ext.y2(); y < ext.y1(); y++) {
					for (int x = ext.x1(); x < ext.x2(); x++) {
						if (lessThan && modelResults[y][x] >= value) {
							modelResults[y][x] = -9999.0f;
						}
						else if (!lessThan && modelResults[y][x] <= value) {
							modelResults[y][x] = -9999.0f;
						}
					}
				}
			}
		}

		JsonNode slopeRestriction = restrictions.get("restrict_by_slope");
		if (slopeRestriction != null) {
			Float value = utils.Json.safeGetOptionalFloat(slopeRestriction, "value", 10.0f);
			Boolean lessThan = utils.Json.safeGetOptionalString(slopeRestriction, "compare", "less-than").equalsIgnoreCase("less-than");
			float slope[][] = Layer_Base.getLayer("slope").getFloatData();
			
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					if (lessThan && slope[y][x] >= value) {
						modelResults[y][x] = -9999.0f;
					}
					else if (!lessThan && slope[y][x] <= value) {
						modelResults[y][x] = -9999.0f;
					}
				}
			}
		}
		
		JsonNode landcoverRestriction = restrictions.get("restrict_to_landcover");
		if (landcoverRestriction != null) {
			Boolean restrictToRowCrops = utils.Json.safeGetOptionalBoolean(landcoverRestriction, "row_crops", false);
			Boolean restrictToGrasses = utils.Json.safeGetOptionalBoolean(landcoverRestriction, "grass_pasture", false);
			if (restrictToRowCrops || restrictToGrasses) {
				modelResults = FloatFilters.restrictToAgriculture(modelResults, ext, 
					restrictToRowCrops, restrictToGrasses);
			}
		}
		
		Long farmId = null;
		JsonNode fieldRestriction = restrictions.get("restrict_to_fields");
		if (fieldRestriction != null) {
			farmId = utils.Json.safeGetOptionalLong(fieldRestriction, "farm_id"); 
		}
		
		AreaStats fs = null;
		ObjectNode fieldStats = null;
		if (farmId != null) {
			Boolean aggregate = utils.Json.safeGetOptionalBoolean(fieldRestriction, "aggregate", false);
			
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
				if (aggregate) {
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
			
			if (options != null && !utils.Json.safeGetOptionalBoolean(options, "slope_as_percent", true)) {
				this.setTransform(new SlopePercentToAngle());
			}
			
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
		
			Layer_Base s_layer = Layer_Base.getLayer("slope");
			float distanceToWater[][] = Layer_Base.getLayer("distance_to_water").getFloatData();
			
			// Continuous Corn
			String model = null;
			switch(landcoverCode) {
			case "cc":
				model = "continuous_corn.csv";
				break;
			case "cg":
				model = "cash_grain.csv";
				break;
			case "dr":
				model = "dairy_rotation.csv";
				break;
			case "cso":
				model = "corn_soy_oats.csv";
				break;
			case "pr":
				model = "pasture_rot.csv";
				coverCropCode = null; tillageCode = null;
				break;
			case "pl":
				model = "pasture_low.csv";
				coverCropCode = null; tillageCode = null;
				break;
			case "ph":
				model = "pasture_high.csv";
				coverCropCode = null; tillageCode = null;
				break;
			}
				
			LinearModel lm = null;
				
			try {
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
			if (coverCropCode != null && tillageCode !=null) {
				lm.setIntercept(coverCropCode + "_" + tillageCode);
			}
			
			float[][] ploss = new float[s_layer.getHeight()][s_layer.getWidth()];
			
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					float value = lm.calculate(x, y).floatValue();
							
					if (applySnapPlusTransmission && !Layer_Float.isNoDataValue(value)) {
						float fixedSlope = 1;
						float distance = distanceToWater[y][x] * 3.281f; // convert meters to feet
						float factor = 0.9415f + 
								0.02122f * fixedSlope + -0.001345f * fixedSlope * fixedSlope + 
								-0.00003511f * distance + 0.0000000007114f * distance * distance;
						
						if (factor > 1) factor = 1.0f;
						else if (factor < 0.4f) factor = 0.4f;
						
						value *= factor;
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
			int ps = wl.stringToMask("Hay", "Pasture","Cool-season Grass","Warm-season Grass", "Reed Canary Grass");
			
			int totalMask = cg | cc | dr | ps;
			
			float initialP = 32.0f;
			float percManure = 20.0f;
			float percSynth = 70.0f;
		
			Layer_Base s_layer = Layer_Base.getLayer("slope");
			float distanceToWater[][] = Layer_Base.getLayer("distance_to_water").getFloatData();

			LinearModel contCorn = null, dairyRotation = null, cashGrain = null, pastureLow = null;
			
			try {
				String modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss/continuous_corn.csv";
		        String definition = new String( Files.readAllBytes( Paths.get(modelPath) ));
				contCorn = new LinearModel().init(definition);
				contCorn.setConstant("manure_app", percManure);
				contCorn.setConstant("synthetic_app", percSynth);
				contCorn.setConstant("initial_p", initialP);
				contCorn.setIntercept("cc_nt");

				modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss/dairy_rotation.csv";
		        definition = new String( Files.readAllBytes( Paths.get(modelPath) ));
		        dairyRotation = new LinearModel().init(definition);
		        dairyRotation.setConstant("manure_app", percManure);
		        dairyRotation.setConstant("synthetic_app", percSynth);
		        dairyRotation.setConstant("initial_p", initialP);
		        dairyRotation.setIntercept("cc_nt");

				modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss/cash_grain.csv";
		        definition = new String( Files.readAllBytes( Paths.get(modelPath) ));
		        cashGrain = new LinearModel().init(definition);
		        cashGrain.setConstant("manure_app", percManure);
		        cashGrain.setConstant("synthetic_app", percSynth);
		        cashGrain.setConstant("initial_p", initialP);
		        cashGrain.setIntercept("cc_nt");
				
				modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss/pasture_rot.csv";
		        definition = new String( Files.readAllBytes( Paths.get(modelPath) ));
		        pastureLow = new LinearModel().init(definition);
		        pastureLow.setConstant("manure_app", 0.0f);
		        pastureLow.setConstant("synthetic_app", 80.0f);
		        pastureLow.setConstant("initial_p", 5.0f);
			} catch (Exception e) {
				e.printStackTrace();
			}

			float[][] ploss = new float[s_layer.getHeight()][s_layer.getWidth()];
			
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					float value = -9999.0f;
					if ((wl_data[y][x] & cg) > 0) {
						value = cashGrain.calculate(x, y).floatValue();
					}
					else if ((wl_data[y][x] & cc) > 0) {
						value = contCorn.calculate(x, y).floatValue();
					}
					else if ((wl_data[y][x] & dr) > 0) {
						value = dairyRotation.calculate(x, y).floatValue();
					}
					else if ((wl_data[y][x] & ps) > 0) {
						value = pastureLow.calculate(x, y).floatValue();
					}
							
					ploss[y][x] = value;
				}
			}
			
			return ploss; 
		}
	}

}
