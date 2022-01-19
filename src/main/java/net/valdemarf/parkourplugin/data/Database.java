package net.valdemarf.parkourplugin.data;

import com.mongodb.ConnectionString;
import com.mongodb.Mongo;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.valdemarf.parkourplugin.ParkourPlugin;
import net.valdemarf.parkourplugin.managers.ConfigManager;
import org.bson.Document;

public class Database {
    private final MongoClient client;
    private final MongoDatabase database;

    private final ParkourPlugin parkourPlugin;
    private final ConfigManager configManager;

    private final MongoCollection<Document> leaderboardCollection;
    private final MongoCollection<Document> personalBestCollection;


    public Database(ParkourPlugin parkourPlugin, ConfigManager config) {
        this.parkourPlugin = parkourPlugin;
        this.configManager = config;

        ConnectionString connectionString = new ConnectionString(
                "mongodb+srv://" +
                config.getString("username") + ":" +
                config.getString("password") + "@cluster0.lwu2m.mongodb.net/" +
                config.getString("databaseName") + "?retryWrites=true&w=majority");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        client = MongoClients.create(settings);
        database = client.getDatabase("parkourplugin");

        this.leaderboardCollection = database.getCollection("leaderboardtimes");
        this.personalBestCollection = database.getCollection("personalbesttimes");
    }


    public MongoCollection<Document> getLeaderboardCollection() {
        return leaderboardCollection;
    }

    public MongoCollection<Document> getPersonalBestCollection() {
        return personalBestCollection;
    }
}
