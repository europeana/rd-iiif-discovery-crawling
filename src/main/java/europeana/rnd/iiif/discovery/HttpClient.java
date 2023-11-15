package europeana.rnd.iiif.discovery;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * An interface to avoid dependency on any particular HTTP library.
 *
 */
public interface HttpClient {

	/**
	 * execute a simple HTTP request for a URL
	 * 
	 * @param url the URL to request
	 * @return a string with the content of the response (in the case IIIF Change
	 *         Discovery, it should always contain a JSON document).
	 * @throws FileNotFoundException on HTTP error code 404
	 * @throws IOException           on network errors on other HTTP error codes
	 */
	public String httpGet(String url) throws FileNotFoundException, IOException;

}
