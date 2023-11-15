package europeana.rnd.iiif.discovery.model;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import europeana.rnd.iiif.discovery.ValidationException;

/**
 * Data object for a as:Activity
 *
 */
public class Activity extends JsonDataObject {

	public Activity(JsonValue json) throws ValidationException {
		super(json);
	}

	protected UpdatedResource updatedResource;

	public void validateJson() throws ValidationException {
		try {
			getEndTime();
		} catch (Exception e) {
			throw new ValidationException("Invalid date/time: " + json.getString("endTime"));
		}

		if (!json.containsKey("type") || !ActivityType.isValid(json.getString("type")))
			throw new ValidationException("'type' is missing or invalid.");

		if (getTypeOfActivity() == ActivityType.Refresh) {
			if (!json.containsKey("startTime"))
				throw new ValidationException("'startTime' is missing from Refresh activity");
			try {
				getStartTime();
			} catch (Exception e) {
				throw new ValidationException("Invalid date/time: " + json.getString("startTime"));
			}
		} else {
			if (!json.containsKey("endTime"))
				throw new ValidationException("'endTime' is missing from activity");
			try {
				getEndTime();
			} catch (Exception e) {
				throw new ValidationException("Invalid date/time: " + json.getString("endTime"));
			}
		}

		if (!json.containsKey("object") || json.get("object").getValueType() != ValueType.OBJECT)
			throw new ValidationException("'object' is missing or invalid");
		JsonObject objectJson = json.getJsonObject("object");
		if (!objectJson.containsKey("id"))
			throw new ValidationException("'object' is missing the 'id'");
		if (!objectJson.containsKey("type"))
			throw new ValidationException("'object' is missing the 'type'");

		if (json.getString("type").equals("Move") && !json.containsKey("target"))
			throw new ValidationException("'target' is missing (required in Move activities)");

		if (json.getString("type").equals("Refresh") && !json.containsKey("startTime"))
			throw new ValidationException("'startTime' is missing (required in Refresh activities)");

		if (!json.getString("type").equals("Refresh") && !json.containsKey("endTime"))
			throw new ValidationException("'endTime' is missing.");
	}

	public boolean endsBefore(Instant lastHarvest) {
		return getEndTime().isBefore(lastHarvest);
	}

	public Instant getEndTime() {
		return parseXsdDate(json.getString("endTime"));
	}

	private Instant parseXsdDate(String timeString) {
		if (timeString.length() <= 21) {
			return Instant.parse(timeString);
		} else {
			OffsetDateTime instant = OffsetDateTime.parse(timeString);
			return instant.toInstant();
		}
	}

	public static boolean validateXsdDateTime(String dateTimeValue) {
		try {
			if (dateTimeValue.length() <= 21) {
				Instant.parse(dateTimeValue);
			} else {
				OffsetDateTime.parse(dateTimeValue);
			}
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	public ActivityType getTypeOfActivity() {
		return ActivityType.valueOf(json.getString("type"));
	}

	public UpdatedResource getObject() {
		try {
			if (updatedResource == null)
				updatedResource = new UpdatedResource(json.getJsonObject("object"));
			return updatedResource;
		} catch (ValidationException e) {
			throw new RuntimeException("Activity should have been validated earlier", e);
		}
	}

	public String getOriginId() {
		if (json.containsKey("origin") && json.get("origin").getValueType() == ValueType.OBJECT
				&& json.getJsonObject("origin").containsKey("id"))
			return json.getJsonObject("origin").getString("id");
		return null;
	}

	public String getTargetId() {
		if (json.containsKey("target") && json.get("target").getValueType() == ValueType.OBJECT
				&& json.getJsonObject("target").containsKey("id"))
			return json.getJsonObject("target").getString("id");
		return null;
	}

	public JsonValue getTargetJson() {
		return json.get("target");
	}

	public Instant getStartTime() {
		return parseXsdDate(json.getString("startTime"));
	}

	/**
	 * Gets the timestamp that is used for sorting the activity in the stream
	 * 
	 * @return The startTime on Refresh activities, the endTime otherwise
	 */
	public Instant getSortingTimestamp() {
		return getTypeOfActivity() == ActivityType.Refresh ? getStartTime() : getEndTime();
	}

	public String getTargetType() {
		if (json.containsKey("target") && json.get("target").getValueType() == ValueType.OBJECT
				&& json.getJsonObject("target").containsKey("type"))
			return json.getJsonObject("target").getString("type");
		return null;
	}

}
