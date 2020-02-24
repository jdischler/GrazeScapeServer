package models;

import play.*;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//------------------------------------------------------------------------------
public class Model_Base
{
    private static final Logger logger = LoggerFactory.getLogger("app");
	
	private static String mBasePath = "./layerData/";
	
	// MASTER switch to turn on debug logging for all models. If set to false,
	//	any given model itself can opt to turn on logging for just itself;
	protected static final boolean ALL_MODELS_DEBUG_LOGGING = true;	
	
	
	protected Boolean mbInitialized = false;
	protected int[][] mRotationData;
	protected int mWidth, mHeight;
	
	//--------------------------------------------------------------------------
	public Boolean initialize() {
		
		mbInitialized = true;
		
		return true;
	}
	
	//--------------------------------------------------------------------------
	protected File getFileForPath(String subPath, String modelFile) {
		
		File testPath = new File(mBasePath + subPath + "/");
		if (!testPath.exists()) {
			logger.info(testPath.toString() + " does not exist. Attempting to create...");
			testPath.mkdirs();
		}
		return new File(mBasePath + subPath + "/" + modelFile + ".dss");
	}
}

