package Music;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class CompilationListPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 248, 250);
    private static final Color PANEL_COLOR = new Color(255, 255, 255);
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static JList<MusicCompilation> compilationList;
    private static CompilationSearchPanel searchPanel;

    public static JScrollPane createScrollPane(DefaultListModel<MusicCompilation> listModel,
                                               Consumer<MusicCompilation> detailsAction) {
        compilationList = new JList<>(listModel);
        compilationList.setCellRenderer(new CompilationListRenderer());
        compilationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        compilationList.setBackground(PANEL_COLOR);

        compilationList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    MusicCompilation selected = compilationList.getSelectedValue();
                    if (selected != null) {
                        detailsAction.accept(selected);
                    }
                }
            }
        });

        ContextMenuFactory.createContextMenu(compilationList);

        JScrollPane scrollPane = new JScrollPane();
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(BACKGROUND_COLOR);

        searchPanel = new CompilationSearchPanel(compilationList, listModel);
        containerPanel.add(searchPanel, BorderLayout.NORTH);

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

    public static JList<MusicCompilation> getCompilationList() {
        return compilationList;
    }

    public static CompilationSearchPanel getSearchPanel() {
        return searchPanel;
    }
}