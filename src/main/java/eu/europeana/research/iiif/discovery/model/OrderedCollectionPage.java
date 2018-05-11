package eu.europeana.research.iiif.discovery.model;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import eu.europeana.research.iiif.discovery.IiifObjectType;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;


//	  "@context": [
//	    "http://iiif.io/api/discovery/0/context.json",
//	    "https://www.w3.org/ns/activitystreams"
//	  ],
//	  "id": "https://example.org/activity/page-1",
//	  "type": "OrderedCollectionPage",
//	  "partOf": {
//	    "id": "https://example.org/activity/all-changes",
//	    "type": "OrderedCollection"
//	  },
//	  "prev": {
//	    "id": "https://example.org/activity/page-0",
//	    "type": "OrderedCollectionPage"
//	  },
//	  "next": {
//	    "id": "https://example.org/activity/page-2",
//	    "type": "OrderedCollectionPage"
//	  },
//	  "orderedItems": [
//	    {
//	      "type": "Update",
//	      "object": {
//	        "id": "https://example.org/iiif/9/manifest",
//	        "type": "Manifest"
//	      },
//	      "endTime": "2018-03-10T10:00:00Z"
//	    },
//	    {
//	      "type": "Update",
//	      "object": {
//	        "id": "https://example.org/iiif/2/manifest",
//	        "type": "Manifest"
//	      },
//	      "endTime": "2018-03-11T16:30:00Z"
//	    }
//	  ]
public class OrderedCollectionPage extends JsonObject{

	ProcesssingAlgorithm processsingAlgorithm;
	String prevPageId;
	private boolean continueToNextActivity=true;
	
	public OrderedCollectionPage() {
		type=IiifObjectType.OrderedCollectionPage;
	}
	@Override
	public void processArray(String name, JsonReader jr) throws IOException {
		if(name.equals("orderedItems")) {
			ArrayList<Activity> items=new ArrayList<>(1000);
			jr.beginArray();
			while(jr.hasNext()) {
				Activity act=new Activity(jr);				
				items.add(act);
			}
			jr.endArray();
			for(int i=items.size()-1 ; i>=0 ; i--) {
				continueToNextActivity = processsingAlgorithm.processActivity(items.get(i));
				if(!continueToNextActivity)
					break;
			}
		}
	}
	
	@Override
	public void processField(String name, Object value) {
	}
	
	@Override
	public void processObject(String name, JsonReader jr) throws IOException {
		if(name.equals("prev")) {
			jr.beginObject();
			while(jr.peek()!=JsonToken.END_OBJECT){
				String field = jr.nextName();
//				if(field.equals("type") && jr.nextString().equals(""OrderedCollectionPage)) {
//					type=Type.valueOf();
//				}else 
				if(field.equals("id")) {
					prevPageId=jr.nextString();
				} else {
					jr.skipValue();
				}
			}
			jr.endObject();
		} else 
			jr.skipValue();
	}

	public void setProcessingAlgorithm(ProcesssingAlgorithm processsingAlgorithm) {
		this.processsingAlgorithm = processsingAlgorithm;
	}
	
	@Override
	public void finalize() throws IOException {
		if(continueToNextActivity && prevPageId!=null)
			processsingAlgorithm.process(prevPageId);
	}
}
