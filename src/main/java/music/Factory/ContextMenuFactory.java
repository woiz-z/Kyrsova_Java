package music.Factory;

import music.Music.MusicCompilation;
import music.MusicAppGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ContextMenuFactory {
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public static void createContextMenu(JList<MusicCompilation> compilationList) {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Змінити назву");
        editItem.setIcon(new ImageIcon(new byte[0]));
        editItem.setFont(MAIN_FONT);
        editItem.addActionListener(e -> {
            MusicAppGUI app = (MusicAppGUI) SwingUtilities.getAncestorOfClass(MusicAppGUI.class, compilationList);
            if (app != null) {
                app.renameCompilation();
            }
        });

        JMenuItem deleteItem = new JMenuItem("Видалити");
        deleteItem.setIcon(new ImageIcon(new byte[0]));
        deleteItem.setFont(MAIN_FONT);
        deleteItem.addActionListener(e -> {
            MusicAppGUI app = (MusicAppGUI) SwingUtilities.getAncestorOfClass(MusicAppGUI.class, compilationList);
            if (app != null) {
                app.deleteCompilation();
            }
        });

        contextMenu.add(editItem);
        contextMenu.add(deleteItem);

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
}