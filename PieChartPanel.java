import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;

public class PieChartPanel extends JPanel {

    private static final Color PANEL_BG = new Color(31, 41, 55);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private Map<String, Double> data;
    private final DecimalFormat df = new DecimalFormat("RM 0.00");

    public PieChartPanel() {
        setOpaque(true);
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(4, 4, 4, 4));
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (data == null || data.isEmpty()) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.drawString("No expense data available for this view.", 28, 40);
            g2.dispose();
            return;
        }

        double total = 0;
        for (double value : data.values()) {
            total += value;
        }

        int width = getWidth();
        int height = getHeight();
        int legendStartY = Math.max(height - (data.size() * 24) - 16, height / 2 + 40);
        int availableChartHeight = Math.max(140, legendStartY - 36);
        int pieDiameter = Math.min(Math.min(width - 50, availableChartHeight), 240);
        pieDiameter = Math.max(pieDiameter, 120);
        int pieX = Math.max((width - pieDiameter) / 2, 20);
        int pieY = 18;
        int innerDiameter = Math.max((int) (pieDiameter * 0.42), 52);
        int innerX = pieX + (pieDiameter - innerDiameter) / 2;
        int innerY = pieY + (pieDiameter - innerDiameter) / 2;

        int startAngle = 0;
        int i = 0;

        Color[] colors = {
                new Color(52, 152, 219),
                new Color(231, 76, 60),
                new Color(46, 204, 113),
                new Color(155, 89, 182),
                new Color(241, 196, 15),
                new Color(230, 126, 34)
        };

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int angle = (int) Math.round((entry.getValue() / total) * 360);

            g2.setColor(colors[i % colors.length]);
            g2.fillArc(pieX, pieY, pieDiameter, pieDiameter, startAngle, angle);

            startAngle += angle;
            i++;
        }

        g2.setColor(PANEL_BG.darker());
        g2.fillOval(innerX, innerY, innerDiameter, innerDiameter);
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics titleMetrics = g2.getFontMetrics();
        String totalLabel = "Total";
        int totalLabelX = innerX + (innerDiameter - titleMetrics.stringWidth(totalLabel)) / 2;
        int totalLabelY = innerY + innerDiameter / 2 - 6;
        g2.drawString(totalLabel, totalLabelX, totalLabelY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics valueMetrics = g2.getFontMetrics();
        String totalValue = df.format(total);
        int totalValueX = innerX + (innerDiameter - valueMetrics.stringWidth(totalValue)) / 2;
        int totalValueY = totalLabelY + 18;
        g2.drawString(totalValue, totalValueX, totalValueY);

        int y = legendStartY;
        i = 0;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (String key : data.keySet()) {
            g2.setColor(colors[i % colors.length]);
            g2.fillRoundRect(24, y, 14, 14, 6, 6);
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(key + "  " + df.format(data.get(key)), 48, y + 12);
            y += 25;
            i++;
        }

        g2.dispose();
    }
}
