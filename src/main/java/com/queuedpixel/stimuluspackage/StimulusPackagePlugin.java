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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;
import org.maxgamer.quickshop.event.ShopSuccessPurchaseEvent;

public class StimulusPackagePlugin extends JavaPlugin implements Listener
{
    final String messagePrefix = ChatColor.GREEN + "[" + ChatColor.DARK_GREEN + "Stimulus" + ChatColor.GREEN + "] ";

    private Path logDirectory;
    private Path versionFile;
    private Path transactionsFile;
    private Path dataFile;
    private final Collection< UUID > excludedPlayers = new HashSet<>();
    private final Collection< Transaction > transactions = new LinkedList<>();
    private StimulusData data = new StimulusData();
    private Economy economy;
    private GriefPrevention griefPrevention;
    private PaymentQueue paymentQueue;
    private double actualVolume = 0;
    private final Map< String, UUID > playerNameMap = new HashMap<>();
    private StimulusInformation stimulusInformation = null;

    public void onEnable()
    {
        Path pluginDirectory  = this.getDataFolder().toPath();
        this.logDirectory     = pluginDirectory.resolve( "logs"             );
        this.versionFile      = pluginDirectory.resolve( "data_version.txt" );
        this.transactionsFile = pluginDirectory.resolve( "transactions.txt" );
        this.dataFile         = pluginDirectory.resolve( "stimulus.json"    );

        try
        {
            if ( !Files.exists( pluginDirectory   )) Files.createDirectory( pluginDirectory   );
            if ( !Files.exists( this.logDirectory )) Files.createDirectory( this.logDirectory );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        this.checkVersion();
        this.checkConfig();

        if ( Files.exists( this.transactionsFile ))
        {
            try
            {
                BufferedReader reader = Files.newBufferedReader( this.transactionsFile );
                String line = reader.readLine();
                while ( line != null )
                {
                    // add each transaction to our linked list
                    this.addTransaction( new Transaction( line ));
                    line = reader.readLine();
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }

        RegisteredServiceProvider< Economy > rsp =
                Bukkit.getServer().getServicesManager().getRegistration( Economy.class );
        this.economy = rsp.getProvider();
        this.griefPrevention =
                (GriefPrevention) Bukkit.getServer().getPluginManager().getPlugin( "GriefPrevention" );
        this.paymentQueue = new PaymentQueue( new PluginPaymentHandler( this ));

        // build our map of player names to player IDs
        this.playerNameMap.clear();
        for ( OfflinePlayer player : Bukkit.getOfflinePlayers() )
        {
            this.playerNameMap.put( player.getName().toLowerCase(), player.getUniqueId() );
        }

        this.loadData();
        this.getCommand( "stimulus" ).setExecutor( new StimulusCommand( this ));
        this.getCommand( "wealth" ).setExecutor( new WealthCommand( this ));
        this.getCommand( "wealthtop" ).setExecutor( new WealthTopCommand( this ));
        this.getServer().getPluginManager().registerEvents( this, this );

        // schedule the stimulus task
        StimulusTask stimulusTask = new StimulusTask( this );
        stimulusTask.runTaskTimer( this, 0, 20 ); // once a second

        // schedule the prune transactions task
        PruneTransactionsTask pruneTransactionsTask = new PruneTransactionsTask( this );
        long pruneInterval = this.getConfig().getLong( "pruneInterval" ) * 20; // 20 ticks per second
        pruneTransactionsTask.runTaskTimer( this, pruneInterval, pruneInterval );

        // schedule the log balances task
        LogBalancesTask logBalancesTask = new LogBalancesTask( this );
        long logBalancesInterval = this.getConfig().getLong( "logBalancesInterval" ) * 20; // 20 ticks per second
        logBalancesTask.runTaskTimer( this, 0, logBalancesInterval );
    }

    public void onDisable()
    {
        this.paymentQueue.makeAllPayments();
    }

    @EventHandler
    public void onPlayerJoinEvent( PlayerJoinEvent event )
    {
        String playerName = event.getPlayer().getName().toLowerCase();
        UUID playerId = event.getPlayer().getUniqueId();
        this.playerNameMap.put( playerName, playerId );

        if ( this.data.playerOfflineStimulusMap.containsKey( playerId ))
        {
            String stimulus = this.economy.format( this.data.playerOfflineStimulusMap.get( playerId ));
            String message = this.messagePrefix + ChatColor.DARK_AQUA + "While offline, you received " +
                             ChatColor.LIGHT_PURPLE + stimulus + ChatColor.DARK_AQUA + " in stimulus!";
            event.getPlayer().sendMessage( message );
            this.data.playerOfflineStimulusMap.remove( playerId );
            this.saveData();
        }
    }

    @EventHandler
    public void onShopSuccessPurchaseEvent( ShopSuccessPurchaseEvent event )
    {
        // log the transaction
        long timestamp = new Date().getTime();
        int fractionalDigits = this.economy.fractionalDigits();
        UUID playerId = event.getPurchaser();
        UUID vendorId = event.getShop().getOwner();
        String currencyFormat = ( fractionalDigits > -1 ) ? "%." + fractionalDigits + "f" : "%f";
        String logEntry = String.format(
                "%tF %<tT.%<tL, %s, %d, " + currencyFormat + ", %s [%s], %s [%s]",
                timestamp, event.getShop().getItem().getType().toString(),
                event.getAmount(), event.getBalance(),
                playerId, Bukkit.getPlayer( playerId ).getName(),
                vendorId, event.getShop().ownerName() );
        StimulusUtil.appendToFile( this.getLogFile( "QuickShop", timestamp ), logEntry );

        // ignore transactions from excluded players
        if (( this.excludedPlayers.contains( playerId )) || ( this.excludedPlayers.contains( vendorId ))) return;

        // ignore transactions from players interacting with their own shops
        if ( playerId.equals( vendorId )) return;

        // store the transaction
        Transaction transaction = new Transaction( timestamp, event.getBalance() );
        this.addTransaction( transaction );
        StimulusUtil.appendToFile( this.transactionsFile, transaction.toString() );
    }

    UUID getPlayerId( String playerName )
    {
        return this.playerNameMap.get( playerName.toLowerCase() );
    }

    Economy getEconomy()
    {
        return this.economy;
    }

    GriefPrevention getGriefPrevention()
    {
        return this.griefPrevention;
    }

    PaymentQueue getPaymentQueue()
    {
        return this.paymentQueue;
    }

    Path getLogFile( String prefix, long timestamp )
    {
        return this.logDirectory.resolve( String.format( "%s-%tF.log", prefix, timestamp ));
    }

    Collection< UUID > getExcludedPlayers()
    {
        return this.excludedPlayers;
    }

    double getActualVolume( long now )
    {
        for ( Iterator< Transaction > iterator = this.transactions.iterator(); iterator.hasNext(); )
        {
            Transaction transaction = iterator.next();
            long transactionAge = ( now - transaction.getTimestamp() ) / 1000;
            if ( transactionAge > this.getConfig().getLong( "economicInterval" ))
            {
                // remove transaction that is outside the economic time interval
                this.actualVolume -= transaction.getAmount();
                iterator.remove();
            }
            else
            {
                // all remaining transactions are within the economic time interval
                break;
            }
        }

        return this.actualVolume;
    }

    void addOfflineStimulus( UUID playerId, double amount )
    {
        double newAmount = amount;
        if ( this.data.playerOfflineStimulusMap.containsKey( playerId ))
        {
            newAmount += this.data.playerOfflineStimulusMap.get( playerId );
        }

        this.data.playerOfflineStimulusMap.put( playerId, newAmount );
    }

    TreeSet< SortedLine< Double >> getActiveWealthTop()
    {
        return this.data.activeWealthTop;
    }

    TreeSet< SortedLine< Double >> getAllWealthTop()
    {
        return this.data.allWealthTop;
    }

    void setLastStimulusTime( long lastStimulusTime )
    {
        this.data.lastStimulusTime = lastStimulusTime;
    }

    long getLastStimulusTime()
    {
        return this.data.lastStimulusTime;
    }

    void pruneTransactions()
    {
        try
        {
            BufferedWriter writer = Files.newBufferedWriter(
                    this.transactionsFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );

            for ( Transaction transaction : this.transactions )
            {
                writer.write( transaction.toString() );
                writer.newLine();
            }

            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    void setStimulusInformation( StimulusInformation stimulusInformation )
    {
        this.stimulusInformation = stimulusInformation;
    }

    StimulusInformation getStimulusInformation()
    {
        return this.stimulusInformation;
    }

    void saveData()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try
        {
            BufferedWriter writer = Files.newBufferedWriter(
                    this.dataFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
            writer.write( gson.toJson( this.data ));
            writer.newLine();
            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private void loadData()
    {
        // create new data file if it doesn't exist
        if ( !Files.exists( this.dataFile ))
        {
            this.data = new StimulusData();
            return;
        }

        try
        {
            Gson gson = new Gson();
            BufferedReader reader = Files.newBufferedReader( this.dataFile );
            this.data = gson.fromJson( reader, StimulusData.class );
            reader.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            this.data = new StimulusData();
        }
    }

    private void checkVersion()
    {
        long desiredVersion = 1;
        Gson gson = new Gson();

        // if version file exists, verify the version
        if ( Files.exists( this.versionFile ))
        {
            long version = -1;
            try
            {
                BufferedReader reader = Files.newBufferedReader( this.versionFile );
                version = gson.fromJson( reader, Long.class );
                reader.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }

            if ( version != desiredVersion )
            {
                throw new IllegalStateException( "Unknown version of StimulusPackage data files!" );
            }
        }
        // otherwise, create version file
        else
        {
            try
            {
                BufferedWriter writer = Files.newBufferedWriter(
                        this.versionFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
                writer.write( gson.toJson( desiredVersion ));
                writer.newLine();
                writer.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    private void checkConfig()
    {
        this.saveDefaultConfig();

        if ( this.getConfig().getDouble( "desiredStimulus" ) < 0 )
        {
            this.getLogger().info( "desiredStimulus is less than 0! Defaulting to 0." );
            this.getConfig().set( "desiredStimulus", 0 );
        }

        if ( this.getConfig().getDouble( "minimumPaymentFactor" ) > 1 )
        {
            this.getLogger().info( "minimumPaymentFactor is greater than 1! Defaulting to 1." );
            this.getConfig().set( "minimumPaymentFactor", 1 );
        }

        if ( this.getConfig().getDouble( "minimumPaymentFactor" ) < 0 )
        {
            this.getLogger().info( "minimumPaymentFactor is less than 0! Defaulting to 0." );
            this.getConfig().set( "minimumPaymentFactor", 0 );
        }

        this.excludedPlayers.clear();
        for ( String playerId : this.getConfig().getStringList( "excludedPlayers" ))
        {
            this.excludedPlayers.add( UUID.fromString( playerId ));
        }
    }

    private void addTransaction( Transaction transaction )
    {
        this.transactions.add( transaction );
        this.actualVolume += transaction.getAmount();
    }
}
