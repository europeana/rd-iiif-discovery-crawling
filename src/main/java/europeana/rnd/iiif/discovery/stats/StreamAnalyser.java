package europeana.rnd.iiif.discovery.stats;

import java.io.IOException;

import javax.json.JsonObject;

import europeana.rnd.iiif.discovery.HttpClient;
import europeana.rnd.iiif.discovery.JavaNetHttpClient;
import europeana.rnd.iiif.discovery.ValidationException;
import europeana.rnd.iiif.discovery.model.Activity;
import europeana.rnd.iiif.discovery.model.JsonUtil;
import europeana.rnd.iiif.discovery.model.OrderedCollection;
import europeana.rnd.iiif.discovery.model.OrderedCollectionPage;

/**
 * Analyses a IIIF Change Discovery API stream. Validates the data and extracts statistics about the content of the stream 
 *
 */
public class StreamAnalyser {
	StreamStatistics stats;
	String streamUrl;
	HttpClient httpClient;

	/**
	 * Analyses a stream
	 * 
	 * @param streamUrl the stream URL to analyse
	 * @param httpClient the HTTP client inplementation to use for HTTP requests
	 * @return a report with statistics about the stream content
	 */
	public StreamStatistics processStream(String streamUrl, HttpClient httpClient) {
		this.streamUrl = streamUrl;
		this.httpClient = httpClient;
		stats = new StreamStatistics(streamUrl);
		processOrderedCollection();
		return stats;
	}

	protected void processOrderedCollection() {
		try {
			JsonObject topLevelJson = JsonUtil.readJson(streamUrl, httpClient);
			OrderedCollection orderedCollection = new OrderedCollection(topLevelJson);
			orderedCollection.validateJson();
			String nextPage = orderedCollection.getUriOfLast();
			while (nextPage != null)
				nextPage = processPage(nextPage);
		} catch (IOException e) {
			System.out.println("It was not possible to validate stream due to error: " + e.getMessage());
		} catch (ValidationException e) {
			System.out.println("It was not possible to validate stream due to a validation error: " + e.getMessage());
		}
	}

	protected String processPage(String pageUrl) throws IOException {
		System.out.println("Processing page " + pageUrl);
		JsonObject topLevelJson = JsonUtil.readJson(pageUrl, httpClient);
		OrderedCollectionPage orderedCollectionPage = null;
		try {
			orderedCollectionPage = new OrderedCollectionPage(topLevelJson);
			orderedCollectionPage.validateJson();
			for (Activity activity : orderedCollectionPage.getActivitiesInReverseOrder()) {
				try {
					activity.validateJson();
				} catch (ValidationException e) {
					stats.incrementValidationErrorOnActivity();
					System.out.println("WARN: Activity does not validate: " + e.getMessage());
				}
				stats.add(activity);
			}
		} catch (ValidationException e) {
			stats.incrementValidationErrorOnPage();
			System.out.println("WARN: Page does not validate: " + e.getMessage());
		}
		return orderedCollectionPage.getPreviousPageId();
	}

	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			System.out.println("Invalid parameters. Pleas indicate the URL of the IIIF Change Discovery API endpoint.");
			return;
		}
		StreamAnalyser streamAnalyser = new StreamAnalyser();
		StreamStatistics report = streamAnalyser.processStream(args[0], new JavaNetHttpClient());
		System.out.println();
		System.out.println(report.printReport());
	}

}
