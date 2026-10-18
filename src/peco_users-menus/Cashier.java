import java.util.ArrayList;

public class Cashier extends User {
    public Cashier(int id, String name, String email, String password, boolean active) {
        super(id, name, email, password, active);
    }

    @Override
    public User displayDashboard() {
        boolean loggedOut = false;
        while (!loggedOut) {
            System.out.print("\n=== CASHIER MENU ===");
            System.out.print("\n1 - Search showtimes\n2 - Counter booking\n3 - Print receipt\n4 - Logout\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 4);

            switch (option) {
                case 1:
                    Guest.searchShowtimes();
                    break;
                case 2:
                    BookingManager.bookAtCounter(this);
                    break;
                case 3:
                    printReceipt();
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

    // ----- print receipt for a counter booking -----

    private void printReceipt() {
        DataStore ds = DataStore.getInstance();
        ArrayList<Booking> bookings = ds.loadBookings(ds.getCounterBookingsPath());

        if (bookings.size() == 0) {
            System.out.println("No counter bookings yet.");
        } else {
            System.out.println("---------------------------------------------------------------------------");
            System.out.printf("%-10s | %-15s | %-8s | %-15s | %-10s | %-12s%n", "Ref", "Customer", "Schedule", "Seats", "Status", "Total");
            System.out.println("---------------------------------------------------------------------------");
            for (Booking b : bookings) {
                String ref = "BK-" + String.format("%06d", b.getId());
                System.out.printf("%-10s | %-15.15s | %-8d | %-15.15s | %-10.10s | Php %-8.2f%n",
                        ref, b.getCustomerName(), b.getScheduleId(), b.getSeats(), b.getStatus(), b.getTotalPaid());
            }

            int bookingId = InputHelper.getValidInt("Booking number to print (the number in BK-000012 is 12): ", 1, 999999);
            Booking found = null;
            for (Booking b : bookings) {
                if (b.getId() == bookingId) {
                    found = b;
                }
            }
            if (found == null) {
                System.out.println("Error: Booking not found.");
            } else {
                BookingManager.printTicket(found);
            }
        }
    }
}
