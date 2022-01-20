package net.valdemarf.parkourplugin;

import co.aikar.commands.PaperCommandManager;
import net.valdemarf.parkourplugin.commands.Parkour;
import net.valdemarf.parkourplugin.commands.Testing;
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


    public boolean checker = false;

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

        // None side effect free objects:
        databaseManager = new Database(configManager, playerTimeManager);
        scoreboardManager = new ScoreboardManager(this);
        parkourManager = new ParkourManager(scoreboardManager);

        databaseManager.deserializeLeaderboard();
        databaseManager.deserializePersonalBests();

        // Commands
        manager.registerCommand(new Parkour(checkpointManager));
        manager.registerCommand(new Testing(this, parkourManager));

        // Listeners
        this.getServer().getPluginManager().registerEvents(new MoveListener(this, parkourManager), this);
        this.getServer().getPluginManager().registerEvents(new JoinLeaveListener(parkourManager, scoreboardManager), this);

        scoreboardManager.updateDefaultScoreboards();
    }

    @Override
    public void onDisable() {
        // Both loops need to run sync to avoid running it after the server has stopped

        // Updates the top 5 times in the database
        databaseManager.saveSync();
    }

    public CheckpointManager getCheckpointManager() {
        return checkpointManager;
    }

    public Database getDatabaseManager() {
        return databaseManager;
    }

    public ParkourManager getParkourManager() {
        return parkourManager;
    }

    public PlayerTimeManager getPlayerTimeManager() {
        return playerTimeManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
