package db;

import io.ebean.annotation.DbEnumValue;

public enum Tillage {
	ENoTill("NT"),
	EChiselDisked("CD"),
	EMoldboardPlow("MP");
	
	String dbValue;
	Tillage(String val) {
		this.dbValue = val;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}
}
