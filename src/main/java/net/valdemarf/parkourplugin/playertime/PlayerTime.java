package net.valdemarf.parkourplugin.playertime;

import net.valdemarf.parkourplugin.Formatter;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public final record PlayerTime(Duration duration, UUID uuid) implements Comparable<PlayerTime> {

    public Duration getDuration() {
        return duration;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getBest() {
        return Formatter.formatTime(duration.toMillis());
    }

    @Override
    public int compareTo(@NotNull PlayerTime o) {
        return this.duration.compareTo(o.duration);
    }

    public String toString() {
        return "PlayerTime[ name = " + uuid + ", time: " + duration.toMillis() + "]";
    }
}
