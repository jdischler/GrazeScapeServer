package utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.inject.ApplicationLifecycle;
import query.Layer_Base;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.io.*;

import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.FileService;
import dataCreation.CreateSoilPropertiesSnapshot;
import query.Layer_CDL;
import query.Layer_Float;
import query.Layer_Integer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;

//-----------------------------------------------------------------------
@Singleton
public class ServerStartup {
	
    private static final Logger logger = LoggerFactory.getLogger("app");
    private static play.Environment mEnv;
    
	@Inject
	public ServerStartup(ApplicationLifecycle lifecycle, play.Environment env) {
		
		mEnv = env;
		
		logger.error("ServerStartup.....");
        lifecycle.addStopHook(() -> {
        	Layer_Base.removeAllLayers();
            return CompletableFuture.completedFuture(null);
        });
    
        systemReport("  Welcome!");        
    
		FileService.init();
        
        Layer_Base.cacheLayers();

        systemReport("  Server is ready!");

        
        // TODO: I want a callback for when the evolution is actually done.
/*		as.scheduler()
			.scheduleOnce(
				Duration.ofSeconds(10L), // delay
				() -> db.PostServerStart.initialize(),
				ec
			);
*/	
		
       // db.Farm.dbInit();
        //db.Farm.testLoadShapes();

        // Tests random forest model
         testPMML_rf();
        // Tests linear model
        // testPMML_linear();
        
         // Rarely used data generation tools
         CreateSoilPropertiesSnapshot.outputUniquePermutations();
         CreateSoilPropertiesSnapshot.randomSampleByPercent(10.0f); // pick 10% of soils
         // GenerateLS.go();
         // GenerateLS.validateLS_Function();
	}
	
	//--------------------------------------------------------------------------
	public static final File getApplicationRoot() {
		return mEnv.rootPath();
	}
	
	//--------------------------------------------------------------------------
	private void systemReport(String customMessage) {
		
		float unitConversion = (1024.0f * 1024.0f); // bytes -> MB
		String appVersionInfo = "GrazeScape Server v0.1";
		String unitName = "MB";
		
		logger.info("┌───────────────────────────────────────────────────────┼");
		logger.info("» " + appVersionInfo);
		logger.info("│ " + customMessage);
		logger.info("├───────────────────────────────────────────────────────┼");
		logger.info("|  Application Path: " + getApplicationRoot().toString());
		logger.info("│  Available Processors: " + 
			Integer.toString(Runtime.getRuntime().availableProcessors()));
		logger.info("│  Total Free Memory: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().freeMemory() / unitConversion)) +
				unitName);
		logger.info("│  Current Total Memory in Use: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().totalMemory() / unitConversion)) +
				unitName);
		logger.info("│  Maximum Memory for Use: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().maxMemory() / unitConversion)) +
				unitName);

		logger.info("└───────────────────────────────────────────────────────┼");
		logger.info("");
	}
	
	//--------------------------------------------------------------------------
	private void testPMML_rf() {
//		String modelPath = ServerStartup.getApplicationRoot() + "/conf/testModels/contPast.pmml";
		String modelPath = ServerStartup.getApplicationRoot() + "/conf/testModels/CC_35_7.pmml";
		Evaluator evaluator = null;
		PerformanceTimer timer = new PerformanceTimer();
		try {
			LoadingModelEvaluatorBuilder lmeb = new LoadingModelEvaluatorBuilder();
			lmeb.load(new File(modelPath));
			logger.info("PMML Load: " + timer.stringSeconds(3));timer.restartTimer();
			
			evaluator = (Evaluator)lmeb.build();
			logger.info("PMML Evaluator build: " + timer.stringSeconds(3));timer.restartTimer();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Perforing the self-check
		evaluator.verify();
		logger.info("PMML self-check: " + timer.stringSeconds(3));timer.restartTimer();

		// Printing input (x1, x2, .., xn) fields
		List<? extends InputField> inputFields = evaluator.getInputFields();
		System.out.println("Input fields: " + inputFields);

		// Printing primary result (y) field(s)
		List<? extends TargetField> targetFields = evaluator.getTargetFields();
		System.out.println("Target field(s): " + targetFields);

		// Printing secondary result (eg. probability(y), decision(y)) fields
		List<? extends OutputField> outputFields = evaluator.getOutputFields();
		System.out.println("Output fields: " + outputFields);
		
		Map<FieldName, FieldValue> arguments = new HashMap<>();
	
		float[][] slope 	= Layer_Base.getFloatData("slope");
		float[][] slopeLen 	= Layer_Base.getFloatData("slope_length");
		float[][] percSilt 	= Layer_Base.getFloatData("silt_perc");
		float[][] soilDepth = Layer_Base.getFloatData("soil_depth");
		float[][] ls 		= Layer_Base.getFloatData("ls");
		float[][] k 		= Layer_Base.getFloatData("k");
		
		Layer_Integer wl = Layer_CDL.get();
		float [][] outputData = new float[wl.getHeight()][wl.getWidth()];
		
	//	FieldValue inputValue = inputField.prepare(rawValue);

		InputField if_contour = null;
		InputField if_density = null;
		InputField if_initialP = null;
		InputField if_slope = null;
		InputField if_OM = null;
		InputField if_totalP205 = null;
		InputField if_manureDM_lbs = null;
		InputField if_slopeLen = null;
		InputField if_silt = null;
		InputField if_k = null;
		InputField if_soilDepth = null;
		InputField if_ls = null;
		
		FieldValue inputValue;
		
		for (InputField if_: inputFields) {
			switch(if_.getFieldName().toString().toLowerCase()) {
			case "contour":
				if_contour = if_;
				inputValue = if_.prepare("1");
				arguments.put(if_.getName(), inputValue);
				break;
			case "density":
				if_density = if_;
				inputValue = if_.prepare("lw");
				arguments.put(if_.getName(), inputValue);
				break;
			case "initialp":
				if_initialP = if_;
				inputValue = if_.prepare(33.4f);
				arguments.put(if_.getName(), inputValue);
				break;
			case "slope":
				if_slope = if_;
				break;
			case "om":
				if_OM = if_;
				inputValue = if_.prepare(3.4f);
				arguments.put(if_.getName(), inputValue);
				break;
			case "totalp2o5":
				if_totalP205 = if_;
				inputValue = if_.prepare(33.4f);
				arguments.put(if_.getName(), inputValue);
				break;
			case "totalmanuredm_lbs":
				if_manureDM_lbs = if_;
				inputValue = if_.prepare(1400.f);
				arguments.put(if_.getName(), inputValue);
				break;
			case "totalp":
				inputValue = if_.prepare(33.4f);
				arguments.put(if_.getName(), inputValue);
				break;
			case "totalmanuredm":
				inputValue = if_.prepare(1.4f);
				arguments.put(if_.getName(), inputValue);
				break;
			case "slopelenusle.r":
				if_slopeLen = if_;
				break;
			case "silt":
				if_silt = if_;
				break;
			case "k":
				if_k = if_;
				break;
			case "total.depth":
				if_soilDepth = if_;
				break;
			case "lssurgo":
				if_ls = if_;
				break;
			}
		}
		
		logger.info("Before raster evaluation: " + timer.stringSeconds(3));timer.restartTimer();
		
		int y = 300;//for (int y = 300; y < wl.getHeight() / 250; y++) {
		int x = 300;
		{
			{
/*				for (int y = 0; y < wl.getHeight() / 250; y++) {
					for (int x = 0; x < wl.getWidth() / 250; x++) {
*/				
				logger.error("Start P Sweep -----------------------");
				for (Integer p_sweep = 0; p_sweep <= 210; p_sweep += 5) {
					try {
						inputValue = if_slope.prepare(slope[y][x]);
						arguments.put(if_slope.getName(), inputValue);
		
						inputValue = if_silt.prepare(percSilt[y][x]);
						arguments.put(if_silt.getName(), inputValue);
		
						inputValue = if_k.prepare(k[y][x]);
						arguments.put(if_k.getName(), inputValue);
		
						inputValue = if_slopeLen.prepare(slopeLen[y][x]);
						arguments.put(if_slopeLen.getName(), inputValue);
		
						inputValue = if_soilDepth.prepare(soilDepth[y][x]);
						arguments.put(if_soilDepth.getName(), inputValue);
		
						inputValue = if_ls.prepare(ls[y][x]);
						arguments.put(if_ls.getName(), inputValue);
				
						inputValue = if_initialP.prepare(p_sweep);
						arguments.put(if_initialP.getName(), inputValue);
						
						if (p_sweep == 0) {
							logger.info("Debug input arguments");
							arguments.forEach((ke,va) -> { 
								if (va != null) {
									logger.info(String.format("key <%s> <%s>", ke, va.asString()));
								}
							});
						}
						
						Map<FieldName, ?> results = evaluator.evaluate(arguments);
						
						// Decoupling results from the JPMML-Evaluator runtime environment
						Map<String, ?> resultRecord = EvaluatorUtil.decodeAll(results);
						
						Object val = resultRecord.get("Predicted_PI");
						if (val != null) {
							logger.info(String.format("InitialP = %s  -> Predicted PI = %s", 
								p_sweep.toString() ,val.toString()));
						};
					}
					catch(Exception e) {
						//e.printStackTrace();
						logger.error(e.toString());
					}
				}
				logger.error("Start SlopeLen Sweep -----------------------");
				inputValue = if_initialP.prepare(25);
				arguments.put(if_initialP.getName(), inputValue);
				for (Integer slopeLen_sweep = 18; slopeLen_sweep <= 76; slopeLen_sweep += 1) {
					try {
						inputValue = if_slope.prepare(slope[y][x]);
						arguments.put(if_slope.getName(), inputValue);
		
						inputValue = if_silt.prepare(percSilt[y][x]);
						arguments.put(if_silt.getName(), inputValue);
		
						inputValue = if_k.prepare(k[y][x]);
						arguments.put(if_k.getName(), inputValue);
		
						inputValue = if_slopeLen.prepare(slopeLen_sweep);
						arguments.put(if_slopeLen.getName(), inputValue);
		
						inputValue = if_soilDepth.prepare(soilDepth[y][x]);
						arguments.put(if_soilDepth.getName(), inputValue);
		
						inputValue = if_ls.prepare(ls[y][x]);
						arguments.put(if_ls.getName(), inputValue);
						
						Map<FieldName, ?> results = evaluator.evaluate(arguments);
						
						// Decoupling results from the JPMML-Evaluator runtime environment
						Map<String, ?> resultRecord = EvaluatorUtil.decodeAll(results);
						
						Object val = resultRecord.get("Predicted_PI");
						if (val != null) {
							logger.info(String.format("SlopeLen = %s  -> Predicted PI = %s", 
								slopeLen_sweep.toString() ,val.toString()));
						}
					}
					catch(Exception e) {
						//e.printStackTrace();
						logger.error(e.toString());
					}
				}
				logger.error("Start Perc Silt Sweep -----------------------");
				inputValue = if_slopeLen.prepare(32);
				arguments.put(if_slopeLen.getName(), inputValue);
				for (Integer silt_sweep = 0; silt_sweep <= 80; silt_sweep += 2) {
					try {
						inputValue = if_slope.prepare(slope[y][x]);
						arguments.put(if_slope.getName(), inputValue);
		
						inputValue = if_silt.prepare(silt_sweep);
						arguments.put(if_silt.getName(), inputValue);
		
						inputValue = if_k.prepare(k[y][x]);
						arguments.put(if_k.getName(), inputValue);
		
						inputValue = if_soilDepth.prepare(soilDepth[y][x]);
						arguments.put(if_soilDepth.getName(), inputValue);
		
						inputValue = if_ls.prepare(ls[y][x]);
						arguments.put(if_ls.getName(), inputValue);
						
						Map<FieldName, ?> results = evaluator.evaluate(arguments);
						
						// Decoupling results from the JPMML-Evaluator runtime environment
						Map<String, ?> resultRecord = EvaluatorUtil.decodeAll(results);
						
						Object val = resultRecord.get("Predicted_PI");
						if (val != null) {
							logger.info(String.format("Silt Perc = %s  -> Predicted PI = %s", 
									silt_sweep.toString() ,val.toString()));
						}
					}
					catch(Exception e) {
						//e.printStackTrace();
						logger.error(e.toString());
					}
				}
				logger.error("Start Soil Depth Sweep -----------------------");
				inputValue = if_silt.prepare(32);
				arguments.put(if_silt.getName(), inputValue);
				for (Integer soildDepth_sweep = 20; soildDepth_sweep <= 210; soildDepth_sweep += 5) {
					try {
						inputValue = if_slope.prepare(slope[y][x]);
						arguments.put(if_slope.getName(), inputValue);
		
						inputValue = if_k.prepare(k[y][x]);
						arguments.put(if_k.getName(), inputValue);
		
						inputValue = if_soilDepth.prepare(soildDepth_sweep);
						arguments.put(if_soilDepth.getName(), inputValue);
		
						inputValue = if_ls.prepare(ls[y][x]);
						arguments.put(if_ls.getName(), inputValue);
						
						Map<FieldName, ?> results = evaluator.evaluate(arguments);
						
						// Decoupling results from the JPMML-Evaluator runtime environment
						Map<String, ?> resultRecord = EvaluatorUtil.decodeAll(results);
						
						Object val = resultRecord.get("Predicted_PI");
						if (val != null) {
							logger.info(String.format("Soil Depth = %s  -> Predicted PI = %s", 
									soildDepth_sweep.toString() ,val.toString()));
						}
					}
					catch(Exception e) {
						//e.printStackTrace();
						logger.error(e.toString());
					}
				}
				
				logger.error("Start Slope Sweep -----------------------");
				inputValue = if_soilDepth.prepare(180);
				arguments.put(if_soilDepth.getName(), inputValue);
				for (Integer slope_sweep = 0; slope_sweep <= 74; slope_sweep += 2) {
					if (slope_sweep < 30) slope_sweep--;
					try {
						inputValue = if_slope.prepare(slope_sweep);
						arguments.put(if_slope.getName(), inputValue);
		
						inputValue = if_k.prepare(k[y][x]);
						arguments.put(if_k.getName(), inputValue);
				
						inputValue = if_ls.prepare(ls[y][x]);
						arguments.put(if_ls.getName(), inputValue);
						
						Map<FieldName, ?> results = evaluator.evaluate(arguments);
						
						// Decoupling results from the JPMML-Evaluator runtime environment
						Map<String, ?> resultRecord = EvaluatorUtil.decodeAll(results);
						
						Object val = resultRecord.get("Predicted_PI");
						if (val != null) {
							logger.info(String.format("Slope = %s  -> Predicted PI = %s", 
									slope_sweep.toString() ,val.toString()));
						}
					}
					catch(Exception e) {
						//e.printStackTrace();
						logger.error(e.toString());
					}
				}
				
				
			}
		}
		logger.info("After raster evaluation: " + timer.stringSeconds(3));timer.restartTimer();
	}

	/*
	private void testPMML_linear() {
		
	}
	*/
}

