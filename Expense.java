import java.time.LocalDate;

public class Expense extends Transaction {

    private String category;
    private String source;

    public Expense(double amount, String category) {
        this(amount, category, "Unassigned");
    }

    public Expense(double amount, String category, LocalDate date) {
        this(amount, category, "Unassigned", date);
    }

    public Expense(double amount, String category, String source) {
        this(amount, category, source, LocalDate.now());
    }

    public Expense(double amount, String category, String source, LocalDate date) {
        super(amount, date);
        this.category = category;
        this.source = normalizeSource(source);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = normalizeSource(source);
    }

    private String normalizeSource(String value) {
        return value == null || value.trim().isEmpty() ? "Unassigned" : value.trim();
    }

    @Override
    public String getType() {
        return "Expense";
    }
}
