package modelInputs;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.ebean.Model;

@Entity
public class ModelInput_BeefHerd extends Model {

	@Id
    public Long id;
	
	public BeefBreed	mBreed;
	
	/*public Boolean mbIsCowCalfOperation;
	public Boolean mbIsStockerOperation;
	public Boolean mbIsFinishingOperation;
	*/
	// TODO: can just use non-zero values to check vs. booleans?
	public Integer 		mNumCowCalf;
	public Integer		mNumStocker;
	public Integer		mNumFinishing;
	
	// specific to a single operation type?
	public Float	mFinishingRatio; // Grass/Forage : Concentrate ratio, unitless, 0 to 1
	
	public Float	mAverageDailyGainTarget;// kg/day
	public Float	mGrazingTime;			// hours/day
	public Float	mGrazingPeriod;			// months/year
	public Float	mConfinedPeriod;		// months/year
	
	// TODO: what is Frequency? From Plectica...
	public Float	mRotationalFrequency;
	
	
}