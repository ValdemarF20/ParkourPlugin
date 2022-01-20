package net.valdemarf.parkourplugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;

public final class ParkourManager {
    private final Set<UUID> parkourPlayers = new HashSet<>();
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, Instant> playerTimers = new HashMap<>();

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
}
