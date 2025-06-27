package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ProximitySensor;
import net.exenco.lightshow.util.ConfigHandler;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;



import java.util.*;

public class BeaconFixture extends ShowFixture {

    private boolean enabled;
    private int red;
    private int green;
    private int blue;
    private final Location[]  blockLocations;
    private final BlockData disabledBlockData;
    private static final BlockData airData = Material.AIR.createBlockData();

    private final ProximitySensor proximity;
    private final World world;
    
    public BeaconFixture(JsonObject jsonObject, StageManager stageManager, World world) {
        super(jsonObject, stageManager);

        this.proximity = stageManager.getProximitySensor();
        this.world = world;


        Material offMat = jsonObject.has("OffBlock")
                ? ConfigHandler.getMaterialFromName(jsonObject.get("OffBlock").getAsString())
                : Material.BLACK_WOOL;
        this.disabledBlockData = offMat.createBlockData();
        int count = jsonObject.has("ColourBlocks") ? jsonObject.get("ColourBlocks").getAsInt() : 7;

        this.blockLocations = new Location[count];
        for (int i = 0; i < count; i++) {
            this.blockLocations[i] = location.clone().add(0, i, 0);
        }
    }

    @Override
    public int getDmxSize() {
        return 4;
    }

    @Override
    public void applyState(int[] data) {
        boolean wantOn   = data[0] > 0;
        int     newRed   = data[1];
        int     newGreen = data[2];
        int     newBlue  = data[3];

        boolean coloursChanged = (newRed != red || newGreen != green || newBlue != blue);
        this.red   = newRed;
        this.green = newGreen;
        this.blue  = newBlue;

        // toggle master on/off
        if (wantOn && !enabled) {
            enabled = true;
            sendChange(location, airData);
        } else if (!wantOn && enabled) {
            enabled = false;
            sendChange(location, disabledBlockData);
        }
        if (!enabled) return;

        // if either just turned on, or colour shifted, rebuild the glass column
        if (coloursChanged) {
            updateGlassList();
        }
    }

    private void sendChange(Location v, BlockData bd) {
        Location loc = v.toLocation(world);
        for (Player p : proximity.getPlayerList()) {
            p.sendBlockChange(loc, bd);
        }
    }

    private void updateGlassList() {
        Map<Location, BlockData> map = new HashMap<>();

        // first picks closest to white
        EnumColour last = getClosest(red, green, blue, -1, -1, -1);
        map.put(blockLocations[0], last.blockData);

        // subsequent pick closest to previous
        for (int i = 1; i < blockLocations.length; i++) {
            last = getClosest(red, green, blue, last.red, last.green, last.blue);
            map.put(blockLocations[i], last.blockData);
        }

        map.forEach(this::sendChange);
    }

    private EnumColour getClosestColour(int red, int green, int blue, int fromRed, int fromGreen, int fromBlue) {
        Vector targetVector = new Vector(red, green, blue);

        double minDistance = (fromRed == -1 ? new Vector(255, 255, 255) : new Vector(fromRed, fromGreen, fromBlue)).distance(targetVector);
        EnumColour bestEnumColor = null;
        for(EnumColour value : EnumColour.values()) {
            int r = value.getRed();
            int g = value.getGreen();
            int b = value.getBlue();

            Vector vector = fromRed == -1 ? new Vector(r, g, b) : new Vector((r + fromRed) / 2, (g + fromGreen) / 2, (b + fromBlue) / 2);
            double distance = vector.distance(targetVector);
            if(bestEnumColor == null || distance < minDistance) {
                minDistance = distance;
                bestEnumColor = value;
            }
        }
        return bestEnumColor;
    }



    private EnumColour getClosest(int tr, int tg, int tb, int pr, int pg, int pb) {
        Vector target = new Vector(tr, tg, tb);
        Vector base   = (pr<0)
                ? new Vector(255,255,255)
                : new Vector(pr, pg, pb);

        double bestDist = base.distance(target);
        EnumColour best = null;

        for (EnumColour c : EnumColour.values()) {
            Vector candidate = (pr<0)
                    ? new Vector(c.red, c.green, c.blue)
                    : new Vector((c.red+pr)/2.0, (c.green+pg)/2.0, (c.blue+pb)/2.0);
            double d = candidate.distance(target);
            if (best == null || d < bestDist) {
                bestDist = d;
                best     = c;
            }
        }
        return best;
    }

    private enum EnumColour {
        WHITE(249, 255, 254, Material.WHITE_STAINED_GLASS.createBlockData()),
        ORANGE(249, 128, 29, Material.ORANGE_STAINED_GLASS.createBlockData()),
        MAGENTA(199, 78, 189, Material.MAGENTA_STAINED_GLASS.createBlockData()),
        LIGHT_BLUE(58, 179, 218, Material.LIGHT_BLUE_STAINED_GLASS.createBlockData()),
        YELLOW(254, 216, 61, Material.YELLOW_STAINED_GLASS.createBlockData()),
        LIME(128, 199, 31, Material.LIME_STAINED_GLASS.createBlockData()),
        PINK(243, 139, 170, Material.PINK_STAINED_GLASS.createBlockData()),
        GRAY(71, 79, 82, Material.GRAY_STAINED_GLASS.createBlockData()),
        LIGHT_GRAY(157, 157, 151, Material.LIGHT_GRAY_STAINED_GLASS.createBlockData()),
        CYAN(22, 156, 156, Material.CYAN_STAINED_GLASS.createBlockData()),
        PURPLE(137, 50, 184, Material.PURPLE_STAINED_GLASS.createBlockData()),
        BLUE(60, 68, 170, Material.BLUE_STAINED_GLASS.createBlockData()),
        BROWN(131, 84, 50, Material.BROWN_STAINED_GLASS.createBlockData()),
        GREEN(94, 124, 22, Material.GREEN_STAINED_GLASS.createBlockData()),
        RED(176, 46, 38, Material.RED_STAINED_GLASS.createBlockData()),
        BLACK(29, 29, 33, Material.BLACK_STAINED_GLASS.createBlockData());

        private final int red;
        private final int green;
        private final int blue;
        private final BlockData blockData;
        EnumColour(int red, int green, int blue, BlockData blockData) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.blockData = blockData;
        }
        public int getRed() {
            return red;
        }
        public int getGreen() {
            return green;
        }
        public int getBlue() {
            return blue;
        }
        public BlockData getBlockData() {
            return blockData;
        }
    }
}