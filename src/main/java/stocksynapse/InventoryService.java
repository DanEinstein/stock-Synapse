package stocksynapse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Manages the inventory data and business logic.
 * This class acts as a service layer, separating data operations
 * from the user interface.
 */
public class InventoryService {

    // A list to store our products. This acts as our in-memory "database".
    private final List<Product> inventory = new ArrayList<>();
    private static final String INVENTORY_FILE = "inventory.json";

    public InventoryService() {
        // Load existing inventory from file on startup.
        try {
            loadInventory();
        } catch (IOException e) {
            // In a real app, you might show a warning dialog to the user.
            System.err.println("Could not load inventory file: " + e.getMessage());
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
        // Generate a unique ID for the new product within the service
        String id = UUID.randomUUID().toString();
        Product newProduct = new Product(id, name, price, quantity, category, description);
        inventory.add(newProduct);
        try {
            saveInventory();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save new product to inventory.", e);
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
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getId().equals(id)) {
                Product updatedProduct = new Product(id, name, price, quantity, category, description);
                // Replace the product at the same index to preserve order
                inventory.set(i, updatedProduct);
                try {
                    saveInventory();
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to save updated product to inventory.", e);
                }
            }
        }
        return false; // Product not found
    }

    /**
     * Deletes a product from the inventory by its ID.
     * 
     * @param id The ID of the product to delete.
     * @return true if the product was found and deleted, false otherwise.
     */
    public boolean deleteProduct(String id) {
        boolean removed = inventory.removeIf(product -> product.getId().equals(id));
        if (removed) {
            try {
                saveInventory();
            } catch (IOException e) {
                throw new RuntimeException("Failed to save inventory after deleting product.", e);
            }
        }
        return removed;
    }

    /**
     * Retrieves a single product by its ID.
     * 
     * @param id The ID of the product to find.
     * @return The Product object, or null if not found.
     */
    public Product getProductById(String id) {
        return inventory.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return A copy of the current inventory list to prevent direct modification.
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(inventory); // Return a copy
    }

    /**
     * Saves the current inventory list to a JSON file.
     */
    private void saveInventory() throws IOException {
        JSONArray jsonArray = new JSONArray();
        for (Product product : inventory) {
            JSONObject productJson = new JSONObject();
            productJson.put("id", product.getId());
            productJson.put("name", product.getName());
            productJson.put("price", product.getPrice());
            productJson.put("quantity", product.getQuantity());
            productJson.put("category", product.getCategory());
            productJson.put("description", product.getDescription());
            jsonArray.put(productJson);
        }

        try (FileWriter file = new FileWriter(INVENTORY_FILE)) {
            file.write(jsonArray.toString(4)); // Use indentation for readability
        }
        // Let IOException propagate to be handled by the caller (UI)
    }

    /**
     * Loads the inventory from a JSON file if it exists.
     */
    private void loadInventory() throws IOException {
        File file = new File(INVENTORY_FILE);
        if (!file.exists()) {
            return; // No file to load, start with an empty inventory.
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(INVENTORY_FILE)));
            JSONArray jsonArray = new JSONArray(content);
            inventory.clear(); // Clear existing list before loading
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject productJson = jsonArray.getJSONObject(i);
                Product product = new Product(
                        productJson.getString("id"),
                        productJson.getString("name"),
                        productJson.getDouble("price"),
                        productJson.getInt("quantity"),
                        productJson.optString("category", ""),
                        productJson.optString("description", ""));
                inventory.add(product);
            }
        } catch (org.json.JSONException e) {
            // Wrap JSONException in an IOException for consistent error handling
            throw new IOException("Error parsing inventory file: " + e.getMessage(), e);
        }
    }
}