package stocksynapse;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Manages the inventory data and business logic.
 * This class acts as a service layer, separating data operations
 * from the user interface.
 */
public class InventoryService {

    private static final String DB_PROPERTIES_FILE = "local.properties";
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    public InventoryService() {
        // Load database configuration on startup.
        try {
            Properties props = new Properties();
            // Ensure the properties file is loaded from the project root
            try (FileInputStream fis = new FileInputStream(DB_PROPERTIES_FILE)) {
                props.load(fis);
                dbUrl = props.getProperty("DB_URL");
                dbUser = props.getProperty("DB_USER");
                dbPassword = props.getProperty("DB_PASSWORD");
            }
        } catch (IOException e) {
            System.err.println("FATAL: Could not load database configuration from " + DB_PROPERTIES_FILE);
            e.printStackTrace();
            // In a real application, you would show an error dialog and possibly exit.
            dbUrl = null; // Ensure connection attempts will fail cleanly.
        }
    }

    /**
     * Adds a new product to the inventory.
     * This method is responsible for generating a unique ID for the product
     * before adding it to the inventory.
     * 
     * @param name        The name of the product.
     * @param price       The price of the product.
     * @param quantity    The quantity of the product.
     * @param category    The category of the product.
     * @param description A description of the product.
     */
    public void addProduct(String name, double price, int quantity, String category, String description) {
        String sql = "INSERT INTO products(id, name, price, quantity, category, description) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, name);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, quantity);
            pstmt.setString(5, category);
            pstmt.setString(6, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add product to the database.", e);
        }
    }

    /**
     * Updates an existing product in the inventory.
     * 
     * @param id          The ID of the product to update.
     * @param name        The new name.
     * @param price       The new price.
     * @param quantity    The new quantity.
     * @param category    The new category.
     * @param description The new description.
     * @return true if the product was found and updated, false otherwise.
     */
    public boolean updateProduct(String id, String name, double price, int quantity, String category,
            String description) {
        String sql = "UPDATE products SET name = ?, price = ?, quantity = ?, category = ?, description = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, category);
            pstmt.setString(5, description);
            pstmt.setString(6, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product in the database.", e);
        }
    }

    /**
     * Retrieves a single product by its ID.
     * 
     * @param id The ID of the product to find.
     * @return The Product object, or null if not found.
     */
    public Product getProductById(String id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getString("category"),
                            rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve product from the database.", e);
        }
        return null;
    }

    /**
     * @return A copy of the current inventory list to prevent direct modification.
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name"; // Default sort by name
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("category"),
                        rs.getString("description")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all products from the database.", e);
        }
        return products;
    }

    private Connection getConnection() throws SQLException {
        if (dbUrl == null) {
            throw new SQLException("Database configuration is missing or failed to load.");
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}