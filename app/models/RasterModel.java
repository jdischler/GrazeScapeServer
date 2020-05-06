package models;

import com.fasterxml.jackson.databind.JsonNode;

import raster.Extents;

//-------------------------------------------------------
// Model interface
//-------------------------------------------------------
public interface RasterModel {
	
	//-------------------------------------------------------
	public class RasterResult {
		public float[][] 	rasterOut;
		public JsonNode		clientData;
		
		public RasterResult(float [][] data) {
			rasterOut = data;
		}
		public RasterResult addClientData(JsonNode data) {
			clientData = data;
			return this;
		}
	};
	
	//-------------------------------------------------------
	public abstract RasterResult compute(Extents computationArea, JsonNode options) throws Exception; 
};
