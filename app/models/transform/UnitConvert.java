package models.transform;

import java.util.HashMap;
import java.util.Map;

// example: "clamp=?/56"
//------------------------------------------------------------
public class UnitConvert implements Transform {
	
	private static Map<String,Double> mConversionTable = null;

	private String mConversion;
	private Double mCoefficient;
	
	public Boolean mMinClamped= false, mMaxClamped = false;
	public Double mMinClamp, mMaxClamp;

	//------------------------------------------------------------
	public UnitConvert(String value) {
		
		if (mConversionTable == null) init();
		
		mConversion = value.toLowerCase();
		mCoefficient = mConversionTable.get(mConversion);
	}
	
	//------------------------------------------------------------
	public final Double apply(Double input) {
		return input * mCoefficient;
	}
	//------------------------------------------------------------
	public final Float apply(Float input) {
		return (float) (input * mCoefficient);
	}
	
	//------------------------------------------------------------
	private static void init() {
		mConversionTable = new HashMap<>();

		// Length conversions
		mConversionTable.put("feet-to-meters", 1.0 / 3.28084);
		mConversionTable.put("meters-to-feet", 3.28084);
		
		// Area conversion
		mConversionTable.put("acres-to-hectares", 1.0 / 2.4711);
		mConversionTable.put("hectares-to-acres", 2.4711);
		mConversionTable.put("meters-squared-to-acres", 0.00024710538146717);
		mConversionTable.put("meters-squared-to-hectares", 0.0001);
		// eh...
		mConversionTable.put("msq-to-hectares", mConversionTable.get("meters-squared-to-hectares"));
		mConversionTable.put("msq-to-acres", mConversionTable.get("meters-squared-to-acres"));
		
		// Misc
		mConversionTable.put("percent-to-fraction", 1.0 / 100.0);
		mConversionTable.put("fraction-to-percent", 100.0);
	}
	
	//-------------------------------------------------
	public final String debug() {
		return "> UnitConvert: " + mConversion + " <" + mCoefficient + "> ";
	}
}
