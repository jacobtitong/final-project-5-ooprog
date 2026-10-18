// Step 0 stub: signatures only, filled in at Step 7
public class RegisteredUser extends User {
    public RegisteredUser(int id, String name, String email, String password, boolean active,
            String birthdate, String savedPaymentType, String savedPaymentNumber) {
        super(id, name, email, password, active);
    }

    public String getBirthdate() { return ""; }
    public String getSavedPaymentType() { return ""; }
    public String getSavedPaymentNumber() { return ""; }

    public void setBirthdate(String birthdate) throws InvalidInputException {
    }

    public void setSavedPaymentType(String savedPaymentType) throws InvalidInputException {
    }

    public void setSavedPaymentNumber(String savedPaymentNumber) throws InvalidInputException {
    }

    @Override
    public User displayDashboard() {
        return null; // stub: filled in Step 7
    }
}
