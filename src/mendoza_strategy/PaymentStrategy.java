package mendoza_strategy;
import cullarin_exceptions_logic.PaymentFailedException;

public interface PaymentStrategy {
    void pay(double amount, String accountNumber) throws PaymentFailedException;
}
