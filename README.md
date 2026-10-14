# Cinema Booking System — Implementation Guide (Source of Truth)

Group 5 · Section C2B — Cullarin, Mendoza, Peco, Titong

**For any AI agent or developer implementing this program**: this file is the single authoritative spec. Read it in full before writing code. Do not change a file schema, class contract, or rule defined here — extend only. When in doubt, this document wins over any chat summary.

## 0. Skill-Level Rules (READ FIRST — these override any "better" way you know)

The code must look like the team's earlier projects (Vehicle Rental, Inventory Manager, Library): beginner-friendly Java — plain classes, `Scanner`, `static` helper methods in a helper class, `while` menu loops with `switch`, `try/catch` around parsing. Write the simplest code that works; prefer more lines over cleverness. Comments are rare and one line, like the originals. See §0.5 for excerpts to copy.

**Scope rule**: build only what the proposal and this guide state. Do not add features, fields, files, or validations that are not written here.

| ✅ ALLOWED | ❌ FORBIDDEN |
| --- | --- |
| Default package (no `package` statements, no `import` of our own classes; member folders are source roots, not packages — see §14) | Packages, `import` of our own classes |
| Classes, `abstract` classes, `extends`, `super(...)`, `@Override`, `private` fields + getters/setters, constructors, `static` fields/methods | Inner/nested/anonymous classes, `record`, `enum` (use `String` constants like `"CONFIRMED"`) |
| `interface` / `implements` — **only** for the two Strategy interfaces | Lambdas (`->`), streams, method references, `Optional`, `var`, switch expressions |
| `ArrayList<Type>` with `add`, `get`, `size`, `remove`, `set`, `for` / enhanced-`for` loops; plain arrays | `HashMap`, `HashSet`, `Map`, `Set`, `Collections.sort`, `Comparator`, writing own generics |
| `Scanner(System.in)`, `System.out.print/println/printf`, `String.format` | `BigDecimal`, `MessageDigest`, `Optional`, regex lookaheads |
| `String` methods: `trim`, `equals`, `equalsIgnoreCase`, `split`, `matches` (simple regex only), `substring`, `length`, `contains`, `toUpperCase`, `compareTo`, `charAt` | `try`-with-resources, `Files`/`Path`/NIO, `FileInputStream` |
| `Integer.parseInt`, `Double.parseDouble`, `Math.round`, `Math.max/min` | Threads, `synchronized`, reflection, annotations other than `@Override` |
| `try / catch / finally`, `throw new`, `throws`, custom exceptions (`extends Exception`) | Any external library, JavaFX/Swing, database |
| `BufferedReader`+`FileReader`, `PrintWriter`+`FileWriter`, `java.io.File`, `IOException` |  |
| `java.time`: **only** `LocalDate.now().toString()`, `LocalTime.now()` (`getHour()`, `getMinute()`), `LocalDateTime.now().toString()` |  |
| `if/else`, `switch`/`case`/`break`, `for`, `while`, `do-while`, ternary `? :` |  |
| `double` for all money, rounded with `Math.round(x * 100) / 100.0`, printed with `%.2f` |  |

**Technology**: Java + plain `.txt` files only. Terminal only (`System.in` / `System.out`). The `.txt` files are created in the project's main folder the first time the program saves; run the program from `Main` in VS Code.

## 0.5 Code Style Reference — copy this style

These are short excerpts from Jacob's earlier projects (Vehicle Rental, Inventory Manager, Library). **The generated code must look like them**: same structure, same naming, same messages, same print format. Do not "upgrade" it.

- One class per file, default package. `Main` has `static` objects and `static` helper methods.
- Entity classes: `private` fields, a constructor, one-line getters, setters only where a value changes; abstract classes with `extends`, `super(...)`, `@Override`.
- A manager class (`Inventory`, `Library`) owns the collection and has `add...`, `find...` (a `for` loop that returns the match or `null`), and `view...` (prints a table).
- Menu loop: `boolean endProgram`, `while (!endProgram)`, menu text with `System.out.print("\n...")`, `getValidInt("--> Option: ")`, `switch` with a `default:` error line.
- Validation: a `getValid...` helper with `boolean isValid`, `while (!isValid)`, `sc.nextLine()`, a blank check with `continue`, `try/catch (NumberFormatException e)`, simple `matches(regex)`; error lines start with `Error: `.
- Plain names (`input`, `number`, `plate`, `option`), braces on every `if`, one statement per line, only occasional one-line `//` comments.
- Money prints as `Php %.2f`; tables use `printf("%-15s | ...%n")` and `%-15.15s` to cut long text. Where this guide uses `ArrayList`, treat it like the `vehicles` array in `Inventory`: loop with `for`, write `find...` methods that return `null`, print tables with `printf`. The file read/write templates in §6 are the only new code style.

```java
// Entity classes (abstract parent + child)
public abstract class Vehicle {
    private String plateNumber;
    private double baseRate;
    private boolean isAvailable;

    public Vehicle(String plateNumber, double baseRate) {
        this.plateNumber = plateNumber;
        this.baseRate = baseRate;
        this.isAvailable = true;
    }

    public String getPlateNumber() { return plateNumber; }
    public double getBaseRate() { return baseRate; }
    public boolean isAvailable() { return isAvailable; }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public abstract double calculateCost(int days);
}

public class Van extends Vehicle {
    private int cargoCapacity;

    public Van(String plateNumber, double baseRate, int cargoCapacity) {
        super(plateNumber, baseRate);
        this.cargoCapacity = cargoCapacity;
    }

    @Override
    public double calculateCost(int days) {
        return (getBaseRate() * days) + 500;
    }

    public int getCargoCapacity() { return cargoCapacity; }
}

// Manager class: owns the array, add / find / view
public class Inventory {
    private Vehicle[] vehicles;

    public Inventory() {
        this.vehicles = new Vehicle[0];
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles = Arrays.copyOf(vehicles, vehicles.length + 1);
        vehicles[vehicles.length - 1] = vehicle;
    }

    public Vehicle findVehicle(String plateNumber) {
        for (Vehicle v : vehicles) {
            if (v.getPlateNumber().equalsIgnoreCase(plateNumber)) {
                return v;
            }
        }
        return null;
    }

    public void viewAllVehicles() {
        System.out.println("---------------------------------------------------------------------------");
        System.out.printf("%-15s | %-15s | %-10.10s | %-10s%n", "Plate Number", "Model", "Type", "Status");
        System.out.println("---------------------------------------------------------------------------");
        for (Vehicle v : vehicles) {
            String status = v.isAvailable() ? "Available" : "Rented";
            String type = v.getClass().getSimpleName();
            System.out.printf("%-15.15s | %-15.15s | %-10.10s | %-10.10s%n",
                    v.getPlateNumber(), v.getModel(), type, status);
        }
    }
}

// Main: menu loop
public class Main {
    static Inventory inventory = new Inventory();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        boolean endProgram = false;

        while (!endProgram) {
            System.out.print("\n=== VEHICLE RENTAL SYSTEM MENU ===");
            System.out.print("\n1 - Add a vehicle\n2 - View all vehicles\n3 - Exit\n");
            int option = getValidInt("--> Option: ");

            switch (option) {
                case 1:
                    addVehicle();
                    break;
                case 2:
                    inventory.viewAllVehicles();
                    break;
                case 3:
                    endProgram = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-3.");
            }
        }
    }

    // Validation helper
    public static int getValidInt(String prompt) {
        int number = -1;
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            String input = sc.nextLine();

            if (input.trim().isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }

            try {
                number = Integer.parseInt(input.trim());
                if (number < 1) {
                    System.out.println("Error: No values less than 1.");
                } else {
                    isValid = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number, not text.");
            }
        }
        return number;
    }

    // Regex validation (same style for plates, names, emails, tracking numbers)
    public static String getValidPlate(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = sc.nextLine().trim();

            if (input.matches("^[A-Z]{3}\\d{4}$")) {
                isValid = true;
            } else {
                System.out.println("Error: Plates must be 3 UPPERCASE letters followed by 4 digits.");
            }
        }
        return input;
    }
}
```

## 1. Project Layout — One Folder per Team Member

Folder name = `<member>_<kind>`. **One member edits one folder; nobody creates, edits, renames, or deletes files in another member's folder.**

```
CinemaBookingSystem/
  src/
    titong_model-data/          Titong   — model classes + DataStore
    peco_users-menus/           Peco     — User classes + Main + InputHelper
    mendoza_strategy/           Mendoza  — Payment + Discount strategies
    cullarin_exceptions-logic/  Cullarin — exceptions + BookingManager + ReportManager
  (the .txt storage files appear in the project's main folder when the program runs; they are not committed)
```

Full file lists, signatures, and commit steps are in §14.

## 2. The Four OOP Pillars — Where Each Lives

| Pillar | Implementation |
| --- | --- |
| Inheritance | `User` is the base class; `Guest`, `RegisteredUser`, `Cashier`, `CustomerService`, `SnackManager`, `Admin` all `extends User` (same style as `Car extends Vehicle`) |
| Abstraction | `User` is `abstract` with `public abstract User displayDashboard();` — every role writes its own menu |
| Encapsulation | Every field in every class is `private`; access only via getters/setters. Setters for `name`, `email`, `savedPaymentNumber`, etc. validate and `throw new InvalidInputException(...)` |
| Polymorphism | `Main` calls `currentUser.displayDashboard()` without checking the role; checkout calls `payment.pay(...)` and `discount.apply(...)` on interface variables |

`displayDashboard()` runs that role's menu loop until logout and **returns the next user**: logout → `new Guest()`; Guest after successful login → the logged-in user object; Exit → `null`. `Main` loop: `User currentUser = new Guest(); while (currentUser != null) { currentUser = currentUser.displayDashboard(); }`

## 3. Class Contracts

All fields `private`; ints/doubles/Strings/booleans only (lists are `ArrayList<...>` inside `DataStore`).

**Users** (`User` fields: `int id, String name, String email, String password, boolean active`; methods: `abstract displayDashboard()`, getters)

| Class | Extra fields | Dashboard actions |
| --- | --- | --- |
| `Guest` | — | browse movies, view schedules/synopsis, search/filter, login, sign-up, exit |
| `RegisteredUser` | `String birthdate, savedPaymentType, savedPaymentNumber` (one saved method; `"none"` if unset) | all Guest browsing, book, cancel, booking history, view ticket, edit profile, review movie, deactivate my account, logout |
| `Cashier` | — | search showtimes, counter booking, print receipt, logout |
| `CustomerService` | — | view a user's bookings, process refund, create bulk request, logout |
| `SnackManager` | — | add/edit/discontinue snack, update price, update stock, low-stock view, logout |
| `Admin` | — | movies/schedules/halls CRUD, discounts/promos CRUD, review bulk requests, manage user accounts, revenue report, set system config, logout |

**Model**

| Class | Fields |
| --- | --- |
| `Movie` | `int id; String title, genre, language, ageRating, synopsis; int durationMinutes` |
| `Hall` | `int id; String cinemaLocation, hallName; int rows, columns` (seat code = row letter + column number, e.g. `B7`) |
| `Schedule` | `int id, movieId, hallId; String date (YYYY-MM-DD), startTime (HH:MM), format (2D/3D/IMAX/4DX); double ticketPrice` |
| `Booking` | `int id; String ownerType (REGISTERED_USER/GUEST/BULK); int ownerId (0 if not a registered user); String customerName; int scheduleId; String seats (comma list "A1,A2"); String status (CONFIRMED/CANCELLED); double totalPaid; String timestamp` |
| `Transaction` | `int id, bookingId; String ownerType; int ownerId, scheduleId, ticketCount; double ticketAmount, snackAmount, totalAmount; String method (CREDIT/DEBIT/EWALLET/CASH); String status (SUCCESS/REFUND_PENDING/REFUNDED); double refundAmount; String timestamp` |
| `Snack` | `int id; String name; double price; int stock; boolean active` |
| `SnackOrder` | `int id, bookingId, snackId, quantity; double unitPrice` |
| `Discount` | `String code, type (PERCENT/FIXED); double value; boolean stackable; String expiryDate; boolean active` |
| `BulkBookingRequest` | `int id; String requesterName, contactInfo; int scheduleId, seatCount; String status (PENDING/APPROVED/REJECTED); int reviewerId (0 until reviewed)` |
| `Review` | `int id, userId, movieId, rating (1–5); String comment, timestamp` |
| `SeatLedgerEntry` | `int scheduleId; String seatCode; int bookingId` — **a row exists only for booked seats**; no row = seat is free |
| `SystemConfig` | `String key; String value` |

Every model class has: a constructor taking all fields, getters, setters only where a value changes, and `String toFileLine()` returning the fields joined by `;`.

## 4. Design Patterns (GoF — Required) with Templates

**Singleton — `DataStore`** (holds every `ArrayList`, loads all files once, saves on every change)

```java
public class DataStore {
    private static DataStore instance;
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Movie> movies = new ArrayList<>();
    // ...one ArrayList per central file in §6

    private DataStore() {          // private: nobody else can call "new DataStore()"
        loadAll();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }
}
```

Rules: **only `DataStore` touches files** (`FileReader`/`FileWriter` appear nowhere else). Everyone else calls `DataStore.getInstance().getMovies()`, `.saveMovies()`, etc.

**Strategy — Payment**

```java
public interface PaymentStrategy {
    void pay(double amount, String accountNumber) throws PaymentFailedException;
}

public class EWalletPayment implements PaymentStrategy {
    @Override
    public void pay(double amount, String accountNumber) throws PaymentFailedException {
        if (!accountNumber.matches("09[0-9]{9}")) {
            throw new PaymentFailedException("E-wallet number must be 11 digits starting with 09.");
        }
    }
}
```

| Class | Check inside `pay` |
| --- | --- |
| `CreditCardPayment` / `DebitCardPayment` | `accountNumber` is exactly 16 digits (`[0-9]{16}`) |
| `EWalletPayment` | `09` + 9 digits |
| `CashPayment` | no check (counter only — the online menu never offers it) |
| All: `amount` must be > 0. Card declines are out of scope (payment is simulated; it only fails on bad format). The menu picks the object with a `switch` on the user's choice (`PaymentStrategy payment = new EWalletPayment();`); **after that, checkout only calls `payment.pay(...)` — no `if (method.equals(...))`.** |  |

**Strategy — Discount**

```java
public interface DiscountStrategy {
    double apply(double ticketSubtotal);   // returns the new subtotal; never below 0
}
```

Implementations and math are in §9. Snacks are never passed to a discount.

## 5. Exception Handling

```java
public class CbsException extends Exception {
    public CbsException(String message) { super(message); }
}
public class SeatUnavailableException extends CbsException {
    public SeatUnavailableException(String message) { super(message); }
}
```

Every exception below `extends CbsException` and has only that one constructor. Methods that can throw one declare `throws ExceptionName`.

| Exception | Thrown when |
| --- | --- |
| `InvalidInputException` | text/number fails a rule in §12 (also: email already registered, id not found, not eligible to cancel) |
| `AuthenticationException` | email not found or password wrong |
| `SeatUnavailableException` | seat code not in hall, malformed, repeated, or already in the ledger |
| `TicketLimitExceededException` | seats requested > `TICKET_LIMIT_PER_TRANSACTION` |
| `TimeOverlapException` | new booking's window overlaps one of the same user's CONFIRMED bookings (this also covers booking the same showing twice) — booking rejected, no override |
| `ScheduleConflictException` | Admin schedule overlaps another in the same hall, or ends after 23:59 |
| `InsufficientStockException` | snack quantity > stock |
| `PaymentFailedException` | bad card/e-wallet number or amount ≤ 0 |
| `InvalidDiscountException` | promo unknown/expired/inactive, ID not confirmed, Senior under 60, or stacking rule broken |

**Validation uses try/catch, like your past projects** (parse inside `try`, throw our exception for rule failures, catch both, reprompt):

```java
public static int getValidInt(String prompt, int min, int max) {
    int number = 0;
    boolean isValid = false;
    while (!isValid) {
        System.out.print(prompt);
        String input = sc.nextLine();
        try {
            number = Integer.parseInt(input.trim());
            if (number < min || number > max) {
                throw new InvalidInputException("Value must be between " + min + " and " + max + ".");
            }
            isValid = true;
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number, not text.");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    return number;
}
```

Business flows (booking, cancel, etc.) wrap the steps in `try { ... } catch (CbsException e) { System.out.println("Error: " + e.getMessage()); }` so any rule failure returns to the menu with a message.

## 6. Data Layer — Plain `.txt` Files

**Format**: one record per line; fields separated by **`;`**; lists inside a field separated by **`,`** (e.g. seats `A1,A2,A3`). No field may ever be empty — write `none`. Text inputs may never contain `;` (enforced by §12). Parse with `line.split(";")` then `Integer.parseInt` / `Double.parseDouble` / `.equals("true")`.

**Read template** (missing file = empty list, not a crash)

```java
try {
    BufferedReader br = new BufferedReader(new FileReader("movies.txt"));
    String line = br.readLine();
    while (line != null) {
        String[] p = line.split(";");
        movies.add(new Movie(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4], p[5], Integer.parseInt(p[6])));
        line = br.readLine();
    }
    br.close();
} catch (IOException e) {
    // file not created yet: start with an empty list
}
```

**Write template** (always rewrite the whole file from the ArrayList; call after every change)

```java
try {
    PrintWriter pw = new PrintWriter(new FileWriter("movies.txt"));
    for (Movie m : movies) {
        pw.println(m.toFileLine());
    }
    pw.close();
} catch (IOException e) {
    System.out.println("Error: could not save movies.txt");
}
```

**Files**

| File | Fields (in order) |
| --- | --- |
| `users.txt` | id;role;name;email;password;active;birthdate;savedPaymentType;savedPaymentNumber — role = `REGISTERED_USER`/`CASHIER`/`CUSTOMER_SERVICE`/`SNACK_MANAGER`/`ADMIN`; staff use `none` for birthdate/payment. On load, build the matching subclass into one `ArrayList<User>` |
| `movies.txt` | id;title;genre;language;ageRating;synopsis;durationMinutes |
| `halls.txt` | id;cinemaLocation;hallName;rows;columns |
| `schedules.txt` | id;movieId;hallId;date;startTime;format;ticketPrice |
| `seat_ledger.txt` | scheduleId;seatCode;bookingId |
| `transactions.txt` | id;bookingId;ownerType;ownerId;scheduleId;ticketCount;ticketAmount;snackAmount;totalAmount;method;status;refundAmount;timestamp |
| `snacks.txt` | id;name;price;stock;active |
| `snack_orders.txt` | id;bookingId;snackId;quantity;unitPrice |
| `discounts_promos.txt` | code;type;value;stackable;expiryDate;active |
| `bulk_requests.txt` | id;requesterName;contactInfo;scheduleId;seatCount;status;reviewerId |
| `reviews.txt` | id;userId;movieId;rating;comment;timestamp |
| `system_config.txt` | key;value (see §7) |
| `counter_bookings.txt` | Booking format below — walk-in (`GUEST`) and approved bulk (`BULK`) bookings |
| `bookings_<userId>.txt` | Booking format below — created empty at sign-up; one file per registered user (their booking history/tickets) |

Booking line: `id;ownerType;ownerId;customerName;scheduleId;seats;status;totalPaid;timestamp`. Both booking file types share it, so one `loadBookings(path)` / `saveBookings(path, list)` pair serves all (path = `"bookings_" + userId + ".txt"` or `"counter_bookings.txt"`).

`seat_ledger.txt` is checked on every seat choice, whichever file the booking lands in — this prevents double-booking across online, counter and bulk.

**IDs**: one global counter `NEXT_ID` in `system_config.txt`. `DataStore.generateId()` returns it, adds 1, saves. Display references as `"BK-" + String.format("%06d", id)`.

**First run**: if a file is missing, `DataStore` creates it with defaults — `users.txt` gets one account per staff role — `admin@cinema.com` / `Admin123` (ADMIN), `cashier@cinema.com` / `Cashier123` (CASHIER), `service@cinema.com` / `Service123` (CUSTOMER_SERVICE), `snacks@cinema.com` / `Snacks123` (SNACK_MANAGER); `system_config.txt` gets §7 defaults.

**Passwords**: stored as plain text in `users.txt` (project decision).

## 7. System Configuration (`system_config.txt`)

| Key | Default | Set by |
| --- | --- | --- |
| `TICKET_LIMIT_PER_TRANSACTION` | 8 (allowed 6–10) | Admin |
| `GRACE_PERIOD_BEFORE_MINUTES` | 15 (0–60) | Admin |
| `GRACE_PERIOD_AFTER_MINUTES` | 45 (0–120) | Admin |
| `LOW_STOCK_THRESHOLD` | 10 (1–100) | Snack Manager |
| `NEXT_ID` | 1 | system |

## 8. Dates, Times, Grace Period & Overlap

- Dates are `String` `YYYY-MM-DD` (compare with `equals` / `compareTo`); times are `String` `HH:MM`. Helper `BookingManager.toMinutes("19:30")` = `19*60+30` using `split(":")`.
- Today = `LocalDate.now().toString()`; now-minutes = `LocalTime.now().getHour()*60 + LocalTime.now().getMinute()`. A showing is **upcoming** if its date is after today, or today and start > now-minutes.
- **Effective window** of a showing = `[start − BEFORE, start + durationMinutes + AFTER]` in minutes. A showing must start so that its window stays within the same day (start − BEFORE ≥ 0 and end ≤ 1439).
- Two windows on the **same date** overlap if `startA < endB && startB < endA` (touching is allowed).
- **Admin schedule add/edit**: check against every other schedule in the *same hall* → `ScheduleConflictException`.
- **User booking** (registered users only): check against that user's own CONFIRMED bookings, *any hall* → `TimeOverlapException`, booking rejected with no override ("users cannot book multiple movies that may overlap").

## 9. Pricing & Discounts (all `double`, rounded to 2 decimals)

| Class | Rate | Rule |
| --- | --- | --- |
| `SeniorDiscount` | 20% off VAT-exclusive price + VAT exempt (RA 9994) | `(subtotal / 1.12) * 0.80`; user must be age ≥ 60; user confirms a valid ID (`Y/N`) |
| `PWDDiscount` | same as Senior (RA 10754) | `(subtotal / 1.12) * 0.80`; user confirms a valid ID (`Y/N`) |
| `StudentDiscount` | 10% off | `subtotal * 0.90`; user confirms a valid school ID (`Y/N`) |
| `PromoDiscount(type, value)` | Admin-defined | `PERCENT`: `subtotal * (1 − value/100)`; `FIXED`: `subtotal − value`; result floor 0 |
| `BulkDiscount(seatCount)` | 10% for 20–49 seats, 15% for 50+ | `subtotal * (1 − rate)` |

Inside Senior/PWD, write the VAT rate as `private static final double VAT_RATE = 0.12;`. **Stacking**: at most one identity discount (Senior/PWD/Student, mutually exclusive) + at most one promo; apply identity first, then promo. A promo with `stackable=false` cannot be used with an identity discount → `InvalidDiscountException`. Bulk replaces all others. Discounts apply to the **ticket subtotal only; snacks are always full price.** `total = discountedTickets + snackTotal`. Age from birthdate: `currentYear − birthYear`, minus 1 if birthday not yet reached this year.

## 10. Revenue Report (Admin)

`ReportManager.printRevenueReport(startDate, endDate)` (dates inclusive, `YYYY-MM-DD`). Only transactions with status `SUCCESS` and a timestamp date in range count. Print:

1. **Total revenue** — overall, plus per movie (sum `ticketAmount` grouped by `scheduleId → movieId`) and per snack (sum `quantity × unitPrice` from `snack_orders.txt` whose booking's transaction is `SUCCESS`).
2. **Best-selling movies** — ranked by tickets sold (sum `ticketCount`), top 5, with revenue.
3. **Best-selling snacks** — ranked by quantity sold, top 5, with revenue. Implementation: build two parallel `ArrayList`s (names, counts) per ranking and sort with a nested-`for` selection sort. No streams, no `Comparator`.

## 11. Menu Flows

Every flow: wrap in `try/catch (CbsException e)`; a failed step prints the message and returns to the role menu. Save every changed ArrayList before returning.

- **Sign-up**: name, email, password, birthdate, optional saved payment → validate (§12) → email unique → store the password → create empty `bookings_<id>.txt` → returns to Guest menu (user then logs in).
- **Login**: email + password → compare with the stored password → `AuthenticationException` on failure or if `active=false` → return the user object.
- **Search/filter** (Guest & Registered): any combination of genre, language, format, cinema location, date, time (blank = skip) → print matches as the §13 table with average review rating.
- **Book online** (Registered User):
  1. Pick movie → pick an upcoming schedule.
  2. Overlap check (§8) → `TimeOverlapException`.
  3. Print seat map → enter seats `A1,A2` → each must exist in the hall, not repeat, not be in the ledger; count ≤ ticket limit. Seats are held only in a local `ArrayList<String>` — **nothing is written yet**.
  4. Snacks: loop (snack id, qty 1–10, `0` to finish); stock check; only `active` snacks.
  5. Discount menu (none/Senior/PWD/Student; for the last three ask `Do you have a valid ID? (Y/N)`) and optional promo (§9).
  6. Show summary (tickets, discount, snacks, total) → confirm `Y/N`.
  7. Choose payment (saved method offered first) → number → `payment.pay()`; on `PaymentFailedException` let the user retry or abort (nothing written yet).
  8. On success: `generateId()`; add ledger rows; save booking to `bookings_<id>.txt`; add Transaction `SUCCESS`; deduct stock and write `snack_orders.txt`; print the §13 e-ticket.
- **Cancel** (Registered User): choose own CONFIRMED booking with an upcoming showtime → confirm → status `CANCELLED`; delete its ledger rows; its Transaction → `REFUND_PENDING`. Snack stock is not restored.
- **Refund** (Customer Service): list `REFUND_PENDING` → choose one → Full (`refundAmount = totalAmount`) or Partial (0.01 to total) → status `REFUNDED`.
- **View user bookings** (Customer Service): enter email → print that user's file.
- **Counter booking** (Cashier): same as online except: set `customerName = "Walk-in"` (no prompt); no overlap check; `ownerType=GUEST`, `ownerId=0`; any payment incl. `CashPayment`; booking saved to `counter_bookings.txt`; print receipt. Ticket limit still applies.
- **Bulk** — Customer Service: requester name, contact (email or 11-digit mobile), schedule, seatCount 20–200 → `PENDING`. Admin: list `PENDING` → Approve or Reject (`reviewerId` = admin id). Approve = auto-assign the first N free seats (row by row); if fewer than N are free, refuse. Then write ledger rows, a `BULK` booking in `counter_bookings.txt`, and a `SUCCESS` Transaction using `BulkDiscount`. Bulk is exempt from the ticket limit.
- **Admin CRUD**:
  - *Movies*: add/list/edit/remove — remove only if it has no schedules.
  - *Schedules*: add/edit/remove — §8 checks; remove only if no CONFIRMED bookings.
  - *Halls*: add/edit/remove — remove only if no schedules use it.
  - *Discounts*: create/edit/extend/deactivate (`active=false`, never delete).
  - *Users*: list REGISTERED_USER accounts and deactivate one (`active=false`, never delete).
  - *Config*: edit §7 values (within allowed ranges).
- **Snack Manager**: add, edit name/price, update stock, discontinue (`active=false`, never delete), low-stock list (`stock <= LOW_STOCK_THRESHOLD`).
- **Review** (Registered User): choose a movie from the movie list → rating 1–5 + optional comment (≤100 chars) → one review per user per movie (resubmitting replaces it).
- **Edit profile**: name, birthdate, saved payment (each re-validated).
- **Deactivate account** (Registered User) / **Manage users** (Admin): confirm `Y/N` → `active=false`; that account can no longer log in; its bookings stay.

## 12. Input Validation Rules (all via try/catch)

| Input | Rule |
| --- | --- |
| Any text | not empty; no leading/trailing space (`!input.equals(input.trim())`); never contains `;` |
| Names (person, snack, hall, requester) | 2–30 chars; letters, digits, spaces only (`[A-Za-z0-9 ]+`) |
| Movie title | 1–40 chars; letters, digits, spaces only |
| Genre / language / format / age rating | letters, digits, spaces; from the lists shown on screen where applicable |
| Synopsis / review comment | up to 200 / 100 chars; letters, digits, spaces, `. , ! ? -` only |
| Email | `[A-Za-z0-9._]+@[A-Za-z0-9]+\\.[A-Za-z]+`; unique |
| Password | 8–20 chars, at least one letter and one digit, no spaces |
| Birthdate | `YYYY-MM-DD`, a real date (month 1–12, day valid for month), not in the future, age 5–120 |
| Card number | exactly 16 digits |
| E-wallet number | `09` + 9 digits |
| Duration | 30–300 minutes |
| Ticket price | 50.00–1000.00 |
| Hall size | rows 3–12, columns 4–20 |
| Snack price / stock | 1.00–500.00 / 0–1000 |
| Snack qty per booking | 1–10 |
| Promo | code 4–12 letters/digits unique; PERCENT 1–50; FIXED 1.00–500.00; expiry `YYYY-MM-DD` not in the past |
| Seat codes | letter + 1–2 digits within the hall |
| Review rating | 1–5 |
| All money | ≥ 0, two decimals (`Math.round(x*100)/100.0`) |
| Menu choice | integer within the menu's range |

## 13. Display Templates (`printf` with fixed widths, like `Inventory.viewAllVehicles()`)

- **Menus**: `=== TITLE ===` header, `1 - Option` lines, `--> Option: ` prompt.
- **Movie list**: columns ID, Title, Genre, Language, Age Rating, Avg Review (e.g. `4.3`) — long text truncated with `%-20.20s`.
- **Schedule list**: ID, Movie, Cinema, Hall, Date, Time, Format, Price.
- **Seat map**: header `SCREEN`, then each row as `A1 A2 A3 ...`, taken seats shown as `XX`.
- **E-ticket / receipt**: bordered block: Ref `BK-000012`, movie, cinema/hall, date/time, seats, snacks, discount applied, total paid, payment method, customer name.

## 14. Team Folders, AI Protocol & Git

### 14.1 Folders

| Folder | Owner | Files (one class per file) |
| --- | --- | --- |
| `src/titong_model-` | Titong | `Movie`, `Hall`, `Schedule`, `Booking`, `Transaction`, `Snack`, `SnackOrder`, `Discount`, `BulkBookingRequest`, `Review`, `SeatLedgerEntry`, `SystemConfig`, `DataStore` |
| `src/peco_users-menus/` | Peco | `User`, `Guest`, `RegisteredUser`, `Cashier`, `CustomerService`, `SnackManager`, `Admin`, `Main`, `InputHelper` |
| `src/mendoza_strategy/` | Mendoza | `PaymentStrategy`, `CreditCardPayment`, `DebitCardPayment`, `EWalletPayment`, `CashPayment`, `DiscountStrategy`, `SeniorDiscount`, `PWDDiscount`, `StudentDiscount`, `PromoDiscount`, `BulkDiscount` |
| `src/cullarin_exceptions-logic/` | Cullarin | `CbsException` + the 9 exceptions in §5, `BookingManager`, `ReportManager` |

### 14.2 Using this guide with an AI

**Human prompt** (attach this file, fill in, paste):

> I am **Peco**. My folder is **`peco_users-menus`**. Follow the guide: build my folder step by step (Step 0 first), then give me the separate GitHub commit-and-push guide.

**AI protocol** — when a member gives a name and a folder (or only a name: look the folder up in §14.1):

1. Find the row in §14.1. Its files are the **only** files you may create or edit. Never touch another member's folder. If the name and folder do not match §14.1, stop and ask.
2. Obey §0 (Skill-Level Rules) and every rule in this guide.
3. For anything outside the folder, code against the exact signatures in §14.3. Never rewrite a teammate's code and never invent different signatures.
4. Work in the order of that folder's steps in §14.4 (Step 0 first). If the member names specific steps, do only those.
5. Your output must contain: (a) every file with its full path; (b) the separate Git guide described in §14.5 and a short `README.md` (§14.6); (c) a short list of anything unclear that a teammate must confirm.

### 14.3 Cross-Folder Contract (exact signatures — never change without every owner agreeing)

Constructors take every field of the §3 table **in table order**; getters `getX()` (booleans `isX()`); setters `setX(v)`; every model has `String toFileLine()`. Exceptions: one constructor `(String message)`. Users: `User(int id, String name, String email, String password, boolean active)`; `RegisteredUser` adds `String birthdate, String savedPaymentType, String savedPaymentNumber`; staff classes take only the five base arguments; `public abstract User displayDashboard()`.

| Class (owner) | Public signatures others may call |
| --- | --- |
| `DataStore` (Titong) | `static DataStore getInstance()` · `ArrayList<User> getUsers()` · `getMovies()` · `getHalls()` · `getSchedules()` · `getLedger()` · `getTransactions()` · `getSnacks()` · `getSnackOrders()` · `getDiscounts()` · `getBulkRequests()` · `getReviews()` (each returns the matching `ArrayList<...>`) · `void saveUsers()` … one `saveX()` per list · `int generateId()` · `int getConfigInt(String key)` · `void setConfig(String key, String value)` · `String getUserBookingsPath(int userId)` · `String getCounterBookingsPath()` · `void createUserBookingsFile(int userId)` · `ArrayList<Booking> loadBookings(String path)` · `void saveBookings(String path, ArrayList<Booking> list)` · `User findUserByEmail(String email)` · `Movie findMovieById(int id)` · `Schedule findScheduleById(int id)` · `Hall findHallById(int id)` · `Snack findSnackById(int id)` (find methods return `null` if absent) |
| `InputHelper` (Peco, all `static`) | `int getValidInt(String prompt, int min, int max)` · `double getValidDouble(String prompt, double min, double max)` · `String getValidName(String prompt, int minLen, int maxLen)` · `String getValidText(String prompt, int minLen, int maxLen)` · `String getValidEmail(String prompt)` · `String getValidPassword(String prompt)` · `String getValidDate(String prompt)` · `String getValidTime(String prompt)` · `boolean getYesNo(String prompt)` |
| `BookingManager` (Cullarin, all `static`) | `int toMinutes(String hhmm)` · `String toTimeString(int minutes)` · `String today()` · `int nowMinutes()` · `double round2(double x)` · `int calculateAge(String birthdate)` · `void checkOverlap(int userId, Schedule s) throws TimeOverlapException` · `void checkScheduleConflict(Schedule s, int ignoreScheduleId) throws ScheduleConflictException` · `void printSeatMap(Schedule s)` · `void printTicket(Booking b)` · flows that prompt and save by themselves: `void bookOnline(RegisteredUser u)` · `void bookAtCounter(Cashier c)` · `void cancelBooking(RegisteredUser u)` · `void showBookingHistory(RegisteredUser u)` · `void submitReview(RegisteredUser u)` · `void processRefund(CustomerService cs)` · `void viewUserBookings(CustomerService cs)` · `void createBulkRequest(CustomerService cs)` · `void reviewBulkRequests(Admin a)` |
| `ReportManager` (Cullarin, `static`) | `void printRevenueReport()` (asks for the date range itself) |
| Strategies (Mendoza) | `PaymentStrategy { void pay(double amount, String accountNumber) throws PaymentFailedException; }` with `new CreditCardPayment()`, `new DebitCardPayment()`, `new EWalletPayment()`, `new CashPayment()` · `DiscountStrategy { double apply(double ticketSubtotal); }` with `new SeniorDiscount()`, `new PWDDiscount()`, `new StudentDiscount()`, `new PromoDiscount(String type, double value)`, `new BulkDiscount(int seatCount)` |

Menu-owned work (Peco): Guest browsing/search/sign-up/login, edit profile, and all CRUD inside `Admin` (movies, halls, schedules, discounts, user accounts, config) and `SnackManager` (snacks). Those call the helpers above.

### 14.4 Commit Steps per Folder (each step = at least one commit)

**Step 0 for everyone = a stub commit**: every file in your folder with the exact public signatures from §14.3, bodies empty or returning a dummy value, compiling. Merge it to `main` first so teammates can compile against you.

| Folder | Steps after Step 0 |
| --- | --- |
| `titong_model-data` | 1 `Movie`, `Hall` · 2 `Schedule`, `Booking` · 3 `Transaction`, `SeatLedgerEntry` · 4 `Snack`, `SnackOrder`, `Discount` · 5 `BulkBookingRequest`, `Review`, `SystemConfig` · 6 `DataStore` Singleton shell + empty lists + getters · 7 `generateId`, config load/save, first-run defaults · 8 users load/save (build the right subclass per role) · 9 movies, halls, schedules load/save · 10 ledger, transactions load/save · 11 snacks, snack orders, discounts load/save · 12 bulk requests, reviews, booking-file methods, `find...` methods |
| `peco_users-menus` | 1 `User` + `InputHelper` basics (`Scanner`, `getValidInt`) · 2 rest of `InputHelper` · 3 `Guest` browsing + search/filter · 4 `Guest` sign-up · 5 `Guest` login · 6 `Main` loop · 7 `RegisteredUser` menu + edit profile · 8 `Cashier` · 9 `CustomerService` · 10 `SnackManager` · 11 `Admin` movies + halls CRUD · 12 `Admin` schedules, discounts · 13 `Admin` user accounts, config, report + bulk-review menu entries |
| `mendoza_strategy` | 1 `PaymentStrategy` · 2 `CreditCardPayment`, `DebitCardPayment` · 3 `EWalletPayment`, `CashPayment` · 4 `DiscountStrategy` · 5 `SeniorDiscount`, `PWDDiscount` · 6 `StudentDiscount` · 7 `PromoDiscount` · 8 `BulkDiscount` |
| `cullarin_exceptions-logic` | 1 `CbsException`, `InvalidInputException`, `AuthenticationException` · 2 the other 7 exceptions (two commits) · 3 `BookingManager` helpers (time, round, age) · 4 `checkOverlap`, `checkScheduleConflict` · 5 `printSeatMap` + seat selection · 6 `bookOnline` · 7 `printTicket`, `cancelBooking`, `showBookingHistory` · 8 `submitReview` · 9 `processRefund`, `viewUserBookings` · 10 `bookAtCounter` · 11 `createBulkRequest`, `reviewBulkRequests` · 12 `ReportManager` totals · 13 `ReportManager` best-sellers |

### 14.5 GitHub Guide — MANDATORY in every output

Progress must show on GitHub in **small increments, not one big commit or push**. Every time the AI/developer produces code for a member, it must also produce a **separate** file named `GIT_STEPS_<member>.md` (saved inside that member's own folder and also shown in the reply). It must contain (examples below use one member's names — substitute the actual member, folder, and branch):

1. **Setup**: `git clone <repo-url>`, then `git checkout -b feature/peco-users-menus`.
2. **One section per step in §14.4** (only the steps that were built), each with: the files; the check that VS Code shows no red errors; the exact `git add <file paths>`; `git commit -m "<folder>: <what changed>"` (example: `peco_users-menus: add Step 1 classes`); `git push -u origin feature/peco-users-menus` the first time and `git push` after; and a line "On GitHub you should see: …".
3. **Milestone merges**: after Step 0 and then after every 3 steps, open a Pull Request into `main`; a teammate reviews and merges it; then everyone runs `git checkout main`, `git pull origin main`, `git checkout feature/peco-users-menus`, `git merge main`.
4. **Fixes**: if a push is rejected, run `git pull origin feature/peco-users-menus` and push again. Never `git push --force`.

**Hard rules the Git guide must state and the code must respect**

- At least **one commit per step** in §14.4 — never fewer commits than steps; each commit touches **at most 3 files**, all inside the member's own folder (the Git guide file itself is committed with the last step).
- Before every commit, check that VS Code shows **no red errors** in your files (pull `main` first) and that `Main` still runs once everyone's code is merged.
- Push after **every** commit.
- Never use `git add .` or `git add -A`; add exact paths only.
- Never commit the `.txt` data files or the `bin/` / `out/` folders (they are generated when you run the program).
- Commit messages start with the folder name, then a short description.

### 14.6 One-Time Repo Setup (done once by Titong, in its own commits)

```
CinemaBookingSystem/
  src/
    titong_model-data/
    peco_users-menus/
    mendoza_strategy/
    cullarin_exceptions-logic/
  .gitignore
  .vscode/settings.json
  README.md
```

Each folder above is a **source root, not a Java package** — all classes stay in the default package, so there are no `package` or `import` lines for our own classes. Without the `.vscode/settings.json` below, VS Code may report "package does not match folder".

- **VS Code** — commit `.vscode/settings.json`:

```json
{
  "java.project.sourcePaths": [
    "src/titong_model-data",
    "src/peco_users-menus",
    "src/mendoza_strategy",
    "src/cullarin_exceptions-logic"
  ],
  "java.project.outputPath": "out"
}
```

- `.gitignore` (one file, three lines): `*.txt` (the storage files the program creates when it runs), `bin/` and `out/` (folders where VS Code puts the compiled `.class` files — a `.class` file is the version of a `.java` file that the computer runs; VS Code makes them automatically, so they are never uploaded).
- `README.md` (short, written by the AI): project name, one sentence on what the program does, how to run it (open `Main.java` in VS Code and press Run), and the table of members and folders.

## 15. Merge & Test Order

1. Everyone merges **Step 0** first (the project compiles with stubs).
2. Next: `titong_model-data` through Step 5 and `cullarin_exceptions-logic` Steps 1–2 (others depend on them).
3. Then `DataStore` (Steps 6–12), `InputHelper`, `mendoza_strategy`.
4. Then `BookingManager` flows and the role menus. A feature is testable once the folders it calls are merged (online booking needs `DataStore`, strategies, `BookingManager`, `RegisteredUser`).
5. `ReportManager` last, then a full test pass against §12.
