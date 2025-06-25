import net.minecrell.pluginyml.bukkit.BukkitPluginDescription



plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17" //update
    id("de.eldoria.plugin-yml.bukkit") version "0.7.1" //update
}

group = "net.exenco.lightshow"
version = "1.2.5" //update
description = "Display a Light-Show in Minecraft."

dependencies {
    paperweight.paperDevBundle("1.21.6-R0.1-SNAPSHOT") //update
    compileOnly("io.papermc.paper:paper-api:1.21.6-R0.1-SNAPSHOT")  //new
    library("com.google.code.gson", "gson", "2.10.1") //new

}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()

        options.release.set(21) // updated

    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

bukkit {
    main = "net.exenco.lightshow.LightShow"
    apiVersion = "1.21" //update
    author = "Exenco"
    commands {
        register("show") {
            description = "Controls the plugin."
            permission = "lightshow.show"
            usage = "/show"
        }
    }

    permissions {
        register("lightshow.*") {
            description = "Gives access to all lightshow commands."
            children = listOf("lightshow.show", "lightshow.check", "lightshow.reload", "lightshow.start", "lightshow.stop", "lightshow.toggle", "lightshow.warning")
        }
        register("lightshow.show") {
            description = "Allows to control the lightshow."
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("lightshow.check") {
            description = "Allows to check connectivity."
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("lightshow.reload") {
            description = "Allows to reload the plugin."
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("lightshow.start") {
            description = "Allows to start ArtNet connection."
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("lightshow.stop") {
            description = "Allows to stop ArtNet connection."
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("lightshow.toggle") {
            description = "Allows to toggle show visibility."
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("lightshow.warning") {
            description = "Allows to view warning message."
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
    }
}




// new
// see https://docs.papermc.io/paper/dev/userdev/#1205-and-beyond
// keeping paper and spigot compatability here
paperweight {
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION
}

