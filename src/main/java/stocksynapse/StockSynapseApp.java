package stocksynapse;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.JOptionPane;

public class StockSynapseApp {

    private static String getApiKey() {
        // 1. Try to get from environment variable (works well for production/CI)
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey;
        }

        // 2. Fallback to reading from local.properties (great for local development)
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("local.properties")) {
            props.load(fis);
            apiKey = props.getProperty("GEMINI_API_KEY");
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                return apiKey;
            }
        } catch (IOException e) {
            // File not found or is unreadable, which is fine, we'll show the error below.
        }

        return null;
    }

    public static void main(String[] args) {
        // Set the modern FlatLaf look and feel for the entire application
        FlatLightLaf.setup();

        // The InventoryService will be shared across different panels.
        InventoryService inventoryService = new InventoryService();

        // Read the API key using our new robust method
        String geminiApiKey = getApiKey();

        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            // Show a user-friendly error dialog and exit if the key is missing.
            JOptionPane.showMessageDialog(null,
                    "FATAL ERROR: 'GEMINI_API_KEY' is not set.\n\n" +
                            "Please set it in your system environment or in a 'local.properties' file.",
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Exit the application
        }
        ForecastingService forecastingService = new ForecastingService(geminiApiKey);

        // Run the GUI creation on the Event Dispatch Thread for thread safety.
        SwingUtilities.invokeLater(() -> StockSynapseGUI.createAndShowGUI(inventoryService, forecastingService));
    }
}