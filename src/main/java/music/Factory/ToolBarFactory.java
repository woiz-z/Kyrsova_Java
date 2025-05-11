package music.Factory;

import javax.swing.*;
import java.awt.*;

public class ToolBarFactory {
    private static final Color PANEL_COLOR = new Color(255, 255, 255);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public static JToolBar createToolBar(Runnable loadAction, Runnable saveAction,
                                         Runnable addAction, Runnable renameAction,
                                         Runnable deleteAction) {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        toolBar.setBackground(PANEL_COLOR);

        addToolbarButton(toolBar, "Завантажити з файлу", "📂", loadAction);
        toolBar.addSeparator();
        addToolbarButton(toolBar, "Зберегти у файл", "💾", saveAction);
        toolBar.addSeparator();
        addToolbarButton(toolBar, "Додати збірку", "➕", addAction);
        toolBar.addSeparator();
        addToolbarButton(toolBar, "Змінити назву", "✏️", renameAction);
        toolBar.addSeparator();
        addToolbarButton(toolBar, "Видалити збірку", "🗑️", deleteAction);

        return toolBar;
    }

    private static void addToolbarButton(JToolBar toolBar, String text, String icon, Runnable action) {
        JButton button = new JButton(String.format("%s %s", icon, text)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(ACCENT_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(ACCENT_COLOR.brighter());
                } else {
                    g2.setColor(ACCENT_COLOR);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_COLOR.darker());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            }
        };
        button.setContentAreaFilled(false);
        button.setFont(MAIN_FONT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> action.run());
        toolBar.add(button);
    }
}