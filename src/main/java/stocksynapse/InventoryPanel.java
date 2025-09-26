package stocksynapse;

import javax.swing.border.CompoundBorder;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import javax.swing.table.TableRowSorter;
import java.util.UUID;
import java.util.ArrayList;

public class InventoryPanel extends JPanel {
    private final InventoryService inventoryService;
    private final ProductTableModel tableModel;
    private final JTable inventoryTable;
    private final DashboardPanel dashboardPanel; // Reference to update dashboard

    public InventoryPanel(InventoryService inventoryService, DashboardPanel dashboardPanel) {
        this.inventoryService = inventoryService;
        this.dashboardPanel = dashboardPanel;
        this.tableModel = new ProductTableModel(new ArrayList<>()); // Start with an empty model

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Define a modern color palette ---
        Color backgroundGreen = new Color(236, 248, 241); // Soft mint green
        Color accentBlue = new Color(70, 130, 180); // Steel Blue
        Color darkText = new Color(40, 40, 40);
        Color tableHeaderBg = new Color(210, 230, 218);
        Color tableGrid = new Color(200, 220, 210);
        Color tableAlternateRow = new Color(247, 252, 249);

        setBackground(backgroundGreen);

        // --- Title ---
        JLabel titleLabel = new JLabel("Inventory Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(darkText);
        titleLabel.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // --- Inventory Table ---
        inventoryTable = new JTable(tableModel);
        inventoryTable.setFillsViewportHeight(true);
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inventoryTable.setRowHeight(30);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow only one row to be selected
        inventoryTable.setGridColor(tableGrid);
        inventoryTable.setIntercellSpacing(new Dimension(0, 1)); // Use grid lines for horizontal separation only
        inventoryTable.setShowVerticalLines(false);

        // Style the table header
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        inventoryTable.getTableHeader().setBackground(tableHeaderBg);
        inventoryTable.getTableHeader().setForeground(darkText);
        inventoryTable.getTableHeader().setBorder(new CompoundBorder(
                inventoryTable.getTableHeader().getBorder(),
                new EmptyBorder(5, 5, 5, 5)));

        // Enable sorting and filtering
        TableRowSorter<ProductTableModel> sorter = new TableRowSorter<>(tableModel);
        inventoryTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(tableGrid));
        add(scrollPane, BorderLayout.CENTER);

        // --- Control Buttons Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundGreen);
        JButton addButton = new JButton("Add New Product");
        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton refreshButton = new JButton("Refresh Table");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Style the buttons
        for (Component comp : buttonPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setFont(new Font("Segoe UI", Font.BOLD, 12));
                button.setBackground(accentBlue);
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false); // Remove the focus border for a cleaner look
            }
        }

        // --- Action Listeners ---
        addButton.addActionListener(e -> openAddProductDialog());
        editButton.addActionListener(e -> openEditProductDialog());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        refreshButton.addActionListener(e -> refreshTable());

        // Initial data load
        refreshTable();
    }

    private void openAddProductDialog() {
        ProductDialog dialog = new ProductDialog();
        int result = dialog.showDialog(this, "Add New Product");

        if (result == JOptionPane.OK_OPTION) {
            try {
                final Product newProduct = dialog.getProductFromFields(UUID.randomUUID().toString());
                // Use SwingWorker to perform file I/O off the EDT
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        inventoryService.addProduct(newProduct.getName(), newProduct.getPrice(),
                                newProduct.getQuantity(),
                                newProduct.getCategory(), newProduct.getDescription());
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get(); // Check for exceptions from doInBackground
                            refreshTable();
                            dashboardPanel.updateStats();
                            JOptionPane.showMessageDialog(InventoryPanel.this, "Product added successfully!", "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(InventoryPanel.this,
                                    "Failed to add product: " + ex.getCause().getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            } catch (IllegalArgumentException ex) {
                // Catches NumberFormatException as well
                JOptionPane.showMessageDialog(this,
                        "Invalid input: " + ex.getMessage(),
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openEditProductDialog() {
        int selectedViewRow = inventoryTable.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "No Product Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert view index to model index in case of sorting/filtering
        int modelRow = inventoryTable.convertRowIndexToModel(selectedViewRow);
        Product productToEdit = tableModel.getProductAt(modelRow);

        ProductDialog dialog = new ProductDialog(productToEdit);
        int result = dialog.showDialog(this, "Edit Product");

        if (result == JOptionPane.OK_OPTION) {
            try {
                final Product updatedProduct = dialog.getProductFromFields(productToEdit.getId());
                // Use SwingWorker to perform file I/O off the EDT
                new SwingWorker<Product, Void>() {
                    @Override
                    protected Product doInBackground() throws Exception {
                        inventoryService.updateProduct(updatedProduct.getId(), updatedProduct.getName(),
                                updatedProduct.getPrice(),
                                updatedProduct.getQuantity(), updatedProduct.getCategory(),
                                updatedProduct.getDescription());
                        return inventoryService.getProductById(productToEdit.getId());
                    }

                    @Override
                    protected void done() {
                        try {
                            tableModel.updateProduct(modelRow, get());
                            dashboardPanel.updateStats();
                            JOptionPane.showMessageDialog(InventoryPanel.this, "Product updated successfully!",
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(InventoryPanel.this,
                                    "Failed to update product: " + ex.getCause().getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            } catch (IllegalArgumentException ex) {
                // Catches NumberFormatException as well
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedProduct() {
        int selectedViewRow = inventoryTable.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "No Product Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = inventoryTable.convertRowIndexToModel(selectedViewRow);
        Product productToDelete = tableModel.getProductAt(modelRow);

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the product: " + productToDelete.getName() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            // Use SwingWorker to perform file I/O off the EDT
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    inventoryService.deleteProduct(productToDelete.getId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions from doInBackground
                        tableModel.removeProduct(modelRow);
                        dashboardPanel.updateStats();
                        JOptionPane.showMessageDialog(InventoryPanel.this, "Product deleted successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(InventoryPanel.this,
                                "Failed to delete product: " + ex.getCause().getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    public void refreshTable() {
        // Get the latest data from the service and update the table model
        tableModel.refresh(inventoryService.getAllProducts());
    }
}
