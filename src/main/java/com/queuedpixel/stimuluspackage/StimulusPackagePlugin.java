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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.event.SaneEconomyTransactionEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;

public class StimulusPackagePlugin extends JavaPlugin implements Listener
{
    private final Path pluginDirectory = Paths.get( "plugins/StimulusPackage" );
    private final Path logDirectory = this.pluginDirectory.resolve( "logs" );
    private final Path transactionsFile = this.pluginDirectory.resolve( "transactions.txt" );
    private final StimulusPackageConfiguration config = new StimulusPackageConfiguration();
    private final Collection< Transaction > transactions = new LinkedList< Transaction >();
    private Economy economy;
    private GriefPrevention griefPrevention;
    private double actualVolume = 0;

    public void onEnable()
    {
        this.getLogger().info( "onEnable() is called!" );

        try
        {
            if ( !Files.exists( this.pluginDirectory )) Files.createDirectory( this.pluginDirectory );
            if ( !Files.exists( this.logDirectory    )) Files.createDirectory( this.logDirectory    );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

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

        this.getCommand( "stimulus" ).setExecutor( new StimulusCommand( this ));
        this.getCommand( "wealth" ).setExecutor( new WealthCommand( this ));
        this.getServer().getPluginManager().registerEvents( this, this );
    }

    public void onDisable()
    {
        this.getLogger().info( "onDisable() is called!" );
    }

    @EventHandler
    public void onSaneEconomyTransactionEvent( SaneEconomyTransactionEvent event )
    {
        long timestamp = new Date().getTime();
        double amount = event.getTransaction().getAmount();
        Transaction transaction = new Transaction( timestamp, amount );
        this.addTransaction( transaction );
        StimulusUtil.appendToFile( this.transactionsFile, transaction.toString() );

        int fractionalDigits = this.economy.fractionalDigits();
        String currencyFormat = ( fractionalDigits > -1 ) ? "%." + fractionalDigits + "f" : "%f";
        String logEntry = String.format(
                "%tF %<tT.%<tL, " + currencyFormat + ", %s, %s, %s",
                timestamp, amount, event.getTransaction().getReason(),
                this.formatEconomable( event.getTransaction().getSender() ),
                this.formatEconomable( event.getTransaction().getReceiver() ));
        StimulusUtil.appendToFile( this.getLogFile( "SaneEconomy", timestamp ), logEntry );
        this.getLogger().info( "Economy Transaction: " + logEntry );
    }

    StimulusPackageConfiguration getConfiguration()
    {
        return this.config;
    }

    Economy getEconomy()
    {
        return this.economy;
    }

    GriefPrevention getGriefPrevention()
    {
        return this.griefPrevention;
    }

    Path getLogFile( String prefix, long timestamp )
    {
        return this.logDirectory.resolve( String.format( "%s-%tF.log", prefix, timestamp ));
    }

    double getActualVolume( long now )
    {
        for ( Iterator< Transaction > iterator = this.transactions.iterator(); iterator.hasNext(); )
        {
            Transaction transaction = iterator.next();
            long transactionAge = ( now - transaction.getTimestamp() ) / 1000;
            if ( transactionAge > this.config.getEconomicInterval() )
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

    private String formatEconomable( Economable economable )
    {
        String result = economable.getUniqueIdentifier();
        OfflinePlayer player = economable.tryCastToPlayer();
        if ( player != null ) result += " [" + player.getName() + "]";
        return result;
    }

    private void addTransaction( Transaction transaction )
    {
        this.transactions.add( transaction );
        this.actualVolume += transaction.getAmount();
    }
}
