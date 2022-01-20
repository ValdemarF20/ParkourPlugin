package net.valdemarf.parkourplugin.listeners;

import fr.mrmicky.fastboard.FastBoard;
import net.valdemarf.parkourplugin.ParkourPlugin;
import net.valdemarf.parkourplugin.managers.ParkourManager;
import net.valdemarf.parkourplugin.managers.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {
    private final ScoreboardManager scoreboardManager;
    private final ParkourManager parkourManager;

    public JoinLeaveListener(ParkourManager parkourManager, ScoreboardManager scoreboardManager) {
        this.parkourManager = parkourManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        FastBoard board = new FastBoard(player);
        scoreboardManager.getBoards().putIfAbsent(player.getUniqueId(), board);

        scoreboardManager.updateDefaultBoard(board);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        FastBoard board = scoreboardManager.getBoards().remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }

        parkourManager.getPlayerTimers().remove(player.getUniqueId());
        parkourManager.removeActivePlayer(player.getUniqueId());
    }
}
