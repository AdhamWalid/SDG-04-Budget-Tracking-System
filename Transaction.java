import java.time.LocalDate;

public abstract class Transaction {

    protected double amount;
    protected LocalDate date; // ✅ store date

    public Transaction(double amount) {
        this.amount = amount;
        this.date = LocalDate.now(); // ✅ auto-set date when created
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() { // ✅ getter for GUI
        return date;
    }

    public abstract String getType();
}