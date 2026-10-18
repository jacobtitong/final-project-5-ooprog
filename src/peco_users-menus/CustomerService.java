public class CustomerService extends User {
    public CustomerService(int id, String name, String email, String password, boolean active) {
        super(id, name, email, password, active);
    }

    @Override
    public User displayDashboard() {
        boolean loggedOut = false;
        while (!loggedOut) {
            System.out.print("\n=== CUSTOMER SERVICE MENU ===");
            System.out.print("\n1 - View a user's bookings\n2 - Process a refund\n3 - Create a bulk request\n4 - Logout\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 4);

            switch (option) {
                case 1:
                    BookingManager.viewUserBookings(this);
                    break;
                case 2:
                    BookingManager.processRefund(this);
                    break;
                case 3:
                    BookingManager.createBulkRequest(this);
                    break;
                case 4:
                    loggedOut = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-4.");
            }
        }

        return new Guest();
    }
}
