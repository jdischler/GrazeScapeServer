package models.transform;

import query.Layer_Float;

// example: "quadratic=2.0393/0.157/0.0001"  (intercept/x/x2) 
//------------------------------------------------------------
public class Quadratic implements Transform {
	
	public Float mIntercept, mX, mX2;

	//------------------------------------------------------------
	public Quadratic(String value) {
		// Split and minimally sanitize
		String clmp[] = value.split("/");
		for (int i = 0; i < clmp.length; i++) {
			clmp[i] = clmp[i].trim();
		}
		mIntercept = Float.valueOf(clmp[0]);
		mX = Float.valueOf(clmp[1]);
		mX2 = Float.valueOf(clmp[2]);
	}
	
	//------------------------------------------------------------
	public final Float apply(Float input) {
		if (Layer_Float.isNoDataValue(input)) return input;
		
		return mIntercept + input * mX + input * input * mX2;
	}
	
	//-------------------------------------------------
	public final String debug() {
		return String.format("> Quadratic: <%0.6f + %0.6fx + %0.6fx2> ", mIntercept, mX, mX2);
	}
	
}
