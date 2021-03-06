package net.valdemarf.parkourplugin.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.valdemarf.parkourplugin.ParkourPlugin;
import net.valdemarf.parkourplugin.managers.ConfigManager;
import net.valdemarf.parkourplugin.playertime.PlayerTime;
import net.valdemarf.parkourplugin.playertime.PlayerTimeAdapter;
import net.valdemarf.parkourplugin.playertime.PlayerTimeManager;
import org.bson.Document;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Database {

    private final MongoCollection<Document> leaderboardCollection;
    private final MongoCollection<Document> personalBestCollection;

    private final PlayerTimeManager playerTimeManager;
    private final ParkourPlugin parkourPlugin;

    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    public static Gson GSON = createGsonInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public Database(ParkourPlugin parkourPlugin, ConfigManager config, PlayerTimeManager playerTimeManager) {
        this.playerTimeManager = playerTimeManager;
        this.parkourPlugin = parkourPlugin;

        MongoDatabase database = initializeDatabase(config);

        this.leaderboardCollection = database.getCollection("leaderboardtimes");
        this.personalBestCollection = database.getCollection("personalbesttimes");

        autoSave();
    }

    /**
     * Initializes the database based on values from config.yml
     * @param config Config.yml file containing connection values
     * @return The MongoDatabase that is being connected to
     */
    private MongoDatabase initializeDatabase(ConfigManager config) {
        ConnectionString connectionString = new ConnectionString(
                "mongodb+srv://" +
                        config.getString("username") + ":" +
                        config.getString("password") + "@cluster0.lwu2m.mongodb.net/" +
                        config.getString("databaseName") + "?retryWrites=true&w=majority");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        final MongoClient client = MongoClients.create(settings);
        return client.getDatabase("parkourplugin");
    }


    public MongoCollection<Document> getLeaderboardCollection() {
        return leaderboardCollection;
    }

    public MongoCollection<Document> getPersonalBestCollection() {
        return personalBestCollection;
    }

    public String toJson(PlayerTime playerTime) {
        return GSON.toJson(playerTime);
    }

    public void serializeLeaderboard(PlayerTime playerTime) {
        Document document = Document.parse(toJson(playerTime));

        CompletableFuture.runAsync(() -> getLeaderboardCollection().
                replaceOne(Filters.eq("_id", playerTime.getUuid()), document, new ReplaceOptions().upsert(true)),
                Database.EXECUTOR);
    }

    public void serializePersonalBest(PlayerTime playerTime) {

        Document document = Document.parse(toJson(playerTime));

        CompletableFuture.runAsync(() -> getPersonalBestCollection().
                replaceOne(Filters.eq("_id", playerTime.getUuid()), document, new ReplaceOptions().upsert(true)),
                Database.EXECUTOR);
    }

    public void serializePersonalBests() {
        for (PlayerTime playerTime : playerTimeManager.getPersonalBests()) {
            serializePersonalBest(playerTime);
        }
    }

    public void serializeLeaderboards() {
        for (PlayerTime playerTime : playerTimeManager.getLeaderboardTimes()) {
            serializeLeaderboard(playerTime);
        }
    }

    public void deserializeLeaderboard() {
        Set<PlayerTime> leaderboardTimes = playerTimeManager.getLeaderboardTimes();

        // Deserializing - getting an object from json in the database
        for (Document playerTimeDocument : getLeaderboardCollection().find()) {
            PlayerTime dbPlayerTime = GSON.fromJson(playerTimeDocument.toJson(), PlayerTime.class);

            if (leaderboardTimes.isEmpty()) {
                leaderboardTimes.add(dbPlayerTime);
            } else {
                for (PlayerTime localPlayerTime : leaderboardTimes) {
                    // Check if player is in TreeSet already
                    if (localPlayerTime.getUuid().equals(dbPlayerTime.getUuid())) {
                        // Check if time from database is faster than local time

                        if (dbPlayerTime.compareTo(localPlayerTime) < 0) {
                            leaderboardTimes.remove(localPlayerTime);
                        }
                        leaderboardTimes.add(dbPlayerTime);
                        break;
                    } else { // Player was not in TreeSet beforehand
                        leaderboardTimes.add(dbPlayerTime);
                    }
                }
            }
        }
    }

    /**
     * Deserializes the personalBests TreeSet from MongoDB
     * Will only add the player to the TreeSet if the
     * local time is faster that the time from database
     */
    public void deserializePersonalBests() {
        Set<PlayerTime> personalBests = playerTimeManager.getPersonalBests();

        // Deserializing - getting an object from json in the database
        for (Document playerTimeDocument : getPersonalBestCollection().find()) {
            PlayerTime dbPlayerTime = GSON.fromJson(playerTimeDocument.toJson(), PlayerTime.class);

            if(personalBests.isEmpty()) {
                personalBests.add(dbPlayerTime);
            } else {
                for (PlayerTime localPlayerTime : personalBests) {
                    // Check if player is in TreeSet already
                    if (localPlayerTime.getUuid().equals(dbPlayerTime.getUuid())) {
                        // Check if time from database is faster than local time
                        if (dbPlayerTime.compareTo(localPlayerTime) < 0) {
                            personalBests.remove(localPlayerTime);
                        }
                        personalBests.add(dbPlayerTime);
                        break;
                    } else { // Player was not in TreeSet beforehand
                        personalBests.add(dbPlayerTime);
                    }
                }
            }
        }
    }

    /**
     *
     * @return the instance of gson that should be used everywhere in the plugin
     */
    public static Gson createGsonInstance() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(PlayerTime.class, new PlayerTimeAdapter());
        builder.setPrettyPrinting();
        return builder.create();
    }

    /**
     * Makes sure that every task is stopped before closing the JVM
     */
    public void shutdown() {
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
     * Serializes the data synchronized to avoid running task after server has stopped
     * @param shutdown Whether the current tasks running should stop or not
     */
    public void save(boolean shutdown) {
        serializeLeaderboards();
        serializePersonalBests();

        if(shutdown) {
            shutdown();
        }
    }

    public void autoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {

            }
        }.runTaskTimerAsynchronously(parkourPlugin, 1, 10 * 60 * 60); // Runs every 10 minutes
    }
}
