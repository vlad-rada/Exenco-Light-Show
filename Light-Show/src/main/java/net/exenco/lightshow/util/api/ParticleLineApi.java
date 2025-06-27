package net.exenco.lightshow.util.api;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class ParticleLineApi {

    private Location start;
    private Location destination;
    private double maxDistance;

    private Particle particle;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double time = 0;
    private Object data = null;

    public ParticleLineApi(Location start) {
        this.start = start.clone();
    }

    public void play() {
        World world = start.getWorld();
        if (world == null || destination == null) return;

        Vector origin = start.clone().toVector();
        Vector iterator = origin.clone();
        Vector direction = destination.clone().toVector()
                .subtract(origin)
                .normalize()
                .multiply(0.15);

        // spawn until we've gone maxDistance from the start point
        while (iterator.distance(origin) < maxDistance) {
            if (data != null) {
                world.spawnParticle(
                        particle,
                        iterator.getX(), iterator.getY(), iterator.getZ(),
                        1,            // count
                        offsetX, offsetY, offsetZ,
                        time,
                        data
                );
            } else {
                world.spawnParticle(
                        particle,
                        iterator.getX(), iterator.getY(), iterator.getZ(),
                        1,            // count
                        offsetX, offsetY, offsetZ,
                        time
                );
            }
            iterator.add(direction);
        }
    }

    public ParticleLineApi setStart(Location start) {
        this.start = start.clone();
        return this;
    }
    public ParticleLineApi setDestination(Location destination) {
        this.destination = destination.clone();
        return this;
    }
    public ParticleLineApi setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    public ParticleLineApi setParticle(Particle particle) {

        this.particle = particle;
        return this;
    }

    public ParticleLineApi setOffsetX(double offsetX) {

        this.offsetX = offsetX;
        return this;
    }

    public ParticleLineApi setOffsetY(double offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public ParticleLineApi setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
        return this;
    }

    public ParticleLineApi setTime(double time) {
        this.time = time;
        return this;
    }

    public ParticleLineApi setData(Object data) {
        this.data = data;
        return this;
    }
}
