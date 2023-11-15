package europeana.rnd.iiif.discovery;

/**
 * Thrown when a JSON response is not compliant with the IIIF Change Discovery
 * API specification
 *
 */
public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ValidationException() {
		super();
	}

	public ValidationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ValidationException(String msg) {
		super(msg);
	}

}
