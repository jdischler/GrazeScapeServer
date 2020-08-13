package db;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.SqlRow;
import play.mvc.Http.Request;

// Just the field Geometry
@Entity
public class FieldGeometry extends Model {

	@Id
    public Long id;
	
    @ManyToOne
    public Farm farm;
    
//    @OneToMany(cascade=CascadeType.ALL)
//   public Field field;

    // WKT format, blah
    @Column(columnDefinition = "TEXT")
	public String geom;
    
	public FieldGeometry fromGeoJSON(String geoJson) {

		SqlRow sw = Ebean.createSqlQuery("SELECT ST_AsText(ST_GeomFromGeoJSON( ? )) as wkt")
				.setParameter(1, geoJson)
				.findOne();
		
		this.geom = sw.getString("wkt");
		return this;
	}
	
	public void fromWKT(String wkt) {
		this.geom = wkt; 
	}
	
	public static JsonNode modifyFields(Request request) {
			
		ArrayNode ar = (ArrayNode)request.body().asJson();

		logger.info(ar.toString());
		for (JsonNode jn: ar) {
			Long field_id = utils.Json.safeGetLong(jn, "field_id");
			String wkt = utils.Json.safeGetString(jn, "wkt");
			
			db.FieldGeometry fg = db.FieldGeometry.find.byId(field_id);
			logger.info("Saving " + fg.geom);
			fg.geom = wkt;
//			fg.save();
			fg.update();
			logger.info("Saving " + wkt);
		}
		
		return utils.Json.pack("result", "ok");
	}

    private static final Logger logger = LoggerFactory.getLogger("app");
    public static final Finder<Long, FieldGeometry> find = new Finder<>(FieldGeometry.class);
}

/*
CREATE [ CONSTRAINT ] TRIGGER name { BEFORE | AFTER | INSTEAD OF } { event [ OR ... ] }
ON table
[ FROM referenced_table_name ]
[ NOT DEFERRABLE | [ DEFERRABLE ] { INITIALLY IMMEDIATE | INITIALLY DEFERRED } ]
[ FOR [ EACH ] { ROW | STATEMENT } ]
[ WHEN ( condition ) ]
EXECUTE PROCEDURE function_name ( arguments )

where event can be one of:

INSERT
UPDATE [ OF column_name [, ... ] ]
DELETE
TRUNCATE
*/