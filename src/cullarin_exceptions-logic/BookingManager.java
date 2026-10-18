public class BookingManager {
    public static int toMinutes(String hhmm) {
        return 0; // stub: filled in Step 3
    }

    public static String toTimeString(int minutes) {
        return ""; // stub: filled in Step 3
    }

    public static String today() {
        return ""; // stub: filled in Step 3
    }

    public static int nowMinutes() {
        return 0; // stub: filled in Step 3
    }

    public static double round2(double x) {
        return x; // stub: filled in Step 3
    }

    public static int calculateAge(String birthdate) {
        return 0; // stub: filled in Step 3
    }

    public static void checkOverlap(int userId, Schedule s) throws TimeOverlapException {
        // stub: filled in Step 4
    }

    public static void checkScheduleConflict(Schedule s, int ignoreScheduleId) throws ScheduleConflictException {
        // stub: filled in Step 4
    }

    public static void printSeatMap(Schedule s) {
        // stub: filled in Step 5
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
