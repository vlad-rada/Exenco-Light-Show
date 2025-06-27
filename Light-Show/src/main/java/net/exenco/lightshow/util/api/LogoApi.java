package net.exenco.lightshow.util.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.ParticleRegistry;
import net.exenco.lightshow.util.VectorUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class LogoApi {
    private final ArrayList<ParticleLineApi>  particleLineApis  = new ArrayList<>();
    private final ArrayList<ParticleFlareApi> particleFlareApis = new ArrayList<>();

    private final double yaw;
    private final double pitch;

    public LogoApi(double yaw, double pitch) {
        this.yaw   = yaw;
        this.pitch = pitch;
    }


    public void setLogo(Location origin, Logo logo, double size) {
        particleFlareApis.clear();
        particleLineApis.clear();

        if (logo == null) return;

        // dots
        for (Logo.ParticleDot dot : logo.getParticleDotList()) {
            // 1) scale + rotate
            Vector v = dot.location().clone().multiply(size);
            v = VectorUtils.getRotatedVector(v, yaw, pitch);

            // 2) translate into world
            Location loc = origin.clone().add(v);

            // 3) configure flare
            Logo.ParticleEntry entry = dot.particleEntry();
            ParticleFlareApi flare = new ParticleFlareApi(loc)
                    .setParticle(entry.particle())
                    .setData(entry.data());
            particleFlareApis.add(flare);
        }

        // lines
        for (Logo.ParticleLine line : logo.getParticleLineList()) {
            // scale & rotate endpoints
            Vector o = line.origin().clone().multiply(size);
            Vector d = line.destination().clone().multiply(size);
            o = VectorUtils.getRotatedVector(o, yaw, pitch);
            d = VectorUtils.getRotatedVector(d, yaw, pitch);

            // translate into world-space Locations
            Location start = origin.clone().add(o);
            Location end   = origin.clone().add(d);

            // build a line renderer
            Logo.ParticleEntry entry = line.particleEntry();
            ParticleLineApi lineApi = new ParticleLineApi(start)
                    .setDestination(end)
                    .setMaxDistance(start.distance(end))
                    .setParticle(entry.particle())
                    .setData(entry.data());
            particleLineApis.add(lineApi);
        }
    }

    public void playLogo() {
        particleFlareApis.forEach(ParticleFlareApi::play);
        particleLineApis.forEach(ParticleLineApi::play);
    }

    //  json parsing remains unchanged

    public static class Logo {
        private final ArrayList<ParticleDot>  particleDotList;
        private final ArrayList<ParticleLine> particleLineList;

        public Logo(JsonObject jsonObject) {
            this.particleDotList  = new ArrayList<>();
            this.particleLineList = new ArrayList<>();

            JsonArray lineArray = jsonObject.getAsJsonArray("ParticleLines");
            for (JsonElement e : lineArray)
                particleLineList.add(ParticleLine.valueOf(e.getAsJsonObject()));

            JsonArray dotArray = jsonObject.getAsJsonArray("ParticleDots");
            for (JsonElement e : dotArray)
                particleDotList.add(ParticleDot.valueOf(e.getAsJsonObject()));
        }

        private record ParticleDot(ParticleEntry particleEntry, Vector location) {
            private static ParticleDot valueOf(JsonObject o) {
                Particle p = ParticleRegistry
                        .valueOf(o.get("Particle").getAsString())
                        .getBukkitParticle();

                Particle.DustOptions data = null;
                if (p.getDataType() == Particle.DustOptions.class)
                    data = ParticleEntry.dataValueOf(o.getAsJsonObject("Data"));

                Vector loc = ConfigHandler.translateVector(o.getAsJsonObject("Location"));
                return new ParticleDot(new ParticleEntry(p, data), loc);
            }
        }

        private record ParticleLine(ParticleEntry particleEntry, Vector origin, Vector destination) {
            private static ParticleLine valueOf(JsonObject o) {
                Particle p = ParticleRegistry
                        .valueOf(o.get("Particle").getAsString())
                        .getBukkitParticle();

                Particle.DustOptions data = null;
                if (p.getDataType() == Particle.DustOptions.class)
                    data = ParticleEntry.dataValueOf(o.getAsJsonObject("Data"));

                Vector orig = ConfigHandler.translateVector(o.getAsJsonObject("Origin"));
                Vector dest = ConfigHandler.translateVector(o.getAsJsonObject("Destination"));
                return new ParticleLine(new ParticleEntry(p, data), orig, dest);
            }
        }

        private record ParticleEntry(Particle particle, Particle.DustOptions data) {
            public static Particle.DustOptions dataValueOf(JsonObject o) {
                Color color = ConfigHandler.translateColor(o);
                float size  = o.get("Size").getAsFloat();
                return new Particle.DustOptions(color, size);
            }
        }

        private ArrayList<ParticleDot>  getParticleDotList()  { return particleDotList; }
        private ArrayList<ParticleLine> getParticleLineList() { return particleLineList; }
    }
}
