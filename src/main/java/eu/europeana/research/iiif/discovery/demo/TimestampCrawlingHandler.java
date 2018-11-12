package eu.europeana.research.iiif.discovery.demo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import eu.europeana.research.iiif.discovery.CrawlingHandler;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.model.Activity;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;

public class TimestampCrawlingHandler implements CrawlingHandler {

	public TimestampCrawlingHandler() throws Exception {
	}
	
	@Override
	public String httpGet(String url) throws IOException {
		int tries = 0;
		while (true) {
			tries++;
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				if(conn.getResponseCode()>=300 && conn.getResponseCode()<400) {
					String location = conn.getHeaderField("Location");
					if(location!=null)
						return httpGet(location);
				}
				return IOUtils.toString(conn.getInputStream(), "UTF-8");
			} catch (IOException ex) {
				if (tries >= 3)
					throw ex;
			}
		}
	}

	@Override
	public void processManifest(Activity activityOnManifest) {
	}
	

	@Override
	public void log(String message) {
		System.out.println(message);
	}


	@Override
	public void log(String message, Exception ex) {
		System.out.println(message);
		ex.printStackTrace(System.out);
	}
	
}