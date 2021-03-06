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

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LogBalancesTask extends BukkitRunnable
{
    private final StimulusPackagePlugin plugin;
    private final Economy economy;
    private final Map< UUID, Double > playerBalanceMap = new HashMap<>();

    public LogBalancesTask( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
        this.economy = this.plugin.getEconomy();
    }

    public void run()
    {
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for ( OfflinePlayer player : offlinePlayers )
        {
            UUID playerId = player.getUniqueId();
            Double pastBalance = this.playerBalanceMap.get( playerId );
            double currentBalance = this.economy.getBalance( player );
            if (( pastBalance == null ) || ( pastBalance != currentBalance ))
            {
                this.playerBalanceMap.put( playerId, currentBalance );
                if ( pastBalance != null )
                {
                    long timestamp = new Date().getTime();
                    int fractionalDigits = this.economy.fractionalDigits();
                    String currencyFormat = ( fractionalDigits > -1 ) ? "%." + fractionalDigits + "f" : "%f";
                    double balanceDelta = currentBalance - pastBalance;
                    String logEntry = String.format(
                            "%tF %<tT.%<tL, %s [%s], " + currencyFormat + ", " + currencyFormat + ", " + currencyFormat,
                            timestamp, playerId, player.getName(), pastBalance, currentBalance, balanceDelta );
                    StimulusUtil.appendToFile( plugin.getLogFile( "Balances", timestamp ), logEntry );
                }
            }
        }
    }
}
