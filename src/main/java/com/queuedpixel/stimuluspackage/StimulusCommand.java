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

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StimulusCommand implements CommandExecutor
{
    private final StimulusPackagePlugin plugin;

    StimulusCommand( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        UUID playerId = null;

        if ( args.length > 0 )
        {
            String playerName = args[ 0 ];
            playerId = plugin.getPlayerId( playerName );

            if ( playerId == null )
            {
                sender.sendMessage( this.plugin.messagePrefix + ChatColor.RED + "Player not found." );
                return false;
            }
        }
        else if ( sender instanceof Player )
        {
            OfflinePlayer player = (OfflinePlayer) sender;
            playerId = player.getUniqueId();
        }

        StimulusInformation stimulusInformation = this.plugin.getStimulusInformation();
        if ( stimulusInformation == null )
        {
            sender.sendMessage( this.plugin.messagePrefix + ChatColor.GOLD +
                                "Stimulus information is not available yet." );
            return true;
        }

        sender.sendMessage(
                this.plugin.messagePrefix + ChatColor.GOLD + "Economic Players: " +
                ChatColor.YELLOW + String.format( "%,d", stimulusInformation.economicPlayers ));
        sender.sendMessage(
                this.plugin.messagePrefix + ChatColor.DARK_AQUA + "Maximum Economic Activity: " +
                ChatColor.AQUA + this.plugin.getEconomy().format( stimulusInformation.maximumEconomicActivity ));
        sender.sendMessage(
                this.plugin.messagePrefix + ChatColor.DARK_AQUA + "Actual Economic Activity: " +
                ChatColor.AQUA + this.plugin.getEconomy().format( stimulusInformation.actualEconomicActivity ));
        sender.sendMessage(
                this.plugin.messagePrefix + ChatColor.DARK_AQUA + "Economic Activity Delta: " +
                ChatColor.AQUA + this.plugin.getEconomy().format( stimulusInformation.economicActivityDelta ));
        sender.sendMessage(
                this.plugin.messagePrefix + ChatColor.GOLD + "Stimulus Players: " +
                ChatColor.YELLOW + String.format( "%,d", stimulusInformation.stimulusPlayers ));
        sender.sendMessage(
                this.plugin.messagePrefix + ChatColor.DARK_AQUA + "Maximum Stimulus: " +
                ChatColor.AQUA + this.plugin.getEconomy().format( stimulusInformation.maximumStimulus ));
        sender.sendMessage(
                this.plugin.messagePrefix + ChatColor.DARK_AQUA + "Available Stimulus: " +
                ChatColor.AQUA + this.plugin.getEconomy().format( stimulusInformation.availableStimulus ) +
                ChatColor.LIGHT_PURPLE +
                " (" + String.format( "%.3f", stimulusInformation.economicActivityFactor * 100 ) + "%)" );

        if ( playerId == null ) return true;

        StimulusPlayerInformation stimulusPlayerInformation =
                stimulusInformation.stimulusPlayerInformationMap.get( playerId );

        if ( stimulusPlayerInformation == null )
        {
            sender.sendMessage( this.plugin.messagePrefix + ChatColor.GOLD + "No stimulus payment." );
            return true;
        }

        sender.sendMessage(
                this.plugin.messagePrefix + ChatColor.DARK_AQUA + "Stimulus Payment: " +
                ChatColor.AQUA + this.plugin.getEconomy().format( stimulusPlayerInformation.stimulusPayment ) +
                ChatColor.LIGHT_PURPLE +
                " (" + String.format( "%.3f", stimulusPlayerInformation.stimulusFactor * 100 ) + "%)" );
        return true;
    }
}
