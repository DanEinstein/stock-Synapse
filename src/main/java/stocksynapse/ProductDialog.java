package stocksynapse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A reusable dialog for adding or editing a Product.
 * This encapsulates the form logic, separating it from the InventoryPanel.
 */
public class ProductDialog {

    private final JTextField nameField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField quantityField = new JTextField();
    private final JTextField categoryField = new JTextField();
    private final JTextField descriptionField = new JTextField();
    private final JPanel formPanel;

    public ProductDialog() {
        this(null);
    }

    public ProductDialog(Product productToEdit) {
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add labels
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        String[] labels = { "Name:", "Price:", "Quantity:", "Category:", "Description:" };
        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i;
            formPanel.add(new JLabel(labels[i]), gbc);
        }

        // Add fields
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JComponent[] fields = { nameField, priceField, quantityField, categoryField, descriptionField };
        for (int i = 0; i < fields.length; i++) {
            gbc.gridy = i;
            formPanel.add(fields[i], gbc);
        }

        if (productToEdit != null) {
            populateFields(productToEdit);
        }
    }

    private void populateFields(Product product) {
        nameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        quantityField.setText(String.valueOf(product.getQuantity()));
        categoryField.setText(product.getCategory());
        descriptionField.setText(product.getDescription());
    }

    public int showDialog(Component parent, String title) {
        return JOptionPane.showConfirmDialog(parent, formPanel, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
    }

    public Product getProductFromFields(String id) throws NumberFormatException {
        String name = nameField.getText();
        double price = Double.parseDouble(priceField.getText());
        int quantity = Integer.parseInt(quantityField.getText());
        String category = categoryField.getText();
        String description = descriptionField.getText();

        // Basic validation
        if (name.trim().isEmpty() || priceField.getText().trim().isEmpty()
                || quantityField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Name, Price, and Quantity fields cannot be empty.");
        }
        if (price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Price and Quantity cannot be negative.");
        }

        return new Product(id, name, price, quantity, category, description);
    }
}