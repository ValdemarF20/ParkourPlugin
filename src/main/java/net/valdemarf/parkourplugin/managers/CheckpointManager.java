package net.valdemarf.parkourplugin.managers;

import net.valdemarf.parkourplugin.ParkourPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class CheckpointManager {
    private final ParkourPlugin parkourPlugin;
    Map<Player, Location> playerCheckpoints = new HashMap<>();

    public CheckpointManager(ParkourPlugin parkourPlugin) {
        this.parkourPlugin = parkourPlugin;
    }

    public void addCheckpoint(Player player) {
        // Adds the player if it's the first time using the parkour. (Since server restart)
        if(!playerCheckpoints.containsKey(player)) { // Player hasn't played since restart
            playerCheckpoints.put(player, parkourPlugin.getCheckpointLocations().get(0));
        } else { // Increments with one - since player has played since restart
            playerCheckpoints.replace(player, getNextPlayerCheckpoint(player));
        }
    }

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

    public Location getPlayerCheckpoint(Player player) {
        if(playerCheckpoints.containsKey(player)) {
            return playerCheckpoints.get(player);
        }
        return parkourPlugin.getCheckpointLocations().get(0);
    }

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

    public int getIndex(Location location) {
        return parkourPlugin.getCheckpointLocations().indexOf(location);
    }
}
