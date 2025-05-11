package Music;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MusicCompilation implements Serializable {
    private Long id; // Для інтеграції з БД
    private String title;
    private List<MusicTrack> tracks;

    // Конструктор
    public MusicCompilation(String title) {
        this.title = title;
        this.tracks = new ArrayList<>();
    }

    // Геттери та сеттери
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MusicTrack> getTracks() {
        return new ArrayList<>(tracks);
    }

    // Додавання треку
    public void addTrack(MusicTrack track) {
        tracks.add(track);
    }

    // Підрахунок загальної тривалості
    public Duration calculateTotalDuration() {
        return tracks.stream()
                .map(MusicTrack::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    // Сортування за жанром
    public void sortByGenre() {
        tracks.sort(Comparator.comparing(track -> track.getGenre().toString()));
    }

    // Пошук треків у заданому діапазоні тривалості
    public List<MusicTrack> findTracksByDurationRange(Duration min, Duration max) {
        return tracks.stream()
                .filter(track -> !track.getDuration().minus(min).isNegative() &&
                        !track.getDuration().minus(max).isPositive())
                .collect(Collectors.toList());
    }

    // Для відображення в GUI
    @Override
    public String toString() {
        return title + " (" + tracks.size() + " tracks, " +
                calculateTotalDuration().toMinutes() + " min)";
    }
}