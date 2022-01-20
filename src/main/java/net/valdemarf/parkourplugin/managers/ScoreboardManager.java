package net.valdemarf.parkourplugin.managers;

import fr.mrmicky.fastboard.FastBoard;
import net.valdemarf.parkourplugin.Formatter;
import net.valdemarf.parkourplugin.ParkourPlugin;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public final class ScoreboardManager {
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final ParkourPlugin parkourPlugin;

    public ScoreboardManager(ParkourPlugin parkourPlugin) {
        this.parkourPlugin = parkourPlugin;
    }


    //TODO: Only run this whenever someone beats top 5 or personal best
    public void updateBoard(FastBoard board) {
        TreeSet<PlayerTime> times = parkourPlugin.getLeaderboard();

        board.updateTitle(ChatColor.RED + "Parkour");
        board.updateLines(
                "Best Attempt: " + parkourPlugin.getPlayerTimeManager().getPersonalBest(board.getPlayer().getUniqueId()),
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
    public void updateAllScoreboards() {
        for (Player playerLoop : Bukkit.getOnlinePlayers()) {
            if(parkourPlugin.getParkourManager().getParkourPlayers().contains(playerLoop.getUniqueId())) {
                Bukkit.getLogger().warning("Board has been updated! - MoveListener");
                parkourPlugin.getScoreboardManager().updateBoard(parkourPlugin.getScoreboardManager().getBoard(playerLoop));
            }
        }
    }
}
