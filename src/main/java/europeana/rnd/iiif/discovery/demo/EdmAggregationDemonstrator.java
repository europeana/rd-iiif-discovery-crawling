package europeana.rnd.iiif.discovery.demo;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import europeana.rnd.iiif.discovery.CrawlReport;
import europeana.rnd.iiif.discovery.JavaNetHttpClient;
import europeana.rnd.iiif.discovery.ProcesssingAlgorithm;
import europeana.rnd.iiif.discovery.demo.syncdb.InMemoryTimestampStore;
import europeana.rnd.iiif.discovery.demo.syncdb.TimestampTracker.Deleted;
import europeana.rnd.iiif.discovery.model.SeeAlsoProfile;

/**
 * Executes a crawl of a IIIF Change Discovery API stream, simulating the use
 * case of an aggregator that wants to keep a local copy of all the EDM metadata
 * present in the "seeAlso" of the IIIF Manifests included in the stream.
 * 
 * This is the starting point to the demonstrator. It uses the implementation of
 * the IIIF Change Discovery processing algorithm in package
 * europeana.rnd.iiif.discovery to crawl a stream, and stores the EDM metadata
 * and the synchronisation timestamps in the filesystem.
 * 
 */
public class EdmAggregationDemonstrator {

	/**
	 * The command line parameters
	 *
	 */
	static class Parameters {
		@Option(name = "-u", usage = "URL of the IIIF Change Discovery API stream", metaVar = "<URL>", required = true)
		protected String streamUrl;

		@Option(name = "-d", usage = "Path to the directory for storing the timestamps", metaVar = "<DIR>", required = true)
		protected String timestampsDbFolder;

		@Option(name = "-m", usage = "Path to the directory for storing the EDM metadata", metaVar = "<DIR>", required = true)
		protected String edmRepositoryFolder;

		@Option(name = "-l", usage = "Path to a log file (optional)", required = false, metaVar = "<FILE>")
		protected String logFilePath;
	}

	static class IiifManifestChangeDiscoveryDemo {
		InMemoryTimestampStore timestampTracker;
		ProcesssingAlgorithm iiifDiscovery;
		String streamUrl;

		public IiifManifestChangeDiscoveryDemo(String streamUrl, String iiifManifestTimestampFile, String edmRepoFolder,
				String logFilePath) throws Exception {
			this.streamUrl = streamUrl;
			JavaNetHttpClient httpClient = new JavaNetHttpClient();
			timestampTracker = new InMemoryTimestampStore(iiifManifestTimestampFile);
			MetadataHarvester seeAlsoHarvester = new MetadataHarvester(new File(edmRepoFolder), httpClient,
					SeeAlsoProfile.EDM);
			ManifestsActivityHandler activityHandler = new ManifestsActivityHandler(timestampTracker, seeAlsoHarvester);
			if (logFilePath != null)
				activityHandler.setLogFile(new File(logFilePath));
			iiifDiscovery = new ProcesssingAlgorithm(activityHandler, httpClient);
		}

		public void executeDiscoveryCrawling() throws Exception {
			System.out.println("Processing IIIF Change Discovery API stream...");
			CrawlReport report = iiifDiscovery.processStream(streamUrl);
			timestampTracker.close();
			System.out.println("Crawl finished.");
			System.out.println(report.toString());
			int countWithDeleted = timestampTracker.getDatasetSize(streamUrl, Deleted.INCLUDE);
			int countWithoutDeleted = timestampTracker.getDatasetSize(streamUrl, Deleted.EXCLUDE);
			System.out.println("Repository status after the crawl:");
			System.out.println(" - Total resources in local repository: " + countWithoutDeleted);
			System.out.println(" - Deleted resources in local repository: " + (countWithDeleted - countWithoutDeleted));
		}

	}

	public static void main(String[] args) throws Exception {
		Parameters params = new Parameters();
		CmdLineParser parser = new CmdLineParser(params);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException clEx) {
			System.out.println("Invalid parameters: " + clEx.getMessage());
			System.out.println();
			System.out.println("Usage information:");
			parser.printUsage(System.out);
			return;
		}

		IiifManifestChangeDiscoveryDemo demo = new IiifManifestChangeDiscoveryDemo(params.streamUrl,
				params.timestampsDbFolder, params.edmRepositoryFolder, params.logFilePath);
		demo.executeDiscoveryCrawling();
	}
}
