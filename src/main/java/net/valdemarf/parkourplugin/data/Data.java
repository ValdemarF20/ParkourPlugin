package net.valdemarf.parkourplugin.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Data {
    private String worldName;
    private int x;
    private int y;
    private int z;

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public Location bukkitLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }
}
