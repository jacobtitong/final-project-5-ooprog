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
        return 0.0; // stub: filled in Step 2
    }

    public static String getValidName(String prompt, int minLen, int maxLen) {
        return ""; // stub: filled in Step 2
    }

    public static String getValidText(String prompt, int minLen, int maxLen) {
        return ""; // stub: filled in Step 2
    }

    public static String getValidEmail(String prompt) {
        return ""; // stub: filled in Step 2
    }

    public static String getValidPassword(String prompt) {
        return ""; // stub: filled in Step 2
    }

    public static String getValidDate(String prompt) {
        return ""; // stub: filled in Step 2
    }

    public static String getValidTime(String prompt) {
        return ""; // stub: filled in Step 2
    }

    public static boolean getYesNo(String prompt) {
        return false; // stub: filled in Step 2
    }
}
