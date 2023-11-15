package europeana.rnd.iiif.discovery.demo.syncdb;

import java.time.Instant;

/**
 * The update/delete status of a resource
 *
 */
public class ResourceStatus {
	String resourceId;
	Instant timestamp;
	boolean deleted;

	public ResourceStatus(String resourceId, Instant timestamp, boolean deleted) {
		super();
		this.resourceId = resourceId;
		this.timestamp = timestamp;
		this.deleted = deleted;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

}
