package Music;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class HeaderPanel {
    private final JPanel panel;
    private final JLabel infoLabel;

    public HeaderPanel(MusicCompilation compilation) {
        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.setOpaque(false);

        // Title label with shadow effect
        JLabel titleLabel = new JLabel(compilation.getTitle()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawString(getText(), 3, 23);

                // Main text
                g2.setColor(new Color(50, 50, 50));
                g2.drawString(getText(), 2, 22);

                // Additional effect
                g2.setColor(new Color(70, 130, 180, 100));
                g2.drawString(getText(), 1, 21);
            }
        };
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.CENTER);

        // Info label
        infoLabel = new JLabel(String.format(
                "%d треків • %d хв %d сек",
                compilation.getTracks().size(),
                compilation.calculateTotalDuration().toMinutes(),
                compilation.calculateTotalDuration().getSeconds() % 60
        ));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        panel.add(infoLabel, BorderLayout.SOUTH);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void updateInfo(int trackCount, long totalMinutes, long totalSeconds) {
        infoLabel.setText(String.format(
                "%d треків • %d хв %d сек",
                trackCount,
                totalMinutes,
                totalSeconds % 60
        ));
    }

    public void updateFilterInfo(int trackCount, long totalMinutes, long totalSeconds,
                                 long minMinutes, long minSeconds, long maxMinutes, long maxSeconds) {
        String filterInfo = String.format(
                "%d треків • %d хв %d сек • Фільтр: %d:%02d - %d:%02d",
                trackCount,
                totalMinutes,
                totalSeconds % 60,
                minMinutes,
                minSeconds % 60,
                maxMinutes,
                maxSeconds % 60
        );
        infoLabel.setText(filterInfo);
    }
}