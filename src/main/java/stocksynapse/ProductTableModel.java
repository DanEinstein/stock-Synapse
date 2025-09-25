package stocksynapse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom TableModel to connect the JTable with the List of Products.
 * This is a crucial part of the Model-View-Controller (MVC) pattern in Swing.
 * It allows the table to display data from the inventory without being tightly
 * coupled to the data source.
 */
public class ProductTableModel extends AbstractTableModel {

    private final List<Product> products;
    private final String[] columnNames = { "ID", "Name", "Price", "Quantity", "Category" };

    public ProductTableModel(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    @Override
    public int getRowCount() {
        return products.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2)
            return Double.class; // Price
        if (columnIndex == 3)
            return Integer.class; // Quantity
        return String.class; // ID, Name, Category
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product product = products.get(rowIndex);
        switch (columnIndex) {
            case 0:
                // Display only a portion of the long UUID for readability
                return product.getId().substring(0, 8) + "...";
            case 1:
                return product.getName();
            case 2:
                return product.getPrice(); // Return the actual double
            case 3:
                return product.getQuantity();
            case 4:
                return product.getCategory();
            default:
                return null;
        }
    }

    /**
     * Refreshes the table data with an updated list of products.
     * Notifies the JTable that the data has changed so it can repaint itself.
     * 
     * @param newProducts The new list of products to display.
     */
    public void refresh(List<Product> newProducts) {
        this.products.clear();
        this.products.addAll(newProducts);
        // This is a crucial step that tells the JTable to update its view
        fireTableDataChanged();
    }

    /**
     * Returns the full Product object for a given row.
     * 
     * @param rowIndex The row index.
     * @return The Product at that row.
     */
    public Product getProductAt(int rowIndex) {
        return products.get(rowIndex);
    }

    /**
     * Updates a product at a specific row.
     * 
     * @param rowIndex The row index to update.
     * @param product  The new product data.
     */
    public void updateProduct(int rowIndex, Product product) {
        products.set(rowIndex, product);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * Removes a product from a specific row.
     * 
     * @param rowIndex The row index to remove.
     */
    public void removeProduct(int rowIndex) {
        products.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
}