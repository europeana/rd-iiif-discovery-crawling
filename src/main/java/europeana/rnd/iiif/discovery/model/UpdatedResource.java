package europeana.rnd.iiif.discovery.model;

import javax.json.JsonValue;

import europeana.rnd.iiif.discovery.ValidationException;

/**
 * Data object for the resources referenced in the "object" and "target"
 * properties of as:Activity
 *
 */
public class UpdatedResource extends JsonDataObject {

	public UpdatedResource(JsonValue json) throws ValidationException {
		super(json);
	}

	public void validateJson() throws ValidationException {
		if (!json.containsKey("id"))
			throw new ValidationException("'id' is missing");
		if (!json.containsKey("type"))
			throw new ValidationException("'type' is missing");
	}

}
