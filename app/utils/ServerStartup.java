package utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import data_types.Farm;
import play.api.db.evolutions.ApplicationEvolutions;
import play.inject.ApplicationLifecycle;
import query.Layer_Base;
import scala.concurrent.ExecutionContext;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorSystem;

//-----------------------------------------------------------------------
@Singleton
public class ServerStartup {
	
    private static final Logger logger = LoggerFactory.getLogger("app");
    
	@Inject
	public ServerStartup(ApplicationLifecycle lifecycle, ActorSystem as, ExecutionContext ec, ApplicationEvolutions ae) {
				
		logger.error("ServerStartup.....");
        lifecycle.addStopHook(() -> {
        	Layer_Base.removeAllLayers();
            return CompletableFuture.completedFuture(null);
        });
    
        systemReport("  Welcome!");        
  //      makeFakeFarms();
        
        Layer_Base.cacheLayers();

        systemReport("  Server is ready!");
        
        // TODO: I want a callback for when the evolution is actually done.
		as.scheduler()
			.scheduleOnce(
				Duration.ofSeconds(10L), // delay
				() -> db.PostServerStart.initialize(),
				ec
			);
	
       // db.Farm.dbInit();
        //db.Farm.testLoadShapes();
        
		//db.Farm.testLoadShapes();
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
		//logger.error("|  Application Path: " + apPath.toString());
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
	
	private void makeFakeFarms() {
		
		new Farm().track().setLocation(-10115067.0f, 5369543.0f).setName("Fancy Farms")
			.setOwner("Annie Mae");
		
		new Farm().track().setLocation(-10120363.0f, 5369991.0f).setName("Breezy Acres")
			.setOwner("Max Winston").setAddress("123 Easy Breezy Ln");
		
		new Farm().track().setLocation(-10116145.0f, 5384521.0f).setName("Lands 'Organica")
			.setOwner("The Wheelers");
		
		new Farm().track().setLocation(-10119551.0f, 5369942.0f).setName("Fancy Farms II")
			.setOwner("Annie Mae");
		
		new Farm().track().setLocation(-10114431.0f, 5385252.0f).setName("Misty Hollow")
			.setOwner("Ichabod Chrane").setAddress("Hazy Hollow Ln");
		
		new Farm().track().setLocation(-10117380.0f, 5388271.0f).setName("Butterville")
			.setOwner("Whippie Mae");

		new Farm().track().setLocation(-10114987.0f, 5373191.0f).setName("Fancy Farms")
			.setOwner("Annie Mae");
	
		new Farm().track().setLocation(-10114233.0f, 5373836.0f).setName("Breezy Acres")
			.setOwner("Max Winston").setAddress("123 Easy Breezy Ln");
		
		new Farm().track().setLocation(-10116145.0f, 5384521.0f).setName("Lands 'Organica")
			.setOwner("The Wheelers");
		
		new Farm().track().setLocation(-10115965.0f, 5376772.0f).setName("Fancy Farms II")
			.setOwner("Annie Mae");
		
		new Farm().track().setLocation(-10118982.0f, 5380191.0f).setName("Misty Hollow")
			.setOwner("Ichabod Chrane").setAddress("Hazy Hollow Ln");
		
		new Farm().track().setLocation(-10120344.0f, 5384748.0f).setName("Butterville")
			.setOwner("Whippie Mae");
		
		new Farm().track().setLocation(-10116090.0f, 5383085.0f).setName("Butterville")
			.setOwner("Whippie Mae");
	}
}

















