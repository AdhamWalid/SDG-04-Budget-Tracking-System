import java.time.LocalDate;

public class ReceiptParseResult {

    private final double amount;
    private final String category;
    private final LocalDate date;
    private final String merchant;
    private final String source;
    private final String transactionType;

    public ReceiptParseResult(double amount, String category, LocalDate date, String merchant,
            String source, String transactionType) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.merchant = merchant;
        this.source = source;
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getMerchant() {
        return merchant;
    }

    public String getSource() {
        return source;
    }

    public String getTransactionType() {
        return transactionType;
    }
}
