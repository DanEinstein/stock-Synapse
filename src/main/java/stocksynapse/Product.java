package stocksynapse;

public class Product {
    private final String id;
    private final String name;
    private final double price;
    private final int quantity;
    private final String category;
    private final String description;

    public Product(String id, String name, double price, int quantity, String category, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f, quantity=%d, category='%s', description='%s'}",
                id, name, price, quantity, category, description);
    }
}
