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
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.PlayerData;
import net.milkbowl.vault.economy.Economy;

public class StimulusCommand implements CommandExecutor
{
    private final StimulusPackagePlugin plugin;
    private final Economy economy;
    private final StimulusPackageConfiguration config;

    public StimulusCommand( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
        this.economy = this.plugin.getEconomy();
        this.config = this.plugin.getConfiguration();
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        sender.sendMessage( "Making stimulus payments..." );

        // current time
        long now = new Date().getTime();

        // stimulus log file
        Path logFile = plugin.getLogFile( "Stimulus", now );
        StimulusUtil.appendToFile( logFile, "------------------------------------------------------------" );
        StimulusUtil.appendToFile( logFile, String.format( "Time: %tF %<tT.%<tL", now ));
        StimulusUtil.appendToFile( logFile, "" );

        // get information on all players
        Map< UUID, OfflinePlayer > offlinePlayerMap = new HashMap< UUID, OfflinePlayer >();
        Map< UUID, Long > playerTimeMap = new HashMap< UUID, Long >();
        Map< UUID, Double > playerWealthMap = new HashMap< UUID, Double >();
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for ( OfflinePlayer player : offlinePlayers )
        {
            // store OfflinePlayer instance
            offlinePlayerMap.put( player.getUniqueId(), player );

            // store number of seconds since player was last on
            playerTimeMap.put( player.getUniqueId(), ( now - player.getLastPlayed() ) / 1000 );

            // store the wealth of the player
            double balance = this.economy.getBalance( player );
            PlayerData playerData =
                    this.plugin.getGriefPrevention().dataStore.getPlayerData( player.getUniqueId() );
            double accruedClaimBlockValue = playerData.getAccruedClaimBlocks() * config.getClaimBlockValue();
            double bonusClaimBlockValue = playerData.getBonusClaimBlocks() * config.getClaimBlockValue();
            double wealth = balance + accruedClaimBlockValue + bonusClaimBlockValue;
            playerWealthMap.put( player.getUniqueId(), wealth );
        }

        // get information on players that are online right now
        Collection< ? extends Player > onlinePlayers = Bukkit.getOnlinePlayers();
        Map< UUID, Player > onlinePlayerMap = new HashMap< UUID, Player >();
        for ( Player player : onlinePlayers )
        {
            // store player instance
            onlinePlayerMap.put( player.getUniqueId(), player );

            // player is on right now, so zero seconds since they were last on the server
            playerTimeMap.put( player.getUniqueId(), 0l );
        }

        // count active players
        int activeEconomicPlayerCount = 0;
        int activeStimulusPlayerCount = 0;
        Collection< UUID > activePlayers = new LinkedList< UUID >();
        Collection< UUID > activeStimulusPlayers = new LinkedList< UUID >();
        Map< UUID, String > playerNameMap = new HashMap< UUID, String >();
        for ( UUID playerId : playerTimeMap.keySet() )
        {
            Long loginInterval = playerTimeMap.get( playerId );
            if (( loginInterval < this.config.getEconomicInterval() ) ||
                ( loginInterval < this.config.getStimulusInterval() ))
            {
                activePlayers.add( playerId );
                playerNameMap.put( playerId, offlinePlayerMap.get( playerId ).getName() );

                if ( loginInterval < this.config.getEconomicInterval() )
                {
                    activeEconomicPlayerCount++;
                }
                if ( loginInterval < this.config.getStimulusInterval() )
                {
                    activeStimulusPlayers.add( playerId );
                    activeStimulusPlayerCount++;
                }
            }
        }

        // adjust player names to be the same length by adding padding on the right
        int nameLength = StimulusUtil.getMaxLength( playerNameMap.values() );
        for ( UUID playerId : playerNameMap.keySet() )
        {
            playerNameMap.put(
                    playerId, String.format( "%-" + nameLength + "s", playerNameMap.get( playerId )));
        }

        // log active players
        StimulusUtil.appendToFile( logFile, "Economic Players: " + activeEconomicPlayerCount +
                                   ", Stimulus Players: " + activeStimulusPlayerCount );

        Map< UUID, String > formattedPlayerTimeMap = new HashMap< UUID, String >();
        for ( UUID playerId : activePlayers )
        {
            formattedPlayerTimeMap.put( playerId, String.format( "%,d", playerTimeMap.get( playerId )));
        }

        int playerTimeLength = StimulusUtil.getMaxLength( formattedPlayerTimeMap.values() );
        TreeSet< SortedLine< Long >> playerTimeOutput = new TreeSet< SortedLine< Long >>();
        for ( UUID playerId : activePlayers )
        {
            String name = playerNameMap.get( playerId );
            String rawTime = formattedPlayerTimeMap.get( playerId );
            String time = String.format( "%" + playerTimeLength + "s", rawTime );
            String line = "    " + name + " - " + playerId + " - " + time;
            playerTimeOutput.add( new SortedLine< Long >( playerTimeMap.get( playerId ), line ));
        }

        for ( SortedLine< Long > line : playerTimeOutput )
        {
            StimulusUtil.appendToFile( logFile, line.line );
        }

        // perform volume calculations
        double actualVolume = this.plugin.getActualVolume( now );
        double totalDesiredVolume = this.config.getDesiredVolume() * activeEconomicPlayerCount;
        double volumeDelta = totalDesiredVolume - actualVolume;

        String formattedTotalDesiredVolume = this.economy.format( totalDesiredVolume );
        String formattedActualVolume       = this.economy.format( actualVolume       );
        String formattedVolumeDelta        = this.economy.format( volumeDelta        );
        int volumeLength = StimulusUtil.getMaxLength(
                formattedTotalDesiredVolume, formattedActualVolume, formattedVolumeDelta );
        StimulusUtil.appendToFile( logFile, "" );
        StimulusUtil.appendToFile( logFile,
                String.format( "Desired Volume : %" + volumeLength + "s", formattedTotalDesiredVolume ));
        StimulusUtil.appendToFile( logFile,
                String.format( "Actual Volume  : %" + volumeLength + "s", formattedActualVolume ));
        StimulusUtil.appendToFile( logFile,
                String.format( "Volume Delta   : %" + volumeLength + "s", formattedVolumeDelta ));

        // make payments if economic volume is below our desired volume and there are active stimulus players
        if (( volumeDelta > 0 ) && ( activeStimulusPlayerCount > 0 ))
        {
            // compute total stimulus
            double stimulusFactor = volumeDelta / totalDesiredVolume;
            double totalStimulus =
                    stimulusFactor * this.config.getDesiredStimulus() * activeStimulusPlayerCount;
            StimulusUtil.appendToFile( logFile, "" );
            StimulusUtil.appendToFile( logFile, "Stimulus Factor : " + stimulusFactor );
            StimulusUtil.appendToFile( logFile, "Total Stimulus  : " + totalStimulus  );

            // determine the wealth of the wealthiest and poorest players
            double highestWealth = Double.NEGATIVE_INFINITY;
            double lowestWealth = Double.POSITIVE_INFINITY;
            for ( UUID playerId : activeStimulusPlayers )
            {
                // add the player to the active wealth top set
                double wealth = playerWealthMap.get( playerId );

                // adjust highest and lowest wealth
                if ( wealth > highestWealth ) highestWealth = wealth;
                if ( wealth < lowestWealth  ) lowestWealth  = wealth;
            }

            double wealthDelta = highestWealth - lowestWealth;
            String formattedHighestWealth = this.economy.format( highestWealth );
            String formattedLowestWealth  = this.economy.format( lowestWealth  );
            String formattedWealthDelta   = this.economy.format( wealthDelta   );
            int wealthLength = StimulusUtil.getMaxLength(
                    formattedHighestWealth, formattedLowestWealth, formattedWealthDelta );
            StimulusUtil.appendToFile( logFile, "" );
            StimulusUtil.appendToFile(
                    logFile, String.format( "Highest Wealth : %" + wealthLength + "s", formattedHighestWealth ));
            StimulusUtil.appendToFile(
                    logFile, String.format( "Lowest Wealth  : %" + wealthLength + "s", formattedLowestWealth ));
            StimulusUtil.appendToFile(
                    logFile, String.format( "Wealth Delta   : %" + wealthLength + "s", formattedWealthDelta ));

            Map< UUID, String > formattedPlayerWealthMap = new HashMap< UUID, String >();
            for ( UUID playerId : activeStimulusPlayers )
            {
                formattedPlayerWealthMap.put( playerId, this.economy.format( playerWealthMap.get( playerId )));
            }

            int playerWealthLength = StimulusUtil.getMaxLength( formattedPlayerWealthMap.values() );
            TreeSet< SortedLine< Double >> playerWealthOutput = new TreeSet< SortedLine< Double >>();
            for ( UUID playerId : activeStimulusPlayers )
            {
                String name = playerNameMap.get( playerId );
                String rawWealth = formattedPlayerWealthMap.get( playerId );
                String wealth = String.format( "%" + playerWealthLength + "s", rawWealth );
                String line = "    " + name + " - " + playerId + " - " + wealth;
                playerWealthOutput.add( new SortedLine< Double >( playerWealthMap.get( playerId ), line ));
            }

            for ( SortedLine< Double > line : playerWealthOutput.descendingSet() )
            {
                StimulusUtil.appendToFile( logFile, line.line );
            }

            StimulusUtil.appendToFile( logFile, "" );
            StimulusUtil.appendToFile( logFile, "Raw Player Payment Factors:" );

            // map players to payment factors
            double paymentFactorSum = 0;
            Map< UUID, Double > playerPaymentFactorMap = new HashMap< UUID, Double >();
            TreeSet< SortedLine< Double >> playerRawPaymentFactorOutput = new TreeSet< SortedLine< Double >>();
            for ( UUID playerId : activeStimulusPlayers )
            {
                double rawPaymentFactor = 0;
                double paymentFactor = 0;

                if ( highestWealth == lowestWealth )
                {
                    rawPaymentFactor = 1;
                    paymentFactor = 1;
                }
                else
                {
                    double playerOffset = playerWealthMap.get( playerId ) - lowestWealth;
                    rawPaymentFactor = 1 - ( playerOffset / wealthDelta );
                    paymentFactor = (( 1 - config.getMinimumPaymentFactor() ) * rawPaymentFactor ) +
                                    config.getMinimumPaymentFactor();
                }

                paymentFactorSum += paymentFactor;
                playerPaymentFactorMap.put( playerId, paymentFactor );
                String name = playerNameMap.get( playerId );
                String line = "    " + name + " - " + playerId + " - " + rawPaymentFactor;
                playerRawPaymentFactorOutput.add(
                        new SortedLine< Double >( playerWealthMap.get( playerId ), line ));
            }

            for ( SortedLine< Double > line : playerRawPaymentFactorOutput.descendingSet() )
            {
                StimulusUtil.appendToFile( logFile, line.line );
            }

            StimulusUtil.appendToFile( logFile, "" );
            StimulusUtil.appendToFile( logFile, "Player Payment Factor Sum: " + paymentFactorSum );

            TreeSet< SortedLine< Double >> playerPaymentFactorOutput = new TreeSet< SortedLine< Double >>();
            for ( UUID playerId : activeStimulusPlayers )
            {
                String name = playerNameMap.get( playerId );
                double paymentFactor = playerPaymentFactorMap.get( playerId );
                String line = "    " + name + " - " + playerId + " - " + paymentFactor;
                playerPaymentFactorOutput.add( new SortedLine< Double >( playerWealthMap.get( playerId ), line ));
            }

            for ( SortedLine< Double > line : playerPaymentFactorOutput.descendingSet() )
            {
                StimulusUtil.appendToFile( logFile, line.line );
            }

            // compute the payment for each player
            Map< UUID, Double > playerPaymentMap = new HashMap< UUID, Double >();
            for ( UUID playerId : activeStimulusPlayers )
            {
                double adjustedPaymentFactor = playerPaymentFactorMap.get( playerId ) / paymentFactorSum;
                double playerPayment = adjustedPaymentFactor * totalStimulus;
                playerPaymentMap.put( playerId, playerPayment );
            }

            StimulusUtil.appendToFile( logFile, "" );
            StimulusUtil.appendToFile( logFile, "Player Payments:" );
            Map< UUID, String > formattedPlayerPaymentMap = new HashMap< UUID, String >();

            for ( UUID playerId : activeStimulusPlayers )
            {
                formattedPlayerPaymentMap.put( playerId, this.economy.format( playerPaymentMap.get( playerId )));
            }

            int playerPaymentLength = StimulusUtil.getMaxLength( formattedPlayerPaymentMap.values() );
            TreeSet< SortedLine< Double >> playerPaymentOutput = new TreeSet< SortedLine< Double >>();
            for ( UUID playerId : activeStimulusPlayers )
            {
                String name = playerNameMap.get( playerId );
                String rawPayment = formattedPlayerPaymentMap.get( playerId );
                String payment = String.format( "%" + playerPaymentLength + "s", rawPayment );
                String line = "    " + name + " - " + playerId + " - " + payment;
                playerPaymentOutput.add( new SortedLine< Double >( playerWealthMap.get( playerId ), line ));
            }

            for ( SortedLine< Double > line : playerPaymentOutput.descendingSet() )
            {
                StimulusUtil.appendToFile( logFile, line.line );
            }

            // make stimulus payments
            for ( UUID playerId : activeStimulusPlayers )
            {
                double rawPayment = playerPaymentMap.get( playerId );
                double payment = StimulusUtil.round( economy.fractionalDigits(), rawPayment );
                if ( payment > 0 )
                {
                    this.economy.depositPlayer( offlinePlayerMap.get( playerId ), payment );
                    playerWealthMap.put( playerId, playerWealthMap.get( playerId ) + payment );
                    Player player = onlinePlayerMap.get( playerId );
                    if ( player != null )
                    {
                        player.sendMessage(
                                "§3You recieved §d" + this.economy.format( payment ) + "§3 in stimulus!" );
                    }
                    else
                    {
                        this.plugin.addOfflineStimulus( playerId, payment );
                    }
                }
            }
        }

        // clear wealth top data structures
        this.plugin.getActiveWealthTop().clear();
        this.plugin.getAllWealthTop().clear();

        // populate wealth top data structure for all players
        for ( OfflinePlayer player : offlinePlayerMap.values() )
        {
            double wealth = playerWealthMap.get( player.getUniqueId() );
            String playerName = player.getName();
            this.plugin.getAllWealthTop().add( new SortedLine< Double >( wealth, playerName ));
        }

        // populate wealth top data structure for active players
        for ( UUID playerId : activeStimulusPlayers )
        {
            double wealth = playerWealthMap.get( playerId );
            String playerName = offlinePlayerMap.get( playerId ).getName();
            this.plugin.getActiveWealthTop().add( new SortedLine< Double >( wealth, playerName ));
        }

        this.plugin.saveData();
        return true;
    }
}
