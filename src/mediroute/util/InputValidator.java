package mediroute.util;

/**
 * Utility for validating user inputs and system parameters.
 */
public class InputValidator {

    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isPositiveInteger(String text) {
        if (!isNonEmpty(text)) return false;
        try {
            int val = Integer.parseInt(text.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Integer parsePositiveInteger(String text) {
        if (!isNonEmpty(text)) return null;
        try {
            int val = Integer.parseInt(text.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
