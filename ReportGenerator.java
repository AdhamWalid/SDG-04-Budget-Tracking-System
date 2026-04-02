import java.text.DecimalFormat;
import java.time.YearMonth;
import java.util.Map;

public class ReportGenerator {

    public static String generate(BudgetManager manager) {
        DecimalFormat df = new DecimalFormat("RM 0.00");
        YearMonth latestMonth = manager.getLatestTransactionMonth();
        Expense highestExpense = manager.getHighestExpense();

        StringBuilder report = new StringBuilder();

        report.append("=== ANALYTICS REPORT ===\n");
        report.append("Total Income: ").append(df.format(manager.getTotalIncome())).append("\n");
        report.append("Total Expense: ").append(df.format(manager.getTotalExpense())).append("\n");
        report.append("Current Balance: ").append(df.format(manager.getBalance())).append("\n\n");

        report.append("=== SAVINGS GOAL ===\n");
        if (manager.getSavingsGoal() > 0) {
            report.append("Goal: ").append(df.format(manager.getSavingsGoal())).append("\n");
            report.append("Progress: ").append(String.format("%.1f%%", manager.getSavingsGoalProgress() * 100)).append("\n\n");
        } else {
            report.append("No savings goal set\n\n");
        }

        report.append("=== ").append(latestMonth).append(" SUMMARY ===\n");
        report.append("Income: ").append(df.format(manager.getTotalIncome(latestMonth))).append("\n");
        report.append("Expense: ").append(df.format(manager.getTotalExpense(latestMonth))).append("\n");
        report.append("Savings: ").append(df.format(manager.getBalance(latestMonth))).append("\n\n");

        report.append("=== CATEGORY BREAKDOWN ===\n");
        double totalExpense = manager.getTotalExpense();

        for (Category c : manager.getCategories().values()) {
            double percentage = totalExpense == 0 ? 0 : (c.getSpent() / totalExpense) * 100;
            report.append(c.getName())
                  .append(" -> Spent: ")
                  .append(df.format(c.getSpent()))
                  .append(" / Limit: ")
                  .append(df.format(c.getLimit()))
                  .append(" (")
                  .append(String.format("%.1f", percentage))
                  .append("%)")
                  .append(c.isOverLimit() ? " OVER LIMIT" : "")
                  .append("\n");
        }

        report.append("\n=== INSIGHTS ===\n");
        report.append("Highest Category: ").append(manager.getHighestCategory()).append("\n");
        report.append("Average Expense: ").append(df.format(manager.getAverageExpense())).append("\n");
        if (highestExpense != null) {
            report.append("Highest Expense: ")
                  .append(df.format(highestExpense.getAmount()))
                  .append(" in ")
                  .append(highestExpense.getCategory())
                  .append(" on ")
                  .append(highestExpense.getDate())
                  .append("\n");
        } else {
            report.append("Highest Expense: None\n");
        }

        report.append("\n=== MONTHLY CATEGORY BREAKDOWN ===\n");
        for (Map.Entry<String, Double> entry : manager.getExpenseByCategory(latestMonth).entrySet()) {
            report.append(entry.getKey())
                  .append(": ")
                  .append(df.format(entry.getValue()))
                  .append("\n");
        }

        report.append("\n=== INCOME STREAMS ===\n");
        Map<String, Double> incomeBySource = manager.getIncomeTotalsBySource();
        if (incomeBySource.isEmpty()) {
            report.append("No income streams recorded\n");
        } else {
            for (Map.Entry<String, Double> entry : incomeBySource.entrySet()) {
                report.append(entry.getKey())
                      .append(": ")
                      .append(df.format(entry.getValue()))
                      .append(" | Balance after deductions: ")
                      .append(df.format(manager.getSourceBalance(entry.getKey())))
                      .append("\n");
            }
        }

        report.append("\n=== EXPENSES BY SOURCE ===\n");
        Map<String, Double> expenseBySource = manager.getExpenseTotalsBySource();
        if (expenseBySource.isEmpty()) {
            report.append("No source-linked expenses recorded\n");
        } else {
            for (Map.Entry<String, Double> entry : expenseBySource.entrySet()) {
                report.append(entry.getKey())
                      .append(": ")
                      .append(df.format(entry.getValue()))
                      .append("\n");
            }
        }

        return report.toString();
    }
}
