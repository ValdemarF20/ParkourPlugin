package net.valdemarf.parkourplugin.playertime;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerTimeAdapter extends TypeAdapter<PlayerTime> {

    @Override
    public void write(JsonWriter out, PlayerTime value) throws IOException {
        out.beginObject();
        out.name("uuid");
        out.value(value.getUuid().toString());
        out.name("time");
        out.value(value.getDuration().toMillis());
        out.endObject();
    }

    @Override
    public PlayerTime read(JsonReader in) throws IOException {
        PlayerTime playerTime;

        String uuidString = null;
        Duration duration = null;

        in.beginObject();

        while (in.hasNext()) {
            switch (in.nextName()) {
                case "uuid" -> uuidString = in.nextString();
                case "time" -> duration = Duration.of(in.nextLong(), ChronoUnit.MILLIS);
                default -> in.skipValue();
            }
        }

        if(uuidString == null) {
            Bukkit.getLogger().log(Level.SEVERE, "UUID IS NULL!");
            in.endObject();
            return null;
        } else if(duration == null) {
            Bukkit.getLogger().log(Level.SEVERE, "DURATION IS NULL!");
            in.endObject();
            return null;
        }

        playerTime = new PlayerTime(duration, UUID.fromString(uuidString));

        in.endObject();
        return playerTime;
    }
}
