package models.transform;

import java.util.HashMap;
import java.util.Map;

import query.Layer_Float;

// example: "clamp=?/56"
//------------------------------------------------------------
public class UnitConvert implements Transform {
	
	private static Map<String,Float> mConversionTable = null;

	private String mConversion;
	private Float mCoefficient;
	
	public Boolean mMinClamped= false, mMaxClamped = false;
	public Float mMinClamp, mMaxClamp;

	//------------------------------------------------------------
	public UnitConvert(String value) {
		
		if (mConversionTable == null) init();
		
		mConversion = value.toLowerCase();
		mCoefficient = mConversionTable.get(mConversion);
	}
	
	//------------------------------------------------------------
	public final Float apply(Float input) {
		if (Layer_Float.isNoDataValue(input)) return input;
		return input * mCoefficient;
	}
	
	//------------------------------------------------------------
	private static void init() {
		mConversionTable = new HashMap<>();

		// Length conversions
		mConversionTable.put("feet-to-meters", 1.0f / 3.28084f);
		mConversionTable.put("meters-to-feet", 3.28084f);
		
		// Area conversion
		mConversionTable.put("acres-to-hectares", 1.0f / 2.4711f);
		mConversionTable.put("hectares-to-acres", 2.4711f);
		mConversionTable.put("meters-squared-to-acres", 0.00024710538146717f);
		mConversionTable.put("meters-squared-to-hectares", 0.0001f);
		// eh...
		mConversionTable.put("msq-to-hectares", mConversionTable.get("meters-squared-to-hectares"));
		mConversionTable.put("msq-to-acres", mConversionTable.get("meters-squared-to-acres"));
		
		// Misc
		mConversionTable.put("percent-to-fraction", 1.0f / 100.0f);
		mConversionTable.put("fraction-to-percent", 100.0f);
	}
	
	//-------------------------------------------------
	public final String debug() {
		return "> UnitConvert: " + mConversion + " <" + mCoefficient + "> ";
	}
}
