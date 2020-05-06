package utils;

import play.*;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
//import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.lang.RuntimeException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;


// Json helper utils...
//------------------------------------------------------------------------------
public class Json
{
	//--------------------------------------------------------------------------	
	public static ObjectNode newPack(){
		
		return JsonNodeFactory.instance.objectNode();
	}
	
	// Arguments must be a multiple of two. always a key-value pair. 
	//	Key must be a String
	//	Value should be some kind of Object (Boolean, Long, Double, String, JsonNode...)
	//--------------------------------------------------------------------------	
	public static JsonNode pack(Object ... arguments) throws RuntimeException {
		
		ObjectNode data = JsonNodeFactory.instance.objectNode();
		addToPack(data, arguments);
		return data;
/*		if (!(arguments.length % 2 == 0)){
			throw new RuntimeException("Json: packageData: bad argument list; not an even number");
		}
		else {
			for (int i = 0; i < arguments.length; i += 2) {
				putKeyValue(data, arguments[i].toString(), arguments[i+1]);
			}
		}
		return data;*/
	}

	// Allows extended an already packed objectNode though no sanity checking is provided for collisions or other badness
	//--------------------------------------------------------------------------	
	public static JsonNode addToPack(ObjectNode pack, Object ... arguments) throws RuntimeException {
		
		if (!(arguments.length % 2 == 0)){
			throw new RuntimeException("Json: packageData: bad argument list; not an even number");
		}
		else {
			for (int i = 0; i < arguments.length; i += 2) {
				putKeyValue(pack, arguments[i].toString(), arguments[i+1]);
			}
		}
		return pack;
	}
	
	// Handle casting values to the correct type for placement on the carrier object node
	// TODO: consider adding arrayList support?
	//--------------------------------------------------------------------------	
	public static final void putKeyValue(ObjectNode carrierNode, String key, Object value) {
	
		if (value instanceof Boolean) {
			carrierNode.put(key, (Boolean)value);
			
		} else if (value instanceof Double) {
			carrierNode.put(key, (Double)value);

		} else if (value instanceof Float) {
			carrierNode.put(key, (Float)value);
			
		} else if (value instanceof Long) {
			carrierNode.put(key, (Long)value);
			
		} else if (value instanceof Integer) {
			carrierNode.put(key, (Integer)value);
			
		} else if (value instanceof JsonNode) {
			carrierNode.set(key, (JsonNode)value);
			
		} else if (value instanceof String) {
			carrierNode.put(key, value.toString());
			
		} else if (value instanceof String[]) { // FIXME: any way to do more generic array handling?
			ArrayNode ar = JsonNodeFactory.instance.arrayNode();
			for (String v: (String[])value) {
				ar.add(v);
			}
			carrierNode.set(key, ar);
		} else if (value instanceof Integer[]) {
			ArrayNode ar = JsonNodeFactory.instance.arrayNode();
			for (Integer v: (Integer[])value) {
				ar.add(v);
			}
			carrierNode.set(key, ar);
/*		} else if (value instanceof Rectangle) {
			Rectangle r = (Rectangle)value;
			carrierNode.set(key, packageData("x",r.x, "y",r.y, "w",r.width, "h", r.height));
*/			
		} else if (value == null) {
			carrierNode.putNull(key);
		}
		else { // last ditch effort, default encoding.
			carrierNode.put(key, value.toString());
		}
	}
	
	//--------------------------------------------------------------------------	
	public static ArrayNode array(Object ... arguments) {
		
		ArrayNode data = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < arguments.length; i++) {
			Object o = arguments[i];
			if (o instanceof Boolean) {
				data.add((Boolean)o);
			
			} else if (o instanceof Double) {
				data.add((Double)o);

			} else if (o instanceof Long) {
				data.add((Long)o);
				
			} else if (o instanceof Integer) {
				data.add((Integer)o);
				
			} else  if (o instanceof String) {
				data.add(o.toString());
				
			} else {
				data.add(o.toString());
			}
		}
		return data;
	}
	//--------------------------------------------------------------------------
	public static final ArrayNode toArray(Collection<? extends GeoJson> tjs) {
		ArrayNode data = JsonNodeFactory.instance.arrayNode();
		tjs.forEach(
			tj -> data.add(tj.toGeoJson())
		);
		return data;
	}
	
	//--------------------------------------------------------------------------
	public static final String safeGetString(JsonNode objectNode, String key) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetString: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null) {
				throw new RuntimeException("Json: safeGetString: key <" + key + "> doesn't exist in object node");
			} 
			else if (!valNode.isTextual()) {
				throw new RuntimeException("Json: safeGetString: key <" + key + "> is not textual");
			}
			return valNode.asText();
		}
	}

	// Allow fetching an optional key/value pair
	//--------------------------------------------------------------------------
	public static final String safeGetOptionalString(JsonNode objectNode, String key) throws RuntimeException {
		// optionalValue can be null
		return safeGetOptionalString(objectNode, key, null);
	}
	//--------------------------------------------------------------------------
	public static final String safeGetOptionalString(JsonNode objectNode, String key, String optionalValue) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetOptionalString: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null || valNode.isNull()) {
				return optionalValue; // NOTE: this can also be null
			} 
			else if (!valNode.isTextual()) {
				throw new RuntimeException("Json: safeGetOptionalString: key <" + key + "> is not textual");
			}
			return valNode.asText();
		}
	}

	//--------------------------------------------------------------------------
	public static final Boolean safeGetBoolean(JsonNode objectNode, String key) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetBoolean: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null) {
				throw new RuntimeException("Json: safeGetBoolean: key <" + key + "> doesn't exist in object node");
			}
			else if (!valNode.isBoolean()) {
				throw new RuntimeException("Json: safeGetBoolean: key <" + key + "> is not a valid boolean");
			}
			return valNode.asBoolean();
		}
	}

	//--------------------------------------------------------------------------
	public static final Boolean safeGetOptionalBoolean(JsonNode objectNode, String key,
			Boolean defaultOptionalValue) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetOptionalBoolean: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null) {
				return defaultOptionalValue; // can be null
			}
			else if (!valNode.isBoolean()) {
				throw new RuntimeException("Json: safeGetOptionalBoolean: key <" + key + "> is not a valid boolean");
			}
			return valNode.asBoolean();
		}
	}
	
	//--------------------------------------------------------------------------
	public static final Integer safeGetInteger(JsonNode objectNode, String key) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetInteger: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null) {
				throw new RuntimeException("Json: safeGetInteger: key <" + key + "> doesn't exist in object node");
			}
			else if (!valNode.canConvertToInt()) {
				throw new RuntimeException("Json: safeGetInteger: key <" + key + "> is not a valid integer");
			}
			return valNode.asInt();
		}
	}

	//--------------------------------------------------------------------------
	public static final Integer safeGetOptionalInteger(JsonNode objectNode, String key, Integer optionalValue) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetOptionalInteger: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null || valNode.isNull()) {
				return optionalValue; // NOTE: this can also be null
			} 
			else if (!valNode.canConvertToInt()) {
				throw new RuntimeException("Json: safeGetOptionalInteger: key <" + key + "> is not a valid integer");
			}
			return valNode.asInt();
		}
	}

	// Allow fetching an optional key/value pair
	//--------------------------------------------------------------------------
	public static Long safeGetOptionalLong(JsonNode objectNode, String key) throws RuntimeException {
		// optionalValue can be null
		return safeGetOptionalLong(objectNode, key, null);
	}
	//--------------------------------------------------------------------------
	public static Long safeGetOptionalLong(JsonNode objectNode, String key, Long optionalValue) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetOptionalLong: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null || valNode.isNull()) {
				return optionalValue; // NOTE: this can also be null
			} 
			else if (!valNode.canConvertToLong()) {
				throw new RuntimeException("Json: safeGetOptionalLong: key <" + key + "> is not a valid long");
			}
			return valNode.asLong();
		}
	}
	
	//--------------------------------------------------------------------------
	public static final Long safeGetLong(JsonNode objectNode, String key) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetLong: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null) {
				throw new RuntimeException("Json: safeGetLong: key <" + key + "> doesn't exist in object node");
			}
			else if (!valNode.canConvertToLong()) {
				throw new RuntimeException("Json: safeGetLong: key <" + key + "> is not a valid long");
			}
			return valNode.asLong();
		}
	}
	
	//--------------------------------------------------------------------------
	public static final Double safeGetDouble(JsonNode objectNode, String key) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetDouble: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null) {
				throw new RuntimeException("Json: safeGetDouble: key <" + key + "> doesn't exist in object node");
			}
			else if (!valNode.isNumber()) {
				throw new RuntimeException("Json: safeGetDouble: key <" + key + "> could not be a double, it's a :" + valNode.toString());
			}
			return valNode.asDouble();
		}
	}

	//--------------------------------------------------------------------------
	public static final Float safeGetFloat(JsonNode objectNode, String key) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetFloat: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null) {
				throw new RuntimeException("Json: safeGetFloat: key <" + key + "> doesn't exist in object node");
			}
			else if (!valNode.isNumber()) {
				throw new RuntimeException("Json: safeGetFloat: key <" + key + "> is not a valid float");
			}
			return valNode.floatValue();
		}
	}

	//--------------------------------------------------------------------------
	public static final Float safeGetOptionalFloat(JsonNode objectNode, String key, Float optionalValue) throws RuntimeException {
	
		if (objectNode == null) {
			throw new RuntimeException("Json: safeGetOptionalFloat: no object node to check");
		}
		else {
			JsonNode valNode = objectNode.get(key);
			if (valNode == null || valNode.isNull()) {
				return optionalValue;
			}
			else if (!valNode.isNumber()) {
				throw new RuntimeException("Json: safeGetOptionalFloat: key <" + key + "> is not a valid float");
			}
			return valNode.floatValue();
		}
	}

	//--------------------------------------------------------------------------
	public static final Rectangle2D.Double getRectangleDouble(JsonNode node, String key) throws RuntimeException {
		
		ArrayNode ar = Json.getArray(node, "coordinates");
		
		if (ar.size() != 4) {
			throw new RuntimeException("Json: getRectangleDouble: expected 4 elements");			
		}
		
		// TODO: add more validation if needed
		return new Rectangle2D.Double(
			ar.get(0).asDouble(),
			ar.get(1).asDouble(),
			ar.get(2).asDouble(),
			ar.get(3).asDouble()
		);
	}

	//--------------------------------------------------------------------------
	public static final Rectangle getRectangleInteger(JsonNode node, String key) throws RuntimeException {
		
		ArrayNode ar = Json.getArray(node, "coordinates");
		
		if (ar.size() != 4) {
			throw new RuntimeException("Json: getRectangleInteger: expected 4 elements");			
		}
		
		// TODO: add more validation if needed
		return new Rectangle(
			ar.get(0).asInt(),
			ar.get(1).asInt(),
			ar.get(2).asInt(),
			ar.get(3).asInt()
		);
	}

	// Given someObject = {
	//		someKey: {
	//			is_active: true,
	//			someData: ...
	//		}
	//	}
	// Usage: if (Json.isActive(someObject, "someKey") ) then...
	//--------------------------------------------------------------------------
	public static final Boolean isActive(JsonNode objectNode, String key) {
		if (objectNode != null && objectNode.get(key) != null) {
			JsonNode subNode = objectNode.get(key);
			return safeGetOptionalBoolean(subNode, "is_active", false);
		}
		return false;
	}
	
	//--------------------------------------------------------------------------
	public static final void toDisk(JsonNode root, String path) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		writer.writeValue(new File(path), root);
	}
	
	//--------------------------------------------------------------------------
	public static final JsonNode fromDisk(String path) throws IOException, JsonProcessingException {
		
		String content = new String ( Files.readAllBytes( Paths.get(path) ) );
		ObjectMapper mapper = new ObjectMapper();
		ObjectReader reader = mapper.reader();
		return reader.readTree(content);
	}
	
	//--------------------------------------------------------------------------
	public static final ArrayNode getArray(JsonNode node, String key) throws RuntimeException {

		if (node == null) {
			throw new RuntimeException("Json: toArray: no object node to check");
		}
		else {
			JsonNode valNode = node.get(key);
			if (valNode == null || valNode.isNull()) {
				throw new RuntimeException("Json: toArray: no value at key <" + key + ">");
			}
			else if (!valNode.isArray()) {
				throw new RuntimeException("Json: toArray: key <" + key + "> is not an array");
			}
			return (ArrayNode)valNode;
		}
	}
	
}
