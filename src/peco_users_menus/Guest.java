package peco_users_menus;
import cullarin_exceptions_logic.AuthenticationException;
import cullarin_exceptions_logic.BookingManager;
import cullarin_exceptions_logic.CbsException;
import cullarin_exceptions_logic.InvalidInputException;
import titong_model_data.DataStore;
import titong_model_data.Hall;
import titong_model_data.Movie;
import titong_model_data.Review;
import titong_model_data.Schedule;

import java.util.ArrayList;

public class Guest extends User {
    public Guest() {
        super(0, "Guest", "none", "none", true);
    }

    @Override
    public User displayDashboard() {
        User nextUser = this;
        User loggedIn = null;
        boolean endMenu = false;
        while (!endMenu) {
            System.out.print("\n=== GUEST MENU ===");
            System.out.print("\n1 - Browse movies\n2 - View schedules\n3 - View movie synopsis\n4 - Search showtimes\n5 - Log in\n6 - Sign up\n7 - Exit\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 7);

            switch (option) {
                case 1:
                    viewMovies();
                    break;
                case 2:
                    viewSchedules();
                    break;
                case 3:
                    viewSynopsis();
                    break;
                case 4:
                    searchShowtimes();
                    break;
                case 5:
                    loggedIn = login();
                    if (loggedIn != null) {
                        nextUser = loggedIn;
                        endMenu = true;
                    }
                    break;
                case 6:
                    signUp();
                    break;
                case 7:
                    nextUser = null;
                    endMenu = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-7.");
            }
        }

        return nextUser;
    }

    // ----- browsing (also used by RegisteredUser, Cashier and Admin) -----

    public static void viewMovies() {
        DataStore ds = DataStore.getInstance();

        if (ds.getMovies().size() == 0) {
            System.out.println("No movies yet.");
        } else {
            System.out.println("-------------------------------------------------------------------------------");
            System.out.printf("%-4s | %-20s | %-12s | %-10s | %-10s | %-10s%n", "ID", "Title", "Genre", "Language", "Rating", "Avg Review");
            System.out.println("-------------------------------------------------------------------------------");
            for (Movie m : ds.getMovies()) {
                System.out.printf("%-4d | %-20.20s | %-12.12s | %-10.10s | %-10.10s | %-10s%n",
                        m.getId(), m.getTitle(), m.getGenre(), m.getLanguage(), m.getAgeRating(), averageRating(m.getId()));
            }
        }
    }

    public static String averageRating(int movieId) {
        DataStore ds = DataStore.getInstance();
        int total = 0;
        int count = 0;

        for (Review r : ds.getReviews()) {
            if (r.getMovieId() == movieId) {
                total += r.getRating();
                count++;
            }
        }
        if (count == 0) {
            return "N/A";
        }
        return String.format("%.1f", (double) total / count);
    }

    public static boolean isUpcoming(Schedule s) {
        String today = BookingManager.today();
        boolean upcoming = false;

        if (s.getDate().compareTo(today) > 0) {
            upcoming = true;
        } else if (s.getDate().equals(today) && BookingManager.toMinutes(s.getStartTime()) > BookingManager.nowMinutes()) {
            upcoming = true;
        }
        return upcoming;
    }

    public static ArrayList<Schedule> getUpcomingSchedules() {
        DataStore ds = DataStore.getInstance();
        ArrayList<Schedule> list = new ArrayList<>();

        for (Schedule s : ds.getSchedules()) {
            if (isUpcoming(s)) {
                list.add(s);
            }
        }
        return list;
    }

    public static void printSchedules(ArrayList<Schedule> list, boolean showRating) {
        DataStore ds = DataStore.getInstance();

        if (list.size() == 0) {
            System.out.println("No schedules to show.");
        } else {
            String line = "-----------------------------------------------------------------------------------------------";
            System.out.println(line);
            if (showRating) {
                System.out.printf("%-4s | %-16s | %-12s | %-8s | %-10s | %-5s | %-6s | %-11s | %-6s%n",
                        "ID", "Movie", "Cinema", "Hall", "Date", "Time", "Format", "Price", "Rating");
            } else {
                System.out.printf("%-4s | %-16s | %-12s | %-8s | %-10s | %-5s | %-6s | %-11s%n",
                        "ID", "Movie", "Cinema", "Hall", "Date", "Time", "Format", "Price");
            }
            System.out.println(line);
            for (Schedule s : list) {
                Movie m = ds.findMovieById(s.getMovieId());
                Hall h = ds.findHallById(s.getHallId());
                String title = "Unknown";
                String rating = "N/A";
                String cinema = "Unknown";
                String hallName = "Unknown";
                if (m != null) {
                    title = m.getTitle();
                    rating = averageRating(m.getId());
                }
                if (h != null) {
                    cinema = h.getCinemaLocation();
                    hallName = h.getHallName();
                }
                String price = String.format("Php %.2f", s.getTicketPrice());

                if (showRating) {
                    System.out.printf("%-4d | %-16.16s | %-12.12s | %-8.8s | %-10s | %-5s | %-6s | %-11s | %-6s%n",
                            s.getId(), title, cinema, hallName, s.getDate(), s.getStartTime(), s.getFormat(), price, rating);
                } else {
                    System.out.printf("%-4d | %-16.16s | %-12.12s | %-8.8s | %-10s | %-5s | %-6s | %-11s%n",
                            s.getId(), title, cinema, hallName, s.getDate(), s.getStartTime(), s.getFormat(), price);
                }
            }
        }
    }

    public static void viewSchedules() {
        printSchedules(getUpcomingSchedules(), false);
    }

    public static void viewSynopsis() {
        DataStore ds = DataStore.getInstance();

        viewMovies();
        if (ds.getMovies().size() > 0) {
            int id = InputHelper.getValidInt("Movie ID: ", 1, 999999);
            Movie m = ds.findMovieById(id);
            if (m == null) {
                System.out.println("Error: Movie ID not found.");
            } else {
                System.out.println("\n" + m.getTitle() + " (" + m.getAgeRating() + ")");
                System.out.println("Genre: " + m.getGenre() + " | Language: " + m.getLanguage() + " | " + m.getDurationMinutes() + " minutes");
                System.out.println("Average review: " + averageRating(m.getId()));
                System.out.println("Synopsis: " + m.getSynopsis());
            }
        }
    }

    // ----- search / filter (blank answer = skip that filter) -----

    public static void searchShowtimes() {
        DataStore ds = DataStore.getInstance();

        ArrayList<String> genres = new ArrayList<>();
        ArrayList<String> languages = new ArrayList<>();
        ArrayList<String> locations = new ArrayList<>();
        for (Movie m : ds.getMovies()) {
            addUnique(genres, m.getGenre());
            addUnique(languages, m.getLanguage());
        }
        for (Hall h : ds.getHalls()) {
            addUnique(locations, h.getCinemaLocation());
        }

        System.out.println("\nPress Enter to skip a filter.");
        printOptions("Genres", genres);
        String genre = InputHelper.getOptional("Genre: ");
        printOptions("Languages", languages);
        String language = InputHelper.getOptional("Language: ");
        System.out.println("Formats: 2D, 3D, IMAX, 4DX");
        String format = InputHelper.getOptional("Format: ");
        printOptions("Cinemas", locations);
        String location = InputHelper.getOptional("Cinema location: ");

        String date = "";
        boolean isValid = false;
        while (!isValid) {
            date = InputHelper.getOptional("Date (YYYY-MM-DD): ");
            if (date.equals("") || InputHelper.isValidDate(date)) {
                isValid = true;
            } else {
                System.out.println("Error: Enter a real date as YYYY-MM-DD, or press Enter to skip.");
            }
        }

        String time = "";
        isValid = false;
        while (!isValid) {
            time = InputHelper.getOptional("Earliest start time (HH:MM): ");
            if (time.equals("") || InputHelper.isValidTime(time)) {
                isValid = true;
            } else {
                System.out.println("Error: Enter a time as HH:MM, or press Enter to skip.");
            }
        }

        ArrayList<Schedule> matches = new ArrayList<>();
        for (Schedule s : getUpcomingSchedules()) {
            Movie m = ds.findMovieById(s.getMovieId());
            Hall h = ds.findHallById(s.getHallId());
            boolean match = true;

            if (m == null || h == null) {
                match = false;
            } else {
                if (!genre.equals("") && !m.getGenre().equalsIgnoreCase(genre)) {
                    match = false;
                }
                if (!language.equals("") && !m.getLanguage().equalsIgnoreCase(language)) {
                    match = false;
                }
                if (!format.equals("") && !s.getFormat().equalsIgnoreCase(format)) {
                    match = false;
                }
                if (!location.equals("") && !h.getCinemaLocation().equalsIgnoreCase(location)) {
                    match = false;
                }
                if (!date.equals("") && !s.getDate().equals(date)) {
                    match = false;
                }
                if (!time.equals("") && BookingManager.toMinutes(s.getStartTime()) < BookingManager.toMinutes(time)) {
                    match = false;
                }
            }

            if (match) {
                matches.add(s);
            }
        }

        if (matches.size() == 0) {
            System.out.println("No showtimes match your search.");
        } else {
            printSchedules(matches, true);
        }
    }

    private static void addUnique(ArrayList<String> list, String value) {
        boolean found = false;
        for (String item : list) {
            if (item.equalsIgnoreCase(value)) {
                found = true;
            }
        }
        if (!found) {
            list.add(value);
        }
    }

    private static void printOptions(String label, ArrayList<String> values) {
        String text = "";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                text += ", ";
            }
            text += values.get(i);
        }
        if (values.size() == 0) {
            text = "(none yet)";
        }
        System.out.println(label + ": " + text);
    }

    // ----- sign up -----

    private static void signUp() {
        DataStore ds = DataStore.getInstance();

        try {
            System.out.println("\n--- SIGN UP ---");
            String name = InputHelper.getValidName("Name (2-30 letters, digits, spaces): ", 2, 30);
            String email = InputHelper.getValidEmail("Email: ");
            if (ds.findUserByEmail(email) != null) {
                throw new InvalidInputException("That email is already registered.");
            }
            String password = InputHelper.getValidPassword("Password (8-20 chars, at least one letter and one digit): ");
            String birthdate = InputHelper.getValidBirthdate("Birthdate (YYYY-MM-DD): ");

            String paymentType = "none";
            String paymentNumber = "none";
            if (InputHelper.getYesNo("Save a payment method? (Y/N): ")) {
                paymentType = InputHelper.getPaymentType();
                paymentNumber = InputHelper.getValidPaymentNumber("Account number: ", paymentType);
            }

            int id = ds.generateId();
            RegisteredUser user = new RegisteredUser(id, name, email, password, true, birthdate, paymentType, paymentNumber);
            ds.getUsers().add(user);
            ds.saveUsers();
            ds.createUserBookingsFile(id);
            System.out.println("Account created. You can now log in.");
        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ----- login -----

    private static User login() {
        DataStore ds = DataStore.getInstance();
        User found = null;

        try {
            System.out.println("\n--- LOG IN ---");
            String email = InputHelper.getOptional("Email: ");
            String password = InputHelper.getOptional("Password: ");

            User u = ds.findUserByEmail(email);
            if (u == null || !u.getPassword().equals(password)) {
                throw new AuthenticationException("Invalid email or password.");
            }
            if (!u.isActive()) {
                throw new AuthenticationException("This account has been deactivated.");
            }
            System.out.println("Welcome, " + u.getName() + "!");
            found = u;
        } catch (AuthenticationException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return found;
    }
}
