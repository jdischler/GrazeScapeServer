package db;

import io.ebean.annotation.DbEnumValue;

//---------------------------------------------------------------
public enum Landcover {
//---------------------------------------------------------------
	
	EDryLot(		
		"DL", "Dry Lot",
		"/conf/modelDefs/yield/dryLot.csv",
		0x0001),
	
	ECornGrain(		
		"CG", "Corn Grain",
		"/conf/modelDefs/yield/corn.csv",
		0x0002),
	
	ECornSilage(	
		"CS", "Corn Silage",
		"/conf/modelDefs/yield/cornSilage.csv",
		0x0004),
	
	ESoybeans(		
		"SB", "Soybeans",
		"/conf/modelDefs/yield/soy.csv",
		0x0008),
	
	EOats(		
		"OT", "Oats",
		"/conf/modelDefs/yield/soy.csv", 	// FIXME
		0x0010),
	
	EWheat(
		"WH", "Wheat",
		"/conf/modelDefs/yield/soy.csv",	// FIXME
		0x0020),
	
	EAlfalfa(		
		"AL", "Alfalfa",
		"/conf/modelDefs/yield/alfalfa.csv",
		0x0040),
	
	EGenericGrass(	
		"GG", "Generic Grass",
		"/conf/modelDefs/yield/genericGrass.csv",
		0x0080),
	
	EBluegrass(		
		"BG", "Bluegrass",
		"/conf/modelDefs/yield/bluegrassWhiteClover.csv",
		0x0100),
	
	EOrchardgrass(	
		"OG", "Orchardgrass",
		"/conf/modelDefs/yield/orchardgrassAlsike.csv",
		0x0200),
	
	ETimothy(		
		"TA", "Timothy",
		"/conf/modelDefs/yield/timothyAlsike.csv",
		0x0400),
	
	//Secondary landcovers (cover crops and legumes)
	//---------------------------------------------------------------
	ELegume(		
		"LG", "Legume",
		null,		// FIXME
		0x0800),
	
	ESmallGrain(	
		"SG", "Small Grain (cover)",
		null,		// FIXME
		0x1000),
	
	EWinterRye(		
		"WR", "Winter Rye",
		null,		// FIXME
		0x2000),
	;
	
	// Extra data...
	public String dbValue;
	public String description;
	public String yieldModel;
	public Integer bitEncoding;
	
	// _encodingValue is the unique value given a single set bit so it limited to 31 landcovers. 
	//	These values are only used run-time to encode multiple landcovers existing at a single pixel
	//	location. In other words, changing these values or ordering is fine as long as each has a unique 
	//	value at the bit-level since they are never persisted...
	//-------------------------------------------------------------------------------------------------
	Landcover(String val, String description, String yModel, Integer _encodingValue) {
		this.dbValue = val;
		this.description = description;
		this.yieldModel = yModel;
		this.bitEncoding = _encodingValue;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}
}
