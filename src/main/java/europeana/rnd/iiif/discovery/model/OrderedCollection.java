package europeana.rnd.iiif.discovery.model;

import javax.json.JsonObject;
import javax.json.JsonValue.ValueType;

import org.apache.commons.lang3.StringUtils;

import europeana.rnd.iiif.discovery.ValidationException;

/**
 * Data object for a as:OrderedCollection
 *
 */
public class OrderedCollection extends JsonDataObject {

	public OrderedCollection(JsonObject json) throws ValidationException {
		super(json);
	}

	public void validateJson() throws ValidationException {
		if (!json.containsKey("type") || !json.getString("type").equals("OrderedCollection"))
			throw new ValidationException("'type' is missing or invalid. Expected: 'OrderedCollection'");
		if (!json.containsKey("last"))
			throw new ValidationException("'last' is missing");
		if (json.get("last").getValueType() != ValueType.OBJECT)
			throw new ValidationException("'last' must be a JSON object");
		JsonObject lastObj = json.getJsonObject("last");
		if (!lastObj.containsKey("id"))
			throw new ValidationException("'last' is missing the 'id'");
		if (!StringUtils.startsWithIgnoreCase(lastObj.getString("id"), "http"))
			throw new ValidationException("in 'last': 'id' must be a HTTP(S) URI");
		if (!lastObj.containsKey("type") || !lastObj.getString("type").equals("OrderedCollectionPage"))
			throw new ValidationException("in 'last': 'type' is missing or invalid. Expected: 'OrderedCollectionPage'");
	}

	public String getUriOfLast() {
		return json.getJsonObject("last").getString("id");
	}

//	ProcesssingAlgorithm processsingAlgorithm;
//	protected String firstPageId;
//	protected String lastPageId;
//	protected List<IiifSeeAlsoReference> seeAlso;
//	protected Integer totalItems;
//	protected List<IiifResourceReference> partOf;
//	
//	public OrderedCollection() {
//		type=IiifObjectType.OrderedCollection;
//		seeAlso=new ArrayList<>();
//		partOf=new ArrayList<>();
//	}
//	
//	@Override
//	public void processArray(String name, JsonReader jr) throws IOException {
//		if(name.equals("orderedItems")) {
//			jr.beginArray();
//			while(jr.hasNext()) {
//				Activity act=new Activity(jr);
//			}
//			jr.endArray();
//		}else if(name.equals("seeAlso")) {
//			jr.beginArray();
//			while(jr.hasNext()) {
//				IiifSeeAlsoReference ctx=new IiifSeeAlsoReference(jr);
//				seeAlso.add(ctx);
//			}
//			jr.endArray();
//		}else if(name.equals("partOf")) {
//			jr.beginArray();
//			while(jr.hasNext()) {
//				IiifResourceReference ctx=new IiifResourceReference(jr);
//				partOf.add(ctx);
//			}
//			jr.endArray();
//		}
//	}
//	
//	
//	@Override
//	public void processObject(String name, JsonReader jr) throws IOException {
////		if(name.equals("first")) {
////			IiifResourceReference ref=new IiifResourceReference(jr);
////			firstPageId=ref.getId();
////		} else 
//		if(name.equals("last")) {
//			IiifResourceReference ref=new IiifResourceReference(jr);
//			lastPageId=ref.getId();
//		} else if(name.equals("first")) {
//				IiifResourceReference ref=new IiifResourceReference(jr);
//				firstPageId=ref.getId();
//		} else if(name.equals("totalItems")) {
//			processField(name, jr);
//			totalItems=jr.nextInt();
//		} else if(name.equals("seeAlso")) {
//			IiifSeeAlsoReference ref=new IiifSeeAlsoReference(jr);
//			seeAlso.add(ref);
//		} else if(name.equals("partOf")) {
//			IiifResourceReference ref=new IiifResourceReference(jr);
//			partOf.add(ref);
//		} else 
//			processField(name, jr);
//	}
//
//	public List<IiifSeeAlsoReference> getSeeAlso() {
//		return seeAlso;
//	}
//
//	public void setProcessingAlgorithm(ProcesssingAlgorithm processsingAlgorithm) {
//		this.processsingAlgorithm = processsingAlgorithm;
//	}
//	
//	@Override
//	public void finalize() throws IOException {
//		processsingAlgorithm.process(lastPageId);
//	}
//	
//	public static List<IiifSeeAlsoReference> getSeeAlso(String jsonStringOfAnOrderedCollection) throws IOException {
//		List<IiifSeeAlsoReference> seeAlso=new ArrayList<>();
//		JsonReader jr=new JsonReader(new StringReader(jsonStringOfAnOrderedCollection));
//		jr.beginObject();
//		while(jr.peek()!=JsonToken.END_OBJECT){
//			String field = jr.nextName();
//			if(field.equals("seeAlso")) {
//				jr.beginArray();
//				while(jr.hasNext()) {
//					IiifSeeAlsoReference ctx=new IiifSeeAlsoReference(jr);
//					seeAlso.add(ctx);
//				}
//				jr.endArray();
//			}else 
//				jr.skipValue();
//		}
//		jr.close();
//		return seeAlso;
//	}

}
