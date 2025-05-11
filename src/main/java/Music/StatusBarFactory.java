package Music;

import javax.swing.*;
import java.awt.*;

public class StatusBarFactory {
    private static final Color PANEL_COLOR = new Color(255, 255, 255);
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public static JLabel createStatusBar() {
        JLabel statusBar = new JLabel(" CREATED BY IHOR");
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        statusBar.setFont(MAIN_FONT);
        statusBar.setForeground(new Color(100, 100, 100));
        statusBar.setBackground(PANEL_COLOR);
        statusBar.setOpaque(true);
        return statusBar;
    }
}