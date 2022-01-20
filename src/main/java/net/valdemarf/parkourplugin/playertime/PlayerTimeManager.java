package net.valdemarf.parkourplugin.playertime;

import net.valdemarf.parkourplugin.ParkourPlugin;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public final class PlayerTimeManager {
    private final TreeSet<PlayerTime> leaderboardTimes;
    private final TreeSet<PlayerTime> personalBests;

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerTimeManager.class);

    public PlayerTimeManager(ParkourPlugin parkourPlugin) {
        this.leaderboardTimes = parkourPlugin.getLeaderboard();
        this.personalBests = parkourPlugin.getPersonalBests();
    }

    public String getPersonalBest(UUID uuid) {
        for (PlayerTime playerTime : personalBests) {
            if(playerTime.getUuid().equals(uuid)) {
                return playerTime.getBest();
            }
        }
        return null;
    }

    public Set<PlayerTime> getPersonalBests() {
        return personalBests;
    }

    public Set<PlayerTime> getLeaderboardTimes() {
        return leaderboardTimes;
    }
}
