package dataCreation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import query.Layer_Base;
import query.Layer_CDL;
import query.Layer_Float;
import query.Layer_Integer;

//--------------------------------------------------------------------------
public class CreateSoilPropertiesSnapshot {

    private static final Logger logger = LoggerFactory.getLogger("app");

	// Tries to find all unique permutations.
	//	Note that some rounding is done to reduce total permutation, 
	//	Example: slope is rounded out to 2 decimal places)
	//--------------------------------------------------------------------------
	public static void outputUniquePermutations()  {

		float[][] slope 	= Layer_Base.getFloatData("slope");
		float[][] slopeLen 	= Layer_Base.getFloatData("slope_length");
		float[][] percSilt 	= Layer_Base.getFloatData("silt_perc");
		float[][] soilDepth = Layer_Base.getFloatData("soil_depth");
		float[][] ls 		= Layer_Base.getFloatData("ls");
		float[][] k 		= Layer_Base.getFloatData("k");
	
		float[][] percSand	= Layer_Base.getFloatData("sand_perc");
		float[][] cec		= Layer_Base.getFloatData("cec");
		float[][] om		= Layer_Base.getFloatData("om");
		float[][] ksat		= Layer_Base.getFloatData("ksat");
		float[][] ph		= Layer_Base.getFloatData("ph");
		
		Layer_Integer wl = Layer_CDL.get();
		
		HashSet<String> uniqueVals = new HashSet<>();
		Integer potentiallyBad = 0;
		for (int y = 0; y < wl.getHeight(); y++) {
			for (int x = 0; x < wl.getWidth(); x++) {
				
				float v1 = slope[y][x];
				if (v1 > 80.0f) {
					if (Math.random() < 0.9) {
						continue;
					}
				}
				if (v1 > 70.0f) {
					if (Math.random() < 0.8) {
						continue;
					}
				}
				if (v1 > 60.0f) {
					if (Math.random() < 0.7) {
						continue;
					}
				}
				
				float v2 = slopeLen[y][x];
				float v3 = percSilt[y][x];
				float v4 = soilDepth[y][x];
				float v5 = ls[y][x];
				float v6 = k[y][x];
				
				float v7 = percSand[y][x];
				float v8 = cec[y][x];
				float v9 = om[y][x];
				float v10 = ksat[y][x];
				float v11 = ph[y][x];
				
				if (Layer_Float.isNoDataValue(v2) ||
					Layer_Float.isNoDataValue(v3) ||
					Layer_Float.isNoDataValue(v4) ||
					Layer_Float.isNoDataValue(v5) ||
					Layer_Float.isNoDataValue(v6) ||
					Layer_Float.isNoDataValue(v7) ||
					Layer_Float.isNoDataValue(v8) ||
					Layer_Float.isNoDataValue(v9) ||
					Layer_Float.isNoDataValue(v10) ||
					Layer_Float.isNoDataValue(v11)) 
				{
					continue;
				}
				
				Float slp = Math.round(v1 * 100) / 100.0f;
				Integer slpLen = Math.round(v2);
				Float pSilt = Math.round(v3 * 20) / 20.0f;
				Integer sDep = Math.round(v4);
				Float sLs = Math.round(v5 * 200) / 200.0f;
				Float sK = v6;
				Float pSand = Math.round(v7 * 20) / 20.0f;
				Float sCec = Math.round(v8 * 20) / 20.0f;
				Float sOm = Math.round(v9 * 20) / 20.0f;
				Float sKsat = Math.round(v10 * 20) / 20.0f;
				Float sPh = Math.round(v11 * 20) / 20.0f;
				
				if (sLs < 0.001) {
					potentiallyBad++;
					continue;
				}
				
				uniqueVals.add(String.format(
					"%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
					slp.toString(), slpLen.toString(), pSilt.toString(), sDep.toString(),
					sLs.toString(), sK.toString(),
					pSand.toString(), sCec.toString(), sOm.toString(), sKsat.toString(), sPh.toString()
				));
			}
		}

		try {
			File writeFile = new File("./uniquePermutations.csv");
			PrintWriter csvOut = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
			
			String header = "Slope,SlopeLength,%Silt,SoilDepth,LS,K,%Sand,CEC,OM,KSAT,PH";
			csvOut.println(header);
			for (String res: uniqueVals) {
				csvOut.println(res);
			}
			csvOut.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			logger.error("Error creating csv");
		}
		
		Integer size = uniqueVals.size();
		logger.debug(" Total Count: " + size);
		logger.error(" Potentially bad LS values: " + potentiallyBad);
	}

	//-------------------------------------------------------
	public static void randomSampleByPercent(float perc) {
		
		PrintWriter csvOut = null;
		try {
			File writeFile = new File("./soilsByPercent.csv");
			csvOut = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
			
			String header = "Slope,SlopeLength,%Silt,SoilDepth,LS,K,%Sand,CEC,OM,KSAT,PH";
			csvOut.println(header);
		}
		catch(Exception e) {
			e.printStackTrace();
			logger.error(" Bad file open in randomSampleByPercent");
		}
		
		float[][] slope 	= Layer_Base.getFloatData("slope");
		float[][] slopeLen 	= Layer_Base.getFloatData("slope_length");
		float[][] percSilt 	= Layer_Base.getFloatData("silt_perc");
		float[][] soilDepth = Layer_Base.getFloatData("soil_depth");
		float[][] ls 		= Layer_Base.getFloatData("ls");
		float[][] k 		= Layer_Base.getFloatData("k");
	
		float[][] percSand	= Layer_Base.getFloatData("sand_perc");
		float[][] cec		= Layer_Base.getFloatData("cec");
		float[][] om		= Layer_Base.getFloatData("om");
		float[][] ksat		= Layer_Base.getFloatData("ksat");
		float[][] ph		= Layer_Base.getFloatData("ph");
		
		Layer_Integer wl = Layer_CDL.get();
		
		perc = perc / 100.0f;
		for (int y = 0; y < wl.getHeight(); y++) {
			for (int x = 0; x < wl.getWidth(); x++) {
				
				Float v1 = slope[y][x];
				Float v2 = slopeLen[y][x];
				Float v3 = percSilt[y][x];
				Float v4 = soilDepth[y][x];
				Float v5 = ls[y][x];
				Float v6 = k[y][x];
				
				Float v7 = percSand[y][x];
				Float v8 = cec[y][x];
				Float v9 = om[y][x];
				Float v10 = ksat[y][x];
				Float v11 = ph[y][x];

				if (Math.random() > perc) continue;
				
				if (v5 < 0.0001f) continue;
				if (Layer_Float.isNoDataValue(v1) ||
					Layer_Float.isNoDataValue(v2) ||
					Layer_Float.isNoDataValue(v3) ||
					Layer_Float.isNoDataValue(v4) ||
					Layer_Float.isNoDataValue(v5) ||
					Layer_Float.isNoDataValue(v6) ||
					Layer_Float.isNoDataValue(v7) ||
					Layer_Float.isNoDataValue(v8) ||
					Layer_Float.isNoDataValue(v9) ||
					Layer_Float.isNoDataValue(v10) ||
					Layer_Float.isNoDataValue(v11)) 
				{
					continue;
				}
				
				csvOut.println(String.format(
					"%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
					v1.toString(), v2.toString(), v3.toString(), v4.toString(),v5.toString(), v6.toString(),
					v7.toString(), v8.toString(), v9.toString(), v10.toString(), v11.toString()
				));
			}
		}

		csvOut.close();
	}
	
}
