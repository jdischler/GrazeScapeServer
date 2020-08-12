package models.transform;

import query.Layer_Float;

// example: "valid-range=?/56"
// ValidRange is quasi-similar to Transform classes but different enough that I left it unrelated
//------------------------------------------------------------
public class ValidRange {
	
	private Boolean mHasMinLegalValue= false, mHasMaxLegalValue = false;
	private Float mMinValue, mMaxValue;
	
	private Float mNoDataValue = -9999.0f;

	//------------------------------------------------------------
	public ValidRange(String value) {
		// Split and minimally sanitize
		String clmp[] = value.split("/");
		for (int i = 0; i < clmp.length; i++) {
			clmp[i] = clmp[i].trim();
		}
		if (clmp[0].length() > 0 && !clmp[0].equalsIgnoreCase("?")) {
			mHasMinLegalValue = true;
			mMinValue = Float.valueOf(clmp[0]);
		}
		if (clmp[1].length() > 0 && !clmp[1].equalsIgnoreCase("?")) {
			mHasMaxLegalValue = true;
			mMaxValue = Float.valueOf(clmp[1]);
		}
	}
	
	//------------------------------------------------------------
	public ValidRange setNoDataValue(Float noDataValue) {
		mNoDataValue = noDataValue;
		return this;
	}
	
	//------------------------------------------------------------
	public final Boolean isValid(Float input) {
		if (Layer_Float.isNoDataValue(input)) return false;
		if (mHasMinLegalValue && input < mMinValue) {
			return false;
		}
		else if (mHasMaxLegalValue && input > mMaxValue) {
			return false;
		}
		return true;
	}
	
	//------------------------------------------------------------
	public final Float apply(Float input) {
		if (Layer_Float.isNoDataValue(input)) return input;
		if (mHasMinLegalValue && input < mMinValue) {
			return mNoDataValue;
		}
		else if (mHasMaxLegalValue && input > mMaxValue) {
			input = mNoDataValue;
		}
		return input;
	}
}
