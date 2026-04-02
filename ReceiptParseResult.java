import java.time.LocalDate;

public class ReceiptParseResult {

    private final double amount;
    private final String category;
    private final LocalDate date;
    private final String merchant;

    public ReceiptParseResult(double amount, String category, LocalDate date, String merchant) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.merchant = merchant;
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
}
