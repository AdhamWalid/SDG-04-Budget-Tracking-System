import java.util.ArrayList;

public class BudgetManager {

    private ArrayList<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public double getBalance() {

        double income = 0;
        double expense = 0;

        for (Transaction t : transactions) {

            if (t.getType().equals("Income"))
                income += t.getAmount();
            else
                expense += t.getAmount();
        }

        return income - expense;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }
}