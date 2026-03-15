import javax.swing.*;

public class MainGUI {

    BudgetManager manager = new BudgetManager();

    public MainGUI() {

        JFrame frame = new JFrame("Expense Tracker");

        JButton addIncome = new JButton("Add Income");
        JButton addExpense = new JButton("Add Expense");
        JButton showBalance = new JButton("Show Balance");
        JButton save = new JButton("Save Data");

        JTextArea output = new JTextArea();

        addIncome.addActionListener(e -> {

            String input = JOptionPane.showInputDialog("Enter income amount:");

            double amount = Double.parseDouble(input);

            manager.addTransaction(new Income(amount));

            output.append("Income added: " + amount + "\n");
        });

        addExpense.addActionListener(e -> {

            String amountInput = JOptionPane.showInputDialog("Enter expense amount:");
            String category = JOptionPane.showInputDialog("Enter category:");

            double amount = Double.parseDouble(amountInput);

            manager.addTransaction(new Expense(amount, category));

            output.append("Expense added: " + amount + " (" + category + ")\n");
        });

        showBalance.addActionListener(e -> {

            double balance = manager.getBalance();

            output.append("Current Balance: " + balance + "\n");
        });

        save.addActionListener(e -> {

            FileManager.save(manager.getTransactions());

            output.append("Data saved to file.\n");
        });

        JPanel panel = new JPanel();

        panel.add(addIncome);
        panel.add(addExpense);
        panel.add(showBalance);
        panel.add(save);

        frame.add(panel, "North");
        frame.add(new JScrollPane(output), "Center");

        frame.setSize(500,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}