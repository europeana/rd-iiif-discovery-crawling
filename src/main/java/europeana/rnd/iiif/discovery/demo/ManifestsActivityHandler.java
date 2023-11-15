package europeana.rnd.iiif.discovery.demo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import europeana.rnd.iiif.discovery.ActivityHandler;
import europeana.rnd.iiif.discovery.ValidationException;
import europeana.rnd.iiif.discovery.demo.syncdb.TimestampTracker;
import europeana.rnd.iiif.discovery.model.Activity;
import europeana.rnd.iiif.discovery.model.UpdatedResource;

/**
 * An Activity handler implementation for demonstration purposes. It processes
 * only activities on Manifests, and aggregates the EDM metadata.
 * 
 * It uses simple database to keep track of the last modification of all
 * Manifests, and the deleted ones. It aggregates EDM metadata from the
 * "seeAlso" elements of the Manifests and stores them in a simple file based
 * repository.
 * 
 */
public class ManifestsActivityHandler implements ActivityHandler {

	/**
	 * A database for persistently storing the Manifests' timestamps across several
	 * crawls
	 */
	TimestampTracker timestampTracker;

	/**
	 * For downloading the EDM metadata records
	 */
	MetadataHarvester seeAlsoHarvester;

	/**
	 * The URL of the stream currently being crawled
	 */
	String currentCrawlStreamUri;

	/**
	 * Writes logging messages in this file. If null, will write to System.out
	 */
	File logFile;

	public ManifestsActivityHandler(TimestampTracker timestampTracker, MetadataHarvester seeAlsoHarvester)
			throws Exception {
		this.timestampTracker = timestampTracker;
		this.seeAlsoHarvester = seeAlsoHarvester;
	}

	/**
	 * Indicated to the ProcessingAlgorith that only activities on Manifests should
	 * be processed
	 */
	@Override
	public boolean isSupportedResourceType(String type) {
		return type.equals("Manifest");
	}

	/**
	 * Processes the Activities obtained by the ProcessingAlgorithm
	 */
	@Override
	public void processActivity(Activity activity) throws IOException {
		try {
			log(activity.getTypeOfActivity() + " on " + activity.getObject().getType() + " at "
					+ activity.getObject().getId());
			switch (activity.getTypeOfActivity()) {
			case Add:
			case Create:
			case Update:
				timestampTracker.setObjectTimestamp(currentCrawlStreamUri, activity.getObject().getId(),
						activity.getEndTime());
				break;
			case Delete:
			case Remove:
				timestampTracker.setObjectTimestamp(currentCrawlStreamUri, activity.getObject().getId(),
						activity.getEndTime(), true);
				break;
			case Move:
				UpdatedResource targetResource = new UpdatedResource(activity.getTargetJson());
				timestampTracker.setObjectTimestamp(currentCrawlStreamUri, activity.getObject().getId(),
						activity.getEndTime(), true);
				timestampTracker.setObjectTimestamp(currentCrawlStreamUri, targetResource.getId(),
						activity.getEndTime());
			case Refresh:
				throw new IllegalStateException("A \"Refresh\" activity should have been handled earlier");
			}
		} catch (ValidationException e) {
			throw new RuntimeException("Activity should have been validated earlier", e);
		}
	}

	@Override
	public Instant getLastCrawlTimestamp(String streamUri) {
		return timestampTracker.getDatasetTimestamp(streamUri);
	}

	@Override
	public void crawlStart(String streamUri) {
		this.currentCrawlStreamUri = streamUri;
	}

	@Override
	public void crawlFail(String errorMessage, Exception cause) throws Exception {
		log(errorMessage, cause);
		timestampTracker.rollback();
		timestampTracker.setDatasetLastError(currentCrawlStreamUri, Instant.now());
		timestampTracker.commit();
		currentCrawlStreamUri = null;
	}

	/**
	 * When a crawl is successful, it processes all the updated/deleted manifests.
	 * Downloads the EDM metadata in all the created/updated Manifests, and deleted
	 * the EDM metadata of the deleted Manifests
	 */
	@Override
	public void crawlEnd(Instant latestTimestamp) throws Exception {
		// Stream crawl was successful. Now crawl the manifests and metadata in seeAlso.
		for (europeana.rnd.iiif.discovery.demo.syncdb.ResourceStatus manifestStatus : timestampTracker
				.getIterableOfObjects(currentCrawlStreamUri, getLastCrawlTimestamp(currentCrawlStreamUri), null)) {
			try {
				if (manifestStatus.isDeleted()) {
					seeAlsoHarvester.deleteSeeAlsoOfManifest(manifestStatus.getResourceId());
				} else {
					seeAlsoHarvester.harvestFromManifest(manifestStatus.getResourceId());
				}
			} catch (IOException | ValidationException e) {
				log("Error in " + manifestStatus.getResourceId() + " - " + e.getMessage(), e);
			}
		}
		timestampTracker.setDatasetTimestamp(currentCrawlStreamUri, latestTimestamp);
		timestampTracker.commit();
		currentCrawlStreamUri = null;
	}

	@Override
	public void log(String message) {
		String logMessage = Instant.now().toString() + " - " + message + "\n";
		if (logFile == null)
			System.out.print(logMessage);
		else
			try {
				FileUtils.write(logFile, logMessage, StandardCharsets.UTF_8, true);
			} catch (IOException e) {
				System.err.println("Error writing to log file: ");
				e.printStackTrace();
			}
	}

	@Override
	public void log(String message, Exception ex) {
		log(message + "\n" + ExceptionUtils.getStackTrace(ex));
	}

	/**
	 * @param logFile sets the location of the log file
	 */
	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}

}