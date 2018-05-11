package eu.europeana.research.iiif.discovery.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import eu.europeana.research.iiif.discovery.IiifObjectType;

public class JsonObject {
	String id;
	IiifObjectType type;
	HashMap<String, Object> fields=new HashMap<>();
	public JsonObject(String id) {
		this.id = id;
	}
	public JsonObject(IiifObjectType type) {
		super();
		this.type = type;
	}
	public JsonObject() {
		// TODO Auto-generated constructor stub
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public IiifObjectType getType() {
		return type;
	}
	public void setType(IiifObjectType type) {
		this.type = type;
	}
	public JsonObject toTypedInstance() {
		if(type==null)
			return this;
		JsonObject inst=type.newInstanceOfType(id);
		if(this.getClass().equals(inst.getClass()))
			return this;
		for(Entry<String, Object> e: fields.entrySet()) 
			inst.processField(e.getKey(), e.getValue());
		return inst;
	}
	public void processField(String name, Object value) {
		fields.put(name, value);
	}
	public void processArray(String name, JsonReader reader) throws IOException {
		throw new UnsupportedOperationException("only subclasses implement this method");
	}
	public void processObject(String name, JsonReader reader)  throws IOException {
		throw new UnsupportedOperationException("only subclasses implement this method");
	}
	public void finalize()  throws IOException {}
}
