public class PWDDiscount implements DiscountStrategy {
    private static final double VAT_RATE = 0.12;

    @Override
    public double apply(double ticketSubtotal) {
        double result = (ticketSubtotal / (1 + VAT_RATE)) * 0.80;
        result = Math.round(result * 100) / 100.0;
        return Math.max(0, result);
    }
}
