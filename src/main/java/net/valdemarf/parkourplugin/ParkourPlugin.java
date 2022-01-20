package net.valdemarf.parkourplugin;

import co.aikar.commands.PaperCommandManager;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.valdemarf.parkourplugin.commands.Parkour;
import net.valdemarf.parkourplugin.commands.Testing;
import net.valdemarf.parkourplugin.data.Database;
import net.valdemarf.parkourplugin.listeners.JoinLeaveListener;
import net.valdemarf.parkourplugin.listeners.MoveListener;
import net.valdemarf.parkourplugin.managers.*;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import net.valdemarf.parkourplugin.playertime.PlayerTimeManager;
import org.bson.Document;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public final class ParkourPlugin extends JavaPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParkourPlugin.class);

    // Objects
    private CheckpointManager checkpointManager;
    private ScoreboardManager scoreboardManager;
    private ParkourManager parkourManager;
    private Database database;
    private PlayerTimeManager playerTimeManager;


    public boolean checker = false;

    @Override
    public void onEnable() {
        LOGGER.info("Parkour Plugin has been enabled!");

        // Register objects
        // Bootstrapping process because it will go through and create the objects without external input

        //TODO: Sort side effect objects

        // Side effect free objects:
        checkpointManager = new CheckpointManager(this);
        checkpointManager.setupDataFolder();
        ConfigManager configManager = new ConfigManager(this);
        configManager.instantiate();
        playerTimeManager = new PlayerTimeManager();
        PaperCommandManager manager = new PaperCommandManager(this);

        // None side effect free objects:
        database = new Database(configManager, playerTimeManager);
        scoreboardManager = new ScoreboardManager(this);
        parkourManager = new ParkourManager(scoreboardManager);

        database.deserializeLeaderboard();
        database.deserializePersonalBests();

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
        for (PlayerTime playerTime : playerTimeManager.getLeaderboardTimes()) {
            Document document = Document.parse(database.toJson(playerTime));

            CompletableFuture.runAsync(() -> database.getLeaderboardCollection().
                    replaceOne(Filters.eq("_id", playerTime.getUuid()), document, new ReplaceOptions().upsert(true)),
                    Database.EXECUTOR);
        }

        // Updates the personal bests
        for (PlayerTime playerTime : playerTimeManager.getPersonalBests()) {
            Document document = Document.parse(database.toJson(playerTime));

            CompletableFuture.runAsync(() -> database.getPersonalBestCollection().
                    replaceOne(Filters.eq("_id", playerTime.getUuid()), document, new ReplaceOptions().upsert(true)),
                    Database.EXECUTOR);
        }

        Database.EXECUTOR.shutdown(); // Stops new tasks from being scheduled to the executor.

        try {
            if (!Database.EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) { // Wait for existing tasks to terminate.
                Database.EXECUTOR.shutdownNow(); // Cancel currently executing tasks that didn't finish in the time.

                if (!Database.EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) { // Wait for tasks to respond to cancellation.
                    LOGGER.error("Pool failed to terminate");
                }
            }
        } catch (InterruptedException e) {
            Database.EXECUTOR.shutdownNow(); // Cancel currently executing tasks if interrupted.
            Thread.currentThread().interrupt(); // Preserve interrupt status.
        }
    }

    public CheckpointManager getCheckpointManager() {
        return checkpointManager;
    }

    public Database getDatabase() {
        return database;
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
