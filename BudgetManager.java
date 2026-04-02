import java.util.*;
import java.time.YearMonth;

public class BudgetManager {

    private final ArrayList<Transaction> transactions = new ArrayList<>();
    private final HashMap<String, Category> categories = new HashMap<>();

    public void addCategory(String name, double limit) {
        String normalizedName = normalizeCategoryName(name);
        if (normalizedName.isEmpty()) {
            return;
        }

        Category existing = categories.get(normalizedName);
        if (existing == null) {
            categories.put(normalizedName, new Category(normalizedName, limit));
        } else {
            existing.setLimit(limit);
        }
    }

    public void addIncome(double amount) {
        transactions.add(new Income(amount));
    }

    public void addIncome(double amount, java.time.LocalDate date) {
        transactions.add(new Income(amount, date));
    }

    public void addIncome(double amount, String source) {
        transactions.add(new Income(amount, source));
    }

    public void addIncome(double amount, String source, java.time.LocalDate date) {
        transactions.add(new Income(amount, source, date));
    }

    public boolean addExpense(double amount, String categoryName) {
        return addExpense(amount, categoryName, "Unassigned", java.time.LocalDate.now());
    }

    public boolean addExpense(double amount, String categoryName, java.time.LocalDate date) {
        return addExpense(amount, categoryName, "Unassigned", date);
    }

    public boolean addExpense(double amount, String categoryName, String source, java.time.LocalDate date) {
        String normalizedName = normalizeCategoryName(categoryName);
        if (normalizedName.isEmpty()) {
            return false;
        }

        if (!categories.containsKey(normalizedName)) {
            categories.put(normalizedName, new Category(normalizedName, 1000));
        }

        Category cat = categories.get(normalizedName);

        Expense expense = new Expense(amount, normalizedName, normalizeSourceName(source), date);
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

    public void removeTransaction(int index) {
        if (index < 0 || index >= transactions.size()) {
            return;
        }

        transactions.remove(index);
        rebuildCategorySpending();
    }

    public void updateTransaction(int index, String type, double amount, String categoryName, String source,
            java.time.LocalDate date) {
        if (index < 0 || index >= transactions.size()) {
            return;
        }

        Transaction updatedTransaction;
        if ("Expense".equalsIgnoreCase(type)) {
            String normalizedName = normalizeCategoryName(categoryName);
            if (!categories.containsKey(normalizedName)) {
                categories.put(normalizedName, new Category(normalizedName, 1000));
            }
            updatedTransaction = new Expense(amount, normalizedName, source, date);
        } else {
            updatedTransaction = new Income(amount, source, date);
        }

        transactions.set(index, updatedTransaction);
        rebuildCategorySpending();
    }

    public double getTotalIncome() {
        return getTotalIncome(null);
    }

    public double getTotalIncome(YearMonth month) {
        double total = 0;
        for (Transaction t : transactions) {
            if (t instanceof Income && matchesMonth(t, month)) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public double getTotalExpense() {
        return getTotalExpense(null);
    }

    public double getTotalExpense(YearMonth month) {
        double total = 0;
        for (Transaction t : transactions) {
            if (t instanceof Expense && matchesMonth(t, month)) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public double getBalance(YearMonth month) {
        return getTotalIncome(month) - getTotalExpense(month);
    }

    public Map<String, Double> getExpenseByCategory() {
        return getExpenseByCategory(null);
    }

    public Map<String, Double> getExpenseByCategory(YearMonth month) {
        Map<String, Double> map = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            if (t instanceof Expense && matchesMonth(t, month)) {
                Expense e = (Expense) t;
                map.put(e.getCategory(),
                        map.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
            }
        }

        return map;
    }

    public Expense getHighestExpense() {
        Expense highest = null;
        for (Transaction t : transactions) {
            if (t instanceof Expense) {
                Expense expense = (Expense) t;
                if (highest == null || expense.getAmount() > highest.getAmount()) {
                    highest = expense;
                }
            }
        }
        return highest;
    }

    public double getAverageExpense() {
        int expenseCount = 0;
        double total = 0;

        for (Transaction t : transactions) {
            if (t instanceof Expense) {
                total += t.getAmount();
                expenseCount++;
            }
        }

        return expenseCount == 0 ? 0 : total / expenseCount;
    }

    public YearMonth getLatestTransactionMonth() {
        if (transactions.isEmpty()) {
            return YearMonth.now();
        }

        YearMonth latest = YearMonth.from(transactions.get(0).getDate());
        for (Transaction t : transactions) {
            YearMonth candidate = YearMonth.from(t.getDate());
            if (candidate.isAfter(latest)) {
                latest = candidate;
            }
        }
        return latest;
    }

    public List<String> getIncomeSources() {
        LinkedHashSet<String> sources = new LinkedHashSet<>();
        for (Transaction transaction : transactions) {
            if (transaction instanceof Income) {
                sources.add(((Income) transaction).getSource());
            }
        }
        if (sources.isEmpty()) {
            sources.add("General");
        }
        return new ArrayList<>(sources);
    }

    public double getSourceBalance(String source) {
        String normalizedSource = normalizeSourceName(source);
        double total = 0;
        for (Transaction transaction : transactions) {
            if (transaction instanceof Income && ((Income) transaction).getSource().equalsIgnoreCase(normalizedSource)) {
                total += transaction.getAmount();
            } else if (transaction instanceof Expense
                    && ((Expense) transaction).getSource().equalsIgnoreCase(normalizedSource)) {
                total -= transaction.getAmount();
            }
        }
        return total;
    }

    public Map<String, Double> getIncomeTotalsBySource() {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (Transaction transaction : transactions) {
            if (transaction instanceof Income) {
                Income income = (Income) transaction;
                totals.put(income.getSource(), totals.getOrDefault(income.getSource(), 0.0) + income.getAmount());
            }
        }
        return totals;
    }

    public Map<String, Double> getExpenseTotalsBySource() {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (Transaction transaction : transactions) {
            if (transaction instanceof Expense) {
                Expense expense = (Expense) transaction;
                totals.put(expense.getSource(), totals.getOrDefault(expense.getSource(), 0.0) + expense.getAmount());
            }
        }
        return totals;
    }

    private void rebuildCategorySpending() {
        for (Category category : categories.values()) {
            category.setSpent(0);
        }

        for (Transaction transaction : transactions) {
            if (transaction instanceof Expense) {
                Expense expense = (Expense) transaction;
                categories.putIfAbsent(expense.getCategory(), new Category(expense.getCategory(), 1000));
                categories.get(expense.getCategory()).addExpense(expense.getAmount());
            }
        }
    }

    private String normalizeCategoryName(String name) {
        return name == null ? "" : name.trim();
    }

    private String normalizeSourceName(String name) {
        return name == null || name.trim().isEmpty() ? "Unassigned" : name.trim();
    }

    private boolean matchesMonth(Transaction transaction, YearMonth month) {
        return month == null || YearMonth.from(transaction.getDate()).equals(month);
    }
}
