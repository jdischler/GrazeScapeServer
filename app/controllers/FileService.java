package controllers;

import play.mvc.*;
import utils.ServerStartup;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//--------------------------------------------------------------------------
public class FileService extends Controller {

	private static final Logger logger = LoggerFactory.getLogger("app");

	private static String mDirPath; // call init() to configure this
     
	//--------------------------------------------------------------------------
	public static final String getDirectory() {
		return mDirPath;
	}
	
	//--------------------------------------------------------------------------
	public Result getFile(String file) {
		
		File myfile = new File (mDirPath + file);
		if (myfile.exists()) {
			return ok(myfile);
		}
		else {
			logger.warn("File <" + file + "> not found");
			return notFound();
		}
	}
	
	//-----------------------------------------------------------------------
	public static void init() {
		
		mDirPath = ServerStartup.getApplicationRoot().toString() + "/dynamicFiles/";

		logger.info(" > Ensuring dynamicFiles path <" + mDirPath + "> exists...");
		File dir = new File(mDirPath);
		dir.mkdirs();

		logger.info(" > Cleaning dynamicFiles directory...");
		for(File file: dir.listFiles()) { 
		    if (!file.isDirectory()) { 
		        file.delete();
		    }
		}
	}
	
}
