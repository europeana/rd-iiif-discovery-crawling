package eu.europeana.research.iiif.discovery.syncdb;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Status {
	Calendar timestamp;
	boolean deleted=false;
	public Status(Calendar timestamp) {
		super();
		this.timestamp = timestamp;
	}
	public Status(long timeInMilis) {
		super();
		this.timestamp = new GregorianCalendar();
		this.timestamp.setTimeInMillis(timeInMilis);
	}
	public Status(Calendar timestamp, boolean deleted) {
		super();
		this.timestamp = timestamp;
		this.deleted = deleted;
	}
	public Status(long timeInMilis, boolean deleted) {
		this.timestamp = new GregorianCalendar();
		this.timestamp.setTimeInMillis(timeInMilis);
		this.deleted = deleted;
	}
	public Calendar getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	
}
