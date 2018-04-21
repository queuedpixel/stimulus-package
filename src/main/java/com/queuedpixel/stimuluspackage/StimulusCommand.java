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
import java.util.LinkedList;
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

        // count active players
        int activeEconomicPlayers = 0;
        int activeStimulusPlayers = 0;
        Collection< UUID > activePlayers = new LinkedList< UUID >();

        for ( UUID playerId : playerMap.keySet() )
        {
            Long loginInterval = playerMap.get( playerId );
            if (( loginInterval < this.config.getEconomicInterval() ) ||
                ( loginInterval < this.config.getStimulusInterval() ))
            {
                activePlayers.add( playerId );
                if ( loginInterval < this.config.getEconomicInterval() ) activeEconomicPlayers++;
                if ( loginInterval < this.config.getStimulusInterval() ) activeStimulusPlayers++;
            }
        }

        // log active players
        plugin.appendToFile( logFile, "Economic Players: " + activeEconomicPlayers +
                             ", Stimulus Players: " + activeStimulusPlayers );

        for ( UUID playerId : playerMap.keySet() )
        {
            plugin.appendToFile(
                    logFile, String.format( "    %s - %,d", playerId, playerMap.get( playerId )));
        }

        // perform volume calculations
        double actualVolume = this.plugin.getActualVolume( now );
        double totalDesiredVolume = this.config.getDesiredVolume() * activeEconomicPlayers;
        double volumeDelta = totalDesiredVolume - actualVolume;

        String formattedTotalDesiredVolume = this.plugin.getEconomy().format( totalDesiredVolume );
        String formattedActualVolume       = this.plugin.getEconomy().format( actualVolume       );
        String formattedVolumeDelta        = this.plugin.getEconomy().format( volumeDelta        );
        int volumeLength = StimulusUtil.getMaxLength(
                formattedTotalDesiredVolume, formattedActualVolume, formattedVolumeDelta );
        plugin.appendToFile( logFile,
                String.format( "Desired Volume : %" + volumeLength + "s", formattedTotalDesiredVolume ));
        plugin.appendToFile( logFile,
                String.format( "Actual Volume  : %" + volumeLength + "s", formattedActualVolume ));
        plugin.appendToFile( logFile,
                String.format( "Volume Delta   : %" + volumeLength + "s", formattedVolumeDelta ));

        if (( volumeDelta <= 0 ) || ( activeStimulusPlayers == 0 )) return true;

        // compute total stimulus
        double stimulusFactor = volumeDelta / totalDesiredVolume;
        double totalStimulus = stimulusFactor * this.config.getDesiredStimulus() * activeStimulusPlayers;
        plugin.appendToFile( logFile, "Stimulus Factor : " + stimulusFactor );
        plugin.appendToFile( logFile, "Total Stimulus  : " + totalStimulus  );

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

        String formattedHighestWealth = this.plugin.getEconomy().format( highestWealth );
        String formattedLowestWealth  = this.plugin.getEconomy().format( lowestWealth  );
        String formattedWealthDelta   = this.plugin.getEconomy().format( wealthDelta   );
        int wealthLength = StimulusUtil.getMaxLength(
                formattedHighestWealth, formattedLowestWealth, formattedWealthDelta );
        plugin.appendToFile(
                logFile, String.format( "Highest Wealth : %" + wealthLength + "s", formattedHighestWealth ));
        plugin.appendToFile(
                logFile, String.format( "Lowest Wealth  : %" + wealthLength + "s", formattedLowestWealth ));
        plugin.appendToFile(
                logFile, String.format( "Wealth Delta   : %" + wealthLength + "s", formattedWealthDelta ));

        LinkedList< String > playerWealth = new LinkedList< String >();

        for ( UUID playerId : playerWealthMap.keySet() )
        {
            playerWealth.add( this.plugin.getEconomy().format( playerWealthMap.get( playerId )));
        }

        int playerWealthLength = StimulusUtil.getMaxLength( playerWealth );

        for ( UUID playerId : playerWealthMap.keySet() )
        {
            String wealth = this.plugin.getEconomy().format( playerWealthMap.get( playerId ));
            plugin.appendToFile( logFile,
                    String.format( "    %s - %" + playerWealthLength + "s", playerId, wealth ));
        }

        plugin.appendToFile( logFile, "Raw Player Payment Factors:" );

        // map players to payment factors
        Map< UUID, Double > playerPaymentFactorMap = new HashMap< UUID, Double >();
        double paymentFactorSum = 0;
        double rawPaymentFactor = 0;
        double paymentFactor = 0;
        for ( OfflinePlayer player : offlinePlayers )
        {
            // skip players who are not active stimulus players
            long loginInterval = playerMap.get( player.getUniqueId() );
            if ( loginInterval >= this.config.getStimulusInterval() ) continue;

            if ( highestWealth == lowestWealth )
            {
                rawPaymentFactor = 1;
                paymentFactor = 1;
            }
            else
            {
                double playerOffset = playerWealthMap.get( player.getUniqueId() ) - lowestWealth;
                rawPaymentFactor = 1 - ( playerOffset / wealthDelta );
                paymentFactor = (( 1 - config.getMinimumPaymentFactor() ) * rawPaymentFactor ) +
                                config.getMinimumPaymentFactor();
            }

            paymentFactorSum += paymentFactor;
            playerPaymentFactorMap.put( player.getUniqueId(), paymentFactor );
            plugin.appendToFile( logFile, "    " + player.getUniqueId() + " - " + rawPaymentFactor );
        }

        plugin.appendToFile( logFile, "Player Payment Factor Sum: " + paymentFactorSum );

        for ( UUID playerId : playerPaymentFactorMap.keySet() )
        {
            plugin.appendToFile( logFile, "    " + playerId + " - " + playerPaymentFactorMap.get( playerId ));
        }

        // compute the payment for each player
        Map< UUID, Double > playerPaymentMap = new HashMap< UUID, Double >();
        for ( OfflinePlayer player : offlinePlayers )
        {
            // skip players who are not active stimulus players
            long loginInterval = playerMap.get( player.getUniqueId() );
            if ( loginInterval >= this.config.getStimulusInterval() ) continue;

            double adjustedPaymentFactor =
                    playerPaymentFactorMap.get( player.getUniqueId() ) / paymentFactorSum;
            double playerPayment = adjustedPaymentFactor * totalStimulus;
            playerPaymentMap.put( player.getUniqueId(), playerPayment );
        }

        plugin.appendToFile( logFile, "Player Payments:" );
        LinkedList< String > playerPayments = new LinkedList< String >();

        for ( UUID playerId : playerPaymentMap.keySet() )
        {
            playerPayments.add( this.plugin.getEconomy().format( playerPaymentMap.get( playerId )));
        }

        int playerPaymentLength = StimulusUtil.getMaxLength( playerPayments );

        for ( UUID playerId : playerPaymentMap.keySet() )
        {
            String payment = this.plugin.getEconomy().format( playerPaymentMap.get( playerId ));
            plugin.appendToFile( logFile,
                    String.format( "    %s - %" + playerPaymentLength + "s", playerId, payment ));
        }

        return true;
    }
}
