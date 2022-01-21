package net.valdemarf.parkourplugin.listeners;

import net.valdemarf.parkourplugin.Formatter;
import net.valdemarf.parkourplugin.ParkourPlugin;
import net.valdemarf.parkourplugin.managers.ScoreboardManager;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import net.valdemarf.parkourplugin.managers.CheckpointManager;
import net.valdemarf.parkourplugin.managers.ParkourManager;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.TreeSet;

public final record MoveListener(ParkourPlugin parkourPlugin, ParkourManager parkourManager, ScoreboardManager scoreboardManager) implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location loc = player.getLocation();

        // Handle parkour region
        if (parkourManager.playerIsWithinRegion(player)) {
            if (!parkourManager.getParkourPlayers().contains(player.getUniqueId())) {
                parkourManager.addActivePlayer(player.getUniqueId());
            }

            // Handle updating checkpoints
            // New location to get rounded up coordinates
            Location newLoc = new Location(player.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            CheckpointManager checkpointManager = parkourPlugin.getCheckpointManager();
            List<Location> checkpointLocations = checkpointManager.getCheckpointLocations();


            if(checkpointManager.getCheckpointLocations().contains(newLoc)) {
                // Player reaches the start checkpoint
                if(newLoc.equals(checkpointManager.getCheckpointLocations().get(0))) {
                    parkourManager.getPlayerTimers().put(player.getUniqueId(), Instant.now());
                }
                // Player reaches the next checkpoint
                else if (newLoc.equals(checkpointManager.getNextPlayerCheckpoint(player))) {
                    // Player's stored checkpoint is already the last
                    if(checkpointManager.getIndex(checkpointManager.getPlayerCheckpoint(player)) == checkpointLocations.size() - 1) {
                        checkpointManager.setCheckpoint(player, 0);
                        return;
                    }

                    checkpointManager.addCheckpoint(player);

                    Location lastCheckpoint = checkpointLocations.get(checkpointLocations.size() - 1);

                    // Player reaches the end checkpoint
                    if (checkpointManager.getPlayerCheckpoint(player).equals(lastCheckpoint)) {
                        if (!parkourPlugin.endChecker) {


                            Instant instantStart = parkourManager.getPlayerTimers().get(player.getUniqueId());
                            Instant instantEnd = Instant.now();

                            if(instantStart == null) {
                                return;
                            }

                            Duration duration = Duration.between(instantStart, instantEnd);

                            player.sendMessage("Congratulations, you've finished the parkour in " +
                                    Formatter.formatTime(duration.toMillis()));

                            PlayerTime newTime = new PlayerTime(duration, player.getUniqueId());

                            TreeSet<PlayerTime> personalBests = (TreeSet<PlayerTime>) parkourPlugin.getPlayerTimeManager().getPersonalBests();
                            TreeSet<PlayerTime> leaderboardTimes = (TreeSet<PlayerTime>) parkourPlugin.getPlayerTimeManager().getLeaderboardTimes();

                            // Update personal best if the player hasn't tried before
                            PlayerTime personalBest = null; // Compiler thinks it can still be null so it's required to initialize
                            PlayerTime oldBest = null; // Used to identify if the player is already on leaderboard
                            // Checking if the player has beaten the parkour before
                            boolean personalBestAlreadyCreated = false;
                            for (PlayerTime prevBest : personalBests) {
                                if (prevBest.getUuid().equals(player.getUniqueId())) {
                                    // The player has beaten the parkour before
                                    if (newTime.compareTo(prevBest) < 0) {
                                        // Personal best has been beaten

                                        personalBests.remove(prevBest);
                                        personalBests.add(newTime);

                                        personalBest = newTime;
                                        oldBest = prevBest;

                                        player.sendMessage(ChatColor.RED + "You've beaten your previous best time!");

                                        // Update personal best on scoreboard
                                        scoreboardManager.updateBoard(scoreboardManager.getBoard(player));
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
                                if (playerTime.getUuid().equals(newTime.getUuid())) {
                                    playerIsInLeaderboard = true;
                                    break;
                                }
                            }

                            // Player hasn't beaten the parkour before
                            if (!personalBestAlreadyCreated) {
                                personalBest = newTime;
                                oldBest = newTime;
                                personalBests.add(newTime);

                                // Update personal best on scoreboard
                                scoreboardManager.updateBoard(scoreboardManager.getBoard(player));
                            }

                            // Update personal if already on leaderboard and personal best has been beaten
                            if (playerIsInLeaderboard && newTime.compareTo(oldBest) < 0) {
                                leaderboardTimes.remove(oldBest);
                                leaderboardTimes.add(newTime);
                                scoreboardManager.updateParkourScoreboards();
                                // Add if player is not on leaderboard and it's not full
                            } else if (leaderboardTimes.size() < 5 && !playerIsInLeaderboard) {
                                leaderboardTimes.add(newTime);
                                scoreboardManager.updateParkourScoreboards();
                                // Add if player beats the worst time on leaderboard
                            } else if (!leaderboardTimes.contains(personalBest) && personalBest.compareTo(leaderboardTimes.last()) < 0) {
                                leaderboardTimes.pollLast();
                                leaderboardTimes.add(personalBest);
                                scoreboardManager.updateParkourScoreboards();
                            }

                            // Fire a firework
                            Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                            FireworkMeta fwm = fw.getFireworkMeta();

                            fwm.setPower(2);
                            fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());

                            fw.setFireworkMeta(fwm);
                            fw.detonate();

                            parkourPlugin.endChecker = false;
                        }
                        parkourManager.getPlayerTimers().remove(player.getUniqueId());
                        checkpointManager.setCheckpoint(player, 0);
                    }
                }
            }


        } else {
            if (parkourManager.getParkourPlayers().contains(player.getUniqueId())) {
                parkourManager.removeActivePlayer(player.getUniqueId());
            }
        }
    }
}