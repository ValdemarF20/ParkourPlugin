package net.valdemarf.parkourplugin.listeners;

import fr.mrmicky.fastboard.FastBoard;
import net.valdemarf.parkourplugin.managers.CheckpointManager;
import net.valdemarf.parkourplugin.managers.ParkourManager;
import net.valdemarf.parkourplugin.managers.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public record JoinLeaveListener(ParkourManager parkourManager,
                                ScoreboardManager scoreboardManager,
                                CheckpointManager checkpointManager) implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        FastBoard board = new FastBoard(player);
        scoreboardManager.getBoards().putIfAbsent(player.getUniqueId(), board);
        checkpointManager.setCheckpoint(player, 0);

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
