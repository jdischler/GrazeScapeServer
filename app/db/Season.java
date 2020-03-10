package db;

import io.ebean.annotation.DbEnumValue;

// Currently assumed to apply to tillage season. A broader use may be appropriate if needed...
public enum Season {
	ESpring("SP"),
	EFall("FL");
	
	String dbValue;
	Season(String val) {
		this.dbValue = val;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}
	
}
