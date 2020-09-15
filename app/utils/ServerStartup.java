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
import dataCreation.*;

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

}

