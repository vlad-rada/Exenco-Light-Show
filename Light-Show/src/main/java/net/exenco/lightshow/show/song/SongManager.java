package net.exenco.lightshow.show.song;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.SoundCategory;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class SongManager {
    private final ConfigHandler configHandler;
    private final ShowSettings showSettings;
    private final PacketHandler packetHandler;
    private final HashMap<Integer, ShowSong> songList = new HashMap<>();
    private ShowSong currentSong;
    public SongManager(ConfigHandler configHandler, ShowSettings showSettings, PacketHandler packetHandler) {
        this.configHandler = configHandler;
        this.showSettings = showSettings;
        this.packetHandler = packetHandler;
        loadSongs();
    }

    public void loadSongs() {
        songList.clear();

        File directory = new File("plugins//Light-Show//Songs");
        configHandler.createDirectory(directory);

        for(File file : Objects.requireNonNull(directory.listFiles())) {
            if(!file.getName().endsWith(".json"))
                continue;
            try {
                JsonObject jsonObject = configHandler.getJsonFromFile(file).getAsJsonObject();
                int id = jsonObject.get("Id").getAsInt();
                ShowSong showSong = new ShowSong(id, jsonObject);
                songList.put(id, showSong);
            } catch(Exception ignored) {}
        }
    }

    /** Play the song with the given ID at the stage’s location. */
    public void play(int id) {
        ShowSong song = songList.get(id);
        if (song == null) return;
        this.currentSong = song;

        Vector loc = showSettings
                .stage()
                .location()
                .toVector();

        // now uses Sound + volume + pitch
        packetHandler.playSound(
                loc,
                song.getSound(),   // org.bukkit.Sound
                song.getVolume(),  // float, e.g. 1.0f
                song.getPitch()    // float, e.g. 1.0f
        );
    }

    /** Stop whatever is currently playing. */
    public void stop() {
        if (currentSong == null) return;
        packetHandler.stopSound(currentSong.getSound());
        this.currentSong = null;
    }

    public ShowSong getCurrentSong() {
        return currentSong;
    }
}
