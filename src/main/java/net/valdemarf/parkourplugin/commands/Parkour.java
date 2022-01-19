package net.valdemarf.parkourplugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.valdemarf.parkourplugin.ParkourPlugin;
import org.bukkit.entity.Player;

@CommandAlias("parkour|par|p")
public class Parkour extends BaseCommand {
    private final ParkourPlugin parkourPlugin;

    public Parkour(ParkourPlugin parkourPlugin) {
        this.parkourPlugin = parkourPlugin;
    }

    @Subcommand("restart")
    public void onRestart(Player player) {
        player.teleport(parkourPlugin.getCheckpointLocations().get(0).clone().add(0.5, 0, 0.5));
        parkourPlugin.getCheckpointManager().setCheckpoint(player, 0);
        parkourPlugin.checker = false;
    }

    @Subcommand("respawn|res")
    public void onRespawn(Player player) {
        player.teleport(parkourPlugin.getCheckpointManager().getPlayerCheckpoint(player).clone().add(0.5, 0, 0.5));
    }
}
