public class DebitCardPayment implements PaymentStrategy {
    @Override
    public void pay(double amount, String accountNumber) throws PaymentFailedException {
        if (amount <= 0) {
            throw new PaymentFailedException("Amount must be greater than 0.");
        }
        if (!accountNumber.matches("[0-9]{16}")) {
            throw new PaymentFailedException("Card number must be exactly 16 digits.");
        }
    }
}
