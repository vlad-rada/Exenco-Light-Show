package net.exenco.lightshow.util;

import com.google.gson.*;
import com.mojang.serialization.DataResult;
import net.exenco.lightshow.LightShow;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

//new
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.component.DataComponentPatch;


import net.minecraft.world.item.Items;
import org.bukkit.*;
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

    public static Vector translateVector(JsonObject jsonObject) {
        if(jsonObject == null)
            return null;

        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();
        return new Vector(x, y, z);
    }

    public static Location translateLocation(JsonObject jsonObject) {
        if(jsonObject == null)
            return null;

        World world = jsonObject.has("world") ? Bukkit.getWorld(jsonObject.get("world").getAsString()) : null;
        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();
        float yaw = jsonObject.has("yaw") ? jsonObject.get("yaw").getAsFloat() : 0.0F;
        float pitch = jsonObject.has("pitch") ? jsonObject.get("pitch").getAsFloat() : 0.0F;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Color translateColor(JsonObject jsonObject) {
        int red = jsonObject.get("Red").getAsInt();
        int green = jsonObject.get("Green").getAsInt();
        int blue = jsonObject.get("Blue").getAsInt();
        return Color.fromRGB(red, green, blue);
    }


    
    //updated june 22 2025 for 1.21.6
    public static ItemStack getItemStackFromJsonObject(JsonObject jsonObject) {
        String itemId = jsonObject.get("Item").getAsString();
        int count = jsonObject.has("Count") ? jsonObject.get("Count").getAsInt() : 1;
        String nbt = jsonObject.has("Nbt") ? jsonObject.get("Nbt").getAsString() : "{}";

        try {
            ResourceLocation itemKey = ResourceLocation.parse(itemId.toLowerCase());
            Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(itemKey);
            if (opt.isEmpty() || opt.get() == Items.AIR) {
                throw new IllegalArgumentException("Unknown or AIR item: " + itemId);
            }
            ItemStack stack = new ItemStack(opt.get(), count);

            CompoundTag itemNbt = TagParser.parseCompoundFully(nbt);
            if (!itemNbt.isEmpty()) {
                DataResult<DataComponentPatch> result = DataComponentPatch.CODEC.parse(NbtOps.INSTANCE, itemNbt);
                DataComponentPatch patch = result.result().orElseThrow(() ->
                        new RuntimeException("Failed to parse patch: " + result.error().map(DataResult.Error::message).orElse("Unknown")));
                stack.applyComponents(patch);
            }
            return stack;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException("Cannot parse item " + count + "x " + itemId + " with NBT: " + nbt, e);
        }
    }




    //updated
    public static Material getMaterialFromName(String name) {
        // old:
        // CompoundTag nbtTagCompound = new CompoundTag();
        // nbtTagCompound.putString("id", name.toLowerCase());
        // nbtTagCompound.putInt("Count", 1);
        // nbtTagCompound.putString("tag", "{}");
        // return CraftItemStack.asBukkitCopy(ItemStack.of(nbtTagCompound)).getType();

        Material mat = Material.matchMaterial(name.toLowerCase());
        if (mat == null) {
            throw new IllegalArgumentException("Invalid material name: " + name);
        }
        return mat;
    }
}
