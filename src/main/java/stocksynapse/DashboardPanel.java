package stocksynapse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final InventoryService inventoryService;
    private JLabel productCountLabel;

    public DashboardPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Title Panel ---
        JLabel titleLabel = new JLabel("Stock Synapse Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        add(titleLabel, BorderLayout.NORTH);

        // --- Stats Panel ---
        JPanel statsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Live Statistics"));

        productCountLabel = new JLabel();
        productCountLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        statsPanel.add(productCountLabel);

        add(statsPanel, BorderLayout.CENTER);

        // --- Control Panel ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Refresh Stats");
        refreshButton.addActionListener(e -> updateStats());
        controlPanel.add(refreshButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Initial update
        updateStats();
    }

    public void updateStats() {
        int count = inventoryService.getAllProducts().size();
        productCountLabel.setText("Total Unique Products in Inventory: " + count);
    }
}
