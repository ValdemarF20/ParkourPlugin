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

    public void deserializeLeaderboard() {
        // Deserializing - getting an object from json in the database
        for (Document playerTimeDocument : ParkourPlugin.getInstance().getDatabase().getLeaderboardCollection().find()) {
            PlayerTime dbPlayerTime = ParkourPlugin.GSON.fromJson(playerTimeDocument.toJson(), PlayerTime.class);

            LOGGER.warn("A document has been found! - Leaderboard");

            if (leaderboardTimes.isEmpty()) {
                LOGGER.warn("Document has been added (was empty) - Leaderboard");
                leaderboardTimes.add(dbPlayerTime);
            } else {
                for (PlayerTime localPlayerTime : leaderboardTimes) {
                    // Check if player is in TreeSet already
                    if (localPlayerTime.getUuid().equals(dbPlayerTime.getUuid())) {
                        LOGGER.warn("Player is already in leaderboardTimes");
                        // Check if time from database is faster than local time

                        if (dbPlayerTime.compareTo(localPlayerTime) < 0) {
                            LOGGER.warn("Time in database is faster than local time - leaderboardTimes");
                            leaderboardTimes.remove(localPlayerTime);
                        }
                        LOGGER.warn("Document has been added and player was found in DB - Leaderboard");
                        leaderboardTimes.add(dbPlayerTime);
                        break;
                    } else { // Player was not in TreeSet beforehand
                        LOGGER.warn("Document has been added - Leaderboard");
                        leaderboardTimes.add(dbPlayerTime);
                    }
                }
            }
        }
    }

    public void serializeLeaderboard() {
        for (PlayerTime playerTime : leaderboardTimes) {
            playerTime.serializeLeaderboard();
        }
    }

    /**
     * Deserializes the personalBests TreeSet from MongoDB
     * Will only add the player to the TreeSet if the
     * local time is faster that the time from database
     */
    public void deserializePersonalBests() {
        // Deserializing - getting an object from json in the database
        for (Document playerTimeDocument : ParkourPlugin.getInstance().getDatabase().getPersonalBestCollection().find()) {
            PlayerTime dbPlayerTime = ParkourPlugin.GSON.fromJson(playerTimeDocument.toJson(), PlayerTime.class);

            LOGGER.warn("A document has been found! - PersonalBests");

            if(personalBests.isEmpty()) {
                LOGGER.warn("Document has been added (was empty) - PersonalBests");
                personalBests.add(dbPlayerTime);
            } else {
                for (PlayerTime localPlayerTime : personalBests) {
                    // Check if player is in TreeSet already
                    if (localPlayerTime.getUuid().equals(dbPlayerTime.getUuid())) {
                        LOGGER.warn("Player is already in personalBests");
                        // Check if time from database is faster than local time
                        if (dbPlayerTime.compareTo(localPlayerTime) < 0) {
                            LOGGER.warn("Time in database is faster than local time - personalBests");
                            personalBests.remove(localPlayerTime);
                        }
                        LOGGER.warn("Document has been added and player was found in DB - PersonalBests");
                        personalBests.add(dbPlayerTime);
                        break;
                    } else { // Player was not in TreeSet beforehand
                        LOGGER.warn("Document has been added - PersonalBests");
                        personalBests.add(dbPlayerTime);
                    }
                }
            }
        }
    }

    public void serializePersonalBests() {
        for (PlayerTime playerTime : personalBests) {
            playerTime.serializePersonalBest();
        }
    }

    public String getPersonalBest(UUID uuid) {
        for (PlayerTime playerTime : personalBests) {
            if(playerTime.getUuid().equals(uuid)) {
                return playerTime.getBest();
            }
        }
        return null;
    }
}
