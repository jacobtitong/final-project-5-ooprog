package mendoza_strategy;
import cullarin_exceptions_logic.PaymentFailedException;

public class EWalletPayment implements PaymentStrategy {
    @Override
    public void pay(double amount, String accountNumber) throws PaymentFailedException {
        if (amount <= 0) {
            throw new PaymentFailedException("Amount must be greater than 0.");
        }
        if (!accountNumber.matches("09[0-9]{9}")) {
            throw new PaymentFailedException("E-wallet number must be 11 digits starting with 09.");
        }
    }
}
