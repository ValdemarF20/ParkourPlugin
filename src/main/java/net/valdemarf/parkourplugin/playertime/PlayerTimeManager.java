package net.valdemarf.parkourplugin.playertime;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public final class PlayerTimeManager {
    private final TreeSet<PlayerTime> leaderboardTimes = new TreeSet<>();
    private final TreeSet<PlayerTime> personalBests = new TreeSet<>();

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
