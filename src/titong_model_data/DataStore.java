package titong_model_data;
import peco_users_menus.Admin;
import peco_users_menus.Cashier;
import peco_users_menus.CustomerService;
import peco_users_menus.RegisteredUser;
import peco_users_menus.SnackManager;
import peco_users_menus.User;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import titong_model_data.AuditLog;

public class DataStore {
    private static volatile DataStore instance;

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
    private ArrayList<AuditLog> auditLog = new ArrayList<>();
    private static final String FILE_AUDIT = "audit_log.txt";

    // Tracks the last-modified timestamp of each file when it was last loaded.
    // If the file's current lastModified() differs from what is stored here,
    // the corresponding collection is reloaded before it is returned to callers.
    private final HashMap<String, Long> fileTimestamps = new HashMap<>();

    private static final String FILE_CONFIG       = "system_config.txt";
    private static final String FILE_USERS        = "users.txt";
    private static final String FILE_MOVIES       = "movies.txt";
    private static final String FILE_HALLS        = "halls.txt";
    private static final String FILE_SCHEDULES    = "schedules.txt";
    private static final String FILE_LEDGER       = "seat_ledger.txt";
    private static final String FILE_TRANSACTIONS = "transactions.txt";
    private static final String FILE_SNACKS       = "snacks.txt";
    private static final String FILE_SNACK_ORDERS = "snack_orders.txt";
    private static final String FILE_DISCOUNTS    = "discounts_promos.txt";
    private static final String FILE_BULK         = "bulk_requests.txt";
    private static final String FILE_REVIEWS      = "reviews.txt";

    private DataStore() {
        loadAll();
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            synchronized (DataStore.class) {
                if (instance == null) {
                    instance = new DataStore();
                }
            }
        }
        return instance;
    }

    // loads every file once, when the single DataStore is created
    private void loadAll() {
        loadConfig();
        loadUsers();
        loadMovies();
        loadHalls();
        loadSchedules();
        loadLedger();
        loadTransactions();
        loadSnacks();
        loadSnackOrders();
        loadDiscounts();
        loadBulkRequests();
        loadReviews();
    }

    // -------------------------------------------------------------------------
    // Stale-data detection helpers
    // -------------------------------------------------------------------------

    /** Records the current last-modified timestamp for a file. */
    private void stampFile(String path) {
        fileTimestamps.put(path, new File(path).lastModified());
    }

    /**
     * Returns true when the file on disk has been modified since it was last
     * loaded into memory (i.e. its lastModified() timestamp has changed).
     */
    private boolean hasChanged(String path) {
        long onDisk = new File(path).lastModified();
        Long loaded = fileTimestamps.get(path);
        return loaded == null || onDisk != loaded;
    }

    // -------------------------------------------------------------------------
    // Accessors — each checks whether its backing file changed and reloads if so
    // -------------------------------------------------------------------------

    public ArrayList<User> getUsers() {
        if (hasChanged(FILE_USERS)) {
            users.clear();
            loadUsers();
        }
        return users;
    }

    public ArrayList<Movie> getMovies() {
        if (hasChanged(FILE_MOVIES)) {
            movies.clear();
            loadMovies();
        }
        return movies;
    }

    public ArrayList<Hall> getHalls() {
        if (hasChanged(FILE_HALLS)) {
            halls.clear();
            loadHalls();
        }
        return halls;
    }

    public ArrayList<Schedule> getSchedules() {
        if (hasChanged(FILE_SCHEDULES)) {
            schedules.clear();
            loadSchedules();
        }
        return schedules;
    }

    public ArrayList<SeatLedgerEntry> getLedger() {
        if (hasChanged(FILE_LEDGER)) {
            ledger.clear();
            loadLedger();
        }
        return ledger;
    }

    public ArrayList<Transaction> getTransactions() {
        if (hasChanged(FILE_TRANSACTIONS)) {
            transactions.clear();
            loadTransactions();
        }
        return transactions;
    }

    public ArrayList<Snack> getSnacks() {
        if (hasChanged(FILE_SNACKS)) {
            snacks.clear();
            loadSnacks();
        }
        return snacks;
    }

    public ArrayList<SnackOrder> getSnackOrders() {
        if (hasChanged(FILE_SNACK_ORDERS)) {
            snackOrders.clear();
            loadSnackOrders();
        }
        return snackOrders;
    }

    public ArrayList<Discount> getDiscounts() {
        if (hasChanged(FILE_DISCOUNTS)) {
            discounts.clear();
            loadDiscounts();
        }
        return discounts;
    }

    public ArrayList<BulkBookingRequest> getBulkRequests() {
        if (hasChanged(FILE_BULK)) {
            bulkRequests.clear();
            loadBulkRequests();
        }
        return bulkRequests;
    }

    public ArrayList<Review> getReviews() {
        if (hasChanged(FILE_REVIEWS)) {
            reviews.clear();
            loadReviews();
        }
        return reviews;
    }

    // ids and system config

    /**
     * Reserves a single unique ID and persists the new counter immediately.
     * Use this only for standalone, single-record operations (e.g. adding a
     * movie, hall, or schedule) where exactly one ID is needed and the record
     * is written to disk in the same statement.
     */
    public int generateId() {
        return generateIdBlock(1)[0];
    }

    /**
     * Reserves a contiguous block of {@code count} unique IDs and persists
     * the updated counter to disk exactly once for the whole block.
     *
     * Callers that need several IDs in one logical operation (e.g. a booking
     * that requires a bookingId, a transactionId, and N snack-order IDs)
     * should call this method once before writing anything.  That way a crash
     * between writes can only leave the ID counter slightly ahead of the
     * highest-used ID -- it cannot produce a duplicate ID, because the full
     * range is committed to disk before any record is written.
     *
     * @param count number of consecutive IDs required (must be >= 1)
     * @return int[] of length {@code count} with consecutive IDs starting at
     *         the old NEXT_ID value
     */
    public int[] generateIdBlock(int count) {
        if (count < 1) {
            count = 1;
        }
        int first = getConfigInt("NEXT_ID");
        // Persist the new ceiling in a single write before any record is saved.
        setConfig("NEXT_ID", String.valueOf(first + count));
        int[] ids = new int[count];
        for (int i = 0; i < count; i++) {
            ids[i] = first + i;
        }
        return ids;
    }

    public int getConfigInt(String key) {
        if (hasChanged(FILE_CONFIG)) {
            configs.clear();
            loadConfig();
        }
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
        File file = new File(FILE_CONFIG);
        if (!file.exists()) {
            configs.add(new SystemConfig("TICKET_LIMIT_PER_TRANSACTION", "8"));
            configs.add(new SystemConfig("GRACE_PERIOD_BEFORE_MINUTES", "15"));
            configs.add(new SystemConfig("GRACE_PERIOD_AFTER_MINUTES", "45"));
            configs.add(new SystemConfig("LOW_STOCK_THRESHOLD", "10"));
            configs.add(new SystemConfig("NEXT_ID", "1"));
            saveConfig();
        } else {
            try {
                BufferedReader br = new BufferedReader(new FileReader(FILE_CONFIG));
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
        stampFile(FILE_CONFIG);
    }

    private void saveConfig() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_CONFIG));
            for (SystemConfig c : configs) {
                pw.println(c.toFileLine());
            }
            pw.close();
            stampFile(FILE_CONFIG);
        } catch (IOException e) {
            System.out.println("Error: could not save system_config.txt");
        }
    }

    // users
    private void loadUsers() {
        File file = new File(FILE_USERS);
        if (!file.exists()) {
            users.add(new Admin(generateId(), "Admin", "admin@cinema.com", "Admin123", true));
            users.add(new Cashier(generateId(), "Cashier", "cashier@cinema.com", "Cashier123", true));
            users.add(new CustomerService(generateId(), "Customer Service", "service@cinema.com", "Service123", true));
            users.add(new SnackManager(generateId(), "Snack Manager", "snacks@cinema.com", "Snacks123", true));
            saveUsers();
        } else {
            try {
                BufferedReader br = new BufferedReader(new FileReader(FILE_USERS));
                String line = br.readLine();
                while (line != null) {
                    String[] p = line.split(";");
                    int id = Integer.parseInt(p[0]);
                    String role = p[1];
                    boolean active = p[5].equals("true");
                    if (role.equals("REGISTERED_USER")) {
                        users.add(new RegisteredUser(id, p[2], p[3], p[4], active, p[6], p[7], p[8]));
                    } else if (role.equals("CASHIER")) {
                        users.add(new Cashier(id, p[2], p[3], p[4], active));
                    } else if (role.equals("CUSTOMER_SERVICE")) {
                        users.add(new CustomerService(id, p[2], p[3], p[4], active));
                    } else if (role.equals("SNACK_MANAGER")) {
                        users.add(new SnackManager(id, p[2], p[3], p[4], active));
                    } else if (role.equals("ADMIN")) {
                        users.add(new Admin(id, p[2], p[3], p[4], active));
                    }
                    line = br.readLine();
                }
                br.close();
            } catch (IOException e) {
                // could not read users.txt
            }
        }
        stampFile(FILE_USERS);
    }

    public void saveUsers() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_USERS));
            for (User u : users) {
                String role = "";
                String birthdate = "none";
                String paymentType = "none";
                String paymentNumber = "none";
                String className = u.getClass().getSimpleName();

                if (className.equals("RegisteredUser")) {
                    RegisteredUser r = (RegisteredUser) u;
                    role = "REGISTERED_USER";
                    birthdate = r.getBirthdate();
                    paymentType = r.getSavedPaymentType();
                    paymentNumber = r.getSavedPaymentNumber();
                } else if (className.equals("Cashier")) {
                    role = "CASHIER";
                } else if (className.equals("CustomerService")) {
                    role = "CUSTOMER_SERVICE";
                } else if (className.equals("SnackManager")) {
                    role = "SNACK_MANAGER";
                } else if (className.equals("Admin")) {
                    role = "ADMIN";
                }

                pw.println(u.getId() + ";" + role + ";" + u.getName() + ";" + u.getEmail() + ";"
                        + u.getPassword() + ";" + u.isActive() + ";" + birthdate + ";" + paymentType + ";"
                        + paymentNumber);
            }
            pw.close();
            stampFile(FILE_USERS);
        } catch (IOException e) {
            System.out.println("Error: could not save users.txt");
        }
    }

    // movies.txt
    private void loadMovies() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_MOVIES));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                movies.add(new Movie(Integer.parseInt(p[0]), p[1], p[2],
                        p[3], p[4], p[5],
                        Integer.parseInt(p[6])));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_MOVIES);
    }

    public void saveMovies() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_MOVIES));
            for (Movie m : movies) {
                pw.println(m.toFileLine());
            }
            pw.close();
            stampFile(FILE_MOVIES);
        } catch (IOException e) {
            System.out.println("Error: could not save movies.txt");
        }
    }

    // halls.txt
    private void loadHalls() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_HALLS));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                halls.add(new Hall(Integer.parseInt(p[0]), p[1], p[2],
                        Integer.parseInt(p[3]), Integer.parseInt(p[4])));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_HALLS);
    }

    public void saveHalls() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_HALLS));
            for (Hall h : halls) {
                pw.println(h.toFileLine());
            }
            pw.close();
            stampFile(FILE_HALLS);
        } catch (IOException e) {
            System.out.println("Error: could not save halls.txt");
        }
    }

    // schedules.txt
    private void loadSchedules() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_SCHEDULES));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                schedules.add(new Schedule(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                        p[3], p[4], p[5],
                        Double.parseDouble(p[6])));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_SCHEDULES);
    }

    public void saveSchedules() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_SCHEDULES));
            for (Schedule s : schedules) {
                pw.println(s.toFileLine());
            }
            pw.close();
            stampFile(FILE_SCHEDULES);
        } catch (IOException e) {
            System.out.println("Error: could not save schedules.txt");
        }
    }

    // seat_ledger.txt
    private void loadLedger() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_LEDGER));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                ledger.add(new SeatLedgerEntry(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2])));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_LEDGER);
    }

    public void saveLedger() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_LEDGER));
            for (SeatLedgerEntry entry : ledger) {
                pw.println(entry.toFileLine());
            }
            pw.close();
            stampFile(FILE_LEDGER);
        } catch (IOException e) {
            System.out.println("Error: could not save seat_ledger.txt");
        }
    }

    // transactions.txt
    private void loadTransactions() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_TRANSACTIONS));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                transactions.add(new Transaction(Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[2],
                        Integer.parseInt(p[3]), Integer.parseInt(p[4]), Integer.parseInt(p[5]),
                        Double.parseDouble(p[6]), Double.parseDouble(p[7]), Double.parseDouble(p[8]),
                        p[9], p[10], Double.parseDouble(p[11]),
                        p[12]));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_TRANSACTIONS);
    }

    public void saveTransactions() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_TRANSACTIONS));
            for (Transaction t : transactions) {
                pw.println(t.toFileLine());
            }
            pw.close();
            stampFile(FILE_TRANSACTIONS);
        } catch (IOException e) {
            System.out.println("Error: could not save transactions.txt");
        }
    }

    // snacks.txt
    private void loadSnacks() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_SNACKS));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                snacks.add(new Snack(Integer.parseInt(p[0]), p[1], Double.parseDouble(p[2]),
                        Integer.parseInt(p[3]), p[4].equals("true")));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_SNACKS);
    }

    public void saveSnacks() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_SNACKS));
            for (Snack sn : snacks) {
                pw.println(sn.toFileLine());
            }
            pw.close();
            stampFile(FILE_SNACKS);
        } catch (IOException e) {
            System.out.println("Error: could not save snacks.txt");
        }
    }

    // snack_orders.txt
    private void loadSnackOrders() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_SNACK_ORDERS));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                snackOrders.add(new SnackOrder(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]), Double.parseDouble(p[4])));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_SNACK_ORDERS);
    }

    public void saveSnackOrders() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_SNACK_ORDERS));
            for (SnackOrder so : snackOrders) {
                pw.println(so.toFileLine());
            }
            pw.close();
            stampFile(FILE_SNACK_ORDERS);
        } catch (IOException e) {
            System.out.println("Error: could not save snack_orders.txt");
        }
    }

    // discounts_promos.txt
    private void loadDiscounts() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_DISCOUNTS));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                discounts.add(new Discount(p[0], p[1], Double.parseDouble(p[2]),
                        p[3].equals("true"), p[4], p[5].equals("true")));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_DISCOUNTS);
    }

    public void saveDiscounts() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_DISCOUNTS));
            for (Discount d : discounts) {
                pw.println(d.toFileLine());
            }
            pw.close();
            stampFile(FILE_DISCOUNTS);
        } catch (IOException e) {
            System.out.println("Error: could not save discounts_promos.txt");
        }
    }

    // bulk_requests.txt
    private void loadBulkRequests() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_BULK));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                bulkRequests.add(new BulkBookingRequest(Integer.parseInt(p[0]), p[1], p[2],
                        Integer.parseInt(p[3]), Integer.parseInt(p[4]), p[5],
                        Integer.parseInt(p[6])));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_BULK);
    }

    public void saveBulkRequests() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_BULK));
            for (BulkBookingRequest b : bulkRequests) {
                pw.println(b.toFileLine());
            }
            pw.close();
            stampFile(FILE_BULK);
        } catch (IOException e) {
            System.out.println("Error: could not save bulk_requests.txt");
        }
    }

    // reviews.txt
    private void loadReviews() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_REVIEWS));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                reviews.add(new Review(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]), p[4], p[5]));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        stampFile(FILE_REVIEWS);
    }

    public void saveReviews() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE_REVIEWS));
            for (Review r : reviews) {
                pw.println(r.toFileLine());
            }
            pw.close();
            stampFile(FILE_REVIEWS);
        } catch (IOException e) {
            System.out.println("Error: could not save reviews.txt");
        }
    }

    // booking files (one per registered user + counter_bookings.txt)
    public String getUserBookingsPath(int userId) {
        return "bookings_" + userId + ".txt";
    }

    public String getCounterBookingsPath() {
        return "counter_bookings.txt";
    }

    public void createUserBookingsFile(int userId) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(getUserBookingsPath(userId)));
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not create " + getUserBookingsPath(userId));
        }
    }

    public ArrayList<Booking> loadBookings(String path) {
        ArrayList<Booking> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split(";");
                list.add(new Booking(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]),
                        p[3], Integer.parseInt(p[4]), p[5],
                        p[6], Double.parseDouble(p[7]), p[8]));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // file not created yet: start with an empty list
        }
        return list;
    }

    public void saveBookings(String path, ArrayList<Booking> list) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(path));
            for (Booking b : list) {
                pw.println(b.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save " + path);
        }
    }

    // find methods return null when nothing matches
    public User findUserByEmail(String email) {
        for (User u : getUsers()) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    public Movie findMovieById(int id) {
        for (Movie m : getMovies()) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    public Schedule findScheduleById(int id) {
        for (Schedule s : getSchedules()) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    public Hall findHallById(int id) {
        for (Hall h : getHalls()) {
            if (h.getId() == id) {
                return h;
            }
        }
        return null;
    }

    public Snack findSnackById(int id) {
        for (Snack sn : getSnacks()) {
            if (sn.getId() == id) {
                return sn;
            }
        }
        return null;
    }

    public String getAuditLogPath() { return FILE_AUDIT; }

    public ArrayList<AuditLog> getAuditLog() {
        auditLog.clear();
        loadAuditLog();
        return auditLog;
    }

    private void loadAuditLog() {
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(FILE_AUDIT));
            String line = br.readLine();
            while (line != null) {
                String[] p = line.split("\\|", 6);
                if (p.length == 6) {
                    auditLog.add(new AuditLog(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4], p[5]));
                }
                line = br.readLine();
            }
            br.close();
        } catch (java.io.IOException e) {
            // file not created yet: start with an empty list
        }
    }
}
