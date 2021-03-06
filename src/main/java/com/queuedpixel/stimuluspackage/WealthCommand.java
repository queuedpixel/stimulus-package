/*

stimulus-package : Give money to players based on economic activity.

Copyright (c) 2018-2021 Queued Pixel <git@queuedpixel.com>

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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.milkbowl.vault.economy.Economy;

import java.util.UUID;

public class WealthCommand implements CommandExecutor
{
    private final StimulusPackagePlugin plugin;
    private final Economy economy;
    private final GriefPrevention griefPrevention;
    private final double claimBlockValue;

    WealthCommand( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.griefPrevention = plugin.getGriefPrevention();
        this.claimBlockValue = plugin.getConfig().getDouble( "claimBlockValue" );
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        String prefix = ChatColor.GREEN + "[" + ChatColor.DARK_GREEN + "Wealth" + ChatColor.GREEN +"] ";

        if (( !( sender instanceof Player )) && ( args.length == 0 ))
        {
            sender.sendMessage( prefix + ChatColor.RED +
                                "You must specify a player name when using this command from the console." );
            return false;
        }

        OfflinePlayer player;

        if ( args.length > 0 )
        {
            String playerName = args[ 0 ];
            UUID playerId = plugin.getPlayerId( playerName );

            if ( playerId == null )
            {
                sender.sendMessage( prefix + ChatColor.RED + "Player not found." );
                return false;
            }

            player = Bukkit.getOfflinePlayer( playerId );
            sender.sendMessage( prefix + ChatColor.DARK_AQUA + "Player Name: " + ChatColor.AQUA + player.getName() );
        }
        else
        {
            player = (OfflinePlayer) sender;
        }

        double balance = this.economy.getBalance( player );
        PlayerData playerData = this.griefPrevention.dataStore.getPlayerData( player.getUniqueId() );
        int totalClaimBlocks = playerData.getAccruedClaimBlocks() + playerData.getBonusClaimBlocks();
        double totalClaimBlockValue = totalClaimBlocks * this.claimBlockValue;
        double totalWealth = balance + totalClaimBlockValue;

        sender.sendMessage( prefix + ChatColor.DARK_AQUA + "Economy Balance: " +
                            ChatColor.AQUA + this.economy.format( balance ));
        sender.sendMessage( prefix + ChatColor.DARK_AQUA + "Claim Block Value: " +
                            ChatColor.AQUA + this.economy.format( totalClaimBlockValue ) +
                            ChatColor.LIGHT_PURPLE + " (" + String.format( "%,d", totalClaimBlocks ) + " blocks)" );
        sender.sendMessage( prefix + ChatColor.GOLD + "Total Wealth: " +
                            ChatColor.YELLOW + this.economy.format( totalWealth ));

        return true;
    }
}
