import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class TrendChartPanel extends JPanel {

    private static final Color PANEL_BG = new Color(16, 24, 39);
    private static final Color TEXT_PRIMARY = new Color(245, 247, 255);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color GRID = new Color(56, 70, 94, 110);
    private static final Color INCOME = new Color(52, 211, 153);
    private static final Color EXPENSE = new Color(248, 113, 113);

    private final DecimalFormat df = new DecimalFormat("RM 0");
    private Map<YearMonth, Double> incomeData = new LinkedHashMap<>();
    private Map<YearMonth, Double> expenseData = new LinkedHashMap<>();

    public TrendChartPanel() {
        setOpaque(true);
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setPreferredSize(new Dimension(0, 280));
    }

    public void setData(Map<YearMonth, Double> incomeData, Map<YearMonth, Double> expenseData) {
        this.incomeData = incomeData == null ? new LinkedHashMap<>() : new LinkedHashMap<>(incomeData);
        this.expenseData = expenseData == null ? new LinkedHashMap<>() : new LinkedHashMap<>(expenseData);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (incomeData.isEmpty() && expenseData.isEmpty()) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.drawString("Add more monthly activity to see trend insights.", 20, 30);
            g2.dispose();
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int left = 52;
        int top = 28;
        int right = width - 24;
        int bottom = height - 54;
        int chartWidth = Math.max(80, right - left);
        int chartHeight = Math.max(80, bottom - top);

        double maxValue = 0;
        for (double value : incomeData.values()) {
            maxValue = Math.max(maxValue, value);
        }
        for (double value : expenseData.values()) {
            maxValue = Math.max(maxValue, value);
        }
        maxValue = maxValue <= 0 ? 1 : maxValue;

        drawGrid(g2, left, top, chartWidth, chartHeight, maxValue);
        drawSeries(g2, incomeData, left, top, chartWidth, chartHeight, maxValue, INCOME);
        drawSeries(g2, expenseData, left, top, chartWidth, chartHeight, maxValue, EXPENSE);
        drawLabels(g2, left, bottom, chartWidth);
        drawLegend(g2, width);

        g2.dispose();
    }

    private void drawGrid(Graphics2D g2, int left, int top, int chartWidth, int chartHeight, double maxValue) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int i = 0; i <= 4; i++) {
            int y = top + (chartHeight * i / 4);
            g2.setColor(GRID);
            g2.drawLine(left, y, left + chartWidth, y);
            g2.setColor(TEXT_MUTED);
            double labelValue = maxValue * (4 - i) / 4.0;
            g2.drawString(df.format(labelValue), 8, y + 4);
        }
    }

    private void drawSeries(Graphics2D g2, Map<YearMonth, Double> series, int left, int top, int chartWidth,
            int chartHeight, double maxValue, Color color) {
        if (series.isEmpty()) {
            return;
        }

        int size = series.size();
        int[] xs = new int[size];
        int[] ys = new int[size];
        int index = 0;

        for (double value : series.values()) {
            double ratio = value / maxValue;
            xs[index] = left + (size == 1 ? chartWidth / 2 : (chartWidth * index / (size - 1)));
            ys[index] = top + chartHeight - (int) Math.round(chartHeight * ratio);
            index++;
        }

        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 55));
        for (int i = 0; i < xs.length - 1; i++) {
            Polygon area = new Polygon();
            area.addPoint(xs[i], ys[i]);
            area.addPoint(xs[i + 1], ys[i + 1]);
            area.addPoint(xs[i + 1], top + chartHeight);
            area.addPoint(xs[i], top + chartHeight);
            g2.fillPolygon(area);
        }

        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        for (int i = 0; i < xs.length - 1; i++) {
            g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }

        for (int i = 0; i < xs.length; i++) {
            g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
        }
    }

    private void drawLabels(Graphics2D g2, int left, int bottom, int chartWidth) {
        Map<YearMonth, Double> source = !incomeData.isEmpty() ? incomeData : expenseData;
        int size = source.size();
        int index = 0;
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(TEXT_MUTED);
        for (YearMonth month : source.keySet()) {
            String label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + month.getYear();
            int x = left + (size == 1 ? chartWidth / 2 : (chartWidth * index / (size - 1)));
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, x - (labelWidth / 2), bottom + 22);
            index++;
        }
    }

    private void drawLegend(Graphics2D g2, int width) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        drawLegendItem(g2, width - 170, 16, INCOME, "Income");
        drawLegendItem(g2, width - 88, 16, EXPENSE, "Expense");
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String label) {
        g2.setColor(color);
        g2.fillRoundRect(x, y, 14, 14, 6, 6);
        g2.setColor(TEXT_PRIMARY);
        g2.drawString(label, x + 20, y + 12);
    }
}
