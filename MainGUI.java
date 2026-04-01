import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MainGUI {

    BudgetManager manager = new BudgetManager();

    DefaultTableModel model;
    JTextArea output;

    JLabel incomeLabel = new JLabel("Income: 0");
    JLabel expenseLabel = new JLabel("Expense: 0");
    JLabel balanceLabel = new JLabel("Balance: 0");
    JLabel highestLabel = new JLabel("Highest Category: None");

    public MainGUI() {

        JFrame frame = new JFrame("Personal Finance Simulator");

        // ===== LOAD PREVIOUS DATA =====
        FileManager.load(manager);

        // ===== TABLE =====
        String[] columns = { "Type", "Amount", "Category" };
        model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        // Fill table from loaded data
        for (Transaction t : manager.getTransactions()) {
            if (t instanceof Expense) {
                Expense e = (Expense) t;
                model.addRow(new Object[] { "Expense", e.getAmount(), e.getCategory() });
            } else {
                model.addRow(new Object[] { "Income", t.getAmount(), "-" });
            }
        }

        // ===== OUTPUT =====
        output = new JTextArea(6, 30);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));
        output.setBackground(new Color(240, 240, 240));
        JScrollPane outputScroll = new JScrollPane(output);

        // ===== DASHBOARD (VISUAL) =====
        JPanel dashboard = new JPanel();
        dashboard.setBackground(new Color(30, 144, 255));
        dashboard.setLayout(new GridLayout(1, 4));

        Font dashFont = new Font("Arial", Font.BOLD, 16);
        incomeLabel.setFont(dashFont);
        expenseLabel.setFont(dashFont);
        balanceLabel.setFont(dashFont);
        highestLabel.setFont(dashFont);

        incomeLabel.setForeground(Color.WHITE);
        expenseLabel.setForeground(Color.WHITE);
        balanceLabel.setForeground(Color.WHITE);
        highestLabel.setForeground(Color.WHITE);

        dashboard.add(incomeLabel);
        dashboard.add(expenseLabel);
        dashboard.add(balanceLabel);
        dashboard.add(highestLabel);

        // ===== BUTTONS =====
        JButton addIncome = new JButton("Add Income");
        JButton addExpense = new JButton("Add Expense");
        JButton addCategory = new JButton("Add Category");
        JButton analytics = new JButton("Analytics");
        JButton save = new JButton("Save");

        addIncome.setBackground(new Color(60, 179, 113));
        addExpense.setBackground(new Color(255, 99, 71));
        addCategory.setBackground(new Color(255, 215, 0));

        // ===== ACTIONS =====

        addIncome.addActionListener(e -> {
            double amount = Double.parseDouble(JOptionPane.showInputDialog("Enter income amount:"));
            manager.addIncome(amount);
            model.addRow(new Object[] { "Income", amount, "-" });
            updateDashboard();
        });

        addCategory.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Category name:");
            double limit = Double.parseDouble(JOptionPane.showInputDialog("Spending limit:"));
            manager.addCategory(name, limit);
        });

        addExpense.addActionListener(e -> {
            double amount = Double.parseDouble(JOptionPane.showInputDialog("Enter expense amount:"));
            String category = JOptionPane.showInputDialog("Enter category:");

            boolean warning = manager.addExpense(amount, category);
            model.addRow(new Object[] { "Expense", amount, category });

            if (warning)
                JOptionPane.showMessageDialog(frame, "⚠ Category limit exceeded!");

            updateDashboard();
        });

        analytics.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                    ReportGenerator.generate(manager),
                    "Analytics Report",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        save.addActionListener(e -> {
            FileManager.save(manager.getTransactions());
            JOptionPane.showMessageDialog(frame, "Data Saved!");
        });

        JPanel topPanel = new JPanel();
        topPanel.add(addIncome);
        topPanel.add(addExpense);
        topPanel.add(addCategory);
        topPanel.add(analytics);
        topPanel.add(save);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(dashboard, BorderLayout.SOUTH);
        frame.add(outputScroll, BorderLayout.EAST);

        frame.setSize(1000, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        updateDashboard(); // initial update
    }

    private void updateDashboard() {

        double income = 0;
        double expense = 0;

        for (Transaction t : manager.getTransactions()) {
            if (t instanceof Income)
                income += t.getAmount();
            else
                expense += t.getAmount();
        }

        incomeLabel.setText("Income: " + income);
        expenseLabel.setText("Expense: " + expense);
        balanceLabel.setText("Balance: " + manager.getBalance());
        highestLabel.setText("Highest Category: " + manager.getHighestCategory());
    }
}