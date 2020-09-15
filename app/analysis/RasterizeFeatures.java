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
import query.Layer_CDL;
import query.Layer_Integer;
import raster.Extents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import ar.com.hjg.pngj.chunks.*;
import io.jsonwebtoken.lang.Collections;

//------------------------------------------------------------------------------
public class RasterizeFeatures {
	
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
		
    	x = x - Extents.areaExtents[0]; // 440000; 
    	y = Extents.areaExtents[3] - y; //340000;
    	
		return new RPoint(Math.round(x / 10.0f), 
					Math.round(y / 10.0f));	
	}

    //-----------------------------------------------------------------------------
    public static int[][] withIntProperty(List<JsonNode> geoJsonFeatures, String key) {

    	// FIXME: TODO: needed some sort of width/height reference.
		int rasterWidth = Layer_CDL.getWidth(), rasterHeight = Layer_CDL.getHeight();
		
		BufferedImage bi = new BufferedImage(rasterWidth, rasterHeight, BufferedImage.TYPE_INT_RGB);//BufferedImage.TYPE_INT_RGB); 
		Graphics g = bi.getGraphics();
    	
		for (JsonNode f: geoJsonFeatures) {
			int value = 0;
			JsonNode props = f.get("properties");
			if (props != null) {
				value = props.get(key).asInt();
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
	    	
	    	g.setColor(new Color(value));
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
			
			if (argb < 1) argb = Layer_Integer.getIntNoDataValue();
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
	    band.SetNoDataValue(Layer_Float.getNoDataValue());
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
