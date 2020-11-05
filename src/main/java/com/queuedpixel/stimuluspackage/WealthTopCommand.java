/*

stimulus-package : Give money to players based on economic activity.

Copyright (c) 2018-2020 Queued Pixel <git@queuedpixel.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

package com.queuedpixel.stimuluspackage;

import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WealthTopCommand implements CommandExecutor
{
    private final StimulusPackagePlugin plugin;

    WealthTopCommand( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        String prefix = ChatColor.GREEN + "[" + ChatColor.DARK_GREEN + "WealthTop" + ChatColor.GREEN +"] ";
        boolean allPlayers = false;
        int pageNum = 1;
        for ( String arg : args )
        {
            if ( arg.toLowerCase().equals( "all" ))
            {
                allPlayers = true;
            }
            else
            {
                try
                {
                    pageNum = Integer.parseInt( arg );
                }
                catch ( NumberFormatException e )
                {
                    sender.sendMessage( prefix + ChatColor.DARK_AQUA + "Invalid page number." );
                    return false;
                }
            }
        }

        if ( pageNum < 1 )
        {
            sender.sendMessage( prefix + ChatColor.DARK_AQUA + "Page number must be greater than zero." );
            return false;
        }

        String playerType = allPlayers ? "All" : "Active";
        sender.sendMessage( prefix + ChatColor.WHITE +
                            "Displaying " + playerType + " Players - Page " + pageNum + ":" );

        TreeSet< SortedLine< Double >> wealthSet =
                allPlayers ? this.plugin.getAllWealthTop() : this.plugin.getActiveWealthTop();
        int size = wealthSet.size();
        int length = Integer.toString( size ).length();

        if ( size - (( pageNum - 1 ) * 10 ) <= 0 )
        {
            sender.sendMessage( prefix + ChatColor.DARK_AQUA + "No players to display." );
            return true;
        }

        int index = 0;
        for ( SortedLine< Double > line : wealthSet.descendingSet() )
        {
            index++;

            // skip players until we reach the desired page
            if ( index <= ( pageNum - 1 ) * 10 ) continue;

            // stop output after we display 10 players
            if ( index > pageNum * 10 ) break;

            String indexString = ChatColor.YELLOW + "[" + ChatColor.GOLD +
                                 String.format( "%0" + length + "d", index ) +
                                 ChatColor.YELLOW + "]";
            String playerName = ChatColor.DARK_AQUA + line.line;
            String wealth = ChatColor.WHITE + "- " + ChatColor.LIGHT_PURPLE +
                            this.plugin.getEconomy().format( line.sortValue );
            sender.sendMessage( prefix + " " + indexString + " " + playerName + " " + wealth );
        }

        return true;
    }
}
