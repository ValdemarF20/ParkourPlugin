package net.valdemarf.parkourplugin.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public final class ParkourManager {
    private final Set<UUID> parkourPlayers = new HashSet<>();
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, Instant> playerTimers = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ParkourManager.class);

    public ParkourManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }


    /**
     * Add player to HashSet to keep track of players within parkour region
     * @param uuid The unique id of the player being added
     */
    public void addActivePlayer(UUID uuid) {
        parkourPlayers.add(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) {
            return;
        }
        scoreboardManager.updateBoard(scoreboardManager.getBoard(player));
    }

    /**
     * Remove a player from HashSet that keeps track of players within parkour region
     * @param uuid The unique id of the player being removed
     */
    public void removeActivePlayer(UUID uuid) {
        parkourPlayers.remove(uuid);
    }

    /**
     * Gets a set of players currently within the parkour region
     * @return A set of unique IDs of the players within the parkour region
     */
    public Set<UUID> getParkourPlayers() {
        return parkourPlayers;
    }

    /**
     * Will only contain players whose parkour timer has started
     * @return The current map of players in a parkour
     */
    public Map<UUID, Instant> getPlayerTimers() {
        return playerTimers;
    }

    /**
     * Checks if a given player is within the parkour region
     * @param player The player that is being checked
     * @return True if the player is within the region and false if not
     */
    public boolean playerIsWithinRegion(Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));

        if (regions == null) {
            LOGGER.info("regions is null");
            return false;
        }

        Location playerLocation = player.getLocation();
        ProtectedRegion parkourRegion = regions.getRegion("parkour");
        if (parkourRegion == null) {
            LOGGER.info("parkour region is null");
            return false;
        }

        return parkourRegion.contains(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
    }
}
