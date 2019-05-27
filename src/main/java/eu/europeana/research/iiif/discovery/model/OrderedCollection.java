package eu.europeana.research.iiif.discovery.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
public class OrderedCollection extends JsonObject {
	
	ProcesssingAlgorithm processsingAlgorithm;
	protected String firstPageId;
	protected String lastPageId;
	protected List<IiifSeeAlsoReference> seeAlso;
	protected Integer totalItems;
	protected List<IiifResourceReference> partOf;
	
	public OrderedCollection() {
		type=IiifObjectType.OrderedCollection;
		seeAlso=new ArrayList<>();
		partOf=new ArrayList<>();
	}
	
	@Override
	public void processArray(String name, JsonReader jr) throws IOException {
		if(name.equals("orderedItems")) {
			jr.beginArray();
			while(jr.hasNext()) {
				Activity act=new Activity(jr);
			}
			jr.endArray();
		}else if(name.equals("seeAlso")) {
			jr.beginArray();
			while(jr.hasNext()) {
				IiifSeeAlsoReference ctx=new IiifSeeAlsoReference(jr);
				seeAlso.add(ctx);
			}
			jr.endArray();
		}else if(name.equals("partOf")) {
			jr.beginArray();
			while(jr.hasNext()) {
				IiifResourceReference ctx=new IiifResourceReference(jr);
				partOf.add(ctx);
			}
			jr.endArray();
		}
	}
	
	
	@Override
	public void processObject(String name, JsonReader jr) throws IOException {
//		if(name.equals("first")) {
//			IiifResourceReference ref=new IiifResourceReference(jr);
//			firstPageId=ref.getId();
//		} else 
		if(name.equals("last")) {
			IiifResourceReference ref=new IiifResourceReference(jr);
			lastPageId=ref.getId();
		} else if(name.equals("first")) {
				IiifResourceReference ref=new IiifResourceReference(jr);
				firstPageId=ref.getId();
		} else if(name.equals("totalItems")) {
			processField(name, jr);
			totalItems=jr.nextInt();
		} else if(name.equals("seeAlso")) {
			IiifSeeAlsoReference ref=new IiifSeeAlsoReference(jr);
			seeAlso.add(ref);
		} else if(name.equals("partOf")) {
			IiifResourceReference ref=new IiifResourceReference(jr);
			partOf.add(ref);
		} else 
			processField(name, jr);
	}

	public List<IiifSeeAlsoReference> getSeeAlso() {
		return seeAlso;
	}

	public void setProcessingAlgorithm(ProcesssingAlgorithm processsingAlgorithm) {
		this.processsingAlgorithm = processsingAlgorithm;
	}
	
	@Override
	public void finalize() throws IOException {
		processsingAlgorithm.process(lastPageId);
	}
	
	public static List<IiifSeeAlsoReference> getSeeAlso(String jsonStringOfAnOrderedCollection) throws IOException {
		List<IiifSeeAlsoReference> seeAlso=new ArrayList<>();
		JsonReader jr=new JsonReader(new StringReader(jsonStringOfAnOrderedCollection));
		jr.beginObject();
		while(jr.peek()!=JsonToken.END_OBJECT){
			String field = jr.nextName();
			if(field.equals("seeAlso")) {
				jr.beginArray();
				while(jr.hasNext()) {
					IiifSeeAlsoReference ctx=new IiifSeeAlsoReference(jr);
					seeAlso.add(ctx);
				}
				jr.endArray();
			}else 
				jr.skipValue();
		}
		jr.close();
		return seeAlso;
	}

}
