package europeana.rnd.iiif.discovery.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;

import europeana.rnd.iiif.discovery.HttpClient;
import europeana.rnd.iiif.discovery.SeeAlsoHarvester;
import europeana.rnd.iiif.discovery.ValidationException;
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
public class MetadataHarvester {

	SeeAlsoHarvester seeAlsoHarvester;
	File repositoryFolder;
	HttpClient httpClient;

	public MetadataHarvester(File repositoryFolder, HttpClient httpClient, SeeAlsoProfile... profilesToHarvest) {
		this.repositoryFolder = repositoryFolder;
		this.httpClient = httpClient;
		seeAlsoHarvester = new SeeAlsoHarvester(httpClient, profilesToHarvest);
	}

	public void harvestFromManifest(String manifestUrl) throws FileNotFoundException, IOException, ValidationException {
		List<SeeAlso> seeAlsos = seeAlsoHarvester.harvestFrom(manifestUrl);
		if (!seeAlsos.isEmpty()) {
			// for this demos we are onnly saving one metadata record
			FileUtils.write(new File(repositoryFolder, URLEncoder.encode(manifestUrl, "UTF-8")),
					seeAlsos.get(0).getMetadataContent(), StandardCharsets.UTF_8);
		}
	}

	public void deleteSeeAlsoOfManifest(String manifestUrl) {
		try {
			new File(repositoryFolder, URLEncoder.encode(manifestUrl, "UTF-8")).delete();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
