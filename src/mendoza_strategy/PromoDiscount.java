package mendoza_strategy;

public class PromoDiscount implements DiscountStrategy {
    private String type;
    private double value;

    public PromoDiscount(String type, double value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public double apply(double ticketSubtotal) {
        double result = ticketSubtotal;

        if (type.equals("PERCENT")) {
            result = ticketSubtotal * (1 - value / 100);
        } else if (type.equals("FIXED")) {
            result = ticketSubtotal - value;
        }
        result = Math.round(result * 100) / 100.0;
        return Math.max(0, result);
    }
}
