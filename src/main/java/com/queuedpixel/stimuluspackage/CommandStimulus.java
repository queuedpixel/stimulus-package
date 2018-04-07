package com.queuedpixel.stimuluspackage;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStimulus implements CommandExecutor
{
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        sender.sendMessage( offlinePlayers.length + " Offline Players:" );

        for ( OfflinePlayer player : offlinePlayers )
        {
            sender.sendMessage( player.getName() + " : " + player.getLastPlayed() );
        }

        Collection< ? extends Player > onlinePlayers = Bukkit.getOnlinePlayers();
        sender.sendMessage( onlinePlayers.size() + " Online Players:" );

        for ( Player player : onlinePlayers )
        {
            sender.sendMessage( player.getName() );
        }

        return true;
    }
}
