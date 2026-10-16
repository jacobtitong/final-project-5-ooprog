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
        loadUsers();
        loadMovies();
        loadHalls();
        loadSchedules();
        loadLedger();
        loadTransactions();
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

    // users
    private void loadUsers() {
        File file = new File("users.txt");
        if (!file.exists()) {
            users.add(new Admin(generateId(), "Admin", "admin@cinema.com", "Admin123", true));
            users.add(new Cashier(generateId(), "Cashier", "cashier@cinema.com", "Cashier123", true));
            users.add(new CustomerService(generateId(), "Customer Service", "service@cinema.com", "Service123", true));
            users.add(new SnackManager(generateId(), "Snack Manager", "snacks@cinema.com", "Snacks123", true));
            saveUsers();
        } else {
            try {
                BufferedReader br = new BufferedReader(new FileReader("users.txt"));
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
    }

    public void saveUsers() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("users.txt"));
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
        } catch (IOException e) {
            System.out.println("Error: could not save users.txt");
        }
    }

    // movies.txt
    private void loadMovies() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("movies.txt"));
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
    }

    public void saveMovies() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("movies.txt"));
            for (Movie m : movies) {
                pw.println(m.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save movies.txt");
        }
    }

    // halls.txt
    private void loadHalls() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("halls.txt"));
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
    }

    public void saveHalls() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("halls.txt"));
            for (Hall h : halls) {
                pw.println(h.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save halls.txt");
        }
    }

    // schedules.txt
    private void loadSchedules() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("schedules.txt"));
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
    }

    public void saveSchedules() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("schedules.txt"));
            for (Schedule s : schedules) {
                pw.println(s.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save schedules.txt");
        }
    }

    // seat_ledger.txt
    private void loadLedger() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("seat_ledger.txt"));
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
    }

    public void saveLedger() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("seat_ledger.txt"));
            for (SeatLedgerEntry entry : ledger) {
                pw.println(entry.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save seat_ledger.txt");
        }
    }

    // transactions.txt
    private void loadTransactions() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("transactions.txt"));
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
    }

    public void saveTransactions() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("transactions.txt"));
            for (Transaction t : transactions) {
                pw.println(t.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save transactions.txt");
        }
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
