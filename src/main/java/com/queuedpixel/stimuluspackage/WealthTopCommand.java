package com.queuedpixel.stimuluspackage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WealthTopCommand implements CommandExecutor
{
    private final StimulusPackagePlugin plugin;
    
    public WealthTopCommand( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        int index = 0;
        for ( SortedLine< Double > line : this.plugin.getWealthTop().descendingSet() )
        {
            index++;
            sender.sendMessage( "§a[§2Wealth§a] §e[§6" + index + "§e] " + line.line );
        }

        return true;
    }
}
