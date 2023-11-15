package europeana.rnd.iiif.discovery.model;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue.ValueType;

/**
 * Data object for a as:OrderedCollectionPage
 *
 */
import europeana.rnd.iiif.discovery.ValidationException;

public class OrderedCollectionPage extends JsonDataObject {

	List<Activity> activitiesInReverseOrder;

	public OrderedCollectionPage(JsonObject json) throws ValidationException {
		super(json);
	}

	public void validateJson() throws ValidationException {
		if (!json.containsKey("type") || !json.getString("type").equals("OrderedCollectionPage"))
			throw new ValidationException("'type' is missing or invalid. Expected: 'OrderedCollectionPage'");

		for (Activity activity : getActivitiesInReverseOrder()) {
			activity.validateJson();
		}
	}

	public List<Activity> getActivitiesInReverseOrder() throws ValidationException {
		if (activitiesInReverseOrder == null) {
			JsonArray jsonArray = json.getJsonArray("orderedItems");
			activitiesInReverseOrder = new ArrayList<Activity>(jsonArray.size());
			for (int i = jsonArray.size() - 1; i >= 0; i--) {
				Activity activity = new Activity(jsonArray.get(i));
				activitiesInReverseOrder.add(activity);
			}
		}
		return activitiesInReverseOrder;
	}

	public String getPreviousPageId() {
		if (json.containsKey("prev") && json.get("prev").getValueType() == ValueType.OBJECT) {
			JsonObject prev = json.getJsonObject("prev");
			if (prev.containsKey("id"))
				return prev.getString("id");
		}
		return null;
	}

}
