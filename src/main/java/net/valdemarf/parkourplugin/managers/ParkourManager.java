package net.valdemarf.parkourplugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;

public class ParkourManager {
    private final List<UUID> activePlayers = new ArrayList<>();
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, Instant> playerTimers = new HashMap<>();

    public ParkourManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }


    public void addActivePlayer(UUID uuid) {
        activePlayers.add(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) {
            return;
        }
        scoreboardManager.updateBoard(scoreboardManager.getBoard(player));
    }

    public void removeActivePlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }

    public List<UUID> getActivePlayers() {
        return activePlayers;
    }

    public Map<UUID, Instant> getPlayerTimers() {
        return playerTimers;
    }
}
