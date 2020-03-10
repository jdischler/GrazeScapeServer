package db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ebean.Finder;
import io.ebean.Model;

@Entity
public class Field extends Model {

	@Id
    public Long id;
	
    @ManyToOne
    public Scenario scenario;
    
    @ManyToOne
    public FieldGeometry geometry;// the geometry representation for this field
    
    // can be NULL. If populated, overrides Field Geometry settings.
    public Float pOverride = null;
    public Float omOverride = null;
    
    @OneToMany
    public Rotation rotation;

    @OneToMany
    public Tillage tillage;
    @OneToMany
    public Season tillageSeason;
    
    
    private static final Logger logger = LoggerFactory.getLogger("app");

    public static final Finder<Long, Field> find = new Finder<>(Field.class);
}
