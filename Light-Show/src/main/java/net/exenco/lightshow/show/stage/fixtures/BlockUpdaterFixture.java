package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.ProximitySensor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;

public class BlockUpdaterFixture extends ShowFixture {
    private final boolean lit;
    private final BlockData enabledState;
    private final BlockData disabledState;

    //new
    private final ProximitySensor proximity;
    private final World world;
    private boolean lastState;


    public BlockUpdaterFixture(JsonObject jsonObject, StageManager stageManager, World world) {
        super(jsonObject, stageManager);
        this.world = world;

        this.proximity = stageManager.getProximitySensor();

        this.lit = !jsonObject.has("Lit") || jsonObject.get("Lit").getAsBoolean();
        Material enabledMaterial = jsonObject.has("EnabledState")
                ? ConfigHandler.getMaterialFromName(jsonObject.get("EnabledState").getAsString())
                : Material.REDSTONE_LAMP;
        this.enabledState = enabledMaterial.createBlockData();
        Material disabledMaterial = jsonObject.has("DisabledState")
                ? ConfigHandler.getMaterialFromName(jsonObject.get("DisabledState").getAsString())
                : Material.REDSTONE_LAMP;
        this.disabledState = disabledMaterial.createBlockData();
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

        BlockData updateBlockData = enabled ? enabledState : disabledState;
        if (lit && updateBlockData instanceof Lightable lightable) {
            lightable.setLit(enabled);
        }
        if (updateBlockData instanceof Levelled levelled) {
            levelled.setLevel(data[0] / 16);
        }

        Location loc = location.clone().toLocation(world);
        for (Player p : proximity.getPlayerList()) {
            p.sendBlockChange(loc, updateBlockData);
        }
    }
}
