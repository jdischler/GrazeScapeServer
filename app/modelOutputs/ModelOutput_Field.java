package modelOutputs;

import java.util.List;

public class ModelOutput_Field {

	//	TODO: define a cropYield object. Yield amount? Quality aspects? Other?
	// Most likely:
	//	public Float mPotentialHayYield;	// tons/ha
	//	public Float mEstimated hayYield;	// tons/ha
//	public List<CropYield> cropYields;
	
	public Float mErosion; 			// tons/ha
	public Float mSoilT;			// "acceptable" soil loss, tons/ha
	public Float mPI;				// phosphorus index,kg/ha
	public Float mSCI;				// soil conditioning index, -1 to 1
	public Float mFrostFreeCurve;	//	0 to 100;
	
}
