/*

stimulus-package : Give money to players based on economic activity.

Copyright (c) 2018 Queued Pixel <git@queuedpixel.com>

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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.milkbowl.vault.economy.Economy;

public class WealthCommand implements CommandExecutor
{
    private final StimulusPackageConfiguration config;
    private final Economy economy;

    public WealthCommand( StimulusPackagePlugin plugin )
    {
        this.config = plugin.getConfiguration();
        this.economy = plugin.getEconomy();
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if ( !( sender instanceof Player ))
        {
            sender.sendMessage( "You are not a player." );
            return true;
        }

        OfflinePlayer player = (OfflinePlayer) sender;
        sender.sendMessage( "You are a player." );

        double balance = this.economy.getBalance( player );
        sender.sendMessage( "Balance: " + this.economy.format( balance ));

        GriefPrevention griefPrevention =
                (GriefPrevention) Bukkit.getServer().getPluginManager().getPlugin( "GriefPrevention" );
        PlayerData playerData = griefPrevention.dataStore.getPlayerData( player.getUniqueId() );

        double accruedClaimBlockValue = playerData.getAccruedClaimBlocks() * config.getClaimBlockValue();
        sender.sendMessage( "Accrued Claim Block Value: " + this.economy.format( accruedClaimBlockValue ));

        double bonusClaimBlockValue = playerData.getBonusClaimBlocks() * config.getClaimBlockValue();
        sender.sendMessage( "Bonus Claim Block Value: " + this.economy.format( bonusClaimBlockValue ));
        
        double wealth = balance + accruedClaimBlockValue + bonusClaimBlockValue;
        sender.sendMessage( "Wealth: " + this.economy.format( wealth ));

        return true;
    }
}
