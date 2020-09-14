package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import models.transform.PassThrough;
import models.transform.SlopePercentToAngle;
import models.transform.Transform;
import query.Layer_Base;
import query.Layer_Float;
import raster.Extents;
import utils.Json;

// Simple fake model - just copy a layer into a returnable dataset
//-------------------------------------------------------------------------------------
public class RasterInspector implements RasterModel {
//-------------------------------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger("app");
	
	private Layer_Base mLayer;
	private Transform mDataTransform = new PassThrough();
	
	public RasterInspector dataLayer(String dataLayer) {
		mLayer = Layer_Base.getLayer(dataLayer);
		return this;
	}
	public RasterInspector setTransform(Transform dataTransform) {
		mDataTransform = dataTransform;
		return this;
	}
	
	// ext is the computation extent
	// options can contain a filter object. That filter object (if exists) can contain
	//	compare: (greater-than, less-than) and value: #
	public RasterResult compute(Extents ext, JsonNode options) throws Exception  { 
		
		if (options != null && !utils.Json.safeGetOptionalBoolean(options, "slope_as_percent", true)) {
			this.setTransform(new SlopePercentToAngle());
		}
		
		Boolean filterEnabled = false;
		Boolean lessThan = true; // if false, then is in greater-than mode
		Float filterValue = 0.0f;
		if (options != null) {
			JsonNode filter = options.get("filter");
			if (filter != null) {
				filterEnabled = true;
				lessThan = Json.safeGetOptionalString(filter, "compare", "less-than").equalsIgnoreCase("less-than");
				filterValue = (float)Json.safeGetOptionalInteger(filter, "value", 0);
			}
		}
		
		if (mLayer == null) throw new Exception("Did not configure a data layer");
		float [][] dataIn = mLayer.getFloatData();
		float [][] dataOut = new float[mLayer.getHeight()][mLayer.getWidth()];

		if (filterEnabled) {
			logger.info("Using filtered view...");
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					Float data = mDataTransform.apply(dataIn[y][x]);
					if (lessThan && data < filterValue) {
						dataOut[y][x] = data;
					}
					else if (!lessThan && data > filterValue) { // in greater than mode
						dataOut[y][x] = data;
					}
					else { // filter it out
						dataOut[y][x] = Layer_Float.getNoDataValue();
					}
				}
			}
		}
		else { // straight copy
			for (int y = ext.y2(); y < ext.y1(); y++) {
				for (int x = ext.x1(); x < ext.x2(); x++) {
					float data = mDataTransform.apply(dataIn[y][x]);
					dataOut[y][x] = data;
				}
			}
		}
		
		return new RasterResult(dataOut);
	}
}
	