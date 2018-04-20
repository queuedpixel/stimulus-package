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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.PlayerData;

public class StimulusCommand implements CommandExecutor
{
    private final StimulusPackagePlugin plugin;
    private final StimulusPackageConfiguration config;

    public StimulusCommand( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
        this.config = this.plugin.getConfiguration();
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        sender.sendMessage( "Making stimulus payments..." );

        // current time
        long now = new Date().getTime();

        // stimulus log file
        Path logFile = plugin.getLogFile( "Stimulus", now );
        plugin.appendToFile( logFile, "------------------------------------------------------------" );
        plugin.appendToFile( logFile, String.format( "Time: %tF %<tT.%<tL", now ));

        // map of players to the number of seconds since they were last on the server
        Map< UUID, Long > playerMap = new HashMap< UUID, Long >();

        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for ( OfflinePlayer player : offlinePlayers )
        {
            // store number of seconds since player was last on
            playerMap.put( player.getUniqueId(), ( now - player.getLastPlayed() ) / 1000 );
        }

        Collection< ? extends Player > onlinePlayers = Bukkit.getOnlinePlayers();
        for ( Player player : onlinePlayers )
        {
            // player is on right now, so zero seconds since they were last on the server
            playerMap.put( player.getUniqueId(), 0l );
        }

        int activeEconomicPlayers = 0;
        int activeStimulusPlayers = 0;

        for ( Long loginInterval : playerMap.values() )
        {
            if ( loginInterval < this.config.getEconomicInterval() ) activeEconomicPlayers++;
            if ( loginInterval < this.config.getStimulusInterval() ) activeStimulusPlayers++;
        }

        // perform volume calculations
        double actualVolume = this.plugin.getActualVolume( now );
        double totalDesiredVolume = this.config.getDesiredVolume() * activeEconomicPlayers;
        double volumeDelta = totalDesiredVolume - actualVolume;

        plugin.appendToFile( logFile, "Economic Players: " + activeEconomicPlayers +
                             ", Stimulus Players: " + activeStimulusPlayers );
        plugin.appendToFile( logFile, "Desired Volume: " + String.format( "%.2f", totalDesiredVolume ) +
                             ", Actual Volume: " + String.format( "%.2f", actualVolume ) +
                             ", Delta: " + String.format( "%.2f", volumeDelta ));

        if (( volumeDelta <= 0 ) || ( activeStimulusPlayers == 0 )) return true;

        // compute total stimulus
        double stimulusFactor = volumeDelta / totalDesiredVolume;
        double totalStimulus = stimulusFactor * this.config.getDesiredStimulus() * activeStimulusPlayers;
        plugin.appendToFile( logFile, "Stimulus Factor: " + String.format( "%.2f", stimulusFactor ) +
                             ", Total Stimulus: " + String.format( "%.2f", totalStimulus ));

        // determine the wealth of the wealthiest and poorest players
        Map< UUID, Double > playerWealthMap = new HashMap< UUID, Double >();
        double highestWealth = Double.NEGATIVE_INFINITY;
        double lowestWealth = Double.POSITIVE_INFINITY;
        for ( OfflinePlayer player : offlinePlayers )
        {
            // skip players who are not active stimulus players
            long loginInterval = playerMap.get( player.getUniqueId() );
            if ( loginInterval >= this.config.getStimulusInterval() ) continue;

            // determine the wealth of the player
            double balance = this.plugin.getEconomy().getBalance( player );
            PlayerData playerData =
                    this.plugin.getGriefPrevention().dataStore.getPlayerData( player.getUniqueId() );
            double accruedClaimBlockValue = playerData.getAccruedClaimBlocks() * config.getClaimBlockValue();
            double bonusClaimBlockValue = playerData.getBonusClaimBlocks() * config.getClaimBlockValue();
            double wealth = balance + accruedClaimBlockValue + bonusClaimBlockValue;
            playerWealthMap.put( player.getUniqueId(), wealth );

            // adjust highest and lowest wealth
            if ( wealth > highestWealth ) highestWealth = wealth;
            if ( wealth < lowestWealth  ) lowestWealth  = wealth;
        }

        double wealthDelta = highestWealth - lowestWealth;
        plugin.appendToFile( logFile, "Highest Wealth: " + String.format( "%.2f", highestWealth ) +
                             ", Lowest Wealth: " + String.format( "%.2f", lowestWealth ) +
                             ", Wealth Delta: " + String.format( "%.2f", wealthDelta ));
        plugin.appendToFile( logFile, "Player Payment Factors:" );

        // map players to payment factors
        Map< UUID, Double > playerPaymentFactorMap = new HashMap< UUID, Double >();
        double paymentFactorSum = 0;
        for ( OfflinePlayer player : offlinePlayers )
        {
            // skip players who are not active stimulus players
            long loginInterval = playerMap.get( player.getUniqueId() );
            if ( loginInterval >= this.config.getStimulusInterval() ) continue;

            if ( highestWealth == lowestWealth )
            {
                playerPaymentFactorMap.put( player.getUniqueId(), 1.0 );
            }
            else
            {
                double playerOffset = playerWealthMap.get( player.getUniqueId() ) - lowestWealth;
                double rawPaymentFactor = 1 - ( playerOffset / wealthDelta );
                double paymentFactor = (( 1 - config.getMinimumPaymentFactor() ) * rawPaymentFactor ) +
                                       config.getMinimumPaymentFactor();
                playerPaymentFactorMap.put( player.getUniqueId(), paymentFactor );
            }

            paymentFactorSum += playerPaymentFactorMap.get( player.getUniqueId() );
            plugin.appendToFile( logFile, "    " + player.getUniqueId() + " - " +
                                 String.format( "%.2f", playerPaymentFactorMap.get( player.getUniqueId() )));
        }

        plugin.appendToFile( logFile, "Sum: " + String.format( "%.2f", paymentFactorSum ));
        plugin.appendToFile( logFile, "Player Payments:" );

        // compute the payment for each player
        for ( OfflinePlayer player : offlinePlayers )
        {
            // skip players who are not active stimulus players
            long loginInterval = playerMap.get( player.getUniqueId() );
            if ( loginInterval >= this.config.getStimulusInterval() ) continue;

            double adjustedPaymentFactor =
                    playerPaymentFactorMap.get( player.getUniqueId() ) / paymentFactorSum;
            double playerPayment = adjustedPaymentFactor * totalStimulus;
            plugin.appendToFile( logFile, "    " + player.getUniqueId() + " - " +
                                 String.format( "%.2f", playerPayment ));
        }

        return true;
    }
}
