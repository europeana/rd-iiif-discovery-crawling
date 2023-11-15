package europeana.rnd.iiif.discovery.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import europeana.rnd.iiif.discovery.HttpClient;

/**
 * Utility class for working with JSON data
 */
public class JsonUtil {

	/**
	 * Obtain a JSON record from a URL and parses it
	 * 
	 * @param url        the URL of the JSON record
	 * @param httpClient the HttpClient implementation to use for HTTP comunication
	 * @return the parsed JSON
	 * @throws IOException on network communication error or JSON parsing error
	 */
	public static JsonObject readJson(String url, HttpClient httpClient) throws IOException {
		InputStream inStream = null;
		Reader reader;
		if (url.startsWith("file:")) {
			inStream = new FileInputStream(url.substring("file:".length()));
			reader = new InputStreamReader(inStream, "UTF-8");
		} else {
			String urlContent = httpClient.httpGet(url);
			reader = new StringReader(urlContent);
		}

		JsonReader jsonReader = Json.createReader(reader);
		JsonObject topLevelJson = jsonReader.readObject();
		jsonReader.close();
		reader.close();
		if (inStream != null)
			inStream.close();
		return topLevelJson;
	}
}
