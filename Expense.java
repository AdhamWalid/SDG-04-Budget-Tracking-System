public class Expense extends Transaction {

    private String category;

    public Expense(double amount, String category) {
        super(amount);
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String getType() {
        return "Expense";
    }
}