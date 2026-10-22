package peco_users_menus;
import cullarin_exceptions_logic.InvalidInputException;
import titong_model_data.DataStore;
import titong_model_data.Snack;

public class SnackManager extends User {
    public SnackManager(int id, String name, String email, String password, boolean active) {
        super(id, name, email, password, active);
    }

    @Override
    public User displayDashboard() {
        boolean loggedOut = false;
        while (!loggedOut) {
            System.out.print("\n=== SNACK MANAGER MENU ===");
            System.out.print("\n1 - View all snacks\n2 - Add a snack\n3 - Edit a snack name\n4 - Update a snack price\n5 - Update a snack stock\n6 - Discontinue a snack\n7 - Low-stock list\n8 - Set low-stock threshold\n9 - Logout\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 9);

            switch (option) {
                case 1:
                    viewSnacks();
                    break;
                case 2:
                    addSnack();
                    break;
                case 3:
                    editName();
                    break;
                case 4:
                    updatePrice();
                    break;
                case 5:
                    updateStock();
                    break;
                case 6:
                    discontinueSnack();
                    break;
                case 7:
                    viewLowStock();
                    break;
                case 8:
                    setThreshold();
                    break;
                case 9:
                    loggedOut = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-9.");
            }
        }

        return new Guest();
    }

    private void viewSnacks() {
        DataStore ds = DataStore.getInstance();

        if (ds.getSnacks().size() == 0) {
            System.out.println("No snacks yet.");
        } else {
            printSnackHeader();
            for (Snack s : ds.getSnacks()) {
                printSnackRow(s);
            }
        }
    }

    private void printSnackHeader() {
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-4s | %-20s | %-12s | %-6s | %-12s%n", "ID", "Name", "Price", "Stock", "Status");
        System.out.println("------------------------------------------------------------");
    }

    private void printSnackRow(Snack s) {
        String status = s.isActive() ? "Active" : "Discontinued";
        System.out.printf("%-4d | %-20.20s | Php %-8.2f | %-6d | %-12s%n", s.getId(), s.getName(), s.getPrice(), s.getStock(), status);
    }

    private Snack chooseSnack() throws InvalidInputException {
        DataStore ds = DataStore.getInstance();

        viewSnacks();
        int id = InputHelper.getValidInt("Snack ID: ", 1, 999999);
        Snack s = ds.findSnackById(id);
        if (s == null) {
            throw new InvalidInputException("Snack ID not found.");
        }
        return s;
    }

    private void addSnack() {
        DataStore ds = DataStore.getInstance();

        String name = InputHelper.getValidName("Snack name (2-30 letters, digits, spaces): ", 2, 30);
        double price = InputHelper.getValidDouble("Price (1.00-500.00): ", 1, 500);
        int stock = InputHelper.getValidInt("Starting stock (0-1000): ", 0, 1000);

        ds.getSnacks().add(new Snack(ds.generateId(), name, price, stock, true));
        ds.saveSnacks();
        System.out.println("Snack added.");
    }

    private void editName() {
        DataStore ds = DataStore.getInstance();

        try {
            Snack s = chooseSnack();
            s.setName(InputHelper.getValidName("New name (2-30 letters, digits, spaces): ", 2, 30));
            ds.saveSnacks();
            System.out.println("Snack name updated.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updatePrice() {
        DataStore ds = DataStore.getInstance();

        try {
            Snack s = chooseSnack();
            s.setPrice(InputHelper.getValidDouble("New price (1.00-500.00): ", 1, 500));
            ds.saveSnacks();
            System.out.println("Snack price updated.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateStock() {
        DataStore ds = DataStore.getInstance();

        try {
            Snack s = chooseSnack();
            System.out.println("Current stock: " + s.getStock());
            s.setStock(InputHelper.getValidInt("New stock (0-1000): ", 0, 1000));
            ds.saveSnacks();
            System.out.println("Snack stock updated.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void discontinueSnack() {
        DataStore ds = DataStore.getInstance();

        try {
            Snack s = chooseSnack();
            if (!s.isActive()) {
                throw new InvalidInputException("That snack is already discontinued.");
            }
            if (InputHelper.getYesNo("Discontinue " + s.getName() + "? (Y/N): ")) {
                s.setActive(false);
                ds.saveSnacks();
                System.out.println("Snack discontinued.");
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewLowStock() {
        DataStore ds = DataStore.getInstance();
        int threshold = ds.getConfigInt("LOW_STOCK_THRESHOLD");
        int count = 0;

        System.out.println("Low stock means " + threshold + " or fewer left.");
        printSnackHeader();
        for (Snack s : ds.getSnacks()) {
            if (s.isActive() && s.getStock() <= threshold) {
                printSnackRow(s);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No snacks are low on stock.");
        }
    }

    private void setThreshold() {
        DataStore ds = DataStore.getInstance();

        System.out.println("Current low-stock threshold: " + ds.getConfigInt("LOW_STOCK_THRESHOLD"));
        int value = InputHelper.getValidInt("New threshold (1-100): ", 1, 100);
        ds.setConfig("LOW_STOCK_THRESHOLD", String.valueOf(value));
        System.out.println("Low-stock threshold updated.");
    }
}
