package music.Dialog;

import music.Music.MusicTrack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TrackDetailsDialog extends JDialog {
    public TrackDetailsDialog(JFrame parent, MusicTrack track) {
        super(parent, "Деталі треку: " + track.getTitle(), true);
        initializeUI(track);
    }

    private void initializeUI(MusicTrack track) {
        setSize(500, 500);  // Збільшив розмір для кращого відображення
        setLocationRelativeTo(getParent());
        setResizable(false);

        // Головна панель з градієнтним фоном
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(245, 248, 250);
                Color color2 = new Color(220, 230, 240);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(mainPanel);

        // Заголовок з тінню та градієнтом
        JLabel titleLabel = new JLabel("Деталі треку", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Тінь
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawString(getText(), 153, 23);

                // Основний текст з градієнтом
                GradientPaint gradient = new GradientPaint(0, 0, new Color(70, 130, 180), 0, 20, new Color(50, 100, 150));
                g2.setPaint(gradient);
                g2.drawString(getText(), 150, 22);
            }
        };
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Панель з інформацією
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(

                ));

        // Додаємо інформацію про трек
        infoPanel.add(createDetailRow("Назва:", track.getTitle()));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(createDetailRow("Виконавець:", track.getArtist()));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(createDetailRow("Жанр:", track.getGenre().toString()));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        long totalSeconds = track.getDuration().getSeconds();
        String durationText = String.format("%d хв %d сек", totalSeconds / 60, totalSeconds % 60);
        infoPanel.add(createDetailRow("Тривалість:", durationText));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(createDetailRow("ID треку:", track.getId() != null ? track.getId().toString() : "N/A"));

        mainPanel.add(infoPanel, BorderLayout.CENTER);

        // Стилізована кнопка закриття
        JButton closeButton = new JButton("Закрити") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(70, 130, 180).darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(70, 130, 180).brighter());
                } else {
                    g2.setColor(new Color(70, 130, 180));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            }
        };
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setForeground(Color.WHITE);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel rowPanel = new JPanel(new BorderLayout(15, 0));
        rowPanel.setOpaque(false);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComponent.setForeground(new Color(80, 80, 80));
        labelComponent.setPreferredSize(new Dimension(120, 20)); // Фіксована ширина для вирівнювання

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueComponent.setForeground(new Color(40, 40, 40));

        // Додаємо іконку перед значенням
        JPanel valuePanel = new JPanel(new BorderLayout(10, 0));
        valuePanel.setOpaque(false);
        valuePanel.add(createIconLabel(), BorderLayout.WEST);
        valuePanel.add(valueComponent, BorderLayout.CENTER);

        rowPanel.add(labelComponent, BorderLayout.WEST);
        rowPanel.add(valuePanel, BorderLayout.CENTER);

        return rowPanel;
    }

    private JLabel createIconLabel() {
        JLabel iconLabel = new JLabel("•") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(70, 130, 180));
                g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("•", 0, y);
            }
        };
        iconLabel.setPreferredSize(new Dimension(15, 15));
        return iconLabel;
    }
}