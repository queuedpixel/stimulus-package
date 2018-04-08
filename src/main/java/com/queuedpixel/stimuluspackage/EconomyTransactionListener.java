package com.queuedpixel.stimuluspackage;

import org.appledash.saneeconomy.event.SaneEconomyTransactionEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EconomyTransactionListener implements Listener
{
    @EventHandler
    public void onSaneEconomyTransactionEvent( SaneEconomyTransactionEvent event )
    {
        Bukkit.getLogger().info( "Economy Transaction: " +
                                 event.getTransaction().getReason().toString() + " - " +
                                 event.getTransaction().getSender().getUniqueIdentifier() + " - " +
                                 event.getTransaction().getReceiver().getUniqueIdentifier() + " - " +
                                 event.getTransaction().getAmount() );
    }
}
