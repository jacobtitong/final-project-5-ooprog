public class Snack {
    private int id;
    private String name;
    private double price;
    private int stock;
    private boolean active;

    public Snack(int id, String name, double price, int stock, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isActive() { return active; }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String toFileLine() {
        return id + ";" + name + ";" + price + ";" + stock
                + ";" + active;
    }
}
