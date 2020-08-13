package db;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import io.ebean.Finder;
import io.ebean.Model;

@Entity
public class CropYear extends Model {
	
	@Id
    public Long id;
	
    @ManyToOne
    public Field field;
    
    public Landcover 	dominantCrop;
    public Float		dominantRatio; // years in crop / total years
    
    public Landcover 	secondaryCrop;
    public Float		secondaryRatio;	// specific to crop, but example for legumes -> represents % legumes, e.g. 0.3 = 30%
    
    public CropYear() {}
    public CropYear(Landcover _dominantCrop) {	// Assumes 100% ratio
    	dominantCrop = _dominantCrop;
    	dominantRatio = 1.0f;
    }
    public CropYear(Landcover _dominantCrop, Float _ratio) {
    	dominantCrop = _dominantCrop;
    	dominantRatio = _ratio;
    }
    
    public CropYear addSecondaryCrop(Landcover secCrop, Float secRatio) {
    	secondaryCrop = secCrop;
    	secondaryRatio = secRatio;
    	return this;
    }
    
    public static final Finder<Long, CropYear> find = new Finder<>(CropYear.class);
    
}
