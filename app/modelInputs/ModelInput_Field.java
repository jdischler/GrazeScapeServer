package modelInputs;

import javax.persistence.Entity;
import javax.persistence.Id;

import db.Rotation;
import db.Tillage;
import db.Field;
import io.ebean.Model;

@Entity
public class ModelInput_Field extends Model {

	@Id
    public Long id;
	
	public Field field;
	
	public Rotation cropRotation;
	public Boolean	coverCrop;
	public Tillage	tillage;
	public Float	initialSoilP;
	public Float	soilOrganicMatter; // as %
	
	// TODO: figure out appropriate nutrient application data tracking
	public Float	starterFertilizer; // can be zero for none
	public Float	manureApplied;
	public Float	syntheticPApplied;
	
	// ENone if not grazed (or if grazed cover crop), otherwise Lactating OR Dry
	public DairyCowState grazedBy = DairyCowState.ENone;
	
	
	// public Boolean rotationalFrequency...
 
}
