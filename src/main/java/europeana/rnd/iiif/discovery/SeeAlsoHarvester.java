package europeana.rnd.iiif.discovery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import europeana.rnd.iiif.discovery.model.ManifestOrCollection;
import europeana.rnd.iiif.discovery.model.SeeAlso;
import europeana.rnd.iiif.discovery.model.SeeAlsoProfile;

/**
 * Implements the step of downloading structured metadata present in the
 * "seeAlso" of IIIF Manifests.
 * 
 * May be configured to download specific metadata profiles (see the IIIF
 * Registry of Profiles at https://iiif.io/api/registry/profiles/)
 * 
 */
public class SeeAlsoHarvester {
	HashSet<SeeAlsoProfile> profilesToHarvest;
	HttpClient httpClient;

	/**
	 * @param httpClient        the implementation of an HTTP client to be used for
	 *                          making HTTP requests
	 * @param profilesToHarvest the metadata profiles that should be downloaded. If
	 *                          none is provided, all records will be downloaded.
	 */
	public SeeAlsoHarvester(HttpClient httpClient, SeeAlsoProfile... profilesToHarvest) {
		this.profilesToHarvest = new HashSet<>();
		this.httpClient = httpClient;
		if (profilesToHarvest != null) {
			for (SeeAlsoProfile profile : profilesToHarvest) {
				this.profilesToHarvest.add(profile);
			}
		}
	}

	/**
	 * Downloads all the metadata records (of the configured profiles) present in
	 * the "seeAlso" element of a IIIF Manifest or Collection.
	 * 
	 * @param manifestOrCollectionUrl
	 * @return list with the seeAlso elements with their respective metadata record
	 *         downloaded
	 * @throws IOException
	 * @throws ValidationException
	 */
	public List<SeeAlso> harvestFrom(String manifestOrCollectionUrl)
			throws FileNotFoundException, IOException, ValidationException {
		try {
			List<SeeAlso> downloadedSeeAlsos = new ArrayList<SeeAlso>();
			String manifestJsonString = httpClient.httpGet(manifestOrCollectionUrl);
			if (manifestJsonString == null)
				return downloadedSeeAlsos;
			Reader reader = new StringReader(manifestJsonString);

			JsonReader jsonReader = Json.createReader(reader);
			JsonObject topLevelJson = jsonReader.readObject();
			jsonReader.close();
			reader.close();

			ManifestOrCollection manifest = new ManifestOrCollection(topLevelJson);
			manifest.validateJson();

			for (SeeAlso seeAlso : manifest.getSeeAlso()) {
				SeeAlsoProfile profile = seeAlso.getProfile();
				if ((profile != null && profilesToHarvest.contains(profile)) || profilesToHarvest.isEmpty()) {
					String edmRecordUrl = seeAlso.getId();
					String edm = httpClient.httpGet(edmRecordUrl);
					seeAlso.setMetadataContent(edm);
					downloadedSeeAlsos.add(seeAlso);
				}
			}
			return downloadedSeeAlsos;
		} catch (ValidationException e) {
			throw new ValidationException("Validation error in " + manifestOrCollectionUrl + "\n" + e.getMessage(), e);
		}
	}

}
