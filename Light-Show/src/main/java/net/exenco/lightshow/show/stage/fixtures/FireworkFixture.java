package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ConfigHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FireworkFixture extends ShowFixture {

    private final World world;
    private boolean fire = true;
    private final HashMap<Integer, List<ItemStack>> fireworksMap = new HashMap<>();

    public FireworkFixture(JsonObject configJson, StageManager stageManager, World world) {
        super(configJson, stageManager);
        ConfigHandler configHandler = stageManager.getConfigHandler();

        File config = new File("plugins/Light-Show/fireworks.json");
        if (!config.exists()) {
            try {
                if (!config.createNewFile())
                    throw new IOException("Could not create fireworks.json!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonArray jsonArray = configHandler.getJsonFromFile(config).getAsJsonArray();
        for (JsonElement elem : jsonArray) {
            JsonObject obj = elem.getAsJsonObject();
            int id = obj.get("Id").getAsInt();

            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("Item", "FIREWORK_ROCKET");
            itemJson.addProperty("Count", 1);
            itemJson.addProperty("Nbt", obj.get("FireworkNbt").getAsString());
            ItemStack rocketStack = ConfigHandler.getItemStackFromJsonObject(itemJson);

            fireworksMap.computeIfAbsent(id, k -> new ArrayList<>())
                    .add(rocketStack);
        }

        this.tickSize = configJson.has("TickSize")
                ? configJson.get("TickSize").getAsInt()
                : 200;

        this.world = world;

    }

    @Override
    public int getDmxSize() {
        return 2;
    }

    @Override
    public void applyState(int[] data) {
        boolean spawn = data[0] > 0;
        if (!spawn) {
            fire = true;
            return;
        }

        // only retrigger on full-on (255) when tick arrives
        if (data[0] == 255 && isTick())
            fire = true;

        if (!fire)
            return;

        fire = false;
        int id = data[1];

        List<ItemStack> rockets = fireworksMap.get(id);
        if (rockets == null) return;

        Location loc = this.location.clone();
        World world = loc.getWorld();
        if (world == null) return;

        for (ItemStack rocketStack : rockets) {
            Firework fw = world.spawn(loc, Firework.class);
            FireworkMeta meta = (FireworkMeta) rocketStack.getItemMeta();
            if (meta != null) {
                fw.setFireworkMeta(meta);
            }
        }
    }
}