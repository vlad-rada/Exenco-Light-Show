package net.exenco.lightshow.show.stage;

import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.artnet.ArtNetPacket;
import net.exenco.lightshow.show.artnet.DmxBuffer;
import net.exenco.lightshow.show.artnet.ArtNetReceiver;
import net.exenco.lightshow.show.song.SongManager;
import net.exenco.lightshow.show.stage.fixtures.*;
import net.exenco.lightshow.util.*;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.ProximitySensor;
import org.bukkit.Location;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;
import org.bukkit.World;


public class StageManager {

    /* Fixture types */
    private final HashMap<String, Class<? extends ShowFixture>> fixtureMap = new HashMap<>();

    /* channel mapping */
    private final TreeMap<Integer, HashMap<Integer, ArrayList<ShowFixture>>> dmxMap = new TreeMap<>();

    /* Art-Net */
    private final DmxBuffer dmxBuffer;
    private boolean receiving;
    private ArtNetReceiver artNetReceiver;

    private final LightShow lightShow;
    private final ConfigHandler configHandler;
    private final ShowSettings showSettings;
    private final SongManager songManager;
    private final ProximitySensor proximitySensor;
    private final PacketHandler packetHandler;
    public StageManager(LightShow lightShow, ConfigHandler configHandler, ShowSettings showSettings, SongManager songManager, PacketHandler packetHandler, ProximitySensor proximitySensor) {
        this.lightShow = lightShow;
        this.configHandler = configHandler;
        this.showSettings = showSettings;
        this.songManager = songManager;
        this.dmxBuffer = new DmxBuffer();
        this.packetHandler =  packetHandler;
        this.proximitySensor = proximitySensor;
    }

    //new:
    private static final File SIGN_FILE = new File("plugins/Light-Show/DmxEntries/signs.json");
    private static final Gson GSON = new Gson();

    /*

    v1.2.6 overhauled this file so the plugin accepts either json files from the config
    or you can register fixtures in-game with signs!

     */


    public void load() {
        this.artNetReceiver = new ArtNetReceiver(this, showSettings, this.lightShow);

        dmxMap.clear();
        for (ShowSettings.DmxEntry dmxEntry : showSettings.dmxEntryList()) {
            int universeId = dmxEntry.universe();
            JsonArray jsonArray = configHandler.getDmxEntriesJson(dmxEntry.filename());

            for (JsonElement jsonElement : jsonArray) {
                JsonObject configJson = jsonElement.getAsJsonObject();
                addFixtureFromJson(universeId, dmxEntry.offset(), configJson);
            }
        }
    }


    private void addFixtureFromJson(int universeId, int offset, JsonObject configJson) {
        int id = configJson.get("DmxId").getAsInt() - 1 + offset;
        int universe = universeId - 1;

        if (id < 0 || universeId < 0) {
            throw new IllegalArgumentException("There is no such Dmx-Channel: " + universeId + "-" + id);
        }

        dmxMap.computeIfAbsent(universe, k -> new HashMap<>());
        HashMap<Integer, ArrayList<ShowFixture>> subMap = dmxMap.get(universe);
        String type = configJson.get("DmxType").getAsString();

        if (!fixtureMap.containsKey(type)) {
            lightShow.getLogger().warning("Given Dmx-Type " + type + " is not a valid type.");
            return;
        }

        Class<? extends ShowFixture> clazz = fixtureMap.get(type);
        if (clazz == null) return;

        subMap.computeIfAbsent(id, k -> new ArrayList<>());


        try {
            World world = showSettings.stage().location().getWorld();
            var ctor = clazz.getDeclaredConstructor(
                    JsonObject.class,
                    StageManager.class,
                    World.class
            );
            ShowFixture fixture = ctor.newInstance(
                    configJson,
                    this,
                    world
            );
            subMap.get(id).add(fixture);

        } catch (NoSuchMethodException e) {
            lightShow.getLogger().severe(
                    "Your fixture “" + type + "” needs a constructor " +
                            "(JsonObject, StageManager, World)"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFixtureFromSign(JsonObject configJson, int universeId, int offset) {
        addFixtureFromJson(universeId, offset, configJson);
        saveFixtureToSignsFile(configJson);
    }

    private void saveFixtureToSignsFile(JsonObject fixture) {
        JsonArray existingFixtures = new JsonArray();

        if (SIGN_FILE.exists()) {
            try (FileReader reader = new FileReader(SIGN_FILE, StandardCharsets.UTF_8)) {
                existingFixtures = JsonParser.parseReader(reader).getAsJsonArray();
            } catch (Exception ignored) {}
        }

        existingFixtures.add(fixture);

        try (FileWriter writer = new FileWriter(SIGN_FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(existingFixtures, writer);
        } catch (IOException e) {
            lightShow.getLogger().severe("Failed to save sign fixture: " + e.getMessage());
        }
    }


    public void removeFixtureFromSignsFile(JsonObject fixture) {
        if (!SIGN_FILE.exists()) return;
        JsonArray arr;
        try (FileReader r = new FileReader(SIGN_FILE, StandardCharsets.UTF_8)) {
            arr = JsonParser.parseReader(r).getAsJsonArray();
        } catch (Exception e) {
            lightShow.getLogger().warning("Failed reading sign fixtures for removal: " + e.getMessage());
            return;
        }
        JsonArray updated = new JsonArray();
        for (JsonElement el : arr) {
            if (!el.getAsJsonObject().equals(fixture)) {
                updated.add(el);
            }
        }
        try (FileWriter w = new FileWriter(SIGN_FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(updated, w);
        } catch (IOException e) {
            lightShow.getLogger().severe("Unable to remove sign fixture: " + e.getMessage());
        }
    }

    public JsonObject createFixtureJsonFromLocation(int address, String type, Location location, int dx, int dy, int dz, Float yaw) {
        JsonObject config = new JsonObject();
        config.addProperty("DmxId", address);
        config.addProperty("DmxType", type);

        JsonObject loc = new JsonObject();
        loc.addProperty("x", location.getX() + dx);
        loc.addProperty("y", location.getY() + dy);
        loc.addProperty("z", location.getZ() + dz);
        config.add("Location", loc);

        if (yaw != null) {
            config.addProperty("Yaw", yaw);
        }

        return config;
    }

    public void receiveArtNet(byte[] message) {
        ArtNetPacket packet = ArtNetPacket.valueOf(message);
        if (packet == null) {
            return;
        }
        this.receiving = true;
        dmxBuffer.setDmxData(packet.getUniverseID(), packet.getDmx());
    }

    public void registerFixture(String key, Class<? extends ShowFixture> clazz) {
        fixtureMap.put(key, clazz);
    }

    public void reloadFixtures() {
        dmxMap.clear();
        load();
        lightShow.getLogger().info("Fixtures reloaded.");
    }

    public boolean start() {
        if (artNetReceiver.isRunning()) {
            return false;
        }
        return artNetReceiver.start();
    }

    public boolean stop() {
        if (!artNetReceiver.isRunning()) {
            return false;
        }
        return artNetReceiver.stop();
    }

    public boolean confirmReceiving() {
        int timeout = showSettings.artNet().timeout();
        this.receiving = false;
        long start = System.currentTimeMillis();
        long current = start;
        while (!receiving) {
            Thread.onSpinWait();
            if (current >= start + timeout) {
                break;
            }
            current = System.currentTimeMillis();
        }
        return receiving;
    }

    public void updateFixtures() {
        for (var universeEntry : dmxMap.entrySet()) {
            int universeZeroBased = universeEntry.getKey();
            byte[] full = dmxBuffer.getDmxData(universeZeroBased);

            int universeOneBased = universeZeroBased + 1;

            for (var channelEntry : universeEntry.getValue().entrySet()) {
                int channelZeroBased = channelEntry.getKey();
                int channelOneBased  = channelZeroBased + 1;

                List<ShowFixture> fixtures = channelEntry.getValue();
                if (fixtures.isEmpty()) {
                    // no fixtures bound here; skip
                    continue;
                }

                try {
                    int size = fixtures.get(0).getDmxSize();
                    int[] payload = new int[size];

                    // build payload, guarding against short DMX packets
                    for (int i = 0; i < size; i++) {
                        int idx = channelZeroBased + i;
                        payload[i] = idx < full.length ? (full[idx] & 0xFF) : 0;
                    }

                    // apply to each fixture
                    for (ShowFixture fixture : fixtures) {
                        try {
                            fixture.applyState(payload);
                        } catch (Exception e) {
                            lightShow.getLogger().warning(
                                    "Error applying DMX→fixture on universe " +
                                            universeOneBased + " channel " +
                                            channelOneBased + ": " + e.getMessage()
                            );
                        }
                    }

                } catch (Exception outer) {
                    lightShow.getLogger().warning(
                            "Skipping universe " + universeOneBased +
                                    " channel " + channelOneBased +
                                    " due to: " + outer.getMessage()
                    );
                }
            }
        }
    }


    public ShowSettings getShowSettings() {
        return showSettings;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public SongManager getSongManager() {
        return songManager;
    }


    public LightShow getLightShow() {
        return lightShow;
    }

    public ProximitySensor getProximitySensor() {
        return proximitySensor;
    }
}

