package Music;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ButtonPanel {
    private final JPanel panel;
    private final CompilationDetailsDialog parent;
    private final MusicCompilation compilation;
    private final TrackListPanel trackListPanel;

    public ButtonPanel(CompilationDetailsDialog parent, MusicCompilation compilation, TrackListPanel trackListPanel) {
        this.parent = parent;
        this.compilation = compilation;
        this.trackListPanel = trackListPanel;
        panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panel.setOpaque(false);

        initializeButtons();
    }

    private void initializeButtons() {
        JButton addButton = createModernButton("Додати трек", new Color(76, 175, 80));
        JButton editButton = createModernButton("Редагувати", new Color(33, 150, 243));
        JButton deleteButton = createModernButton("Видалити", new Color(244, 67, 54));
        JButton sortButton = createModernButton("Сортувати за жанром", new Color(156, 39, 176));
        JButton filterDurationButton = createModernButton("Фільтр за тривалістю", new Color(255, 152, 0));
        JButton statsButton = createModernButton("Статистика", new Color(121, 85, 72));
        JButton closeButton = createModernButton("Закрити", new Color(120, 120, 120));
        JButton resetFilterButton = createModernButton("Скинути фільтр", new Color(96, 125, 139));

        addButton.addActionListener(e -> TrackDialogs.showAddTrackDialog(parent, compilation, trackListPanel));
        editButton.addActionListener(e -> TrackDialogs.showEditTrackDialog(parent, trackListPanel));
        deleteButton.addActionListener(e -> TrackDatabaseManager.deleteSelectedTrack(parent, trackListPanel, compilation));
        sortButton.addActionListener(e -> TrackDatabaseManager.sortTracksByGenre(trackListPanel, compilation));
        filterDurationButton.addActionListener(e -> TrackDialogs.showFilterByDurationDialog(parent, trackListPanel));
        resetFilterButton.addActionListener(e -> trackListPanel.resetFilter(getHeaderPanel()));
        statsButton.addActionListener(e -> showStatistics());
        closeButton.addActionListener(e -> parent.dispose());

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(sortButton);
        panel.add(filterDurationButton);
        panel.add(resetFilterButton);
        panel.add(statsButton);
        panel.add(closeButton);
    }

    private void showStatistics() {
        StatisticsDialog dialog = new StatisticsDialog((JFrame) parent.getParent(), compilation);
        dialog.setVisible(true);
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };

        button.setContentAreaFilled(false);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public JPanel getPanel() {
        return panel;
    }

    private HeaderPanel getHeaderPanel() {
        JPanel mainPanel = (JPanel) parent.getContentPane().getComponent(0);
        return new HeaderPanel(compilation); // Note: This might need adjustment based on how HeaderPanel is actually accessed
    }
}