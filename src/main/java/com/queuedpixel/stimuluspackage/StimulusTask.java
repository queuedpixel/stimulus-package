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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.ryanhamshire.GriefPrevention.PlayerData;
import net.milkbowl.vault.economy.Economy;

public class StimulusTask extends BukkitRunnable
{
    private final StimulusPackagePlugin plugin;
    private final Economy economy;

    private final long   economicInterval;
    private final long   stimulusInterval;
    private final double desiredVolume;
    private final double desiredStimulus;
    private final double minimumPaymentFactor;
    private final double claimBlockValue;

    StimulusTask( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
        this.economy = this.plugin.getEconomy();

        this.economicInterval     = this.plugin.getConfig().getLong(   "economicInterval"     );
        this.stimulusInterval     = this.plugin.getConfig().getLong(   "stimulusInterval"     );
        this.desiredVolume        = this.plugin.getConfig().getDouble( "desiredVolume"        );
        this.desiredStimulus      = this.plugin.getConfig().getDouble( "desiredStimulus"      );
        this.minimumPaymentFactor = this.plugin.getConfig().getDouble( "minimumPaymentFactor" );
        this.claimBlockValue      = this.plugin.getConfig().getDouble( "claimBlockValue"      );
    }

    public void run()
    {
        String prefix = ChatColor.GREEN + "[" + ChatColor.DARK_GREEN + "Stimulus" + ChatColor.GREEN + "] ";

        // current time
        long now = new Date().getTime();

        // stimulus log file
        Path logFile = plugin.getLogFile( "Stimulus", now );
        if ( Files.exists( logFile )) StimulusUtil.appendToFile( logFile, "" );
        StimulusUtil.appendToFile( logFile, String.format( "Time: %tF %<tT.%<tL", now ));
        StimulusUtil.appendToFile( logFile, "" );

        // get information on all players
        Map< UUID, OfflinePlayer > offlinePlayerMap = new HashMap<>();
        Map< UUID, Long > playerTimeMap = new HashMap<>();
        Map< UUID, Double > playerWealthMap = new HashMap<>();
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
            double accruedClaimBlockValue = playerData.getAccruedClaimBlocks() * this.claimBlockValue;
            double bonusClaimBlockValue = playerData.getBonusClaimBlocks() * this.claimBlockValue;
            double wealth = balance + accruedClaimBlockValue + bonusClaimBlockValue;
            playerWealthMap.put( player.getUniqueId(), wealth );
        }

        // get information on players that are online right now
        Collection< ? extends Player > onlinePlayers = Bukkit.getOnlinePlayers();
        Map< UUID, Player > onlinePlayerMap = new HashMap<>();
        for ( Player player : onlinePlayers )
        {
            // store player instance
            onlinePlayerMap.put( player.getUniqueId(), player );

            // player is on right now, so zero seconds since they were last on the server
            playerTimeMap.put( player.getUniqueId(), 0L );
        }

        // count active players
        int activeEconomicPlayerCount = 0;
        int activeStimulusPlayerCount = 0;
        Collection< UUID > activePlayers = new LinkedList<>();
        Collection< UUID > activeStimulusPlayers = new LinkedList<>();
        Map< UUID, String > playerNameMap = new HashMap<>();
        for ( UUID playerId : playerTimeMap.keySet() )
        {
            Long loginInterval = playerTimeMap.get( playerId );
            if ((( loginInterval < this.economicInterval ) ||
                 ( loginInterval < this.stimulusInterval )) &&
                ( !this.plugin.getExcludedPlayers().contains( playerId )))
            {
                activePlayers.add( playerId );
                playerNameMap.put( playerId, offlinePlayerMap.get( playerId ).getName() );

                if ( loginInterval < this.economicInterval )
                {
                    activeEconomicPlayerCount++;
                }
                if ( loginInterval < this.stimulusInterval )
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
        StimulusUtil.appendToFile( logFile, "" );

        Map< UUID, String > formattedPlayerTimeMap = new HashMap<>();
        for ( UUID playerId : activePlayers )
        {
            formattedPlayerTimeMap.put( playerId, String.format( "%,d", playerTimeMap.get( playerId )));
        }

        int playerTimeLength = StimulusUtil.getMaxLength( formattedPlayerTimeMap.values() );
        TreeSet< SortedLine< Long >> playerTimeOutput = new TreeSet<>();
        for ( UUID playerId : activePlayers )
        {
            String name = playerNameMap.get( playerId );
            String rawTime = formattedPlayerTimeMap.get( playerId );
            String time = String.format( "%" + playerTimeLength + "s", rawTime );
            String line = name + " - " + playerId + " - " + time;
            playerTimeOutput.add( new SortedLine<>( playerTimeMap.get( playerId ), line ));
        }

        for ( SortedLine< Long > line : playerTimeOutput )
        {
            StimulusUtil.appendToFile( logFile, line.line );
        }

        // perform volume calculations
        double actualVolume = this.plugin.getActualVolume( now );
        double totalDesiredVolume = this.desiredVolume * activeEconomicPlayerCount;
        double volumeDelta = totalDesiredVolume - actualVolume;

        String formattedTotalDesiredVolume = this.economy.format( totalDesiredVolume );
        String formattedActualVolume       = this.economy.format( actualVolume       );
        String formattedVolumeDelta        = this.economy.format( volumeDelta        );
        int volumeLength = StimulusUtil.getMaxLength(
                formattedTotalDesiredVolume, formattedActualVolume, formattedVolumeDelta );
        StimulusUtil.appendToFile( logFile, "" );
        StimulusUtil.appendToFile( logFile,
                String.format( "Desired Volume  : %" + volumeLength + "s", formattedTotalDesiredVolume ));
        StimulusUtil.appendToFile( logFile,
                String.format( "Actual Volume   : %" + volumeLength + "s", formattedActualVolume ));
        StimulusUtil.appendToFile( logFile,
                String.format( "Volume Delta    : %" + volumeLength + "s", formattedVolumeDelta ));

        // make payments if economic volume is below our desired volume and there are active stimulus players
        if (( volumeDelta > 0 ) && ( activeStimulusPlayerCount > 0 ))
        {
            // compute total stimulus
            double stimulusFactor = volumeDelta / totalDesiredVolume;
            double totalStimulus =
                    stimulusFactor * this.desiredStimulus * activeStimulusPlayerCount;
            StimulusUtil.appendToFile( logFile, "" );
            StimulusUtil.appendToFile( logFile, "Stimulus Factor : " + stimulusFactor                      );
            StimulusUtil.appendToFile( logFile, "Total Stimulus  : " + this.economy.format( totalStimulus ));

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
                    logFile, String.format( "Highest Wealth  : %" + wealthLength + "s", formattedHighestWealth ));
            StimulusUtil.appendToFile(
                    logFile, String.format( "Lowest Wealth   : %" + wealthLength + "s", formattedLowestWealth ));
            StimulusUtil.appendToFile(
                    logFile, String.format( "Wealth Delta    : %" + wealthLength + "s", formattedWealthDelta ));


            // map players to payment factors
            double paymentFactorSum = 0;
            Map< UUID, Double > playerRawPaymentFactorMap = new HashMap<>();
            Map< UUID, Double > playerPaymentFactorMap = new HashMap<>();
            for ( UUID playerId : activeStimulusPlayers )
            {
                double rawPaymentFactor;
                double paymentFactor;

                if ( highestWealth == lowestWealth )
                {
                    rawPaymentFactor = 1;
                    paymentFactor = 1;
                }
                else
                {
                    double playerOffset = playerWealthMap.get( playerId ) - lowestWealth;
                    rawPaymentFactor = 1 - ( playerOffset / wealthDelta );
                    paymentFactor = (( 1 - this.minimumPaymentFactor ) * rawPaymentFactor ) +
                                    this.minimumPaymentFactor;
                }

                paymentFactorSum += paymentFactor;
                playerRawPaymentFactorMap.put( playerId, rawPaymentFactor );
                playerPaymentFactorMap.put( playerId, paymentFactor );
            }

            StimulusUtil.appendToFile( logFile, "" );
            StimulusUtil.appendToFile( logFile, "Player Payment Factor Sum: " + paymentFactorSum );

            // compute the payment for each player
            Map< UUID, Double > playerPaymentMap = new HashMap<>();
            for ( UUID playerId : activeStimulusPlayers )
            {
                double adjustedPaymentFactor = playerPaymentFactorMap.get( playerId ) / paymentFactorSum;
                double playerPayment = adjustedPaymentFactor * totalStimulus;
                playerPaymentMap.put( playerId, playerPayment );
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
                        player.sendMessage( prefix + ChatColor.DARK_AQUA + "You received " +
                                            ChatColor.LIGHT_PURPLE + this.economy.format( payment ) +
                                            ChatColor.DARK_AQUA + " in stimulus!" );
                    }
                    else
                    {
                        this.plugin.addOfflineStimulus( playerId, payment );
                    }
                }
                else
                {
                    Player player = onlinePlayerMap.get( playerId );
                    if ( player != null )
                    {
                        player.sendMessage( prefix + ChatColor.DARK_AQUA + "You received no stimulus." );
                    }
                }
            }

            // output player information
            String playerNameHeader             = "Name";
            String playerWealthHeader           = "Wealth";
            String playerRawPaymentFactorHeader = "Raw Payment Factor";
            String playerPaymentFactorHeader    = "Payment Factor";
            String playerPaymentHeader          = "Payment";

            Map< UUID, String > formattedPlayerNameMap             = new HashMap<>();
            Map< UUID, String > formattedPlayerWealthMap           = new HashMap<>();
            Map< UUID, String > formattedPlayerRawPaymentFactorMap = new HashMap<>();
            Map< UUID, String > formattedPlayerPaymentFactorMap    = new HashMap<>();
            Map< UUID, String > formattedPlayerPaymentMap          = new HashMap<>();

            for ( UUID playerId : activeStimulusPlayers )
            {
                formattedPlayerNameMap.put( playerId, offlinePlayerMap.get( playerId ).getName() );
                formattedPlayerWealthMap.put( playerId, this.economy.format( playerWealthMap.get( playerId )));
                formattedPlayerRawPaymentFactorMap.put(
                        playerId, Double.toString( playerRawPaymentFactorMap.get( playerId )));
                formattedPlayerPaymentFactorMap.put(
                        playerId, Double.toString( playerPaymentFactorMap.get( playerId )));
                formattedPlayerPaymentMap.put( playerId, this.economy.format( playerPaymentMap.get( playerId )));
            }

            int playerNameLength             = StimulusUtil.getMaxLength( formattedPlayerNameMap.values()             );
            int playerWealthLength           = StimulusUtil.getMaxLength( formattedPlayerWealthMap.values()           );
            int playerRawPaymentFactorLength = StimulusUtil.getMaxLength( formattedPlayerRawPaymentFactorMap.values() );
            int playerPaymentFactorLength    = StimulusUtil.getMaxLength( formattedPlayerPaymentFactorMap.values()    );
            int playerPaymentLength          = StimulusUtil.getMaxLength( formattedPlayerPaymentMap.values()          );

            if ( playerNameLength < playerNameHeader.length() )
            {
                playerNameLength = playerNameHeader.length();
            }

            if ( playerWealthLength < playerWealthHeader.length() )
            {
                playerWealthLength = playerWealthHeader.length();
            }

            if ( playerRawPaymentFactorLength < playerRawPaymentFactorHeader.length() )
            {
                playerRawPaymentFactorLength = playerRawPaymentFactorHeader.length();
            }

            if ( playerPaymentFactorLength < playerPaymentFactorHeader.length() )
            {
                playerPaymentFactorLength = playerPaymentFactorHeader.length();
            }

            if ( playerPaymentLength < playerPaymentHeader.length() )
            {
                playerPaymentLength = playerPaymentHeader.length();
            }

            TreeSet< SortedLine< Double >> playerOutput = new TreeSet<>();
            for ( UUID playerId : activeStimulusPlayers )
            {
                String name = String.format(
                        "%-" + playerNameLength + "s", formattedPlayerNameMap.get( playerId ));
                String wealth = String.format(
                        "%" + playerWealthLength + "s", formattedPlayerWealthMap.get( playerId ));
                String rawPaymentFactor = String.format(
                        "%-" + playerRawPaymentFactorLength + "s", formattedPlayerRawPaymentFactorMap.get( playerId ));
                String paymentFactor = String.format(
                        "%-" + playerPaymentFactorLength + "s", formattedPlayerPaymentFactorMap.get( playerId ));
                String payment = String.format(
                        "%" + playerPaymentLength + "s", formattedPlayerPaymentMap.get( playerId ));
                String line =
                        name + "  " + wealth + "  " + rawPaymentFactor + "  " + paymentFactor + "  " + payment;
                playerOutput.add( new SortedLine<>( playerWealthMap.get( playerId ), line ));
            }

            String header = String.format(
                    "%-" + playerNameLength             + "s  " +
                    "%-" + playerWealthLength           + "s  " +
                    "%-" + playerRawPaymentFactorLength + "s  " +
                    "%-" + playerPaymentFactorLength    + "s  " +
                    "%-" + playerPaymentLength          + "s",
                    playerNameHeader, playerWealthHeader, playerRawPaymentFactorHeader,
                    playerPaymentFactorHeader, playerPaymentHeader );

            String divider =
                    StimulusUtil.getRepeatedDash( playerNameLength             ) + "  " +
                    StimulusUtil.getRepeatedDash( playerWealthLength           ) + "  " +
                    StimulusUtil.getRepeatedDash( playerRawPaymentFactorLength ) + "  " +
                    StimulusUtil.getRepeatedDash( playerPaymentFactorLength    ) + "  " +
                    StimulusUtil.getRepeatedDash( playerPaymentLength          );

            StimulusUtil.appendToFile( logFile, "" );
            StimulusUtil.appendToFile( logFile, header );
            StimulusUtil.appendToFile( logFile, divider );

            for ( SortedLine< Double > line : playerOutput.descendingSet() )
            {
                StimulusUtil.appendToFile( logFile, line.line );
            }

            StimulusUtil.appendToFile( logFile, divider );
        }
        else
        {
            // send a message to all players about the lack of stimulus
            for ( UUID playerId : activeStimulusPlayers )
            {
                Player player = onlinePlayerMap.get( playerId );
                if ( player != null )
                {
                    player.sendMessage(
                            prefix + ChatColor.DARK_AQUA + "You received no stimulus due to strong economy." );
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
            this.plugin.getAllWealthTop().add( new SortedLine<>( wealth, playerName ));
        }

        // populate wealth top data structure for active players
        for ( UUID playerId : activeStimulusPlayers )
        {
            double wealth = playerWealthMap.get( playerId );
            String playerName = offlinePlayerMap.get( playerId ).getName();
            this.plugin.getActiveWealthTop().add( new SortedLine<>( wealth, playerName ));
        }

        this.plugin.saveData();
    }
}
