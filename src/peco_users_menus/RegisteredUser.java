package peco_users_menus;
import cullarin_exceptions_logic.BookingManager;
import cullarin_exceptions_logic.InvalidInputException;
import titong_model_data.Booking;
import titong_model_data.DataStore;
import titong_model_data.Review;

import java.util.ArrayList;

public class RegisteredUser extends User {
    private String birthdate;
    private String savedPaymentType;
    private String savedPaymentNumber;

    public RegisteredUser(int id, String name, String email, String password, boolean active,
            String birthdate, String savedPaymentType, String savedPaymentNumber) {
        super(id, name, email, password, active);
        this.birthdate = birthdate;
        this.savedPaymentType = savedPaymentType;
        this.savedPaymentNumber = savedPaymentNumber;
    }

    public String getBirthdate() { return birthdate; }
    public String getSavedPaymentType() { return savedPaymentType; }
    public String getSavedPaymentNumber() { return savedPaymentNumber; }

    public void setBirthdate(String birthdate) throws InvalidInputException {
        if (!InputHelper.isValidDate(birthdate)) {
            throw new InvalidInputException("Birthdate must be a real date as YYYY-MM-DD.");
        }
        if (birthdate.compareTo(BookingManager.today()) > 0) {
            throw new InvalidInputException("Birthdate cannot be in the future.");
        }
        int age = BookingManager.calculateAge(birthdate);
        if (age < 5 || age > 120) {
            throw new InvalidInputException("Age must be between 5 and 120.");
        }
        this.birthdate = birthdate;
    }

    public void setSavedPaymentType(String savedPaymentType) throws InvalidInputException {
        if (!savedPaymentType.equals("none") && !savedPaymentType.equals("CREDIT")
                && !savedPaymentType.equals("DEBIT") && !savedPaymentType.equals("EWALLET")) {
            throw new InvalidInputException("Payment type must be CREDIT, DEBIT, EWALLET or none.");
        }
        this.savedPaymentType = savedPaymentType;
    }

    // set the type first: the number is checked against the saved type
    public void setSavedPaymentNumber(String savedPaymentNumber) throws InvalidInputException {
        if (!savedPaymentNumber.equals("none")) {
            if (savedPaymentType.equals("EWALLET")) {
                if (!savedPaymentNumber.matches("09[0-9]{9}")) {
                    throw new InvalidInputException("E-wallet number must be 11 digits starting with 09.");
                }
            } else if (savedPaymentType.equals("CREDIT") || savedPaymentType.equals("DEBIT")) {
                if (!savedPaymentNumber.matches("[0-9]{16}")) {
                    throw new InvalidInputException("Card number must be exactly 16 digits.");
                }
            } else {
                throw new InvalidInputException("Choose a payment type first.");
            }
        }
        this.savedPaymentNumber = savedPaymentNumber;
    }

    @Override
    public User displayDashboard() {
        boolean loggedOut = false;
        while (!loggedOut) {
            System.out.print("\n=== REGISTERED USER MENU ===");
            System.out.print("\n1 - Browse movies\n2 - View schedules\n3 - View movie synopsis\n4 - Search showtimes\n5 - Book tickets\n6 - Cancel a booking\n7 - Booking history\n8 - View a ticket\n9 - Review a movie\n10 - Edit profile\n11 - Deactivate my account\n12 - Logout\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 12);

            switch (option) {
                case 1:
                    Guest.viewMovies();
                    break;
                case 2:
                    Guest.viewSchedules();
                    break;
                case 3:
                    Guest.viewSynopsis();
                    break;
                case 4:
                    Guest.searchShowtimes();
                    break;
                case 5:
                    BookingManager.bookOnline(this);
                    break;
                case 6:
                    BookingManager.cancelBooking(this);
                    break;
                case 7:
                    BookingManager.showBookingHistory(this);
                    break;
                case 8:
                    viewTicket();
                    break;
                case 9:
                    BookingManager.submitReview(this);
                    break;
                case 10:
                    editProfile();
                    break;
                case 11:
                    if (deactivate()) {
                        loggedOut = true;
                    }
                    break;
                case 12:
                    loggedOut = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-12.");
            }
        }

        return new Guest();
    }

    // ----- view ticket -----

    private void viewTicket() {
        DataStore ds = DataStore.getInstance();
        ArrayList<Booking> bookings = ds.loadBookings(ds.getUserBookingsPath(getId()));

        if (bookings.size() == 0) {
            System.out.println("You have no bookings yet.");
        } else {
            BookingManager.showBookingHistory(this);
            try {
                int bookingId = InputHelper.getValidInt("Booking number (the number in BK-000012 is 12): ", 1, 999999);
                Booking found = null;
                for (Booking b : bookings) {
                    if (b.getId() == bookingId) {
                        found = b;
                    }
                }
                if (found == null) {
                    throw new InvalidInputException("Booking not found.");
                }
                BookingManager.printTicket(found);
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ----- deactivate my account -----

    private boolean deactivate() {
        boolean done = false;

        if (InputHelper.getYesNo("Deactivate your account? You will not be able to log in again. (Y/N): ")) {
            setActive(false);
            DataStore.getInstance().saveUsers();
            System.out.println("Your account has been deactivated. Your bookings are kept.");
            done = true;
        }
        return done;
    }

    // ----- edit profile -----

    private void editProfile() {
        System.out.println("\nName: " + getName() + " | Birthdate: " + birthdate + " | Saved payment: " + maskedPayment());
        boolean back = false;
        while (!back) {
            System.out.print("\n=== EDIT PROFILE ===");
            System.out.print("\n1 - Change name\n2 - Change birthdate\n3 - Change saved payment method\n4 - Remove saved payment method\n5 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 5);

            switch (option) {
                case 1:
                    changeName();
                    break;
                case 2:
                    changeBirthdate();
                    break;
                case 3:
                    changePayment();
                    break;
                case 4:
                    removePayment();
                    break;
                case 5:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-5.");
            }
        }
    }

    private String maskedPayment() {
        String text = "none";
        if (!savedPaymentType.equals("none") && savedPaymentNumber.length() >= 4) {
            text = savedPaymentType + " ****" + savedPaymentNumber.substring(savedPaymentNumber.length() - 4);
        }
        return text;
    }

    private void changeName() {
        try {
            setName(InputHelper.getValidName("New name (2-30 letters, digits, spaces): ", 2, 30));
            DataStore.getInstance().saveUsers();
            System.out.println("Name updated.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void changeBirthdate() {
        try {
            setBirthdate(InputHelper.getValidBirthdate("New birthdate (YYYY-MM-DD): "));
            DataStore.getInstance().saveUsers();
            System.out.println("Birthdate updated.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void changePayment() {
        try {
            String type = InputHelper.getPaymentType();
            String number = InputHelper.getValidPaymentNumber("Account number: ", type);
            setSavedPaymentType(type);
            setSavedPaymentNumber(number);
            DataStore.getInstance().saveUsers();
            System.out.println("Saved payment method updated.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void removePayment() {
        try {
            setSavedPaymentType("none");
            setSavedPaymentNumber("none");
            DataStore.getInstance().saveUsers();
            System.out.println("Saved payment method removed.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
