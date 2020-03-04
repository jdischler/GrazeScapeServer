package analysis;

import utils.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import play.libs.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import ar.com.hjg.pngj.chunks.*;
import io.jsonwebtoken.lang.Collections;

//------------------------------------------------------------------------------
public class RasterizeFeature {
	
    private static final Logger logger = LoggerFactory.getLogger("application");

    public static class RPoint {
    	RPoint(int x, int y) {
    		mX = x; mY = y;
    	}
    	RPoint(RPoint other) {
    		this.mX = other.mX; this.mY = other.mY;
    	}
    	public RPoint copy(RPoint other) {
    		this.mX = other.mX; this.mY = other.mY;
    		return this;
    	}
    	public String toString() {
    		return "-> " + this.mX + " " + this.mY;
		}
    	public Integer mX;
    	public Integer mY;
    };
    
    //-----------------------------------------------------------------------------
	private static RPoint alignPoint(float x, float y) {
		
    	x = x - 440000;
    	y = 340000 - y;
    	
		return new RPoint(Math.round(x / 10.0f), 
					Math.round(y / 10.0f));	
	}

	@Deprecated
    //-----------------------------------------------------------------------------
    public static int[][] toInt(boolean someInputs) {

		int rasterWidth = 1500, rasterHeight = 2600;
		BufferedImage bi = new BufferedImage(rasterWidth, rasterHeight, BufferedImage.TYPE_INT_RGB); 
		Graphics g = bi.getGraphics();
    	
		File file = new File("./public/shapeFiles/exampleFields.geojson");
		JsonNode fasJson = null;
		try (FileInputStream fis = new FileInputStream(file)) {
			fasJson = Json.parse(fis);
		}
		catch(Exception e) {
		}
		if (fasJson != null) {
			ArrayNode an = (ArrayNode)fasJson.get("features");
			
			for (int idx = 0; idx < an.size(); idx++) {
				JsonNode f = an.get(idx);
				
				int f_id = 0;
				JsonNode props = f.get("properties");
				if (props != null) {
					f_id = props.get("f_id").asInt();
				}
				
				JsonNode geo = f.get("geometry");
				ArrayNode coords = (ArrayNode)geo.get("coordinates");
				coords = (ArrayNode)coords.get(0);
				
		    	List<Integer> lx = new ArrayList<Integer>();
		    	List<Integer> ly = new ArrayList<Integer>();
				
				for (int a_idx = 0; a_idx < coords.size(); a_idx++) {
					ArrayNode duple = (ArrayNode)coords.get(a_idx);
					
		    		RPoint rp = alignPoint((float)duple.get(0).asDouble(), (float)duple.get(1).asDouble());
		    		lx.add(rp.mX);
		    		ly.add(rp.mY);
				}
				
		    	int xs3[] = lx.stream().mapToInt(i->i).toArray();
		    	int ys3[] = ly.stream().mapToInt(i->i).toArray();
		    	
		    	g.setColor(new Color(f_id));
		    	g.fillPolygon(xs3, ys3, xs3.length);
			}
/*			"features": [
			{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[-10116865,5369440],
				"properties":{"f_id":1}},*/			
/*	    	int stream[] = {-10116865,5369440,-10116853,5369402,-10116773,5369415,-10116600,5369537,-10116690,5369602,-10116711,5369658,-10116665,5369652,-10116622,5369606,-10116563,5369613,-10116535,5369598,-10116420,5369623,-10116374,5369607,-10116363,5369687,-10116389,5369766,-10116360,5369811,-10116379,5369835,-10116347,5369869,-10116252,5369804,-10116197,5369874,-10116189,5369879,-10116135,5369769,-10116067,5369771,-10116051,5369744,-10116135,5369686,-10116130,5369653,-10116035,5369621,-10116007,5369571,-10116011,5369527,-10116087,5369506,-10116071,5369464,-10116005,5369454,-10116010,5369349,-10115966,5369347,-10115962,5369363,-10115844,5369304,-10115838,5369252,-10115857,5369084,-10115802,5369050,-10115734,5369070,-10115614,5369009,-10115579,5369028,-10115475,5369005,-10115471,5369025,-10115591,5369078,-10115661,5369152,-10115665,5369228,-10115637,5369291,-10115638,5369327,-10115708,5369437,-10115752,5369478,-10115802,5369597,-10115737,5369699,-10115684,5369697,-10115591,5369759,-10115505,5369768,-10115471,5369863,-10115422,5369891,-10115349,5369888,-10115348,5369917,-10115450,5369921,-10115532,5369896,-10115659,5369898,-10115700,5369923,-10115780,5369917,-10115840,5369918,-10115874,5369888,-10115862,5369855,-10115929,5369853,-10115942,5369880,-10115929,5369893,-10116095,5370020,-10116217,5370002,-10116107,5369926,-10116009,5369848,-10115896,5369803,-10115818,5369808,-10115741,5369882,-10115703,5369855,-10115726,5369763,-10115789,5369755,-10115828,5369777,-10115917,5369785,-10116042,5369836,-10116150,5369922,-10116250,5369992,-10116342,5370003,-10116483,5369837,-10116555,5369755,-10116639,5369752,-10116776,5369774,-10116852,5369711,-10116878,5369597,-10116820,5369572,-10116828,5369502,-10116819,5369472,-10116865,5369440};
	    	
	    	for (int idx = 0; idx < stream.length; idx += 2) {
	    		RPoint rp = alignPoint(stream[idx], stream[idx+1]);
	    		lx.add(rp.mX);
	    		ly.add(rp.mY);
	    	}
	   */ 	
		}
    	
		int [][] array = new int[rasterHeight][rasterWidth];
		int width = bi.getWidth();

		WritableRaster wr = bi.getRaster();
		DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
		
		Integer stride = wr.getNumDataElements();
		Integer bufferSz = rasterWidth * rasterHeight * stride;
		
		int[] pixels = db.getData();
		
		for (int pixel = 0, y = 0, x = 0; pixel < bufferSz; pixel += stride) {
			Integer argb = pixels[pixel]; argb &= 0x00ffffff;
			
			if (argb < 1) argb = -9999;
			array[y][x] = argb;
			x++;
			if (x == width) {
				x = 0;
				y++;
			}
		}		
		return array;
    }
    
    //-----------------------------------------------------------------------------
    public static int[][] testToInt(List<JsonNode> geoJsonFeatures) {

		int rasterWidth = 1500, rasterHeight = 2600;
		BufferedImage bi = new BufferedImage(rasterWidth, rasterHeight, BufferedImage.TYPE_INT_RGB); 
		Graphics g = bi.getGraphics();
    	
		for (JsonNode f: geoJsonFeatures) {
			int f_id = 0;
			JsonNode props = f.get("properties");
			if (props != null) {
				f_id = props.get("f_id").asInt();
			}
			
			JsonNode geo = f.get("geometry");
			ArrayNode coords = (ArrayNode)geo.get("coordinates");
			coords = (ArrayNode)coords.get(0);
			
	    	List<Integer> lx = new ArrayList<Integer>();
	    	List<Integer> ly = new ArrayList<Integer>();
			
			for (int a_idx = 0; a_idx < coords.size(); a_idx++) {
				ArrayNode duple = (ArrayNode)coords.get(a_idx);
				
	    		RPoint rp = alignPoint((float)duple.get(0).asDouble(), (float)duple.get(1).asDouble());
	    		lx.add(rp.mX);
	    		ly.add(rp.mY);
			}
			
	    	int xs3[] = lx.stream().mapToInt(i->i).toArray();
	    	int ys3[] = ly.stream().mapToInt(i->i).toArray();
	    	
	    	g.setColor(new Color(f_id));
	    	g.fillPolygon(xs3, ys3, xs3.length);
		}
    	
		int [][] array = new int[rasterHeight][rasterWidth];
		int width = bi.getWidth();

		WritableRaster wr = bi.getRaster();
		DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
		
		Integer stride = wr.getNumDataElements();
		Integer bufferSz = rasterWidth * rasterHeight * stride;
		
		int[] pixels = db.getData();
		
		for (int pixel = 0, y = 0, x = 0; pixel < bufferSz; pixel += stride) {
			Integer argb = pixels[pixel]; argb &= 0x00ffffff;
			
			if (argb < 1) argb = -9999;
			array[y][x] = argb;
			x++;
			if (x == width) {
				x = 0;
				y++;
			}
		}		
		return array;
    }
    
}
 

/*	// topLeftX, topLeftY, bottomRightX, bottomRightY
	
	// Clip Selection to area
	if (selExtents[0] < areaExtents[0]) 		selExtents[0] = areaExtents[0];
	else if (selExtents[0] > areaExtents[2]) 	selExtents[0] = areaExtents[2];
	
	if (selExtents[2] < areaExtents[0]) 		selExtents[2] = areaExtents[0];
	else if (selExtents[2] > areaExtents[2]) 	selExtents[2] = areaExtents[2];
	
	if (selExtents[1] > areaExtents[1]) 		selExtents[1] = areaExtents[1];
	else if (selExtents[1] < areaExtents[3]) 	selExtents[1] = areaExtents[3];
	
	if (selExtents[3] > areaExtents[1]) 		selExtents[3] = areaExtents[1];
	else if (selExtents[3] < areaExtents[3]) 	selExtents[3] = areaExtents[3];
	//logger.error("clipped [" + selExtents[0] + "," + selExtents[1] + "][" + selExtents[2] + "," + selExtents[3] + "]");
	
	int rasterWidth = 1900, rasterHeight = 3400;
	
	// re-index
	Integer indexX = (selExtents[0] - areaExtents[0]) / 10;
	Integer indexY = -(selExtents[1] - areaExtents[1]) / 10;
	
	Integer indexX2 = (selExtents[2] - areaExtents[0]) / 10;
	Integer indexY2 = -(selExtents[3] - areaExtents[1]) / 10;
	
	public static float[][] toInt(boolean someInputs) {
	    //# Define pixel_size and NoData value of new raster
	    int pixel_size = 10;
	    float NoData_value = -9999.0f;
	
	    gdal.AllRegister();
	    org.gdal.ogr.ogr.RegisterAll();
	
	    String shpFilePath = "./public/shapeFiles/exampleFields.geojson";
	
	    //# Open the data source and read in the extent
	    DataSource sourceDs = org.gdal.ogr.ogr.Open(shpFilePath,gdalconstConstants.GA_ReadOnly);
	    Layer sourceLayer = sourceDs.GetLayer(0);
	
	    SpatialReference sourceSrs = sourceLayer.GetSpatialRef();
	    double[] extent = sourceLayer.GetExtent();
	
	    //# Create the destination data source
	    Float x_res = (float) ((extent[1] - extent[0]) / pixel_size);
	    Float y_res = (float) ((extent[3] - extent[2]) / pixel_size);
	    logger.error("x_res -------" + x_res +  "y " + y_res );
	    Integer xCor = Math.round(x_res);
	    Integer yCor = Math.round(y_res);
	
	    System.out.println("xCor -------" + xCor);
	    System.out.println("yCor -------" + yCor);
	
	    String output = "./public/dynamicFiles/pautch.tiff";
	    org.gdal.gdal.Dataset target_ds = gdal.GetDriverByName("GTiff").Create(output, xCor, yCor, 1, gdalconst.GDT_Float32);// GDT_Int32);
	    target_ds.SetGeoTransform(new double[]{extent[0], pixel_size, 0, extent[2], 0, -pixel_size});
	    Band band = target_ds.GetRasterBand(1);
	    band.SetNoDataValue(NoData_value);
	    band.FlushCache();
	
	    int[] intArr = {1};
	
	    //# Rasterize
	    gdal.RasterizeLayer(target_ds, intArr, sourceLayer, null);
		
		float [][] array = new float[3400][1900];
	    
	    // 1900, 3400
	    for (int y = 0; y < 3400; y++) {
	    	target_ds.GetRasterBand(1).ReadRaster(0, y, 1900, 1, array[y]);
	    }
		return array;
	}
}
*/
