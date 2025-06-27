package net.exenco.lightshow.listener;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.bukkit.ChatColor;

import java.util.logging.Logger;

public class FixtureSignListener implements Listener {
    private final StageManager stageManager;
    private final Logger logger;

    public FixtureSignListener(StageManager stageManager) {
        this.stageManager = stageManager;
        this.logger = stageManager.getLightShow().getLogger();
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (!lines[0].equalsIgnoreCase("[Fixture]")) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        try {
            int address = Integer.parseInt(lines[1].trim());
            String type = lines[2].trim();
            String[] parts = lines[3].trim().split("\\s+");
            int dx = Integer.parseInt(parts[0]);
            int dy = Integer.parseInt(parts[1]);
            int dz = Integer.parseInt(parts[2]);

            Vector offset = new Vector(dx, dy, dz);
            JsonObject json = stageManager.createFixtureJsonFromLocation(
                    address, type, block.getLocation(), dx, dy, dz, null
            );
            // add immediately
            stageManager.addFixtureFromSign(json, /* universeId= */1, /* offset= */0);
            stageManager.reloadFixtures();

            event.line(0, Component.text("[Fixture]").color(NamedTextColor.GREEN));
            player.sendMessage(Component.text("Fixture added and activated.").color(NamedTextColor.GREEN));
            logger.info("Added fixture at " + block.getLocation() + ": addr=" + address + ", type=" + type + ", offset=" + offset);
        } catch (Exception ex) {
            player.sendMessage(Component.text("Invalid fixture sign format.").color(NamedTextColor.RED));

            logger.warning("Failed to add fixture from sign: " + ex.getMessage());
        }
    }


    /**
     * When a fixture sign is broken, remove its entry from signs.json and reload.
     */
    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;
        Sign sign = (Sign) state;
        String first = ChatColor.stripColor(sign.getLine(0));
        if (!first.equalsIgnoreCase("[Fixture]")) return;

        // identify fixture
        try {
            // read lines to reconstruct JSON
            int address = Integer.parseInt(sign.getLine(1).trim());
            String type = sign.getLine(2).trim();
            String[] parts = sign.getLine(3).trim().split("\\s+");
            int dx = Integer.parseInt(parts[0]);
            int dy = Integer.parseInt(parts[1]);
            int dz = Integer.parseInt(parts[2]);
            JsonObject json = stageManager.createFixtureJsonFromLocation(
                    address, type, block.getLocation(), dx, dy, dz, null
            );

            stageManager.removeFixtureFromSignsFile(json);
            stageManager.reloadFixtures();
            logger.info("Removed fixture from sign at " + block.getLocation());
        } catch (Exception ex) {
            logger.warning("Failed to remove fixture on sign break: " + ex.getMessage());
        }
    }
}






