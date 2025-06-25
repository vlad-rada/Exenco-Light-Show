package net.exenco.lightshow.util;

import org.bukkit.Particle;

public enum ParticleRegistry {
    //most particles have changed names -- they have been updated though there may be mistakes.
    EXPLOSION_NORMAL(0, Particle.POOF), 
    EXPLOSION_LARGE(1, Particle.EXPLOSION), 
    EXPLOSION_HUGE(2, Particle.EXPLOSION_EMITTER), 
    FIREWORKS_SPARK(3, Particle.FIREWORK), 
    WATER_BUBBLE(4, Particle.BUBBLE), 
    WATER_SPLASH(5, Particle.SPLASH), 
    WATER_WAKE(6, Particle.FISHING), 
    SUSPENDED(7, Particle.UNDERWATER), 
    SUSPENDED_DEPTH(8, Particle.UNDERWATER), //unclear what new version is
    CRIT(9, Particle.CRIT),
    CRIT_MAGIC(10, Particle.ENCHANTED_HIT),
    SMOKE_NORMAL(11, Particle.SMOKE),
    SMOKE_LARGE(12, Particle.LARGE_SMOKE),
    SPELL(13, Particle.EFFECT),
    SPELL_INSTANT(14, Particle.INSTANT_EFFECT),
    SPELL_MOB(15, Particle.ENTITY_EFFECT),
    SPELL_MOB_AMBIENT(16, Particle.ENTITY_EFFECT), //unclear what new version is
    SPELL_WITCH(17, Particle.WITCH),
    DRIP_WATER(18, Particle.DRIPPING_WATER),
    DRIP_LAVA(19, Particle.DRIPPING_LAVA),
    VILLAGER_ANGRY(20, Particle.ANGRY_VILLAGER),
    VILLAGER_HAPPY(21, Particle.HAPPY_VILLAGER),
    TOWN_AURA(21, Particle.MYCELIUM), //double check for correctness
    NOTE(22, Particle.NOTE),
    PORTAL(23, Particle.PORTAL),
    ENCHANTMENT_TABLE(24, Particle.ENCHANT),
    FLAME(25, Particle.FLAME),
    LAVA(26, Particle.LAVA),
    CLOUD(27, Particle.CLOUD),
     //new particle here: dust?
    SNOWBALL(28, Particle.ITEM_SNOWBALL),
    SNOW_SHOVEL(29, Particle.DUST), //using dust here -- unsure if correct
    SLIME(30, Particle.ITEM_SLIME),
    HEART(31, Particle.HEART),
    WATER_DROP(33, Particle.DRIPPING_WATER), //replacing with dripping water?
    MOB_APPEARANCE(34, Particle.POOF), //if this is mob spawner, seems to be reusing poof? check for correctness
    DRAGON_BREATH(35, Particle.DRAGON_BREATH),
    END_ROD(36, Particle.END_ROD),
    DAMAGE_INDICATOR(37, Particle.DAMAGE_INDICATOR),
    SWEEP_ATTACK(38, Particle.SWEEP_ATTACK),
    TOTEM(39, Particle.TOTEM_OF_UNDYING),
    SPIT(40, Particle.SPIT),
    SQUID_INK(41, Particle.SQUID_INK),
    BUBBLE_POP(42, Particle.BUBBLE_POP),
    CURRENT_DOWN(43, Particle.CURRENT_DOWN),
    BUBBLE_COLUMN_UP(44, Particle.BUBBLE_COLUMN_UP),
    NAUTILUS(45, Particle.NAUTILUS),
    DOLPHIN(46, Particle.DOLPHIN),
    SNEEZE(47, Particle.SNEEZE),
    CAMPFIRE_COSY_SMOKE(48, Particle.CAMPFIRE_COSY_SMOKE),
    CAMPFIRE_SIGNAL_SMOKE(49, Particle.CAMPFIRE_SIGNAL_SMOKE), //fixed to use signal instead of cosy?
    COMPOSTER(50, Particle.COMPOSTER),
    FLASH(51, Particle.FLASH),
    FALLING_LAVA(52, Particle.FALLING_LAVA),
    LANDING_LAVA(53, Particle.LANDING_LAVA),
    FALLING_WATER(54, Particle.FALLING_WATER),
    DRIPPING_HONEY(55, Particle.DRIPPING_HONEY),
    FALLING_HONEY(56, Particle.FALLING_HONEY),
    LANDING_HONEY(57, Particle.LANDING_HONEY),
    FALLING_NECTAR(58, Particle.FALLING_NECTAR),
    SOUL_FIRE_FLAME(59, Particle.SOUL_FIRE_FLAME),
    ASH(60, Particle.ASH),
    CRIMSON_SPORE(61, Particle.CRIMSON_SPORE),
    WARPED_SPORE(62, Particle.WARPED_SPORE),
    SOUL(63, Particle.SOUL),
    DRIPPING_OBSIDIAN_TEAR(64, Particle.DRIPPING_OBSIDIAN_TEAR),
    FALLING_OBSIDIAN_TEAR(65, Particle.FALLING_OBSIDIAN_TEAR),
    LANDING_OBSIDIAN_TEAR(66, Particle.LANDING_OBSIDIAN_TEAR),
    REVERSE_PORTAL(67, Particle.REVERSE_PORTAL),
    WHITE_ASH(68, Particle.WHITE_ASH),
    FALLING_SPORE_BLOSSOM(70, Particle.FALLING_SPORE_BLOSSOM),
    SPORE_BLOSSOM_AIR(71, Particle.SPORE_BLOSSOM_AIR),
    SMALL_FLAME(72, Particle.SMALL_FLAME),
    SNOWFLAKE(73, Particle.SNOWFLAKE),
    DRIPPING_DRIPSTONE_LAVA(74, Particle.DRIPPING_DRIPSTONE_LAVA),
    FALLING_DRIPSTONE_LAVA(75, Particle.DRIPPING_DRIPSTONE_LAVA),
    DRIPPING_DRIPSTONE_WATER(76, Particle.DRIPPING_DRIPSTONE_WATER),
    FALLING_DRIPSTONE_WATER(77, Particle.FALLING_DRIPSTONE_WATER),
    GLOW_SQUID_INK(78, Particle.GLOW_SQUID_INK),
    GLOW(79, Particle.GLOW),
    WAX_ON(80, Particle.WAX_ON),
    WAX_OFF(81, Particle.WAX_OFF),
    ELECTRIC_SPARK(82, Particle.ELECTRIC_SPARK),
    SCRAPE(83, Particle.SCRAPE),
    REDSTONE(100, Particle.DUST); //check for correctness

    

    private final int id;
    private final Particle bukkitParticle;

    ParticleRegistry(int id, Particle bukkitParticle) {
        this.id = id;
        this.bukkitParticle = bukkitParticle;
    }

    public static Particle getById(int id) {
        for(ParticleRegistry particleRegistry : ParticleRegistry.values())
            if(particleRegistry.getId() == id)
                return particleRegistry.getBukkitParticle();
            return null;
    }

    private int getId() {
        return id;
    }

    public Particle getBukkitParticle() {
        return bukkitParticle;
    }
}
