
package fileHandling;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import query.Layer_CDL;
import query.Layer_Float;
import query.Layer_Integer;

//------------------------------------------------------------------------------
public class Asc_Writer {
	
    private static final Logger logger = LoggerFactory.getLogger("app");

  //------------------------------------------------------------------------------
    public static void quickDump(float[][] raster) {
		PrintWriter ascOut = null;
		
		// FIXME: TODO: at least there's a reference layer to define the dimensions but something more
		//	flexible is wanted
		Integer width = Layer_CDL.getWidth(), height = Layer_CDL.getHeight();
		String stringNoData = Layer_Integer.getIntNoDataValue().toString();
		
		try {
			File writeFile = new File("./testLS_convert.asc");
			ascOut = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
			ascOut.println("ncols         " + width.toString());
			ascOut.println("nrows         " + height.toString());
			ascOut.println("xllcorner     440000.0");
			ascOut.println("yllcorner     314000.0");
			ascOut.println("cellsize      10");
			ascOut.println("NODATA_value  " + stringNoData);
		} 
		catch (Exception err) {
			logger.error(err.toString());
		}
		
		if (ascOut != null) {	
			for (int y = 0; y < height; y++) {
				StringBuilder ascLine = new StringBuilder(width * 10); // estimate 10 characters per x-raster
				for (int x=0; x < width; x++) {
					float data = raster[y][x];
					if (!Layer_Float.isNoDataValue(data)) {
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

