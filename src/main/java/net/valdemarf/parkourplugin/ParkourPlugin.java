package net.valdemarf.parkourplugin;

import co.aikar.commands.PaperCommandManager;
import com.google.gson.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.valdemarf.parkourplugin.commands.Parkour;
import net.valdemarf.parkourplugin.commands.Testing;
import net.valdemarf.parkourplugin.data.Data;
import net.valdemarf.parkourplugin.data.Database;
import net.valdemarf.parkourplugin.listeners.JoinLeaveListener;
import net.valdemarf.parkourplugin.listeners.MoveListener;
import net.valdemarf.parkourplugin.managers.*;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import net.valdemarf.parkourplugin.playertime.PlayerTimeAdapter;
import net.valdemarf.parkourplugin.playertime.PlayerTimeManager;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.*;

public final class ParkourPlugin extends JavaPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParkourPlugin.class);
    private List<Data> dataList;
    private List<Location> checkpointLocations;

    // Objects
    private CheckpointManager checkpointManager;
    private ScoreboardManager scoreboardManager;
    private ParkourManager parkourManager;
    private Database database;
    private PlayerTimeManager playerTimeManager;

    private final TreeSet<PlayerTime> leaderboard = new TreeSet<>();
    private final TreeSet<PlayerTime> personalBests = new TreeSet<>();

    public static Gson GSON;
    private static ParkourPlugin INSTANCE;

    public boolean checker = false;

    @Override
    public void onEnable() {
        LOGGER.info("Parkour Plugin has been enabled!");
        INSTANCE = this;
        GSON = createGsonInstance();

        setupDataFolder();

        // Debugging dataList
        for (Data data : dataList) {
            LOGGER.info(data.bukkitLocation().toString());
        }

        // Register objects
        ConfigManager configManager = new ConfigManager(this);
        configManager.instantiate();
        playerTimeManager = new PlayerTimeManager(this);
        database = new Database(configManager, this);

        checkpointManager = new CheckpointManager(this);
        PaperCommandManager manager = new PaperCommandManager(this);
        scoreboardManager = new ScoreboardManager(this);
        parkourManager = new ParkourManager(scoreboardManager);

        database.deserializeLeaderboard();
        database.deserializePersonalBests();

        // Commands
        manager.registerCommand(new Parkour(this));
        manager.registerCommand(new Testing(this, parkourManager));

        // Listeners
        this.getServer().getPluginManager().registerEvents(new MoveListener(this, parkourManager), this);
        this.getServer().getPluginManager().registerEvents(new JoinLeaveListener(this), this);

        updateScoreboards();
    }

    /**
     * Updates the default scoreboards based on whether they are inside the parkour region or not
     */
    private void updateScoreboards() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Only update scoreboard if player is not in parkour
                    if(!parkourManager.getParkourPlayers().contains(player.getUniqueId())) {
                        scoreboardManager.updateDefaultBoard((scoreboardManager.getBoard(player)));
                    }
                }
            }
        }.runTaskTimer(this, 1, 20);
    }

    @Override
    public void onDisable() {
        // Both loops need to run sync to avoid running it after the server has stopped

        // Updates the top 5 times in the database
        for (PlayerTime playerTime : leaderboard) {
            Document document = Document.parse(database.toJson(playerTime));

            CompletableFuture.runAsync(() -> database.getLeaderboardCollection().
                    replaceOne(Filters.eq("_id", playerTime.getUuid()), document, new ReplaceOptions().upsert(true)),
                    Database.EXECUTOR);
        }

        // Updates the personal bests
        for (PlayerTime playerTime : personalBests) {
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

    /**
     * Sets up the data folder for parkour checkpoints if it's missing.
     */
    public void setupDataFolder() {
        try {
            getDataFolder().mkdir();
            File file = new File(getDataFolder(), "checkpoints.json");

            if (file.createNewFile()) {
                LOGGER.info("Data File for Parkour has been generated");
            }

            Reader reader = new FileReader(file);
            JsonArray parser = JsonParser.parseReader(reader).getAsJsonObject().get("checkpointsData").getAsJsonArray();

            dataList = new ArrayList<>();

            for (JsonElement element : parser) {
                dataList.add(GSON.fromJson(element, Data.class));
            }

            checkpointLocations = new ArrayList<>();

            for (Data data : dataList) {
                checkpointLocations.add(data.bukkitLocation());
            }


        } catch(IOException e) {
            LOGGER.error("DataFolder could not be created", e);
        }
    }

    /**
     *
     * @return the instance of gson that should be used everywhere in the plugin
     */
    public Gson createGsonInstance() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(PlayerTime.class, new PlayerTimeAdapter());
        builder.setPrettyPrinting();
        return builder.create();
    }

    public List<Location> getCheckpointLocations() {
        return checkpointLocations;
    }

    public CheckpointManager getCheckpointManager() {
        return checkpointManager;
    }

    public TreeSet<PlayerTime> getLeaderboard() {
        return leaderboard;
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

    public static ParkourPlugin getInstance() {
        return INSTANCE;
    }
}
