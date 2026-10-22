package cullarin_exceptions_logic;
import peco_users_menus.InputHelper;
import titong_model_data.DataStore;
import titong_model_data.Movie;
import titong_model_data.Schedule;
import titong_model_data.Snack;
import titong_model_data.SnackOrder;
import titong_model_data.Transaction;

import java.util.ArrayList;

public class ReportManager {

    // Maximum allowed date-range span in days (1 year).
    private static final int MAX_DATE_RANGE_DAYS = 366;

    // Number of detail rows printed before pausing for the user.
    private static final int PAGE_SIZE = 10;

    public static void printRevenueReport() {
        DataStore ds = DataStore.getInstance();

        try {
            String start = InputHelper.getValidDate("Start date (YYYY-MM-DD): ");
            String end   = InputHelper.getValidDate("End date   (YYYY-MM-DD): ");

            if (end.compareTo(start) < 0) {
                throw new InvalidInputException("The end date cannot be before the start date.");
            }

            // Cap the date range to MAX_DATE_RANGE_DAYS so the report stays manageable.
            if (daysBetween(start, end) > MAX_DATE_RANGE_DAYS) {
                throw new InvalidInputException(
                    "The date range cannot exceed " + MAX_DATE_RANGE_DAYS
                    + " days. Please narrow the period and run the report again.");
            }

            double totalRevenue = 0;
            // Refund tracking (for REFUNDED transactions inside the range)
            int    refundCount        = 0;
            double totalRefunds       = 0;
            // Pending-refund tracking (REFUND_PENDING - cancelled but not yet processed)
            int    pendingRefundCount  = 0;
            double totalPendingRefunds = 0;

            ArrayList<Integer> movieIds     = new ArrayList<>();
            ArrayList<Integer> movieTickets = new ArrayList<>();
            ArrayList<Double>  movieRevenue = new ArrayList<>();
            ArrayList<Integer> snackIds     = new ArrayList<>();
            ArrayList<Integer> snackQuantities = new ArrayList<>();
            ArrayList<Double>  snackRevenue = new ArrayList<>();

            // Only SUCCESS transactions inside the date range count toward revenue.
            // REFUNDED transactions inside the range are tallied separately.
            for (Transaction t : ds.getTransactions()) {
                if (isCounted(t, start, end)) {
                    totalRevenue += t.getTotalAmount();

                    Schedule s = ds.findScheduleById(t.getScheduleId());
                    if (s != null) {
                        int index = indexOf(movieIds, s.getMovieId());
                        if (index == -1) {
                            movieIds.add(s.getMovieId());
                            movieTickets.add(t.getTicketCount());
                            movieRevenue.add(t.getTicketAmount());
                        } else {
                            movieTickets.set(index, movieTickets.get(index) + t.getTicketCount());
                            movieRevenue.set(index, movieRevenue.get(index) + t.getTicketAmount());
                        }
                    }
                } else if (isRefunded(t, start, end)) {
                    refundCount++;
                    totalRefunds += t.getRefundAmount();
                } else if (isPendingRefund(t, start, end)) {
                    pendingRefundCount++;
                    totalPendingRefunds += t.getTotalAmount();
                }
            }

            // Snack orders count when their booking's transaction is SUCCESS in range.
            for (SnackOrder o : ds.getSnackOrders()) {
                Transaction t = findTransaction(o.getBookingId());
                if (t != null && isCounted(t, start, end)) {
                    int index = indexOf(snackIds, o.getSnackId());
                    if (index == -1) {
                        snackIds.add(o.getSnackId());
                        snackQuantities.add(o.getQuantity());
                        snackRevenue.add(o.getQuantity() * o.getUnitPrice());
                    } else {
                        snackQuantities.set(index, snackQuantities.get(index) + o.getQuantity());
                        snackRevenue.set(index, snackRevenue.get(index) + o.getQuantity() * o.getUnitPrice());
                    }
                }
            }

            // ----------------------------------------------------------------
            // Print report header
            // ----------------------------------------------------------------
            System.out.println("\n=== REVENUE REPORT: " + start + " to " + end + " ===");
            System.out.printf("Gross revenue  : Php %.2f%n", BookingManager.round2(totalRevenue));

            // ----------------------------------------------------------------
            // Refund summary
            // ----------------------------------------------------------------
            System.out.println("\n--- Refund summary ---");
            double netRevenue = BookingManager.round2(totalRevenue - totalRefunds);
            if (refundCount == 0 && pendingRefundCount == 0) {
                System.out.println("No refunds in this period.");
            } else {
                if (refundCount > 0) {
                    System.out.printf("Refunds processed : %d%n", refundCount);
                    System.out.printf("Total refunded    : Php %.2f%n", BookingManager.round2(totalRefunds));
                }
                if (pendingRefundCount > 0) {
                    System.out.printf("Refunds pending   : %d  (Php %.2f - awaiting CS approval)%n",
                        pendingRefundCount, BookingManager.round2(totalPendingRefunds));
                }
            }
            System.out.printf("Net revenue       : Php %.2f%n", netRevenue);

            // ----------------------------------------------------------------
            // Revenue by movie (paginated)
            // ----------------------------------------------------------------
            System.out.println("\n--- Revenue by movie ---");
            if (movieIds.size() == 0) {
                System.out.println("No sales in this period.");
            } else {
                System.out.printf("%-24s | %-8s | %-12s%n", "Movie", "Tickets", "Revenue");
                printDivider(24 + 3 + 8 + 3 + 12);
                int rowsPrinted = 0;
                for (int i = 0; i < movieIds.size(); i++) {
                    System.out.printf("%-24.24s | %-8d | Php %-8.2f%n",
                            movieTitle(movieIds.get(i)), movieTickets.get(i),
                            BookingManager.round2(movieRevenue.get(i)));
                    rowsPrinted++;
                    if (rowsPrinted % PAGE_SIZE == 0 && rowsPrinted < movieIds.size()) {
                        pause();
                    }
                }
            }

            // ----------------------------------------------------------------
            // Revenue by snack (paginated)
            // ----------------------------------------------------------------
            System.out.println("\n--- Revenue by snack ---");
            if (snackIds.size() == 0) {
                System.out.println("No snack sales in this period.");
            } else {
                System.out.printf("%-24s | %-8s | %-12s%n", "Snack", "Quantity", "Revenue");
                printDivider(24 + 3 + 8 + 3 + 12);
                int rowsPrinted = 0;
                for (int i = 0; i < snackIds.size(); i++) {
                    System.out.printf("%-24.24s | %-8d | Php %-8.2f%n",
                            snackName(snackIds.get(i)), snackQuantities.get(i),
                            BookingManager.round2(snackRevenue.get(i)));
                    rowsPrinted++;
                    if (rowsPrinted % PAGE_SIZE == 0 && rowsPrinted < snackIds.size()) {
                        pause();
                    }
                }
            }

            // ----------------------------------------------------------------
            // Best sellers: sort each pair of lists, then print the top 5
            // ----------------------------------------------------------------
            sortByCount(movieIds, movieTickets, movieRevenue);
            System.out.println("\n--- Best-selling movies (by tickets sold) ---");
            printTopFive(movieIds, movieTickets, movieRevenue, true);

            sortByCount(snackIds, snackQuantities, snackRevenue);
            System.out.println("\n--- Best-selling snacks (by quantity sold) ---");
            printTopFive(snackIds, snackQuantities, snackRevenue, false);

        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Returns true for SUCCESS transactions whose date falls in [start, end]. */
    private static boolean isCounted(Transaction t, String start, String end) {
        if (t.getStatus().equals("SUCCESS") && t.getTimestamp().length() >= 10) {
            String date = t.getTimestamp().substring(0, 10);
            return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
        }
        return false;
    }

    /** Returns true for REFUNDED transactions whose date falls in [start, end]. */
    private static boolean isRefunded(Transaction t, String start, String end) {
        if (t.getStatus().equals("REFUNDED") && t.getTimestamp().length() >= 10) {
            String date = t.getTimestamp().substring(0, 10);
            return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
        }
        return false;
    }

    /** Returns true for REFUND_PENDING transactions whose date falls in [start, end]. */
    private static boolean isPendingRefund(Transaction t, String start, String end) {
        if (t.getStatus().equals("REFUND_PENDING") && t.getTimestamp().length() >= 10) {
            String date = t.getTimestamp().substring(0, 10);
            return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
        }
        return false;
    }

    /**
     * Returns the number of calendar days between two YYYY-MM-DD strings.
     * Uses the same simplified day-count the rest of the project uses (no
     * java.time dependency needed).
     */
    private static int daysBetween(String start, String end) {
        return (int) ((dateToDay(end) - dateToDay(start)));
    }

    /** Converts a YYYY-MM-DD string to a simple day count from a fixed epoch. */
    private static long dateToDay(String date) {
        int y = Integer.parseInt(date.substring(0, 4));
        int m = Integer.parseInt(date.substring(5, 7));
        int d = Integer.parseInt(date.substring(8, 10));
        // Use the proleptic Gregorian formula (Julian Day Number simplified)
        long jdn = (1461L * (y + 4800 + (m - 14) / 12)) / 4
                 + (367L * (m - 2 - 12 * ((m - 14) / 12))) / 12
                 - (3L * ((y + 4900 + (m - 14) / 12) / 100)) / 4
                 + d - 32075;
        return jdn;
    }

    /** Prints a plain horizontal divider of the given width. */
    private static void printDivider(int width) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) {
            sb.append('-');
        }
        System.out.println(sb.toString());
    }

    /** Pauses output and waits for the user to press Enter before continuing. */
    private static void pause() {
        System.out.print("-- Press Enter for more --");
        InputHelper.getOptional("");
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

    private static int indexOf(ArrayList<Integer> ids, int id) {
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == id) {
                return i;
            }
        }
        return -1;
    }

    private static String movieTitle(int movieId) {
        Movie m = DataStore.getInstance().findMovieById(movieId);
        if (m == null) {
            return "Unknown movie";
        }
        return m.getTitle();
    }

    private static String snackName(int snackId) {
        Snack s = DataStore.getInstance().findSnackById(snackId);
        if (s == null) {
            return "Unknown snack";
        }
        return s.getName();
    }

    // Nested-for selection sort, biggest count first; the three lists move together.
    private static void sortByCount(ArrayList<Integer> ids, ArrayList<Integer> counts, ArrayList<Double> revenue) {
        for (int i = 0; i < counts.size() - 1; i++) {
            int best = i;
            for (int j = i + 1; j < counts.size(); j++) {
                if (counts.get(j) > counts.get(best)) {
                    best = j;
                }
            }

            int tempId = ids.get(i);
            ids.set(i, ids.get(best));
            ids.set(best, tempId);

            int tempCount = counts.get(i);
            counts.set(i, counts.get(best));
            counts.set(best, tempCount);

            double tempRevenue = revenue.get(i);
            revenue.set(i, revenue.get(best));
            revenue.set(best, tempRevenue);
        }
    }

    private static void printTopFive(ArrayList<Integer> ids, ArrayList<Integer> counts,
                                     ArrayList<Double> revenue, boolean isMovie) {
        if (ids.size() == 0) {
            System.out.println("No sales in this period.");
        } else {
            System.out.printf("%-4s | %-24s | %-8s | %-12s%n", "Rank", "Name", "Sold", "Revenue");
            int limit = Math.min(5, ids.size());
            for (int i = 0; i < limit; i++) {
                String name = isMovie ? movieTitle(ids.get(i)) : snackName(ids.get(i));
                System.out.printf("%-4d | %-24.24s | %-8d | Php %-8.2f%n",
                        i + 1, name, counts.get(i), BookingManager.round2(revenue.get(i)));
            }
        }
    }
}
