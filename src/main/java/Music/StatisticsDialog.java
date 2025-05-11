package Music;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsDialog extends JDialog {
    private final MusicCompilation compilation;
    private JTabbedPane tabbedPane;

    public StatisticsDialog(JFrame parent, MusicCompilation compilation) {
        super(parent, "Статистика: " + compilation.getTitle(), true);
        this.compilation = compilation;
        initializeUI();
    }

    private void initializeUI() {
        setSize(1400, 900);
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

        // Заголовок
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Статистика збірки: " + compilation.getTitle()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

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
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel(String.format(
                "%d треків • %d хв %d сек",
                compilation.getTracks().size(),
                compilation.calculateTotalDuration().toMinutes(),
                compilation.calculateTotalDuration().getSeconds() % 60
        ));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        headerPanel.add(infoLabel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Вкладки з різними видами статистики
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setOpaque(false);

        // Додаємо вкладки
        addDurationTab();
        addGenreTab();
        addArtistTab();

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Кнопка закриття
        JButton closeButton = createModernButton("Закрити", new Color(120, 120, 120));
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addDurationTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Обчислюємо статистику
        List<MusicTrack> tracks = compilation.getTracks();
        Duration totalDuration = compilation.calculateTotalDuration();
        Duration avgDuration = tracks.isEmpty() ? Duration.ZERO :
                Duration.ofSeconds(totalDuration.getSeconds() / tracks.size());
        Duration shortest = tracks.stream().map(MusicTrack::getDuration)
                .min(Duration::compareTo).orElse(Duration.ZERO);
        Duration longest = tracks.stream().map(MusicTrack::getDuration)
                .max(Duration::compareTo).orElse(Duration.ZERO);

        // Панель з основними показниками
        JPanel statsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        statsPanel.add(createStatLabel("Загальна тривалість:"));
        statsPanel.add(createStatValue(formatDuration(totalDuration)));
        statsPanel.add(createStatLabel("Середня тривалість:"));
        statsPanel.add(createStatValue(formatDuration(avgDuration)));
        statsPanel.add(createStatLabel("Найкоротший трек:"));
        statsPanel.add(createStatValue(formatDuration(shortest)));
        statsPanel.add(createStatLabel("Найдовший трек:"));
        statsPanel.add(createStatValue(formatDuration(longest)));

        panel.add(statsPanel, BorderLayout.NORTH);

        // Гістограма тривалості
        JPanel histogramPanel = createDurationHistogram(tracks);
        panel.add(new JScrollPane(histogramPanel), BorderLayout.CENTER);

        tabbedPane.addTab("Тривалість", panel);
    }

    private void addGenreTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Групуємо треки за жанрами
        Map<MusicGenre, Long> genreCounts = compilation.getTracks().stream()
                .collect(Collectors.groupingBy(MusicTrack::getGenre, Collectors.counting()));

        // Сортуємо за кількістю треків
        List<Map.Entry<MusicGenre, Long>> sortedGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<MusicGenre, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Панель зі статистикою
        JPanel statsPanel = new JPanel(new GridLayout(sortedGenres.size() + 1, 2, 10, 5));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Заголовки
        statsPanel.add(createStatLabel("Жанр", true));
        statsPanel.add(createStatLabel("Кількість треків", true));

        // Дані
        for (Map.Entry<MusicGenre, Long> entry : sortedGenres) {
            statsPanel.add(createStatLabel(entry.getKey().toString()));
            statsPanel.add(createStatValue(entry.getValue().toString()));
        }

        panel.add(statsPanel, BorderLayout.NORTH);

        // Діаграма жанрів
        JPanel chartPanel = createGenreChart(sortedGenres);
        panel.add(new JScrollPane(chartPanel), BorderLayout.CENTER);

        tabbedPane.addTab("Жанри", panel);
    }

    private void addArtistTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Групуємо треки за виконавцями
        Map<String, Long> artistCounts = compilation.getTracks().stream()
                .collect(Collectors.groupingBy(MusicTrack::getArtist, Collectors.counting()));

        // Сортуємо за кількістю треків
        List<Map.Entry<String, Long>> sortedArtists = artistCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Обмежуємо кількість для відображення (наприклад, топ-10)
        int maxArtists = Math.min(10, sortedArtists.size());
        sortedArtists = sortedArtists.subList(0, maxArtists);

        // Панель зі статистикою
        JPanel statsPanel = new JPanel(new GridLayout(sortedArtists.size() + 1, 2, 10, 5));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Заголовки
        statsPanel.add(createStatLabel("Виконавець", true));
        statsPanel.add(createStatLabel("Кількість треків", true));

        // Дані
        for (Map.Entry<String, Long> entry : sortedArtists) {
            statsPanel.add(createStatLabel(entry.getKey()));
            statsPanel.add(createStatValue(entry.getValue().toString()));
        }

        panel.add(statsPanel, BorderLayout.NORTH);

        // Діаграма виконавців
        JPanel chartPanel = createArtistChart(sortedArtists);
        panel.add(new JScrollPane(chartPanel), BorderLayout.CENTER);

        tabbedPane.addTab("Виконавці", panel);
    }

    private JPanel createDurationHistogram(List<MusicTrack> tracks) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 80;
                int chartWidth = width - 2 * padding;
                int chartHeight = height - 2 * padding;

                // Знаходимо максимальну тривалість для масштабування
                Duration maxDuration = tracks.stream()
                        .map(MusicTrack::getDuration)
                        .max(Duration::compareTo)
                        .orElse(Duration.ofMinutes(5));
                long maxSeconds = maxDuration.getSeconds();

                // Малюємо осі
                g2d.setColor(new Color(100, 100, 100));
                g2d.drawLine(padding, height - padding, width - padding, height - padding); // X
                g2d.drawLine(padding, height - padding, padding, padding); // Y

                // Малюємо підписи на осях
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                for (int i = 0; i <= 5; i++) {
                    int x = padding + (i * chartWidth / 5);
                    String label = String.format("%d:%02d",
                            (maxSeconds * i / 5) / 60,
                            (maxSeconds * i / 5) % 60);
                    g2d.drawString(label, x - 15, height - padding + 20);
                    g2d.drawLine(x, height - padding, x, height - padding + 5);
                }

                // Малюємо стовпці
                int barWidth = Math.max(10, chartWidth / (tracks.size() * 2));
                int x = padding + barWidth / 2;
                Color barColor = new Color(70, 130, 180, 200);

                for (MusicTrack track : tracks) {
                    long seconds = track.getDuration().getSeconds();
                    int barHeight = (int) (chartHeight * seconds / maxSeconds);
                    g2d.setColor(barColor);
                    g2d.fillRect(x, height - padding - barHeight, barWidth, barHeight);
                    g2d.setColor(barColor.darker());
                    g2d.drawRect(x, height - padding - barHeight, barWidth, barHeight);

                    // Підписи під стовпцями (кожен 5-й або якщо мало треків)
                    if (tracks.size() < 15 || tracks.indexOf(track) % 5 == 0) {
                        String title = track.getTitle().length() > 10 ?
                                track.getTitle().substring(0, 7) + "..." : track.getTitle();
                        g2d.setColor(new Color(70, 70, 70));
                        g2d.rotate(-Math.PI / 4, x + barWidth / 2, height - padding + 15);
                        g2d.drawString(title, x - 10, height - padding + 15);
                        g2d.rotate(Math.PI / 4, x + barWidth / 2, height - padding + 15);
                    }

                    x += barWidth * 2;
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 300);
            }
        };

        return panel;
    }

    private JPanel createGenreChart(List<Map.Entry<MusicGenre, Long>> genres) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 80;
                int chartWidth = width - 2 * padding;
                int chartHeight = height - 2 * padding;

                // Знаходимо максимальну кількість для масштабування
                long maxCount = genres.stream()
                        .mapToLong(Map.Entry::getValue)
                        .max()
                        .orElse(1);

                // Малюємо осі
                g2d.setColor(new Color(100, 100, 100));
                g2d.drawLine(padding, height - padding, width - padding, height - padding); // X
                g2d.drawLine(padding, height - padding, padding, padding); // Y

                // Малюємо підписи на осях
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                for (int i = 0; i <= 5; i++) {
                    int y = height - padding - (i * chartHeight / 5);
                    g2d.drawString(String.valueOf(maxCount * i / 5), padding - 30, y + 5);
                    g2d.drawLine(padding, y, padding - 5, y);
                }

                // Малюємо стовпці
                int barWidth = Math.max(20, chartWidth / (genres.size() * 2));
                int x = padding + barWidth / 2;
                Color[] colors = {
                        new Color(70, 130, 180),
                        new Color(76, 175, 80),
                        new Color(244, 67, 54),
                        new Color(255, 152, 0),
                        new Color(156, 39, 176)
                };

                for (Map.Entry<MusicGenre, Long> entry : genres) {
                    int barHeight = (int) (chartHeight * entry.getValue() / maxCount);
                    Color color = colors[genres.indexOf(entry) % colors.length];
                    g2d.setColor(color);
                    g2d.fillRect(x, height - padding - barHeight, barWidth, barHeight);
                    g2d.setColor(color.darker());
                    g2d.drawRect(x, height - padding - barHeight, barWidth, barHeight);

                    // Підписи під стовпцями
                    String genre = entry.getKey().toString();
                    if (genre.length() > 10) {
                        genre = genre.substring(0, 7) + "...";
                    }
                    g2d.setColor(new Color(70, 70, 70));
                    g2d.drawString(genre, x - 10, height - padding + 15);

                    // Підписи над стовпцями
                    g2d.drawString(entry.getValue().toString(), x + 5, height - padding - barHeight - 5);

                    x += barWidth * 2;
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 300);
            }
        };

        return panel;
    }

    private JPanel createArtistChart(List<Map.Entry<String, Long>> artists) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 80;
                int chartWidth = width - 2 * padding;
                int chartHeight = height - 2 * padding;

                // Знаходимо максимальну кількість для масштабування
                long maxCount = artists.stream()
                        .mapToLong(Map.Entry::getValue)
                        .max()
                        .orElse(1);

                // Малюємо осі
                g2d.setColor(new Color(100, 100, 100));
                g2d.drawLine(padding, height - padding, width - padding, height - padding); // X
                g2d.drawLine(padding, height - padding, padding, padding); // Y

                // Малюємо підписи на осях
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                for (int i = 0; i <= 5; i++) {
                    int y = height - padding - (i * chartHeight / 5);
                    g2d.drawString(String.valueOf(maxCount * i / 5), padding - 30, y + 5);
                    g2d.drawLine(padding, y, padding - 5, y);
                }

                // Малюємо стовпці
                int barWidth = Math.max(20, chartWidth / (artists.size() * 2));
                int x = padding + barWidth / 2;
                Color[] colors = {
                        new Color(70, 130, 180),
                        new Color(76, 175, 80),
                        new Color(244, 67, 54),
                        new Color(255, 152, 0),
                        new Color(156, 39, 176),
                        new Color(96, 125, 139),
                        new Color(121, 85, 72),
                        new Color(233, 30, 99),
                        new Color(0, 150, 136),
                        new Color(63, 81, 181)
                };

                for (Map.Entry<String, Long> entry : artists) {
                    int barHeight = (int) (chartHeight * entry.getValue() / maxCount);
                    Color color = colors[artists.indexOf(entry) % colors.length];
                    g2d.setColor(color);
                    g2d.fillRect(x, height - padding - barHeight, barWidth, barHeight);
                    g2d.setColor(color.darker());
                    g2d.drawRect(x, height - padding - barHeight, barWidth, barHeight);

                    // Підписи під стовпцями
                    String artist = entry.getKey();
                    if (artist.length() > 10) {
                        artist = artist.substring(0, 7) + "...";
                    }
                    g2d.setColor(new Color(70, 70, 70));
                    g2d.rotate(-Math.PI / 4, x + barWidth / 2, height - padding + 15);
                    g2d.drawString(artist, x - 10, height - padding + 15);
                    g2d.rotate(Math.PI / 4, x + barWidth / 2, height - padding + 15);

                    // Підписи над стовпцями
                    g2d.drawString(entry.getValue().toString(), x + 5, height - padding - barHeight - 5);

                    x += barWidth * 2;
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 300);
            }
        };

        return panel;
    }

    private JLabel createStatLabel(String text) {
        return createStatLabel(text, false);
    }

    private JLabel createStatLabel(String text, boolean isHeader) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", isHeader ? Font.BOLD : Font.PLAIN, 14));
        label.setForeground(isHeader ? new Color(70, 70, 70) : new Color(50, 50, 50));
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        return label;
    }

    private JLabel createStatValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180));
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        return label;
    }

    private String formatDuration(Duration duration) {
        return String.format("%d хв %02d сек",
                duration.toMinutes(),
                duration.getSeconds() % 60);
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
}