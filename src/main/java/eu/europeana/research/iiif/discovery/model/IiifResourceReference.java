package eu.europeana.research.iiif.discovery.model;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class IiifResourceReference {
	String id;
	String type;
	

	public IiifResourceReference(JsonReader jr) throws IOException {
		jr.beginObject();
		while(jr.peek()!=JsonToken.END_OBJECT){
			String field = jr.nextName();
			if(field.equals("type")) {
				type=jr.nextString();
			}else if(field.equals("id")) {
				id=jr.nextString();
			} else {
				jr.skipValue();
			}
		}
		jr.endObject();
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}


	public boolean isManifest() {
		return type!=null && (type.equals("Manifest") || type.equals("sc:Manifest"));
	}
	
	
}
