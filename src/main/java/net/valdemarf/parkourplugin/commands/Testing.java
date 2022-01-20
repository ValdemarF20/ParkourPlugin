package net.valdemarf.parkourplugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import net.valdemarf.parkourplugin.managers.CheckpointManager;
import net.valdemarf.parkourplugin.ParkourPlugin;
import net.valdemarf.parkourplugin.managers.ParkourManager;
import net.valdemarf.parkourplugin.playertime.PlayerTimeManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@CommandAlias("test|t")
public final class Testing extends BaseCommand {
    private final ParkourPlugin parkourPlugin;
    private final ParkourManager parkourManager;
    private final PlayerTimeManager playerTimeManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(Testing.class);

    public Testing(ParkourPlugin parkourPlugin, ParkourManager parkourManager) {
        this.parkourPlugin = parkourPlugin;
        this.parkourManager = parkourManager;
        this.playerTimeManager = parkourPlugin.getPlayerTimeManager();
    }

    @Default
    public void onTest(Player player) {
        CheckpointManager checkpointManager = parkourPlugin.getCheckpointManager();
        Location latestCheckpoint = checkpointManager.getPlayerCheckpoint(player);

        LOGGER.info("Latest checkpoint: " + latestCheckpoint.toString());
        LOGGER.info("Index: " + checkpointManager.getIndex(latestCheckpoint));

        LOGGER.info("Amount of players in ActivePlayers list: " + parkourManager.getParkourPlayers().size());
        for (UUID uuid : parkourManager.getParkourPlayers()) {
            Player uuidPlayer = Bukkit.getPlayer(uuid);
            if(uuidPlayer == null) {
                continue;
            }
            LOGGER.info("Player: " + uuidPlayer.displayName());
        }
    }

    @Subcommand("add")
    public void onAdd(Player player) {
        CheckpointManager checkpointManager = parkourPlugin.getCheckpointManager();
        checkpointManager.addCheckpoint(player);
    }

    @Subcommand("set")
    public void onSet(Player player, int index) {
        CheckpointManager checkpointManager = parkourPlugin.getCheckpointManager();
        if(!(checkpointManager.setCheckpoint(player, index))) {
            player.sendMessage("There are not that many checkpoint available");
        }
    }

    @Subcommand("times")
    public void onTimes(Player player) {
        LOGGER.warn("All times stored in TreeSet:");

        int counter = 1;
        for (PlayerTime time : playerTimeManager.getLeaderboardTimes()) {
            LOGGER.warn(counter + time.getDuration().toString());
            counter++;
        }
    }

    @Subcommand("save")
    public void onSave(Player player) {
        parkourPlugin.getDatabase().serializeLeaderboards();
        parkourPlugin.getDatabase().serializePersonalBests();
        player.sendMessage("Server has been saved!");
        LOGGER.warn("Server has been saved!");
    }

    @Subcommand("deserialize|des")
    public void onDeserialize(Player player) {
        player.sendMessage("PlayerTimes has been deserialized");

        // Deserialize player times to get the java objects instead of json
        parkourPlugin.getDatabase().deserializeLeaderboard();
        parkourPlugin.getDatabase().deserializePersonalBests();
    }
}
