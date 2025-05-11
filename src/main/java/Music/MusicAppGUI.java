package Music;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MusicAppGUI extends JFrame {
    private final DiscManager discManager = new DiscManager();
    private final DefaultListModel<MusicCompilation> listModel = new DefaultListModel<>();
    private JList<MusicCompilation> compilationList;
    private JLabel statusBar;
    private CompilationSearchPanel searchPanel;
    private static final Color BACKGROUND_COLOR = new Color(245, 248, 250);

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

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        add(mainPanel);

        JToolBar toolBar = ToolBarFactory.createToolBar(this::loadFromFile, this::saveToFile,
                this::addCompilation, this::renameCompilation, this::deleteCompilation);
        mainPanel.add(toolBar, BorderLayout.NORTH);

        JScrollPane scrollPane = CompilationListPanel.createScrollPane(listModel, this::showDetails);
        compilationList = CompilationListPanel.getCompilationList();
        searchPanel = CompilationListPanel.getSearchPanel();
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        statusBar = StatusBarFactory.createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);
    }

    private void showDetails(MusicCompilation compilation) {
        new CompilationDetailsDialog(this, compilation).setVisible(true);
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

    private void addCompilation() {
        DialogFactory.showAddCompilationDialog(this, discManager, listModel, statusBar);
    }

    void renameCompilation() {
        MusicCompilation selected = compilationList.getSelectedValue();
        if (selected == null) {
            showError("Помилка", "Спочатку виберіть збірку для перейменування");
            return;
        }
        DialogFactory.showRenameCompilationDialog(this, discManager, listModel, statusBar, selected);
    }

    void deleteCompilation() {
        MusicCompilation selected = compilationList.getSelectedValue();
        if (selected == null) {
            showError("Помилка", "Спочатку виберіть збірку для видалення");
            return;
        }
        DialogFactory.showDeleteCompilationDialog(this, discManager, listModel, statusBar, selected);
    }

    private void refreshCompilationList() {
        listModel.clear();
        discManager.getCompilations().forEach(listModel::addElement);
        if (searchPanel != null) {
            searchPanel.updateCompilationList(discManager.getCompilations());
        }
    }

    private void showError(String title, String message) {
        statusBar.setText(" Помилка: " + message);
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                UIManager.put("ScrollPane.background", BACKGROUND_COLOR);
                UIManager.put("Viewport.background", new Color(255, 255, 255));
                UIManager.put("List.background", new Color(255, 255, 255));
                UIManager.put("Panel.background", BACKGROUND_COLOR);
                UIManager.put("ToolBar.background", new Color(255, 255, 255));
            } catch (Exception e) {
                e.printStackTrace();
            }
            MusicAppGUI app = new MusicAppGUI();
            app.setVisible(true);
        });
    }
}