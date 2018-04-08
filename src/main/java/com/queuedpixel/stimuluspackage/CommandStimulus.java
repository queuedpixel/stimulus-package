package com.queuedpixel.stimuluspackage;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        // current time
        Date now = new Date();

        // map of players to the number of seconds since they were last on the server
        Map< UUID, Long > playerMap = new HashMap< UUID, Long >();

        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        sender.sendMessage( offlinePlayers.length + " Offline Players:" );

        for ( OfflinePlayer player : offlinePlayers )
        {
            // store number of seconds since player was last on
            playerMap.put( player.getUniqueId(), ( now.getTime() - player.getLastPlayed() ) / 1000 );
            sender.sendMessage( player.getUniqueId().toString() + " : " + player.getLastPlayed() );
        }

        Collection< ? extends Player > onlinePlayers = Bukkit.getOnlinePlayers();
        sender.sendMessage( onlinePlayers.size() + " Online Players:" );

        for ( Player player : onlinePlayers )
        {
            // player is on right now, so zero seconds since they were last on the server
            playerMap.put( player.getUniqueId(), 0l );
            sender.sendMessage( player.getUniqueId().toString() );
        }

        sender.sendMessage( playerMap.size() + " Total Players:" );

        for ( UUID playerId : playerMap.keySet() )
        {
            sender.sendMessage( playerId.toString() + " : " + playerMap.get( playerId ));
        }

        return true;
    }
}
