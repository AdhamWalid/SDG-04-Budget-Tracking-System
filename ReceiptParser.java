import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptParser {

    private static final Pattern LABELED_AMOUNT_PATTERN = Pattern.compile(
            "(?:amount|total amount|total|transfer amount|payment amount)\\s*[:=]?\\s*(?:rm|myr)?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.\\d{1,2})?|[0-9]+(?:\\.\\d{1,2})?)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CURRENCY_AMOUNT_PATTERN = Pattern.compile(
            "(?:rm|myr)\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.\\d{1,2})?|[0-9]+(?:\\.\\d{1,2})?)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern GENERIC_MONEY_PATTERN = Pattern.compile(
            "\\b([0-9]{1,5}(?:\\.\\d{1,2})?)\\b");
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}|\\d{2}-\\d{2}-\\d{4}|\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}(?:,\\s*\\d{1,2}:\\d{2}\\s*[AP]M)?)");
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile(
            "(?:from account|account|source account|debited account)\\s*[:=]?\\s*([A-Za-z0-9*\\- ]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern RECIPIENT_PATTERN = Pattern.compile(
            "(?:to|recipient|beneficiary|merchant|receiver|payee)\\s*[:=]?\\s*(.+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern BENEFICIARY_LABEL_PATTERN = Pattern.compile(
            "(?:beneficiary|benepciary)\\s+name\\s*[:=]?\\s*(.+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern RECIPIENT_REFERENCE_PATTERN = Pattern.compile(
            "recipient\\s+reference\\s*[:=]?\\s*(.+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern STATUS_LINE_PATTERN = Pattern.compile(
            "^(successful|failed|pending|status)$",
            Pattern.CASE_INSENSITIVE);

    public static ReceiptParseResult parse(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return new ReceiptParseResult(0, "Miscellaneous", LocalDate.now(), "Unknown merchant",
                    "Unassigned", "Expense");
        }

        String cleaned = rawText.trim();
        double amount = detectAmount(cleaned);
        String transactionType = detectTransactionType(cleaned);
        String merchant = detectMerchant(cleaned);
        String category = detectCategory(cleaned, merchant);
        LocalDate date = detectDate(cleaned);
        String source = detectSource(cleaned, transactionType);

        return new ReceiptParseResult(amount, category, date, merchant, source, transactionType);
    }

    private static double detectAmount(String text) {
        String[] lines = text.split("\\R");

        for (String line : lines) {
            double value = extractAmountFromLine(line, LABELED_AMOUNT_PATTERN);
            if (value > 0) {
                return value;
            }
        }

        for (String line : lines) {
            double value = extractAmountFromLine(line, CURRENCY_AMOUNT_PATTERN);
            if (value > 0) {
                return value;
            }
        }

        double best = 0;
        for (String line : lines) {
            String cleanedLine = line.trim();
            if (cleanedLine.isEmpty() || looksLikeIdentifierLine(cleanedLine)) {
                continue;
            }

            double value = extractAmountFromLine(cleanedLine, GENERIC_MONEY_PATTERN);
            if (value > best) {
                best = value;
            }
        }
        return best;
    }

    private static double extractAmountFromLine(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line);
        double best = 0;
        while (matcher.find()) {
            String raw = matcher.group(1).replace(",", "").trim();
            try {
                double value = Double.parseDouble(raw);
                if (value > 0 && value < 1_000_000 && value > best) {
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
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm a", Locale.ENGLISH)
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
        Matcher beneficiaryMatcher = BENEFICIARY_LABEL_PATTERN.matcher(text);
        while (beneficiaryMatcher.find()) {
            String candidate = cleanValue(beneficiaryMatcher.group(1));
            if (isReadableMerchant(candidate)) {
                return toTitleCase(candidate);
            }
        }

        Matcher recipientMatcher = RECIPIENT_PATTERN.matcher(text);
        while (recipientMatcher.find()) {
            String candidate = cleanValue(recipientMatcher.group(1));
            if (isReadableMerchant(candidate)) {
                return toTitleCase(candidate);
            }
        }

        String[] lines = text.split("\\R");
        for (String line : lines) {
            String candidate = line.trim();
            if (isReadableMerchant(candidate)) {
                return toTitleCase(candidate);
            }
        }
        return "Unknown merchant";
    }

    private static String detectSource(String text, String transactionType) {
        Matcher accountMatcher = ACCOUNT_PATTERN.matcher(text);
        while (accountMatcher.find()) {
            String candidate = cleanValue(accountMatcher.group(1));
            if (!candidate.isEmpty()) {
                return candidate;
            }
        }
        return "Income".equalsIgnoreCase(transactionType) ? "General" : "Unassigned";
    }

    private static String detectTransactionType(String text) {
        String haystack = text.toLowerCase(Locale.ENGLISH);
        if (containsAny(haystack, "received", "credited", "credit advice", "fund received", "incoming", "salary")) {
            return "Income";
        }
        return "Expense";
    }

    private static String detectCategory(String text, String merchant) {
        String recipientReference = detectRecipientReference(text);
        String haystack = (text + " " + merchant).toLowerCase(Locale.ENGLISH);
        if (containsAny(haystack, "third party transfer", "instant transfer", "bank transfer", "duitnow")) {
            if (!recipientReference.isEmpty() && !looksGenericReference(recipientReference)) {
                return toTitleCase(recipientReference);
            }
            return "Transfers";
        }
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
        if (containsAny(haystack, "transfer", "duitnow", "maybank2u", "instant transfer", "bank transfer")) {
            return "Transfers";
        }
        return "Miscellaneous";
    }

    private static String detectRecipientReference(String text) {
        Matcher matcher = RECIPIENT_REFERENCE_PATTERN.matcher(text);
        while (matcher.find()) {
            String candidate = cleanValue(matcher.group(1));
            if (!candidate.isEmpty()) {
                return candidate;
            }
        }
        return "";
    }

    private static boolean looksGenericReference(String value) {
        String lower = value.toLowerCase(Locale.ENGLISH);
        return containsAny(lower, "payment", "transfer", "fund", "receipt", "reference", "money");
    }

    private static boolean isReadableMerchant(String candidate) {
        String cleaned = cleanValue(candidate);
        if (cleaned.isEmpty() || cleaned.length() < 3) {
            return false;
        }

        String lower = cleaned.toLowerCase(Locale.ENGLISH);
        if (containsAny(lower, "reference", "status", "amount", "date", "time", "account", "transaction", "receipt")) {
            return false;
        }
        if (STATUS_LINE_PATTERN.matcher(cleaned).matches()) {
            return false;
        }
        if (containsAny(lower, "third party transfer", "maybank", "malayan banking", "computer generated", "signature required")) {
            return false;
        }

        return !cleaned.matches(".*\\d{4,}.*");
    }

    private static boolean looksLikeIdentifierLine(String line) {
        String lower = line.toLowerCase(Locale.ENGLISH);
        if (containsAny(lower, "reference", "account", "reg.", "co. reg", "beneficiary account", "recipient reference")) {
            return true;
        }
        return line.matches(".*\\d{6,}.*");
    }

    private static String cleanValue(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
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
