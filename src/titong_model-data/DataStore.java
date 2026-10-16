import java.util.ArrayList;

public class DataStore {
    private static DataStore instance;

    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Movie> movies = new ArrayList<>();
    private ArrayList<Hall> halls = new ArrayList<>();
    private ArrayList<Schedule> schedules = new ArrayList<>();
    private ArrayList<SeatLedgerEntry> ledger = new ArrayList<>();
    private ArrayList<Transaction> transactions = new ArrayList<>();
    private ArrayList<Snack> snacks = new ArrayList<>();
    private ArrayList<SnackOrder> snackOrders = new ArrayList<>();
    private ArrayList<Discount> discounts = new ArrayList<>();
    private ArrayList<BulkBookingRequest> bulkRequests = new ArrayList<>();
    private ArrayList<Review> reviews = new ArrayList<>();
    private ArrayList<SystemConfig> configs = new ArrayList<>();

    private DataStore() {
        loadAll();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private void loadAll() {
    }

    public ArrayList<User> getUsers() { return users; }
    public ArrayList<Movie> getMovies() { return movies; }
    public ArrayList<Hall> getHalls() { return halls; }
    public ArrayList<Schedule> getSchedules() { return schedules; }
    public ArrayList<SeatLedgerEntry> getLedger() { return ledger; }
    public ArrayList<Transaction> getTransactions() { return transactions; }
    public ArrayList<Snack> getSnacks() { return snacks; }
    public ArrayList<SnackOrder> getSnackOrders() { return snackOrders; }
    public ArrayList<Discount> getDiscounts() { return discounts; }
    public ArrayList<BulkBookingRequest> getBulkRequests() { return bulkRequests; }
    public ArrayList<Review> getReviews() { return reviews; }

    public int generateId() {
        return 0;
    }

    public int getConfigInt(String key) {
        return 0;
    }

    public void setConfig(String key, String value) {
    }

    public void saveUsers() {
    }

    public void saveMovies() {
    }

    public void saveHalls() {
    }

    public void saveSchedules() {
    }

    public void saveLedger() {
    }

    public void saveTransactions() {
    }

    public void saveSnacks() {
    }

    public void saveSnackOrders() {
    }

    public void saveDiscounts() {
    }

    public void saveBulkRequests() {
    }

    public void saveReviews() {
    }

    public String getUserBookingsPath(int userId) {
        return "";
    }

    public String getCounterBookingsPath() {
        return "";
    }

    public void createUserBookingsFile(int userId) {
    }

    public ArrayList<Booking> loadBookings(String path) {
        return new ArrayList<Booking>();
    }

    public void saveBookings(String path, ArrayList<Booking> list) {
    }

    public User findUserByEmail(String email) {
        return null;
    }

    public Movie findMovieById(int id) {
        return null;
    }

    public Schedule findScheduleById(int id) {
        return null;
    }

    public Hall findHallById(int id) {
        return null;
    }

    public Snack findSnackById(int id) {
        return null;
    }
}
