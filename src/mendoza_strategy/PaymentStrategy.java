// Step 0 stub: same signature as the final interface, only the comment changes at Step 1
public interface PaymentStrategy {
    void pay(double amount, String accountNumber) throws PaymentFailedException;
}
