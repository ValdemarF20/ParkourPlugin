package net.valdemarf.parkourplugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import net.valdemarf.parkourplugin.ParkourPlugin;
import org.bukkit.entity.Player;

@CommandAlias("parkour|par|p")
public final class Parkour extends BaseCommand {
    private final ParkourPlugin parkourPlugin;

    public Parkour(ParkourPlugin parkourPlugin) {
        this.parkourPlugin = parkourPlugin;
    }

    /**
     * Restarts the players parkour checkpoint and TPs back to start
     * @param player The player sending the command
     */
    @Subcommand("restart")
    public void onRestart(Player player) {
        player.teleport(parkourPlugin.getCheckpointLocations().get(0).clone().add(0.5, 0, 0.5));
        parkourPlugin.getCheckpointManager().setCheckpoint(player, 0);
        parkourPlugin.checker = false;
    }


    /**
     * Teleports the player to the latest checkpoint
     * @param player The player sending the command
     */
    @Subcommand("respawn|res")
    public void onRespawn(Player player) {
        player.teleport(parkourPlugin.getCheckpointManager().getPlayerCheckpoint(player).clone().add(0.5, 0, 0.5));
    }
}
