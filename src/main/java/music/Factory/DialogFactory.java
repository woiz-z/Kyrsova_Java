package music.Factory;

import music.Manager.DiscManager;
import music.Music.MusicCompilation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogFactory {
    private static final Color EDIT_COLOR = new Color(33, 150, 243);
    private static final Color DELETE_COLOR = new Color(220, 53, 69);
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);

    public static void showAddCompilationDialog(JFrame parent, DiscManager discManager,
                                                DefaultListModel<MusicCompilation> listModel,
                                                JLabel statusBar) {
        JPanel panel = createGradientPanel();
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Нова збірка");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(new Color(50, 50, 50));
        panel.add(titleLabel, BorderLayout.NORTH);

        JTextField textField = createTextField();
        panel.add(textField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton okButton = createModernButton("Додати", new Color(76, 175, 80));
        JButton cancelButton = createModernButton("Скасувати", new Color(120, 120, 120));

        okButton.addActionListener(e -> {
            String title = textField.getText().trim();
            if (!title.isEmpty()) {
                discManager.addCompilation(new MusicCompilation(title));
                refreshList(listModel, discManager, statusBar, " Додано нову збірку: " + title);
                ((Window) SwingUtilities.getRoot(panel)).dispose();
            }
        });

        cancelButton.addActionListener(e -> ((Window) SwingUtilities.getRoot(panel)).dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        showDialog(parent, panel, 400, 200);
    }

    public static void showRenameCompilationDialog(JFrame parent, DiscManager discManager,
                                                   DefaultListModel<MusicCompilation> listModel,
                                                   JLabel statusBar, MusicCompilation selected) {
        JPanel panel = createGradientPanel();
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Змінити назву збірки");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(new Color(50, 50, 50));
        panel.add(titleLabel, BorderLayout.NORTH);

        JTextField textField = createTextField();
        textField.setText(selected.getTitle());
        panel.add(textField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton okButton = createModernButton("Зберегти", EDIT_COLOR);
        JButton cancelButton = createModernButton("Скасувати", new Color(120, 120, 120));

        okButton.addActionListener(e -> {
            String newTitle = textField.getText().trim();
            if (!newTitle.isEmpty()) {
                discManager.updateCompilationTitle(selected, newTitle);
                refreshList(listModel, discManager, statusBar, " Збірку перейменовано на: " + newTitle);
                ((Window) SwingUtilities.getRoot(panel)).dispose();
            }
        });

        cancelButton.addActionListener(e -> ((Window) SwingUtilities.getRoot(panel)).dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        showDialog(parent, panel, 400, 200);
    }

    public static void showDeleteCompilationDialog(JFrame parent, DiscManager discManager,
                                                   DefaultListModel<MusicCompilation> listModel,
                                                   JLabel statusBar, MusicCompilation selected) {
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(250, 245, 245);
                Color color2 = new Color(240, 230, 230);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel messageLabel = new JLabel(String.format(
                "<html><div style='text-align: center;'>" +
                        "<b>Ви впевнені, що хочете видалити збірку?</b><br><br>" +
                        "Назва: <i>%s</i><br>" +
                        "Кількість треків: <i>%d</i><br><br>" +
                        "Ця дія незворотня!" +
                        "</div></html>",
                selected.getTitle(), selected.getTracks().size()));
        messageLabel.setFont(MAIN_FONT);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton deleteButton = createModernButton("Видалити", DELETE_COLOR);
        JButton cancelButton = createModernButton("Скасувати", new Color(120, 120, 120));

        deleteButton.addActionListener(e -> {
            discManager.removeCompilation(selected);
            refreshList(listModel, discManager, statusBar, " Збірку видалено: " + selected.getTitle());
            ((Window) SwingUtilities.getRoot(panel)).dispose();
        });

        cancelButton.addActionListener(e -> ((Window) SwingUtilities.getRoot(panel)).dispose());

        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        showDialog(parent, panel, 350, 250);
    }

    private static JPanel createGradientPanel() {
        return new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(245, 248, 250);
                Color color2 = new Color(230, 235, 240);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }

    private static JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(MAIN_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return textField;
    }

    private static JButton createModernButton(String text, Color color) {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color.darker());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            }
        };
        button.setContentAreaFilled(false);
        button.setForeground(Color.WHITE);
        button.setFont(MAIN_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static void showDialog(JFrame parent, JPanel panel, int width, int height) {
        JDialog dialog = new JDialog(parent, true);
        dialog.setContentPane(panel);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private static void refreshList(DefaultListModel<MusicCompilation> listModel,
                                    DiscManager discManager, JLabel statusBar, String message) {
        listModel.clear();
        discManager.getCompilations().forEach(listModel::addElement);
        statusBar.setText(message);
    }
}