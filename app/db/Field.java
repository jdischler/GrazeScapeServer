package db;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.DbArray;
import utils.Json;

@Entity
public class Field extends Model {

	@Id
    public Long id;
	
    @ManyToOne
    public Scenario scenario;
    
    @ManyToOne
    public FieldGeometry geometry;// the geometry representation for this field
    
    @NotNull
    public Float soilP = 32.0f; // county average is 32, can't be null;
    
    // When null, uses SSURGO raster OM data
    public Float om = null;
   
    // Rotation and Tillage informs the PLoss model
    public Rotation rotation;
    public Tillage tillage;
//    public Season fertSeason;
    public Boolean hasCoverCrop;
    public Boolean onContour;
    
    // The yield model needs actual crop years and specific crops, particularly in the case of Pasture.
    //	Crop year can default to whatever specific grass species/mix but then be overridden in the 
    //	more detailed pasture setup
    @OneToMany(cascade=CascadeType.ALL)
    public Set<CropYear> cropYears = new HashSet<>();
    
    // Additionally, pastures need to have a density setting, which again default to whatever but is refined/updated
    //	based on the animal density specified for each pasture.
    public Float rotationalDensity;
    
       
    // Assign settings to a field from the client
    //-----------------------------------------------------------------------
	public Field withSettings(JsonNode settings) {
	
		Ebean.beginTransaction();
		try {
			if (Json.isActive(settings, "soil_p")) {
				soilP = Json.safeGetOptionalFloat(settings.get("soil_p"), "value", 32.0f);
			}
			if (Json.isActive(settings, "crop")) {
				String rotationCode = Json.safeGetOptionalString(settings.get("crop"), "value", "PS");
				this.rotation = Rotation.byCode(rotationCode);
				try {
					// TODO: is this necessary? VERIFY whether Ebean truly cleans these up automatically 
					if (!cropYears.isEmpty()) {
						cropYears.clear();
					}
					cropYears = Rotation.toCropYears(rotationCode);
				} catch (Exception e) {
					logger.error(e.toString());
				}
			}
		}
		finally {
			Ebean.endTransaction();
		}
/*		"soil_p":{"is_active":true,"value":32},
		"crop":{"is_active":false,"value":"ps"},
		"tillage":{"is_active":false,"value":"spcu"},
		"manure":{"is_active":false,"value":10},
		"fertilizer":{"is_active":false,"n":100,"p":30},
		"cropValue":{"crop":"ps"},
		"tillageValue":{"tillage":"spcu"}}*/		
		return this;
	}

    private static final Logger logger = LoggerFactory.getLogger("app");
    public static final Finder<Long, Field> find = new Finder<>(Field.class);
	
}
