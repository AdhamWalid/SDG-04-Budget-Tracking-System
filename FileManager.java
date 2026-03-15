import java.io.*;
import java.util.ArrayList;

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
}