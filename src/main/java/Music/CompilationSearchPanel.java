package Music;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CompilationSearchPanel extends JPanel {
    private final JTextField searchField;
    private final DefaultListModel<MusicCompilation> originalModel;
    private final DefaultListModel<MusicCompilation> filteredModel;
    private final JList<MusicCompilation> compilationList;
    private List<MusicCompilation> allCompilations;

    public CompilationSearchPanel(JList<MusicCompilation> compilationList, DefaultListModel<MusicCompilation> listModel) {
        this.compilationList = compilationList;
        this.originalModel = listModel;
        this.filteredModel = new DefaultListModel<>();
        this.allCompilations = new ArrayList<>();

        // Зберігаємо всі збірки перед пошуком
        for (int i = 0; i < originalModel.getSize(); i++) {
            allCompilations.add(originalModel.get(i));
        }

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        // Створення поля пошуку з покращеним стилем
        searchField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Фон
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                super.paintComponent(g);

                // Якщо поле порожнє і не в фокусі, малюємо плейсхолдер
                if (getText().isEmpty() && !hasFocus()) {
                    g2.setColor(new Color(150, 150, 150, 150));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString("Пошук збірок...", 23, y);
                }
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isFocusOwner()) {
                    g2.setColor(new Color(70, 130, 180));
                } else {
                    g2.setColor(new Color(200, 200, 200));
                }
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(new Color(50, 50, 50));
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        searchField.setOpaque(false);
        searchField.setColumns(20);

        // Панель пошуку з іконкою
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Іконка пошуку
        JLabel searchIcon = new JLabel("🔍") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(150, 150, 150));
                g2.setFont(getFont().deriveFont(16f));
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("🔍", 0, y);
            }
        };
        searchIcon.setPreferredSize(new Dimension(30, 30));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        searchIcon.setOpaque(false);

        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton clearButton = new JButton("×") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight());
                g2.setColor(getModel().isRollover() ? new Color(220, 220, 220) : new Color(240, 240, 240));
                g2.fillOval(0, 5, size - 1, size - 1);

                g2.setColor(new Color(120, 120, 120));
                g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
                FontMetrics fm = g2.getFontMetrics();
                int x = (size - fm.stringWidth("×")) / 2;
                int y = (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("×", x, y+5);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Без рамки
            }
        };
        clearButton.setPreferredSize(new Dimension(24, 24));
        clearButton.setMaximumSize(new Dimension(24, 24));
        clearButton.setContentAreaFilled(false);
        clearButton.setOpaque(false);
        clearButton.setFocusPainted(false);
        clearButton.setBorder(BorderFactory.createEmptyBorder());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            searchField.requestFocus();
        });

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(clearButton, BorderLayout.EAST);
        searchPanel.add(rightPanel, BorderLayout.EAST);

        add(searchPanel, BorderLayout.CENTER);

        // Додавання слухача для пошуку в реальному часі
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterCompilations();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterCompilations();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterCompilations();
            }
        });
    }

    private void filterCompilations() {
        String searchText = searchField.getText().toLowerCase();
        filteredModel.clear();

        if (searchText.isEmpty()) {
            // Якщо поле пошуку порожнє, показуємо всі збірки
            compilationList.setModel(originalModel);
        } else {
            // Фільтруємо збірки за введеним текстом
            for (MusicCompilation compilation : allCompilations) {
                if (matchesSearch(compilation, searchText)) {
                    filteredModel.addElement(compilation);
                }
            }
            compilationList.setModel(filteredModel);
        }
    }

    private boolean matchesSearch(MusicCompilation compilation, String searchText) {
        return compilation.getTitle().toLowerCase().contains(searchText) ||
                String.valueOf(compilation.getTracks().size()).contains(searchText);
    }

    public void updateCompilationList(List<MusicCompilation> compilations) {
        allCompilations = new ArrayList<>(compilations);
        filterCompilations(); // Повторно застосовуємо поточний фільтр після оновлення списку
    }
}