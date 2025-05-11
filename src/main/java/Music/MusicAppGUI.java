package Music;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.SQLException;

public class MusicAppGUI extends JFrame {
    private final DiscManager discManager = new DiscManager();
    private final DefaultListModel<MusicCompilation> listModel = new DefaultListModel<>();
    private JList<MusicCompilation> compilationList;
    private JLabel statusBar;
    private CompilationSearchPanel searchPanel;
    private static final Color BACKGROUND_COLOR = new Color(245, 248, 250);
    private static final Color PANEL_COLOR = new Color(255, 255, 255);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);
    private static final Color DELETE_COLOR = new Color(220, 53, 69);
    private static final Color EDIT_COLOR = new Color(33, 150, 243);
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);

    public MusicAppGUI() {
        initializeUI();
        refreshCompilationList();
    }

    private void initializeUI() {
        setTitle("Менеджер музичних збірок");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Головна панель
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        add(mainPanel);

        // Панель інструментів
        JToolBar toolBar = createToolBar();
        mainPanel.add(toolBar, BorderLayout.NORTH);

        // Список компіляцій
        JScrollPane scrollPane = createScrollPane();
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Контекстне меню для списку
        createContextMenu();

        // Статусний бар
        statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        toolBar.setBackground(PANEL_COLOR);

        // Кнопки
        addToolbarButton(toolBar, "Завантажити з файлу", "📂", this::loadFromFile);
        toolBar.addSeparator();
        addToolbarButton(toolBar, "Зберегти у файл", "💾", this::saveToFile);
        toolBar.addSeparator();
        addToolbarButton(toolBar, "Додати збірку", "➕", this::addCompilation);
        toolBar.addSeparator();
        addToolbarButton(toolBar, "Змінити назву", "✏️", this::renameCompilation);
        toolBar.addSeparator();
        addToolbarButton(toolBar, "Видалити збірку", "🗑️", this::deleteCompilation);

        return toolBar;
    }

    private JScrollPane createScrollPane() {
        compilationList = new JList<>(listModel);
        compilationList.setCellRenderer(new CompilationListRenderer());
        compilationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        compilationList.setBackground(PANEL_COLOR);

        // Додавання обробника подвійного кліку
        compilationList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    MusicCompilation selected = compilationList.getSelectedValue();
                    if (selected != null) {
                        new CompilationDetailsDialog(MusicAppGUI.this, selected).setVisible(true);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane();

        // Створення панелі з пошуком та списком
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(BACKGROUND_COLOR);

        // Додавання панелі пошуку

        searchPanel = new CompilationSearchPanel(compilationList, listModel);
        containerPanel.add(searchPanel, BorderLayout.NORTH);

        // Додавання списку
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Список збірок",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        MAIN_FONT.deriveFont(Font.BOLD),
                        new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        listPanel.setBackground(PANEL_COLOR);
        listPanel.add(compilationList, BorderLayout.CENTER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        containerPanel.add(listPanel, BorderLayout.CENTER);

        scrollPane.setViewportView(containerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        return scrollPane;
    }

    private void createContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Змінити назву");
        editItem.setIcon(new ImageIcon(new byte[0])); // Порожній значок для вирівнювання
        editItem.setFont(MAIN_FONT);
        editItem.addActionListener(e -> renameCompilation());

        JMenuItem deleteItem = new JMenuItem("Видалити");
        deleteItem.setIcon(new ImageIcon(new byte[0])); // Порожній значок для вирівнювання
        deleteItem.setFont(MAIN_FONT);
        deleteItem.addActionListener(e -> deleteCompilation());

        contextMenu.add(editItem);
        contextMenu.add(deleteItem);

        // Додавання контекстного меню до списку
        compilationList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            private void checkPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    compilationList.setSelectedIndex(compilationList.locationToIndex(e.getPoint()));
                    contextMenu.show(compilationList, e.getX(), e.getY());
                }
            }
        });
    }

    private JLabel createStatusBar() {
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

    private void addToolbarButton(JToolBar toolBar, String text, String icon, Runnable action) {
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

    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                discManager.loadFromFile(fileChooser.getSelectedFile().getPath());
                refreshCompilationList();
                statusBar.setText(" Успішно завантажено з файлу");
            } catch (IOException | ClassNotFoundException ex) {
                showError("Помилка завантаження з файлу", ex.getMessage());
            }
        }
    }

    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                discManager.saveToFile(fileChooser.getSelectedFile().getPath());
                statusBar.setText(" Успішно збережено у файл");
            } catch (IOException ex) {
                showError("Помилка збереження у файл", ex.getMessage());
            }
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

    private void addCompilation() {
        // Створення панелі з градієнтним фоном
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
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
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Заголовок
        JLabel titleLabel = new JLabel("Нова збірка");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(new Color(50, 50, 50));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Поле введення
        JTextField textField = new JTextField();
        textField.setFont(MAIN_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.add(textField, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton okButton = createModernButton("Додати", new Color(76, 175, 80));
        JButton cancelButton = createModernButton("Скасувати", new Color(120, 120, 120));

        okButton.addActionListener(e -> {
            String title = textField.getText().trim();
            if (!title.isEmpty()) {
                discManager.addCompilation(new MusicCompilation(title));
                refreshCompilationList();
                statusBar.setText(" Додано нову збірку: " + title);
                ((Window) SwingUtilities.getRoot(panel)).dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            ((Window) SwingUtilities.getRoot(panel)).dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Створення діалогового вікна
        JDialog dialog = new JDialog(this, true);
        dialog.setContentPane(panel);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private void renameCompilation() {
        MusicCompilation selected = compilationList.getSelectedValue();
        if (selected == null) {
            showError("Помилка", "Спочатку виберіть збірку для перейменування");
            return;
        }

        // Створення панелі з градієнтним фоном
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
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
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Заголовок
        JLabel titleLabel = new JLabel("Змінити назву збірки");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(new Color(50, 50, 50));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Поле введення
        JTextField textField = new JTextField(selected.getTitle());
        textField.setFont(MAIN_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.add(textField, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton okButton = createModernButton("Зберегти", EDIT_COLOR);
        JButton cancelButton = createModernButton("Скасувати", new Color(120, 120, 120));

        okButton.addActionListener(e -> {
            String newTitle = textField.getText().trim();
            if (!newTitle.isEmpty()) {
                discManager.updateCompilationTitle(selected, newTitle);
                refreshCompilationList();
                statusBar.setText(" Збірку перейменовано на: " + newTitle);
                ((Window) SwingUtilities.getRoot(panel)).dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            ((Window) SwingUtilities.getRoot(panel)).dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Створення діалогового вікна
        JDialog dialog = new JDialog(this, true);
        dialog.setContentPane(panel);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private void deleteCompilation() {
        MusicCompilation selected = compilationList.getSelectedValue();
        if (selected == null) {
            showError("Помилка", "Спочатку виберіть збірку для видалення");
            return;
        }

        // Діалог підтвердження видалення
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

        // Повідомлення
        JLabel messageLabel = new JLabel(String.format(
                "<html><div style='text-align: center;'>" +
                        "<b>Ви впевнені, що хочете видалити збірку?</b><br><br>" +
                        "Назва: <i>%s</i><br>" +
                        "Кількість треків: <i>%d</i><br><br>" +
                        "Ця дія незворотня!" +
                        "</div></html>",
                selected.getTitle(), selected.getTracks().size()
        ));
        messageLabel.setFont(MAIN_FONT);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton deleteButton = createModernButton("Видалити", DELETE_COLOR);
        JButton cancelButton = createModernButton("Скасувати", new Color(120, 120, 120));

        deleteButton.addActionListener(e -> {
            discManager.removeCompilation(selected);
            refreshCompilationList();
            statusBar.setText(" Збірку видалено: " + selected.getTitle());
            ((Window) SwingUtilities.getRoot(panel)).dispose();
        });

        cancelButton.addActionListener(e -> {
            ((Window) SwingUtilities.getRoot(panel)).dispose();
        });

        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Створення діалогового вікна
        JDialog dialog = new JDialog(this, true);
        dialog.setContentPane(panel);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private void refreshCompilationList() {
        listModel.clear();
        discManager.getCompilations().forEach(listModel::addElement);
        if (searchPanel != null) { // Додайте перевірку на null
            searchPanel.updateCompilationList(discManager.getCompilations());
        }
    }
    private void showError(String title, String message) {
        statusBar.setText(" Помилка: " + message);
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private static class CompilationListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof MusicCompilation) {
                MusicCompilation compilation = (MusicCompilation) value;
                setText(String.format("<html><div style='padding:5px;'>" +
                                "<b style='font-size:14px; color:#333;'>%s</b><br>" +
                                "<span style='color:#666; font-size:12px;'>%d треків • %d хв</span>" +
                                "</div></html>",
                        compilation.getTitle(),
                        compilation.getTracks().size(),
                        compilation.calculateTotalDuration().toMinutes()));
                setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

                if (isSelected) {
                    setBackground(new Color(220, 240, 255));
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(180, 220, 255)),
                            BorderFactory.createEmptyBorder(5, 15, 5, 15)));
                } else {
                    setBackground(index % 2 == 0 ? new Color(255, 255, 255) : new Color(245, 248, 250));
                }
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // Глобальні налаштування для усунення проблем з фоном
                UIManager.put("ScrollPane.background", BACKGROUND_COLOR);
                UIManager.put("Viewport.background", PANEL_COLOR);
                UIManager.put("List.background", PANEL_COLOR);
                UIManager.put("Panel.background", BACKGROUND_COLOR);
                UIManager.put("ToolBar.background", PANEL_COLOR);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MusicAppGUI app = new MusicAppGUI();
            app.setVisible(true);
        });
    }
}