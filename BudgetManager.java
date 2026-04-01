import java.util.*;

public class BudgetManager {

    private ArrayList<Transaction> transactions = new ArrayList<>();
    private HashMap<String, Category> categories = new HashMap<>();

    public void addCategory(String name, double limit) {
        categories.put(name, new Category(name, limit));
    }

    public void addIncome(double amount) {
        transactions.add(new Income(amount));
    }

    public boolean addExpense(double amount, String categoryName) {

        if (!categories.containsKey(categoryName)) {
            categories.put(categoryName, new Category(categoryName, 1000));
        }

        Category cat = categories.get(categoryName);

        Expense expense = new Expense(amount, categoryName);
        transactions.add(expense);

        cat.addExpense(amount);

        return cat.isOverLimit();
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public HashMap<String, Category> getCategories() {
        return categories;
    }

    public double getBalance() {

        double income = 0, expense = 0;

        for (Transaction t : transactions) {
            if (t instanceof Income)
                income += t.getAmount();
            else
                expense += t.getAmount();
        }

        return income - expense;
    }

    public String getHighestCategory() {

        String highest = "None";
        double max = 0;

        for (Category c : categories.values()) {
            if (c.getSpent() > max) {
                max = c.getSpent();
                highest = c.getName();
            }
        }

        return highest;
    }
}