package net.valdemarf.parkourplugin.playertime;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.valdemarf.parkourplugin.Formatter;
import net.valdemarf.parkourplugin.ParkourPlugin;
import org.bson.Document;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public class PlayerTime implements Comparable<PlayerTime> {
    private final Duration duration;
    private final UUID uuid;

    public PlayerTime(Duration bestTime, UUID uuid) {
        this.duration = bestTime;
        this.uuid = uuid;
    }

    public Duration getDuration() {
        return duration;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public int compareTo(@NotNull PlayerTime o) {
        return this.duration.compareTo(o.duration);
    }

    public String toJson() {
        PlayerTimeManager playerTimeManager = ParkourPlugin.getInstance().getPlayerTimeManager();
        return playerTimeManager.getGsonBuilder().toJson(this);
    }


    public void serializeLeaderboard() {
        Document document = Document.parse(toJson());

        new BukkitRunnable() {
            @Override
            public void run() {
                ParkourPlugin.getInstance().getDatabase().getLeaderboardCollection().
                        replaceOne(Filters.eq("_id", uuid), document, new ReplaceOptions().upsert(true));
            }
        }.runTaskAsynchronously(ParkourPlugin.getInstance());
    }

    public void serializeLeaderboardSync() {
        Document document = Document.parse(toJson());

        ParkourPlugin.getInstance().getDatabase().getLeaderboardCollection().
                replaceOne(Filters.eq("_id", uuid), document, new ReplaceOptions().upsert(true));

    }

    public void serializePersonalBests() {
        Document document = Document.parse(toJson());

        new BukkitRunnable() {
            @Override
            public void run() {
                ParkourPlugin.getInstance().getDatabase().getPersonalBestCollection().
                        replaceOne(Filters.eq("_id", uuid), document, new ReplaceOptions().upsert(true));
            }
        }.runTaskAsynchronously(ParkourPlugin.getInstance());
    }

    public void serializePersonalBestsSync() {
        Document document = Document.parse(toJson());

        ParkourPlugin.getInstance().getDatabase().getPersonalBestCollection().
                replaceOne(Filters.eq("_id", uuid), document, new ReplaceOptions().upsert(true));
    }

    public String getBest() {
        return Formatter.formatTime(duration.toMillis());
    }

    public String toString() {
        return "PlayerTime[ name = " + uuid + ", time: "+ duration.toMillis() + "]";
    }
}
