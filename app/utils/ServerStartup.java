package utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import data_types.Farm;
import play.inject.ApplicationLifecycle;
import java.util.concurrent.CompletableFuture;

//-----------------------------------------------------------------------
@Singleton
public class ServerStartup {
	
	@Inject
	public ServerStartup(ApplicationLifecycle lifecycle) {
				
        lifecycle.addStopHook(() -> {
            return CompletableFuture.completedFuture(null);
        });
        
        makeFakeFarms();
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

















