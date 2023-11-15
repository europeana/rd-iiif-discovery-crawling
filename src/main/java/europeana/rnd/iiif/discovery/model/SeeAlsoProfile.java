package europeana.rnd.iiif.discovery.model;

/**
 * An enumeration of the metadata profiles currently registered in the IIIF
 * Registry of Profiles See https://iiif.io/api/registry/profiles/
 */
public enum SeeAlsoProfile {

	EDM("http://www.europeana.eu/schemas/edm/"), MODS("http://www.loc.gov/mods/v3"),
	LINKED_ART("https://linked.art/ns/terms"), LIDO("http://www.lido-schema.org/"),
	WIKIDATA("https://www.wikidata.org/entity/Q115365241"), DARWIN_CORE("http://www.tdwg.org/standards/450"),
	ALTO("http://www.loc.gov/standards/alto"), MARC_XML("http://www.loc.gov/standards/marcxml"),
	DUBLIN_CORE("http://purl.org/dc/terms/");

	String uri;

	private SeeAlsoProfile(String profileUri) {
		this.uri = profileUri;
	}

	public static SeeAlsoProfile fromUri(String profileUri) {
		for (SeeAlsoProfile profile : values()) {
			if (profile.uri.equals(profileUri))
				return profile;
		}
		return null;
	}

	public String getUri() {
		return uri;
	}

}
