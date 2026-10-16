import java.io.*;
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

    // loads every file once, when the single DataStore is created
    private void loadAll() {
        loadConfig();
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

    // ids and system config
    public int generateId() {
        int id = getConfigInt("NEXT_ID");
        setConfig("NEXT_ID", String.valueOf(id + 1));
        return id;
    }

    public int getConfigInt(String key) {
        SystemConfig config = findConfig(key);
        int value = 0;
        if (config != null) {
            try {
                value = Integer.parseInt(config.getValue().trim());
            } catch (NumberFormatException e) {
                value = 0;
            }
        }
        return value;
    }

    public void setConfig(String key, String value) {
        SystemConfig config = findConfig(key);
        if (config == null) {
            configs.add(new SystemConfig(key, value));
        } else {
            config.setValue(value);
        }
        saveConfig();
    }

    private SystemConfig findConfig(String key) {
        for (SystemConfig c : configs) {
            if (c.getKey().equals(key)) {
                return c;
            }
        }
        return null;
    }

    private void loadConfig() {
        File file = new File("system_config.txt");
        if (!file.exists()) {
            configs.add(new SystemConfig("TICKET_LIMIT_PER_TRANSACTION", "8"));
            configs.add(new SystemConfig("GRACE_PERIOD_BEFORE_MINUTES", "15"));
            configs.add(new SystemConfig("GRACE_PERIOD_AFTER_MINUTES", "45"));
            configs.add(new SystemConfig("LOW_STOCK_THRESHOLD", "10"));
            configs.add(new SystemConfig("NEXT_ID", "1"));
            saveConfig();
        } else {
            try {
                BufferedReader br = new BufferedReader(new FileReader("system_config.txt"));
                String line = br.readLine();
                while (line != null) {
                    String[] p = line.split(";");
                    configs.add(new SystemConfig(p[0], p[1]));
                    line = br.readLine();
                }
                br.close();
            } catch (IOException e) {
                // could not read system_config.txt
            }
        }
    }

    private void saveConfig() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("system_config.txt"));
            for (SystemConfig c : configs) {
                pw.println(c.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save system_config.txt");
        }
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
