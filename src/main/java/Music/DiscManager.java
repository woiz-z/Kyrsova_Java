package Music;

import java.io.*;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DiscManager {
    private List<MusicCompilation> compilations;

    public DiscManager() {
        this.compilations = new ArrayList<>();
        loadFromDatabaseSilently(); // Автоматичне завантаження при створенні
    }

    // Додавання збірки
    public void addCompilation(MusicCompilation compilation) {
        compilations.add(compilation);
        saveCompilationToDatabase(compilation); // Зберігаємо тільки нову збірку
    }

    // Видалення збірки
    public boolean removeCompilation(MusicCompilation compilation) {
        boolean removed = compilations.remove(compilation);
        if (removed && compilation.getId() > 0) {
            deleteCompilationFromDatabase(compilation.getId());
        }
        return removed;
    }

    // Оновлення назви збірки
    public void updateCompilationTitle(MusicCompilation compilation, String newTitle) {
        compilation.setTitle(newTitle);
        if (compilation.getId() > 0) {
            updateCompilationInDatabase(compilation);
        }
    }

    private void loadFromDatabaseSilently() {
        try {
            loadFromDatabase();
        } catch (SQLException e) {
            System.err.println("Помилка завантаження з БД: " + e.getMessage());
        }
    }

    // Збереження на диск (серіалізація)
    public void saveToFile(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(compilations);
        }
    }

    // Завантаження з диска (десеріалізація)
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            compilations = (List<MusicCompilation>) ois.readObject();
        }
    }

    private void saveCompilationToDatabase(MusicCompilation compilation) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            // Збереження компіляції
            String insertCompilationSQL = "INSERT INTO compilations (title) VALUES (?)";
            PreparedStatement compilationStatement = connection.prepareStatement(insertCompilationSQL, Statement.RETURN_GENERATED_KEYS);

            compilationStatement.setString(1, compilation.getTitle());
            compilationStatement.executeUpdate();

            ResultSet generatedKeys = compilationStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                long compilationId = generatedKeys.getLong(1);
                compilation.setId(compilationId);

                // Збереження треків збірки
                String insertTrackSQL = "INSERT INTO tracks (title, artist, genre, duration, compilation_id) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement trackStatement = connection.prepareStatement(insertTrackSQL);

                for (MusicTrack track : compilation.getTracks()) {
                    trackStatement.setString(1, track.getTitle());
                    trackStatement.setString(2, track.getArtist());
                    trackStatement.setString(3, track.getGenre().name());
                    trackStatement.setLong(4, track.getDuration().toSeconds());
                    trackStatement.setLong(5, compilationId);
                    trackStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка збереження збірки в БД: " + e.getMessage());
        }
    }

    // Видалення збірки з бази даних
    private void deleteCompilationFromDatabase(long compilationId) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            // Спочатку видаляємо треки для цієї збірки (зовнішній ключ)
            String deleteTracksSQL = "DELETE FROM tracks WHERE compilation_id = ?";
            PreparedStatement deleteTracksStatement = connection.prepareStatement(deleteTracksSQL);
            deleteTracksStatement.setLong(1, compilationId);
            deleteTracksStatement.executeUpdate();

            // Потім видаляємо саму збірку
            String deleteCompilationSQL = "DELETE FROM compilations WHERE id = ?";
            PreparedStatement deleteCompilationStatement = connection.prepareStatement(deleteCompilationSQL);
            deleteCompilationStatement.setLong(1, compilationId);
            deleteCompilationStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Помилка видалення збірки з БД: " + e.getMessage());
        }
    }

    // Оновлення інформації про збірку в базі даних
    private void updateCompilationInDatabase(MusicCompilation compilation) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String updateSQL = "UPDATE compilations SET title = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(updateSQL);
            statement.setString(1, compilation.getTitle());
            statement.setLong(2, compilation.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Помилка оновлення збірки в БД: " + e.getMessage());
        }
    }

    // Завантаження з бази даних
    public void loadFromDatabase() throws SQLException {
        compilations.clear();

        try (Connection connection = DatabaseConfig.getConnection()) {
            String selectCompilationsSQL = "SELECT * FROM compilations";
            Statement compilationStatement = connection.createStatement();
            ResultSet compilationResult = compilationStatement.executeQuery(selectCompilationsSQL);

            while (compilationResult.next()) {
                MusicCompilation compilation = new MusicCompilation(compilationResult.getString("title"));
                compilation.setId(compilationResult.getLong("id"));

                String selectTracksSQL = "SELECT * FROM tracks WHERE compilation_id = ?";
                PreparedStatement trackStatement = connection.prepareStatement(selectTracksSQL);
                trackStatement.setLong(1, compilation.getId());
                ResultSet trackResult = trackStatement.executeQuery();

                while (trackResult.next()) {
                    MusicTrack track = new MusicTrack(
                            trackResult.getString("title"),
                            trackResult.getString("artist"),
                            MusicGenre.valueOf(trackResult.getString("genre")),
                            Duration.ofSeconds(trackResult.getLong("duration"))
                    );
                    track.setId(trackResult.getLong("id"));
                    compilation.addTrack(track);
                }

                compilations.add(compilation);
            }
        }
    }

    // Отримання всіх збірок для GUI
    public List<MusicCompilation> getCompilations() {
        return new ArrayList<>(compilations);
    }
}