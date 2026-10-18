public interface PaymentStrategy {
    void pay(double amount, String accountNumber) throws PaymentFailedException;
}
