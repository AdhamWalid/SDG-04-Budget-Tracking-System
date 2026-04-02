import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AnalyticsDialog extends JDialog {

    private static final Color APP_BG = new Color(17, 24, 39);
    private static final Color PANEL_BG = new Color(31, 41, 55);
    private static final Color PANEL_ALT = new Color(15, 23, 42);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color BORDER = new Color(51, 65, 85);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color INFO = new Color(59, 130, 246);
    private static final Color WARNING = new Color(245, 158, 11);

    private final DecimalFormat df = new DecimalFormat("RM 0.00");

    public AnalyticsDialog(JFrame owner, BudgetManager manager) {
        super(owner, "Analytics Dashboard", true);
        getContentPane().setBackground(APP_BG);
        setLayout(new BorderLayout());

        YearMonth latestMonth = manager.getLatestTransactionMonth();
        Expense highestExpense = manager.getHighestExpense();

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBackground(APP_BG);
        content.setBorder(new EmptyBorder(18, 18, 18, 18));

        content.add(createHeroPanel(latestMonth), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(16, 16));
        main.setOpaque(false);
        main.add(createOverviewPanel(manager, latestMonth, highestExpense), BorderLayout.NORTH);
        main.add(createCenterPanel(manager, latestMonth), BorderLayout.CENTER);

        content.add(main, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(content);
        styleScrollPane(scrollPane, APP_BG);

        add(scrollPane, BorderLayout.CENTER);
        setSize(1100, 760);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(owner);
    }

    private JPanel createHeroPanel(YearMonth latestMonth) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_ALT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel title = new JLabel("Analytics Dashboard");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel subtitle = new JLabel("Review performance, spending pressure, and " + latestMonth + " activity at a glance.");
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        JLabel badge = new JLabel("Insights", SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(new Color(30, 41, 59));
        badge.setForeground(new Color(125, 211, 252));
        badge.setFont(new Font("SansSerif", Font.BOLD, 13));
        badge.setBorder(new EmptyBorder(10, 18, 10, 18));

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(badge, BorderLayout.EAST);
        return panel;
    }

    private JPanel createOverviewPanel(BudgetManager manager, YearMonth latestMonth, Expense highestExpense) {
        JPanel cards = new JPanel(new GridLayout(2, 3, 14, 14));
        cards.setOpaque(false);

        cards.add(createMetricCard("Total Income", df.format(manager.getTotalIncome()), SUCCESS));
        cards.add(createMetricCard("Total Expense", df.format(manager.getTotalExpense()), DANGER));
        cards.add(createMetricCard("Current Balance", df.format(manager.getBalance()), INFO));
        cards.add(createMetricCard("Latest Month Savings", df.format(manager.getBalance(latestMonth)), WARNING));
        cards.add(createMetricCard("Average Expense", df.format(manager.getAverageExpense()), new Color(16, 185, 129)));
        cards.add(createMetricCard(
                "Highest Expense",
                highestExpense == null ? "None" : df.format(highestExpense.getAmount()) + " in " + highestExpense.getCategory(),
                new Color(168, 85, 247)));

        return cards;
    }

    private JPanel createMetricCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(0, 6));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        card.add(accentBar, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCenterPanel(BudgetManager manager, YearMonth latestMonth) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 16));
        panel.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(0, 16));
        left.setOpaque(false);
        left.add(createSectionPanel("Category Budget Status", createCategoryPanel(manager)), BorderLayout.CENTER);
        left.add(createSectionPanel("Latest Month Snapshot", createMonthSnapshot(manager, latestMonth)), BorderLayout.SOUTH);

        JPanel reportArea = createReportArea(manager);
        JPanel right = createSectionPanel("Detailed Report", reportArea);

        panel.add(left);
        panel.add(right);
        return panel;
    }

    private JScrollPane createCategoryPanel(BudgetManager manager) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        List<Category> categories = new ArrayList<>(manager.getCategories().values());
        categories.sort(Comparator.comparingDouble(Category::getSpent).reversed());

        if (categories.isEmpty()) {
            JLabel empty = new JLabel("No categories yet. Add expenses to see budget progress.");
            empty.setForeground(TEXT_MUTED);
            empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
            list.add(empty);
            return wrapScroll(list, PANEL_BG);
        }

        for (Category category : categories) {
            list.add(createCategoryRow(category));
            list.add(Box.createVerticalStrut(10));
        }

        return wrapScroll(list, PANEL_BG);
    }

    private JPanel createCategoryRow(Category category) {
        JPanel row = new JPanel(new BorderLayout(0, 8));
        row.setBackground(PANEL_ALT);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel name = new JLabel(category.getName());
        name.setForeground(TEXT_PRIMARY);
        name.setFont(new Font("SansSerif", Font.BOLD, 14));

        double limit = category.getLimit();
        double percent = limit <= 0 ? 0 : (category.getSpent() / limit) * 100;
        JLabel amount = new JLabel(df.format(category.getSpent()) + " / " + df.format(limit));
        amount.setForeground(category.isOverLimit() ? DANGER : TEXT_MUTED);
        amount.setFont(new Font("SansSerif", Font.PLAIN, 12));

        top.add(name, BorderLayout.WEST);
        top.add(amount, BorderLayout.EAST);

        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue((int) Math.min(Math.round(percent), 100));
        progress.setStringPainted(true);
        progress.setString(String.format("%.0f%% used", percent));
        progress.setBackground(new Color(30, 41, 59));
        progress.setForeground(category.isOverLimit() ? DANGER : INFO);
        progress.setBorderPainted(false);
        progress.setFont(new Font("SansSerif", Font.BOLD, 11));

        JLabel status = new JLabel(category.isOverLimit() ? "Over limit" : "Within budget");
        status.setForeground(category.isOverLimit() ? DANGER : SUCCESS);
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));

        row.add(top, BorderLayout.NORTH);
        row.add(progress, BorderLayout.CENTER);
        row.add(status, BorderLayout.SOUTH);
        return row;
    }

    private JPanel createMonthSnapshot(BudgetManager manager, YearMonth latestMonth) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 10));
        panel.setOpaque(false);

        panel.add(createInfoRow("Month", latestMonth.toString()));
        panel.add(createInfoRow("Income", df.format(manager.getTotalIncome(latestMonth))));
        panel.add(createInfoRow("Expense", df.format(manager.getTotalExpense(latestMonth))));
        panel.add(createInfoRow("Savings", df.format(manager.getBalance(latestMonth))));

        Map<String, Double> monthlyBreakdown = manager.getExpenseByCategory(latestMonth);
        String topCategory = "None";
        double topAmount = 0;
        for (Map.Entry<String, Double> entry : monthlyBreakdown.entrySet()) {
            if (entry.getValue() > topAmount) {
                topCategory = entry.getKey();
                topAmount = entry.getValue();
            }
        }
        panel.add(createInfoRow("Top Monthly Category", topCategory));

        return panel;
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel left = new JLabel(label);
        left.setForeground(TEXT_MUTED);
        left.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel right = new JLabel(value);
        right.setForeground(TEXT_PRIMARY);
        right.setFont(new Font("SansSerif", Font.BOLD, 13));

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel createSectionPanel(String title, Component content) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportArea(BudgetManager manager) {
        JTextArea textArea = new JTextArea(ReportGenerator.generate(manager));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(PANEL_ALT);
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setCaretColor(TEXT_PRIMARY);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        styleScrollPane(scrollPane, PANEL_ALT);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JScrollPane wrapScroll(Component component, Color viewportColor) {
        JScrollPane scrollPane = new JScrollPane(component);
        styleScrollPane(scrollPane, viewportColor);
        scrollPane.setPreferredSize(new Dimension(0, 320));
        return scrollPane;
    }

    private void styleScrollPane(JScrollPane scrollPane, Color viewportColor) {
        scrollPane.setBorder(new LineBorder(BORDER, 1, true));
        scrollPane.getViewport().setBackground(viewportColor);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, Integer.MAX_VALUE));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 10));
        scrollPane.getVerticalScrollBar().setUI(createScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(createScrollBarUI());
    }

    private BasicScrollBarUI createScrollBarUI() {
        return new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(71, 85, 105);
                trackColor = PANEL_ALT;
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
                g2.setColor(PANEL_ALT);
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
                g2.setColor(new Color(100, 116, 139));
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
}
