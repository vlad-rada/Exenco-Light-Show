package net.exenco.lightshow.util.api;

import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.util.ProximitySensor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * api for spawning and controlling an end crystal beam
 */
public class EndCrystalApi {
    private final LightShow plugin;
    private final ProximitySensor proximity;
    private final Location start;
    private EnderCrystal crystal;
    private boolean spawned = false;

    /**
     * @param plugin      plugin instance
     * @param proximity   the proximity sensor to control visibility
     * @param start       the starting location of the crystal beam
     */
    public EndCrystalApi(LightShow plugin, ProximitySensor proximity, Location start) {
        this.plugin = plugin;
        this.proximity = proximity;
        this.start = start.clone();
    }

    public void spawn() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (spawned) return;
            World world = start.getWorld();
            crystal = (EnderCrystal) world.spawnEntity(start, EntityType.END_CRYSTAL);
            crystal.setShowingBottom(false);
            crystal.setBeamTarget(start);

            // hide from all players, then show only to those in proximity
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.hideEntity(plugin, crystal);
            }
            for (Player p : proximity.getPlayerList()) {
                p.showEntity(plugin, crystal);
            }

            spawned = true;
        });
    }


    public void setDestination(Location dest) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!spawned || crystal == null) return;
            Location current = crystal.getBeamTarget();
            if (current != null && current.equals(dest)) return;
            crystal.setBeamTarget(dest);
        });
    }


    public void destroy() {
        if (!spawned || crystal == null) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            crystal.remove();
            spawned = false;
            crystal = null;
        });
    }


    public boolean isSpawned() {
        return spawned;
    }
}
