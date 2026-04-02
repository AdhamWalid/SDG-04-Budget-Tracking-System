import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SplashScreen extends JWindow {

    public SplashScreen() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, new Color(8, 15, 34), getWidth(), getHeight(),
                        new Color(20, 47, 89));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);

                g2.setColor(new Color(59, 130, 246, 80));
                g2.fillOval(getWidth() - 170, -35, 190, 190);
                g2.setColor(new Color(34, 197, 94, 70));
                g2.fillOval(-55, getHeight() - 140, 190, 190);
                g2.setColor(new Color(255, 255, 255, 28));
                g2.drawRoundRect(12, 12, getWidth() - 24, getHeight() - 24, 24, 24);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(28, 34, 28, 34));

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);

        JLabel icon = new JLabel("RM", SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setPreferredSize(new Dimension(78, 78));
        icon.setBackground(new Color(255, 255, 255, 30));
        icon.setForeground(new Color(191, 219, 254));
        icon.setFont(new Font("SansSerif", Font.BOLD, 26));
        icon.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 55), 1, true));

        JLabel title = new JLabel("Personal Finance Simulator");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(248, 250, 252));

        JLabel subtitle = new JLabel("Loading your budgets, analytics, and latest transactions.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(191, 219, 254));

        JLabel footer = new JLabel("Building your dashboard experience");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.setForeground(new Color(148, 163, 184));

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);
        textPanel.add(footer);

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.add(icon, BorderLayout.WEST);
        header.add(textPanel, BorderLayout.CENTER);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 12));
        bar.setBackground(new Color(30, 41, 59));
        bar.setForeground(new Color(96, 165, 250));

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        badges.setOpaque(false);
        badges.add(createBadge("Analytics"));
        badges.add(createBadge("Categories"));
        badges.add(createBadge("Reports"));

        content.add(header, BorderLayout.NORTH);
        content.add(badges, BorderLayout.CENTER);
        content.add(bar, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.CENTER);

        add(panel);
        setBackground(new Color(0, 0, 0, 0));
        setSize(620, 260);
        setLocationRelativeTo(null);
        setVisible(true);

        try {
            Thread.sleep(1800);
        } catch (InterruptedException ignored) {
        }

        setVisible(false);
        new MainGUI();
    }

    private JLabel createBadge(String text) {
        JLabel badge = new JLabel(text);
        badge.setOpaque(true);
        badge.setBackground(new Color(255, 255, 255, 24));
        badge.setForeground(new Color(224, 231, 255));
        badge.setBorder(new EmptyBorder(8, 12, 8, 12));
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        return badge;
    }
}
