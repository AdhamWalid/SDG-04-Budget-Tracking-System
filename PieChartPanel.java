import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class PieChartPanel extends JPanel {

    private Map<String, Double> data;

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data == null || data.isEmpty())
            return;

        double total = 0;
        for (double value : data.values()) {
            total += value;
        }

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

            g.setColor(colors[i % colors.length]);
            g.fillArc(50, 30, 250, 250, startAngle, angle);

            startAngle += angle;
            i++;
        }

        // Legend
        int y = 300;
        i = 0;
        for (String key : data.keySet()) {
            g.setColor(colors[i % colors.length]);
            g.fillRect(50, y, 15, 15);
            g.setColor(Color.BLACK);
            g.drawString(key + " - RM " + data.get(key), 75, y + 12);
            y += 25;
            i++;
        }
    }
}