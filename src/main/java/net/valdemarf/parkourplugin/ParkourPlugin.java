package net.valdemarf.parkourplugin;

import co.aikar.commands.PaperCommandManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.valdemarf.parkourplugin.commands.Parkour;
import net.valdemarf.parkourplugin.commands.Testing;
import net.valdemarf.parkourplugin.data.Data;
import net.valdemarf.parkourplugin.data.Database;
import net.valdemarf.parkourplugin.listeners.MoveListener;
import net.valdemarf.parkourplugin.managers.*;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import net.valdemarf.parkourplugin.playertime.PlayerTimeManager;
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

public final class ParkourPlugin extends JavaPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParkourPlugin.class);
    private File file;
    private List<Data> dataList;
    private List<Location> checkpointLocations;

    // Objects
    private CheckpointManager checkpointManager;
    private ScoreboardManager scoreboardManager;
    private ParkourManager parkourManager;
    private ConfigManager configManager;
    private Database database;
    private PlayerTimeManager playerTimeManager;

    private final TreeSet<PlayerTime> leaderboard = new TreeSet<>();
    private final TreeSet<PlayerTime> personalBests = new TreeSet<>();

    public static Gson GSON = new Gson();

    public boolean checker = false;

    @Override
    public void onEnable() {
        LOGGER.info("Parkour Plugin has been enabled!");
        INSTANCE = this;

        setupDataFolder();

        // Debugging dataList
        for (Data data : dataList) {
            LOGGER.info(data.bukkitLocation().toString());
        }

        configManager = new ConfigManager(this);
        configManager.instantiate();
        database = new Database(this, configManager);
        playerTimeManager = new PlayerTimeManager(this);

        checkpointManager = new CheckpointManager(this);
        PaperCommandManager manager = new PaperCommandManager(this);
        scoreboardManager = new ScoreboardManager(this);
        parkourManager = new ParkourManager(scoreboardManager);

        playerTimeManager.deserializeLeaderboard();
        playerTimeManager.deserializePersonalBests();

        // Commands
        manager.registerCommand(new Parkour(this));
        manager.registerCommand(new Testing(this, parkourManager));

        // Listeners
        this.getServer().getPluginManager().registerEvents(new MoveListener(this, parkourManager), this);
        this.getServer().getPluginManager().registerEvents(scoreboardManager, this);

        updateScoreboards();
    }

    private void updateScoreboards() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if(parkourManager.getActivePlayers().contains(player.getUniqueId())) {
                        scoreboardManager.updateBoard(scoreboardManager.getBoard(player));
                    } else {
                        scoreboardManager.updateDefaultBoard((scoreboardManager.getBoard(player)));
                    }
                }
            }
        }.runTaskTimer(this, 1, 20);
    }

    @Override
    public void onDisable() {
        // Updates the top 5 times in the database
        for (PlayerTime playerTime : leaderboard) {
            playerTime.serializeLeaderboardSync();
        }

        // Updates the personal bests
        for (PlayerTime playerTime : personalBests) {
            playerTime.serializePersonalBestsSync();
        }
    }

    /**
     *
     * @return File that contains coordinates for parkour checkpoints
     */
    public File getCheckpointFile() {
        return file;
    }

    /**
     * Sets up the data folder for parkour checkpoints if it's missing.
     */
    public void setupDataFolder() {
        try {
            getDataFolder().mkdir();
            file = new File(getDataFolder(), "checkpoints.json");

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

    public List<Location> getCheckpointLocations() {
        return checkpointLocations;
    }

    public CheckpointManager getCheckpointManager() {
        return checkpointManager;
    }

    public TreeSet<PlayerTime> getLeaderboard() {
        return leaderboard;
    }

    public TreeSet<PlayerTime> getPersonalBests() {
        return personalBests;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Database getDatabase() {
        return database;
    }

    private static ParkourPlugin INSTANCE;

    public static ParkourPlugin getInstance() {
        return INSTANCE;
    }

    public ParkourManager getParkourManager() {
        return parkourManager;
    }

    public PlayerTimeManager getPlayerTimeManager() {
        return playerTimeManager;
    }
}
