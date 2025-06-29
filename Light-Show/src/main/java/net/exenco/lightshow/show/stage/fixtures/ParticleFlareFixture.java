package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ParticleRegistry;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class ParticleFlareFixture extends ShowFixture {
    private final double maxXOffset;
    private final double maxYOffset;
    private final double maxZOffset;
    private final double maxTime;
    private final float maxSize;

    public ParticleFlareFixture(JsonObject jsonObject, StageManager stageManager, World world) {
        super(jsonObject, stageManager);
        this.maxXOffset = jsonObject.has("MaxXOffset") ? jsonObject.get("MaxXOffset").getAsDouble() : 1;
        this.maxYOffset = jsonObject.has("MaxYOffset") ? jsonObject.get("MaxYOffset").getAsDouble() : 1;
        this.maxZOffset = jsonObject.has("MaxZOffset") ? jsonObject.get("MaxZOffset").getAsDouble() : 1;
        this.maxTime = jsonObject.has("MaxTime") ? jsonObject.get("MaxTime").getAsDouble() : 20;
        this.maxSize = jsonObject.has("MaxSize") ? jsonObject.get("MaxSize").getAsFloat() : 1;
    }

    @Override
    public int getDmxSize() {
        return 8;
    }

    @Override
    public void applyState(int[] data) {
        int count      = asRoundedPercentage(data[0]);
        int particleId = data[1];
        double offset  = valueOf(data[2]);
        double time    = valueOfMax(maxTime, data[3]);
        int red        = data[4];
        int green      = data[5];
        int blue       = data[6];
        float size     = (float) valueOfMax(maxSize, data[7]);

        Particle particle = ParticleRegistry.getById(particleId);
        if (particle == null) {
            particle = ParticleRegistry.getById(0);
        }

        Object particleData = null;
        if (particle.getDataType() == Particle.DustOptions.class) {
            particleData = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);
        }

        double offsetX = maxXOffset * offset;
        double offsetY = maxYOffset * offset;
        double offsetZ = maxZOffset * offset;

        if (isTick() && count > 0) {
            World world = location.getWorld();
            if (world == null) return;
            //center on block
            Location center = location.clone().add(0.5, 0, 0.5);

             world.spawnParticle(
                        particle,
                        center,
                        count,
                        offsetX, offsetY, offsetZ,
                        time,
                        particleData, //is allowed to be null!
                        true //force: try to force despite player distance
                );

        }
    }
}