package Music;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TrackListDragAndDropHandler {
    private final JList<MusicTrack> trackList;
    private final DefaultListModel<MusicTrack> listModel;
    private int dragSourceIndex;

    public TrackListDragAndDropHandler(JList<MusicTrack> trackList, DefaultListModel<MusicTrack> listModel) {
        this.trackList = trackList;
        this.listModel = listModel;
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        trackList.setDragEnabled(true);
        trackList.setDropMode(DropMode.INSERT);
        trackList.setTransferHandler(new TrackTransferHandler());

        trackList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragSourceIndex = trackList.locationToIndex(e.getPoint());
            }
        });

        new DropTarget(trackList, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                handleDragEnter(dtde);
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                handleDragOver(dtde);
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {
                handleDragExit();
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                handleDrop(dtde);
            }
        });
    }

    private void handleDragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }

    private void handleDragOver(DropTargetDragEvent dtde) {
        Point location = dtde.getLocation();
        int index = trackList.locationToIndex(location);

        if (index >= 0 && index != dragSourceIndex) {
            Rectangle cellBounds = trackList.getCellBounds(index, index);
            trackList.setSelectionInterval(index, index);
            trackList.scrollRectToVisible(cellBounds);
        }
    }

    private void handleDragExit() {
        trackList.clearSelection();
    }

    private void handleDrop(DropTargetDropEvent dtde) {
        if (!dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.rejectDrop();
            return;
        }

        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
        Point location = dtde.getLocation();
        int dropIndex = trackList.locationToIndex(location);

        if (dropIndex < 0) {
            dropIndex = listModel.getSize() - 1;
        }

        if (dragSourceIndex != dropIndex) {
            MusicTrack draggedTrack = listModel.getElementAt(dragSourceIndex);
            listModel.remove(dragSourceIndex);

            listModel.add(dropIndex, draggedTrack);
            trackList.setSelectedIndex(dropIndex);

            // Оновлення бази даних через батьківський компонент
            updateDatabaseAfterReorder();
        }

        dtde.dropComplete(true);
    }

    private void updateDatabaseAfterReorder() {
        // Отримуємо батьківський діалог через ієрархію компонентів
        Component parent = SwingUtilities.getWindowAncestor(trackList);
        if (parent instanceof CompilationDetailsDialog) {
            ((CompilationDetailsDialog) parent).updateTracksInDatabase();
        }
    }

    private class TrackTransferHandler extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            return new StringSelection(trackList.getSelectedValue().toString());
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {}
    }
}