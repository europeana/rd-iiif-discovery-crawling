package eu.europeana.research.iiif.discovery.demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

import eu.europeana.research.iiif.discovery.CrawlingHandler;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.model.Activity;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker.Deleted;

public class ScriptIiifManifestChangeDiscoveryDemo {

	
	public static void main(String[] args) throws Exception {
//		String inputDiscoveryJson="file:src/test/data/iiif-discovery-oclc-15878.json";
//		String inputDiscoveryJson="file:src/test/data/iiif-discovery-ncsu.json";
//		String inputDiscoveryJson="file:src/test/data/3.json";
//		String inputDiscoveryJson="http://52.204.112.237:3051/activity-streams/15878";
//		String inputDiscoveryJson="http://52.204.112.237:3051/activity-streams/16003";
//		String inputDiscoveryJson="http://52.204.112.237:3051/activity-streams/16007";
//		String inputDiscoveryJson="http://52.204.112.237:3051/activity-streams/16022";
//		String inputDiscoveryJson="http://52.204.112.237:3051/activity-streams/16079";
//		String inputDiscoveryJson="http://52.204.112.237:3051/activity-streams/16214";
//		String inputDiscoveryJson="http://52.204.112.237:3051/activity-streams/17272";
//		String inputDiscoveryJson="http://52.204.112.237:3051/activity-streams/17287";
//		String inputDiscoveryJson="https://scrc.lib.ncsu.edu/sal_staging/iiif-discovery.json";
		String inputDiscoveryJson="https://mcgrattan.org/as/";
		
		String iiifManifestListCsv="target/syncdb";

//		 new InMemoryTimestampStore(iiifManifestListCsv).open();
		
		IiifManifestChangeDiscoveryDemo demo=new IiifManifestChangeDiscoveryDemo(inputDiscoveryJson, iiifManifestListCsv);
		demo.executeDiscoveryCrawling();
	}

	static class IiifManifestChangeDiscoveryDemo implements CrawlingHandler {

		private InMemoryTimestampStore timestampTracker;
		private ProcesssingAlgorithm iiifDiscovery;
		String dataset;
		
		public IiifManifestChangeDiscoveryDemo(String dataset, String iiifManifestTimestampFile) throws Exception {
			this.dataset = dataset;
			timestampTracker = new InMemoryTimestampStore(iiifManifestTimestampFile);
			iiifDiscovery = new ProcesssingAlgorithm(timestampTracker, this); 
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
		
		public void executeDiscoveryCrawling() throws Exception {
			iiifDiscovery.startProcess(dataset);
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
	
}
