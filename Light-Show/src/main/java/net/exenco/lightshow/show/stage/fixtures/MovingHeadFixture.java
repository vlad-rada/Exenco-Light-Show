package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ProximitySensor;
import net.exenco.lightshow.util.ShowSettings;
import net.exenco.lightshow.util.VectorUtils;
import net.exenco.lightshow.util.api.GuardianBeamApi;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;


import java.util.UUID;

public class MovingHeadFixture extends ShowFixture {

    private final World world;
    //dmx state
    private int state;
    private float yaw;
    private float pitch;

    //armor stands
    private ArmorStand headStand, lightStand;


    //beam api
    private final GuardianBeamApi guardianBeamApi;
    private final LightShow show;
    private final ProximitySensor proximity;
    private final NamespacedKey marker;

    //beam positions
    private final Location startLocation;
    private final double maxDistance;

    //textures
    private final String offTexture;
    private final String lowTexture;
    private final String mediumTexture;
    private final String highTexture;

    //bool
    private boolean beamOn = false;


    public MovingHeadFixture(JsonObject jsonObject, StageManager stageManager, World world) {
        super(jsonObject, stageManager);
        this.world = world;
        this.show = stageManager.getLightShow();
        this.marker = new NamespacedKey(show, "moving_head_fixture");
        this.proximity = stageManager.getProximitySensor();
        this.guardianBeamApi = new GuardianBeamApi(stageManager.getLightShow(), stageManager.getProximitySensor());
        ShowSettings showSettings = stageManager.getShowSettings();

        //textures
        this.offTexture = showSettings.showEffects().movingLight().offTexture();
        this.lowTexture = showSettings.showEffects().movingLight().lowTexture();
        this.mediumTexture = showSettings.showEffects().movingLight().mediumTexture();
        this.highTexture = showSettings.showEffects().movingLight().highTexture();

        //pos
        this.startLocation = this.location.clone();
        this.maxDistance = jsonObject.has("MaxDistance") ? jsonObject.get("MaxDistance").getAsDouble() : 100;

        //armor stands
        spawnHeadArmorStand(offTexture);
        spawnLightArmorStand();
        this.state = 0;


    }


    private ItemStack makeSkull(String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            PlayerProfile prof = Bukkit.createProfile(UUID.randomUUID());
            prof.setProperty(new ProfileProperty("textures", base64));
            meta.setOwnerProfile(prof);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private void lookAt(float yaw, float pitch) {
        double xr = Math.toRadians(pitch), yr = Math.toRadians(yaw);
        headStand.setHeadPose(new EulerAngle(xr, yr, 0));
        lightStand.setHeadPose(new EulerAngle(xr, yr, 0));
    }

    private void spawnHeadArmorStand(String texture) {
        Location at = startLocation.clone().subtract(0.5, 1.5, 0.5);

        for (Entity e : world.getNearbyEntities(at, 0.5, 0.5, 0.5)) {
            if (e instanceof ArmorStand as
                    && as.getPersistentDataContainer().has(marker, PersistentDataType.BYTE)) {
                headStand = as;
                updateHeadArmorStand(texture);
                return;
            }
        }

        headStand = (ArmorStand) world.spawnEntity(at, EntityType.ARMOR_STAND);
        headStand.setGravity(false);
        headStand.setVisible(false);
        headStand.setInvulnerable(true);
        headStand.setHelmet(makeSkull(texture));

        headStand.getPersistentDataContainer()
                .set(marker, PersistentDataType.BYTE, (byte)1);

        // 4) hide from all players, then show only to your proximity list
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hideEntity(show, headStand);
        }
        for (Player p : proximity.getPlayerList()) {
            p.showEntity(show, headStand);
        }
    }

    private void updateHeadArmorStand(String texture) {
        headStand.setHelmet(makeSkull(texture));
    }

    private void spawnLightArmorStand() {
        Location at = startLocation.clone().subtract(0.5, 0.775, 0.5);
        lightStand = (ArmorStand) at.getWorld().spawnEntity(at, EntityType.ARMOR_STAND);
        lightStand.setGravity(false);
        lightStand.setVisible(false);
        lightStand.setSmall(true);
        lightStand.setHelmet(new ItemStack(Material.AIR));

        Bukkit.getOnlinePlayers().forEach(p -> p.hideEntity(show, lightStand));
        proximity.getPlayerList().forEach(p -> p.showEntity(show, lightStand));
    }

    private void updateLightArmorStand(Material mat) {
        lightStand.setHelmet(new ItemStack(mat));
    }

    @Override
    public void shutdown() {
        if (beamOn) {
            guardianBeamApi.destroyBeam();
            beamOn = false;
        }
    }

    @Override
    public int getDmxSize() {
        return 7;
    }

    @Override
    public void applyState(int[] data) {

        int dim = asRoundedPercentage(data[0]);
        float pan  = 360f * -(((data[1] << 8 | data[2]) & 0xFFFF) / 65535f);
        float tilt = 360f * -(((data[3] << 8 | data[4]) & 0xFFFF) / 65535f);
        double distance = valueOfMax(maxDistance, data[5]);
        boolean colourChange = data[6] > 0;


        int newState = (dim > 66 ? 3 : dim > 33 ? 2 : dim > 0 ? 1 : 0);
        if (newState != state) {
            state = newState;
            switch (state) {
                case 3 -> {
                    updateHeadArmorStand(highTexture);
                    updateLightArmorStand(Material.TORCH);
                }
                case 2 -> {
                    updateHeadArmorStand(mediumTexture);
                    updateLightArmorStand(Material.SOUL_TORCH);
                }
                case 1 -> {
                    updateHeadArmorStand(lowTexture);
                    updateLightArmorStand(Material.REDSTONE_TORCH);
                }
                default -> {
                    updateHeadArmorStand(offTexture);
                    updateLightArmorStand(Material.AIR);
                }
            }
        }

        if (pan != yaw || tilt != pitch) {
            yaw = pan;
            pitch = tilt;
            lookAt(yaw, tilt);
        }

        guardianBeamApi.updateRotation(yaw, pitch);

        Vector dir = VectorUtils.getDirectionVector(yaw - 90, tilt + 90);
        Location rawEnd = startLocation.clone().add(dir.multiply(distance));

        Location endLoc;
        if (distance <= 0 || dir.lengthSquared() < 1e-6) {
            endLoc = rawEnd;
        } else {
            // normalize direction and ray-trace up to `distance`
            Vector unit = dir.clone().normalize();
            RayTraceResult hit = world.rayTraceBlocks(
                    startLocation,
                    unit,
                    distance,
                    FluidCollisionMode.NEVER,
                    true
            );
            endLoc = (hit != null)
                    ? hit.getHitPosition().toLocation(world)
                    : rawEnd;
        }

        // beam on/off logic
        boolean wantBeam = dim > 0 && distance > 0;
        if (wantBeam) {
            if (!beamOn) {
                guardianBeamApi.spawnBeam(startLocation, endLoc);
                beamOn = true;
            } else {
                guardianBeamApi.moveEndpoint(endLoc);
            }
        } else if (beamOn) {
            guardianBeamApi.destroyBeam();
            beamOn = false;
        }
    }



}

