package net.exenco.lightshow.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Tracks who’s “close enough” to the stage anchor, and when they enter/leave
 * calls PacketHandler.set/reset(player).
 */
public class ProximitySensor {
    private final List<Player> playerList = new ArrayList<>();
    private final List<Player> toggleList = new ArrayList<>();

    private Location anchor;
    private double radius;

    private final ShowSettings showSettings;
    private PacketHandler packetHandler;

    public ProximitySensor(ShowSettings showSettings) {
        this.showSettings = showSettings;
        load();
    }

    public void load() {
        playerList.clear();
        toggleList.clear();
        this.radius = showSettings.stage().radius();
        this.anchor = showSettings.stage().location();
    }

    public void setPacketHandler(PacketHandler handler) {
        this.packetHandler = handler;
    }

    public Location getAnchor() {
        return anchor;
    }

    public List<Player> getPlayerList() {
        return Collections.unmodifiableList(playerList);
    }

    public void playerMove(Player player) {
        boolean inside = player.getLocation().distance(anchor) <= radius;

        if (inside) {
            if (playerList.contains(player) || toggleList.contains(player)) return;
            playerList.add(player);
            player.sendMessage(showSettings.stage().termsOfService());
            packetHandler.set(player);
        } else {
            if (!playerList.contains(player)) return;
            playerList.remove(player);
            packetHandler.reset(player);
        }
    }

    public void addTogglePlayer(Player player) {
        if (toggleList.contains(player)) return;
        packetHandler.reset(player);
        toggleList.add(player);
        playerList.remove(player);
    }

    public void removeTogglePlayer(Player player) {
        toggleList.remove(player);
    }

    public boolean containsTogglePlayer(Player player) {
        return toggleList.contains(player);
    }
}
