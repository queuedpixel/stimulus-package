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
    private final Economy economy;
    private final GriefPrevention griefPrevention;
    private final double claimBlockValue;

    public WealthCommand( StimulusPackagePlugin plugin )
    {
        this.economy = plugin.getEconomy();
        this.griefPrevention = plugin.getGriefPrevention();
        this.claimBlockValue = plugin.getConfig().getDouble( "claimBlockValue" );
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if ( !( sender instanceof Player ))
        {
            sender.sendMessage( "You are not a player." );
            return true;
        }

        OfflinePlayer player = (OfflinePlayer) sender;
        double balance = this.economy.getBalance( player );
        PlayerData playerData = this.griefPrevention.dataStore.getPlayerData( player.getUniqueId() );
        double accruedClaimBlockValue = playerData.getAccruedClaimBlocks() * this.claimBlockValue;
        double bonusClaimBlockValue = playerData.getBonusClaimBlocks() * this.claimBlockValue;
        double totalClaimBlockValue = accruedClaimBlockValue + bonusClaimBlockValue;
        double totalWealth = balance + totalClaimBlockValue;

        sender.sendMessage( "§aEconomy Balance: §c" + this.economy.format( balance ));
        sender.sendMessage( "§aClaim Block Value: §c" + this.economy.format( totalClaimBlockValue ));
        sender.sendMessage( "§3Total Wealth: §d" + this.economy.format( totalWealth ));

        return true;
    }
}
