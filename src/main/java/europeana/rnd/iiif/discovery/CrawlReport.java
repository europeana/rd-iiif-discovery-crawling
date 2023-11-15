package europeana.rnd.iiif.discovery;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import europeana.rnd.iiif.discovery.model.ActivityType;

/**
 * a report of the outcomes of a crawl
 */
public class CrawlReport {
	String streamUrl;
	Instant previousCrawlTimestamp;
	Instant latestCrawledTimestamp;
	Map<ActivityType, Integer> activityCount;
	String errorMessage = null;

	/**
	 * @param streamUrl              the URL of the crawled stream
	 * @param previousCrawlTimestamp the date/time of the previous crawled (used as
	 *                               the starting point for the current crawl)
	 */
	public CrawlReport(String streamUrl, Instant previousCrawlTimestamp) {
		super();
		this.streamUrl = streamUrl;
		this.previousCrawlTimestamp = previousCrawlTimestamp;
		activityCount = new HashMap<ActivityType, Integer>();
		for (ActivityType activity : ActivityType.values()) {
			activityCount.put(activity, 0);
		}
	}

	/**
	 * @return the URL of the crawled stream
	 */
	public String getStreamUrl() {
		return streamUrl;
	}

	public void setStreamUrl(String streamUrl) {
		this.streamUrl = streamUrl;
	}

	/**
	 * @return the date/time of the previous crawled (used as the starting point for
	 *         the current crawl)
	 */
	public Instant getPreviousCrawlTimestamp() {
		return previousCrawlTimestamp;
	}

	public void setPreviousCrawlTimestamp(Instant previousCrawlTimestamp) {
		this.previousCrawlTimestamp = previousCrawlTimestamp;
	}

	/**
	 * @return the timestamp of the latest Activity processed during the crawl
	 */
	public Instant getLatestCrawledTimestamp() {
		return latestCrawledTimestamp;
	}

	public void setLatestCrawledTimestamp(Instant latestCrawledTimestamp) {
		this.latestCrawledTimestamp = latestCrawledTimestamp;
	}

	public void incrementActivity(ActivityType type) {
		activityCount.put(type, activityCount.get(type) + 1);
	}

	/**
	 * @param type the type of Activity
	 * @return the number of Activities of the given type that were processed during
	 *         the crawl
	 */
	public int getCount(ActivityType type) {
		return activityCount.get(type);
	}

	public void setCrawlFailure(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return if the crawled ran without errors. The CralingHandler should only
	 *         uptade it date of last crawl f this method returns true
	 */
	public boolean wasSuccessful() {
		return errorMessage == null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Crawl successful: " + wasSuccessful() + "\n");
		if (errorMessage != null)
			sb.append("Error: " + errorMessage);
		else {
			sb.append("Latest crawled timestamp: " + latestCrawledTimestamp + "\n");
			boolean hasProcessedActivities = false;
			for (ActivityType actType : ActivityType.values()) {
				Integer count = activityCount.get(actType);
				if (count > 0) {
					if (!hasProcessedActivities) {
						hasProcessedActivities = true;
						sb.append("Activities processed:\n");
					}
					sb.append(actType.name() + ": " + count + "\n");
				}
			}
			if (!hasProcessedActivities)
				sb.append("No activities processed. Synchronization was up-to-date.\n");
		}
		return sb.toString();
	}
}
