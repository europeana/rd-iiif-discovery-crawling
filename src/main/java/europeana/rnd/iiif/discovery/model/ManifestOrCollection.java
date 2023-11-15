package europeana.rnd.iiif.discovery.model;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import europeana.rnd.iiif.discovery.ValidationException;

/**
 * Data object for a iiif:Manifest or a iiif:Collection. Processes only the
 * "seeAlso" part of the Manifest/Collection
 */
public class ManifestOrCollection extends JsonDataObject {

	List<SeeAlso> seeAlso;

	public ManifestOrCollection(JsonValue json) throws ValidationException {
		super(json);
		seeAlso = new ArrayList<SeeAlso>();
		for (JsonValue val : ((JsonObject) json).getJsonArray("seeAlso")) {
			seeAlso.add(new SeeAlso(val));
		}
		;
	}

	public void validateJson() throws ValidationException {
		if (!json.containsKey("@type"))
			throw new ValidationException("'@type' is missing.");
		String type = json.getString("@type");
		if (!type.equals("Manifest") && !type.equals("Collection") && !type.equals("sc:Manifest")
				&& !type.equals("sc:Collection"))
			throw new ValidationException("'type' not supported: " + type);

		if (!json.containsKey("seeAlso") || json.get("seeAlso").getValueType() != ValueType.ARRAY)
			throw new ValidationException("'seeAlso' is missing or invalid");
	}

	public List<SeeAlso> getSeeAlso() {
		return seeAlso;
	}

}
