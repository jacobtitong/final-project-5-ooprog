import java.util.ArrayList;

public class ReportManager {
    public static void printRevenueReport() {
        DataStore ds = DataStore.getInstance();

        try {
            String start = InputHelper.getValidDate("Start date (YYYY-MM-DD): ");
            String end = InputHelper.getValidDate("End date (YYYY-MM-DD): ");
            if (end.compareTo(start) < 0) {
                throw new InvalidInputException("The end date cannot be before the start date.");
            }

            double totalRevenue = 0;
            ArrayList<Integer> movieIds = new ArrayList<>();
            ArrayList<Integer> movieTickets = new ArrayList<>();
            ArrayList<Double> movieRevenue = new ArrayList<>();
            ArrayList<Integer> snackIds = new ArrayList<>();
            ArrayList<Integer> snackQuantities = new ArrayList<>();
            ArrayList<Double> snackRevenue = new ArrayList<>();

            // only SUCCESS transactions inside the date range count
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
                }
            }

            // snack orders count when their booking's transaction is counted
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

            System.out.println("\n=== REVENUE REPORT: " + start + " to " + end + " ===");
            System.out.printf("Total revenue: Php %.2f%n", BookingManager.round2(totalRevenue));

            System.out.println("\n--- Revenue by movie ---");
            if (movieIds.size() == 0) {
                System.out.println("No sales in this period.");
            } else {
                System.out.printf("%-24s | %-8s | %-12s%n", "Movie", "Tickets", "Revenue");
                for (int i = 0; i < movieIds.size(); i++) {
                    System.out.printf("%-24.24s | %-8d | Php %-8.2f%n",
                            movieTitle(movieIds.get(i)), movieTickets.get(i), BookingManager.round2(movieRevenue.get(i)));
                }
            }

            System.out.println("\n--- Revenue by snack ---");
            if (snackIds.size() == 0) {
                System.out.println("No snack sales in this period.");
            } else {
                System.out.printf("%-24s | %-8s | %-12s%n", "Snack", "Quantity", "Revenue");
                for (int i = 0; i < snackIds.size(); i++) {
                    System.out.printf("%-24.24s | %-8d | Php %-8.2f%n",
                            snackName(snackIds.get(i)), snackQuantities.get(i), BookingManager.round2(snackRevenue.get(i)));
                }
            }

            // best sellers: sort each pair of lists, then print the top 5
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

    private static boolean isCounted(Transaction t, String start, String end) {
        boolean counted = false;

        if (t.getStatus().equals("SUCCESS") && t.getTimestamp().length() >= 10) {
            String date = t.getTimestamp().substring(0, 10);
            if (date.compareTo(start) >= 0 && date.compareTo(end) <= 0) {
                counted = true;
            }
        }
        return counted;
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

    // nested-for selection sort, biggest count first; the three lists move together
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

    private static void printTopFive(ArrayList<Integer> ids, ArrayList<Integer> counts, ArrayList<Double> revenue, boolean isMovie) {
        if (ids.size() == 0) {
            System.out.println("No sales in this period.");
        } else {
            System.out.printf("%-4s | %-24s | %-8s | %-12s%n", "Rank", "Name", "Sold", "Revenue");
            int limit = Math.min(5, ids.size());
            for (int i = 0; i < limit; i++) {
                String name = snackName(ids.get(i));
                if (isMovie) {
                    name = movieTitle(ids.get(i));
                }
                System.out.printf("%-4d | %-24.24s | %-8d | Php %-8.2f%n",
                        i + 1, name, counts.get(i), BookingManager.round2(revenue.get(i)));
            }
        }
    }
}
