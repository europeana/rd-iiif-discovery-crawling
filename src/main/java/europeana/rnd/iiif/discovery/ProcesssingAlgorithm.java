package europeana.rnd.iiif.discovery;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;

import javax.json.JsonObject;

import europeana.rnd.iiif.discovery.model.Activity;
import europeana.rnd.iiif.discovery.model.ActivityType;
import europeana.rnd.iiif.discovery.model.JsonUtil;
import europeana.rnd.iiif.discovery.model.OrderedCollection;
import europeana.rnd.iiif.discovery.model.OrderedCollectionPage;

/**
 * Implementation of the processing algorithm specified by IIIF change
 * discovery:
 * https://iiif.io/api/discovery/1.0/#activity-streams-processing-algorithm
 *
 */
public class ProcesssingAlgorithm {

	/**
	 * Holds the current state of an ongoing crawl
	 */
	class ProcessState {
		boolean onlyDelete = false;
		Instant lastCrawl = null;
		HashSet<String> processedItems = new HashSet<String>();
		Instant latestCrawledTimestamp = null;
		CrawlReport report;

		public ProcessState(Instant lastCrawl) {
			this.lastCrawl = lastCrawl;
			report = new CrawlReport(streamUrl, lastCrawl);
		}
	}

	ActivityHandler crawler;
	HttpClient httpClient;

	String streamUrl;
	ProcessState status;

	/**
	 * @param crawler the handler for the Activities found during the crawl
	 * @throws Exception
	 */
	public ProcesssingAlgorithm(ActivityHandler crawler) {
		this.crawler = crawler;
		this.httpClient = new JavaNetHttpClient();
	}

	/**
	 * @param crawler    the handler for the Activities found during the crawl
	 * @param httpClient the HTTP client to be used for performing HTTP requests
	 *                   during the crawl
	 * @throws Exception
	 */
	public ProcesssingAlgorithm(ActivityHandler crawler, HttpClient httpClient) {
		this.crawler = crawler;
		this.httpClient = httpClient;
	}

	/**
	 * executes the processing algorithm
	 * 
	 * @param streamUrl the URL of the activity stream, which points to an
	 *                  OrderedCollection
	 * @return a report of the result of the crawl
	 * @throws Exception if some unrecoverable exception occours during the crawl
	 */
	public CrawlReport processStream(String streamUrl) throws Exception {
		this.streamUrl = streamUrl;
		status = new ProcessState(crawler.getLastCrawlTimestamp(streamUrl));

		processOrderedCollection();
		return status.report;
	}

	protected void processOrderedCollection() throws ValidationException, Exception {
		try {
			crawler.crawlStart(streamUrl);
			crawler.log("Processing collection " + streamUrl);
			JsonObject topLevelJson = JsonUtil.readJson(streamUrl, httpClient);

			OrderedCollection orderedCollection = new OrderedCollection(topLevelJson);
			orderedCollection.validateJson();
			String nextPage = orderedCollection.getUriOfLast();
			while (nextPage != null)
				nextPage = processPage(nextPage);
			status.report.setLatestCrawledTimestamp(status.latestCrawledTimestamp);
			crawler.crawlEnd(status.latestCrawledTimestamp);
		} catch (Exception e) {
			status.report.setCrawlFailure(e.getMessage());
			crawler.crawlFail(e.getMessage(), e);
		}
	}

	protected String processPage(String pageUri) throws ValidationException, IOException {
		crawler.log("Processing page " + pageUri);
		JsonObject topLevelJson = JsonUtil.readJson(pageUri, httpClient);
		OrderedCollectionPage orderedCollectionPage = new OrderedCollectionPage(topLevelJson);
		orderedCollectionPage.validateJson();
		boolean continueToNextPage = true;
		for (Activity activity : orderedCollectionPage.getActivitiesInReverseOrder()) {
			if (status.lastCrawl != null && activity.endsBefore(status.lastCrawl)) {
				continueToNextPage = false;
				crawler.log("Crawl finished - timestamp of last crawl reached.");
				break;
			}
//			System.out.println(activity.getTypeOfActivity()+" "+activity.getObject().getType());
			if (status.latestCrawledTimestamp == null)
				status.latestCrawledTimestamp = activity.getTypeOfActivity() == ActivityType.Refresh
						? activity.getStartTime()
						: activity.getEndTime();
			if (activity.getTypeOfActivity() == ActivityType.Refresh) {
				if (status.lastCrawl == null) {
					continueToNextPage = false;
				} else {
					status.onlyDelete = true;
				}
				status.report.incrementActivity(activity.getTypeOfActivity());
			} else if (status.processedItems.contains(activity.getObject().getId())) {
//				System.out.println("Already processed - "+ activity.getType()+" on "+activity.getObject().getId());
				continue;
			} else if (!crawler.isSupportedResourceType(activity.getObject().getType())) {
				status.processedItems.add(activity.getObject().getId());
				// do nothing
			} else if (activity.getTypeOfActivity() == ActivityType.Remove) {
				String originId = activity.getOriginId();
				if (originId == null || originId.equals(streamUrl)) {
					status.report.incrementActivity(activity.getTypeOfActivity());
					crawler.processActivity(activity);
					status.processedItems.add(activity.getObject().getId());
				}
			} else if (activity.getTypeOfActivity() == ActivityType.Delete) {
				status.report.incrementActivity(activity.getTypeOfActivity());
				crawler.processActivity(activity);
				status.processedItems.add(activity.getObject().getId());
			} else if (status.onlyDelete) {
				// do nothing
			} else if (activity.getTypeOfActivity() == ActivityType.Add) {
				String targetId = activity.getTargetId();
				if (targetId == null || targetId.equals(streamUrl)) {
					status.report.incrementActivity(activity.getTypeOfActivity());
					crawler.processActivity(activity);
					status.processedItems.add(activity.getObject().getId());
				}
			} else if (activity.getTypeOfActivity() == ActivityType.Create
					|| activity.getTypeOfActivity() == ActivityType.Update) {
				status.report.incrementActivity(activity.getTypeOfActivity());
				crawler.processActivity(activity);
				status.processedItems.add(activity.getObject().getId());
			} else if (activity.getTypeOfActivity() == ActivityType.Move) {
				if (activity.getObject().getType().equals(activity.getTargetType())) {
					status.report.incrementActivity(activity.getTypeOfActivity());
					crawler.processActivity(activity);
					status.processedItems.add(activity.getObject().getId());
					status.processedItems.add(activity.getTargetId());
				}
			}
		}
		return continueToNextPage ? orderedCollectionPage.getPreviousPageId() : null;
	}

}
