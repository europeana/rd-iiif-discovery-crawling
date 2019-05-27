package eu.europeana.research.iiif.discovery.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class IiifResourceReference {
	protected String id;
	protected  String type;
	protected  List<IiifSeeAlsoReference> seeAlso;
	
	protected IiifResourceReference() {
		seeAlso=new ArrayList<>();
	}
	
	public IiifResourceReference(JsonReader jr) throws IOException {
		this();
		jr.beginObject();
		while(jr.peek()!=JsonToken.END_OBJECT){
			String field = jr.nextName();
			if(field.equals("type")) {
				type=jr.nextString();
			}else if(field.equals("id")) {
				id=jr.nextString();
			}else if(field.equals("seeAlso")) {
				jr.beginArray();
				while(jr.hasNext()) {
					IiifSeeAlsoReference ctx=new IiifSeeAlsoReference(jr);
					seeAlso.add(ctx);
				}
				jr.endArray();
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

	public List<IiifSeeAlsoReference> getSeeAlso() {
		return seeAlso;
	}

}
