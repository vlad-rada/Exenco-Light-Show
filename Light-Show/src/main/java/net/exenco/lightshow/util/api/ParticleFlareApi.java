package net.exenco.lightshow.util.api;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;


public class ParticleFlareApi {
    private Location location;
    private Particle particle;
    private int count = 1;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double time = 0;
    private Object data = null;  // blockData, dust options, item stack, etc.

    public ParticleFlareApi(Location location) {
        this.location = location;
    }

    public void play() {
        World world = location.getWorld();
        if (world == null) return;

        if (data != null) {
            world.spawnParticle(particle,
                    location,
                    count,
                    offsetX, offsetY, offsetZ,
                    time,
                    data);
        } else {
            world.spawnParticle(particle,
                    location,
                    count,
                    offsetX, offsetY, offsetZ,
                    time);
        }
    }

    public ParticleFlareApi setLocation(Location location) {
        this.location = location;
        return this;
    }

    public ParticleFlareApi setParticle(Particle particle) {
        this.particle = particle;
        return this;
    }

    public ParticleFlareApi setCount(int count) {
        this.count = count;
        return this;
    }

    public ParticleFlareApi setOffsetX(double offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    public ParticleFlareApi setOffsetY(double offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public ParticleFlareApi setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
        return this;
    }

    public ParticleFlareApi setTime(double time) {
        this.time = time;
        return this;
    }

    public ParticleFlareApi setData(Object data) {
        this.data = data;
        return this;
    }
}
