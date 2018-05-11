package eu.europeana.research.iiif.discovery;

import java.io.IOException;

import eu.europeana.research.iiif.discovery.model.Activity;

public interface CrawlingHandler {
	public String httpGet(String url) throws IOException;
	public void processManifest(Activity activityOnManifest);
}
