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
            //Setting default particle instead of silently failing
            particle = ParticleRegistry.getById(0);

            //removed:
            //return
            }
        Object particleData = null;
        if(particle.getDataType() == Particle.DustOptions.class)
            particleData = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);

        double offsetX = maxXOffset * offset;
        double offsetY = maxYOffset * offset;
        double offsetZ = maxZOffset * offset;

        //debug
//        System.out.println(
//                "[ParticleFlareFixture] Applying state:\n" +
//                        "  Location: " + location + "\n" +
//                        "  Particle: " + particle + " (" + particle.getDataType() + ")\n" +
//                        "  Count: " + count + "\n" +
//                        "  Offset: " + offset + " (X=" + offsetX + ", Y=" + offsetY + ", Z=" + offsetZ + ")\n" +
//                        "  Time: " + time + "\n" +
//                        "  RGB: " + red + ", " + green + ", " + blue + "\n" +
//                        "  Size: " + size + "\n" +
//                        "  Is tick: " + isTick()
//        );


        if(isTick() && count > 0) {
            packetHandler.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, time, particleData);
        }
    }
}
