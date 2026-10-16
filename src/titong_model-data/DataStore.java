import java.util.ArrayList;

public class DataStore {
    public static DataStore getInstance() {
        return null;
    }

    public ArrayList<User> getUsers() {
        return new ArrayList<User>();
    }
    public ArrayList<Movie> getMovies() {
        return new ArrayList<Movie>();
    }
    public ArrayList<Hall> getHalls() {
        return new ArrayList<Hall>(); 
    }
    public ArrayList<Schedule> getSchedules() {
        return new ArrayList<Schedule>(); 
    }
    public ArrayList<SeatLedgerEntry> getLedger() {
        return new ArrayList<SeatLedgerEntry>(); 
    }
    public ArrayList<Transaction> getTransactions() {
        return new ArrayList<Transaction>(); 
    }
    public ArrayList<Snack> getSnacks() {
        return new ArrayList<Snack>(); 
    }
    public ArrayList<SnackOrder> getSnackOrders() {
        return new ArrayList<SnackOrder>(); 
    }
    public ArrayList<Discount> getDiscounts() {
        return new ArrayList<Discount>(); 
    }
    public ArrayList<BulkBookingRequest> getBulkRequests() {
        return new ArrayList<BulkBookingRequest>(); 
    }
    public ArrayList<Review> getReviews() {
        return new ArrayList<Review>(); 
    }

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
