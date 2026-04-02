public class Category {

    private String name;
    private double limit;
    private double spent;

    public Category(String name, double limit) {
        this.name = name;
        this.limit = limit;
        this.spent = 0;
    }

    public void addExpense(double amount) {
        spent += amount;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public boolean isOverLimit() {
        return spent > limit;
    }

    public String getName() {
        return name;
    }

    public double getSpent() {
        return spent;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }
}
