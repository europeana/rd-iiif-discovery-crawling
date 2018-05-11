package eu.europeana.research.iiif.discovery.demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import eu.europeana.research.iiif.discovery.CrawlingHandler;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.model.Activity;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;

public class ScriptIiifManifestChangeDiscoveryDemo {

	
	public static void main(String[] args) throws Exception {
//		String inputDiscoveryJson="file:src/test/data/iiif-discovery-oclc-15878.json";
		String inputDiscoveryJson="file:src/test/data/iiif-discovery-ncsu.json";
//		String inputDiscoveryJson="http://...";
		
		String iiifManifestListCsv="target";
		
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
			    java.io.Reader reader = null;
			    try {
			        reader = new java.io.InputStreamReader((java.io.InputStream) new URL(url).getContent());
			        StringBuilder content = new StringBuilder();
			        char[] buf = new char[1024];
			        for (int n = reader.read(buf); n > -1; n = reader.read(buf))
			            content.append(buf, 0, n);
			        return content.toString();
			    } finally {
			        if (reader != null) try {
			            reader.close();
			        } catch (Throwable t) {
			        }
			    }
		}

		@Override
		public void processManifest(Activity activityOnManifest) {
		}
		
		public void executeDiscoveryCrawling() throws Exception {
			iiifDiscovery.startProcess(dataset);			
		}
		
	}
	
}
