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
