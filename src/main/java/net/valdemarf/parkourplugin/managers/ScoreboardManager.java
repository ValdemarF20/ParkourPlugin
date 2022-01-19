package net.valdemarf.parkourplugin.managers;

import fr.mrmicky.fastboard.FastBoard;
import net.valdemarf.parkourplugin.Formatter;
import net.valdemarf.parkourplugin.ParkourPlugin;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public final class ScoreboardManager implements Listener {

    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private ParkourPlugin parkourPlugin;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreboardManager.class);

    public ScoreboardManager(ParkourPlugin parkourPlugin) {
        this.parkourPlugin = parkourPlugin;
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        FastBoard board = new FastBoard(player);

        board.updateTitle(ChatColor.RED + "Parkour");

        boards.putIfAbsent(player.getUniqueId(), board);

        updateDefaultBoard(board);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        FastBoard board = this.boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }

        parkourPlugin.getParkourManager().removeActivePlayer(player.getUniqueId());
    }


    public void updateBoard(FastBoard board) {
        board.updateTitle(ChatColor.RED + "Parkour");

        TreeSet<PlayerTime> times = parkourPlugin.getLeaderboard();

        board.updateLines(
                "Best Attempt: " + parkourPlugin.getPlayerTimeManager().getPersonalBest(board.getPlayer().getUniqueId()),
                "",
                ChatColor.RED + "Leaderboard:" + ChatColor.WHITE
        );


        // Loop through the times in the TreeSet - fastest time first
        int counter = 4;

        for (PlayerTime time : times) {
            // Replaces the 5 best times
            // TODO: Increase performance by only running this after someone beats a time on leaderboard - create variable on move event

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
}
