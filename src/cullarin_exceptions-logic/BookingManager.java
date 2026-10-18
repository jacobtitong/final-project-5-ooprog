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
                    // 8. everything is saved only now
                    String timestamp = LocalDateTime.now().toString();
                    int bookingId = ds.generateId();

                    for (String seat : seats) {
                        ds.getLedger().add(new SeatLedgerEntry(schedule.getId(), seat, bookingId));
                    }
                    ds.saveLedger();

                    Booking booking = new Booking(bookingId, ownerType, ownerId, customerName, schedule.getId(),
                            seatsText, "CONFIRMED", total, timestamp);
                    ArrayList<Booking> bookings = ds.loadBookings(path);
                    bookings.add(booking);
                    ds.saveBookings(path, bookings);

                    ds.getTransactions().add(new Transaction(ds.generateId(), bookingId, ownerType, ownerId, schedule.getId(),
                            seats.size(), discountedTickets, snackTotal, total, method, "SUCCESS", 0.0, timestamp));
                    ds.saveTransactions();

                    for (int i = 0; i < pickedSnacks.size(); i++) {
                        Snack snack = pickedSnacks.get(i);
                        snack.setStock(snack.getStock() - pickedQuantities.get(i));
                        ds.getSnackOrders().add(new SnackOrder(ds.generateId(), bookingId, snack.getId(),
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
            if (choice == 2 && !counter && calculateAge(user.getBirthdate()) < 60) {
                throw new InvalidDiscountException("The Senior discount is only for age 60 and above.");
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
            String code = InputHelper.getValidName("Promo code: ", 4, 12);
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
                accountNumber = InputHelper.getValidName("Account number: ", 1, 30);
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
            for (Booking b : bookings) {
                Schedule s = ds.findScheduleById(b.getScheduleId());
                if (b.getStatus().equals("CONFIRMED") && s != null && isUpcoming(s)) {
                    eligible.add(b);
                }
            }
            if (eligible.size() == 0) {
                throw new InvalidInputException("You have no bookings that can be cancelled.");
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
                chosen.setStatus("CANCELLED");
                ds.saveBookings(path, bookings);

                for (int i = ds.getLedger().size() - 1; i >= 0; i--) {
                    if (ds.getLedger().get(i).getBookingId() == chosen.getId()) {
                        ds.getLedger().remove(i);
                    }
                }
                ds.saveLedger();

                Transaction t = findTransaction(chosen.getId());
                if (t != null) {
                    t.setStatus("REFUND_PENDING");
                    ds.saveTransactions();
                }
                System.out.println("Booking cancelled. Your refund is now waiting for Customer Service.");
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

    public static void submitReview(RegisteredUser u) {
        // stub: filled in Step 8
    }

    public static void processRefund(CustomerService cs) {
        // stub: filled in Step 9
    }

    public static void viewUserBookings(CustomerService cs) {
        // stub: filled in Step 9
    }

    public static void bookAtCounter(Cashier c) {
        // stub: filled in Step 10
    }

    public static void createBulkRequest(CustomerService cs) {
        // stub: filled in Step 11
    }

    public static void reviewBulkRequests(Admin a) {
        // stub: filled in Step 11
    }
}
