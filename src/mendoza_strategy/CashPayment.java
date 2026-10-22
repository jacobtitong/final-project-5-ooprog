package mendoza_strategy;
import cullarin_exceptions_logic.PaymentFailedException;

public class CashPayment implements PaymentStrategy {
    @Override
    public void pay(double amount, String accountNumber) throws PaymentFailedException {
        if (amount <= 0) {
            throw new PaymentFailedException("Amount must be greater than 0.");
        }
    }
}
