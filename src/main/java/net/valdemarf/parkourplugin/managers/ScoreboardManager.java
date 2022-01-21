package net.valdemarf.parkourplugin.managers;

import fr.mrmicky.fastboard.FastBoard;
import net.valdemarf.parkourplugin.Formatter;
import net.valdemarf.parkourplugin.ParkourPlugin;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import net.valdemarf.parkourplugin.playertime.PlayerTimeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public final class ScoreboardManager {
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final PlayerTimeManager playerTimeManager;
    private final ParkourPlugin parkourPlugin;
    private final ScoreboardManager scoreboardManager;

    public ScoreboardManager(ParkourPlugin parkourPlugin, PlayerTimeManager playerTimeManager, ScoreboardManager scoreboardManager) {
        this.parkourPlugin = parkourPlugin;
        this.playerTimeManager = playerTimeManager;
        this.scoreboardManager = scoreboardManager;
    }

    //TODO: Only run this whenever someone beats top 5 or personal best
    public void updateBoard(FastBoard board) {
        TreeSet<PlayerTime> times = (TreeSet<PlayerTime>) playerTimeManager.getLeaderboardTimes();

        board.updateTitle(ChatColor.RED + "Parkour");
        board.updateLines(
                "Best Attempt: " + playerTimeManager.getPersonalBest(board.getPlayer().getUniqueId()),
                "",
                ChatColor.RED + "Leaderboard:" + ChatColor.WHITE
        );

        // Loop through the times in the TreeSet - fastest time first
        int counter = 4;

        for (PlayerTime time : times) {
            // Replaces the 5 best times
            if(counter < 9 ) {
                board.updateLine(counter,
                        ChatColor.RED + String.valueOf(counter - 3) +
                        ChatColor.WHITE + " - " +
                        Bukkit.getOfflinePlayer(time.getUuid()).getName() + " - " +
                        Formatter.formatTime(time.getDuration().toMillis()));
            }
            counter++;
        }
    }


    public void updateDefaultBoard(FastBoard board) {
        board.updateTitle(ChatColor.BLUE + "Lobby");
        board.updateLines(
                "",
                "Players: " + getServer().getOnlinePlayers().size(),
                "",
                "Kills: " + board.getPlayer().getStatistic(Statistic.PLAYER_KILLS),
                ""
        );
    }

    public FastBoard getBoard(Player player) {
        return boards.get(player.getUniqueId());
    }

    public Map<UUID, FastBoard> getBoards() {
        return boards;
    }

    //TODO: Scoreboard updates twice if player didn't have a personal best before
    /**
     * Update scoreboard for every player inside the parkour region
     */
    public void updateParkourScoreboards() {
        for (Player playerLoop : Bukkit.getOnlinePlayers()) {
            if(parkourPlugin.getParkourManager().getParkourPlayers().contains(playerLoop.getUniqueId())) {
                scoreboardManager.updateBoard(scoreboardManager.getBoard(playerLoop));
            }
        }
    }

    /**
     * Updates the default scoreboards based on whether they are inside the parkour region or not
     * Will run every second
     */
    public void updateDefaultScoreboards() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Only update scoreboard if player is not in parkour
                    if(!parkourPlugin.getParkourManager().getParkourPlayers().contains(player.getUniqueId())) {
                        updateDefaultBoard((getBoard(player)));
                    }
                }
            }
        }.runTaskTimer(parkourPlugin, 1, 20);
    }
}