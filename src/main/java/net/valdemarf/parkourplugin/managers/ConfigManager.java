package net.valdemarf.parkourplugin.managers;


import net.valdemarf.parkourplugin.ParkourPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public final class ConfigManager {
    private final ParkourPlugin parkourPlugin;
    public FileConfiguration config;

    public ConfigManager(ParkourPlugin parkourPlugin) {
        this.parkourPlugin = parkourPlugin;

        config = parkourPlugin.getConfig();
    }


    public void instantiate() {
        setupConfig();

        new BukkitRunnable() {
            @Override
            public void run() {
                saveConfig();
            }
        }.runTaskTimerAsynchronously(parkourPlugin, 1, 1200);
    }

    public void setupConfig() {
        config.options().copyDefaults(true);
        parkourPlugin.saveDefaultConfig();
    }

    public void saveConfig() {
        config.options().copyDefaults();
        parkourPlugin.saveConfig();
    }

    public String getString(String string) {
        return config.getString(string);
    }
}

