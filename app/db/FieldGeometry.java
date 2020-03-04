package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import play.mvc.Http.Request;

@Entity
public class FieldGeometry extends Model {

	@Id
    public Long id;
	
    @ManyToOne
    public Farm farm;

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
	
	public static void old_dbInit() {
		
		// TODO: DB connection pooling
		// https://commons.apache.org/proper/commons-dbcp/
		try (Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/GrazeScape", "postgres", "GrazeScape")) {
			try (Statement st = con.createStatement()) {
				String sql = "CREATE TABLE IF NOT EXISTS field_geometry (" + 
						"id  		BIGSERIAL PRIMARY KEY, " +
						"farm_id	BIGINT NOT NULL, " + // TODO: mark as a fK to farm table...
						"geom_wkt 	TEXT, " +
						"geom		geometry(MultiPolygon,3857) DEFAULT NULL" +
					");";

				st.execute(sql);
			}
		}
		catch(Exception e) {
			logger.error("FieldGeometry:dbInit   error");	
			logger.error(e.toString());
		}
	}
	
	public void old_save() {
		
		try (Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/GrazeScape", "postgres", "GrazeScape")) {
			if (id == null) { // INSERT
				String sql = "INSERT INTO field_geometry " + 
						"(farm_id, geom_wkt, geom) VALUES " +
						"(?, ?, ST_Multi(ST_SetSRID(ST_GeomFromText(?), 3857)))";
				
				try (PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
					
					st.setLong(1, 999);//this.mFarmID);
					st.setString(2, this.geom);
					st.setString(3, this.geom);
				
					int affectedRows = st.executeUpdate();
					if (affectedRows == 0) {
						throw new SQLException("Creating field failed, no rows affected.");
					}
					try (ResultSet generatedKeys = st.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							this.id = generatedKeys.getLong(1);
						}
						else {
							throw new SQLException("Creating field failed, no ID obtained.");
						}
					}
				}
			}
			else { // UPDATE
				String sql = "UPDATE field_geometry SET " + 
						"geom_wkt = ?, geom = ST_Multi(ST_SetSRID(ST_GeomFromText(?), 3857)) " + 
						"WHERE id = ?";
				try (PreparedStatement st = con.prepareStatement(sql)) {
					
					st.setString(1,this.geom);
					st.setString(2,this.geom);
					st.setLong(3, this.id);
					st.executeUpdate();
				}
			}
		}
		catch(Exception e) {
			logger.error("FieldGeometry:save   error");	
			logger.error(e.toString());
		}
	}

    private static final Logger logger = LoggerFactory.getLogger("app");

    public static final Finder<Long, FieldGeometry> find = new Finder<>(FieldGeometry.class);

	public static JsonNode modifyFields(Request request) {
			
		ArrayNode ar = (ArrayNode)request.body().asJson();

		logger.info(ar.toString());
		for (JsonNode jn: ar) {
			Long field_id = utils.Json.safeGetLong(jn, "field_id");
			String wkt = utils.Json.safeGetString(jn, "wkt");
			
			db.FieldGeometry fg = db.FieldGeometry.find.byId(field_id);
			fg.geom = wkt;
			fg.save();
		}
		
		return utils.Json.pack("result", "ok");
	}
	
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