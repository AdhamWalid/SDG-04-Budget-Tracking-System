import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {

    public SplashScreen() {
        JLabel label = new JLabel("Personal Finance Simulator", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 26));
        label.setForeground(Color.WHITE);
        label.setBackground(new Color(30, 30, 30));
        label.setOpaque(true);

        add(label);
        setSize(500, 200);
        setLocationRelativeTo(null);
        setVisible(true);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }

        setVisible(false);
        new MainGUI();
    }
}