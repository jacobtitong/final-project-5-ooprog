// Step 0 stub: signatures only, filled in at Step 7
public class PromoDiscount implements DiscountStrategy {
    public PromoDiscount(String type, double value) {
    }

    @Override
    public double apply(double ticketSubtotal) {
        return ticketSubtotal; // stub: no discount until Step 7
    }
}
