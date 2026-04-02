import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;

public class MainGUI {

    BudgetManager manager = new BudgetManager();
    DefaultTableModel model;
    JTextArea output;
    PieChartPanel pieChart = new PieChartPanel();

    JLabel incomeLabel = new JLabel();
    JLabel expenseLabel = new JLabel();
    JLabel balanceLabel = new JLabel();
    JLabel highestLabel = new JLabel();

    DecimalFormat df = new DecimalFormat("RM 0.00");

    public MainGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background", new Color(30, 30, 30));
            UIManager.put("OptionPane.background", new Color(30, 30, 30));
            UIManager.put("TextField.background", new Color(50, 50, 50));
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("Table.background", new Color(40, 40, 40));
            UIManager.put("Table.foreground", Color.WHITE);
            UIManager.put("Table.gridColor", Color.GRAY);
            UIManager.put("Label.foreground", Color.WHITE);
            UIManager.put("OptionPane.messageForeground", Color.WHITE);
        } catch (Exception ignored) {
        }

        JFrame frame = new JFrame("💰 Personal Finance Simulator");
        FileManager.load(manager);

        // ===== TABLE =====
        String[] columns = { "Type", "Amount", "Category" , "Date"};
        model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setBackground(new Color(40, 40, 40));
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.GRAY);
        table.getTableHeader().setBackground(new Color(60, 60, 60));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        // ===== OUTPUT LOG =====
        output = new JTextArea(8, 30);
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane outputScroll = new JScrollPane(output);

        // ===== PIE CHART =====
        pieChart.setPreferredSize(new Dimension(350, 350));
        pieChart.setBorder(BorderFactory.createTitledBorder("Expense Distribution"));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(outputScroll, BorderLayout.NORTH);
        rightPanel.add(pieChart, BorderLayout.CENTER);

        // ===== DASHBOARD =====
        JPanel dashboard = new JPanel(new GridLayout(1, 4));
        dashboard.setBackground(new Color(45, 62, 80));

        Font dashFont = new Font("Arial", Font.BOLD, 16);
        for (JLabel lbl : new JLabel[] { incomeLabel, expenseLabel, balanceLabel, highestLabel }) {
            lbl.setFont(dashFont);
            lbl.setForeground(Color.WHITE);
            dashboard.add(lbl);
        }

        // ===== BUTTONS =====
        JButton addIncome = styledButton("Add Income", new Color(46, 204, 113));
        JButton addExpense = styledButton("Add Expense", new Color(231, 76, 60));
        JButton addCategory = styledButton("Add Category", new Color(241, 196, 15));
        JButton delete = styledButton("Delete", new Color(127, 140, 141));
        JButton analytics = styledButton("Analytics", new Color(52, 152, 219));
        JButton save = styledButton("Save Data", new Color(155, 89, 182));

        JPanel topPanel = new JPanel();
        topPanel.add(addIncome);
        topPanel.add(addExpense);
        topPanel.add(addCategory);
        topPanel.add(delete);
        topPanel.add(analytics);
        topPanel.add(save);

        // ===== ACTIONS =====

        addIncome.addActionListener(e -> {
            Double amount = getAmount("Enter income amount:");
            if (amount == null)
                return;
            manager.addIncome(amount);
            log("Income added: " + df.format(amount));
            refreshAll();
        });

        addCategory.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Category name:");
            if (name == null || name.isEmpty())
                return;

            Double limit = getAmount("Spending limit:");
            if (limit == null)
                return;

            manager.addCategory(name, limit);
            log("Category added: " + name);
        });

        addExpense.addActionListener(e -> {
            Double amount = getAmount("Enter expense amount:");
            if (amount == null)
                return;

            String category = JOptionPane.showInputDialog("Enter category:");
            if (category == null || category.isEmpty())
                return;

            boolean warning = manager.addExpense(amount, category);
            log("Expense: " + df.format(amount) + " | " + category);

            if (warning)
                JOptionPane.showMessageDialog(frame, "⚠ Category limit exceeded!");

            refreshAll();
        });

        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "Select a row to delete.");
                return;
            }
            manager.removeTransaction(row);
            log("Transaction deleted");
            refreshAll();
        });

        analytics.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                ReportGenerator.generate(manager),
                "Analytics Report",
                JOptionPane.INFORMATION_MESSAGE));

        save.addActionListener(e -> {
            FileManager.save(manager.getTransactions());
            log("Data saved");
        });

        // ===== FRAME LAYOUT =====
        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(dashboard, BorderLayout.SOUTH);
        frame.add(rightPanel, BorderLayout.EAST);

        frame.setSize(1200, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        refreshAll();
    }

    // ===== REFRESH EVERYTHING =====
    private void refreshAll() {
        refreshTable();
        updateDashboard();
        pieChart.setData(manager.getExpenseByCategory());
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Transaction t : manager.getTransactions()) {
            if (t instanceof Expense) {
                Expense e = (Expense) t;
                model.addRow(new Object[] {
                        "Expense",
                        df.format(e.getAmount()),
                        e.getCategory(),
                        t.getDate()
                });
            } else {
                model.addRow(new Object[] {
                        "Income",
                        df.format(t.getAmount()),
                        "-",
                        t.getDate()
                });
            }
        }
    }

    private void updateDashboard() {
        incomeLabel.setText("Income: " + df.format(manager.getTotalIncome()));
        expenseLabel.setText("Expense: " + df.format(manager.getTotalExpense()));
        balanceLabel.setText("Balance: " + df.format(manager.getBalance()));
        highestLabel.setText("Highest Category: " + manager.getHighestCategory());
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        return btn;
    }

    private Double getAmount(String message) {
        try {
            String input = JOptionPane.showInputDialog(message);
            if (input == null)
                return null;
            return Double.parseDouble(input);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Invalid number!");
            return null;
        }
    }

    private void log(String msg) {
        output.append(msg + "\n");
    }
}