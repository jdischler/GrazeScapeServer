package data_types;

import utils.Json;

import java.awt.geom.Point2D;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public class Farm {

	//--------------------------------------------------------------------------
	// MEMBER Variables
	//--------------------------------------------------------------------------
	// Unique ID of this farm
	private Integer			mID;
	
	private Point2D.Float 	mLocation = new Point2D.Float();
	
	// Optional 
	private String 			mName;
	private String			mOwner;
	private String			mAddress;
	private String			mNotes;
	
	private Integer			mAcreage;

	// FUTURE: TODO
	// Field ShapeFiles associated with this farm. Probably more likely to be a different class with a
	//	farm mID on it to link it back or similar.
	
	// FUTURE: TODO
	// Some kind of details about who created this and who controls how the data is exposed to other users, etc
	//private User	mUser;	
	
	//--------------------------------------------------------------------------
	public Farm() {
		
	}

	//--------------------------------------------------------------------------
	// Chainable Initialization and Setters
	//--------------------------------------------------------------------------
	public Farm assignID() {
		this.mID = mNextID.getAndIncrement();	return this;
	}
	//--------------------------------------------------------------------------
	public Farm setLocation(Float x, Float y) {
		this.mLocation.setLocation(x,y);		return this;
	}
	public Farm setLocation(Point2D.Float pt) {
		this.mLocation = pt; 	return this;
	}
	public Farm setName(String name) {
		this.mName = name; 		return this;
	}
	public Farm setOwner(String owner) {
		this.mOwner = owner;	return this;
	}
	public Farm setAddress(String address) {
		this.mAddress = address;	return this;
	}
	public Farm setNotes(String notes) {
		this.mNotes = notes;	return this;
	}
	
	public Farm track() {
		if (this.mID == null || this.mID < 1) {
			this.assignID();
		}
		mFarms.put(this.mID, this);
		return this;
	}

	public JsonNode getProperties() {
		return Json.pack("id",this.mID,
			"name", this.mName,
			"owner", this.mOwner,
			"address", this.mAddress
		);
				
	}
	public JsonNode getFeature() {
		return Json.pack("type", "Feature",
			"geometry", Json.pack("type", "Point", "coordinates", Json.array(this.mLocation.x, this.mLocation.y)),
			"properties", this.getProperties()
		);
	}
	
	//--------------------------------------------------------------------------
	// STATIC Variables
	//--------------------------------------------------------------------------
	private static AtomicInteger mNextID = new AtomicInteger(1);
	private static ConcurrentMap<Integer,Farm> mFarms = new ConcurrentHashMap<Integer,Farm>();
	
	
	public static JsonNode getAllAsGeoJson() {
		
		JsonNode crs = Json.pack(
			"type", "name",
			"properties", Json.pack(
				"name", "urn:ogc:def:crs:EPSG::3857"
			)
		);

		ArrayNode features = JsonNodeFactory.instance.arrayNode();
		mFarms.values().forEach(farm -> features.add(farm.getFeature()));
		
		return Json.pack(
			"type", "FeatureCollection",
			"name", "farmSet",//"TainterSimpleBoundary",
			"crs", crs,
			"features", features
		);		
	}
	
}
