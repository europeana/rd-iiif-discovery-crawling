package europeana.rnd.iiif.discovery.model;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import europeana.rnd.iiif.discovery.ValidationException;

/**
 * The base class for the utility classes for processing the JSON-LD responses
 * of IIIF Change Discovery
 *
 */
public abstract class JsonDataObject {

	protected JsonObject json;

	public JsonDataObject(JsonValue json) throws ValidationException {
		super();
		if (json.getValueType() != ValueType.OBJECT)
			throw new ValidationException("Invalid JSON. Object expected.");
		this.json = json.asJsonObject();
	}

	public String getId() {
		return json.getString("id");
	}

	public String getType() {
		return json.getString("type");
	}

	public abstract void validateJson() throws ValidationException;

}
