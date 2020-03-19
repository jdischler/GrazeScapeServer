package models.transform;

// example: "valid-range=?/56"
//------------------------------------------------------------
public class ValidRange {
	
	private Boolean mHasMinLegalValue= false, mHasMaxLegalValue = false;
	private Double mMinValue, mMaxValue;
	
	private Double mNoDataValue = -9999.0;

	//------------------------------------------------------------
	public ValidRange(String value) {
		// Split and minimally sanitize
		String clmp[] = value.split("/");
		for (int i = 0; i < clmp.length; i++) {
			clmp[i] = clmp[i].trim();
		}
		if (clmp[0].length() > 0 && !clmp[0].equalsIgnoreCase("?")) {
			mHasMinLegalValue = true;
			mMinValue = Double.valueOf(clmp[0]);
		}
		if (clmp[1].length() > 0 && !clmp[1].equalsIgnoreCase("?")) {
			mHasMaxLegalValue = true;
			mMaxValue = Double.valueOf(clmp[1]);
		}
	}
	
	//------------------------------------------------------------
	public ValidRange setNoDataValue(Double noDataValue) {
		mNoDataValue = noDataValue;
		return this;
	}
	
	//------------------------------------------------------------
	public final Boolean isValid(Double input) {
		if (mHasMinLegalValue && input < mMinValue) {
			return false;
		}
		else if (mHasMaxLegalValue && input > mMaxValue) {
			return false;
		}
		return true;
	}
	
	//------------------------------------------------------------
	public final Double apply(Double input) {
		if (mHasMinLegalValue && input < mMinValue) {
			return mNoDataValue;
		}
		else if (mHasMaxLegalValue && input > mMaxValue) {
			input = mNoDataValue;
		}
		return input;
	}
}
