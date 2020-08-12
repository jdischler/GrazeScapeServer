package modelInputs;

import io.ebean.annotation.DbEnumValue;

public enum DairyBreed {
	EHolstein		("Holstein", 		"HO"), 
	ESmall_Holstein	("Small Holstein", 	"SH"), 
	EBrown_Swiss	("Brown Swiss", 	"BS"), 
	EAyrshire		("Ayrshire", 		"AY"), 
	EJersey			("Jersey", 			"JE"), 
	EGuernsey		("Guernsey", 		"GU"), 
	EOther			("Other", 			"OT");
	
	String displayName, dbValue;
	
	DairyBreed(String displayName, String dbValue) {
		this.displayName = displayName;
		this.dbValue = dbValue;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}
};
	