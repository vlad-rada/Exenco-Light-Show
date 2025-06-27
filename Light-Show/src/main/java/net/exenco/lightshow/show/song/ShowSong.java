package net.exenco.lightshow.show.song;

import com.google.gson.JsonObject;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.NamespacedKey;

public class ShowSong {
    private final int id;
    private final String title;
    private final String artist;
    private final String album;
    private final int year;
    private final String description;
    private final long duration;
    private final Sound sound;
    private final SoundCategory soundCategory;
    private final float volume;
    private final float pitch;

    public ShowSong(int id, JsonObject songJson) {
        this.id = id;
        this.title = songJson.get("Title").getAsString();
        this.artist = songJson.get("Artist").getAsString();
        this.album = songJson.get("Album").getAsString();
        this.year = songJson.get("Year").getAsInt();
        this.description = songJson.has("Description") ? songJson.get("Description").getAsString() : "";
        this.duration = songJson.get("Duration").getAsLong();
        NamespacedKey key = NamespacedKey.fromString(songJson.get("Sound").getAsString());
        assert key != null;
        Sound s = Registry.SOUNDS.get(key);
        if (s == null) {
            throw new IllegalArgumentException("Unknown sound: " + key);
        }
        this.sound = s;
        this.soundCategory = songJson.has("SoundCategory") ? SoundCategory.valueOf(songJson.get("SoundCategory").getAsString().toUpperCase()) : SoundCategory.RECORDS;
        this.volume = songJson.has("Volume")
                ? songJson.get("Volume").getAsFloat()
                : 1.0f;

        this.pitch  = songJson.has("Pitch")
                ? songJson.get("Pitch").getAsFloat()
                : 1.0f;

    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public int getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }

    public long getDuration() {
        return duration;
    }

    public Sound getSound() {
        return sound;
    }

    public SoundCategory getSoundCategory() {
        return soundCategory;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;

    }
}
