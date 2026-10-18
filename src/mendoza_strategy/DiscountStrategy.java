public interface DiscountStrategy {
    double apply(double ticketSubtotal);   // returns the new subtotal; never below 0
}
