package net.valdemarf.parkourplugin;

import co.aikar.commands.PaperCommandManager;
import net.valdemarf.parkourplugin.commands.Parkour;
import net.valdemarf.parkourplugin.data.Database;
import net.valdemarf.parkourplugin.listeners.JoinLeaveListener;
import net.valdemarf.parkourplugin.listeners.MoveListener;
import net.valdemarf.parkourplugin.managers.*;
import net.valdemarf.parkourplugin.playertime.PlayerTimeManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ParkourPlugin extends JavaPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParkourPlugin.class);

    // Objects
    private CheckpointManager checkpointManager;
    private ScoreboardManager scoreboardManager;
    private ParkourManager parkourManager;
    private Database databaseManager;
    private PlayerTimeManager playerTimeManager;

    public boolean endChecker = false;

    @Override
    public void onEnable() {
        LOGGER.info("Parkour Plugin has been enabled!");

        // Side effect free objects:
        checkpointManager = new CheckpointManager(this);
        checkpointManager.setupDataFolder();
        ConfigManager configManager = new ConfigManager(this);
        configManager.instantiate();
        playerTimeManager = new PlayerTimeManager();
        PaperCommandManager manager = new PaperCommandManager(this);

        // None side effect free objects (relies on other classes):
        scoreboardManager = new ScoreboardManager(this, playerTimeManager, scoreboardManager);
        parkourManager = new ParkourManager(scoreboardManager);
        databaseManager = new Database(this, configManager, playerTimeManager);

        databaseManager.deserializeLeaderboard();
        databaseManager.deserializePersonalBests();

        // Commands
        manager.registerCommand(new Parkour(checkpointManager));

        // Listeners
        this.getServer().getPluginManager().registerEvents(new MoveListener(this, parkourManager, scoreboardManager), this);
        this.getServer().getPluginManager().registerEvents(new JoinLeaveListener(parkourManager, scoreboardManager, checkpointManager), this);

        scoreboardManager.updateDefaultScoreboards();
    }

    @Override
    public void onDisable() {
        // Updates the database and stops all tasks after they're done
        databaseManager.save(true);
    }

    // Note on why the main instance should be avoided:
    // - It gets really hard to see what our classes really depend on
    // - It sometimes gets hard to reuse the code as that plugin instance always has to be passed
    // - (Harder to unit test)
    //
    // So the getters should be avoided as much as possible (without making everything messy)

    public CheckpointManager getCheckpointManager() {
        return checkpointManager;
    }

    public ParkourManager getParkourManager() {
        return parkourManager;
    }

    public PlayerTimeManager getPlayerTimeManager() {
        return playerTimeManager;
    }
}
