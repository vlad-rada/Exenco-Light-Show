package net.exenco.lightshow.util.api;

//import net.exenco.lightshow.util.PacketHandler;
//import net.minecraft.world.entity.EntityType;
////import net.minecraft.world.entity.animal.Squid;
////import net.minecraft.world.entity.monster.Guardian;
//import net.minecraft.world.scores.Scoreboard;
//import net.minecraft.world.scores.PlayerTeam;
//import net.minecraft.world.scores.Team;
//import org.bukkit.util.Vector;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Collection;
//import java.util.UUID;

//public class GuardianBeamApi {
//    private static int teamId = 2000000;
//
//    private final Vector start;
//    private boolean spawned;
//    private Vector destination;
//    private Guardian entityGuardian;
//
//    private Squid entitySquid;
//
//    private final PacketHandler packetHandler;
//    public GuardianBeamApi(Vector location, PacketHandler packetHandler) {
//        this.start = location;
//        this.packetHandler = packetHandler;
//    }
//
//    public void setDestination(Vector destination) {
//        if(this.destination != null && this.destination.equals(destination))
//            return;
//        this.destination = destination;
//        entitySquid.setPos(destination.getX(), destination.getY(), destination.getZ());
//        packetHandler.moveEntity(entitySquid);
//    }
//
//    public void spawn() {
//        if(this.start == null)
//            return;
//        spawned = true;
//        Vector destination = this.destination;
//        if(this.destination == null)
//            destination = start;
//
//        this.entityGuardian = new Guardian(EntityType.GUARDIAN, packetHandler.getLevel());
//        this.entityGuardian.setPos(start.getX(), start.getY(), start.getZ());
//        this.entityGuardian.setInvisible(true);
//        this.entityGuardian.setSilent(true);
//
//        this.entitySquid = new Squid(EntityType.SQUID, packetHandler.getLevel());
//        this.entitySquid.setPos(destination.getX(), destination.getY(), destination.getZ());
//        this.entitySquid.setInvisible(true);
//        this.entitySquid.setSilent(true);
//
//        //deprecated
//        //setGuardianTarget(entityGuardian, entitySquid.getId());
//
//        this.entityGuardian.setActiveAttackTarget(this.entitySquid.getId());
//        packetHandler.spawnEntity(entitySquid);
//        packetHandler.spawnEntity(entityGuardian);
//
//        UUID[] uuids = new UUID[] {entityGuardian.getUUID(), entitySquid.getUUID()};
//        PlayerTeam scoreboardTeam = registerNewTeam("noClip" + teamId++, uuids);
//        packetHandler.createTeam(scoreboardTeam);
//    }
//
//    public void destroy() {
//        spawned = false;
//        packetHandler.destroyEntity(entityGuardian.getId());
//        packetHandler.destroyEntity(entitySquid.getId());
//    }
//
//    public void callColorChange() {
//        if(this.entityGuardian == null)
//            return;
//        packetHandler.updateEntity(entityGuardian);
//    }
//
////    private void setGuardianTarget(Guardian entityGuardian, int entityId) {
////        try {
////            Method setSpikes = entityGuardian.getClass().getDeclaredMethod("w", boolean.class);
////            Method setAttackId = entityGuardian.getClass().getDeclaredMethod("b", int.class);
////            setSpikes.setAccessible(true);
////            setAttackId.setAccessible(true);
////            setSpikes.invoke(entityGuardian, false);
////            setAttackId.invoke(entityGuardian, entityId);
////        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
////            e.printStackTrace();
////        }
////    }
//
//    private PlayerTeam registerNewTeam(String teamName, UUID[] uuids) {
//        PlayerTeam scoreboardTeam = new PlayerTeam(new Scoreboard(), teamName);
//        scoreboardTeam.setCollisionRule(Team.CollisionRule.NEVER);
//        Collection<String> entries = scoreboardTeam.getPlayers();
//        for(UUID uuid : uuids) {
//            entries.add(uuid.toString());
//        }
//        return scoreboardTeam;
//    }
//
//    public boolean isSpawned() {
//        return spawned;
//    }
//}

// new code avoids NMS

import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.util.ProximitySensor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class GuardianBeamApi {
    private final LightShow plugin;
    private final ProximitySensor proximity;
    private Guardian guardian;
    private Squid squid;
    private int beamKeeperTaskId = -1;

    public GuardianBeamApi(LightShow plugin, ProximitySensor proximity) {
        this.plugin = plugin;
        this.proximity = proximity;
    }


    public void spawnBeam(Location start, Location end) {
        // ensure main thread
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> spawnBeam(start, end));
            return;
        }

        plugin.getLogger().info("spawnBeam() called – start=" + start + ", end=" + end);

        World world = start.getWorld();
        if (world == null) return;

        // remove old entities
        if (guardian != null) guardian.remove();
        if (squid != null) squid.remove();

        // spawn guardian centered on block
        Location eye = start.clone().add(0.5, -0.5, 0.5);
        guardian = (Guardian) world.spawnEntity(eye, EntityType.GUARDIAN);
        guardian.setAI(false);
        guardian.setGravity(false);
        guardian.teleport(eye, TeleportCause.PLUGIN);
        guardian.setInvulnerable(true);
        guardian.setSilent(true);
        guardian.setInvisible(true);
        guardian.setCollidable(false);

        // spawn squid at end-point
        Location targetLoc = end.clone().add(0.5, 0.5, 0.5);
        squid = (Squid) world.spawnEntity(targetLoc, EntityType.SQUID);
        squid.setInvulnerable(true);
        squid.setSilent(true);
        squid.setInvisible(true);
        squid.setGravity(false);
        squid.setCollidable(false);

        // hide from all, then show only to proximate players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hideEntity(plugin, guardian);
            p.hideEntity(plugin, squid);
        }
        for (Player p : proximity.getPlayerList()) {
            p.showEntity(plugin, guardian);
            p.showEntity(plugin, squid);
        }

        // initialize beam targeting
        guardian.setTarget(squid);
        guardian.setLaser(true);

        // schedule keep-alive to refresh beam and laser ticks
        if (beamKeeperTaskId != -1) {
            Bukkit.getScheduler().cancelTask(beamKeeperTaskId);
        }
        beamKeeperTaskId = Bukkit.getScheduler()
                .runTaskTimer(plugin, () -> {
                    if (guardian != null && squid != null) {
                        guardian.setTarget(squid);
                        guardian.setLaser(true);
                        guardian.setLaserTicks(guardian.getLaserDuration() / 2);
                    }
                }, 0L, 1L)
                .getTaskId();
    }

    public void moveEndpoint(Location newEnd) {
        if (squid != null && guardian != null) {
            Location sq = newEnd.clone().add(0.5, 0.5, 0.5);
            squid.teleport(sq, TeleportCause.PLUGIN);
            guardian.setTarget(squid);
        }
    }

    public void updateRotation(float yaw, float pitch) {
        if (guardian == null) return;
        Location loc = guardian.getLocation();
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        guardian.teleport(loc, TeleportCause.PLUGIN);
    }


    public void destroyBeam() {
        if (guardian != null) {
            guardian.remove();
            guardian = null;
        }
        if (squid != null) {
            squid.remove();
            squid = null;
        }
        if (beamKeeperTaskId != -1) {
            Bukkit.getScheduler().cancelTask(beamKeeperTaskId);
            beamKeeperTaskId = -1;
        }
    }
}
