import java.io.*;
import java.util.*;

public class FileManager {

    public static void save(ArrayList<Transaction> list) {

        try {
            PrintWriter writer = new PrintWriter("transactions.txt");

            for (Transaction t : list) {

                if (t instanceof Expense) {
                    Expense e = (Expense) t;
                    writer.println("EXPENSE," + e.getAmount() + "," + e.getCategory());
                } else {
                    writer.println("INCOME," + t.getAmount());
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

                if (parts[0].equals("INCOME")) {
                    double amount = Double.parseDouble(parts[1]);
                    manager.addIncome(amount);
                } else if (parts[0].equals("EXPENSE")) {
                    double amount = Double.parseDouble(parts[1]);
                    String category = parts[2];
                    manager.addExpense(amount, category);
                }
            }

            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}