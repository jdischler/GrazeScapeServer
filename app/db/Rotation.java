package db;

import java.util.HashSet;
import java.util.Set;

import io.ebean.annotation.DbEnumValue;

public enum Rotation {
	
	// Many of these will be specified by a fixed underlying cropYear definition
	//	Grass-based systems default the cropYear/Landcover but it can change
	EContinuousCorn(		"CC", "Continuous Corn",	true, true), // CG
	ECashGrain(				"CG", "Cash Grain",			true, true), // CG, SB
	EDairyRotation1(		"D1", "Dairy Rotation 1 (Corn Grain/Silage & Alfalfa)",	true, true), // CG, CS, AE, A1-2
	EDairyRotation2(		"D2", "Dairy Rotation 2 (Corn Silage, Soybeans, & Oats)",	true, true), // CS, SB, OT
	
	EDryLot(				"DL", "Dry Lot",			false, false),
	EPasture(				"PS", "Pasture",			false, false),
	EEstablishPasture(		"PE", "Pasture (Establish)",true,  false),
	;
	
	public String dbValue;
	public String description;
	public Boolean canTill;
	public Boolean canCoverCrop;
	
	Rotation(String val, String desc, Boolean _tillable, Boolean _canCoverCrop) {
		this.dbValue = val;
		this.description = desc;
		this.canTill = _tillable;
		this.canCoverCrop = _canCoverCrop;
	}
	
	@DbEnumValue
	public String getValue() {
		return dbValue;
	}
	
	//-----------------------------------------------------------------------------------------
	public static Rotation byCode(String dbCodeValue) {
		for(Rotation r: Rotation.values()) {
			if (r.dbValue.equalsIgnoreCase(dbCodeValue)) {
				return r;
			}
		}
		return null;
	}
	
	//-----------------------------------------------------------------------------------------
	public static Set<CropYear> toCropYears(String dbValString) throws Exception {
		
		Rotation rotation = null;
		for(Rotation r: Rotation.values()) {
			if (dbValString.equalsIgnoreCase(r.dbValue)) {
				rotation = r;
				break;
			}
		}
		if (rotation == null) throw new Exception("Rotation:toCropYears no matching dbValue <" + dbValString +">");
		
		return toCropYears(rotation);
	}
	
	//-----------------------------------------------------------------------------------------
	public static Set<CropYear> toCropYears(Rotation rot) {
		
		Set<CropYear> scy = new HashSet<>();
		
		switch(rot) {
			case EPasture:
			default:
				// Defaults to generic grass but the grazing setup will override with a specific grass composition
				scy.add(new CropYear(Landcover.EGenericGrass, 1.0f));
				break;

			case EEstablishPasture:
				// TODO: should this be zero or a small value like 0.25???				
				scy.add(new CropYear(Landcover.EGenericGrass, 1.0f)); 
				break;
				
			case EDryLot:
				scy.add(new CropYear(Landcover.EDryLot, 1.0f));
				break;
				
			case EContinuousCorn:
				scy.add(new CropYear(Landcover.ECornGrain, 1.0f));
				break;
				
			case ECashGrain:
				scy.add(new CropYear(Landcover.ECornGrain, 1.0f / 2.0f));
				scy.add(new CropYear(Landcover.ESoybeans, 1.0f / 2.0f));
				break;
				
			case EDairyRotation1:
				scy.add(new CropYear(Landcover.ECornGrain, 1.0f / 5.0f));
				scy.add(new CropYear(Landcover.ECornSilage, 1.0f / 5.0f));
				scy.add(new CropYear(Landcover.EAlfalfa, 3.0f / 5.0f));
				break;
				
			case EDairyRotation2:
				scy.add(new CropYear(Landcover.ECornSilage, 1.0f / 3.0f));
				scy.add(new CropYear(Landcover.ESoybeans, 1.0f / 3.0f));
				scy.add(new CropYear(Landcover.EOats, 1.0f / 3.0f));
				break;
		}
		return scy;
	}
}
