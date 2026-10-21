public class Admin extends User {
    public Admin(int id, String name, String email, String password, boolean active) {
        super(id, name, email, password, active);
    }

    @Override
    public User displayDashboard() {
        boolean loggedOut = false;
        while (!loggedOut) {
            System.out.print("\n=== ADMIN MENU ===");
            System.out.print("\n1 - Manage movies\n2 - Manage halls\n3 - Manage schedules\n4 - Manage discounts and promos\n5 - Review bulk requests\n6 - Manage user accounts\n7 - Revenue report\n8 - System configuration\n9 - Logout\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 9);

            switch (option) {
                case 1:
                    manageMovies();
                    break;
                case 2:
                    manageHalls();
                    break;
                case 3:
                    manageSchedules();
                    break;
                case 4:
                    manageDiscounts();
                    break;
                case 5:
                    BookingManager.reviewBulkRequests(this);
                    break;
                case 6:
                    manageUsers();
                    break;
                case 7:
                    ReportManager.printRevenueReport();
                    break;
                case 8:
                    manageConfig();
                    break;
                case 9:
                    loggedOut = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-9.");
            }
        }

        return new Guest();
    }

    // ----- movies (Step 11) -----

    private void manageMovies() {
        boolean back = false;
        while (!back) {
            System.out.print("\n=== MANAGE MOVIES ===");
            System.out.print("\n1 - Add a movie\n2 - List movies\n3 - Edit a movie\n4 - Remove a movie\n5 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 5);

            switch (option) {
                case 1:
                    addMovie();
                    break;
                case 2:
                    Guest.viewMovies();
                    break;
                case 3:
                    editMovie();
                    break;
                case 4:
                    removeMovie();
                    break;
                case 5:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-5.");
            }
        }
    }

    private Movie chooseMovie() throws InvalidInputException {
        DataStore ds = DataStore.getInstance();

        int id = InputHelper.getValidInt("Movie ID: ", 1, 999999);
        Movie m = ds.findMovieById(id);
        if (m == null) {
            throw new InvalidInputException("Movie ID not found.");
        }
        return m;
    }

    private String chooseAgeRating() {
        String[] ratings = {"G", "PG", "R13", "R16", "R18"};

        System.out.print("\n1 - G\n2 - PG\n3 - R13\n4 - R16\n5 - R18\n");
        int choice = InputHelper.getValidInt("--> Age rating: ", 1, 5);
        return ratings[choice - 1];
    }

    private void addMovie() {
        DataStore ds = DataStore.getInstance();

        String title = InputHelper.getValidName("Title (1-40 letters, digits, spaces): ", 1, 40);
        String genre = InputHelper.getValidName("Genre (letters, digits, spaces): ", 2, 20);
        String language = InputHelper.getValidName("Language: ", 2, 20);
        String ageRating = chooseAgeRating();
        String synopsis = InputHelper.getValidText("Synopsis (up to 200 characters): ", 1, 200);
        int duration = InputHelper.getValidInt("Duration in minutes (30-300): ", 30, 300);

        ds.getMovies().add(new Movie(ds.generateId(), title, genre, language, ageRating, synopsis, duration));
        ds.saveMovies();
        System.out.println("Movie added.");
    }

    private void editMovie() {
        DataStore ds = DataStore.getInstance();

        try {
            Movie m = chooseMovie();
            boolean done = false;
            while (!done) {
                System.out.print("\n=== EDIT MOVIE ===");
                System.out.print("\n1 - Title\n2 - Genre\n3 - Language\n4 - Age rating\n5 - Synopsis\n6 - Duration\n7 - Done\n");
                int option = InputHelper.getValidInt("--> Option: ", 1, 7);

                switch (option) {
                    case 1:
                        m.setTitle(InputHelper.getValidName("New title (1-40): ", 1, 40));
                        ds.saveMovies();
                        System.out.println("Title updated.");
                        break;
                    case 2:
                        m.setGenre(InputHelper.getValidName("New genre: ", 2, 20));
                        ds.saveMovies();
                        System.out.println("Genre updated.");
                        break;
                    case 3:
                        m.setLanguage(InputHelper.getValidName("New language: ", 2, 20));
                        ds.saveMovies();
                        System.out.println("Language updated.");
                        break;
                    case 4:
                        m.setAgeRating(chooseAgeRating());
                        ds.saveMovies();
                        System.out.println("Age rating updated.");
                        break;
                    case 5:
                        m.setSynopsis(InputHelper.getValidText("New synopsis (up to 200 characters): ", 1, 200));
                        ds.saveMovies();
                        System.out.println("Synopsis updated.");
                        break;
                    case 6:
                        m.setDurationMinutes(InputHelper.getValidInt("New duration in minutes (30-300): ", 30, 300));
                        ds.saveMovies();
                        System.out.println("Duration updated.");
                        break;
                    case 7:
                        done = true;
                        break;
                    default:
                        System.out.println("Error: Invalid option. Please choose 1-7.");
                }
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void removeMovie() {
        DataStore ds = DataStore.getInstance();

        try {
            Movie m = chooseMovie();
            for (Schedule s : ds.getSchedules()) {
                if (s.getMovieId() == m.getId()) {
                    throw new InvalidInputException("Cannot remove: this movie still has schedules.");
                }
            }
            if (InputHelper.getYesNo("Remove " + m.getTitle() + "? (Y/N): ")) {
                ds.getMovies().remove(m);
                ds.saveMovies();
                System.out.println("Movie removed.");
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ----- halls (Step 11) -----

    private void manageHalls() {
        boolean back = false;
        while (!back) {
            System.out.print("\n=== MANAGE HALLS ===");
            System.out.print("\n1 - Add a hall\n2 - List halls\n3 - Edit a hall\n4 - Remove a hall\n5 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 5);

            switch (option) {
                case 1:
                    addHall();
                    break;
                case 2:
                    listHalls();
                    break;
                case 3:
                    editHall();
                    break;
                case 4:
                    removeHall();
                    break;
                case 5:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-5.");
            }
        }
    }

    private Hall chooseHall() throws InvalidInputException {
        DataStore ds = DataStore.getInstance();

        int id = InputHelper.getValidInt("Hall ID: ", 1, 999999);
        Hall h = ds.findHallById(id);
        if (h == null) {
            throw new InvalidInputException("Hall ID not found.");
        }
        return h;
    }

    private void listHalls() {
        DataStore ds = DataStore.getInstance();

        if (ds.getHalls().size() == 0) {
            System.out.println("No halls yet.");
        } else {
            System.out.println("-----------------------------------------------------------------");
            System.out.printf("%-4s | %-20s | %-12s | %-5s | %-7s | %-5s%n", "ID", "Cinema", "Hall", "Rows", "Columns", "Seats");
            System.out.println("-----------------------------------------------------------------");
            for (Hall h : ds.getHalls()) {
                System.out.printf("%-4d | %-20.20s | %-12.12s | %-5d | %-7d | %-5d%n",
                        h.getId(), h.getCinemaLocation(), h.getHallName(), h.getRows(), h.getColumns(), h.getRows() * h.getColumns());
            }
        }
    }

    private void addHall() {
        DataStore ds = DataStore.getInstance();

        String location = InputHelper.getValidName("Cinema location (2-30 letters, digits, spaces): ", 2, 30);
        String hallName = InputHelper.getValidName("Hall name (2-30 letters, digits, spaces): ", 2, 30);
        int rows = InputHelper.getValidInt("Rows (3-12): ", 3, 12);
        int columns = InputHelper.getValidInt("Columns (4-20): ", 4, 20);

        ds.getHalls().add(new Hall(ds.generateId(), location, hallName, rows, columns));
        ds.saveHalls();
        System.out.println("Hall added.");
    }

    // a hall cannot change size while an upcoming showing in it has booked seats
    private void checkHallSizeCanChange(Hall h) throws InvalidInputException {
        DataStore ds = DataStore.getInstance();

        for (Schedule s : ds.getSchedules()) {
            if (s.getHallId() == h.getId() && Guest.isUpcoming(s)) {
                for (SeatLedgerEntry e : ds.getLedger()) {
                    if (e.getScheduleId() == s.getId()) {
                        throw new InvalidInputException("Cannot change the size: seats are already booked for an upcoming showing in this hall.");
                    }
                }
            }
        }
    }

    private void editHall() {
        DataStore ds = DataStore.getInstance();

        try {
            Hall h = chooseHall();
            boolean done = false;
            while (!done) {
                System.out.print("\n=== EDIT HALL ===");
                System.out.print("\n1 - Cinema location\n2 - Hall name\n3 - Rows\n4 - Columns\n5 - Done\n");
                int option = InputHelper.getValidInt("--> Option: ", 1, 5);

                switch (option) {
                    case 1:
                        h.setCinemaLocation(InputHelper.getValidName("New cinema location: ", 2, 30));
                        ds.saveHalls();
                        System.out.println("Cinema location updated.");
                        break;
                    case 2:
                        h.setHallName(InputHelper.getValidName("New hall name: ", 2, 30));
                        ds.saveHalls();
                        System.out.println("Hall name updated.");
                        break;
                    case 3:
                        checkHallSizeCanChange(h);
                        h.setRows(InputHelper.getValidInt("New rows (3-12): ", 3, 12));
                        ds.saveHalls();
                        System.out.println("Rows updated.");
                        break;
                    case 4:
                        checkHallSizeCanChange(h);
                        h.setColumns(InputHelper.getValidInt("New columns (4-20): ", 4, 20));
                        ds.saveHalls();
                        System.out.println("Columns updated.");
                        break;
                    case 5:
                        done = true;
                        break;
                    default:
                        System.out.println("Error: Invalid option. Please choose 1-5.");
                }
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void removeHall() {
        DataStore ds = DataStore.getInstance();

        try {
            Hall h = chooseHall();
            for (Schedule s : ds.getSchedules()) {
                if (s.getHallId() == h.getId()) {
                    throw new InvalidInputException("Cannot remove: this hall is used by a schedule.");
                }
            }
            if (InputHelper.getYesNo("Remove " + h.getHallName() + "? (Y/N): ")) {
                ds.getHalls().remove(h);
                ds.saveHalls();
                System.out.println("Hall removed.");
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ----- schedules (Step 12) -----

    private void manageSchedules() {
        boolean back = false;
        while (!back) {
            System.out.print("\n=== MANAGE SCHEDULES ===");
            System.out.print("\n1 - Add a schedule\n2 - List schedules\n3 - Edit a schedule\n4 - Remove a schedule\n5 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 5);

            switch (option) {
                case 1:
                    addSchedule();
                    break;
                case 2:
                    Guest.printSchedules(DataStore.getInstance().getSchedules(), false);
                    break;
                case 3:
                    editSchedule();
                    break;
                case 4:
                    removeSchedule();
                    break;
                case 5:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-5.");
            }
        }
    }

    private Schedule chooseSchedule() throws InvalidInputException {
        DataStore ds = DataStore.getInstance();

        int id = InputHelper.getValidInt("Schedule ID: ", 1, 999999);
        Schedule s = ds.findScheduleById(id);
        if (s == null) {
            throw new InvalidInputException("Schedule ID not found.");
        }
        return s;
    }

    private String chooseFormat() {
        String[] formats = {"2D", "3D", "IMAX", "4DX"};

        System.out.print("\n1 - 2D\n2 - 3D\n3 - IMAX\n4 - 4DX\n");
        int choice = InputHelper.getValidInt("--> Format: ", 1, 4);
        return formats[choice - 1];
    }

    private void addSchedule() {
        DataStore ds = DataStore.getInstance();

        try {
            Guest.viewMovies();
            Movie m = chooseMovie();
            listHalls();
            Hall h = chooseHall();
            String date = InputHelper.getValidDate("Date (YYYY-MM-DD): ");
            String startTime = InputHelper.getValidTime("Start time (HH:MM, 24-hour): ");
            String format = chooseFormat();
            double price = InputHelper.getValidDouble("Ticket price (50.00-1000.00): ", 50, 1000);

            Schedule test = new Schedule(0, m.getId(), h.getId(), date, startTime, format, price);
            BookingManager.checkScheduleConflict(test, 0);

            ds.getSchedules().add(new Schedule(ds.generateId(), m.getId(), h.getId(), date, startTime, format, price));
            ds.saveSchedules();
            System.out.println("Schedule added.");
        } catch (CbsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void editSchedule() {
        try {
            Schedule s = chooseSchedule();
            boolean done = false;

            while (!done) {
                System.out.print("\n=== EDIT SCHEDULE ===");
                System.out.print("\n1 - Movie\n2 - Hall\n3 - Date\n4 - Start time\n5 - Format\n6 - Ticket price\n7 - Done\n");
                int field = InputHelper.getValidInt("--> Option: ", 1, 7);

                if (field == 7) {
                    done = true;
                } else {
                    try {
                        changeSchedule(s, field);
                    } catch (CbsException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // the change is only saved when it does not overlap another schedule in the same hall
    private void changeSchedule(Schedule s, int field) throws CbsException {
        DataStore ds = DataStore.getInstance();
        int movieId = s.getMovieId();
        int hallId = s.getHallId();
        String date = s.getDate();
        String startTime = s.getStartTime();
        String format = s.getFormat();
        double price = s.getTicketPrice();

        if (field == 1) {
            Guest.viewMovies();
            movieId = chooseMovie().getId();
        } else if (field == 2) {
            listHalls();
            hallId = chooseHall().getId();
        } else if (field == 3) {
            date = InputHelper.getValidDate("New date (YYYY-MM-DD): ");
        } else if (field == 4) {
            startTime = InputHelper.getValidTime("New start time (HH:MM): ");
        } else if (field == 5) {
            format = chooseFormat();
        } else {
            price = InputHelper.getValidDouble("New ticket price (50.00-1000.00): ", 50, 1000);
        }

        Schedule test = new Schedule(s.getId(), movieId, hallId, date, startTime, format, price);
        BookingManager.checkScheduleConflict(test, s.getId());

        s.setMovieId(movieId);
        s.setHallId(hallId);
        s.setDate(date);
        s.setStartTime(startTime);
        s.setFormat(format);
        s.setTicketPrice(price);
        ds.saveSchedules();
        System.out.println("Schedule updated.");
    }

    private void removeSchedule() {
        DataStore ds = DataStore.getInstance();

        try {
            Schedule s = chooseSchedule();
            // a seat row in the ledger means a confirmed booking exists for this schedule
            for (SeatLedgerEntry entry : ds.getLedger()) {
                if (entry.getScheduleId() == s.getId()) {
                    throw new InvalidInputException("Cannot remove: this schedule has confirmed bookings.");
                }
            }
            if (InputHelper.getYesNo("Remove schedule " + s.getId() + "? (Y/N): ")) {
                ds.getSchedules().remove(s);
                ds.saveSchedules();
                System.out.println("Schedule removed.");
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ----- discounts and promos (Step 12) -----

    private void manageDiscounts() {
        boolean back = false;
        while (!back) {
            System.out.print("\n=== MANAGE PROMOS ===");
            System.out.print("\n1 - Create a promo\n2 - List promos\n3 - Edit a promo\n4 - Extend a promo\n5 - Deactivate a promo\n6 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 6);

            switch (option) {
                case 1:
                    createDiscount();
                    break;
                case 2:
                    listDiscounts();
                    break;
                case 3:
                    editDiscount();
                    break;
                case 4:
                    extendDiscount();
                    break;
                case 5:
                    deactivateDiscount();
                    break;
                case 6:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-6.");
            }
        }
    }

    private Discount findDiscount(String code) {
        DataStore ds = DataStore.getInstance();

        for (Discount d : ds.getDiscounts()) {
            if (d.getCode().equalsIgnoreCase(code)) {
                return d;
            }
        }
        return null;
    }

    private Discount chooseDiscount() throws InvalidInputException {
        listDiscounts();
        String code = InputHelper.getValidCode("Promo code: ");
        Discount d = findDiscount(code);
        if (d == null) {
            throw new InvalidInputException("Promo code not found.");
        }
        return d;
    }

    private void listDiscounts() {
        DataStore ds = DataStore.getInstance();

        if (ds.getDiscounts().size() == 0) {
            System.out.println("No promos yet.");
        } else {
            System.out.println("-----------------------------------------------------------------------");
            System.out.printf("%-12s | %-8s | %-10s | %-9s | %-10s | %-8s%n", "Code", "Type", "Value", "Stackable", "Expiry", "Status");
            System.out.println("-----------------------------------------------------------------------");
            for (Discount d : ds.getDiscounts()) {
                String stackable = d.isStackable() ? "Yes" : "No";
                String status = d.isActive() ? "Active" : "Inactive";
                System.out.printf("%-12s | %-8s | %-10.2f | %-9s | %-10s | %-8s%n",
                        d.getCode(), d.getType(), d.getValue(), stackable, d.getExpiryDate(), status);
            }
        }
    }

    private String getValidFutureDate(String prompt) {
        String date = "";
        boolean isValid = false;

        while (!isValid) {
            date = InputHelper.getValidDate(prompt);
            if (date.compareTo(BookingManager.today()) < 0) {
                System.out.println("Error: The date cannot be in the past.");
            } else {
                isValid = true;
            }
        }
        return date;
    }

    private void createDiscount() {
        DataStore ds = DataStore.getInstance();

        try {
            String code = InputHelper.getValidCode("Promo code (4-12 letters or digits): ");
            if (findDiscount(code) != null) {
                throw new InvalidInputException("That promo code already exists.");
            }
            System.out.print("\n1 - PERCENT (1-50)\n2 - FIXED (Php 1.00-500.00)\n");
            int choice = InputHelper.getValidInt("--> Discount type: ", 1, 2);
            String type = "PERCENT";
            double value;
            if (choice == 1) {
                value = InputHelper.getValidDouble("Percent off (1-50): ", 1, 50);
            } else {
                type = "FIXED";
                value = InputHelper.getValidDouble("Amount off in Php (1.00-500.00): ", 1, 500);
            }
            boolean stackable = InputHelper.getYesNo("Can it be used together with Senior/PWD/Student discounts? (Y/N): ");
            String expiry = getValidFutureDate("Expiry date (YYYY-MM-DD): ");

            ds.getDiscounts().add(new Discount(code, type, value, stackable, expiry, true));
            ds.saveDiscounts();
            System.out.println("Promo created.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void editDiscount() {
        DataStore ds = DataStore.getInstance();

        try {
            Discount d = chooseDiscount();
            boolean done = false;

            while (!done) {
                System.out.print("\n=== EDIT PROMO " + d.getCode() + " ===");
                System.out.print("\n1 - Change type and value\n2 - Change stackable\n3 - Done\n");
                int option = InputHelper.getValidInt("--> Option: ", 1, 3);

                if (option == 1) {
                    System.out.print("\n1 - PERCENT (1-50)\n2 - FIXED (Php 1.00-500.00)\n");
                    int choice = InputHelper.getValidInt("--> Discount type: ", 1, 2);
                    if (choice == 1) {
                        d.setType("PERCENT");
                        d.setValue(InputHelper.getValidDouble("Percent off (1-50): ", 1, 50));
                    } else {
                        d.setType("FIXED");
                        d.setValue(InputHelper.getValidDouble("Amount off in Php (1.00-500.00): ", 1, 500));
                    }
                    ds.saveDiscounts();
                    System.out.println("Promo updated.");
                } else if (option == 2) {
                    d.setStackable(InputHelper.getYesNo("Can it be used together with Senior/PWD/Student discounts? (Y/N): "));
                    ds.saveDiscounts();
                    System.out.println("Promo updated.");
                } else {
                    done = true;
                }
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void extendDiscount() {
        DataStore ds = DataStore.getInstance();

        try {
            Discount d = chooseDiscount();
            System.out.println("Current expiry date: " + d.getExpiryDate());
            String newDate = getValidFutureDate("New expiry date (YYYY-MM-DD): ");
            if (newDate.compareTo(d.getExpiryDate()) <= 0) {
                throw new InvalidInputException("The new date must be after the current expiry date.");
            }
            d.setExpiryDate(newDate);
            ds.saveDiscounts();
            System.out.println("Promo extended.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deactivateDiscount() {
        DataStore ds = DataStore.getInstance();

        try {
            Discount d = chooseDiscount();
            if (!d.isActive()) {
                throw new InvalidInputException("That promo is already inactive.");
            }
            if (InputHelper.getYesNo("Deactivate " + d.getCode() + "? (Y/N): ")) {
                d.setActive(false);
                ds.saveDiscounts();
                System.out.println("Promo deactivated.");
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ----- user accounts (Step 13) -----

    private void manageUsers() {
        boolean back = false;
        while (!back) {
            System.out.print("\n=== MANAGE USER ACCOUNTS ===");
            System.out.print("\n1 - List registered users\n2 - Deactivate a user\n3 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 3);

            switch (option) {
                case 1:
                    listUsers();
                    break;
                case 2:
                    deactivateUser();
                    break;
                case 3:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-3.");
            }
        }
    }

    private void listUsers() {
        DataStore ds = DataStore.getInstance();
        int count = 0;

        System.out.println("-----------------------------------------------------------------------");
        System.out.printf("%-4s | %-18s | %-26s | %-10s | %-8s%n", "ID", "Name", "Email", "Birthdate", "Status");
        System.out.println("-----------------------------------------------------------------------");
        for (User u : ds.getUsers()) {
            if (u instanceof RegisteredUser) {
                RegisteredUser r = (RegisteredUser) u;
                String status = r.isActive() ? "Active" : "Inactive";
                System.out.printf("%-4d | %-18.18s | %-26.26s | %-10s | %-8s%n",
                        r.getId(), r.getName(), r.getEmail(), r.getBirthdate(), status);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No registered users yet.");
        }
    }

    private void deactivateUser() {
        DataStore ds = DataStore.getInstance();

        try {
            listUsers();
            int id = InputHelper.getValidInt("User ID to deactivate: ", 1, 999999);
            User found = null;
            for (User u : ds.getUsers()) {
                if (u instanceof RegisteredUser && u.getId() == id) {
                    found = u;
                }
            }
            if (found == null) {
                throw new InvalidInputException("Registered user ID not found.");
            }
            if (!found.isActive()) {
                throw new InvalidInputException("That account is already deactivated.");
            }
            if (InputHelper.getYesNo("Deactivate " + found.getName() + "? Their bookings stay. (Y/N): ")) {
                found.setActive(false);
                ds.saveUsers();
                System.out.println("Account deactivated.");
            }
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ----- system configuration (Step 13) -----

    private void manageConfig() {
        boolean back = false;
        while (!back) {
            System.out.print("\n=== SYSTEM CONFIGURATION ===");
            System.out.print("\n1 - Ticket limit per transaction (6-10)\n2 - Grace period before showtime in minutes (0-60)\n3 - Grace period after showtime in minutes (0-120)\n4 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 4);

            switch (option) {
                case 1:
                    changeConfig("TICKET_LIMIT_PER_TRANSACTION", "Ticket limit per transaction", 6, 10);
                    break;
                case 2:
                    changeConfig("GRACE_PERIOD_BEFORE_MINUTES", "Grace period before", 0, 60);
                    break;
                case 3:
                    changeConfig("GRACE_PERIOD_AFTER_MINUTES", "Grace period after", 0, 120);
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-4.");
            }
        }
    }

    private void changeConfig(String key, String label, int min, int max) {
        DataStore ds = DataStore.getInstance();

        System.out.println(label + " is currently " + ds.getConfigInt(key) + ".");
        int value = InputHelper.getValidInt("New value (" + min + "-" + max + "): ", min, max);
        ds.setConfig(key, String.valueOf(value));
        System.out.println(label + " updated.");
    }
}
