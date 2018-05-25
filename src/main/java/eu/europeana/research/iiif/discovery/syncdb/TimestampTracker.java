package eu.europeana.research.iiif.discovery.syncdb;

import java.util.Calendar;

public interface TimestampTracker {
	public enum Deleted {INCLUDE, EXCLUDE};
	
	public void open() throws Exception;
	public void commit()  throws Exception;
	public void close() throws Exception;
	
	public void setDatasetTimestamp(String dataset, Calendar timestamp);
	public void setObjectTimestamp(String dataset, String object, Calendar timestamp);
	public void setObjectTimestamp(String dataset, String object, Calendar timestamp, boolean deleted);

	public Calendar getDatasetStatus(String dataset);
	public Status getObjectStatus(String dataset, String object);
	public Calendar getObjectTimestamp(String dataset, String object);
	
	public Iterable<String> getIterableOfObjects(String dataset, Deleted deletedOption);
	public int getDatasetSize(String dataset, Deleted deletedOption);
}
