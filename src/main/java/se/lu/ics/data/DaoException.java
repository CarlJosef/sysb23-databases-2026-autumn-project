package se.lu.ics.data;

/**
 * Runtime exception used to report failures in the data access layer.
 */
public class DaoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a data access exception with a message and underlying cause.
     *
     * @param message descriptive error message
     * @param cause   underlying cause of the failure
     */
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a data access exception with a descriptive message.
     *
     * @param message descriptive error message
     */
    public DaoException(String message) {
        super(message);
    }
}
