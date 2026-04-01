public class ReportGenerator {

    public static String generate(BudgetManager manager) {

        StringBuilder report = new StringBuilder();

        report.append("=== ANALYTICS REPORT ===\n");

        for (Category c : manager.getCategories().values()) {
            report.append(c.getName())
                  .append(" -> Spent: ")
                  .append(c.getSpent())
                  .append(" / Limit: ")
                  .append(c.getLimit())
                  .append("\n");
        }

        report.append("\nBalance: ").append(manager.getBalance());

        return report.toString();
    }
}