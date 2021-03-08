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
import org.bukkit.entity.Player;

import java.util.UUID;

public class PluginPaymentHandler implements PaymentHandler
{
    private final StimulusPackagePlugin plugin;

    public PluginPaymentHandler( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
    }

    public void handlePayment( UUID playerId, double payment )
    {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer( playerId );
        this.plugin.getEconomy().depositPlayer( offlinePlayer, payment );

        Player player = offlinePlayer.getPlayer();
        if ( player != null )
        {
            player.sendMessage( this.plugin.messagePrefix + ChatColor.DARK_AQUA + "You received " +
                                ChatColor.LIGHT_PURPLE + this.plugin.getEconomy().format( payment ) +
                                ChatColor.DARK_AQUA + " in stimulus!" );
        }
    }
}
