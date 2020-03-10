package db;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import utils.Json;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.SqlRow;
import play.mvc.Http.Request;

@Entity
public class Farm extends Model {
	
	@Id
    public Long id;

    public String farmName;
    public String farmOwner;
    public String farmAddress;
    
    @OneToMany(cascade = CascadeType.ALL)
    public List<FieldGeometry> fieldGeometry = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    public List<Scenario> scenarios = new ArrayList<>();
    
    // WKT format, blah
    @Column(columnDefinition = "TEXT")
	public String location;
 
    public Farm name(String farmName) {
    	this.farmName = farmName;
    	return this;
    }
    public Farm owner(String farmOwner) {
    	this.farmOwner = farmOwner;
    	return this;
    }
    public Farm address(String farmAddress) {
    	this.farmAddress = farmAddress;
    	return this;
    }
 
    public Farm location(Float x, Float y) {
		SqlRow sw = Ebean.createSqlQuery("SELECT ST_AsText(ST_POINT(?,?)) as wkt_pt")
				.setParameter(1, x)
				.setParameter(2, y)
				.findOne();
		
		this.location = sw.getString("wkt_pt");
		return this;
    }
    
    //------------------------------------------------------
    public static void testLoadShapes() {
    	
    	File file = new File("./public/shapeFiles/exampleFields.geojson");
    	JsonNode fasJson = null;
    	Farm f = new Farm().
    			name("Grazing Acres").
    			owner("Jeff Dischler").
    			address("1034 Friar Ln.");
    	
    	try (FileInputStream fis = new FileInputStream(file)) {
    		fasJson = play.libs.Json.parse(fis);
            f.createFieldsFrom(fasJson);
    	}
    	catch(Exception e) {
			logger.error(e.toString());
    	}
    	f.save();
    }
   
    //------------------------------------------------------------
    public void createFieldsFrom(JsonNode geom) {

		ArrayNode featureList = (ArrayNode)geom.get("features");
		ObjectMapper om = new ObjectMapper();
		
		for (int idx = 0; idx < featureList.size(); idx++) {
			JsonNode feature = featureList.get(idx);
			
			// NOTE: just an example of getting the properties. Nothing is done with them right now but if field shapes
			//	included any extra data, such as OM, P, whatever...Then we could also extract that and do something useful.
			int f_id = 0;
			JsonNode props = feature.get("properties");
			if (props != null) {
				f_id = props.get("f_id").asInt();
			}
			
			JsonNode geo = feature.get("geometry");
			try {
				String geoJson = om.writeValueAsString(geo);
				
				FieldGeometry fg = new FieldGeometry()
						.fromGeoJSON(geoJson);
				//		.fromWKT(wkt); //TODO
	
				this.fieldGeometry.add(fg);
				
			} catch (JsonProcessingException e) {
				logger.error(e.toString());
			}
		}
    }

    //----------------------------------------------------
    public String getFieldShapesAsGeoJson() {
    	
    	StringBuilder sb = new StringBuilder(2048);
 /*   	sb.append("{type:\"FeatureCollection\",name:\"shapes\"," +
    		"crs:{type:\"name\",properties:{name:\"urn:ogc:def:crs:EPSG::3857\"}}," +
    		"features:[");
*/
    	sb.append("{\"type\":\"FeatureCollection\",\"name\":\"shapes\"," +
        		"\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:EPSG::3857\"}}," +
        		"\"features\":[");
    	
    	boolean isFirst = true;
    	for (FieldGeometry fg: this.fieldGeometry) {
    		if (!isFirst) sb.append(",");
    		
    		sb.append("{\"type\":\"Feature\",\"geometry\":");
    		
    		SqlRow sw;
/*    		sw = Ebean.createSqlQuery("SELECT ST_AsGeoJSON(ST_Transform(ST_SetSRID(ST_GeomFromText( ? ),3857),3071)) as gjson")
    				.setParameter(1, fg.geom)
    				.findOne();
    		
    		JsonNode jn = play.libs.Json.parse(sw.getString("gjson"));
    		ArrayNode an = (ArrayNode)jn.get("coordinates");
    		an = (ArrayNode) an.get(0);
    		
    		String polygon = "Polygon((";
    		for (int idx = 0; idx < an.size(); idx++) {
    			if (idx > 0) polygon += ",";
    			ArrayNode coord = (ArrayNode)an.get(idx);
    			Long x = Math.round(coord.get(0).asDouble() / 10.0) * 10;
    			Long y = Math.round(coord.get(1).asDouble() / 10.0) * 10;
    			polygon += x + " " + y;
    		}
    		polygon += "))";
    		sw = Ebean.createSqlQuery("SELECT ST_AsGeoJSON(ST_Transform(ST_SetSRID(ST_GeomFromText( ? ),3071),3857)) as gjson")
    				.setParameter(1, polygon)
    				.findOne();*/
    		sw = Ebean.createSqlQuery("SELECT ST_AsGeoJSON(ST_GeomFromText( ? )) as gjson")
    				.setParameter(1, fg.geom)
    				.findOne();
    		
    		String properties = "\"properties\":{\"f_id\":" + fg.id + "}";
    		sb.append(sw.getString("gjson"));
    		sb.append("," + properties + "}");
    		isFirst = false;
    	}
    	sb.append("]}");

    	return sb.toString();
    }
    
    //----------------------------------------------------
	public JsonNode getProperties() {
		return Json.pack("id",this.id,
			"name", this.farmName,
			"owner", this.farmOwner,
			"address", this.farmAddress
		);
				
	}
    //----------------------------------------------------
	public JsonNode getFeature() {
		SqlRow sw = Ebean.createSqlQuery("SELECT ST_AsGeoJSON(ST_GeomFromText(?)) as json")
				.setParameter(1, this.location)
				.findOne();
		
		JsonNode geoJson = play.libs.Json.parse(sw.getString("json"));
		
		return Json.pack("type", "Feature",
			"geometry", geoJson,
			"properties", this.getProperties()
		);
	}
    
    //----------------------------------------------------
    public static JsonNode getAllAsGeoJson() {
		JsonNode crs = Json.pack(
				"type", "name",
				"properties", Json.pack(
					"name", "urn:ogc:def:crs:EPSG::3857"
				)
			);

			ArrayNode features = JsonNodeFactory.instance.arrayNode();
			
			List<Farm> fs = Farm.find.all();
			for (Farm f: fs) {
				features.add(f.getFeature());
			}
			
			return Json.pack(
				"type", "FeatureCollection",
				"name", "farmSet",//"TainterSimpleBoundary",
				"crs", crs,
				"features", features
			);		
    	
    }
    
    public static final Finder<Long, Farm> find = new Finder<>(Farm.class);
    
    //----------------------------------------------------
    
    public Farm old_setName(String farmName) {
    	this.farmName = farmName;
    	return this;
    }
    public Farm old_setOwner(String farmOwner) {
    	this.farmOwner = farmOwner;
    	return this;
    }
    public Farm old_setAddress(String farmAddress) {
    	this.farmAddress = farmAddress;
    	return this;
    }
    
    public void old_createFieldsFrom(JsonNode geom) {

		ArrayNode featureList = (ArrayNode)geom.get("features");
		ObjectMapper om = new ObjectMapper();
		
		for (int idx = 0; idx < featureList.size(); idx++) {
			JsonNode feature = featureList.get(idx);
			
			int f_id = 0;
			JsonNode props = feature.get("properties");
			if (props != null) {
				f_id = props.get("f_id").asInt();
			}
			
			JsonNode geo = feature.get("geometry");
			try {
				String geoJson = om.writeValueAsString(geo);
				
				FieldGeometry fg = new FieldGeometry()
						.fromGeoJSON(geoJson);
				//		.fromWKT(wkt); //TODO
	
				this.fieldGeometry.add(fg);
				
			} catch (JsonProcessingException e) {
				logger.error(e.toString());
			}
		}
    }
    
    //------------------------------------------------------
    public static void old_testLoadShapes() {
    	
    	File file = new File("./public/shapeFiles/exampleFields.geojson");
    	JsonNode fasJson = null;
    	Farm f = new Farm().
    			old_setName("Grazing Acres").
    			old_setOwner("Jeff Dischler").
    			old_setAddress("1034 Friar Ln.");
    	f.save();
    	
    	try (FileInputStream fis = new FileInputStream(file)) {
    		fasJson =  play.libs.Json.parse(fis);
            f.old_createFieldsFrom(fasJson);
    	}
    	catch(Exception e) {
			logger.error(e.toString());
    	}
    	f.save();
    }
    
    //------------------------------------------------------
	public void old_save() {
		
		try (Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/GrazeScape", "postgres", "GrazeScape")) {
			if (id == null) { // INSERT
				String sql = "INSERT INTO farm " + 
						"(name, owner, address) VALUES " +
						"(?, ?, ?)";
				
				try (PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
					
					st.setString(1, this.farmName);
					st.setString(2, this.farmOwner);
					st.setString(3, this.farmAddress);
				
					int affectedRows = st.executeUpdate();
					if (affectedRows == 0) {
						throw new SQLException("Creating field farm, no rows affected.");
					}
					try (ResultSet generatedKeys = st.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							this.id = generatedKeys.getLong(1);
						}
						else {
							throw new SQLException("Creating farm failed, no ID obtained.");
						}
					}
				}
			}
			else { // UPDATE
				String sql = "UPDATE farm SET " + 
						"name = ?, owner = ?, address = ? " + 
						"WHERE id = ?";
				try (PreparedStatement st = con.prepareStatement(sql)) {
					
					st.setString(1,this.farmName);
					st.setString(2,this.farmOwner);
					st.setString(3, this.farmAddress);
					st.setLong(4, this.id);
					st.executeUpdate();
				}
			}
			
			// FIXME: TODO: Maybe best to pass the whole list...
			//	The underlying class needs to note whether items have been added to or removed from the list...
			//	
			for (FieldGeometry fg: this.fieldGeometry) {
				fg.save();
			}
		}
		catch(Exception e) {
			logger.error("Farm:save   error");
			logger.error(e.toString());
		}
	}
    
    //------------------------------------------------------
    public static Farm old_load(Long farmID) {
    	
    	return null;
    }
    
    //------------------------------------------------------
	public static void old_dbInit() {
		
		// TODO: DB connection pooling
		// https://commons.apache.org/proper/commons-dbcp/
		try (Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/GrazeScape", "postgres", "GrazeScape")) {
			try (Statement st = con.createStatement()) {
				String sql = "CREATE TABLE IF NOT EXISTS farm (" + 
						"id  		BIGSERIAL PRIMARY KEY, " +
						"name		TEXT, " + 
						"owner 		TEXT, " +
						"address	TEXT" +
					");";

				st.execute(sql);
			}
		}
		catch(Exception e) {
			logger.error("Farm:dbInit   error");	
			logger.error(e.toString());
		}
		FieldGeometry.old_dbInit();
	}
    
    private static final Logger logger = LoggerFactory.getLogger("app");

    //------------------------------------------------------
	public static JsonNode addField(Request request) {
		
		JsonNode node = request.body().asJson();

		Long farmId = utils.Json.safeGetLong(node, "farm_id");
		String wkt = utils.Json.safeGetString(node, "wkt");
		
		Long field_id = -1L;
		
		db.Farm f = db.Farm.find.byId(farmId);
		if (f != null) {
			FieldGeometry fg = new FieldGeometry();
			fg.fromWKT(wkt);
			f.fieldGeometry.add(fg);
			f.save();
			field_id = fg.id;
		}
		
		return utils.Json.pack("f_id", field_id);
	}
}