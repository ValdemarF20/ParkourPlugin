package net.valdemarf.parkourplugin.managers;

import net.valdemarf.parkourplugin.ParkourPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public final class CheckpointManager {
    private final ParkourPlugin parkourPlugin;
    Map<Player, Location> playerCheckpoints = new HashMap<>();

    public CheckpointManager(ParkourPlugin parkourPlugin) {
        this.parkourPlugin = parkourPlugin;
    }

    /**
     * Sets the players checkpoint to the next
     * @param player The player that has reached the next checkpoint
     */
    public void addCheckpoint(Player player) {
        // Adds the player if it's the first time using the parkour. (Since server restart)
        if(!playerCheckpoints.containsKey(player)) { // Player hasn't played since restart
            playerCheckpoints.put(player, parkourPlugin.getCheckpointLocations().get(0));
        } else { // Increments with one - since player has played since restart
            playerCheckpoints.replace(player, getNextPlayerCheckpoint(player));
        }
    }

    /**
     * Set a specific checkpoint for a player
     * @param player Player whose checkpoint is being set
     * @param index Decides checkpoint based on index
     * @return True if checkpoint was successfully set and false if not
     */
    public boolean setCheckpoint(Player player, int index) {
        List<Location> locations = parkourPlugin.getCheckpointLocations();

        // Adds the player if it's the first time using the parkour. (Since server restart)
        if(!playerCheckpoints.containsKey(player)) { // Player hasn't played since restart
            playerCheckpoints.put(player, locations.get(0));
        } else { // Increments with one - since player has played since restart

            // There are less checkpoints stored than the amount specified
            if(locations.size() < (index + 1)) {
                return false;
            }

            playerCheckpoints.replace(player, locations.get(index));
        }
        return true;
    }

    /**
     * Gets the latest checkpoint of a specific player
     * @param player The player whose checkpoint is requested
     * @return A location based on the players latest checkpoint
     */
    public Location getPlayerCheckpoint(Player player) {
        if(playerCheckpoints.containsKey(player)) {
            return playerCheckpoints.get(player);
        }
        return parkourPlugin.getCheckpointLocations().get(0);
    }

    /**
     * Gets a players next checkpoint
     * @param player Player whose checkpoint is requested
     * @return A location based on the players next checkpoint
     */
    public Location getNextPlayerCheckpoint(Player player) {
        List<Location> checkpointLocations = parkourPlugin.getCheckpointLocations();

        boolean checker = false;
        for (Location location : checkpointLocations) {
            if(checker) {
                return location;
            }
            if (location.equals(getPlayerCheckpoint(player))){
                checker = true;
            }
        }

        // This will run if there are no more checkpoints
        if(checker) {
            return getPlayerCheckpoint(player);
        }

        // If the player hasn't played since server restart - return start checkpoint
        return checkpointLocations.get(0);
    }

    /**
     * Gets the index based on checkpoint
     * @param location Has to be a valid checkpoint
     * @return The index based on given checkpoint / location
     */
    public int getIndex(Location location) {
        return parkourPlugin.getCheckpointLocations().indexOf(location);
    }
}
