package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.ParticleRegistry;
import org.bukkit.Color;
import org.bukkit.Particle;

public class ParticleFlareFixture extends ShowFixture {
    private final double maxXOffset;
    private final double maxYOffset;
    private final double maxZOffset;
    private final double maxTime;
    private final float maxSize;

    private final PacketHandler packetHandler;
    public ParticleFlareFixture(JsonObject jsonObject, StageManager stageManager) {
        super(jsonObject, stageManager);
        this.packetHandler = stageManager.getPacketHandler();

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
        int count = asRoundedPercentage(data[0]);
        int particleId = data[1];
        double offset = valueOf(data[2]);
        double time = valueOfMax(maxTime, data[3]);
        int red = data[4];
        int green = data[5];
        int blue = data[6];
        float size = (float) valueOfMax(maxSize, data[7]);

        Particle particle = ParticleRegistry.getById(particleId);
        if (particle == null) {
            System.out.println("[ParticleFlareFixture] Invalid particle ID: " + particleId);
        return;
            }
        Object particleData = null;
        if(particle.getDataType() == Particle.DustOptions.class)
            particleData = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);

        double offsetX = maxXOffset * offset;
        double offsetY = maxYOffset * offset;
        double offsetZ = maxZOffset * offset;

        System.out.println("[ParticleFlareFixture] Applying state:");
        System.out.println("  Location: " + location);
        System.out.println("  Particle: " + particle + " (" + particle.getDataType() + ")");
        System.out.println("  Count: " + count);
        System.out.println("  Offset: " + offset + " (X=" + offsetX + ", Y=" + offsetY + ", Z=" + offsetZ + ")");
        System.out.println("  Time: " + time);
        System.out.println("  RGB: " + red + ", " + green + ", " + blue);
        System.out.println("  Size: " + size);
        System.out.println("  Is tick: " + isTick());

        if(isTick() && count > 0) {
            packetHandler.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, time, particleData);
        }
    }
}
