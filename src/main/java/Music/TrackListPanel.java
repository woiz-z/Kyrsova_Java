package Music;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TrackListPanel {
    private final JPanel panel;
    private final DefaultListModel<MusicTrack> trackListModel;
    private final JList<MusicTrack> trackList;
    private List<MusicTrack> allTracks;
    private final TrackSearchPanel searchPanel;
    private final CompilationDetailsDialog parent;
    final MusicCompilation compilation;

    public TrackListPanel(CompilationDetailsDialog parent, MusicCompilation compilation) {
        this.parent = parent;
        this.compilation = compilation;
        panel = new JPanel(new BorderLayout());
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

        searchPanel = new TrackSearchPanel(trackList, trackListModel);
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTracksFromCompilation() {
        trackListModel.clear();
        List<MusicTrack> tracks = compilation.getTracks();
        for (MusicTrack track : tracks) {
            trackListModel.addElement(track);
        }
    }

    private void showTrackDetails(MusicTrack track) {
        TrackDetailsDialog dialog = new TrackDetailsDialog((JFrame) parent.getParent(), track);
        dialog.setVisible(true);
    }

    public JPanel getPanel() {
        return panel;
    }

    public JList<MusicTrack> getTrackList() {
        return trackList;
    }

    public DefaultListModel<MusicTrack> getTrackListModel() {
        return trackListModel;
    }

    public void filterTracksByDuration(Duration minDuration, Duration maxDuration, HeaderPanel headerPanel) {
        if (allTracks == null) {
            allTracks = new ArrayList<>();
            for (int i = 0; i < trackListModel.getSize(); i++) {
                allTracks.add(trackListModel.get(i));
            }
        }

        List<MusicTrack> filteredTracks = new ArrayList<>();
        for (MusicTrack track : allTracks) {
            Duration trackDuration = track.getDuration();
            if (trackDuration.compareTo(minDuration) >= 0 && trackDuration.compareTo(maxDuration) <= 0) {
                filteredTracks.add(track);
            }
        }

        trackListModel.clear();
        for (MusicTrack track : filteredTracks) {
            trackListModel.addElement(track);
        }

        headerPanel.updateFilterInfo(
                trackListModel.getSize(),
                calculateFilteredTotalDuration().toMinutes(),
                calculateFilteredTotalDuration().getSeconds(),
                minDuration.toMinutes(),
                minDuration.getSeconds(),
                maxDuration.toMinutes(),
                maxDuration.getSeconds()
        );

        JOptionPane.showMessageDialog(parent,
                filteredTracks.isEmpty() ? "Не знайдено треків у вказаному діапазоні тривалості" :
                        "Знайдено " + filteredTracks.size() + " треків у вказаному діапазоні",
                "Результат фільтрації",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private Duration calculateFilteredTotalDuration() {
        long totalSeconds = 0;
        for (int i = 0; i < trackListModel.getSize(); i++) {
            totalSeconds += trackListModel.get(i).getDuration().getSeconds();
        }
        return Duration.ofSeconds(totalSeconds);
    }

    public void resetFilter(HeaderPanel headerPanel) {
        if (allTracks != null && !allTracks.isEmpty()) {
            trackListModel.clear();
            for (MusicTrack track : allTracks) {
                trackListModel.addElement(track);
            }

            headerPanel.updateInfo(
                    trackListModel.getSize(),
                    compilation.calculateTotalDuration().toMinutes(),
                    compilation.calculateTotalDuration().getSeconds()
            );

            JOptionPane.showMessageDialog(parent,
                    "Фільтр скинуто",
                    "Інформація",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public CompilationDetailsDialog getParent() {
        return parent;
    }
}