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

                GradientPaint gradient = new GradientPaint(0, 0, new Color(6, 14, 28), getWidth(), getHeight(),
                        new Color(20, 46, 84));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);

                g2.setColor(new Color(96, 165, 250, 90));
                g2.fillOval(getWidth() - 190, -40, 210, 210);
                g2.setColor(new Color(244, 114, 182, 55));
                g2.fillOval(getWidth() - 310, 90, 180, 180);
                g2.setColor(new Color(52, 211, 153, 70));
                g2.fillOval(-55, getHeight() - 140, 190, 190);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(28, 28, getWidth() - 56, getHeight() - 56, 26, 26);
                g2.setColor(new Color(255, 255, 255, 28));
                g2.drawRoundRect(12, 12, getWidth() - 24, getHeight() - 24, 24, 24);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 36, 30, 36));

        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setOpaque(false);

        JLabel icon = new JLabel("RM", SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setPreferredSize(new Dimension(88, 88));
        icon.setBackground(new Color(255, 255, 255, 26));
        icon.setForeground(new Color(224, 231, 255));
        icon.setFont(new Font("SansSerif", Font.BOLD, 28));
        icon.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 65), 1, true));

        JLabel eyebrow = new JLabel("PREPARING YOUR WORKSPACE");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 12));
        eyebrow.setForeground(new Color(125, 211, 252));

        JLabel title = new JLabel("Personal Finance Simulator");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(new Color(248, 250, 252));

        JLabel subtitle = new JLabel("Loading your budgets, analytics, receipts, and dashboard widgets.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(191, 219, 254));

        JLabel footer = new JLabel("Building a smoother finance experience");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.setForeground(new Color(148, 163, 184));

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        textPanel.setOpaque(false);
        textPanel.add(eyebrow);
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
        bar.setPreferredSize(new Dimension(0, 14));
        bar.setBackground(new Color(14, 24, 42));
        bar.setForeground(new Color(125, 211, 252));

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        badges.setOpaque(false);
        badges.add(createBadge("Analytics"));
        badges.add(createBadge("Receipt Import"));
        badges.add(createBadge("Source Tracking"));
        badges.add(createBadge("Reports"));

        JPanel lower = new JPanel(new BorderLayout(0, 14));
        lower.setOpaque(false);
        lower.add(badges, BorderLayout.NORTH);
        lower.add(bar, BorderLayout.SOUTH);

        content.add(header, BorderLayout.NORTH);
        content.add(lower, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.CENTER);

        add(panel);
        setBackground(new Color(0, 0, 0, 0));
        setSize(700, 300);
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
        badge.setBackground(new Color(255, 255, 255, 20));
        badge.setForeground(new Color(224, 231, 255));
        badge.setBorder(new EmptyBorder(9, 14, 9, 14));
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        return badge;
    }
}
