package db;

import io.ebean.annotation.DbEnumValue;

public enum Tillage {
	ENoTill("NT", "No-Till"),
	
	ESpringCultivation("SCU", "Spring Cultivation"),
	
	ESpringChisel("SCH", "Spring Chisel + Disk"),
	EFallChisel("FCH", "Fall Chisel + Disk"),
	
	EMSpringMoldboard("SMB", "Spring Moldboard"),
	EFallMoldboard("FMB", "Fall Moldboard");
	
	String dbValue;
	String description;
	Tillage(String val, String _description) {
		this.dbValue = val;
		this.description = _description;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}
}
