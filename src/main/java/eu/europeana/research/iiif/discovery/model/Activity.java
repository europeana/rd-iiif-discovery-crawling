package eu.europeana.research.iiif.discovery.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import eu.europeana.research.iiif.discovery.IiifObjectType;

//	  "type": "Create",
//	  "object": {
//	    "id": "https://example.org/iiif/1/manifest",
//	    "type": "Manifest"
//	  },
//	  "endTime": "2017-09-20T00:00:00Z"
public class Activity extends JsonObject {
	ActivityType subtype;
	Calendar endTime;
	String objectId;
	
	public Activity() {
		type=IiifObjectType.Activity;
	}
	
	public Activity(JsonReader jr) throws IOException {
		jr.beginObject();
		while(jr.hasNext()){
			String field = jr.nextName();
			if(field.equals("type")) {
				subtype=ActivityType.valueOf(jr.nextString());
			}else if(field.equals("endTime")) {
				if(jr.peek()!=JsonToken.NULL) {
					String timeStr = jr.nextString();
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						endTime=new GregorianCalendar();
						endTime.setTime(sdf.parse(timeStr));
					} catch (ParseException e) {
						throw new IOException("On date: "+timeStr, e);
					}
				} else 
					jr.skipValue();
			}else if(field.equals("object")) {
				IiifResourceReference ref=new IiifResourceReference(jr);
				if(ref.isManifest())
					objectId=ref.getId();
			} else {
				jr.skipValue();
			}
		}
		jr.endObject();
	}

	@Override
	public String toString() {
		return subtype.toString();
	}

	public ActivityType getSubtype() {
		return subtype;
	}

	public void setSubtype(ActivityType subtype) {
		this.subtype = subtype;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
}
