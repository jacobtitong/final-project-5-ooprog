package cullarin_exceptions_logic;
import mendoza_strategy.BulkDiscount;
import mendoza_strategy.CashPayment;
import mendoza_strategy.CreditCardPayment;
import mendoza_strategy.DebitCardPayment;
import mendoza_strategy.DiscountStrategy;
import mendoza_strategy.EWalletPayment;
import mendoza_strategy.PaymentStrategy;
import mendoza_strategy.PromoDiscount;
import mendoza_strategy.PWDDiscount;
import mendoza_strategy.SeniorDiscount;
import mendoza_strategy.StudentDiscount;
import peco_users_menus.Admin;
import peco_users_menus.Cashier;
import peco_users_menus.CustomerService;
import peco_users_menus.InputHelper;
import peco_users_menus.RegisteredUser;
import peco_users_menus.User;
import titong_model_data.Booking;
import titong_model_data.BulkBookingRequest;
import titong_model_data.DataStore;
import titong_model_data.Discount;
import titong_model_data.Hall;
import titong_model_data.Movie;
import titong_model_data.Review;
import titong_model_data.Schedule;
import titong_model_data.SeatLedgerEntry;
import titong_model_data.Snack;
import titong_model_data.SnackOrder;
import titong_model_data.Transaction;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class BookingManager {
    // ----- time, money and age helpers -----

    public static int toMinutes(String hhmm) {
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public static String toTimeString(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    public static String today() {
        return LocalDate.now().toString();
    }

    public static int nowMinutes() {
        return LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
    }

    public static double round2(double x) {
        return Math.round(x * 100) / 100.0;
    }

    // Minimum ages for each rating (G = 0 so it is always allowed)
    private static int minimumAge(String ageRating) {
        switch (ageRating.toUpperCase()) {
            case "PG":  return 13;
            case "R13": return 13;
            case "R16": return 16;
            case "R18": return 18;
            default:    return 0;  // G or unknown -- no restriction
        }
    }

    /**
     * Throws AgeRestrictionException when the registered user is too young
     * for the movie's age rating.  Counter bookings skip this check because
     * the cashier is responsible for verifying a physical ID on the spot.
     */
    public static void checkAgeRating(RegisteredUser user, Movie movie)
            throws AgeRestrictionException {
        int required = minimumAge(movie.getAgeRating());
        if (required > 0) {
            int userAge = calculateAge(user.getBirthdate());
            if (userAge < required) {
                throw new AgeRestrictionException(
                        "You must be at least " + required + " years old to book \""
                        + movie.getTitle() + "\" (" + movie.getAgeRating() + "). "
                        + "Your registered age is " + userAge + ".");
            }
        }
    }

    public static int calculateAge(String birthdate) {
        String[] born = birthdate.split("-");
        String[] now = today().split("-");
        int age = Integer.parseInt(now[0]) - Integer.parseInt(born[0]);
        int monthBorn = Integer.parseInt(born[1]);
        int dayBorn = Integer.parseInt(born[2]);
        int monthNow = Integer.parseInt(now[1]);
        int dayNow = Integer.parseInt(now[2]);

        if (monthNow < monthBorn || (monthNow == monthBorn && dayNow < dayBorn)) {
            age--;
        }
        return age;
    }

    // a showing is upcoming if its date is after today, or today and it starts later than now
    private static boolean isUpcoming(Schedule s) {
        boolean upcoming = false;

        if (s.getDate().compareTo(today()) > 0) {
            upcoming = true;
        } else if (s.getDate().equals(today()) && toMinutes(s.getStartTime()) > nowMinutes()) {
            upcoming = true;
        }
        return upcoming;
    }

    // a booking can only be cancelled up to this many minutes before showtime
    public static final int CANCEL_DEADLINE_MINUTES = 30;

    // minutes from now until the showing starts (negative once it has started)
    private static long minutesUntilStart(Schedule s) {
        LocalDateTime start = LocalDateTime.of(LocalDate.parse(s.getDate()), LocalTime.parse(s.getStartTime()));
        return Duration.between(LocalDateTime.now(), start).toMinutes();
    }

    // removes every seat a booking holds from the ledger and saves it
    private static void releaseSeats(int bookingId) {
        DataStore ds = DataStore.getInstance();

        for (int i = ds.getLedger().size() - 1; i >= 0; i--) {
            if (ds.getLedger().get(i).getBookingId() == bookingId) {
                ds.getLedger().remove(i);
            }
        }
        ds.saveLedger();
    }

    private static String bookingRef(int id) {
        return "BK-" + String.format("%06d", id);
    }

    // ----- overlap checks (window = start - BEFORE .. start + duration + AFTER) -----

    private static int windowStart(Schedule s) {
        return toMinutes(s.getStartTime()) - DataStore.getInstance().getConfigInt("GRACE_PERIOD_BEFORE_MINUTES");
    }

    private static int windowEnd(Schedule s) {
        DataStore ds = DataStore.getInstance();
        Movie m = ds.findMovieById(s.getMovieId());
        int duration = 0;

        if (m != null) {
            duration = m.getDurationMinutes();
        }
        return toMinutes(s.getStartTime()) + duration + ds.getConfigInt("GRACE_PERIOD_AFTER_MINUTES");
    }

    // two windows on the same date overlap if startA < endB && startB < endA (touching is allowed)
    private static boolean windowsOverlap(Schedule a, Schedule b) {
        return a.getDate().equals(b.getDate()) && windowStart(a) < windowEnd(b) && windowStart(b) < windowEnd(a);
    }

    public static void checkOverlap(int userId, Schedule s) throws TimeOverlapException {
        DataStore ds = DataStore.getInstance();
        ArrayList<Booking> bookings = ds.loadBookings(ds.getUserBookingsPath(userId));

        for (Booking b : bookings) {
            if (b.getStatus().equals("CONFIRMED")) {
                Schedule other = ds.findScheduleById(b.getScheduleId());
                if (other != null && windowsOverlap(s, other)) {
                    throw new TimeOverlapException("This showing overlaps your booking " + bookingRef(b.getId())
                            + " (" + other.getStartTime() + " on " + other.getDate() + ").");
                }
            }
        }
    }

    public static void checkScheduleConflict(Schedule s, int ignoreScheduleId) throws ScheduleConflictException {
        DataStore ds = DataStore.getInstance();

        if (ds.findMovieById(s.getMovieId()) == null) {
            throw new ScheduleConflictException("Movie not found.");
        }
        if (windowStart(s) < 0 || windowEnd(s) > 1439) {
            throw new ScheduleConflictException("The showing, with its grace periods, must stay within the same day (00:00-23:59).");
        }
        for (Schedule other : ds.getSchedules()) {
            if (other.getId() != ignoreScheduleId && other.getHallId() == s.getHallId() && windowsOverlap(s, other)) {
                throw new ScheduleConflictException("This overlaps schedule " + other.getId() + " in the same hall ("
                        + other.getStartTime() + " on " + other.getDate() + ").");
            }
        }
    }

    // ----- seat map and seat selection -----

    public static void printSeatMap(Schedule s) {
        DataStore ds = DataStore.getInstance();
        Hall hall = ds.findHallById(s.getHallId());

        if (hall == null) {
            System.out.println("Error: Hall not found.");
        } else {
            int width = hall.getColumns() * 4;
            String padding = "";
            String line = "";
            for (int i = 0; i < (width - 6) / 2; i++) {
                padding += " ";
            }
            for (int i = 0; i < width; i++) {
                line += "-";
            }

            System.out.println("\n" + padding + "SCREEN");
            System.out.println(line);
            for (int r = 0; r < hall.getRows(); r++) {
                String row = "";
                for (int c = 1; c <= hall.getColumns(); c++) {
                    String code = seatCode(r, c);
                    if (isSeatTaken(s.getId(), code)) {
                        row += String.format("%-4s", "XX");
                    } else {
                        row += String.format("%-4s", code);
                    }
                }
                System.out.println(row);
            }
            System.out.println("XX = taken");
        }
    }

    // row letter + column number, for example row 1, column 7 gives B7
    private static String seatCode(int rowIndex, int column) {
        return "" + (char) ('A' + rowIndex) + column;
    }

    private static boolean isSeatTaken(int scheduleId, String code) {
        DataStore ds = DataStore.getInstance();

        for (SeatLedgerEntry entry : ds.getLedger()) {
            if (entry.getScheduleId() == scheduleId && entry.getSeatCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    private static int countFreeSeats(Schedule s, Hall hall) {
        DataStore ds = DataStore.getInstance();
        int taken = 0;

        for (SeatLedgerEntry entry : ds.getLedger()) {
            if (entry.getScheduleId() == s.getId()) {
                taken++;
            }
        }
        return hall.getRows() * hall.getColumns() - taken;
    }

    // asks for seats like A1,A2 and checks every rule; nothing is saved here
    private static ArrayList<String> chooseSeats(Schedule s, int limit) throws SeatUnavailableException, TicketLimitExceededException {
        DataStore ds = DataStore.getInstance();
        Hall hall = ds.findHallById(s.getHallId());

        if (hall == null) {
            throw new SeatUnavailableException("The hall for this showing was not found.");
        }
        if (countFreeSeats(s, hall) <= 0) {
            throw new SeatUnavailableException("This showing is sold out.");
        }

        printSeatMap(s);
        String input = InputHelper.getValidText("\nEnter seats separated by commas (example A1,A2): ", 2, 100);
        String[] parts = input.split(",");
        if (parts.length > limit) {
            throw new TicketLimitExceededException("You can book at most " + limit + " tickets per transaction.");
        }

        ArrayList<String> seats = new ArrayList<>();
        for (String part : parts) {
            String code = part.trim().toUpperCase();
            if (!code.matches("[A-Z][0-9]{1,2}")) {
                throw new SeatUnavailableException("Seat code " + code + " is not valid. Use a letter and a number like B7.");
            }
            int row = code.charAt(0) - 'A';
            int column = Integer.parseInt(code.substring(1));
            if (row >= hall.getRows() || column < 1 || column > hall.getColumns()) {
                throw new SeatUnavailableException("Seat " + code + " does not exist in this hall.");
            }
            code = seatCode(row, column);

            for (String chosen : seats) {
                if (chosen.equals(code)) {
                    throw new SeatUnavailableException("Seat " + code + " was entered more than once.");
                }
            }
            if (isSeatTaken(s.getId(), code)) {
                throw new SeatUnavailableException("Seat " + code + " is already taken.");
            }
            seats.add(code);
        }
        return seats;
    }

    // the first free seats, row by row (used when a bulk request is approved)
    private static ArrayList<String> findFreeSeats(Schedule s, int count) {
        DataStore ds = DataStore.getInstance();
        Hall hall = ds.findHallById(s.getHallId());
        ArrayList<String> seats = new ArrayList<>();

        if (hall != null) {
            for (int r = 0; r < hall.getRows(); r++) {
                for (int c = 1; c <= hall.getColumns(); c++) {
                    String code = seatCode(r, c);
                    if (seats.size() < count && !isSeatTaken(s.getId(), code)) {
                        seats.add(code);
                    }
                }
            }
        }
        return seats;
    }

    // ----- booking (online and counter share bookTickets) -----

    public static void bookOnline(RegisteredUser u) {
        bookTickets(false, u);
    }

    private static void bookTickets(boolean counter, RegisteredUser user) {
        DataStore ds = DataStore.getInstance();

        try {
            String ownerType = "GUEST";
            int ownerId = 0;
            String customerName = "Walk-in";
            String path = ds.getCounterBookingsPath();
            if (!counter) {
                ownerType = "REGISTERED_USER";
                ownerId = user.getId();
                customerName = user.getName();
                path = ds.getUserBookingsPath(user.getId());
            }

            // 1. movie and an upcoming schedule
            printMovieList();
            int movieId = InputHelper.getValidInt("Movie ID: ", 1, 999999);
            Movie movie = ds.findMovieById(movieId);
            if (movie == null) {
                throw new InvalidInputException("Movie ID not found.");
            }

            // age-rating enforcement (online only; cashier checks ID at counter)
            if (!counter) {
                checkAgeRating(user, movie);
            }

            ArrayList<Schedule> upcoming = upcomingSchedulesOf(movieId);
            if (upcoming.size() == 0) {
                throw new InvalidInputException("This movie has no upcoming showings.");
            }
            printScheduleList(upcoming);
            int scheduleId = InputHelper.getValidInt("Schedule ID: ", 1, 999999);
            Schedule schedule = null;
            for (Schedule s : upcoming) {
                if (s.getId() == scheduleId) {
                    schedule = s;
                }
            }
            if (schedule == null) {
                throw new InvalidInputException("Choose a schedule ID from the list.");
            }

            // 2. overlap check (registered users only)
            if (!counter) {
                checkOverlap(user.getId(), schedule);
            }

            // 3. seats are only held in this list, nothing is written yet
            ArrayList<String> seats = chooseSeats(schedule, ds.getConfigInt("TICKET_LIMIT_PER_TRANSACTION"));
            double ticketSubtotal = round2(schedule.getTicketPrice() * seats.size());

            // 4. snacks
            ArrayList<Snack> pickedSnacks = new ArrayList<>();
            ArrayList<Integer> pickedQuantities = new ArrayList<>();
            pickSnacks(pickedSnacks, pickedQuantities);
            double snackTotal = 0;
            for (int i = 0; i < pickedSnacks.size(); i++) {
                snackTotal += pickedSnacks.get(i).getPrice() * pickedQuantities.get(i);
            }
            snackTotal = round2(snackTotal);

            // 5. discounts (tickets only, snacks stay full price)
            String[] discountName = new String[1];
            double discountedTickets = applyDiscounts(ticketSubtotal, counter, user, discountName);
            double total = round2(discountedTickets + snackTotal);

            // 6. summary and confirmation
            String seatsText = "";
            for (int i = 0; i < seats.size(); i++) {
                if (i > 0) {
                    seatsText += ",";
                }
                seatsText += seats.get(i);
            }
            Hall hall = ds.findHallById(schedule.getHallId());
            String place = "Unknown";
            if (hall != null) {
                place = hall.getCinemaLocation() + " / " + hall.getHallName();
            }
            System.out.println("\n=== BOOKING SUMMARY ===");
            System.out.println("Movie: " + movie.getTitle() + " (" + schedule.getFormat() + ")");
            System.out.println("Showing: " + schedule.getDate() + " " + schedule.getStartTime() + " at " + place);
            System.out.println("Seats: " + seatsText + " | Tickets: " + seats.size());
            System.out.printf("Tickets: Php %.2f%n", ticketSubtotal);
            System.out.printf("Discount (%s): -Php %.2f%n", discountName[0], round2(ticketSubtotal - discountedTickets));
            for (int i = 0; i < pickedSnacks.size(); i++) {
                System.out.printf("Snack: %s x%d = Php %.2f%n", pickedSnacks.get(i).getName(), pickedQuantities.get(i),
                        pickedSnacks.get(i).getPrice() * pickedQuantities.get(i));
            }
            System.out.printf("Snacks: Php %.2f%n", snackTotal);
            System.out.printf("TOTAL: Php %.2f%n", total);

            if (!InputHelper.getYesNo("Confirm booking? (Y/N): ")) {
                System.out.println("Booking cancelled. Nothing was saved.");
            } else {
                // 7. payment (retry or abort, nothing is written until it works)
                String method = takePayment(total, counter, user);

                if (!method.equals("")) {
                    // 8. everything is saved only now.
                    // Reserve ALL IDs needed for this booking in a single atomic write:
                    //   ids[0] = bookingId, ids[1] = transactionId,
                    //   ids[2..N] = one snack-order ID per picked snack item.
                    // The counter is persisted once here; a crash after this point
                    // can only leave a harmless gap -- it cannot produce duplicate IDs.
                    int[] ids = ds.generateIdBlock(2 + pickedSnacks.size());
                    int bookingId     = ids[0];
                    int transactionId = ids[1];
                    String timestamp = LocalDateTime.now().toString();

                    for (String seat : seats) {
                        ds.getLedger().add(new SeatLedgerEntry(schedule.getId(), seat, bookingId));
                    }
                    ds.saveLedger();

                    Booking booking = new Booking(bookingId, ownerType, ownerId, customerName, schedule.getId(),
                            seatsText, "CONFIRMED", total, timestamp);
                    ArrayList<Booking> bookings = ds.loadBookings(path);
                    bookings.add(booking);
                    ds.saveBookings(path, bookings);

                    ds.getTransactions().add(new Transaction(transactionId, bookingId, ownerType, ownerId, schedule.getId(),
                            seats.size(), discountedTickets, snackTotal, total, method, "SUCCESS", 0.0, timestamp));
                    ds.saveTransactions();

                    for (int i = 0; i < pickedSnacks.size(); i++) {
                        Snack snack = pickedSnacks.get(i);
                        snack.setStock(snack.getStock() - pickedQuantities.get(i));
                        ds.getSnackOrders().add(new SnackOrder(ids[2 + i], bookingId, snack.getId(),
                                pickedQuantities.get(i), snack.getPrice()));
                    }
                    ds.saveSnacks();
                    ds.saveSnackOrders();

                    System.out.println("\nPayment successful. Booking " + bookingRef(bookingId) + " is confirmed.");
                    printTicket(booking);
                }
            }
        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printMovieList() {
        DataStore ds = DataStore.getInstance();

        System.out.println("\n-------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-20s | %-12s | %-10s | %-10s | %-10s%n", "ID", "Title", "Genre", "Language", "Rating", "Avg Review");
        System.out.println("-------------------------------------------------------------------------------");
        for (Movie m : ds.getMovies()) {
            System.out.printf("%-4d | %-20.20s | %-12.12s | %-10.10s | %-10.10s | %-10s%n",
                    m.getId(), m.getTitle(), m.getGenre(), m.getLanguage(), m.getAgeRating(), averageRating(m.getId()));
        }
    }

    private static String averageRating(int movieId) {
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

    private static ArrayList<Schedule> upcomingSchedulesOf(int movieId) {
        DataStore ds = DataStore.getInstance();
        ArrayList<Schedule> list = new ArrayList<>();

        for (Schedule s : ds.getSchedules()) {
            if (s.getMovieId() == movieId && isUpcoming(s)) {
                list.add(s);
            }
        }
        return list;
    }

    private static void printScheduleList(ArrayList<Schedule> list) {
        DataStore ds = DataStore.getInstance();
        String line = "-----------------------------------------------------------------------------------------------";

        System.out.println("\n" + line);
        System.out.printf("%-4s | %-16s | %-12s | %-8s | %-10s | %-5s | %-6s | %-11s%n",
                "ID", "Movie", "Cinema", "Hall", "Date", "Time", "Format", "Price");
        System.out.println(line);
        for (Schedule s : list) {
            Movie m = ds.findMovieById(s.getMovieId());
            Hall h = ds.findHallById(s.getHallId());
            String title = "Unknown";
            String cinema = "Unknown";
            String hallName = "Unknown";
            if (m != null) {
                title = m.getTitle();
            }
            if (h != null) {
                cinema = h.getCinemaLocation();
                hallName = h.getHallName();
            }
            System.out.printf("%-4d | %-16.16s | %-12.12s | %-8.8s | %-10s | %-5s | %-6s | Php %-7.2f%n",
                    s.getId(), title, cinema, hallName, s.getDate(), s.getStartTime(), s.getFormat(), s.getTicketPrice());
        }
    }

    // snack id + quantity until the user types 0; only active snacks, stock is checked
    private static void pickSnacks(ArrayList<Snack> picked, ArrayList<Integer> quantities) throws InvalidInputException, InsufficientStockException {
        DataStore ds = DataStore.getInstance();
        int available = 0;

        System.out.println("\n--- SNACKS ---");
        for (Snack s : ds.getSnacks()) {
            if (s.isActive()) {
                if (available == 0) {
                    System.out.printf("%-4s | %-20s | %-12s | %-6s%n", "ID", "Name", "Price", "Stock");
                }
                System.out.printf("%-4d | %-20.20s | Php %-8.2f | %-6d%n", s.getId(), s.getName(), s.getPrice(), s.getStock());
                available++;
            }
        }

        if (available == 0) {
            System.out.println("No snacks are available right now.");
        } else {
            boolean done = false;
            while (!done) {
                int id = InputHelper.getValidInt("Snack ID (0 to finish): ", 0, 999999);

                if (id == 0) {
                    done = true;
                } else {
                    Snack snack = ds.findSnackById(id);
                    if (snack == null) {
                        throw new InvalidInputException("Snack ID not found.");
                    }
                    if (!snack.isActive()) {
                        throw new InvalidInputException("That snack is discontinued.");
                    }
                    int quantity = InputHelper.getValidInt("Quantity (1-10): ", 1, 10);

                    int index = -1;
                    for (int i = 0; i < picked.size(); i++) {
                        if (picked.get(i).getId() == id) {
                            index = i;
                        }
                    }
                    int already = 0;
                    if (index != -1) {
                        already = quantities.get(index);
                    }
                    if (already + quantity > 10) {
                        throw new InvalidInputException("You can order at most 10 of the same snack.");
                    }
                    if (already + quantity > snack.getStock()) {
                        throw new InsufficientStockException("Only " + snack.getStock() + " " + snack.getName() + " left in stock.");
                    }
                    if (index == -1) {
                        picked.add(snack);
                        quantities.add(quantity);
                    } else {
                        quantities.set(index, already + quantity);
                    }
                }
            }
        }
    }

    // identity discount first, then the promo; returns the discounted ticket subtotal
    private static double applyDiscounts(double ticketSubtotal, boolean counter, RegisteredUser user, String[] discountName) throws InvalidDiscountException {
        DataStore ds = DataStore.getInstance();
        DiscountStrategy identity = null;
        DiscountStrategy promo = null;
        String name = "None";

        System.out.print("\n1 - No discount\n2 - Senior citizen\n3 - PWD\n4 - Student\n");
        int choice = InputHelper.getValidInt("--> Discount: ", 1, 4);
        if (choice >= 2) {
            if (!InputHelper.getYesNo("Do you have a valid ID? (Y/N): ")) {
                throw new InvalidDiscountException("A valid ID is required for this discount.");
            }
            if (choice == 2) {
                // Online: verify against the registered birthdate.
                // Counter: cashier must confirm the customer is 60+ before applying.
                if (!counter) {
                    if (calculateAge(user.getBirthdate()) < 60) {
                        throw new InvalidDiscountException("The Senior discount is only for age 60 and above.");
                    }
                } else {
                    if (!InputHelper.getYesNo("Confirm the customer is 60 years old or above? (Y/N): ")) {
                        throw new InvalidDiscountException("The Senior discount is only for age 60 and above.");
                    }
                }
            }
            switch (choice) {
                case 2:
                    identity = new SeniorDiscount();
                    name = "Senior";
                    break;
                case 3:
                    identity = new PWDDiscount();
                    name = "PWD";
                    break;
                default:
                    identity = new StudentDiscount();
                    name = "Student";
            }
        }

        if (InputHelper.getYesNo("Do you have a promo code? (Y/N): ")) {
            String code = InputHelper.getValidCode("Promo code: ");
            Discount found = null;
            for (Discount d : ds.getDiscounts()) {
                if (d.getCode().equalsIgnoreCase(code)) {
                    found = d;
                }
            }
            if (found == null) {
                throw new InvalidDiscountException("Promo code not found.");
            }
            if (!found.isActive()) {
                throw new InvalidDiscountException("That promo is no longer active.");
            }
            if (found.getExpiryDate().compareTo(today()) < 0) {
                throw new InvalidDiscountException("That promo has expired.");
            }
            if (identity != null && !found.isStackable()) {
                throw new InvalidDiscountException("That promo cannot be used together with Senior, PWD or Student discounts.");
            }
            promo = new PromoDiscount(found.getType(), found.getValue());
            if (identity == null) {
                name = "Promo " + found.getCode();
            } else {
                name = name + " + Promo " + found.getCode();
            }
        }

        double result = ticketSubtotal;
        if (identity != null) {
            result = identity.apply(result);
        }
        if (promo != null) {
            result = promo.apply(result);
        }
        discountName[0] = name;
        return result;
    }

    // returns the payment method (CREDIT, DEBIT, EWALLET, CASH), or "" if the user gave up
    private static String takePayment(double total, boolean counter, RegisteredUser user) {
        int choice = 0;
        boolean useSaved = false;
        String accountNumber = "none";

        if (!counter && !user.getSavedPaymentType().equals("none")) {
            if (InputHelper.getYesNo("Use your saved " + user.getSavedPaymentType() + " account? (Y/N): ")) {
                useSaved = true;
                accountNumber = user.getSavedPaymentNumber();
                if (user.getSavedPaymentType().equals("CREDIT")) {
                    choice = 1;
                } else if (user.getSavedPaymentType().equals("DEBIT")) {
                    choice = 2;
                } else {
                    choice = 3;
                }
            }
        }
        if (!useSaved) {
            int max = 3;
            System.out.print("\n1 - Credit card\n2 - Debit card\n3 - E-wallet");
            if (counter) {
                System.out.print("\n4 - Cash");
                max = 4;
            }
            System.out.print("\n");
            choice = InputHelper.getValidInt("--> Payment method: ", 1, max);
        }

        PaymentStrategy payment;
        switch (choice) {
            case 1:
                payment = new CreditCardPayment();
                break;
            case 2:
                payment = new DebitCardPayment();
                break;
            case 3:
                payment = new EWalletPayment();
                break;
            default:
                payment = new CashPayment();
        }
        String[] methods = {"CREDIT", "DEBIT", "EWALLET", "CASH"};

        String result = "";
        boolean done = false;
        boolean firstTry = true;
        while (!done) {
            if (choice != 4 && !(useSaved && firstTry)) {
                // Derive the type string that getValidPaymentNumber expects so
                // the format is validated here -- before it reaches the strategy --
                // giving the user a clear, specific error message right away.
                String[] types = {"CREDIT", "DEBIT", "EWALLET"};
                accountNumber = InputHelper.getValidPaymentNumber("Account number: ", types[choice - 1]);
            }
            firstTry = false;

            try {
                payment.pay(total, accountNumber);
                result = methods[choice - 1];
                done = true;
            } catch (PaymentFailedException e) {
                System.out.println("Error: " + e.getMessage());
                if (!InputHelper.getYesNo("Try again? (Y/N): ")) {
                    System.out.println("Payment cancelled. Nothing was booked.");
                    done = true;
                }
            }
        }
        return result;
    }

    // ----- ticket, cancel, history -----

    public static void printTicket(Booking b) {
        DataStore ds = DataStore.getInstance();
        Schedule s = ds.findScheduleById(b.getScheduleId());
        Movie movie = null;
        Hall hall = null;
        if (s != null) {
            movie = ds.findMovieById(s.getMovieId());
            hall = ds.findHallById(s.getHallId());
        }
        Transaction t = findTransaction(b.getId());

        String title = "Unknown";
        String place = "Unknown";
        String when = "Unknown";
        if (movie != null) {
            title = movie.getTitle() + " (" + s.getFormat() + ")";
        }
        if (hall != null) {
            place = hall.getCinemaLocation() + " / " + hall.getHallName();
        }
        if (s != null) {
            when = s.getDate() + " " + s.getStartTime();
        }

        String border = "+----------------------------------------------------+";
        System.out.println("\n" + border);
        printBoxLine("E-TICKET / RECEIPT");
        System.out.println(border);
        printBoxLine("Ref: " + bookingRef(b.getId()));
        printBoxLine("Movie: " + title);
        printBoxLine("Cinema / Hall: " + place);
        printBoxLine("Date / Time: " + when);
        printBoxLine("Seats: " + b.getSeats());

        boolean hasSnacks = false;
        for (SnackOrder o : ds.getSnackOrders()) {
            if (o.getBookingId() == b.getId()) {
                Snack snack = ds.findSnackById(o.getSnackId());
                String snackName = "Snack " + o.getSnackId();
                if (snack != null) {
                    snackName = snack.getName();
                }
                printBoxLine(String.format("Snack: %s x%d = Php %.2f", snackName, o.getQuantity(), o.getQuantity() * o.getUnitPrice()));
                hasSnacks = true;
            }
        }
        if (!hasSnacks) {
            printBoxLine("Snacks: none");
        }

        // the discount is worked out from the saved amounts (a booking has no discount field)
        String discountText = "None";
        if (t != null && s != null) {
            double off = round2(t.getTicketCount() * s.getTicketPrice() - t.getTicketAmount());
            if (off > 0) {
                discountText = String.format("-Php %.2f on tickets", off);
            }
        }
        printBoxLine("Discount applied: " + discountText);
        printBoxLine(String.format("Total paid: Php %.2f", b.getTotalPaid()));
        if (t != null) {
            printBoxLine("Payment method: " + t.getMethod());
        }
        printBoxLine("Customer: " + b.getCustomerName());
        if (b.getStatus().equals("CANCELLED")) {
            printBoxLine("Status: CANCELLED");
        }
        System.out.println(border);
    }

    private static void printBoxLine(String text) {
        System.out.printf("| %-50.50s |%n", text);
    }

    private static Transaction findTransaction(int bookingId) {
        DataStore ds = DataStore.getInstance();

        for (Transaction t : ds.getTransactions()) {
            if (t.getBookingId() == bookingId) {
                return t;
            }
        }
        return null;
    }

    public static void cancelBooking(RegisteredUser u) {
        DataStore ds = DataStore.getInstance();

        try {
            String path = ds.getUserBookingsPath(u.getId());
            ArrayList<Booking> bookings = ds.loadBookings(path);
            ArrayList<Booking> eligible = new ArrayList<>();
            int tooLate = 0;
            for (Booking b : bookings) {
                Schedule s = ds.findScheduleById(b.getScheduleId());
                if (b.getStatus().equals("CONFIRMED") && s != null && isUpcoming(s)) {
                    if (minutesUntilStart(s) >= CANCEL_DEADLINE_MINUTES) {
                        eligible.add(b);
                    } else {
                        tooLate++;
                    }
                }
            }
            if (eligible.size() == 0) {
                if (tooLate > 0) {
                    throw new InvalidInputException("Bookings can only be cancelled at least " + CANCEL_DEADLINE_MINUTES
                            + " minutes before showtime.");
                }
                throw new InvalidInputException("You have no bookings that can be cancelled.");
            }
            if (tooLate > 0) {
                System.out.println("Note: " + tooLate + " booking(s) are less than " + CANCEL_DEADLINE_MINUTES
                        + " minutes from showtime and can no longer be cancelled.");
            }

            System.out.println("\nBookings you can cancel:");
            printBookingTable(eligible);
            int bookingId = InputHelper.getValidInt("Booking number to cancel (the number in BK-000012 is 12): ", 1, 999999);
            Booking chosen = null;
            for (Booking b : eligible) {
                if (b.getId() == bookingId) {
                    chosen = b;
                }
            }
            if (chosen == null) {
                throw new InvalidInputException("That booking is not eligible to cancel.");
            }

            if (InputHelper.getYesNo("Cancel booking " + bookingRef(chosen.getId()) + "? (Y/N): ")) {
                // check the deadline again: time may have passed while the user was deciding
                Schedule s = ds.findScheduleById(chosen.getScheduleId());
                if (s == null || minutesUntilStart(s) < CANCEL_DEADLINE_MINUTES) {
                    throw new InvalidInputException("The cancellation deadline (" + CANCEL_DEADLINE_MINUTES
                            + " minutes before showtime) has passed.");
                }

                chosen.setStatus("CANCELLED");
                ds.saveBookings(path, bookings);

                // the seats stay reserved in the ledger until Customer Service processes
                // the refund (see processRefund), so nobody can rebook them before then
                Transaction t = findTransaction(chosen.getId());
                if (t != null) {
                    t.setStatus("REFUND_PENDING");
                    ds.saveTransactions();
                    System.out.println("Booking cancelled. Your refund is now waiting for Customer Service.");
                    System.out.println("Your seats stay reserved until the refund is processed.");
                } else {
                    // no payment record means there is no refund to wait for
                    releaseSeats(chosen.getId());
                    System.out.println("Booking cancelled.");
                }
            }
        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void showBookingHistory(RegisteredUser u) {
        DataStore ds = DataStore.getInstance();

        System.out.println("\n--- BOOKING HISTORY: " + u.getName() + " ---");
        printBookingTable(ds.loadBookings(ds.getUserBookingsPath(u.getId())));
    }

    private static void printBookingTable(ArrayList<Booking> list) {
        DataStore ds = DataStore.getInstance();

        if (list.size() == 0) {
            System.out.println("No bookings to show.");
        } else {
            String line = "------------------------------------------------------------------------------------------";
            System.out.println(line);
            System.out.printf("%-10s | %-16s | %-10s | %-5s | %-14s | %-10s | %-12s%n",
                    "Ref", "Movie", "Date", "Time", "Seats", "Status", "Total");
            System.out.println(line);
            for (Booking b : list) {
                Schedule s = ds.findScheduleById(b.getScheduleId());
                String title = "Unknown";
                String date = "-";
                String time = "-";
                if (s != null) {
                    Movie m = ds.findMovieById(s.getMovieId());
                    if (m != null) {
                        title = m.getTitle();
                    }
                    date = s.getDate();
                    time = s.getStartTime();
                }
                System.out.printf("%-10s | %-16.16s | %-10s | %-5s | %-14.14s | %-10.10s | Php %-8.2f%n",
                        bookingRef(b.getId()), title, date, time, b.getSeats(), b.getStatus(), b.getTotalPaid());
            }
        }
    }

    // ----- review -----

    // Returns true when the user has at least one CONFIRMED booking for a
    // showing of the given movie whose showtime is already in the past.
    private static boolean hasPastConfirmedBookingFor(RegisteredUser u, int movieId) {
        DataStore ds = DataStore.getInstance();
        ArrayList<Booking> bookings = ds.loadBookings(ds.getUserBookingsPath(u.getId()));

        for (Booking b : bookings) {
            if (!b.getStatus().equals("CONFIRMED")) {
                continue;
            }
            Schedule s = ds.findScheduleById(b.getScheduleId());
            if (s != null && s.getMovieId() == movieId && !isUpcoming(s)) {
                return true;
            }
        }
        return false;
    }

    public static void submitReview(RegisteredUser u) {
        DataStore ds = DataStore.getInstance();

        try {
            printMovieList();
            int movieId = InputHelper.getValidInt("Movie ID to review: ", 1, 999999);
            if (ds.findMovieById(movieId) == null) {
                throw new InvalidInputException("Movie ID not found.");
            }
            if (!hasPastConfirmedBookingFor(u, movieId)) {
                throw new InvalidInputException(
                        "You can only review a movie after attending a confirmed showing of it.");
            }
            int rating = InputHelper.getValidInt("Rating (1-5): ", 1, 5);
            String comment = InputHelper.getValidText("Comment (up to 100 characters, press Enter to skip): ", 0, 100);
            String timestamp = LocalDateTime.now().toString();

            Review existing = null;
            for (Review r : ds.getReviews()) {
                if (r.getUserId() == u.getId() && r.getMovieId() == movieId) {
                    existing = r;
                }
            }
            if (existing == null) {
                ds.getReviews().add(new Review(ds.generateId(), u.getId(), movieId, rating, comment, timestamp));
                System.out.println("Thank you! Your review was saved.");
            } else {
                existing.setRating(rating);
                existing.setComment(comment);
                existing.setTimestamp(timestamp);
                System.out.println("Your earlier review for this movie was replaced.");
            }
            ds.saveReviews();
        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ----- customer service: refunds and a user's bookings -----

    public static void processRefund(CustomerService cs) {
        DataStore ds = DataStore.getInstance();

        try {
            ArrayList<Transaction> pending = new ArrayList<>();
            for (Transaction t : ds.getTransactions()) {
                if (t.getStatus().equals("REFUND_PENDING")) {
                    pending.add(t);
                }
            }
            if (pending.size() == 0) {
                throw new InvalidInputException("There are no refunds waiting.");
            }

            String line = "------------------------------------------------------------------------";
            System.out.println("\n" + line);
            System.out.printf("%-6s | %-10s | %-16s | %-7s | %-12s | %-8s%n", "Txn ID", "Booking", "Owner", "Tickets", "Total", "Method");
            System.out.println(line);
            for (Transaction t : pending) {
                System.out.printf("%-6d | %-10s | %-16.16s | %-7d | Php %-8.2f | %-8s%n",
                        t.getId(), bookingRef(t.getBookingId()), t.getOwnerType(), t.getTicketCount(), t.getTotalAmount(), t.getMethod());
            }

            int id = InputHelper.getValidInt("Transaction ID to refund: ", 1, 999999);
            Transaction chosen = null;
            for (Transaction t : pending) {
                if (t.getId() == id) {
                    chosen = t;
                }
            }
            if (chosen == null) {
                throw new InvalidInputException("Choose a transaction ID from the list.");
            }

            System.out.printf("\n1 - Full refund (Php %.2f)\n2 - Partial refund\n", chosen.getTotalAmount());
            int option = InputHelper.getValidInt("--> Option: ", 1, 2);
            double amount = chosen.getTotalAmount();
            if (option == 2) {
                amount = InputHelper.getValidDouble("Refund amount (0.01-" + String.format("%.2f", chosen.getTotalAmount()) + "): ",
                        0.01, chosen.getTotalAmount());
            }

            chosen.setRefundAmount(round2(amount));
            chosen.setStatus("REFUNDED");
            ds.saveTransactions();
            // the refund is settled, so the cancelled booking's seats can be sold again
            releaseSeats(chosen.getBookingId());
            AuditLogger.log(cs, "PROCESS_REFUND", "txnID=" + chosen.getId() + " bookingID=" + chosen.getBookingId() + " amount=" + round2(amount));
            System.out.printf("Refund of Php %.2f recorded by %s.%n", round2(amount), cs.getName());
        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void viewUserBookings(CustomerService cs) {
        DataStore ds = DataStore.getInstance();

        try {
            String email = InputHelper.getValidEmail("User email: ");
            User found = ds.findUserByEmail(email);
            if (found == null) {
                throw new InvalidInputException("No user was found with that email.");
            }
            if (!(found instanceof RegisteredUser)) {
                throw new InvalidInputException("Only registered users have bookings.");
            }

            System.out.println("\n--- BOOKINGS OF " + found.getName() + " ---");
            printBookingTable(ds.loadBookings(ds.getUserBookingsPath(found.getId())));
        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ----- counter booking -----

    public static void bookAtCounter(Cashier c) {
        bookTickets(true, null);
    }

    // ----- bulk requests -----

    public static void createBulkRequest(CustomerService cs) {
        DataStore ds = DataStore.getInstance();

        try {
            String requester = InputHelper.getValidName("Requester name (2-30 letters, digits, spaces): ", 2, 30);

            System.out.print("\n1 - Email\n2 - Mobile number\n");
            int contactType = InputHelper.getValidInt("--> Contact type: ", 1, 2);
            String contact;
            if (contactType == 1) {
                contact = InputHelper.getValidEmail("Contact email: ");
            } else {
                contact = InputHelper.getValidName("Mobile number (11 digits): ", 11, 11);
                if (!contact.matches("[0-9]{11}")) {
                    throw new InvalidInputException("Mobile number must be exactly 11 digits.");
                }
            }

            ArrayList<Schedule> upcoming = new ArrayList<>();
            for (Schedule s : ds.getSchedules()) {
                if (isUpcoming(s)) {
                    upcoming.add(s);
                }
            }
            if (upcoming.size() == 0) {
                throw new InvalidInputException("There are no upcoming showings.");
            }
            printScheduleList(upcoming);
            int scheduleId = InputHelper.getValidInt("Schedule ID: ", 1, 999999);
            Schedule schedule = null;
            for (Schedule s : upcoming) {
                if (s.getId() == scheduleId) {
                    schedule = s;
                }
            }
            if (schedule == null) {
                throw new InvalidInputException("Choose a schedule ID from the list.");
            }
            Hall hall = ds.findHallById(schedule.getHallId());
            if (hall == null) {
                throw new InvalidInputException("The hall for this schedule was not found.");
            }
            int freeSeats = countFreeSeats(schedule, hall);
            if (freeSeats < 20) {
                throw new InvalidInputException("This showing only has " + freeSeats + " seat(s) available, which is below the minimum of 20 for a bulk request.");
            }
            int maxSeats = Math.min(freeSeats, 200);
            System.out.println("Available seats for this showing: " + freeSeats + ".");
            int seatCount = InputHelper.getValidInt("Number of seats (20-" + maxSeats + "): ", 20, maxSeats);

            ds.getBulkRequests().add(new BulkBookingRequest(ds.generateId(), requester, contact, scheduleId, seatCount, "PENDING", 0));
            ds.saveBulkRequests();
            AuditLogger.log(cs, "CREATE_BULK_REQUEST", "requester=" + requester + " scheduleID=" + scheduleId + " seats=" + seatCount);
            System.out.println("Bulk request saved as PENDING. An Admin will review it.");
        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void reviewBulkRequests(Admin a) {
        DataStore ds = DataStore.getInstance();

        try {
            ArrayList<BulkBookingRequest> pending = new ArrayList<>();
            for (BulkBookingRequest r : ds.getBulkRequests()) {
                if (r.getStatus().equals("PENDING")) {
                    pending.add(r);
                }
            }
            if (pending.size() == 0) {
                throw new InvalidInputException("There are no pending bulk requests.");
            }

            String line = "------------------------------------------------------------------------------";
            System.out.println("\n" + line);
            System.out.printf("%-4s | %-18s | %-26s | %-8s | %-5s%n", "ID", "Requester", "Contact", "Schedule", "Seats");
            System.out.println(line);
            for (BulkBookingRequest r : pending) {
                System.out.printf("%-4d | %-18.18s | %-26.26s | %-8d | %-5d%n",
                        r.getId(), r.getRequesterName(), r.getContactInfo(), r.getScheduleId(), r.getSeatCount());
            }

            int id = InputHelper.getValidInt("Request ID: ", 1, 999999);
            BulkBookingRequest request = null;
            for (BulkBookingRequest r : pending) {
                if (r.getId() == id) {
                    request = r;
                }
            }
            if (request == null) {
                throw new InvalidInputException("Choose a request ID from the list.");
            }

            System.out.print("\n1 - Approve\n2 - Reject\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 2);

            if (option == 2) {
                request.setStatus("REJECTED");
                request.setReviewerId(a.getId());
                ds.saveBulkRequests();
                AuditLogger.log(a, "REJECT_BULK", "requestID=" + request.getId() + " requester=" + request.getRequesterName());
                System.out.println("Request rejected.");
            } else {
                Schedule schedule = ds.findScheduleById(request.getScheduleId());
                if (schedule == null) {
                    throw new InvalidInputException("The schedule for this request no longer exists.");
                }
                ArrayList<String> seats = findFreeSeats(schedule, request.getSeatCount());
                if (seats.size() < request.getSeatCount()) {
                    throw new SeatUnavailableException("Only " + seats.size() + " seats are free, but " + request.getSeatCount()
                            + " are needed. The request stays PENDING.");
                }

                double subtotal = round2(schedule.getTicketPrice() * seats.size());
                DiscountStrategy discount = new BulkDiscount(request.getSeatCount());
                double total = discount.apply(subtotal);

                System.out.printf("\nBulk tickets (%d seats): Php %.2f%n", seats.size(), subtotal);
                System.out.printf("Bulk discount (15%%): -Php %.2f%n", round2(subtotal - total));
                System.out.printf("TOTAL DUE: Php %.2f%n", total);

                String method = takePayment(total, true, null);
                if (method.equals("")) {
                    System.out.println("Payment cancelled. Request stays PENDING.");
                    return;
                }

                // Reserve both IDs (bookingId + transactionId) in a single write
                // before touching any file, so a mid-crash cannot create a duplicate.
                int[] bulkIds      = ds.generateIdBlock(2);
                int bookingId      = bulkIds[0];
                int transactionId  = bulkIds[1];
                String timestamp = LocalDateTime.now().toString();

                String seatsText = "";
                for (int i = 0; i < seats.size(); i++) {
                    if (i > 0) {
                        seatsText += ",";
                    }
                    seatsText += seats.get(i);
                    ds.getLedger().add(new SeatLedgerEntry(schedule.getId(), seats.get(i), bookingId));
                }
                ds.saveLedger();

                Booking booking = new Booking(bookingId, "BULK", 0, request.getRequesterName(), schedule.getId(),
                        seatsText, "CONFIRMED", total, timestamp);
                String path = ds.getCounterBookingsPath();
                ArrayList<Booking> bookings = ds.loadBookings(path);
                bookings.add(booking);
                ds.saveBookings(path, bookings);

                ds.getTransactions().add(new Transaction(transactionId, bookingId, "BULK", 0, schedule.getId(),
                        seats.size(), total, 0.0, total, method, "SUCCESS", 0.0, timestamp));
                ds.saveTransactions();

                request.setStatus("APPROVED");
                request.setReviewerId(a.getId());
                ds.saveBulkRequests();
                AuditLogger.log(a, "APPROVE_BULK", "requestID=" + request.getId() + " booking=" + bookingRef(bookingId) + " seats=" + seats.size() + " method=" + method);

                System.out.println("Request approved. Booking " + bookingRef(bookingId) + " was created.");
                printTicket(booking);
            }
        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
