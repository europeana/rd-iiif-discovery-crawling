package europeana.rnd.iiif.discovery.model;

/**
 * The types of as:Activity used in IIIF Change Discovery
 *
 */
public enum ActivityType {
	Create, Update, Delete, Move, Add, Remove, Refresh;

	public static boolean isValid(String string) {
		try {
			valueOf(string);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
