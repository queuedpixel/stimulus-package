package com.queuedpixel.stimuluspackage;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStimulus implements CommandExecutor
{
    private static final long economicInterval = 604800; // seconds; one week
    private static final long stimulusInterval = 86400;  // seconds; one day

    private final Main plugin;

    public CommandStimulus( Main plugin )
    {
        this.plugin = plugin;
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        // current time
        Date now = new Date();

        // map of players to the number of seconds since they were last on the server
        Map< UUID, Long > playerMap = new HashMap< UUID, Long >();

        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for ( OfflinePlayer player : offlinePlayers )
        {
            // store number of seconds since player was last on
            playerMap.put( player.getUniqueId(), ( now.getTime() - player.getLastPlayed() ) / 1000 );
        }

        Collection< ? extends Player > onlinePlayers = Bukkit.getOnlinePlayers();
        for ( Player player : onlinePlayers )
        {
            // player is on right now, so zero seconds since they were last on the server
            playerMap.put( player.getUniqueId(), 0l );
        }

        int economicPlayers = 0;
        int stimulusPlayers = 0;

        for ( Long loginInterval : playerMap.values() )
        {
            if ( loginInterval < economicInterval ) economicPlayers++;
            if ( loginInterval < stimulusInterval ) stimulusPlayers++;
        }

        sender.sendMessage(
                "Economic Players: " + economicPlayers + ", Stimulus Players: " + stimulusPlayers );

        sender.sendMessage( "Recent Transactions:" );

        int transactionCount = 0;
        for ( Iterator< Transaction > iterator = plugin.getTransactionIterator(); iterator.hasNext(); )
        {
            Transaction transaction = iterator.next();
            if ( ++transactionCount > 10 ) break;
            sender.sendMessage( transaction.getTimestamp() + " - " + transaction.getAmount() );
        }

        return true;
    }
}
