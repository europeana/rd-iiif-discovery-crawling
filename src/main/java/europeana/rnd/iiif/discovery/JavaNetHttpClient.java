package europeana.rnd.iiif.discovery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * A simple implementation of HTTP client functionality required by the
 * processing algorith.
 * 
 * Implementation for demonstration purposes, simply based on java.net.
 * 
 */
public class JavaNetHttpClient implements HttpClient {

	/**
	 * execute a simple HTTP request for a URL
	 * 
	 * @param url the URL to request
	 * @return a string with the content of the response (in the case IIIF Change
	 *         Discovery, it should always contain a JSON document).
	 * @throws FileNotFoundException on HTTP error code 404
	 * @throws IOException           on network errors on other HTTP error codes
	 */
	public String httpGet(String url) throws FileNotFoundException, IOException {
		int tries = 0;
		while (true) {
			tries++;
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				if (conn.getResponseCode() >= 300 && conn.getResponseCode() < 400) {
					String location = conn.getHeaderField("Location");
					if (location != null)
						return httpGet(location);
				} else if (conn.getResponseCode() == 429) {
					String retryAfter = conn.getHeaderField("Retry-After");
					if (retryAfter != null) {
						try {
							Thread.sleep(Long.parseLong(retryAfter));
							return httpGet(url);
						} catch (NumberFormatException e) {
							// ignore and proceed to sleep
						}
					}
					Thread.sleep(1000);
					return httpGet(url);
				}
				return IOUtils.toString(conn.getInputStream(), "UTF-8");
			} catch (IOException ex) {
				if (tries >= 3)
					throw ex;
			} catch (InterruptedException ex) {
				return null;
			}
		}
	}
}
