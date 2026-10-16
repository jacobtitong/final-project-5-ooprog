public class Discount {
    public Discount(String code, String type, double value, boolean stackable,
            String expiryDate, boolean active) {
    }

    public String getCode() { return ""; }
    public String getType() { return ""; }
    public double getValue() { return 0.0; }
    public boolean isStackable() { return false; }
    public String getExpiryDate() { return ""; }
    public boolean isActive() { return false; }

    public void setType(String type) {
    }

    public void setValue(double value) {
    }

    public void setStackable(boolean stackable) {
    }

    public void setExpiryDate(String expiryDate) {
    }

    public void setActive(boolean active) {
    }

    public String toFileLine() { return ""; }
}
