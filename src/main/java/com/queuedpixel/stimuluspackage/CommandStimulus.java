package com.queuedpixel.stimuluspackage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandStimulus implements CommandExecutor
{
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        OfflinePlayer[] players = Bukkit.getOfflinePlayers();
        sender.sendMessage( "There are " + players.length + " players." );
        return true;
    }
}
