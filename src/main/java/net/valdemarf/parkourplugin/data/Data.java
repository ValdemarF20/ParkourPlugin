package net.valdemarf.parkourplugin.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Class is used to store the locations given in JSON file
 */
public final class Data {
    private String worldName;
    private int x;
    private int y;
    private int z;

    public Location bukkitLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }
}
