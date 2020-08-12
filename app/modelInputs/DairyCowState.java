package modelInputs;

import io.ebean.annotation.DbEnumValue;

public enum DairyCowState {

	// TODO: Determine if this is the best way to do this...
	ENone		("None", "NO"), 
	ELactating	("Lactating", "LA"), 
	EDry		("Dry", "DR"), 
	// EBoth		("Lactating and Dry", "LD") // Supposedly not needed, i.e., not mixing per pasture
	; 
	
	String displayName, dbValue;
	
	DairyCowState(String displayName, String dbValue) {
		this.displayName = displayName;
		this.dbValue = dbValue;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}

}
