package stocksynapse;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class ForecastingPanel extends JPanel {
    private final InventoryService inventoryService;
    private final ForecastingService forecastingService;

    private JComboBox<ProductWrapper> productSelector;
    private JTextArea forecastResultArea;
    private JButton generateButton;

    public ForecastingPanel(InventoryService inventoryService, ForecastingService forecastingService) {
        this.inventoryService = inventoryService;
        this.forecastingService = forecastingService;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 248, 255)); // A light, airy blue

        // --- Title Panel ---
        JLabel titleLabel = new JLabel("Sales & Inventory Forecasting", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204)); // A nice blue color
        titleLabel.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // --- Main Content Panel ---
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new TitledBorder("AI-Powered Forecast Generator"));

        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Select Product to Forecast:"));
        productSelector = new JComboBox<>();
        productSelector.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(productSelector);

        generateButton = new JButton("Generate Forecast");
        inputPanel.add(generateButton);

        contentPanel.add(inputPanel, BorderLayout.NORTH);

        // --- Result Display Area ---
        forecastResultArea = new JTextArea(
                "Select a product and click 'Generate Forecast' to see the AI-powered analysis.");
        forecastResultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        forecastResultArea.setEditable(false);
        forecastResultArea.setLineWrap(true);
        forecastResultArea.setWrapStyleWord(true);
        forecastResultArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(forecastResultArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // --- Action Listeners ---
        generateButton.addActionListener(e -> generateForecast());
        // Refresh product list when the panel becomes visible
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                refreshProductList();
            }

            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
            }

            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }

    private void refreshProductList() {
        productSelector.removeAllItems();
        List<Product> products = inventoryService.getAllProducts();
        if (products.isEmpty()) {
            productSelector.setEnabled(false);
            generateButton.setEnabled(false);
        } else {
            for (Product p : products) {
                productSelector.addItem(new ProductWrapper(p));
            }
            productSelector.setEnabled(true);
            generateButton.setEnabled(true);
        }
    }

    private void generateForecast() {
        ProductWrapper selectedWrapper = (ProductWrapper) productSelector.getSelectedItem();
        if (selectedWrapper == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first.", "No Product Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        forecastResultArea.setText("Generating forecast... Please wait.");
        generateButton.setEnabled(false);

        // Use SwingWorker to perform network I/O off the Event Dispatch Thread
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return forecastingService.generateForecast(selectedWrapper.getProduct());
            }

            @Override
            protected void done() {
                try {
                    forecastResultArea.setText(get());
                } catch (java.util.concurrent.ExecutionException e) {
                    // Unwrap the actual exception from doInBackground
                    Throwable cause = e.getCause();
                    if (cause instanceof ForecastingException) {
                        forecastResultArea.setText("Forecasting Error: " + cause.getMessage());
                    } else {
                        forecastResultArea.setText("An unexpected error occurred: " + cause.getMessage());
                    }
                    cause.printStackTrace(); // Log the full stack trace to the console
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    forecastResultArea.setText("Forecasting interrupted: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    generateButton.setEnabled(true);
                }
            }
        }.execute();
    }

    // Wrapper class to display product names in JComboBox but hold the full object
    private static class ProductWrapper {
        private final Product product;

        public ProductWrapper(Product product) {
            this.product = product;
        }

        public Product getProduct() {
            return product;
        }

        @Override
        public String toString() {
            return product.getName();
        }
    }
}
