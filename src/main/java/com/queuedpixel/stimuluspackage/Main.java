package com.queuedpixel.stimuluspackage;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    public void onEnable()
    {
        getLogger().info( "onEnable is called!" );
    }

    public void onDisable()
    {
        getLogger().info( "onDisable is called!" );
    }
}
