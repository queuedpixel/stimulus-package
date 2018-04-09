package com.queuedpixel.stimuluspackage;

import java.util.Date;

import org.appledash.saneeconomy.event.SaneEconomyTransactionEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EconomyTransactionListener implements Listener
{
    private final Main plugin;

    public EconomyTransactionListener( Main plugin )
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSaneEconomyTransactionEvent( SaneEconomyTransactionEvent event )
    {
        plugin.addTransaction( new Transaction( new Date().getTime(),
                               event.getTransaction().getAmount() ));

        Bukkit.getLogger().info( "Economy Transaction: " +
                                 event.getTransaction().getReason().toString() + " - " +
                                 event.getTransaction().getSender().getUniqueIdentifier() + " - " +
                                 event.getTransaction().getReceiver().getUniqueIdentifier() + " - " +
                                 event.getTransaction().getAmount() );
    }
}
