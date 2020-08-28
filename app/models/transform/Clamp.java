package models.transform;

import query.Layer_Float;

/**
 *  Provides a clamping data transform
 *   
 *  example in model file: 
 *  "clamp=?/56"
 *  example in Java code: 
 *  Clamp c = new Clamp("?/56");
 *///------------------------------------------------------------
public class Clamp implements Transform {
	
	public Boolean mMinClamped= false, mMaxClamped = false;
	public Float mMinClamp, mMaxClamp;

	/**
	 *  Constructor
	 * 
	 * ```java
	 * // Create a clamp object that doesn't test a minimum but ensures the an output value never exceeds 56.  
	 * Clamp c = new Clamp("?/56");
	 *  ```
	 *  @param String Clamp specification in "<min>/<max>" format, where <min> and <max> are <Float> types. A '?' can be subsitited instead if the given element doesn't need to be clamped 
	 *///------------------------------------------------------------
	public Clamp(String value) {
		// Split and minimally sanitize
		String clmp[] = value.split("/");
		for (int i = 0; i < clmp.length; i++) {
			clmp[i] = clmp[i].trim();
		}
		if (clmp[0].length() > 0 && !clmp[0].equalsIgnoreCase("?")) {
			mMinClamped = true;
			mMinClamp = Float.valueOf(clmp[0]);
		}
		if (clmp[1].length() > 0 && !clmp[1].equalsIgnoreCase("?")) {
			mMaxClamped = true;
			mMaxClamp = Float.valueOf(clmp[1]);
		}
	}
	
	/** Apply the configured clamp to the requested input
	 * 
	 * ```java
	 * // Create a clamp object that doesn't test a minimum but ensures the an output value never exceeds 56.  
	 * Clamp c = new Clamp("?/56");
	 * Float result = c.apply(64.3f); // result will equal 56.0
	 * result = c.apply(-64.3f); // result will equal -64.3
	 *  ```
	 *  @param Float the value to clamp
	 *  @return The input value as clamped by the specification from the constructor 
	 *///------------------------------------------------------------
	public final Float apply(Float input) {
		if (Layer_Float.isNoDataValue(input)) return input;
		if (mMinClamped && input < mMinClamp) input = mMinClamp;
		if (mMaxClamped && input > mMaxClamp) input = mMaxClamp;
		return input;
	}
	
	//-------------------------------------------------
	public final String debug() {
		final String min = mMinClamped ? "" + mMinClamp : "?";
		final String max = mMaxClamped ? "" + mMaxClamp : "?";
		return String.format("> Clamp: <%s / %s> ", min, max);
	}
	
}
