package eu.europeana.research.iiif.discovery;

import java.io.IOException;
import java.util.logging.Level;

import eu.europeana.research.iiif.discovery.model.Activity;

public interface CrawlingHandler {
	public String httpGet(String url) throws IOException;
	public void processManifest(Activity activityOnManifest);
	public void log(String message);
	public void log(String message, Exception ex);
}
