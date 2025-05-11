package Music;

import java.io.Serializable;

public enum MusicGenre implements Serializable {
    ROCK("Rock"),
    POP("Pop"),
    JAZZ("Jazz"),
    CLASSICAL("Classical"),
    ELECTRONIC("Electronic"),
    HIP_HOP("Hip Hop"),
    RAP("Rap"),
    BLUES("Blues"),
    COUNTRY("Country"),
    FOLK("Folk"),
    REGGAE("Reggae"),
    METAL("Metal"),
    PUNK("Punk"),
    ALTERNATIVE("Alternative"),
    INDIE("Indie"),
    SOUL("Soul"),
    FUNK("Funk"),
    RNB("R&B"),
    GOSPEL("Gospel"),
    LATIN("Latin"),
    SALSA("Salsa"),
    TANGO("Tango"),
    FLAMENCO("Flamenco"),
    K_POP("K-Pop"),
    J_POP("J-Pop"),
    WORLD("World"),
    AMBIENT("Ambient"),
    TRANCE("Trance"),
    TECHNO("Techno"),
    HOUSE("House"),
    DUBSTEP("Dubstep"),
    DRUM_AND_BASS("Drum and Bass"),
    CHILL("Chill"),
    OPERA("Opera"),
    ORCHESTRAL("Orchestral"),
    BAROQUE("Baroque"),
    DISCO("Disco"),
    SKA("Ska"),
    BLUEGRASS("Bluegrass"),
    NEW_AGE("New Age");

    private final String name;

    MusicGenre(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}