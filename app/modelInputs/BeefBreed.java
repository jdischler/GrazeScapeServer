package modelInputs;

import io.ebean.annotation.DbEnumValue;

public enum BeefBreed {
	EHolstein	("Holstein", "HO"), 
	ESimmental	("Simmental", "SI"), 
	ELimousin	("Limousin", "LI"), 
	EShort_Horn	("Short Horn", "SH"), 
	EHereford	("Hereford", "HE"), 
	ECharlais	("Charlais", "CH"), 
	EAngus		("Angus", "AN"), 
	EOther		("Other", "OT");
	
	String displayName, dbValue;
	
	BeefBreed(String displayName, String dbValue) {
		this.displayName = displayName;
		this.dbValue = dbValue;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}
	
};
