package net.exenco.lightshow.util;

import com.google.gson.*;
import net.exenco.lightshow.LightShow;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;


public class ConfigHandler {
    private final File config = new File("plugins//Light-Show//config.json");

    private final File directory = new File("plugins//Light-Show");
    private final File dmxDirectory = new File(directory + "//DmxEntries");

    private final LightShow lightShow;
    public ConfigHandler(LightShow lightShow) {
        this.lightShow = lightShow;
        load();
    }

    public void load() {
        createDirectory(directory);
        createDirectory(dmxDirectory);

        if(!config.exists())
            lightShow.saveResource("config.json", false);
    }

    public void createDirectory(File directory) {
        if(!directory.exists())
            if(!directory.mkdir())
                lightShow.getLogger().severe("There has been an error creating directory " + directory);
    }

    public JsonArray getDmxEntriesJson(String fileName) {
        File file = new File(dmxDirectory + "//" + fileName + ".json");
        return Objects.requireNonNull(getJsonFromFile(file)).getAsJsonArray();
    }

    public JsonObject getConfigJson() {
        return Objects.requireNonNull(getJsonFromFile(config)).getAsJsonObject();
    }

    public JsonElement getJsonFromFile(File file) {
        if(!file.exists()) {
            lightShow.getLogger().warning("Requested file " + file + " does not exist!");
            return null;
        }
        try {
            FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
            return JsonParser.parseReader(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Vector translateVector(JsonObject json) {
        if (json == null) return null;
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        return new Vector(x, y, z);
    }

    public static Location translateLocation(JsonObject json) {
        if (json == null) return null;
        World world = json.has("world")
                ? Bukkit.getWorld(json.get("world").getAsString())
                : Bukkit.getWorlds().get(0);
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        float yaw   = json.has("yaw")   ? json.get("yaw").getAsFloat()   : 0f;
        float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0f;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Color translateColor(JsonObject json) {
        int r = json.get("Red").getAsInt();
        int g = json.get("Green").getAsInt();
        int b = json.get("Blue").getAsInt();
        return Color.fromRGB(r, g, b);
    }


    public static ItemStack getItemStackFromJsonObject(JsonObject json) {
        String id    = json.get("Item").getAsString();
        int    count = json.has("Count") ? json.get("Count").getAsInt() : 1;

        Material mat = Material.matchMaterial(id.toUpperCase());
        if (mat == null) {
            throw new IllegalArgumentException("Invalid material: " + id);
        }

        return new ItemStack(mat, count);
    }



    //updated
    public static Material getMaterialFromName(String name) {
        // old:
        // CompoundTag nbtTagCompound = new CompoundTag();
        // nbtTagCompound.putString("id", name.toLowerCase());
        // nbtTagCompound.putInt("Count", 1);
        // nbtTagCompound.putString("tag", "{}");
        // return CraftItemStack.asBukkitCopy(ItemStack.of(nbtTagCompound)).getType();

        Material mat = Material.matchMaterial(name.toUpperCase());
        if (mat == null) {
            throw new IllegalArgumentException("Invalid material name: " + name);
        }
        return mat;
    }
}
