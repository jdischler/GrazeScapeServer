package modelInputs;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.ebean.Model;

@Entity
public class ModelInput_DairyHerd extends Model {
	
	@Id
    public Long id;

	public DairyBreed mBreed;
	
	public Integer 	mNumLactactingCows;
	public Integer 	mNumDryCows;
	// TODO: what about being exactly on year 1?
	public Integer 	mNumYoungStock;	// < 1 yr
	public Integer 	mNumHeifers;	// > 1 yr

	// Desired yield goals for each category, kg/cow/day
	public Float 	mMilkYield;
	
	// Hours/day
	public Float	mGrazingTimeLactating;	
	public Float	mGrazingTimeNonLactating;
	
	// Months/year
	public Float 	mGrazingPeriodLactating;
	public Float 	mGrazingPeriodNonLactating;
	
	// Months/year
	public Float	mConfinedPeriodLactating;
	public Float	mConfinedPeriodNonLactating;

	// days, then moved to the next paddock .. 
	// public Float	mRotationalFrequencyLactating;
	// public Float	mRotationalFrequencyNonLactating;
	
	// Returns total [kg/year]
	public static Double getMilkYieldFor(List<ModelInput_DairyHerd> inputs) {
		Double output = 0.0;
		
		for (ModelInput_DairyHerd midh: inputs) {
			output += midh.mMilkYield * midh.mNumLactactingCows;
		}
		return output;
	}

	public static Integer getCountLactactingFor(List<ModelInput_DairyHerd> inputs) {
		Integer output = 0;
		
		for (ModelInput_DairyHerd midh: inputs) {
			output += midh.mNumLactactingCows;
		}
		return output;
	}
	
}
