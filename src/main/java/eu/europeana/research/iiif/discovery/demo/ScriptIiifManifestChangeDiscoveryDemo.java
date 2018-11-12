package eu.europeana.research.iiif.discovery.demo;

import java.io.FileInputStream;
import java.io.UTFDataFormatException;
import java.net.URLConnection;

import org.apache.commons.io.input.BOMInputStream;

import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
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
	
	
	static class IiifManifestChangeDiscoveryDemo {
		InMemoryTimestampStore timestampTracker;
		ProcesssingAlgorithm iiifDiscovery;
		String dataset;
		
		public IiifManifestChangeDiscoveryDemo(String dataset, String iiifManifestTimestampFile) throws Exception {
			this.dataset=dataset;
			timestampTracker = new InMemoryTimestampStore(iiifManifestTimestampFile);
			iiifDiscovery = new ProcesssingAlgorithm(timestampTracker, new TimestampCrawlingHandler()); 
		}

		public void executeDiscoveryCrawling() throws Exception {
			iiifDiscovery.startProcess(dataset, true);
		}

		
		
	}
}
