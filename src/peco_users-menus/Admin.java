public class Admin extends User {
    public Admin(int id, String name, String email, String password, boolean active) {
        super(id, name, email, password, active);
    }

    @Override
    public User displayDashboard() {
        boolean loggedOut = false;
        while (!loggedOut) {
            System.out.print("\n=== ADMIN MENU ===");
            System.out.print("\n1 - Manage movies\n2 - Manage halls\n3 - Logout\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 3);

            switch (option) {
                case 1:
                    manageMovies();
                    break;
                case 2:
                    manageHalls();
                    break;
                case 3:
                    loggedOut = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-3.");
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
                        h.setRows(InputHelper.getValidInt("New rows (3-12): ", 3, 12));
                        ds.saveHalls();
                        System.out.println("Rows updated.");
                        break;
                    case 4:
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
}
