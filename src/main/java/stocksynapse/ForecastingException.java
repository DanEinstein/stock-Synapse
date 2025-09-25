package stocksynapse;

/**
 * Custom exception for handling errors related to the ForecastingService.
 * This allows for more specific error handling than a generic Exception.
 */
public class ForecastingException extends Exception {
    public ForecastingException(String message) {
        super(message);
    }

    public ForecastingException(String message, Throwable cause) {
        super(message, cause);
    }
}