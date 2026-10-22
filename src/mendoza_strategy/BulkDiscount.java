package mendoza_strategy;

public class BulkDiscount implements DiscountStrategy {
    private double rate;

    public BulkDiscount(int seatCount) {
        if (seatCount >= 50) {
            this.rate = 0.15;
        } else if (seatCount >= 20) {
            this.rate = 0.10;
        } else {
            this.rate = 0;
        }
    }

    @Override
    public double apply(double ticketSubtotal) {
        
        double result = ticketSubtotal * (1 - rate);
        result = Math.round(result * 100) / 100.0;
        return Math.max(0, result);
    }
}
