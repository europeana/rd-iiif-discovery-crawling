package europeana.rnd.iiif.discovery.demo.syncdb;

import java.time.Instant;

/**
 * The interface for databases that keep track of the update/delete timestamps
 * of resources that are organised in datasets.
 *
 */
public interface TimestampTracker {

	public enum Deleted {
		INCLUDE, EXCLUDE
	};

	public void open() throws Exception;

	public void commit() throws Exception;

	public void close() throws Exception;

	public void setDatasetTimestamp(String dataset, Instant timestamp);

	public void setDatasetLastError(String dataset, Instant timestamp);

	public void setObjectTimestamp(String dataset, String object, Instant timestamp);

	public void setObjectTimestamp(String dataset, String object, Instant timestamp, boolean deleted);

	public Instant getDatasetTimestamp(String dataset);

	public Instant getDatasetLastError(String dataset);

	public ResourceStatus getObjectStatus(String dataset, String object);

	public Instant getObjectTimestamp(String dataset, String object);

	public Iterable<ResourceStatus> getIterableOfObjects(String dataset, Instant since, Instant until);

	public int getDatasetSize(String dataset, Deleted deletedOption);

	public void clear(String dataset);

	public void rollback() throws Exception;

}
