package peco_users_menus;
import cullarin_exceptions_logic.InvalidInputException;

public abstract class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private boolean active;

    public User(int id, String name, String email, String password, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public boolean isActive() { return active; }

    public void setName(String name) throws InvalidInputException {
        if (!name.equals(name.trim()) || !name.matches("[A-Za-z0-9 ]{2,30}")) {
            throw new InvalidInputException("Name must be 2-30 letters, digits or spaces, with no space at the start or end.");
        }
        this.name = name;
    }

    public void setEmail(String email) throws InvalidInputException {
        if (!email.matches("[A-Za-z0-9._]+@[A-Za-z0-9]+\\.[A-Za-z]+")) {
            throw new InvalidInputException("Email must look like name@example.com.");
        }
        this.email = email;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public abstract User displayDashboard();
}
