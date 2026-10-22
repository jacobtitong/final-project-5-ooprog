package titong_model_data;

public class Discount {
    private String code;
    private String type;
    private double value;
    private boolean stackable;
    private String expiryDate;
    private boolean active;

    public Discount(String code, String type, double value, boolean stackable,
            String expiryDate, boolean active) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.stackable = stackable;
        this.expiryDate = expiryDate;
        this.active = active;
    }

    public String getCode() { return code; }
    public String getType() { return type; }
    public double getValue() { return value; }
    public boolean isStackable() { return stackable; }
    public String getExpiryDate() { return expiryDate; }
    public boolean isActive() { return active; }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String toFileLine() {
        return code + ";" + type + ";" + value + ";" + stackable
                + ";" + expiryDate + ";" + active;
    }
}
