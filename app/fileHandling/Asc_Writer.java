
package fileHandling;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//------------------------------------------------------------------------------
public class Asc_Writer {
	
    private static final Logger logger = LoggerFactory.getLogger("app");

    public static void quickDump(float[][] raster) {
		PrintWriter ascOut = null;
		int width = 1900, height = 3400;
		try {
			File writeFile = new File("./test.asc");
			ascOut = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
			ascOut.println("ncols         1900");
			ascOut.println("nrows         3400");
			ascOut.println("xllcorner     -10128000.0");
			ascOut.println("yllcorner     5358000.0");
			ascOut.println("cellsize      10");
			ascOut.println("NODATA_value  -9999");
		} 
		catch (Exception err) {
			logger.error(err.toString());
		}
		
		if (ascOut != null) {	
			String stringNoData = Integer.toString(-9999);
	
			for (int y = 0; y < height; y++) {
				StringBuilder ascLine = new StringBuilder(width * 10); // estimate 10 characters per x-raster
				for (int x=0; x < width; x++) {
					float data = raster[y][x];
					if (data > -9999.0f) {
						ascLine.append(data);
					}
					else {
						ascLine.append(stringNoData);
					}
					if (x != width - 1) {
						ascLine.append(" ");
					}
				}
				ascOut.println(ascLine.toString());
			}
			try {
				ascOut.close();
			}
			catch (Exception err) {
				logger.error(err.toString());
			}
		}
	}
}

