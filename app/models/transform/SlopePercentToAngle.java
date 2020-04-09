package models.transform;

import query.Layer_Float;

//------------------------------------------------------------
public class SlopePercentToAngle implements Transform {
	
	//------------------------------------------------------------
//	public SlopePercentToAngle(String value) {}
	
	//------------------------------------------------------------
	public final Float apply(Float input) {
		if (Layer_Float.isNoDataValue(input)) return input;
		else return (float) (Math.atan(input / 100.0f) * (180.0f / Math.PI));
	}
	
	//-------------------------------------------------
	public final String debug() {
		return "> SlopePercentToAngle ";
	}
}
