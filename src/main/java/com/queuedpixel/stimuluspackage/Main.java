package com.queuedpixel.stimuluspackage;

import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    private LinkedList< Transaction > transactions = new LinkedList< Transaction >();

    public void onEnable()
    {
        getLogger().info( "onEnable is called!" );
        this.getCommand( "stimulus" ).setExecutor( new CommandStimulus( this ));
        this.getServer().getPluginManager().registerEvents( new EconomyTransactionListener( this ), this );
    }

    public void onDisable()
    {
        getLogger().info( "onDisable is called!" );
    }

    protected void addTransaction( Transaction transaction )
    {
        this.transactions.add( transaction );
    }

    protected Iterator< Transaction > getTransactionIterator()
    {
        return this.transactions.descendingIterator();
    }
}
