package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.ProximitySensor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 Overhauled to not use NMS
 */

public class BlockChangerFixture extends ShowFixture {
    private final LightShow plugin;
    private final ProximitySensor proximity;
    private final World world;
    private final boolean lit;
    private final BlockData enabledState;
    private final BlockData disabledState;


    private boolean lastState = false;

    public BlockChangerFixture(JsonObject jsonObject, StageManager stageManager, World world) {
        super(jsonObject, stageManager);
        this.plugin = stageManager.getLightShow();
        this.proximity = stageManager.getProximitySensor();
        this.world = world;


        this.lit = !jsonObject.has("Lit") || jsonObject.get("Lit").getAsBoolean();

        Material onMat = jsonObject.has("EnabledState")
                ? ConfigHandler.getMaterialFromName(jsonObject.get("EnabledState").getAsString())
                : Material.REDSTONE_LAMP;
        this.enabledState = onMat.createBlockData();

        Material offMat = jsonObject.has("DisabledState")
                ? ConfigHandler.getMaterialFromName(jsonObject.get("DisabledState").getAsString())
                : Material.REDSTONE_LAMP;
        this.disabledState = offMat.createBlockData();
    }

    @Override
    public int getDmxSize() {
        return 1;
    }

    @Override
    public void applyState(int[] data) {
        boolean enabled = data[0] > 0;
        if (enabled == lastState) return;
        lastState = enabled;

        BlockData update = enabled ? enabledState.clone() : disabledState.clone();
        // apply "lit" if supported
        if (lit && update instanceof Lightable lightable) {
            lightable.setLit(enabled);
        }
        if (update instanceof Levelled levelled) {
            levelled.setLevel(data[0] / 16);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Location loc = location;
            world.setBlockData(loc, update);
            world.getBlockAt(loc).getState().update(true, true);
            for (Player p : proximity.getPlayerList()) {
                p.sendBlockChange(loc, update);
            }
        });
    }
}
