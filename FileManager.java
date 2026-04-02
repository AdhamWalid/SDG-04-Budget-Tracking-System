import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class FileManager {

    public static void save(BudgetManager manager) {

        try {
            PrintWriter writer = new PrintWriter("transactions.txt");

            for (Category category : manager.getCategories().values()) {
                writer.println("CATEGORY," + category.getName() + "," + category.getLimit());
            }

            for (Transaction t : manager.getTransactions()) {

                if (t instanceof Expense) {
                    Expense e = (Expense) t;
                    writer.println("EXPENSE," + e.getAmount() + "," + e.getCategory() + "," + e.getSource() + ","
                            + t.getDate());
                } else {
                    Income income = (Income) t;
                    writer.println("INCOME," + t.getAmount() + "," + income.getSource() + "," + t.getDate());
                }
            }

            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== LOAD DATA =====
    public static void load(BudgetManager manager) {

        try {
            File file = new File("transactions.txt");
            if (!file.exists())
                return;

            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {

                String line = sc.nextLine();
                String[] parts = line.split(",");

                if (parts.length == 0) {
                    continue;
                }

                if (parts[0].equals("CATEGORY") && parts.length >= 3) {
                    String name = parts[1];
                    double limit = Double.parseDouble(parts[2]);
                    manager.addCategory(name, limit);
                } else if (parts[0].equals("INCOME")) {
                    double amount = Double.parseDouble(parts[1]);
                    String source = parts.length >= 4 ? parts[2] : "General";
                    LocalDate date = parts.length >= 4 ? LocalDate.parse(parts[3])
                            : parts.length >= 3 ? LocalDate.parse(parts[2]) : LocalDate.now();
                    manager.addIncome(amount, source, date);
                } else if (parts[0].equals("EXPENSE")) {
                    double amount = Double.parseDouble(parts[1]);
                    String category = parts[2];
                    String source = parts.length >= 5 ? parts[3] : "Unassigned";
                    LocalDate date = parts.length >= 5 ? LocalDate.parse(parts[4])
                            : parts.length >= 4 ? LocalDate.parse(parts[3]) : LocalDate.now();
                    manager.addExpense(amount, category, source, date);
                }
            }

            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
