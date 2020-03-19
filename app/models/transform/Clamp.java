package models.transform;

// example: "clamp=?/56"
//------------------------------------------------------------
public class Clamp implements Transform {
	
	public Boolean mMinClamped= false, mMaxClamped = false;
	public Double mMinClamp, mMaxClamp;

	//------------------------------------------------------------
	public Clamp(String value) {
		// Split and minimally sanitize
		String clmp[] = value.split("/");
		for (int i = 0; i < clmp.length; i++) {
			clmp[i] = clmp[i].trim();
		}
		if (clmp[0].length() > 0 && !clmp[0].equalsIgnoreCase("?")) {
			mMinClamped = true;
			mMinClamp = Double.valueOf(clmp[0]);
		}
		if (clmp[1].length() > 0 && !clmp[1].equalsIgnoreCase("?")) {
			mMaxClamped = true;
			mMaxClamp = Double.valueOf(clmp[1]);
		}
	}
	
	//------------------------------------------------------------
	public final Double apply(Double input) {
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
