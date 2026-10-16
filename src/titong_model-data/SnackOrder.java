public class SnackOrder {
    private int id;
    private int bookingId;
    private int snackId;
    private int quantity;
    private double unitPrice;

    public SnackOrder(int id, int bookingId, int snackId, int quantity, double unitPrice) {
        this.id = id;
        this.bookingId = bookingId;
        this.snackId = snackId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getId() { return id; }
    public int getBookingId() { return bookingId; }
    public int getSnackId() { return snackId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }

    public String toFileLine() {
        return id + ";" + bookingId + ";" + snackId + ";" + quantity
                + ";" + unitPrice;
    }
}
