package models;

import modelInputs.ModelInput_BeefHerd;
import modelInputs.ModelInput_DairyHerd;
import modelInputs.ModelInput_Master;
import modelOutputs.ModelOutput_Master;
import utils.Constants;

public class AnimalModule {
	
	public class Results {
	
		// kg/year
		Double lactatingManure;
		Double lactatingManureConfined;
		Double lactatingManureGrazed;
		
		// kg/year
		Double lactatingManureP;
		Double lactatingManureP_Confined;
		Double lactatingManureP_Grazed;
		
		// kg/year
		Double nonLactatingManure;
		Double nonLactatingManureConfined;
		Double nonLactatingManureGrazed;
		
		// kg/year
		Double nonLactatingManureP;
		Double nonLactatingManureP_Confined;
		Double nonLactatingManureP_Grazed;
		
		// Total milk Production [kg/year]
		Double totalMilk;
		
		Double beefManure;
		Double beefManureP;
		
		Double totalBeefWeightGain;

		
		Double milkFeedDemand;
		Double beefFeedDemand;
		Double totalFeedDemand;
		Double totalFeedDeficit;
		
		Double cornGrainDM_Import;
		Double soyMealDM_Import;
	}
	
	public Results results;
	
	public void process(ModelInput_Master inputs, ModelOutput_Master outputs) {
		
		
		// TODO: Feed supply should come from a prior Feed Module...
		
		//Feed supply [kg-DM/year] = (Corn grain yield DM)+(Alfalfa yield DM)+
		//		(Oat yield DM)+(Non-alfalfa hay yield DM)+(Pasture yield DM)
		Double feedSupply = 0.0;
		
	//---------------------------
	// DAIRY Processing
	//---------------------------
		
		ModelInput_DairyHerd dh = inputs.mDairyHerd;
		
		// Total milk Production [kg/year]
		results.totalMilk = dh.mMilkYield * dh.mNumLactactingCows * Constants.DAYS_PER_YEAR;
			
		// days/year
		Double daysConfinedLactating = dh.mConfinedPeriodLactating * Constants.DAYS_PER_MONTH;
		Double daysConfinedNonLactating = dh.mConfinedPeriodNonLactating * Constants.DAYS_PER_MONTH;
		
		// kg/year
		results.lactatingManure = (dh.mMilkYield * 0.616 + 46.2) * dh.mNumLactactingCows * Constants.DAYS_PER_YEAR;
		results.lactatingManureP = (dh.mMilkYield * 0.781 + 50.4) * dh.mNumLactactingCows * Constants.DAYS_PER_YEAR / 1000.0;
			
		// kg/year
		results.lactatingManureConfined = results.lactatingManure * daysConfinedLactating / Constants.DAYS_PER_YEAR;
		results.lactatingManureGrazed = results.lactatingManure - results.lactatingManureConfined;
			
		// kg/year
		results.lactatingManureP_Confined = results.lactatingManureP * daysConfinedLactating / Constants.DAYS_PER_YEAR;
		results.lactatingManureP_Grazed = results.lactatingManureP - results.lactatingManureP_Confined;

		results.milkFeedDemand = results.totalMilk * 0.9;
		
				
		// DAIRY - calves, heifers, dry cows (based on WiDATCP)
		// kg/year
		Double _toP = ((4.0 / Constants.TONS) * Constants.P_IN_P2O5); // 4lbs P2O5 per ton;
		Double dairyCalfManure = 17.0 * Constants.LBS_TO_KG * dh.mNumYoungStock * Constants.DAYS_PER_YEAR;
		Double dairyCalfManureP = dairyCalfManure * _toP;
		
		// kg/year
		Double dairyHeiferManure = 65.0 * Constants.LBS_TO_KG * dh.mNumHeifers * Constants.DAYS_PER_YEAR;
		Double dairyHeiferManureP = dairyHeiferManure * _toP;
		
		// kg/year
		Double dryCowManure = 98.5 * Constants.LBS_TO_KG * dh.mNumDryCows * Constants.DAYS_PER_YEAR;
		Double dryCowManureP = dryCowManure * _toP;
		
		// kg/year
		results.nonLactatingManure = dairyCalfManure + dairyHeiferManure + dryCowManure;
		results.nonLactatingManureP = dairyCalfManureP + dairyHeiferManureP + dryCowManureP;

		// kg/year						
		results.nonLactatingManureConfined = results.nonLactatingManure * daysConfinedNonLactating / Constants.DAYS_PER_YEAR;
		results.nonLactatingManureGrazed = results.nonLactatingManure - results.nonLactatingManureConfined;
		
		results.nonLactatingManureP_Confined = results.nonLactatingManureP * daysConfinedNonLactating / Constants.DAYS_PER_YEAR;
		results.nonLactatingManureP_Grazed = results.nonLactatingManureP - results.nonLactatingManureP_Confined;
				
	//---------------------------
	// BEEF Processing
	//---------------------------

		ModelInput_BeefHerd bh = inputs.mBeefHerd;
		
		_toP = ((6.0 / Constants.TONS) * Constants.P_IN_P2O5); // 6lbs P2O5 per ton;
		
		// kg/year
		Double cowCalfManure = 63.0 * Constants.LBS_TO_KG * bh.mNumCowCalf * Constants.DAYS_PER_YEAR;
		Double cowCalfManureP = cowCalfManure * _toP;
		
		Double stockerManure = 58.0 * Constants.LBS_TO_KG * bh.mNumStocker * Constants.DAYS_PER_YEAR;
		Double stockerManureP = stockerManure * _toP;
		
		Double finishingManure = 86.0 * Constants.LBS_TO_KG * bh.mNumFinishing * Constants.DAYS_PER_YEAR;
		Double finishingManureP = finishingManure * _toP;
		
		results.beefManure = cowCalfManure + stockerManure + finishingManure;
		results.beefManureP = cowCalfManureP + stockerManureP + finishingManureP;
		
		// For beef operations, v1 will determine feed based on herd-level annual weight gain
		// kg/year
		results.totalBeefWeightGain = bh.mAverageDailyGainTarget * 
				(bh.mNumCowCalf + bh.mNumFinishing + bh.mNumStocker) * // total herd size
				Constants.DAYS_PER_YEAR;

		// Next, convert average daily weight gain to feed demand based on Peters et al. 2014
		// [kg-DM/year]
		results.beefFeedDemand = results.totalBeefWeightGain * 14.30;

	//---------------------------
	// FINAL Processing
	//---------------------------
		
		// Calculate feed deficit for combined dairy and beef
		// [kg-DM/year]
		results.totalFeedDemand = results.milkFeedDemand + results.beefFeedDemand;
		results.totalFeedDeficit = results.totalFeedDemand - feedSupply;

		// Assume that feed deficit is split between imported corn grain and soybean meal
		// [kg-DM/year]
		results.cornGrainDM_Import = results.totalFeedDeficit / 2.0;
		results.soyMealDM_Import = results.totalFeedDeficit / 2.0;
	}
}