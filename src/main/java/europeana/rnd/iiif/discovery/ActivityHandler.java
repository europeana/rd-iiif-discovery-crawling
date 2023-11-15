package europeana.rnd.iiif.discovery;

import java.io.IOException;
import java.time.Instant;

import europeana.rnd.iiif.discovery.model.Activity;

/**
 * The interface for implementation by clients to process the Activities
 * collected by the ProcessingAlgorithm
 *
 */
public interface ActivityHandler {

	/**
	 * Called by the ProcessingAlgorith to check if the CrawlingHandler processes
	 * activities on a certain type of resource.
	 * 
	 * Some CrawlingHandlers may process only IIIF Collections or Manifests, or may
	 * process both.
	 * 
	 * @param type the type of the resource that is object of an activity
	 * @return true if the handler processes this type of activity, false otherwise.
	 */
	public boolean isSupportedResourceType(String type);

	/**
	 * Process an activity collected by the ProcessingAlgorithm
	 * 
	 * @param activity
	 * @throws IOException
	 */
	public void processActivity(Activity activity) throws IOException;

	/**
	 * Receive a logging message from the ProcessingAlgorithm
	 * 
	 * @param message
	 */
	public void log(String message);

	/**
	 * Receive a logging error from the ProcessingAlgorithm
	 * 
	 * @param message
	 * @param ex
	 */
	public void log(String message, Exception ex);

	/**
	 * Indicates to the ProcessingAlgorithm which was the date/time from the
	 * previous crawl
	 * 
	 * @param streamUri the URL of the IIIF Change Discovery stream
	 * @return date/time from the previous crawl, or null if no crawl was performed
	 *         earlier.
	 */
	public Instant getLastCrawlTimestamp(String streamUri);

	/**
	 * Signals to the handler that a crawl is about to start, allowing the
	 * CrawlingHandler to do any necessary initialisations
	 * 
	 * @param streamUri the URL of the IIIF Change Discovery stream that is about to
	 *                  be crawled
	 */
	public void crawlStart(String streamUri);

	/**
	 * Signals to the handler that the crawl ended successfully
	 * 
	 * @param latestTimestamp the timestamp of the latest Activity collected during
	 *                        the crawl. This timestamp should be used as the
	 *                        starting point for the next crawl.
	 * @throws Exception
	 */
	public void crawlEnd(Instant latestTimestamp) throws Exception;

	/**
	 * Signals to the handler that the crawl ended with an error
	 * 
	 * @param errorMessage
	 * @param cause
	 * @throws Exception
	 */
	public void crawlFail(String errorMessage, Exception cause) throws Exception;

}
