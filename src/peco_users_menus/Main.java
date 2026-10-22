package peco_users_menus;
import titong_model_data.Booking;
import titong_model_data.DataStore;

public class Main {
    public static void main(String[] args) {
        DataStore.getInstance();
        System.out.println("=== CINEMA BOOKING SYSTEM ===");

        User currentUser = new Guest();
        while (currentUser != null) {
            currentUser = currentUser.displayDashboard();
        }

        System.out.println("\nThank you for using the Cinema Booking System. Goodbye!");
    }
}
