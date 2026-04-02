import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptParser {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?:RM|MYR|TOTAL|AMOUNT)?\\s*[:=]?\\s*(\\d+(?:\\.\\d{1,2})?)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}|\\d{2}-\\d{2}-\\d{4})");

    public static ReceiptParseResult parse(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return new ReceiptParseResult(0, "Miscellaneous", LocalDate.now(), "Unknown merchant");
        }

        String cleaned = rawText.trim();
        double amount = detectAmount(cleaned);
        String merchant = detectMerchant(cleaned);
        String category = detectCategory(cleaned, merchant);
        LocalDate date = detectDate(cleaned);

        return new ReceiptParseResult(amount, category, date, merchant);
    }

    private static double detectAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text.replace(",", ""));
        double best = 0;
        while (matcher.find()) {
            try {
                double value = Double.parseDouble(matcher.group(1));
                if (value > best) {
                    best = value;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return best;
    }

    private static LocalDate detectDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        while (matcher.find()) {
            String candidate = matcher.group(1);
            LocalDate parsed = tryParseDate(candidate);
            if (parsed != null) {
                return parsed;
            }
        }
        return LocalDate.now();
    }

    private static LocalDate tryParseDate(String candidate) {
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(candidate, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static String detectMerchant(String text) {
        String[] lines = text.split("\\R");
        for (String line : lines) {
            String candidate = line.trim();
            if (!candidate.isEmpty() && candidate.length() > 2 && !candidate.matches(".*\\d{2,}.*")) {
                return toTitleCase(candidate);
            }
        }
        return "Unknown merchant";
    }

    private static String detectCategory(String text, String merchant) {
        String haystack = (text + " " + merchant).toLowerCase(Locale.ENGLISH);
        if (containsAny(haystack, "grab", "uber", "petrol", "shell", "caltex", "ron", "fuel")) {
            return "Transport";
        }
        if (containsAny(haystack, "restaurant", "cafe", "coffee", "tea", "food", "dining", "mcd", "kfc", "pizza")) {
            return "Food";
        }
        if (containsAny(haystack, "tesco", "aeon", "lotus", "grocery", "market", "mart")) {
            return "Groceries";
        }
        if (containsAny(haystack, "netflix", "spotify", "youtube", "subscription", "steam")) {
            return "Entertainment";
        }
        if (containsAny(haystack, "clinic", "pharmacy", "hospital", "guardian", "watsons")) {
            return "Health";
        }
        if (containsAny(haystack, "electric", "water", "utility", "internet", "wifi", "bill")) {
            return "Bills";
        }
        return "Miscellaneous";
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String toTitleCase(String value) {
        String[] words = value.toLowerCase(Locale.ENGLISH).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }
        return builder.toString();
    }
}
