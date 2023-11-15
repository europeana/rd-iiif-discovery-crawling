package europeana.rnd.iiif.discovery.model;

import javax.json.JsonValue;

import europeana.rnd.iiif.discovery.ValidationException;

/**
 * Data object for the "seeAlso" resources in a (IIIF Presentation) resource
 *
 */
public class SeeAlso extends JsonDataObject {

	String metadataContent;

	public SeeAlso(JsonValue json) throws ValidationException {
		super(json);
	}

	public void validateJson() throws ValidationException {
		if (!json.containsKey("@id"))
			throw new ValidationException("'@id' is missing in 'seeAlso'.");
	}

	public String getId() {
		return json.getString("@id");
	}

	public SeeAlsoProfile getProfile() {
		if (!json.containsKey("profile"))
			return null;
		return SeeAlsoProfile.fromUri(json.getString("profile"));
	}

	public String getMetadataContent() {
		return metadataContent;
	}

	public void setMetadataContent(String metadataContent) {
		this.metadataContent = metadataContent;
	}

}
