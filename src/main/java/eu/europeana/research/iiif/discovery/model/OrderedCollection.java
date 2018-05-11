package eu.europeana.research.iiif.discovery.model;

import java.io.IOException;
import java.util.HashSet;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import eu.europeana.research.iiif.discovery.IiifObjectType;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;


//"@context": [
//             "http://iiif.io/api/discovery/0/context.json",
//             "https://www.w3.org/ns/activitystreams"
//           ],
//           "id": "https://example.org/activity/all-changes",
//           "type": "OrderedCollection",
//           "totalItems": 21456,
//           "first": {
//           	"id": "https://example.org/activity/page-0",
//           	"type": "OrderedCollectionPage"
//           },
//           "last": {
//           	"id": "https://example.org/activity/page-214",
//           	"type": "OrderedCollectionPage"
//           }
public class OrderedCollection extends JsonObject{
	
	ProcesssingAlgorithm processsingAlgorithm;
//	String firstPageId;
	String lastPageId;
	
	public OrderedCollection() {
		type=IiifObjectType.OrderedCollection;
	}
	
	@Override
	public void processArray(String name, JsonReader jr) throws IOException {
		if(name.equals("orderedItems")) {
			jr.beginArray();
			while(jr.hasNext()) {
				Activity act=new Activity(jr);
			}
			jr.endArray();
		}
	}
	
	@Override
	public void processField(String name, Object value) {
	}
	
	@Override
	public void processObject(String name, JsonReader jr) throws IOException {
		if(name.equals("first")) {
//			IiifResourceReference ref=new IiifResourceReference(jr);
//			firstPageId=ref.getId();
			jr.skipValue();
		} else if(name.equals("last")) {
			IiifResourceReference ref=new IiifResourceReference(jr);
			lastPageId=ref.getId();
		} else 
			jr.skipValue();
	}

	public void setProcessingAlgorithm(ProcesssingAlgorithm processsingAlgorithm) {
		this.processsingAlgorithm = processsingAlgorithm;
	}
	
	@Override
	public void finalize() throws IOException {
		processsingAlgorithm.process(lastPageId);
	}
}
