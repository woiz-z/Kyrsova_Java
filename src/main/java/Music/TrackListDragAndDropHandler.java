package Music;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrackListDragAndDropHandler {
    private final JList<MusicTrack> trackList;
    private final DefaultListModel<MusicTrack> listModel;
    private final TrackListPanel trackListPanel;
    private final CompilationDetailsDialog parent;
    private int dragSourceIndex;

    public TrackListDragAndDropHandler(JList<MusicTrack> trackList, DefaultListModel<MusicTrack> listModel,
                                       TrackListPanel trackListPanel, CompilationDetailsDialog parent) {
        this.trackList = trackList;
        this.listModel = listModel;
        this.trackListPanel = trackListPanel;
        this.parent = parent;
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
        if (dtde.isDataFlavorSupported(new DataFlavor(MusicTrack.class, "MusicTrack"))) {
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
        DataFlavor trackFlavor = new DataFlavor(MusicTrack.class, "MusicTrack");
        if (!dtde.isDataFlavorSupported(trackFlavor)) {
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
            try {
                Transferable transferable = dtde.getTransferable();
                MusicTrack draggedTrack = (MusicTrack) transferable.getTransferData(trackFlavor);

                // Reorder in the list model
                listModel.remove(dragSourceIndex);
                listModel.add(dropIndex, draggedTrack);
                trackList.setSelectedIndex(dropIndex);

                // Update the compilation tracks and database
                updateCompilationTracks();
                TrackDatabaseManager.updateTracksInDatabase(parent, parent.compilation, trackListPanel);

                dtde.dropComplete(true);
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
                dtde.dropComplete(false);
            }
        } else {
            dtde.dropComplete(true);
        }
    }

    private void updateCompilationTracks() {
        List<MusicTrack> updatedTracks = new ArrayList<>();
        for (int i = 0; i < listModel.getSize(); i++) {
            updatedTracks.add(listModel.get(i));
        }

        try {
            java.lang.reflect.Field tracksField = MusicCompilation.class.getDeclaredField("tracks");
            tracksField.setAccessible(true);
            List<MusicTrack> internalList = (List<MusicTrack>) tracksField.get(parent.compilation);
            internalList.clear();
            internalList.addAll(updatedTracks);
        } catch (Exception e) {
            System.err.println("Помилка при оновленні внутрішнього списку треків: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Помилка при оновленні порядку треків: " + e.getMessage(),
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private class TrackTransferHandler extends TransferHandler {
        private final DataFlavor trackFlavor = new DataFlavor(MusicTrack.class, "MusicTrack");

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            MusicTrack track = trackList.getSelectedValue();
            if (track != null) {
                return new TrackTransferable(track);
            }
            return null;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {}
    }

    private class TrackTransferable implements Transferable {
        private final MusicTrack track;
        private final DataFlavor trackFlavor = new DataFlavor(MusicTrack.class, "MusicTrack");

        public TrackTransferable(MusicTrack track) {
            this.track = track;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{trackFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(trackFlavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return track;
        }
    }
}