package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.*;
import query.Layer_Base;
import query.Layer_Integer;
import query.Layer_CDL;

//------------------------------------------------------------------------------
// Modeling Process
//
// This model uses slope, soil depth, silt, and CEC to calculate corn, soy, grass, and alfalfa yield 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are slope, soil depth, silt, CEC layers, and the selected cells in the raster & rotation map 
// Outputs are an array of corn, soy, grass, and alfalfa yields. The units are in tonnes per hectare
//
//  -Version 08/20/2013
//--------------------------------------------------------------------------------
// 2020 Notes:
//
// Output is additionally packed into a long. The model clamped values were originally clamped from 0 - 25 so we
//	exploit the fairly small fixed/known range and pack that into the long as described in packYield...
// Depending on the final packing settings, there will be some loss of yield per cell but the fraction
//	is IMHO so low that it's not worth worrying about. Alternately, packYield could track the error of
//	packed vs. actual and then use that to put a compensation factor on a neighbor.  In aggregate,
//	the total error should end up so small as to not be worth worrying about. ..at the expense of
//	individual cells potentially being slightly less accurate.
//--------------------------------------------------------------------------------


//------------------------------------------------------------------------------
public class Model_CropYield extends Model_Base
{
    private static final Logger logger = LoggerFactory.getLogger("app");

	private static final boolean SELF_DEBUG_LOGGING = false;
	
	long[][] mOutput;
	
	public enum EYieldPacks {
		ECornGrain,	// Yield model predicts corn grain. Corn Silage is assumed to be corn grain * stover contribution (2x)
		ESoyBeans,	// Yield model predicts soy beans. Soylage is assumed to be beans * residuals (2.5x)
		EAlfalfa,
		EGrass,
		// TODO: oats?
		// TODO: 
	};
	// Holds up to 7 - 9-bit chunks in a 64 bit long
	// The choice of bits per pack is arbitrary-ish but this gives us
	// accuracy down to about 0.04... with 7 crops
	//--------------------------------------------------------------------
	private final long getPackedYield(float fValue, EYieldPacks pack) {
		
		long scaled = (long)Math.round(fValue * (511.0f / 20.0f));
		scaled = scaled > 511 ? 511 : (scaled < 0 ? 0 : scaled);
		return (scaled << (pack.ordinal() * 9));
	}

	//--------------------------------------------------------------------
	public static final float unpackYield(long lValue, EYieldPacks pack) {
		Long res = (lValue >> (pack.ordinal() * 9)) & 0x1ff;
		return res * (20.0f / 511.0f);
	}	
	
	//--------------------------------------------------------------------
	public static final float unpackCornSilage(long lValue) {
		// The model predicted corn grain. Stover contributes 2x that amount
		return unpackYield(lValue, EYieldPacks.ECornGrain) * 2.0f;
	}
	
	//--------------------------------------------------------------------
	public static final float unpackSoylage(long lValue) {
		// The model predicted soy beans. Residue contributes 2.5x that amount
		return unpackYield(lValue, EYieldPacks.ESoyBeans) * 2.5f;
	}
	
	// Check to see if the required data layers are available
	//--------------------------------------------------------------------
	public static Boolean available() {
		return (Layer_CDL.get() != null) &
			(Layer_Base.getLayer("slope") != null) &
			(Layer_Base.getLayer("silt_perc") != null) &
			(Layer_Base.getLayer("soil_depth") != null) &
			(Layer_Base.getLayer("cec") != null);
	}
	
	//--------------------------------------------------------------------------
	public Boolean initialize() {
		
		mOutput = new long[mHeight][mWidth];
		debugLog("  > Allocated memory for Yield");

		mbInitialized = true;
		return true;
	}
	
	//--------------------------------------------------------------------------
	public long[][] run() throws Exception {

		if (!mbInitialized) throw new Exception("uninitialized model run");
		
		debugLog(" >> Computing Yield");

		float slope[][] = Layer_Base.getFloatData("slope");
		float silt[][] 	= Layer_Base.getFloatData("silt_perc");
		float depth[][] = Layer_Base.getFloatData("soil_depth");
		float cec[][]	= Layer_Base.getFloatData("cec");
		
		final float cornCoefficient = 1.30f 	// correction for technological advances 
								* 0.053f; 		// conversion to Mg per Ha  TODO: verify
		
		final float soyCoefficient = 1.2f		// Correct for technological advances
								* 0.0585f;		// conversion to Mg per Ha  TODO: verify

		final float alfalfaCoefficient = 1.05f 	// Correct Factor for modern yield
								* 1.91f;		// conversion to Mg per Ha

		final float grassCoefficient = 1.05f 	// Correction for technological advances
								* 1.91f;		// conversion to Mg per Ha
		
		// corn soy roots don't typically extend below 5 feet so clamp depth to prevent unrealistic benefits of extremely deep soils
		float maxCornSoyDepth = 152.0f; 	// centimeters  
		
		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				
				long packedYield = 0;
				float 	_slope 	= slope[y][x], 
						_depth 	= depth[y][x], 
						_silt 	= silt[y][x], 
						_cec 	= cec[y][x];				
				
				float _clippedDepth = _depth < maxCornSoyDepth ? _depth : maxCornSoyDepth; 
				
				//------------------------------------------------------------
				// Bushels per acre?? * conversion coeff
				//------------------------------------------------------------
				float cornYield = (22.0f + 
						-1.05f 			* _slope + 
						0.19f 			* _clippedDepth + 
						(0.817f/100.0f) * _silt + 
						1.32f 			* _cec) *
							cornCoefficient;
				
				packedYield |= getPackedYield(cornYield, EYieldPacks.ECornGrain);
				
				//------------------------------------------------------------
				// Bushels per acre * conversion coeff
				//------------------------------------------------------------
				float soyYield = (6.37f + 
						-0.34f 			* _slope + 
						0.065f 			* _clippedDepth + 
						(0.278f/100.0f) * _silt + 
						0.437f 			* _cec) 	*
							soyCoefficient; 
				
				packedYield |= getPackedYield(soyYield, EYieldPacks.ESoyBeans);
				
				//------------------------------------------------------------
				// Short tons per acre * conversion coeff
				//------------------------------------------------------------
				float alfalfaYield = (1.26f + 
						-0.045f 		* _slope + 
						0.007f 			* _depth + 
						(0.027f/100.0f) * _silt + 
						0.041f 			* _cec) *
							alfalfaCoefficient;
				
				packedYield |= getPackedYield(alfalfaYield, EYieldPacks.EAlfalfa);
				
				//------------------------------------------------------------
				// Short tons per acre * conversion coeff
				//------------------------------------------------------------
				float grassYield = (0.77f + 
						-0.031f 		* _slope + 
						0.008f 			* _depth + 
						(0.029f/100.0f) * _silt + 
						0.038f 			* _cec) *
							grassCoefficient;
				
				packedYield |= getPackedYield(grassYield, EYieldPacks.EGrass);
				
				//------------------------------------------------------------
				mOutput[y][x] = packedYield;
			}
		}
		
		return mOutput;
	}
	
	//-------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private static final void debugLog(String conditionalLog) {
		
		if (ALL_MODELS_DEBUG_LOGGING || SELF_DEBUG_LOGGING) {
			logger.debug(conditionalLog);
		}
	}
	
}
