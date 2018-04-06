package com.queuedpixel.stimuluspackage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandStimulus implements CommandExecutor
{
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        sender.sendMessage( "Hello!" );
        return true;
    }
}
