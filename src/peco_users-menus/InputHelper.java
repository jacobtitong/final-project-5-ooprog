import java.util.Scanner;

public class InputHelper {
    static Scanner sc = new Scanner(System.in);

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

    public static double getValidDouble(String prompt, double min, double max) {
        double number = 0;
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            String input = sc.nextLine();

            try {
                number = Double.parseDouble(input.trim());
                if (!(number >= min && number <= max)) {
                    throw new InvalidInputException(String.format("Value must be between %.2f and %.2f.", min, max));
                }
                isValid = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number, not text.");
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return Math.round(number * 100) / 100.0;
    }

    public static String getValidName(String prompt, int minLen, int maxLen) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = sc.nextLine();

            try {
                checkText(input);
                if (input.length() < minLen || input.length() > maxLen) {
                    throw new InvalidInputException("Must be " + minLen + "-" + maxLen + " characters long.");
                }
                if (!input.matches("[A-Za-z0-9 ]+")) {
                    throw new InvalidInputException("Only letters, digits and spaces are allowed.");
                }
                isValid = true;
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return input;
    }

    // minLen 0 means the text is optional: a blank answer is saved as "none"
    public static String getValidText(String prompt, int minLen, int maxLen) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = sc.nextLine();

            try {
                if (minLen == 0 && input.trim().isEmpty()) {
                    input = "none";
                    isValid = true;
                } else {
                    checkText(input);
                    if (input.length() < minLen || input.length() > maxLen) {
                        throw new InvalidInputException("Must be " + minLen + "-" + maxLen + " characters long.");
                    }
                    if (!input.matches("[A-Za-z0-9 .,!?-]+")) {
                        throw new InvalidInputException("Only letters, digits, spaces and . , ! ? - are allowed.");
                    }
                    isValid = true;
                }
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return input;
    }

    public static String getValidEmail(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = sc.nextLine();

            try {
                checkText(input);
                if (!input.matches("[A-Za-z0-9._]+@[A-Za-z0-9]+\\.[A-Za-z]+")) {
                    throw new InvalidInputException("Email must look like name@example.com.");
                }
                isValid = true;
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return input;
    }

    public static String getValidPassword(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = sc.nextLine();

            try {
                checkText(input);
                if (input.length() < 8 || input.length() > 20) {
                    throw new InvalidInputException("Password must be 8-20 characters long.");
                }
                if (input.contains(" ")) {
                    throw new InvalidInputException("Password cannot contain spaces.");
                }
                boolean hasLetter = false;
                boolean hasDigit = false;
                for (int i = 0; i < input.length(); i++) {
                    char c = input.charAt(i);
                    if (Character.isLetter(c)) {
                        hasLetter = true;
                    }
                    if (Character.isDigit(c)) {
                        hasDigit = true;
                    }
                }
                if (!hasLetter || !hasDigit) {
                    throw new InvalidInputException("Password needs at least one letter and one digit.");
                }
                isValid = true;
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return input;
    }

    public static String getValidDate(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = sc.nextLine().trim();

            if (isValidDate(input)) {
                isValid = true;
            } else {
                System.out.println("Error: Enter a real date as YYYY-MM-DD.");
            }
        }
        return input;
    }

    public static String getValidTime(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = sc.nextLine().trim();

            if (isValidTime(input)) {
                isValid = true;
            } else {
                System.out.println("Error: Enter a time as HH:MM (24-hour), for example 19:30.");
            }
        }
        return input;
    }

    public static boolean getYesNo(String prompt) {
        boolean answer = false;
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("Y")) {
                answer = true;
                isValid = true;
            } else if (input.equalsIgnoreCase("N")) {
                answer = false;
                isValid = true;
            } else {
                System.out.println("Error: Please enter Y or N.");
            }
        }
        return answer;
    }

    // ----- extra helpers used by Peco's menus -----

    // returns the typed text, or "" when the user just presses Enter
    public static String getOptional(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static boolean isValidDate(String date) {
        boolean valid = false;
        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            if (month >= 1 && month <= 12 && day >= 1 && day <= daysInMonth(year, month)) {
                valid = true;
            }
        }
        return valid;
    }

    public static boolean isValidTime(String time) {
        return time.matches("([01][0-9]|2[0-3]):[0-5][0-9]");
    }

    public static String getValidBirthdate(String prompt) {
        String birthdate = "";
        boolean isValid = false;

        while (!isValid) {
            birthdate = getValidDate(prompt);
            try {
                if (birthdate.compareTo(BookingManager.today()) > 0) {
                    throw new InvalidInputException("Birthdate cannot be in the future.");
                }
                int age = BookingManager.calculateAge(birthdate);
                if (age < 5 || age > 120) {
                    throw new InvalidInputException("Age must be between 5 and 120.");
                }
                isValid = true;
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return birthdate;
    }

    // shows a small menu and returns CREDIT, DEBIT or EWALLET
    public static String getPaymentType() {
        System.out.print("\n1 - Credit card\n2 - Debit card\n3 - E-wallet\n");
        int choice = getValidInt("--> Payment type: ", 1, 3);
        String type = "EWALLET";
        if (choice == 1) {
            type = "CREDIT";
        } else if (choice == 2) {
            type = "DEBIT";
        }
        return type;
    }

    public static String getValidPaymentNumber(String prompt, String type) {
        String regex = "[0-9]{16}";
        String message = "Card number must be exactly 16 digits.";
        if (type.equals("EWALLET")) {
            regex = "09[0-9]{9}";
            message = "E-wallet number must be 11 digits starting with 09.";
        }

        String input = "";
        boolean isValid = false;
        while (!isValid) {
            System.out.print(prompt);
            input = sc.nextLine();

            try {
                checkText(input);
                if (!input.matches(regex)) {
                    throw new InvalidInputException(message);
                }
                isValid = true;
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return input;
    }

    // promo code: 4-12 letters or digits, saved in capitals
    public static String getValidCode(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = sc.nextLine();

            try {
                checkText(input);
                if (!input.matches("[A-Za-z0-9]{4,12}")) {
                    throw new InvalidInputException("Code must be 4-12 letters or digits.");
                }
                isValid = true;
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return input.toUpperCase();
    }

    // shared rule for every text: not empty, no space at the start or end, no ;
    private static void checkText(String input) throws InvalidInputException {
        if (input.trim().isEmpty()) {
            throw new InvalidInputException("Input cannot be empty or just spaces.");
        }
        if (!input.equals(input.trim())) {
            throw new InvalidInputException("No spaces at the start or end.");
        }
        if (input.contains(";")) {
            throw new InvalidInputException("The ; character is not allowed.");
        }
    }

    private static int daysInMonth(int year, int month) {
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        boolean leap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
        int result = days[month - 1];
        if (month == 2 && leap) {
            result = 29;
        }
        return result;
    }
}
