package utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.inject.ApplicationLifecycle;
import query.Layer_Base;

import java.util.concurrent.CompletableFuture;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.FileService;
/*
import query.Layer_CDL;
import query.Layer_Integer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;
*/
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

//		GenerateLS.go();
 //       GenerateLS.validateLS_Function();
        
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
        //testPMML_rf();
        // Tests linear model
        // testPMML_linear();
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
		logger.info("|  Application Path: " + mEnv.rootPath().toString());
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
	
/*	//--------------------------------------------------------------------------
	private void testPMML_rf() {
		String modelPath = ServerStartup.getApplicationRoot() + "/conf/testModels/contPast.pmml";
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

		InputField if_density = null;
		InputField if_initialP = null;
		InputField if_slope = null;
		InputField if_OM = null;
		InputField if_totalP = null;
		InputField if_manureDM = null;
		InputField if_slopeLen = null;
		InputField if_silt = null;
		InputField if_k = null;
		InputField if_soilDepth = null;
		InputField if_ls = null;
		
		FieldValue inputValue;
		
		for (InputField if_: inputFields) {
			switch(if_.getFieldName().toString().toLowerCase()) {
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
			case "totalp":
				if_totalP = if_;
				inputValue = if_.prepare(33.4f);
				arguments.put(if_.getName(), inputValue);
				break;
			case "totalmanuredm":
				if_manureDM = if_;
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
		
		for (int y = 0; y < wl.getHeight() / 250; y++) {
			for (int x = 0; x < wl.getWidth() / 250; x++) {
				
				logger.error("New P Sweep -----------------------");
				for (Integer p_sweep = 10; p_sweep <= 250; p_sweep += 5) {
					logger.error(" Initial P Would Be: " + p_sweep);
					try {
						if (y == 0  && x == 0) logger.info("Before setting slope");
						inputValue = if_slope.prepare(slope[y][x]);
						arguments.put(if_slope.getName(), inputValue);
		
						if (y == 0  && x == 0) logger.info("Before setting silt");
						inputValue = if_silt.prepare(percSilt[y][x]);
						arguments.put(if_silt.getName(), inputValue);
		
						if (y == 0  && x == 0) logger.info("Before setting k");
						inputValue = if_k.prepare(k[y][x]);
						arguments.put(if_k.getName(), inputValue);
		
						if (y == 0  && x == 0) logger.info("Before setting slopelen");
						inputValue = if_slopeLen.prepare(slopeLen[y][x]);
						arguments.put(if_slopeLen.getName(), inputValue);
		
						if (y == 0  && x == 0) logger.info("Before setting depth");
						inputValue = if_soilDepth.prepare(soilDepth[y][x]);
						arguments.put(if_soilDepth.getName(), inputValue);
		
						if (y == 0  && x == 0) logger.info("Before setting ls");
						inputValue = if_ls.prepare(ls[y][x]);
						arguments.put(if_ls.getName(), inputValue);
				
						inputValue = if_initialP.prepare(p_sweep);
						arguments.put(if_initialP.getName(), inputValue);
						
						if (y == 0 && x == 0) {
							logger.info("Before arguments debug");
							arguments.forEach((ke,va) -> { 
								if (va != null) {
									logger.info(String.format("key <%s> <%s>", ke, va.asString()));
								}
							});
						}
						
						Map<FieldName, ?> results = evaluator.evaluate(arguments);
						
						// Decoupling results from the JPMML-Evaluator runtime environment
						if (y == 0  && x == 0) logger.info("Before decoding");
						Map<String, ?> resultRecord = EvaluatorUtil.decodeAll(results);
						
						if (y == 0  && x == 0) logger.info("Before reading hashmap");
						Object val = resultRecord.get("Predicted_PI");
						if (val != null) {
							logger.info(String.format("key <Predicted PI> <%s>", val.toString()));
						};
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

	private void testPMML_linear() {
		
	}
	*/
}

