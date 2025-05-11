package Music;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class CompilationDetailsDialog extends JDialog {
    private final MusicCompilation compilation;
    private final DiscManager discManager;
    private DefaultListModel<MusicTrack> trackListModel;
    private JList<MusicTrack> trackList;
    private List<MusicTrack> allTracks;
    private TrackSearchPanel searchPanel;


    public CompilationDetailsDialog(JFrame parent, MusicCompilation compilation) {
        super(parent, "Деталі збірки: " + compilation.getTitle(), true);
        this.compilation = compilation;
        this.discManager = new DiscManager();
        initializeUI();
    }

    private void initializeUI() {
        setSize(1300, 650);
        setLocationRelativeTo(getParent());
        setResizable(true);

        // Основна панель з градієнтним фоном
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
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
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(mainPanel);

        // Заголовок з тінню
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Список треків з покращеним скролом
        JPanel trackListPanel = createTrackListPanel();
        mainPanel.add(trackListPanel, BorderLayout.CENTER);

        // Панель кнопок з ефектами наведення
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.setOpaque(false);

        // Текст з тінню
        JLabel titleLabel = new JLabel(compilation.getTitle()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Тінь
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawString(getText(), 3, 23);

                // Основний текст
                g2.setColor(new Color(50, 50, 50));
                g2.drawString(getText(), 2, 22);

                // Додатковий ефект
                g2.setColor(new Color(70, 130, 180, 100));
                g2.drawString(getText(), 1, 21);
            }
        };
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel(String.format(
                "%d треків • %d хв %d сек",
                compilation.getTracks().size(),
                compilation.calculateTotalDuration().toMinutes(),
                compilation.calculateTotalDuration().getSeconds() % 60
        ));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTrackListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Треки",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.setOpaque(false);

        trackListModel = new DefaultListModel<>();
        loadTracksFromCompilation();

        trackList = new JList<>(trackListModel);
        trackList.setOpaque(false);
        trackList.setCellRenderer(new ModernTrackListRenderer());
        trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trackList.setBackground(new Color(255, 255, 255, 200));

        new TrackListDragAndDropHandler(trackList, trackListModel);

        trackList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showTrackDetails(trackList.getSelectedValue());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(trackList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Додаємо панель пошуку
        searchPanel = new TrackSearchPanel(trackList, trackListModel);
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Новий метод для завантаження треків з колекції в модель списку
    private void loadTracksFromCompilation() {
        trackListModel.clear();
        List<MusicTrack> tracks = compilation.getTracks();
        for (MusicTrack track : tracks) {
            trackListModel.addElement(track);
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panel.setOpaque(false);

        JButton addButton = createModernButton("Додати трек", new Color(76, 175, 80));
        JButton editButton = createModernButton("Редагувати", new Color(33, 150, 243));
        JButton deleteButton = createModernButton("Видалити", new Color(244, 67, 54));
        JButton sortButton = createModernButton("Сортувати за жанром", new Color(156, 39, 176));
        JButton filterDurationButton = createModernButton("Фільтр за тривалістю", new Color(255, 152, 0));
        JButton statsButton = createModernButton("Статистика", new Color(121, 85, 72));
        JButton closeButton = createModernButton("Закрити", new Color(120, 120, 120));
        JButton resetFilterButton = createModernButton("Скинути фільтр", new Color(96, 125, 139));



        addButton.addActionListener(this::showAddTrackDialog);
        editButton.addActionListener(e -> showEditTrackDialog());
        deleteButton.addActionListener(e -> deleteSelectedTrack());
        sortButton.addActionListener(e -> sortTracksByGenre());
        closeButton.addActionListener(e -> dispose());
        filterDurationButton.addActionListener(e -> showFilterByDurationDialog());
        resetFilterButton.addActionListener(e -> resetFilter());
        statsButton.addActionListener(e -> showStatistics());

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(sortButton);
        panel.add(filterDurationButton);
        panel.add(resetFilterButton);
        panel.add(statsButton);
        panel.add(closeButton);

        return panel;
    }

    private void showStatistics() {
        StatisticsDialog dialog = new StatisticsDialog((JFrame) getParent(), compilation);
        dialog.setVisible(true);
    }

    private void filterTracksByDuration(Duration minDuration, Duration maxDuration) {
        // Зберігаємо всі треки перед фільтрацією, якщо вони ще не збережені
        if (allTracks == null) {
            allTracks = new ArrayList<>();
            for (int i = 0; i < trackListModel.getSize(); i++) {
                allTracks.add(trackListModel.get(i));
            }
        }

        // Створюємо новий список для відфільтрованих треків
        List<MusicTrack> filteredTracks = new ArrayList<>();

        // Фільтруємо треки за діапазоном тривалості
        for (MusicTrack track : allTracks) {
            Duration trackDuration = track.getDuration();
            if (trackDuration.compareTo(minDuration) >= 0 && trackDuration.compareTo(maxDuration) <= 0) {
                filteredTracks.add(track);
            }
        }

        // Оновлюємо модель списку
        trackListModel.clear();
        for (MusicTrack track : filteredTracks) {
            trackListModel.addElement(track);
        }

        // Оновлюємо інформацію в заголовку
        updateFilterInfo(minDuration, maxDuration);

        if (filteredTracks.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Не знайдено треків у вказаному діапазоні тривалості",
                    "Результат фільтрації",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Знайдено " + filteredTracks.size() + " треків у вказаному діапазоні",
                    "Результат фільтрації",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 4. Метод для оновлення інформації в заголовку після фільтрації
    private void updateFilterInfo(Duration minDuration, Duration maxDuration) {
        JPanel headerPanel = (JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0);
        JLabel infoLabel = (JLabel) headerPanel.getComponent(1);

        // Формуємо текст з інформацією про фільтр
        String filterInfo = String.format(
                "%d треків • %d хв %d сек • Фільтр: %d:%02d - %d:%02d",
                trackListModel.getSize(),
                calculateFilteredTotalDuration().toMinutes(),
                calculateFilteredTotalDuration().getSeconds() % 60,
                minDuration.toMinutes(),
                minDuration.getSeconds() % 60,
                maxDuration.toMinutes(),
                maxDuration.getSeconds() % 60
        );

        infoLabel.setText(filterInfo);
    }

    // 5. Метод для підрахунку загальної тривалості відфільтрованих треків
    private Duration calculateFilteredTotalDuration() {
        long totalSeconds = 0;
        for (int i = 0; i < trackListModel.getSize(); i++) {
            totalSeconds += trackListModel.get(i).getDuration().getSeconds();
        }
        return Duration.ofSeconds(totalSeconds);
    }

    // 7. Метод для скидання фільтра
    private void resetFilter() {
        // Перевіряємо, чи є збережені треки
        if (allTracks != null && !allTracks.isEmpty()) {
            // Оновлюємо модель списку
            trackListModel.clear();
            for (MusicTrack track : allTracks) {
                trackListModel.addElement(track);
            }

            // Оновлюємо заголовок без інформації про фільтр
            JPanel headerPanel = (JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0);
            JLabel infoLabel = (JLabel) headerPanel.getComponent(1);

            String info = String.format(
                    "%d треків • %d хв %d сек",
                    trackListModel.getSize(),
                    compilation.calculateTotalDuration().toMinutes(),
                    compilation.calculateTotalDuration().getSeconds() % 60
            );

            infoLabel.setText(info);


            JOptionPane.showMessageDialog(this,
                    "Фільтр скинуто",
                    "Інформація",
                    JOptionPane.INFORMATION_MESSAGE);
        }
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

    private void showTrackDetails(MusicTrack track) {
        TrackDetailsDialog dialog = new TrackDetailsDialog((JFrame) getParent(), track);
        dialog.setVisible(true);
    }

    // Оновлений метод showAddTrackDialog
    private void showAddTrackDialog(ActionEvent e) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(245, 248, 250));

        // Заголовок
        JLabel titleLabel = new JLabel("Додати новий трек", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Основні поля
        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        fieldsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        fieldsPanel.setBackground(new Color(245, 248, 250));

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

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        buttonPanel.setBackground(new Color(245, 248, 250));

        JButton okButton = createDialogButton("Додати", new Color(76, 175, 80));
        JButton cancelButton = createDialogButton("Скасувати", new Color(120, 120, 120));

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Створення діалогу
        JDialog dialog = new JDialog(this, "Додати трек", true);
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // Обробники подій
        okButton.addActionListener(evt -> {
            String title = titleField.getText().trim();
            String artist = artistField.getText().trim();

            if (title.isEmpty() || artist.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Назва треку та виконавець не можуть бути порожніми",
                        "Помилка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            MusicGenre genre = (MusicGenre) genreCombo.getSelectedItem();
            Duration duration = Duration.ofSeconds(
                    (int) minutesSpinner.getValue() * 60 + (int) secondsSpinner.getValue()
            );

            if (duration.isZero()) {
                JOptionPane.showMessageDialog(dialog,
                        "Тривалість треку не може бути нульовою",
                        "Помилка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            MusicTrack newTrack = new MusicTrack(title, artist, genre, duration);
            addTrackToCompilation(newTrack);
            dialog.dispose();
        });

        cancelButton.addActionListener(evt -> dialog.dispose());

        dialog.setVisible(true);
    }

    // Оновлений метод showEditTrackDialog
    private void showEditTrackDialog() {
        MusicTrack selectedTrack = trackList.getSelectedValue();
        if (selectedTrack == null) {
            JOptionPane.showMessageDialog(this,
                    "Будь ласка, виберіть трек для редагування",
                    "Попередження",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(245, 248, 250));

        // Заголовок
        JLabel titleLabel = new JLabel("Редагувати трек: " + selectedTrack.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Основні поля
        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        fieldsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        fieldsPanel.setBackground(new Color(245, 248, 250));

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

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        buttonPanel.setBackground(new Color(245, 248, 250));

        JButton okButton = createDialogButton("Зберегти", new Color(33, 150, 243));
        JButton cancelButton = createDialogButton("Скасувати", new Color(120, 120, 120));

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Створення діалогу
        JDialog dialog = new JDialog(this, "Редагувати трек", true);
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // Обробники подій
        okButton.addActionListener(evt -> {
            String title = titleField.getText().trim();
            String artist = artistField.getText().trim();

            if (title.isEmpty() || artist.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Назва треку та виконавець не можуть бути порожніми",
                        "Помилка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int minutes = ((Number)minutesSpinner.getValue()).intValue();
            int seconds = ((Number)secondsSpinner.getValue()).intValue();
            Duration duration = Duration.ofSeconds(minutes * 60 + seconds);

            if (duration.isZero()) {
                JOptionPane.showMessageDialog(dialog,
                        "Тривалість треку не може бути нульовою",
                        "Помилка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            selectedTrack.setTitle(title);
            selectedTrack.setArtist(artist);
            selectedTrack.setGenre((MusicGenre) genreCombo.getSelectedItem());
            selectedTrack.setDuration(duration);

            updateTrackInDatabase(selectedTrack);
            trackListModel.set(trackList.getSelectedIndex(), selectedTrack);
            updateHeaderInfo();
            dialog.dispose();
        });

        cancelButton.addActionListener(evt -> dialog.dispose());

        dialog.setVisible(true);
    }

    // Оновлений метод showFilterByDurationDialog
    private void showFilterByDurationDialog() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(245, 248, 250));

        // Заголовок
        JLabel titleLabel = new JLabel("Фільтрувати треки за тривалістю", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Основні поля
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

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        buttonPanel.setBackground(new Color(245, 248, 250));

        JButton okButton = createDialogButton("Фільтрувати", new Color(255, 152, 0));
        JButton cancelButton = createDialogButton("Скасувати", new Color(120, 120, 120));

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Створення діалогу
        JDialog dialog = new JDialog(this, "Фільтр за тривалістю", true);
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(350, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // Обробники подій
        okButton.addActionListener(evt -> {
            int minMinutes = ((Number)minMinutesSpinner.getValue()).intValue();
            int minSeconds = ((Number)minSecondsSpinner.getValue()).intValue();
            int maxMinutes = ((Number)maxMinutesSpinner.getValue()).intValue();
            int maxSeconds = ((Number)maxSecondsSpinner.getValue()).intValue();

            Duration minDuration = Duration.ofSeconds(minMinutes * 60 + minSeconds);
            Duration maxDuration = Duration.ofSeconds(maxMinutes * 60 + maxSeconds);

            if (maxDuration.compareTo(minDuration) < 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Максимальна тривалість повинна бути більшою за мінімальну",
                        "Помилка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            filterTracksByDuration(minDuration, maxDuration);
            dialog.dispose();
        });

        cancelButton.addActionListener(evt -> dialog.dispose());

        dialog.setVisible(true);
    }

    // Допоміжні методи для створення стилізованих компонентів
    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return field;
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = createStyledTextField();
        field.setText(text);
        return field;
    }

    private <T> JComboBox<T> createStyledComboBox(T[] items) {
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

    private JSpinner createStyledSpinner(int min, int max, int value) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        // Стилізація кнопок спінера
        Component editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setColumns(3);
        }

        return spinner;
    }

    private JButton createDialogButton(String text, Color color) {
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

    private void deleteSelectedTrack() {
        MusicTrack selectedTrack = trackList.getSelectedValue();
        if (selectedTrack == null) {
            JOptionPane.showMessageDialog(this,
                    "Будь ласка, виберіть трек для видалення",
                    "Попередження",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Ви впевнені, що хочете видалити трек '" + selectedTrack.getTitle() + "'?",
                "Підтвердження видалення",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Видаляємо з бази даних
                deleteTrackFromDatabase(selectedTrack);

                // Видаляємо з моделі візуального списку
                trackListModel.removeElement(selectedTrack);

                // Напряму модифікуємо список треків в об'єкті компіляції
                // Оскільки getTracks() повертає копію списку, ми повинні модифікувати
                // список треків напряму за допомогою updateCompilationTracks()
                updateCompilationTracks();

                // Виводимо повідомлення про успішне видалення
                JOptionPane.showMessageDialog(this,
                        "Трек '" + selectedTrack.getTitle() + "' успішно видалено",
                        "Видалення треку",
                        JOptionPane.INFORMATION_MESSAGE);

                // Оновлюємо заголовок з інформацією про кількість треків
                updateHeaderInfo();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Помилка при видаленні треку: " + ex.getMessage(),
                        "Помилка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Новий метод для оновлення списку треків у компіляції з trackListModel
    private void updateCompilationTracks() {
        // Отримуємо доступ до внутрішнього списку треків через сеттер
        List<MusicTrack> updatedTracks = new ArrayList<>();
        for (int i = 0; i < trackListModel.getSize(); i++) {
            updatedTracks.add(trackListModel.get(i));
        }

        // Тепер ми можемо напряму замінити треки в MusicCompilation
        setCompilationTracks(updatedTracks);
    }

    // Метод для прямого встановлення треків в об'єкт компіляції
    private void setCompilationTracks(List<MusicTrack> tracks) {
        try {
            // Отримуємо внутрішній список треків компіляції за допомогою рефлексії
            java.lang.reflect.Field tracksField = MusicCompilation.class.getDeclaredField("tracks");
            tracksField.setAccessible(true);

            // Замінюємо вміст внутрішнього списку
            List<MusicTrack> internalList = (List<MusicTrack>) tracksField.get(compilation);
            internalList.clear();
            internalList.addAll(tracks);
        } catch (Exception e) {
            // Якщо щось пішло не так, виведемо помилку
            System.err.println("Помилка при оновленні внутрішнього списку треків: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sortTracksByGenre() {
        // Отримуємо поточні треки з trackListModel
        List<MusicTrack> tracksToSort = new ArrayList<>();
        for (int i = 0; i < trackListModel.getSize(); i++) {
            tracksToSort.add(trackListModel.get(i));
        }

        // Сортуємо список за жанром
        tracksToSort.sort((t1, t2) -> t1.getGenre().compareTo(t2.getGenre()));

        // Оновлюємо модель списку
        trackListModel.clear();
        for (MusicTrack track : tracksToSort) {
            trackListModel.addElement(track);
        }

        // Оновлюємо список треків в об'єкті компіляції
        updateCompilationTracks();

        // Оновлюємо треки в базі даних
        updateTracksInDatabase();

        JOptionPane.showMessageDialog(this,
                "Треки успішно відсортовані за жанром",
                "Сортування",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateHeaderInfo() {
        // Це метод, який оновлює інформацію в заголовку діалогу
        // після видалення треку або інших змін
        JPanel headerPanel = (JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0);
        JLabel infoLabel = (JLabel) headerPanel.getComponent(1);

        infoLabel.setText(String.format(
                "%d треків • %d хв %d сек",
                trackListModel.getSize(),
                compilation.calculateTotalDuration().toMinutes(),
                compilation.calculateTotalDuration().getSeconds() % 60
        ));
    }

    private void addTrackToCompilation(MusicTrack track) {
        // Додаємо трек до візуальної моделі
        trackListModel.addElement(track);

        // Оновлюємо список треків у об'єкті компіляції
        updateCompilationTracks();

        // Зберігаємо в базу даних
        saveTrackToDatabase(track);

        // Оновлюємо інформацію в заголовку
        updateHeaderInfo();
    }

    private void saveTrackToDatabase(MusicTrack track) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO tracks (title, artist, genre, duration, compilation_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, track.getTitle());
            statement.setString(2, track.getArtist());
            statement.setString(3, track.getGenre().name());
            statement.setLong(4, track.getDuration().getSeconds());
            statement.setLong(5, compilation.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    track.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Помилка при збереженні треку: " + ex.getMessage(),
                    "Помилка бази даних",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTrackInDatabase(MusicTrack track) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String sql = "UPDATE tracks SET title = ?, artist = ?, genre = ?, duration = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, track.getTitle());
            statement.setString(2, track.getArtist());
            statement.setString(3, track.getGenre().name());
            statement.setLong(4, track.getDuration().getSeconds());
            statement.setLong(5, track.getId());

            statement.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Помилка при оновленні треку: " + ex.getMessage(),
                    "Помилка бази даних",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTrackFromDatabase(MusicTrack track) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String sql = "DELETE FROM tracks WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, track.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Помилка при видаленні треку: " + ex.getMessage(),
                    "Помилка бази даних",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void updateTracksInDatabase() {
        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Видаляємо всі треки цієї збірки в базі даних
                String deleteSql = "DELETE FROM tracks WHERE compilation_id = ?";
                PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                deleteStatement.setLong(1, compilation.getId());
                deleteStatement.executeUpdate();

                // Додаємо всі поточні треки знову
                String insertSql = "INSERT INTO tracks (title, artist, genre, duration, compilation_id) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);

                for (int i = 0; i < trackListModel.getSize(); i++) {
                    MusicTrack track = trackListModel.get(i);
                    insertStatement.setString(1, track.getTitle());
                    insertStatement.setString(2, track.getArtist());
                    insertStatement.setString(3, track.getGenre().name());
                    insertStatement.setLong(4, track.getDuration().getSeconds());
                    insertStatement.setLong(5, compilation.getId());
                    insertStatement.executeUpdate();

                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        track.setId(generatedKeys.getLong(1));
                    }
                }

                connection.commit();
            } catch (SQLException ex) {
                // У випадку помилки відкочуємо транзакцію
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Помилка при оновленні треків у базі даних: " + ex.getMessage(),
                    "Помилка бази даних",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class ModernTrackListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            if (value instanceof MusicTrack) {
                MusicTrack track = (MusicTrack) value;
                setText(String.format("<html><div style='padding:5px;'>" +
                                "<b style='font-size:14px; color:#333;'>%s</b><br>" +
                                "<span style='color:#666; font-size:12px;'>%s • <span style='color:#5e8c31;'>%s</span> • %d:%02d</span>" +
                                "</div></html>",
                        track.getTitle(),
                        track.getArtist(),
                        track.getGenre(),
                        track.getDuration().toMinutes(),
                        track.getDuration().getSeconds() % 60));

                if (isSelected) {
                    setBackground(new Color(220, 240, 255));
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(180, 220, 255)),
                            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
                } else {
                    setBackground(index % 2 == 0 ? new Color(255, 255, 255, 200) : new Color(245, 248, 250, 200));
                }
            }
            return this;
        }
    }
}