package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.VectorUtils;
import net.exenco.lightshow.util.ParticleRegistry;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class FogMachineFixture extends ShowFixture {
    private final double maxOffset;
    private final double maxVelocity;
    private final Vector direction;
    private final World world;

    public FogMachineFixture(JsonObject configJson, StageManager stageManager, World world) {
        super(configJson, stageManager);
        this.world = world;

        this.tickSize = configJson.has("TickSize") ? configJson.get("TickSize").getAsInt() : 50;
        this.maxOffset = configJson.has("MaxOffset") ? configJson.get("MaxOffset").getAsDouble() : 0.5;
        this.maxVelocity = configJson.has("MaxVelocity") ? configJson.get("MaxVelocity").getAsDouble() : 2;
        double yaw = configJson.has("Yaw") ? configJson.get("Yaw").getAsDouble() : 0;
        double pitch = configJson.has("Pitch") ? configJson.get("Pitch").getAsDouble() : 0;

        this.direction = VectorUtils.getDirectionVector(-yaw, -pitch + 90).normalize();
    }

    @Override
    public int getDmxSize() {
        return 4;
    }

    @Override
    public void applyState(int[] data) {
        System.out.println("FogMachine.applyState: DMX="+ Arrays.toString(data));
        double velocity   = valueOfMax(maxVelocity, data[0]);
        int    particleId = data[1];
        int    count      = data[2];
        double offset     = valueOfMax(maxOffset, data[3]);

        Particle particle = ParticleRegistry.getById(particleId);
        if (particle == null
                || particle.getDataType() != Void.class
                || velocity == 0.0
                || count <= 0
                || !isTick()) {
            return;
        }

        World world = location.getWorld();
        if (world == null) return;

        for (int i = 0; i < count; i++) {
            // random distance along main direction
            double d       = ThreadLocalRandom.current().nextDouble(offset);
            double small   = offset * 0.1;

            // compute spawn point
            Location spawn = location.clone()
                    .add(direction.clone().multiply(d))    // along the tube
                    .add(getRandomVector(offset));            // jitter

            // compute slight drift
            Vector drift = direction.clone()
                    .add(getRandomVector(small));

            // Paper API call
            world.spawnParticle(
                    particle,
                    spawn,
                    /*count=*/1,
                    /*offsetX=*/drift.getX(),
                    /*offsetY=*/drift.getY(),
                    /*offsetZ=*/drift.getZ(),
                    /*speed=*/velocity
            );
        }
    }

    private Vector getRandomVector(double max) {
        return new Vector(getRandomDouble(max*2) - max, getRandomDouble(max*2) - max, getRandomDouble(max*2) - max);
    }

    private double getRandomDouble(double max) {
        if(max == 0)
            return 0;
        return ThreadLocalRandom.current().nextDouble(max);
    }
}
