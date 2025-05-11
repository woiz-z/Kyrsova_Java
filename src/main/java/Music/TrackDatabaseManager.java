package Music;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrackDatabaseManager {
    public static void addTrackToCompilation(CompilationDetailsDialog parent, MusicCompilation compilation,
                                             TrackListPanel trackListPanel, MusicTrack track) {
        trackListPanel.getTrackListModel().addElement(track);
        updateCompilationTracks(compilation, trackListPanel);
        saveTrackToDatabase(parent, track, compilation);
        updateHeaderInfo(parent, compilation, trackListPanel);
    }

    public static void updateTrack(CompilationDetailsDialog parent, TrackListPanel trackListPanel, MusicTrack track) {
        updateTrackInDatabase(parent, track);
        trackListPanel.getTrackListModel().set(trackListPanel.getTrackList().getSelectedIndex(), track);
        updateHeaderInfo(parent, trackListPanel.compilation, trackListPanel);
    }

    public static void deleteSelectedTrack(CompilationDetailsDialog parent, TrackListPanel trackListPanel,
                                           MusicCompilation compilation) {
        MusicTrack selectedTrack = trackListPanel.getTrackList().getSelectedValue();
        if (selectedTrack == null) {
            JOptionPane.showMessageDialog(parent,
                    "Будь ласка, виберіть трек для видалення",
                    "Попередження",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                parent,
                "Ви впевнені, що хочете видалити трек '" + selectedTrack.getTitle() + "'?",
                "Підтвердження видалення",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                deleteTrackFromDatabase(parent, selectedTrack);
                trackListPanel.getTrackListModel().removeElement(selectedTrack);
                updateCompilationTracks(compilation, trackListPanel);
                JOptionPane.showMessageDialog(parent,
                        "Трек '" + selectedTrack.getTitle() + "' успішно видалено",
                        "Видалення треку",
                        JOptionPane.INFORMATION_MESSAGE);
                updateHeaderInfo(parent, compilation, trackListPanel);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent,
                        "Помилка при видаленні треку: " + ex.getMessage(),
                        "Помилка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void sortTracksByGenre(TrackListPanel trackListPanel, MusicCompilation compilation) {
        List<MusicTrack> tracksToSort = new ArrayList<>();
        for (int i = 0; i < trackListPanel.getTrackListModel().getSize(); i++) {
            tracksToSort.add(trackListPanel.getTrackListModel().get(i));
        }

        tracksToSort.sort((t1, t2) -> t1.getGenre().compareTo(t2.getGenre()));

        trackListPanel.getTrackListModel().clear();
        for (MusicTrack track : tracksToSort) {
            trackListPanel.getTrackListModel().addElement(track);
        }

        updateCompilationTracks(compilation, trackListPanel);
        updateTracksInDatabase(trackListPanel.getParent(), compilation, trackListPanel);

        JOptionPane.showMessageDialog(trackListPanel.getParent(),
                "Треки успішно відсортовані за жанром",
                "Сортування",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static void updateCompilationTracks(MusicCompilation compilation, TrackListPanel trackListPanel) {
        List<MusicTrack> updatedTracks = new ArrayList<>();
        for (int i = 0; i < trackListPanel.getTrackListModel().getSize(); i++) {
            updatedTracks.add(trackListPanel.getTrackListModel().get(i));
        }

        try {
            java.lang.reflect.Field tracksField = MusicCompilation.class.getDeclaredField("tracks");
            tracksField.setAccessible(true);
            List<MusicTrack> internalList = (List<MusicTrack>) tracksField.get(compilation);
            internalList.clear();
            internalList.addAll(updatedTracks);
        } catch (Exception e) {
            System.err.println("Помилка при оновленні внутрішнього списку треків: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void saveTrackToDatabase(CompilationDetailsDialog parent, MusicTrack track, MusicCompilation compilation) {
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
            JOptionPane.showMessageDialog(parent,
                    "Помилка при збереженні треку: " + ex.getMessage(),
                    "Помилка бази даних",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateTrackInDatabase(CompilationDetailsDialog parent, MusicTrack track) {
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
            JOptionPane.showMessageDialog(parent,
                    "Помилка при оновленні треку: " + ex.getMessage(),
                    "Помилка бази даних",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void deleteTrackFromDatabase(CompilationDetailsDialog parent, MusicTrack track) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String sql = "DELETE FROM tracks WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, track.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(parent,
                    "Помилка при видаленні треку: " + ex.getMessage(),
                    "Помилка бази даних",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateTracksInDatabase(CompilationDetailsDialog parent, MusicCompilation compilation,
                                               TrackListPanel trackListPanel) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String deleteSql = "DELETE FROM tracks WHERE compilation_id = ?";
                PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                deleteStatement.setLong(1, compilation.getId());
                deleteStatement.executeUpdate();

                String insertSql = "INSERT INTO tracks (title, artist, genre, duration, compilation_id) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);

                for (int i = 0; i < trackListPanel.getTrackListModel().getSize(); i++) {
                    MusicTrack track = trackListPanel.getTrackListModel().get(i);
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
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(parent,
                    "Помилка при оновленні треків у базі даних: " + ex.getMessage(),
                    "Помилка бази даних",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateHeaderInfo(CompilationDetailsDialog parent, MusicCompilation compilation,
                                         TrackListPanel trackListPanel) {
        JPanel mainPanel = (JPanel) parent.getContentPane().getComponent(0);
        HeaderPanel headerPanel = new HeaderPanel(compilation); // Note: This might need adjustment
        headerPanel.updateInfo(
                trackListPanel.getTrackListModel().getSize(),
                compilation.calculateTotalDuration().toMinutes(),
                compilation.calculateTotalDuration().getSeconds()
        );
    }
}