package db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.ebean.annotation.DbEnumValue;
import utils.Json;

public enum RotationalFrequency {
	
	ERotational4("R4", "< 1 Day",	1.17f),
	ERotational3("R3", "1 Day", 	1.0f),
	ERotational2("R2", "3 Days", 	0.95f),
	ERotational1("R1", "7 Days", 	0.75f),
	EContinuous2("C2", "> 7 Days", 	0.65f),
//	EContinuous2("C1", "> 7 Days", 	0.65f),??
	;
	
	public String dbValue;
	public String description;
	public Float relativeYield;
	
	RotationalFrequency(String val, String desc, Float _relativeYield) {
		this.dbValue = val;
		this.description = desc;
		this.relativeYield = _relativeYield;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}
	
	//-----------------------------------------------------------------------------------------
	public static RotationalFrequency byCode(String dbCodeValue) {
		for(RotationalFrequency r: RotationalFrequency.values()) {
			if (r.dbValue.equalsIgnoreCase(dbCodeValue)) {
				return r;
			}
		}
		return null;
	}
	
	//-----------------------------------------------------------------------------------------
	public static JsonNode toJson() {
		
		ArrayNode an = Json.makeArray();
		for(RotationalFrequency r: RotationalFrequency.values()) {
			an.add(Json.pack("label", r.description, "enum", r.dbValue));
		}
		return an;
	}

}
