package Music;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;

public class TrackDialogs {
    public static void showAddTrackDialog(CompilationDetailsDialog parent, MusicCompilation compilation,
                                          TrackListPanel trackListPanel) {
        JPanel panel = createDialogPanel("Додати новий трек");

        // Fields
        JPanel fieldsPanel = createFieldsPanel();
        JTextField titleField = createStyledTextField();
        JTextField artistField = createStyledTextField();
        JComboBox<MusicGenre> genreCombo = createStyledComboBox(MusicGenre.values());
        JSpinner minutesSpinner = createStyledSpinner(0, 59, 3);
        JSpinner secondsSpinner = createStyledSpinner(0, 59, 30);

        fieldsPanel.add(createFormLabel("Назва треку:"));
        fieldsPanel.add(titleField);
        fieldsPanel.add(createFormLabel("Виконавець:"));
        fieldsPanel.add(artistField);
        fieldsPanel.add(createFormLabel("Жанр:"));
        fieldsPanel.add(genreCombo);
        fieldsPanel.add(createFormLabel("Тривалість (хвилини):"));
        fieldsPanel.add(minutesSpinner);
        fieldsPanel.add(createFormLabel("Тривалість (секунди):"));
        fieldsPanel.add(secondsSpinner);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = createButtonPanel();
        JButton okButton = createDialogButton("Додати", new Color(76, 175, 80));
        JButton cancelButton = createDialogButton("Скасувати", new Color(120, 120, 120));

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Dialog
        JDialog dialog = createDialog(parent, "Додати трек", panel, 400, 400);

        okButton.addActionListener(evt -> {
            String title = titleField.getText().trim();
            String artist = artistField.getText().trim();

            if (title.isEmpty() || artist.isEmpty()) {
                showErrorMessage(dialog, "Назва треку та виконавець не можуть бути порожніми");
                return;
            }

            MusicGenre genre = (MusicGenre) genreCombo.getSelectedItem();
            Duration duration = Duration.ofSeconds(
                    (int) minutesSpinner.getValue() * 60 + (int) secondsSpinner.getValue()
            );

            if (duration.isZero()) {
                showErrorMessage(dialog, "Тривалість треку не може бути нульовою");
                return;
            }

            MusicTrack newTrack = new MusicTrack(title, artist, genre, duration);
            TrackDatabaseManager.addTrackToCompilation(parent, compilation, trackListPanel, newTrack);
            dialog.dispose();
        });

        cancelButton.addActionListener(evt -> dialog.dispose());

        dialog.setVisible(true);
    }

    public static void showEditTrackDialog(CompilationDetailsDialog parent, TrackListPanel trackListPanel) {
        MusicTrack selectedTrack = trackListPanel.getTrackList().getSelectedValue();
        if (selectedTrack == null) {
            JOptionPane.showMessageDialog(parent,
                    "Будь ласка, виберіть трек для редагування",
                    "Попередження",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = createDialogPanel("Редагувати трек: " + selectedTrack.getTitle());
        JPanel fieldsPanel = createFieldsPanel();

        JTextField titleField = createStyledTextField(selectedTrack.getTitle());
        JTextField artistField = createStyledTextField(selectedTrack.getArtist());
        JComboBox<MusicGenre> genreCombo = createStyledComboBox(MusicGenre.values());
        genreCombo.setSelectedItem(selectedTrack.getGenre());
        long totalSeconds = selectedTrack.getDuration().getSeconds();
        JSpinner minutesSpinner = createStyledSpinner(0, 59, (int)(totalSeconds / 60));
        JSpinner secondsSpinner = createStyledSpinner(0, 59, (int)(totalSeconds % 60));

        fieldsPanel.add(createFormLabel("Назва треку:"));
        fieldsPanel.add(titleField);
        fieldsPanel.add(createFormLabel("Виконавець:"));
        fieldsPanel.add(artistField);
        fieldsPanel.add(createFormLabel("Жанр:"));
        fieldsPanel.add(genreCombo);
        fieldsPanel.add(createFormLabel("Тривалість (хвилини):"));
        fieldsPanel.add(minutesSpinner);
        fieldsPanel.add(createFormLabel("Тривалість (секунди):"));
        fieldsPanel.add(secondsSpinner);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        JButton okButton = createDialogButton("Зберегти", new Color(33, 150, 243));
        JButton cancelButton = createDialogButton("Скасувати", new Color(120, 120, 120));

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = createDialog(parent, "Редагувати трек", panel, 400, 400);

        okButton.addActionListener(evt -> {
            String title = titleField.getText().trim();
            String artist = artistField.getText().trim();

            if (title.isEmpty() || artist.isEmpty()) {
                showErrorMessage(dialog, "Назва треку та виконавець не можуть бути порожніми");
                return;
            }

            int minutes = ((Number)minutesSpinner.getValue()).intValue();
            int seconds = ((Number)secondsSpinner.getValue()).intValue();
            Duration duration = Duration.ofSeconds(minutes * 60 + seconds);

            if (duration.isZero()) {
                showErrorMessage(dialog, "Тривалість треку не може бути нульовою");
                return;
            }

            selectedTrack.setTitle(title);
            selectedTrack.setArtist(artist);
            selectedTrack.setGenre((MusicGenre) genreCombo.getSelectedItem());
            selectedTrack.setDuration(duration);

            TrackDatabaseManager.updateTrack(parent, trackListPanel, selectedTrack);
            dialog.dispose();
        });

        cancelButton.addActionListener(evt -> dialog.dispose());

        dialog.setVisible(true);
    }

    public static void showFilterByDurationDialog(CompilationDetailsDialog parent, TrackListPanel trackListPanel) {
        JPanel panel = createDialogPanel("Фільтрувати треки за тривалістю");
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        fieldsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        fieldsPanel.setBackground(new Color(245, 248, 250));

        JSpinner minMinutesSpinner = createStyledSpinner(0, 59, 0);
        JSpinner minSecondsSpinner = createStyledSpinner(0, 59, 0);
        JSpinner maxMinutesSpinner = createStyledSpinner(0, 59, 10);
        JSpinner maxSecondsSpinner = createStyledSpinner(0, 59, 0);

        fieldsPanel.add(createFormLabel("Мін. хвилин:"));
        fieldsPanel.add(minMinutesSpinner);
        fieldsPanel.add(createFormLabel("Мін. секунд:"));
        fieldsPanel.add(minSecondsSpinner);
        fieldsPanel.add(createFormLabel("Макс. хвилин:"));
        fieldsPanel.add(maxMinutesSpinner);
        fieldsPanel.add(createFormLabel("Макс. секунд:"));
        fieldsPanel.add(maxSecondsSpinner);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        JButton okButton = createDialogButton("Фільтрувати", new Color(255, 152, 0));
        JButton cancelButton = createDialogButton("Скасувати", new Color(120, 120, 120));

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = createDialog(parent, "Фільтр за тривалістю", panel, 350, 350);

        okButton.addActionListener(evt -> {
            int minMinutes = ((Number)minMinutesSpinner.getValue()).intValue();
            int minSeconds = ((Number)minSecondsSpinner.getValue()).intValue();
            int maxMinutes = ((Number)maxMinutesSpinner.getValue()).intValue();
            int maxSeconds = ((Number)maxSecondsSpinner.getValue()).intValue();

            Duration minDuration = Duration.ofSeconds(minMinutes * 60 + minSeconds);
            Duration maxDuration = Duration.ofSeconds(maxMinutes * 60 + maxSeconds);

            if (maxDuration.compareTo(minDuration) < 0) {
                showErrorMessage(dialog, "Максимальна тривалість повинна бути більшою за мінімальну");
                return;
            }

            trackListPanel.filterTracksByDuration(minDuration, maxDuration, getHeaderPanel(parent));
            dialog.dispose();
        });

        cancelButton.addActionListener(evt -> dialog.dispose());

        dialog.setVisible(true);
    }

    private static JPanel createDialogPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(245, 248, 250));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    private static JPanel createFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        fieldsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        fieldsPanel.setBackground(new Color(245, 248, 250));
        return fieldsPanel;
    }

    private static JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        buttonPanel.setBackground(new Color(245, 248, 250));
        return buttonPanel;
    }

    private static JDialog createDialog(CompilationDetailsDialog parent, String title, JPanel panel, int width, int height) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        return dialog;
    }

    private static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return field;
    }

    private static JTextField createStyledTextField(String text) {
        JTextField field = createStyledTextField();
        field.setText(text);
        return field;
    }

    private static <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(3, 5, 3, 5));
                return this;
            }
        });
        return comboBox;
    }

    private static JSpinner createStyledSpinner(int min, int max, int value) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        Component editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setColumns(3);
        }

        return spinner;
    }

    private static JButton createDialogButton(String text, Color color) {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
            }
        };

        button.setContentAreaFilled(false);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        return button;
    }

    private static void showErrorMessage(JDialog dialog, String message) {
        JOptionPane.showMessageDialog(dialog,
                message,
                "Помилка",
                JOptionPane.ERROR_MESSAGE);
    }

    private static HeaderPanel getHeaderPanel(CompilationDetailsDialog parent) {
        JPanel mainPanel = (JPanel) parent.getContentPane().getComponent(0);
        return new HeaderPanel(parent.compilation); // Note: This might need adjustment
    }
}