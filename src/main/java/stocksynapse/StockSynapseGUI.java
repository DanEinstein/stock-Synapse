package stocksynapse;

import javax.swing.*;

import java.awt.*;

/**
 * The main GUI class for the Stock Synapse application.
 * It creates the main window and manages navigation between different panels
 * (Dashboard, Inventory, Forecasting) using a CardLayout.
 */
public class StockSynapseGUI {

    public static void createAndShowGUI(InventoryService inventoryService, ForecastingService forecastingService) {
        // --- Main Window Setup ---
        JFrame frame = new JFrame("Stock Synapse");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Define a consistent background color
        Color appBackgroundColor = new Color(235, 245, 235); // A light, pleasant green

        // --- CardLayout for Page Switching ---
        JPanel mainPanel = new JPanel(new CardLayout());

        // Create panel instances so we can link them
        DashboardPanel dashboardPanel = new DashboardPanel(inventoryService);
        InventoryPanel inventoryPanel = new InventoryPanel(inventoryService, dashboardPanel);
        ForecastingPanel forecastingPanel = new ForecastingPanel(inventoryService, forecastingService);

        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(inventoryPanel, "Inventory");
        mainPanel.add(forecastingPanel, "Forecasting");

        // --- Navigation Panel ---
        JPanel navPanel = new JPanel();
        JButton dashboardButton = new JButton("Dashboard");
        JButton inventoryButton = new JButton("Inventory");
        JButton forecastingButton = new JButton("Forecasting");

        navPanel.add(dashboardButton);
        navPanel.add(inventoryButton);
        navPanel.add(forecastingButton);

        // Apply the background color to the main containers
        frame.getContentPane().setBackground(appBackgroundColor);
        navPanel.setBackground(appBackgroundColor);

        // --- Button Actions to Switch Cards ---
        CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
        dashboardButton.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        inventoryButton.addActionListener(e -> cardLayout.show(mainPanel, "Inventory"));
        forecastingButton.addActionListener(e -> cardLayout.show(mainPanel, "Forecasting"));

        // --- Add Panels to Frame ---
        frame.getContentPane().add(navPanel, BorderLayout.NORTH);
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);

        // --- Finalize and Show ---
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }
}