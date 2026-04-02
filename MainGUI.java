import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainGUI {

    private static final Color APP_BG = new Color(6, 11, 22);
    private static final Color PANEL_BG = new Color(16, 24, 39);
    private static final Color PANEL_ALT = new Color(11, 19, 33);
    private static final Color HEADER_BG = new Color(10, 18, 34);
    private static final Color TEXT_PRIMARY = new Color(245, 247, 255);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color BORDER = new Color(56, 70, 94);
    private static final Color SUCCESS = new Color(52, 211, 153);
    private static final Color DANGER = new Color(248, 113, 113);
    private static final Color WARNING = new Color(251, 191, 36);
    private static final Color INFO = new Color(96, 165, 250);
    private static final Color SECONDARY = new Color(100, 116, 139);
    private static final Color HIGHLIGHT = new Color(14, 165, 233);
    private static final Color CARD_TOP = new Color(29, 41, 64);
    private static final Color CARD_BOTTOM = new Color(14, 22, 38);
    private static final Color SURFACE_OVERLAY = new Color(255, 255, 255, 10);

    private final BudgetManager manager = new BudgetManager();
    private final DefaultTableModel model;
    private final JTextArea output;
    private final PieChartPanel pieChart = new PieChartPanel();
    private final List<Integer> visibleTransactionIndexes = new ArrayList<>();

    private final JLabel incomeLabel = new JLabel();
    private final JLabel expenseLabel = new JLabel();
    private final JLabel balanceLabel = new JLabel();
    private final JLabel highestLabel = new JLabel();
    private final JLabel monthIncomeLabel = new JLabel();
    private final JLabel monthBalanceLabel = new JLabel();
    private final JTextArea recentActivityWidget = new JTextArea();
    private final JTextArea alertsWidget = new JTextArea();
    private final JTextArea budgetHealthWidget = new JTextArea();

    private final JComboBox<String> typeFilter = new JComboBox<>(new String[] { "All", "Income", "Expense" });
    private final JTextField categoryFilter = new JTextField(10);
    private final JTextField monthFilter = new JTextField(7);
    private final JLabel filterStatusLabel = new JLabel("Showing all transactions");

    private final DecimalFormat df = new DecimalFormat("RM 0.00");
    private JFrame mainFrame;

    public MainGUI() {
        configureLookAndFeel();

        JFrame frame = new JFrame("Personal Finance Simulator");
        this.mainFrame = frame;
        frame.getContentPane().setBackground(APP_BG);
        FileManager.load(manager);

        String[] columns = { "Type", "Amount", "Category", "Source", "Date" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = createTable();

        output = new JTextArea(8, 30);
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));
        output.setBackground(PANEL_ALT);
        output.setForeground(TEXT_PRIMARY);
        output.setCaretColor(TEXT_PRIMARY);
        output.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane outputScroll = new JScrollPane(output);
        styleScrollPane(outputScroll);

        pieChart.setPreferredSize(new Dimension(340, 340));

        JPanel summaryPanel = createSummaryPanel();
        JPanel homeWidgetsPanel = createHomeWidgetsPanel();
        JPanel actionPanel = createActionPanel(frame, table);
        JPanel filtersPanel = createFiltersPanel();
        JPanel northPanel = new JPanel(new BorderLayout(0, 16));
        northPanel.setOpaque(false);
        northPanel.add(createHeroPanel(), BorderLayout.NORTH);
        northPanel.add(actionPanel, BorderLayout.CENTER);
        northPanel.add(filtersPanel, BorderLayout.SOUTH);

        JPanel leftTop = new JPanel(new BorderLayout(0, 16));
        leftTop.setOpaque(false);
        leftTop.add(summaryPanel, BorderLayout.NORTH);
        leftTop.add(homeWidgetsPanel, BorderLayout.CENTER);

        JPanel leftContent = new JPanel(new BorderLayout(0, 16));
        leftContent.setOpaque(false);
        leftContent.add(leftTop, BorderLayout.NORTH);
        leftContent.add(createTablePanel(table), BorderLayout.CENTER);

        JPanel sidePanel = new JPanel(new BorderLayout(0, 16));
        sidePanel.setOpaque(false);
        sidePanel.add(createSectionPanel("Activity Log", outputScroll), BorderLayout.NORTH);
        sidePanel.add(createSectionPanel("Expense Distribution", pieChart), BorderLayout.CENTER);

        JPanel content = createBackgroundCanvas();
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        content.add(northPanel, BorderLayout.NORTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContent, sidePanel);
        splitPane.setOpaque(false);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(10);
        splitPane.setResizeWeight(0.72);
        splitPane.setContinuousLayout(true);
        splitPane.setBackground(APP_BG);
        splitPane.setLeftComponent(leftContent);
        splitPane.setRightComponent(sidePanel);
        content.add(splitPane, BorderLayout.CENTER);

        JScrollPane rootScroll = new JScrollPane(content);
        rootScroll.setBorder(BorderFactory.createEmptyBorder());
        rootScroll.getViewport().setBackground(APP_BG);
        styleScrollPane(rootScroll);

        frame.setLayout(new BorderLayout());
        frame.add(rootScroll, BorderLayout.CENTER);
        frame.setSize(1380, 780);
        frame.setMinimumSize(new Dimension(980, 620));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        refreshAll();
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        UIManager.put("Panel.background", APP_BG);
        UIManager.put("OptionPane.background", PANEL_BG);
        UIManager.put("TextField.background", PANEL_ALT);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", TEXT_PRIMARY);
        UIManager.put("ComboBox.background", PANEL_ALT);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", HIGHLIGHT);
        UIManager.put("ComboBox.selectionForeground", TEXT_PRIMARY);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
    }

    private JTable createTable() {
        JTable table = new JTable(model);
        table.setBackground(PANEL_BG);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setSelectionBackground(HIGHLIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.getTableHeader().setBackground(PANEL_ALT);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 12, 0, 12));

                if (!isSelected) {
                    component.setBackground(row % 2 == 0 ? PANEL_BG : PANEL_ALT);
                    component.setForeground(TEXT_PRIMARY);

                    String type = String.valueOf(table.getValueAt(row, 0));
                    if (column == 0) {
                        component.setForeground("Income".equals(type) ? SUCCESS : DANGER);
                    }
                }

                return component;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        return table;
    }

    private JPanel createHeroPanel() {
        GradientPanel panel = new GradientPanel(new Color(18, 36, 64), new Color(8, 16, 30), 30);
        panel.setLayout(new BorderLayout(24, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(90, 112, 150), 1, true),
                new EmptyBorder(24, 24, 24, 24)));

        JLabel eyebrow = new JLabel("FINANCE CONTROL CENTER");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 12));
        eyebrow.setForeground(new Color(125, 211, 252));

        JLabel title = new JLabel("Personal Finance Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Track every ringgit with clearer actions, stronger insights, and a smoother daily workflow.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(214, 223, 238));

        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statsRow.setOpaque(false);
        statsRow.add(createTag("Live Overview", INFO));
        statsRow.add(createTag("Receipt Import", new Color(244, 114, 182)));
        statsRow.add(createTag("PDF Reports", new Color(167, 139, 250)));

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        textPanel.setOpaque(false);
        textPanel.add(eyebrow);
        textPanel.add(title);
        textPanel.add(subtitle);
        textPanel.add(statsRow);

        JPanel spotlight = new GradientPanel(new Color(255, 255, 255, 22), new Color(148, 163, 184, 12), 24);
        spotlight.setLayout(new GridLayout(0, 1, 0, 8));
        spotlight.setBorder(new EmptyBorder(18, 18, 18, 18));
        spotlight.add(createMiniStat("Balance", balanceLabel.getText().isEmpty() ? "RM 0.00" : balanceLabel.getText()));
        spotlight.add(createMiniStat("Best For", "Fast reviews and cleaner budgeting"));
        spotlight.add(createMiniStat("Latest Focus", monthBalanceLabel.getText().isEmpty() ? "Monthly snapshot" : monthBalanceLabel.getText()));

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(spotlight, BorderLayout.EAST);
        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel cards = new JPanel(new GridLayout(2, 3, 14, 14));
        cards.setOpaque(false);

        cards.add(createMetricCard("Total Income", incomeLabel, SUCCESS));
        cards.add(createMetricCard("Total Expense", expenseLabel, DANGER));
        cards.add(createMetricCard("Balance", balanceLabel, INFO));
        cards.add(createMetricCard("Top Category", highestLabel, WARNING));
        cards.add(createMetricCard("Latest Month Income", monthIncomeLabel, new Color(16, 185, 129)));
        cards.add(createMetricCard("Latest Month Balance", monthBalanceLabel, new Color(6, 182, 212)));

        return cards;
    }

    private JPanel createHomeWidgetsPanel() {
        configureWidgetArea(recentActivityWidget);
        configureWidgetArea(alertsWidget);
        configureWidgetArea(budgetHealthWidget);

        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 14));
        grid.setOpaque(false);
        grid.add(createWidgetCard("Recent Activity", "Latest transactions at a glance", recentActivityWidget));
        grid.add(createWidgetCard("Alerts", "Warnings and notable budget signals", alertsWidget));
        grid.add(createWidgetCard("Budget Health", "Snapshot of your spending position", budgetHealthWidget));
        return grid;
    }

    private JPanel createWidgetCard(String title, String subtitle, JTextArea area) {
        GradientPanel panel = new GradientPanel(CARD_TOP, CARD_BOTTOM, 26);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel header = new JPanel(new GridLayout(0, 1, 0, 4));
        header.setOpaque(false);
        header.add(titleLabel);
        header.add(subtitleLabel);

        panel.add(header, BorderLayout.NORTH);
        panel.add(area, BorderLayout.CENTER);
        return panel;
    }

    private void configureWidgetArea(JTextArea area) {
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(TEXT_PRIMARY);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accent) {
        GradientPanel card = new GradientPanel(CARD_TOP, CARD_BOTTOM, 26);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 16, 16)));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(0, 5));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 19));

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        card.add(accentBar, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionPanel(JFrame frame, JTable table) {
        JButton addIncome = styledButton("Add Income", SUCCESS);
        JButton addExpense = styledButton("Add Expense", DANGER);
        JButton addCategory = styledButton("Add Category", WARNING);
        JButton edit = styledButton("Edit", new Color(249, 115, 22));
        JButton delete = styledButton("Delete", SECONDARY);
        JButton analytics = styledButton("Analytics", INFO);
        JButton importReceipt = styledButton("Import Receipt", new Color(236, 72, 153));
        JButton save = styledButton("Save Data", new Color(14, 165, 233));
        JButton exportPdf = styledButton("Export PDF", new Color(168, 85, 247));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(addIncome);
        buttons.add(addExpense);
        buttons.add(addCategory);
        buttons.add(edit);
        buttons.add(delete);
        buttons.add(analytics);
        buttons.add(importReceipt);
        buttons.add(save);
        buttons.add(exportPdf);

        JPanel panel = createSectionPanel("Quick Actions", buttons);

        addIncome.addActionListener(e -> {
            showIncomeDialog(frame, null, null, null, null, null, "Income added");
        });

        addCategory.addActionListener(e -> {
            String name = showTextInputDialog(frame, "Add Category", "Category name", "");
            if (name == null || name.trim().isEmpty()) {
                showMessageDialog(frame, "Category name cannot be empty.", "Missing Category");
                return;
            }

            Double limit = getValidAmount("Spending limit:");
            if (limit == null) {
                return;
            }

            manager.addCategory(name, limit);
            log("Category saved: " + name.trim());
            refreshAll();
        });

        addExpense.addActionListener(e -> {
            showExpenseDialog(frame, null, null, null, null, null, null, "Manual expense");
        });

        edit.addActionListener(e -> editSelectedTransaction(frame, table));

        delete.addActionListener(e -> {
            Integer index = getSelectedTransactionIndex(frame, table);
            if (index == null) {
                return;
            }

            manager.removeTransaction(index);
            log("Transaction deleted");
            refreshAll();
        });

        analytics.addActionListener(e -> new AnalyticsDialog(frame, manager).setVisible(true));
        importReceipt.addActionListener(e -> importReceipt(frame));

        save.addActionListener(e -> {
            FileManager.save(manager);
            log("Data saved");
        });

        exportPdf.addActionListener(e -> exportReportAsPdf(frame));

        return panel;
    }

    private JPanel createFiltersPanel() {
        JLabel helper = new JLabel("Tip: enter month as YYYY-MM");
        helper.setForeground(TEXT_MUTED);
        helper.setFont(new Font("SansSerif", Font.PLAIN, 12));
        filterStatusLabel.setForeground(TEXT_MUTED);
        filterStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        styleComboBox(typeFilter);
        styleInput(categoryFilter);
        styleInput(monthFilter);

        JButton applyFilters = styledButton("Apply Filter", new Color(20, 184, 166));
        JButton clearFilters = styledButton("Clear", SECONDARY);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.add(createFieldLabel("Type"));
        row.add(typeFilter);
        row.add(createFieldLabel("Category"));
        row.add(categoryFilter);
        row.add(createFieldLabel("Month"));
        row.add(monthFilter);
        row.add(applyFilters);
        row.add(clearFilters);

        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);
        container.add(row, BorderLayout.NORTH);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(helper, BorderLayout.WEST);
        footer.add(filterStatusLabel, BorderLayout.EAST);
        container.add(footer, BorderLayout.SOUTH);

        applyFilters.addActionListener(e -> refreshAll());
        clearFilters.addActionListener(e -> {
            typeFilter.setSelectedIndex(0);
            categoryFilter.setText("");
            monthFilter.setText("");
            refreshAll();
        });
        typeFilter.addActionListener(e -> refreshAll());
        addLiveRefresh(categoryFilter);
        addLiveRefresh(monthFilter);

        return createSectionPanel("Filters", container);
    }

    private JPanel createTablePanel(JTable table) {
        JScrollPane tableScroll = new JScrollPane(table);
        styleScrollPane(tableScroll);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        return createSectionPanel("Transactions", tableScroll);
    }

    private JPanel createSectionPanel(String title, Component content) {
        GradientPanel panel = new GradientPanel(CARD_TOP, CARD_BOTTOM, 28);
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel chip = new JLabel("Live");
        chip.setOpaque(true);
        chip.setBackground(new Color(255, 255, 255, 22));
        chip.setForeground(new Color(191, 219, 254));
        chip.setFont(new Font("SansSerif", Font.BOLD, 11));
        chip.setBorder(new EmptyBorder(6, 10, 6, 10));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(chip, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    private void styleInput(JComponent component) {
        component.setBackground(PANEL_ALT);
        component.setForeground(TEXT_PRIMARY);
        component.setFont(new Font("SansSerif", Font.PLAIN, 13));
        component.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(82, 96, 122), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        component.setPreferredSize(new Dimension(420, 42));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(PANEL_ALT);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        comboBox.setFocusable(false);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(82, 96, 122), 1, true),
                new EmptyBorder(2, 8, 2, 8)));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(8, 10, 8, 10));
                label.setFont(new Font("SansSerif", Font.PLAIN, 13));
                if (isSelected) {
                    label.setBackground(HIGHLIGHT);
                    label.setForeground(TEXT_PRIMARY);
                } else {
                    label.setBackground(index == -1 ? PANEL_ALT : PANEL_BG);
                    label.setForeground(TEXT_PRIMARY);
                }
                return label;
            }
        });
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("\u25BE");
                button.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                button.setForeground(TEXT_MUTED);
                button.setFont(new Font("SansSerif", Font.BOLD, 11));
                return button;
            }
        });
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.setBorder(new LineBorder(new Color(74, 88, 114), 1, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, Integer.MAX_VALUE));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 12));
        scrollPane.getVerticalScrollBar().setUI(createScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(createScrollBarUI());
    }

    private void refreshAll() {
        refreshTable();
        updateDashboard();
        updateHomeWidgets();
        pieChart.setData(manager.getExpenseByCategory(parseMonthFilter()));
    }

    private void refreshTable() {
        model.setRowCount(0);
        visibleTransactionIndexes.clear();

        String selectedType = (String) typeFilter.getSelectedItem();
        String categoryText = categoryFilter.getText().trim().toLowerCase();
        YearMonth selectedMonth = parseMonthFilter();
        boolean hasInvalidMonth = hasInvalidMonthFilter();
        styleFilterValidation(!hasInvalidMonth);

        List<Transaction> transactions = manager.getTransactions();
        for (int index = 0; index < transactions.size(); index++) {
            Transaction transaction = transactions.get(index);
            if (!matchesType(transaction, selectedType)) {
                continue;
            }
            if (!matchesCategory(transaction, categoryText)) {
                continue;
            }
            if (selectedMonth != null && !YearMonth.from(transaction.getDate()).equals(selectedMonth)) {
                continue;
            }

            if (transaction instanceof Expense) {
                Expense expense = (Expense) transaction;
                model.addRow(new Object[] {
                        "Expense",
                        df.format(expense.getAmount()),
                        expense.getCategory(),
                        expense.getSource(),
                        expense.getDate()
                });
            } else {
                Income income = (Income) transaction;
                model.addRow(new Object[] {
                        "Income",
                        df.format(transaction.getAmount()),
                        "-",
                        income.getSource(),
                        transaction.getDate()
                });
            }

            visibleTransactionIndexes.add(index);
        }

        updateFilterStatus(hasInvalidMonth);
    }

    private void updateDashboard() {
        YearMonth latestMonth = manager.getLatestTransactionMonth();
        incomeLabel.setText(df.format(manager.getTotalIncome()));
        expenseLabel.setText(df.format(manager.getTotalExpense()));
        balanceLabel.setText(df.format(manager.getBalance()));
        highestLabel.setText(manager.getHighestCategory());
        monthIncomeLabel.setText(latestMonth + "  " + df.format(manager.getTotalIncome(latestMonth)));
        monthBalanceLabel.setText(latestMonth + "  " + df.format(manager.getBalance(latestMonth)));
    }

    private void updateHomeWidgets() {
        updateRecentActivityWidget();
        updateAlertsWidget();
        updateBudgetHealthWidget();
    }

    private void updateRecentActivityWidget() {
        List<Transaction> transactions = manager.getTransactions();
        if (transactions.isEmpty()) {
            recentActivityWidget.setText("No transactions yet.\nAdd income or expenses to start building your activity feed.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        int start = Math.max(0, transactions.size() - 5);
        for (int i = transactions.size() - 1; i >= start; i--) {
            Transaction transaction = transactions.get(i);
            if (transaction instanceof Expense) {
                Expense expense = (Expense) transaction;
                builder.append("Expense  ")
                        .append(df.format(expense.getAmount()))
                        .append("  ")
                        .append(expense.getCategory())
                        .append("  ")
                        .append(expense.getSource())
                        .append("  ")
                        .append(expense.getDate())
                        .append("\n");
            } else {
                Income income = (Income) transaction;
                builder.append("Income   ")
                        .append(df.format(transaction.getAmount()))
                        .append("  ")
                        .append(income.getSource())
                        .append("  ")
                        .append(transaction.getDate())
                        .append("\n");
            }
        }
        recentActivityWidget.setText(builder.toString().trim());
    }

    private void updateAlertsWidget() {
        StringBuilder builder = new StringBuilder();
        int alertCount = 0;

        for (Category category : manager.getCategories().values()) {
            if (category.isOverLimit()) {
                builder.append("Over budget: ")
                        .append(category.getName())
                        .append(" by ")
                        .append(df.format(category.getSpent() - category.getLimit()))
                        .append("\n");
                alertCount++;
            }
        }

        Expense highestExpense = manager.getHighestExpense();
        if (highestExpense != null) {
            builder.append("Largest expense: ")
                    .append(df.format(highestExpense.getAmount()))
                    .append(" in ")
                    .append(highestExpense.getCategory())
                    .append("\n");
            alertCount++;
        }

        if (manager.getBalance() < 0) {
            builder.append("Balance is negative: ")
                    .append(df.format(manager.getBalance()))
                    .append("\n");
            alertCount++;
        }

        for (String source : manager.getIncomeSources()) {
            double sourceBalance = manager.getSourceBalance(source);
            if (sourceBalance < 0) {
                builder.append("Source overdrawn: ")
                        .append(source)
                        .append(" at ")
                        .append(df.format(sourceBalance))
                        .append("\n");
                alertCount++;
            }
        }

        if (alertCount == 0) {
            builder.append("No urgent alerts.\nYour finances look stable right now.");
        }

        alertsWidget.setText(builder.toString().trim());
    }

    private void updateBudgetHealthWidget() {
        double income = manager.getTotalIncome();
        double expense = manager.getTotalExpense();
        double balance = manager.getBalance();
        double savingsRate = income == 0 ? 0 : (balance / income) * 100;
        int categoryCount = manager.getCategories().size();

        String healthLabel;
        if (balance < 0) {
            healthLabel = "Needs attention";
        } else if (savingsRate >= 20) {
            healthLabel = "Strong";
        } else if (savingsRate >= 0) {
            healthLabel = "Steady";
        } else {
            healthLabel = "Under pressure";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Status: ").append(healthLabel).append("\n")
                .append("Savings rate: ").append(String.format("%.1f%%", savingsRate)).append("\n")
                .append("Tracked categories: ").append(categoryCount).append("\n")
                .append("Top category: ").append(manager.getHighestCategory()).append("\n")
                .append("Expense ratio: ")
                .append(income == 0 ? "N/A" : String.format("%.1f%%", (expense / income) * 100));

        Map<String, Double> incomeBySource = manager.getIncomeTotalsBySource();
        if (!incomeBySource.isEmpty()) {
            String strongestSource = "General";
            double strongestAmount = 0;
            for (Map.Entry<String, Double> entry : incomeBySource.entrySet()) {
                if (entry.getValue() > strongestAmount) {
                    strongestSource = entry.getKey();
                    strongestAmount = entry.getValue();
                }
            }
            builder.append("\nPrimary income stream: ")
                    .append(strongestSource)
                    .append(" (")
                    .append(df.format(strongestAmount))
                    .append(")");
        }

        budgetHealthWidget.setText(builder.toString());
    }

    private void editSelectedTransaction(JFrame frame, JTable table) {
        Integer index = getSelectedTransactionIndex(frame, table);
        if (index == null) {
            return;
        }

        Transaction transaction = manager.getTransactions().get(index);
        if (transaction instanceof Income) {
            showIncomeDialog(frame, index, (Income) transaction, null, null, null, "Income updated");
        } else if (transaction instanceof Expense) {
            showExpenseDialog(frame, index, (Expense) transaction, null, null, null, null, "Edited transaction");
        }
    }

    private Integer getSelectedTransactionIndex(JFrame frame, JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            showMessageDialog(frame, "Select a row first.", "No Selection");
            return null;
        }
        return visibleTransactionIndexes.get(row);
    }

    private boolean matchesType(Transaction transaction, String selectedType) {
        if (selectedType == null || "All".equalsIgnoreCase(selectedType)) {
            return true;
        }
        return transaction.getType().equalsIgnoreCase(selectedType.trim());
    }

    private boolean matchesCategory(Transaction transaction, String categoryText) {
        if (categoryText.isEmpty()) {
            return true;
        }
        return transaction instanceof Expense
                && ((Expense) transaction).getCategory().toLowerCase().contains(categoryText);
    }

    private YearMonth parseMonthFilter() {
        String text = monthFilter.getText().trim();
        if (text.isEmpty()) {
            return null;
        }

        try {
            return YearMonth.parse(text);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private boolean hasInvalidMonthFilter() {
        return !monthFilter.getText().trim().isEmpty() && parseMonthFilter() == null;
    }

    private void updateFilterStatus(boolean hasInvalidMonth) {
        if (hasInvalidMonth) {
            filterStatusLabel.setForeground(WARNING);
            filterStatusLabel.setText("Invalid month format");
            return;
        }

        int visibleCount = visibleTransactionIndexes.size();
        int totalCount = manager.getTransactions().size();
        boolean hasFilters = !"All".equals(typeFilter.getSelectedItem())
                || !categoryFilter.getText().trim().isEmpty()
                || !monthFilter.getText().trim().isEmpty();

        filterStatusLabel.setForeground(TEXT_MUTED);
        if (!hasFilters) {
            filterStatusLabel.setText("Showing all transactions (" + totalCount + ")");
        } else {
            filterStatusLabel.setText("Showing " + visibleCount + " of " + totalCount + " transactions");
        }
    }

    private void styleFilterValidation(boolean isValid) {
        monthFilter.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(isValid ? BORDER : WARNING, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
    }

    private void addLiveRefresh(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshAll();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshAll();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshAll();
            }
        });
    }

    private BasicScrollBarUI createScrollBarUI() {
        return new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(125, 211, 252, 190);
                this.trackColor = PANEL_ALT;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(9, 15, 26));
                g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, 10, 10);
                g2.dispose();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(125, 211, 252, 190));
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                        Math.max(thumbBounds.width - 4, 6),
                        Math.max(thumbBounds.height - 4, 6), 10, 10);
                g2.dispose();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        };
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(TEXT_PRIMARY);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color.brighter(), 1, true),
                new EmptyBorder(11, 18, 11, 18)));
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    private void showIncomeDialog(JFrame frame, Integer index, Income existingIncome, Double suggestedAmount,
            String suggestedSource, LocalDate suggestedDate, String logPrefix) {
        JTextField amountField = new JTextField(existingIncome != null
                ? String.valueOf(existingIncome.getAmount())
                : suggestedAmount == null ? "" : String.valueOf(suggestedAmount));
        JTextField sourceField = new JTextField(existingIncome != null
                ? existingIncome.getSource()
                : suggestedSource == null ? "" : suggestedSource);
        JTextField dateField = new JTextField((existingIncome != null
                ? existingIncome.getDate()
                : suggestedDate == null ? LocalDate.now() : suggestedDate).toString());
        styleInput(amountField);
        styleInput(sourceField);
        styleInput(dateField);

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setBackground(PANEL_BG);
        panel.add(createFieldLabel("Amount"));
        panel.add(amountField);
        panel.add(createFieldLabel("Income Stream"));
        panel.add(sourceField);
        panel.add(createFieldLabel("Date (YYYY-MM-DD)"));
        panel.add(dateField);

        boolean confirmed = showFormDialog(frame, index == null ? "Add Income Stream" : "Edit Income Stream", panel);
        if (!confirmed) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                showMessageDialog(frame, "Amount must be greater than zero.", "Invalid Amount");
                return;
            }

            String source = sourceField.getText().trim();
            if (source.isEmpty()) {
                showMessageDialog(frame, "Income stream name cannot be empty.", "Missing Stream");
                return;
            }

            LocalDate date = LocalDate.parse(dateField.getText().trim());
            if (index == null) {
                manager.addIncome(amount, source, date);
                log(logPrefix + ": " + df.format(amount) + " | " + source);
            } else {
                manager.updateTransaction(index, "Income", amount, "", source, date);
                log(logPrefix + ": " + source);
            }
            refreshAll();
        } catch (NumberFormatException ex) {
            showMessageDialog(frame, "Invalid amount.", "Input Error");
        } catch (DateTimeParseException ex) {
            showMessageDialog(frame, "Invalid date. Use YYYY-MM-DD.", "Input Error");
        }
    }

    private void showExpenseDialog(JFrame frame, Integer index, Expense existingExpense, Double suggestedAmount,
            String suggestedCategory, LocalDate suggestedDate, String suggestedSource, String logPrefix) {
        JTextField amountField = new JTextField(existingExpense != null
                ? String.valueOf(existingExpense.getAmount())
                : suggestedAmount == null ? "" : String.valueOf(suggestedAmount));
        JTextField categoryField = new JTextField(existingExpense != null
                ? existingExpense.getCategory()
                : suggestedCategory == null ? "" : suggestedCategory);
        JTextField dateField = new JTextField((existingExpense != null
                ? existingExpense.getDate()
                : suggestedDate == null ? LocalDate.now() : suggestedDate).toString());

        List<String> sources = new ArrayList<>(manager.getIncomeSources());
        if (!sources.contains("Unassigned")) {
            sources.add("Unassigned");
        }
        String selectedSource = existingExpense == null
                ? suggestedSource == null ? sources.get(0) : suggestedSource
                : existingExpense.getSource();
        if (!sources.contains(selectedSource)) {
            sources.add(0, selectedSource);
        }

        JComboBox<String> sourceBox = new JComboBox<>(sources.toArray(new String[0]));
        sourceBox.setSelectedItem(selectedSource);
        styleInput(amountField);
        styleInput(categoryField);
        styleInput(dateField);
        styleComboBox(sourceBox);

        JLabel sourceHint = new JLabel();
        sourceHint.setForeground(TEXT_MUTED);
        sourceHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Runnable refreshSourceHint = () -> {
            String source = String.valueOf(sourceBox.getSelectedItem());
            sourceHint.setText("Available in " + source + ": " + df.format(manager.getSourceBalance(source)));
        };
        sourceBox.addActionListener(e -> refreshSourceHint.run());
        refreshSourceHint.run();

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setBackground(PANEL_BG);
        panel.add(createFieldLabel("Amount"));
        panel.add(amountField);
        panel.add(createFieldLabel("Category"));
        panel.add(categoryField);
        panel.add(createFieldLabel("Deduct From"));
        panel.add(sourceBox);
        panel.add(sourceHint);
        panel.add(createFieldLabel("Date (YYYY-MM-DD)"));
        panel.add(dateField);

        boolean confirmed = showFormDialog(frame, index == null ? "Add Expense" : "Edit Expense", panel);
        if (!confirmed) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                showMessageDialog(frame, "Amount must be greater than zero.", "Invalid Amount");
                return;
            }

            String category = categoryField.getText().trim();
            if (category.isEmpty()) {
                showMessageDialog(frame, "Expense category cannot be empty.", "Missing Category");
                return;
            }

            String source = String.valueOf(sourceBox.getSelectedItem());
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            boolean warning;
            if (index == null) {
                warning = manager.addExpense(amount, category, source, date);
                log(logPrefix + ": " + df.format(amount) + " | " + category + " | " + source);
            } else {
                manager.updateTransaction(index, "Expense", amount, category, source, date);
                warning = false;
                log("Expense updated: " + category + " | " + source);
            }

            refreshAll();

            double sourceBalance = manager.getSourceBalance(source);
            if (warning || sourceBalance < 0) {
                String message = warning ? "Category limit exceeded." : "";
                if (sourceBalance < 0) {
                    if (!message.isEmpty()) {
                        message += "\n";
                    }
                    message += "Selected source is now negative: " + df.format(sourceBalance);
                }
                showMessageDialog(frame, message, "Budget Warning");
            }
        } catch (NumberFormatException ex) {
            showMessageDialog(frame, "Invalid amount.", "Input Error");
        } catch (DateTimeParseException ex) {
            showMessageDialog(frame, "Invalid date. Use YYYY-MM-DD.", "Input Error");
        }
    }

    private void importReceipt(JFrame frame) {
        String receiptText = showReceiptTextDialog(frame);
        if (receiptText == null || receiptText.trim().isEmpty()) {
            return;
        }

        ReceiptParseResult parsed = ReceiptParser.parse(receiptText);
        String logPrefix = "Receipt import";
        if (!"Unknown merchant".equals(parsed.getMerchant())) {
            logPrefix += " from " + parsed.getMerchant();
        }
        if ("Income".equalsIgnoreCase(parsed.getTransactionType())) {
            showIncomeDialog(frame, null, null,
                    parsed.getAmount() > 0 ? parsed.getAmount() : null,
                    parsed.getSource(),
                    parsed.getDate(),
                    logPrefix);
        } else {
            showExpenseDialog(frame, null, null,
                    parsed.getAmount() > 0 ? parsed.getAmount() : null,
                    parsed.getCategory(),
                    parsed.getDate(),
                    parsed.getSource(),
                    logPrefix);
        }
    }

    private String showReceiptTextDialog(JFrame owner) {
        JDialog dialog = createBaseDialog(owner, "Import Receipt", true);
        final String[] result = new String[1];

        JLabel titleLabel = new JLabel("Import Receipt");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JTextArea receiptArea = new JTextArea(14, 42);
        receiptArea.setLineWrap(true);
        receiptArea.setWrapStyleWord(true);
        receiptArea.setBackground(PANEL_ALT);
        receiptArea.setForeground(TEXT_PRIMARY);
        receiptArea.setCaretColor(TEXT_PRIMARY);
        receiptArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        styleScrollPane(scrollPane);

        JLabel helper = new JLabel("Paste receipt text or load a text/PDF file. Scanned image PDFs still need OCR.");
        helper.setForeground(TEXT_MUTED);
        helper.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton loadFile = styledButton("Load File", WARNING);
        loadFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Receipt File");
            int resultCode = chooser.showOpenDialog(dialog);
            if (resultCode != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File selectedFile = chooser.getSelectedFile();
            String fileName = selectedFile.getName().toLowerCase();
            if (fileName.endsWith(".txt") || fileName.endsWith(".log") || fileName.endsWith(".csv")) {
                try {
                    receiptArea.setText(Files.readString(selectedFile.toPath()));
                } catch (IOException ex) {
                    showMessageDialog(owner, "Failed to read file:\n" + ex.getMessage(), "File Error");
                }
            } else if (fileName.endsWith(".pdf")) {
                try {
                    String extractedText = PdfTextExtractor.extractText(selectedFile.toPath());
                    if (extractedText == null || extractedText.trim().isEmpty()) {
                        showMessageDialog(owner,
                                "This PDF does not appear to contain selectable text.\nIf it is a scanned receipt image, OCR would be needed to read it.",
                                "No Readable PDF Text");
                    } else {
                        receiptArea.setText(extractedText);
                    }
                } catch (IOException ex) {
                    showMessageDialog(owner, "Failed to read PDF:\n" + ex.getMessage(), "PDF Read Error");
                }
            } else {
                showMessageDialog(owner,
                        "Supported file types are text files and text-based PDFs.\nScanned image receipts still need OCR support.",
                        "Unsupported File");
            }
        });

        JButton cancel = styledButton("Cancel", SECONDARY);
        JButton parse = styledButton("Use Receipt", INFO);
        cancel.addActionListener(e -> dialog.dispose());
        parse.addActionListener(e -> {
            result[0] = receiptArea.getText();
            dialog.dispose();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(loadFile);
        actions.add(cancel);
        actions.add(parse);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(PANEL_BG);
        body.setBorder(new EmptyBorder(18, 18, 18, 18));
        body.add(titleLabel, BorderLayout.NORTH);
        body.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        footer.add(helper, BorderLayout.NORTH);
        footer.add(actions, BorderLayout.SOUTH);
        body.add(footer, BorderLayout.SOUTH);

        dialog.add(body);
        showPreparedDialog(dialog, owner, 700, 500);
        dialog.setVisible(true);
        return result[0];
    }

    private void exportReportAsPdf(JFrame frame) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Analytics Report");
        chooser.setSelectedFile(new File("finance-report.pdf"));

        int result = chooser.showSaveDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        String path = selectedFile.getAbsolutePath().toLowerCase().endsWith(".pdf")
                ? selectedFile.getAbsolutePath()
                : selectedFile.getAbsolutePath() + ".pdf";

        try {
            PdfExporter.exportReport(ReportGenerator.generate(manager), new File(path).toPath());
            log("PDF exported: " + path);
            showMessageDialog(frame, "Report exported to:\n" + path, "Export Complete");
        } catch (IOException ex) {
            showMessageDialog(frame, "Failed to export PDF:\n" + ex.getMessage(), "Export Failed");
        }
    }

    private Double getValidAmount(String message) {
        while (true) {
            String input = showTextInputDialog(mainFrame, "Amount Entry", message, "");
            if (input == null) {
                return null;
            }

            try {
                double amount = Double.parseDouble(input.trim());
                if (amount <= 0) {
                    showMessageDialog(mainFrame, "Amount must be greater than zero.", "Invalid Amount");
                    continue;
                }
                return amount;
            } catch (Exception ex) {
                showMessageDialog(mainFrame, "Invalid number.", "Input Error");
            }
        }
    }

    private LocalDate getDateInput(String message, LocalDate fallback) {
        while (true) {
            String input = showTextInputDialog(mainFrame, "Date Entry", message, fallback.toString());
            if (input == null) {
                return null;
            }

            try {
                return LocalDate.parse(input.trim());
            } catch (DateTimeParseException ex) {
                showMessageDialog(mainFrame, "Invalid date. Use YYYY-MM-DD.", "Input Error");
            }
        }
    }

    private void showMessageDialog(JFrame owner, String message, String title) {
        JDialog dialog = createBaseDialog(owner, title, true);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel eyebrow = new JLabel("NOTICE");
        eyebrow.setForeground(new Color(125, 211, 252));
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));

        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        messageArea.setForeground(TEXT_MUTED);
        messageArea.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JButton close = styledButton("Close", INFO);
        close.addActionListener(e -> dialog.dispose());

        GradientPanel body = new GradientPanel(CARD_TOP, CARD_BOTTOM, 26);
        body.setLayout(new BorderLayout(0, 14));
        body.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel header = new JPanel(new GridLayout(0, 1, 0, 6));
        header.setOpaque(false);
        header.add(eyebrow);
        header.add(titleLabel);
        body.add(header, BorderLayout.NORTH);
        body.add(messageArea, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(close);
        body.add(footer, BorderLayout.SOUTH);

        dialog.add(body);
        showPreparedDialog(dialog, owner, 460, 240);
        dialog.setVisible(true);
    }

    private String showTextInputDialog(JFrame owner, String title, String label, String initialValue) {
        JDialog dialog = createBaseDialog(owner, title, true);
        final String[] result = new String[1];

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel eyebrow = new JLabel("QUICK ENTRY");
        eyebrow.setForeground(new Color(125, 211, 252));
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));

        JLabel fieldLabel = createFieldLabel(label);
        JTextField field = new JTextField(initialValue);
        styleInput(field);

        JButton cancel = styledButton("Cancel", SECONDARY);
        JButton save = styledButton("Save", INFO);

        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            result[0] = field.getText();
            dialog.dispose();
        });

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setOpaque(false);
        form.add(fieldLabel);
        form.add(field);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(cancel);
        actions.add(save);

        GradientPanel body = new GradientPanel(CARD_TOP, CARD_BOTTOM, 26);
        body.setLayout(new BorderLayout(0, 16));
        body.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel header = new JPanel(new GridLayout(0, 1, 0, 6));
        header.setOpaque(false);
        header.add(eyebrow);
        header.add(titleLabel);
        body.add(header, BorderLayout.NORTH);
        body.add(form, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);

        dialog.add(body);
        showPreparedDialog(dialog, owner, 560, 230);
        SwingUtilities.invokeLater(field::requestFocusInWindow);
        dialog.setVisible(true);
        return result[0];
    }

    private boolean showFormDialog(JFrame owner, String title, JPanel contentPanel) {
        JDialog dialog = createBaseDialog(owner, title, true);
        final boolean[] confirmed = { false };

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel eyebrow = new JLabel("FORM REVIEW");
        eyebrow.setForeground(new Color(125, 211, 252));
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));

        JButton cancel = styledButton("Cancel", SECONDARY);
        JButton save = styledButton("Save", INFO);

        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(cancel);
        actions.add(save);

        GradientPanel body = new GradientPanel(CARD_TOP, CARD_BOTTOM, 26);
        body.setLayout(new BorderLayout(0, 16));
        body.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel header = new JPanel(new GridLayout(0, 1, 0, 6));
        header.setOpaque(false);
        header.add(eyebrow);
        header.add(titleLabel);
        body.add(header, BorderLayout.NORTH);
        body.add(contentPanel, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);

        contentPanel.setPreferredSize(new Dimension(
                Math.max(contentPanel.getPreferredSize().width, 500),
                contentPanel.getPreferredSize().height));

        dialog.add(body);
        showPreparedDialog(dialog, owner, 560, 320);
        dialog.setVisible(true);
        return confirmed[0];
    }

    private JDialog createBaseDialog(JFrame owner, String title, boolean modal) {
        JDialog dialog = new JDialog(owner, title, modal);
        dialog.getContentPane().setBackground(APP_BG);
        dialog.setLayout(new BorderLayout());
        dialog.setUndecorated(false);
        dialog.setResizable(true);
        return dialog;
    }

    private void showPreparedDialog(JDialog dialog, JFrame owner, int minWidth, int minHeight) {
        dialog.pack();
        int width = Math.max(dialog.getWidth(), minWidth);
        int height = Math.max(dialog.getHeight(), minHeight);
        dialog.setSize(width, height);
        dialog.setMinimumSize(new Dimension(minWidth, minHeight));
        dialog.setLocationRelativeTo(owner);
    }

    private void log(String msg) {
        output.append(msg + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    private JPanel createBackgroundCanvas() {
        GradientPanel panel = new GradientPanel(new Color(7, 12, 23), new Color(4, 9, 18), 0);
        panel.setLayout(new BorderLayout(16, 16));
        return panel;
    }

    private JLabel createTag(String text, Color color) {
        JLabel tag = new JLabel(text);
        tag.setOpaque(true);
        tag.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 36));
        tag.setForeground(TEXT_PRIMARY);
        tag.setBorder(new EmptyBorder(8, 12, 8, 12));
        tag.setFont(new Font("SansSerif", Font.BOLD, 12));
        return tag;
    }

    private JPanel createMiniStat(String label, String value) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 3));
        panel.setOpaque(false);

        JLabel labelView = new JLabel(label);
        labelView.setForeground(new Color(191, 219, 254));
        labelView.setFont(new Font("SansSerif", Font.BOLD, 11));

        JLabel valueView = new JLabel(value);
        valueView.setForeground(TEXT_PRIMARY);
        valueView.setFont(new Font("SansSerif", Font.BOLD, 14));

        panel.add(labelView);
        panel.add(valueView);
        return panel;
    }

    private static class GradientPanel extends JPanel {
        private final Color topColor;
        private final Color bottomColor;
        private final int arc;

        private GradientPanel(Color topColor, Color bottomColor, int arc) {
            this.topColor = topColor;
            this.bottomColor = bottomColor;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint paint = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
            g2.setPaint(paint);
            if (arc > 0) {
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(1, 1, getWidth() - 2, Math.max(getHeight() / 3, 24), arc, arc);
            } else {
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.setColor(new Color(255, 255, 255, 8));
            g2.fillOval(getWidth() - 120, -20, 140, 140);
            g2.setColor(new Color(59, 130, 246, 18));
            g2.fillOval(-40, getHeight() - 100, 160, 160);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
