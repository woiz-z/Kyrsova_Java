package music.Music;

import java.io.Serializable;
import java.time.Duration;

public class MusicTrack implements Serializable {
    private Long id; // Для інтеграції з БД
    private String title;
    private String artist;
    private MusicGenre genre;
    private Duration duration;

    // Конструктор
    public MusicTrack(String title, String artist, MusicGenre genre, Duration duration) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
    }

    // Геттери та сеттери для інкапсуляції
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

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public MusicGenre getGenre() {
        return genre;
    }

    public void setGenre(MusicGenre genre) {
        this.genre = genre;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    // Для відображення в GUI
    @Override
    public String toString() {
        return title + " - " + artist + " (" + genre + ", " + duration.toMinutes() + " min)";
    }
}

