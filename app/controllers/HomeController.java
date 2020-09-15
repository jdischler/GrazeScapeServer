package controllers;

import javax.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import play.mvc.*;
import play.mvc.Http.Session;
import play.twirl.api.Content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import analysis.*;
import query.*;
import raster.Extents;
import db.FieldGeometry;
import db.RotationalFrequency;
import io.ebean.Ebean;
import io.ebean.SqlRow;
import models.BirdHabitat;
import models.DryMatter;
import models.LinearModel;
import models.RasterInspector;
import models.RasterModel;
import models.RasterModel.RasterResult;
import models.Yield;
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
  
/*GrazeScape 2.0 Design Considerations

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
    private static Boolean DETAILED_LOG = true;
    
    private static AtomicLong pngCounter = new AtomicLong(1l);

    private Cache<String,session.Session> mSessions = null;
    
	//------------------------------------------------------------------
	@Inject
	public HomeController(ServerStartup startup) {
		mSessions = Caffeine.newBuilder().expireAfterAccess(8, TimeUnit.DAYS).build();
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
	public Result getOptions(String type) {
		
		JsonNode js = null;
		if (DETAILED_LOG) {
			logger.debug("Get options type: " + type);
		}
		if (type.equalsIgnoreCase("rotationalFrequency")) {
			js = RotationalFrequency.toJson();
		}
		return ok(js);
	}
	
	//------------------------------------------------------------------
	public Result getFarms() {
		return ok(db.Farm.getAllAsGeoJson());
	}
	
	//------------------------------------------------------------------
	public Result createOperation(Http.Request request) {
		
		return ok(db.Farm.createFarm(request));
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
	//------------------------------------------------------------------
	public Result getFields(Long farmId) {
		
		db.Farm f = db.Farm.find.byId(farmId);
		if (f != null) {
			return ok(f.getFieldShapesAsGeoJson());
		}
		return ok("Farm did not exist");
	}
	
	//------------------------------------------------------------------
	public Result addField(Http.Request request) {
		return ok(db.Farm.addField(request));
	}

	//------------------------------------------------------------------
	public Result modifyFields(Http.Request request) {
		return ok(db.FieldGeometry.modifyFields(request));
	}
	
	public Result deleteFields(Http.Request request) {
		return ok(db.Farm.deleteFields(request));
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
		//JsonNode options = node.get("options");
		
		RasterModel mc = null;
		if (mode.equalsIgnoreCase("bird-habitat")) mc = new BirdHabitat();
		else if (mode.equalsIgnoreCase("crop-yield")) mc = new Yield();
		else if (mode.equalsIgnoreCase("dry-matter")) mc = new DryMatter();
		else if (mode.equalsIgnoreCase("p-loss")) mc = new PLossModel();
		else if (mode.equalsIgnoreCase("p-loss-real")) mc = new AwePLossModel();
		
		else if (mode.equalsIgnoreCase("slope")) {
			mc = new RasterInspector().dataLayer("slope");//.setTransform(new SlopePercentToAngle());
		}
		else if (mode.equalsIgnoreCase("ssurgo-slope")) {
			mc = new RasterInspector().dataLayer("ssurgo_slope");
		}
		else if (mode.equalsIgnoreCase("soil-depth")) mc = new RasterInspector().dataLayer("soil_depth");
		else if (mode.equalsIgnoreCase("dem")) mc = new RasterInspector().dataLayer("dem");
		else if (mode.equalsIgnoreCase("dist-water")) mc = new RasterInspector().dataLayer("distance_to_water");
		else if (mode.equalsIgnoreCase("perc-sand")) mc = new RasterInspector().dataLayer("sand_perc");
		else if (mode.equalsIgnoreCase("perc-silt")) mc = new RasterInspector().dataLayer("silt_perc");
		else if (mode.equalsIgnoreCase("perc-clay")) mc = new RasterInspector().dataLayer("clay_perc");
		else if (mode.equalsIgnoreCase("om")) mc = new RasterInspector().dataLayer("om");
		else if (mode.equalsIgnoreCase("k")) mc = new RasterInspector().dataLayer("k");
		else if (mode.equalsIgnoreCase("ls")) mc = new RasterInspector().dataLayer("ls");
		else if (mode.equalsIgnoreCase("ksat")) mc = new RasterInspector().dataLayer("ksat");
		else if (mode.equalsIgnoreCase("cec")) mc = new RasterInspector().dataLayer("cec");
		else if (mode.equalsIgnoreCase("ph")) mc = new RasterInspector().dataLayer("ph");
		else if (mode.equalsIgnoreCase("slope-length")) mc = new RasterInspector().dataLayer("slope_length");
		else if (mode.equalsIgnoreCase("t")) mc = new RasterInspector().dataLayer("t");
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
		JsonNode restrictions = node.get("restrictions");
		JsonNode options = node.get("options");
		if (DETAILED_LOG) {
			if (restrictions != null) {
				logger.info("Has model restrictions:" + restrictions.toPrettyString());
			}
			if (options != null) {
				logger.debug("Has model options:" + options.toPrettyString());
			}
		}
		
		Map<Integer,Integer> deMask = new HashMap<>();
		for (int i = 1; i < 31; i++) {
			deMask.put((1 << (i-1)), i);
		}
		int w = ext.width(), h = ext.height();
		int x1 = ext.x1(), y1 = ext.y2();
		byte[][] data = new byte[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				data[y][x] = deMask.get(cdlData[y + y1][x + x1]).byteValue();
			}
		}
		
		JsonNode slopeRestriction = restrictions.get("restrict_by_slope");
		if (slopeRestriction != null) {
			Float value = utils.Json.safeGetOptionalFloat(slopeRestriction, "value", 10.0f);
			Boolean lessThan = utils.Json.safeGetOptionalString(slopeRestriction, "compare", "less-than").equalsIgnoreCase("less-than");
			data = Filters.restrictToSlope(data, w, h, lessThan, value);
		}
		
		JsonNode landcoverRestriction = restrictions.get("restrict_to_landcover");
		if (landcoverRestriction != null) {
			Boolean restrictToRowCrops = utils.Json.safeGetOptionalBoolean(landcoverRestriction, "row_crops", false);
			Boolean restrictToGrasses = utils.Json.safeGetOptionalBoolean(landcoverRestriction, "grass_pasture", false);
			if (restrictToRowCrops || restrictToGrasses) {
				data = Filters.restrictToAgriculture(data, w, h, restrictToRowCrops, restrictToGrasses);
			}
		}
		
		ObjectNode result = null;
		Long idx = pngCounter.getAndIncrement();
		File fp = new File(FileService.getDirectory() + "out" + idx + ".png");
		result  = (ObjectNode)RasterToPNG.saveClassified(data, cdl.getKey(), w, h, fp);
		
		Json.addToPack(result, "url", "renders/out" + idx + ".png", 
						"extent", ext.toJson());	
		
		return ok(result);
	}
	
	//-------------------------------------------------------
	public Result computeModel(Http.Request request, RasterModel modelFunction) {
	
		JsonNode settings = request.body().asJson();

		Long farmId = utils.Json.safeGetOptionalLong(settings, "farm_id", null); 

		JsonNode restrictions = settings.get("restrictions");
		JsonNode options = settings.get("options");
		if (DETAILED_LOG) {
			if (restrictions != null) {
				logger.info("Has model restrictions:" + restrictions.toPrettyString());
			}
			if (options != null) {
				logger.debug("Has model options:" + options.toPrettyString());
			}
		}
		// Extent array node can be missing, in which case we get a clipper that extracts the entire area
		Extents ext = new Extents().fromJson((ArrayNode)settings.get("extent")).toRasterSpace();
		
		final Integer rasterHeight = 2600, rasterWidth = 1500;
		ObjectNode result = null;
		
		float[][] modelResults = null;
		RasterResult rr = null;
		try {
			rr = modelFunction.compute(ext, settings); 
			modelResults = rr.rasterOut;
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1.toString());
		}
		
		//--------------------------------------------------------------------
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
							modelResults[y][x] = Layer_Float.getNoDataValue();
						}
						else if (!lessThan && modelResults[y][x] <= value) {
							modelResults[y][x] = Layer_Float.getNoDataValue();
						}
					}
				}
			}
		}

		JsonNode slopeRestriction = restrictions.get("restrict_by_slope");
		if (slopeRestriction != null) {
			Float value = utils.Json.safeGetOptionalFloat(slopeRestriction, "value", 10.0f);
			Boolean lessThan = utils.Json.safeGetOptionalString(slopeRestriction, "compare", "less-than").equalsIgnoreCase("less-than");
			modelResults = Filters.restrictToSlope(modelResults, ext, lessThan, value);
		}
		
		JsonNode landcoverRestriction = restrictions.get("restrict_to_landcover");
		if (landcoverRestriction != null) {
			Boolean restrictToRowCrops = utils.Json.safeGetOptionalBoolean(landcoverRestriction, "row_crops", false);
			Boolean restrictToGrasses = utils.Json.safeGetOptionalBoolean(landcoverRestriction, "grass_pasture", false);
			if (restrictToRowCrops || restrictToGrasses) {
				modelResults = Filters.restrictToAgriculture(modelResults, ext, 
					restrictToRowCrops, restrictToGrasses);
			}
		}
		
		AreaStats fs = null;
		ObjectNode fieldStats = null;
		
		JsonNode fieldRestriction = restrictions.get("restrict_to_operation");
		if (farmId != null && fieldRestriction != null) {
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
				
				// for each feature...render the Integer value "f_id" inside of feature.property
				int layer[][] = RasterizeFeatures.withIntProperty(features, "f_id");
			
				for (int y = 0; y < rasterHeight; y++) {
					for (int x = 0; x < rasterWidth; x++) {
						if (layer[y][x] <= 0) modelResults[y][x] = Layer_Float.getNoDataValue();
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
							if (DETAILED_LOG) {
								results += " »FIELD CELLS: " + ct + "\n";
								results += String.format("  Area: %.2f(ac)   %.2f(km2) \n", area / 4047.0f, area / 1000000.0f);
								results += " »YIELD STATS \n";
								results += String.format("  Total Yield: %.2f\n", sum);
								results += String.format("  Min: %.2f  Mid: %.2f  Max: %.2f \n", min, mid, max);
								results += String.format("  Mean: %.2f\n", mean);
								results += String.format("  Median: %.2f\n", median);
								results += " »HISTOGRAM (" + histogramCt + " bins) \n";
								results += hs.toString();
							}
							
							Json.addToPack(fieldStats, fs_idx.toString(), Json.pack("area", area, "mean", mean));
						}
						
						if (DETAILED_LOG) {
							results += "─────────────────────────────────────────────────────\n";
							logger.info(results);
						}
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
			
			fs = new AreaStats(modelResults).forExtents(ext);
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
		Long idx = pngCounter.getAndIncrement();
		File fp = new File(FileService.getDirectory() + "out" + idx + ".png");
		result  = (ObjectNode)RasterToPNG.save(clipped, ext.width(), ext.height(), fp);
		
		Json.addToPack(result, "url", "renders/out" + idx + ".png", 
						"extent", ext.toJson());	
		
		if (fieldStats != null) {
			Json.addToPack(result, "fields", fieldStats);
		}
	//	if (rr.clientData != null) {
			Json.addToPack(result, "model-results", rr.clientData);
	//	}
		
		return ok(result);
	}

	//-------------------------------------------------------------------------------------
	public class PLossModel implements RasterModel {
		
		public RasterResult compute(Extents ext, JsonNode options) throws Exception { 
			
			String landcoverCode = "cc";
			String coverCropCode = "cc";
			String tillageCode = "nt";
			
			Boolean applySnapPlusTransmission = false;
			float initialP = 32.0f;
			float percManure = 50.0f;
			float percSynth = 50.0f;
			int onContour = 1;
			
			if (options != null) {
				landcoverCode 	= Json.safeGetOptionalString(options, "landcover", 	"cc"); // cc, cg, dr
				coverCropCode 	= Json.safeGetOptionalString(options, "cover-crop", "cc"); // cc or nc
				tillageCode 	= Json.safeGetOptionalString(options, "tillage", 	"nt"); // nt, scu, sch, fch, smb, fmb
				initialP 		= Json.safeGetOptionalFloat(options, "soil-p", 		initialP);
				percManure 		= Json.safeGetOptionalFloat(options, "manure-dm", percManure);
				percSynth 		= Json.safeGetOptionalFloat(options, "total-p", percSynth);
				onContour 		= Json.safeGetOptionalInteger(options, "on-contour", 1);
				applySnapPlusTransmission = Json.safeGetOptionalBoolean(options, "snap-plus-transmission", applySnapPlusTransmission);
			}
		
			Layer_Base s_layer = Layer_Base.getLayer("slope");
			float distanceToWater[][] = Layer_Base.getLayer("distance_to_water").getFloatData();
			
			// Continuous Corn
			String model = null;
			switch(landcoverCode) {
			case "cc":
				model = "cont_corn";
				// Cover crop doesn't have all tillage practices...redirect to spring cultivation
				//	only no till cover crop (continuous corn) has onContour so otherwise force to zero...
				if (coverCropCode.equalsIgnoreCase("cc")) {
					switch(tillageCode) {
						case "fmb":
						case "smb":
						case "fch":
							tillageCode = "scu";
							onContour = 0;
							break;
						case "scu":
						case "sch":
							onContour  = 0;
							break;
					}
				}
				else {
					// No Cover crop has no cases where contour would be supported yet
					onContour = 0;
				}
				break;
			case "dr":
				model = "dairy_rot";
				// Cover crop doesn't have all tillage practices...redirect to spring cultivation
				//	only no till cover crop (dairy rotation) has onContour so otherwise force to zero...
				if (coverCropCode.equalsIgnoreCase("cc")) {
					switch(tillageCode) {
						case "fmb":
						case "smb":
						case "fch":
							tillageCode = "scu";
							onContour = 0;
							break;
						case "scu":
						case "sch":
							onContour  = 0;
							break;
					}
				}
				else if (!tillageCode.equalsIgnoreCase("fch")) {
					// Only fall chisel no cover can do contour so zero everything else
					onContour = 0;
				}
				break;
			case "ep":
				model = "pasture_est";
				if (!tillageCode.equalsIgnoreCase("fch")) {
					// Only fall chisel no cover can do contour so zero everything else
					onContour = 0;
				}
				break;
			case "pr":
				model = "pasture_rotational";
				coverCropCode = null; tillageCode = null;
				break;
			case "pl":
				model = "pasture_cont_low";
				coverCropCode = null; tillageCode = null;
				break;
			case "ph":
				model = "pasture_cont_high";
				coverCropCode = null; tillageCode = null;
				break;
			}
			
			if (coverCropCode != null && tillageCode != null) {
				if (landcoverCode.equalsIgnoreCase("ep")) {
					// prairie establishment has no cover crop option
					model = String.format("%s_%s_cont%d", model, tillageCode, onContour);
				}
				else {
					model = String.format("%s_%s_%s_cont%d", model, tillageCode, coverCropCode, onContour);
				}
			}
			model += ".csv";
			LinearModel lm = null;
				
			try {
				String modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss_new/" + model;
				if (DETAILED_LOG) {
					logger.error("model path is: " + modelPath);
				}
				lm = new LinearModel().init(modelPath);

//					lm.debug();
//					lm.measureResponse();
			} catch (Exception e) {
				e.printStackTrace();
			}
				
			lm.setConstant("@manure_dm", percManure);
			lm.setConstant("@total_p", percSynth);
			lm.setConstant("@initial_p", initialP);
			
			float[][] ploss = new float[s_layer.getHeight()][s_layer.getWidth()];
			
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					float value = lm.calculate(x, y).floatValue();
							
					if (applySnapPlusTransmission && !Layer_Float.isNoDataValue(value)) {
						float fixedSlope = 1;// TODO: what is this value?
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
			
			return new RasterResult(ploss); 
		}
	}
	
	//-------------------------------------------------------------------------------------
	public class AwePLossModel implements RasterModel {
		
		public RasterResult compute(Extents ext, JsonNode options) throws Exception { 
			
			Layer_Integer wl = Layer_CDL.get();
			int [][] wl_data = wl.getIntData();
			
			int cg = wl.stringToMask("Cash Grain");
			int cc = wl.stringToMask("Continuous Corn");
			int dr = wl.stringToMask("Dairy Rotation");
			int ps = wl.stringToMask("Hay", "Pasture","Cool-season Grass","Warm-season Grass", "Reed Canary Grass");
			
			int totalMask = cg | cc | dr | ps;
			
			float initialP = 32.0f;
			float percManure = 2.0f;
			float percSynth = 70.0f;
		
			Layer_Base s_layer = Layer_Base.getLayer("slope");
			float distanceToWater[][] = Layer_Base.getLayer("distance_to_water").getFloatData();

			LinearModel contCorn = null, dairyRotation = null, cashGrain = null, pastureLow = null;
			
			try {
				String modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss_new/cont_corn_scu_cc_cont0.csv";
				contCorn = new LinearModel().init(modelPath);
				contCorn.setConstant("@manure_dm", 1.0f);
				contCorn.setConstant("@total_p", 40.0f);
				contCorn.setConstant("@initial_p", 32.0f);

				modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss_new/dairy_rot_scu_cc_cont0.csv";
		        dairyRotation = new LinearModel().init(modelPath);
		        dairyRotation.setConstant("@manure_dm", 1.0f);
		        dairyRotation.setConstant("@total_p", 40.0f);
		        dairyRotation.setConstant("@initial_p", 32.0f);

				modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss/cash_grain.csv";
		        cashGrain = new LinearModel().init(modelPath);
		        cashGrain.setConstant("@manure_app", percManure);
		        cashGrain.setConstant("@synthetic_app", percSynth);
		        cashGrain.setConstant("@initial_p", initialP);
		        cashGrain.setIntercept("nc_nt");
				
				modelPath = ServerStartup.getApplicationRoot() + "/conf/modelDefs/ploss_new/pasture_cont_low.csv";
		        pastureLow = new LinearModel().init(modelPath);
		        dairyRotation.setConstant("@manure_dm", 2.4f);
		        dairyRotation.setConstant("@total_p", 20.0f);
		        dairyRotation.setConstant("@initial_p", 25.0f);
			} catch (Exception e) {
				e.printStackTrace();
			}

			float[][] ploss = new float[s_layer.getHeight()][s_layer.getWidth()];
			
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					float value = Layer_Float.getNoDataValue();
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
			
			return new RasterResult(ploss); 
		}
	}

}
