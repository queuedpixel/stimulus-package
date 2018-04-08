package com.queuedpixel.stimuluspackage;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    public void onEnable()
    {
        getLogger().info( "onEnable is called!" );
        this.getCommand( "stimulus" ).setExecutor( new CommandStimulus() );
        this.getServer().getPluginManager().registerEvents( new EconomyTransactionListener(), this );
    }

    public void onDisable()
    {
        getLogger().info( "onDisable is called!" );
    }
}
