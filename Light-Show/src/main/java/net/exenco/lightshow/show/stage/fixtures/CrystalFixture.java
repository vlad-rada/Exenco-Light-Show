package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ProximitySensor;
import net.exenco.lightshow.util.ShowSettings;
import net.exenco.lightshow.util.VectorUtils;
import net.exenco.lightshow.util.api.EndCrystalApi;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class CrystalFixture extends ShowFixture {

    private final double maxDistance;
    private final EndCrystalApi endCrystalApi;
    private final Location startLocation;
    private final ProximitySensor proximity;
    private final World world;

    public CrystalFixture(JsonObject configJson, StageManager stageManager, World world) {
        super(configJson, stageManager);
        LightShow show = stageManager.getLightShow();
        ShowSettings settings = stageManager.getShowSettings();

        this.proximity = stageManager.getProximitySensor();
        this.world = world;

        this.maxDistance = configJson.has("MaxDistance")
                ? configJson.get("MaxDistance").getAsDouble()
                : 100;
        this.startLocation = this.location.clone();

        this.endCrystalApi = new EndCrystalApi(show, stageManager.getProximitySensor(),this.startLocation);
        this.endCrystalApi.spawn();
    }

    @Override
    public int getDmxSize() {
        return 5;
    }

    @Override
    public void applyState(int[] data) {
        double distance = valueOfMax(this.maxDistance, data[0]);
        float pan  = 360f * -((data[1] << 8 | data[2]) & 0xFFFF) / 65535f;
        float tilt = 360f * -((data[3] << 8 | data[4]) & 0xFFFF) / 65535f;

        Vector dir = VectorUtils.getDirectionVector(pan - 90, tilt + 90);
        Location dest;
        if (distance <= 0) {
            // fallback below the crystal
            dest = startLocation.clone().add(0, -2, 0);
        } else {
            Location rawEnd = startLocation.clone().add(dir.multiply(distance));
            RayTraceResult hit = startLocation.getWorld().rayTraceBlocks(
                    startLocation,
                    dir,
                    distance,
                    FluidCollisionMode.NEVER,
                    true
            );
            dest = (hit != null)
                    ? hit.getHitPosition().toLocation(startLocation.getWorld())
                    : rawEnd;
        }

        endCrystalApi.setDestination(dest);
    }
    @Override
    public void shutdown() {
        endCrystalApi.destroy();
    }
}