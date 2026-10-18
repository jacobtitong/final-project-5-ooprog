// Step 0 stub: signatures only, filled in at Step 1
public abstract class User {
    public User(int id, String name, String email, String password, boolean active) {
    }

    public int getId() { return 0; }
    public String getName() { return ""; }
    public String getEmail() { return ""; }
    public String getPassword() { return ""; }
    public boolean isActive() { return false; }

    public void setName(String name) throws InvalidInputException {
    }

    public void setEmail(String email) throws InvalidInputException {
    }

    public void setActive(boolean active) {
    }

    public abstract User displayDashboard();
}
