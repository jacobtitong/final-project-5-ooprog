import java.time.LocalDate;
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

    public static void bookOnline(RegisteredUser u) {
        // stub: filled in Step 6
    }

    public static void printTicket(Booking b) {
        // stub: filled in Step 7
    }

    public static void cancelBooking(RegisteredUser u) {
        // stub: filled in Step 7
    }

    public static void showBookingHistory(RegisteredUser u) {
        // stub: filled in Step 7
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
