import java.time.LocalDate;

public class Income extends Transaction {

    private String source;

    public Income(double amount) {
        this(amount, "General");
    }

    public Income(double amount, LocalDate date) {
        this(amount, "General", date);
    }

    public Income(double amount, String source) {
        this(amount, source, LocalDate.now());
    }

    public Income(double amount, String source, LocalDate date) {
        super(amount, date);
        this.source = normalizeSource(source);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = normalizeSource(source);
    }

    private String normalizeSource(String value) {
        return value == null || value.trim().isEmpty() ? "General" : value.trim();
    }

    @Override
    public String getType() {
        return "Income";
    }
}
