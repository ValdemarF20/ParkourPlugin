package net.valdemarf.parkourplugin.playertime;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.valdemarf.parkourplugin.Formatter;
import net.valdemarf.parkourplugin.ParkourPlugin;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final record PlayerTime(Duration duration, UUID uuid) implements Comparable<PlayerTime> {

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
        return ParkourPlugin.GSON.toJson(this);
    }


    public void serializeLeaderboard() {
        Document document = Document.parse(toJson());

        CompletableFuture.runAsync(() -> ParkourPlugin.getInstance().getDatabase().getLeaderboardCollection().
                replaceOne(Filters.eq("_id", uuid), document, new ReplaceOptions().upsert(true)),
                ParkourPlugin.getInstance().getExecutor());
    }

    public void serializePersonalBest() {
        Document document = Document.parse(toJson());

        CompletableFuture.runAsync(() -> ParkourPlugin.getInstance().getDatabase().getPersonalBestCollection().
                replaceOne(Filters.eq("_id", uuid), document, new ReplaceOptions().upsert(true)),
                ParkourPlugin.getInstance().getExecutor());
    }

    public String getBest() {
        return Formatter.formatTime(duration.toMillis());
    }

    public String toString() {
        return "PlayerTime[ name = " + uuid + ", time: " + duration.toMillis() + "]";
    }
}
