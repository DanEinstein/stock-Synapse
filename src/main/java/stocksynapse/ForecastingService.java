package stocksynapse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import org.json.JSONObject;

/**
 * A service to interact with the Google Gemini API for sales forecasting.
 */
public class ForecastingService {

    private final String apiKey;
    private final HttpClient httpClient;
    private static final String API_URL_FORMAT = "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s";
    private static final String MODEL_NAME = "gemini-pro";

    public ForecastingService(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key for Gemini API cannot be null or empty.");
        }
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Generates a sales forecast for a given product using the Gemini API.
     * Includes a retry mechanism for transient API errors like rate limiting.
     *
     * @param product The product to be forecasted.
     * @return A string containing the AI-generated forecast and advice.
     * @throws ForecastingException if the API call fails after all retries.
     */
    public String generateForecast(Product product) throws ForecastingException {
        String prompt = createPromptForProduct(product);
        final int MAX_RETRIES = 3;
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            // Create the JSON payload
            JSONObject content = new JSONObject();
            content.put("text", prompt);

            JSONObject payload = new JSONObject()
                    .put("contents", new org.json.JSONArray()
                            .put(new JSONObject().put("parts", new org.json.JSONArray().put(content))));

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(API_URL_FORMAT, MODEL_NAME, apiKey)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            try {
                // Send the request and get the response
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 429) {
                    // Handle rate limiting specifically with a retry
                    if (attempt < MAX_RETRIES - 1) {
                        long retryAfter = 30L; // Default wait time
                        System.out.println("Quota exceeded. Retrying in " + retryAfter + " seconds...");
                        try {
                            Thread.sleep(retryAfter * 1000);
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } else if (response.statusCode() != 200) {
                    // For any other non-200 status code, throw an exception immediately
                    throw new ForecastingException(
                            "Gemini API returned an error. Status: " + response.statusCode() + "\nResponse: "
                                    + response.body());
                } else {
                    // Success! Parse and return the response.
                    return parseResponse(response.body());
                }
            } catch (IOException | InterruptedException e) {
                // Network errors can be transient, so we'll allow a retry
                if (attempt == MAX_RETRIES - 1) {
                    throw new ForecastingException(
                            "Network error while communicating with Gemini API after multiple retries.", e);
                }
                System.out.println("Network error. Retrying...");
            }
            attempt++;
        }
        throw new ForecastingException("Failed to generate forecast after all retries.");
    }

    private String createPromptForProduct(Product product) {
        return String.format(
                "You are an expert inventory management analyst for a retail business. " +
                        "Analyze the following product and provide a brief sales forecast and restocking recommendation. "
                        +
                        "Be concise and provide actionable advice. Assume a standard retail environment. " +
                        "Format your response clearly with headings for 'Forecast' and 'Recommendation'.\n\n" +
                        "Product Details:\n" +
                        "- Name: %s\n" +
                        "- Category: %s\n" +
                        "- Price: $%.2f\n" +
                        "- Current Quantity in Stock: %d\n\n" +
                        "Your Analysis:",
                product.getName(), product.getCategory(), product.getPrice(), product.getQuantity());
    }

    private String parseResponse(String responseBody) throws ForecastingException {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Defensive parsing to avoid crashes on unexpected API responses
            if (jsonResponse.has("candidates") && !jsonResponse.getJSONArray("candidates").isEmpty()) {
                JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);
                if (candidate.has("content") && candidate.getJSONObject("content").has("parts")
                        && !candidate.getJSONObject("content").getJSONArray("parts").isEmpty()) {
                    return candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                }
            }

            // If the expected structure isn't found, throw a specific exception
            throw new ForecastingException(
                    "Could not find forecast text in Gemini API response. Raw response: " + responseBody);
        } catch (org.json.JSONException e) {
            throw new ForecastingException("Error parsing JSON from Gemini API.", e);
        }
    }
}
