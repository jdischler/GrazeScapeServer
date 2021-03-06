package models.transform;

import query.Layer_Float;

// scale=1.2
//------------------------------------------------------------
public class Multiply implements Transform {
	
	private Float mCoefficient;
	
	//------------------------------------------------------------
	public Multiply(String value) {
		mCoefficient = Float.valueOf(value); 
	}
	
	//------------------------------------------------------------
	public final Float apply(Float input) {
		if (Layer_Float.isNoDataValue(input)) return input;
		return input * mCoefficient;
	}
	
	//-------------------------------------------------
	public final String debug() {
		return "> Multiply: <" + mCoefficient + "> ";
	}
}
