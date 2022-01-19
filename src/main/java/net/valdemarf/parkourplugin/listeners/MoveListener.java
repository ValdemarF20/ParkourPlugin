package net.valdemarf.parkourplugin.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.valdemarf.parkourplugin.Formatter;
import net.valdemarf.parkourplugin.ParkourPlugin;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import net.valdemarf.parkourplugin.managers.CheckpointManager;
import net.valdemarf.parkourplugin.managers.ParkourManager;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class MoveListener implements Listener {
    private final ParkourPlugin parkourPlugin;
    private final ParkourManager parkourManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(MoveListener.class);

    public MoveListener(ParkourPlugin parkourPlugin, ParkourManager parkourManager) {
        this.parkourPlugin = parkourPlugin;
        this.parkourManager = parkourManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location loc = player.getLocation();

        // Handle parkour region
        if(playerIsWithinRegion(player)) {
            if(!parkourManager.getActivePlayers().contains(player.getUniqueId())) {
                parkourManager.addActivePlayer(player.getUniqueId());
            }

            // Handle updating checkpoints
            // New location to get rounded up coordinates
            Location newLoc = new Location(player.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            CheckpointManager checkpointManager = parkourPlugin.getCheckpointManager();
            List<Location> checkpointLocations = parkourPlugin.getCheckpointLocations();

            if(newLoc.equals(parkourPlugin.getCheckpointLocations().get(0))) {
                parkourManager.getPlayerTimers().put(player.getUniqueId(), Instant.now());
            }

            // Player reaches a new checkpoint
            if(newLoc.equals(checkpointManager.getNextPlayerCheckpoint(player))) {
                checkpointManager.addCheckpoint(player);

                Location lastCheckpoint = checkpointLocations.get(checkpointLocations.size() - 1);

                // Player reaches the end checkpoint
                if(checkpointManager.getPlayerCheckpoint(player).equals(lastCheckpoint)) {
                    if(!parkourPlugin.checker) {

                        Instant instantStart = parkourManager.getPlayerTimers().get(player.getUniqueId());
                        Instant instantEnd = Instant.now();

                        Duration duration = Duration.between(instantStart, instantEnd);

                        player.sendMessage("Congratulations, you've finished the parkour in " +
                                Formatter.formatTime(duration.toMillis()));

                        PlayerTime newTime = new PlayerTime(
                                duration,
                                player.getUniqueId());

                        TreeSet<PlayerTime> personalBests = parkourPlugin.getPersonalBests();

                        TreeSet<PlayerTime> leaderboardTimes = parkourPlugin.getLeaderboard();

                        // Update personal best if the player hasn't tried before
                        PlayerTime personalBest = null; // Compiler thinks it can still be null so it's required to initialize
                        PlayerTime oldBest = null; // Used to identify if the player is already on leaderboard
                        // Checking if the player has beaten the parkour before
                        boolean personalBestAlreadyCreated = false;
                        for (PlayerTime prevBest : personalBests) {
                            if(prevBest.getUuid().equals(player.getUniqueId())) {
                                // The player has beaten the parkour before
                                if(newTime.compareTo(prevBest) < 0) {
                                    // Personal best has been beaten

                                    personalBests.remove(prevBest);
                                    personalBests.add(newTime);

                                    personalBest = newTime;
                                    oldBest = prevBest;

                                    player.sendMessage(ChatColor.RED + "You've beaten your previous best time!");
                                } else {
                                    oldBest = newTime;
                                    personalBest = prevBest;
                                }
                                personalBestAlreadyCreated = true;
                                break;
                            }
                        }

                        // Figure out if player is already on the leaderboard
                        boolean playerIsInLeaderboard = false;
                        for (PlayerTime playerTime : leaderboardTimes) {
                            // Check if the player is already in the leaderboardTimes
                            if(playerTime.getUuid().equals(newTime.getUuid())) {
                                playerIsInLeaderboard = true;
                                break;
                            }
                        }

                        // Player hasn't beaten the parkour before
                        if(!personalBestAlreadyCreated) {
                            personalBest = newTime;
                            oldBest = newTime;
                            personalBests.add(newTime);
                        }

                        // Update personal if already on leaderboard and personal best has been beaten
                        if(playerIsInLeaderboard && newTime.compareTo(oldBest) < 0) {
                            leaderboardTimes.remove(oldBest);
                            leaderboardTimes.add(newTime);
                        } else if(leaderboardTimes.size() < 5) {
                            if(!playerIsInLeaderboard) { // Add if player is not on leaderboard and it's not full
                                leaderboardTimes.add(newTime);
                            }
                        } else if(!leaderboardTimes.contains(personalBest) && personalBest.compareTo(leaderboardTimes.last()) < 0) { // Add if player beats the worst time on leaderboard
                            leaderboardTimes.pollLast();
                            leaderboardTimes.add(personalBest);
                        }


                        checkpointManager.setCheckpoint(player, 0);

                        // Fire a firework
                        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                        FireworkMeta fwm = fw.getFireworkMeta();

                        fwm.setPower(2);
                        fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());

                        fw.setFireworkMeta(fwm);
                        fw.detonate();

                        parkourPlugin.checker = true;
                    }
                }
            }

        } else {
            if(parkourManager.getActivePlayers().contains(player.getUniqueId())) {
                parkourManager.removeActivePlayer(player.getUniqueId());
            }
        }
    }

    public boolean playerIsWithinRegion(Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));

        if(regions == null) {
            LOGGER.info("regions is null");
            return false;
        }

        Location playerLocation = player.getLocation();
        ProtectedRegion parkourRegion = regions.getRegion("parkour");
        if(parkourRegion == null) {
            LOGGER.info("parkour region is null");
            return false;
        }

        if(parkourRegion.contains(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ())) {
            return true;
        }
        return false;
    }
}