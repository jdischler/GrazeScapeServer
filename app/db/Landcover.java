package db;

import io.ebean.annotation.DbEnumValue;

public enum Landcover {
	
	ECornGrain("CG"),
	ECornSilage("CS"),
	ESoybeans("SB"),
	ESoybeanSilage("SS"),
	EAlfalfa("AL"),
	EOats("OT");
	
	String dbValue;
	Landcover(String val) {
		this.dbValue = val;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}
}
