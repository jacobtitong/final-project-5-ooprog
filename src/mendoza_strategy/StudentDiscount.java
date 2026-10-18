public class StudentDiscount implements DiscountStrategy {
    @Override
    public double apply(double ticketSubtotal) {
        double result = ticketSubtotal * 0.90;
        result = Math.round(result * 100) / 100.0;
        return Math.max(0, result);
    }
}
