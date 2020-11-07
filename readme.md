# Stimulus Package

Stimulus Package is a Spigot plugin that gives money to players based on the number of active players and volume of
economic transactions.

## Under Development

This is a development snapshot version of this project that may not be stable or function at all.

## Commands

### `/wealth`

View the wealth for a specified player.
A player's wealth is the sum of their economy balance and the value of their GriefPrevention claim blocks.

Specify the player name as the first argument of the command: `/wealth [player name]`

The command will display your wealth if you don't specify a player name.

### `/wealthtop`

View the wealthiest players on the server.

By default, this command only displays active players:
players who have logged in recently enough they are still eligible to receive stimulus payments.
Use `/wealthtop all` to display all players instead.

This command only displays 10 players.
Specify a page number to view additional players.
Example:

- `/wealthtop 2`
- `/wealthtop all 5`

## Installation

### Download

A pre-compiled Jar file is available for recent releases of this plugin:
[Stimulus Package Releases](https://github.com/queuedpixel/stimulus-package/releases)

### Compile

You may compile this plugin yourself.
We use [Apache Maven](https://maven.apache.org/) to compile this plugin.

### Install

Copy `stimulus-package-1.3.0-SNAPSHOT.jar` into your server `plugins` directory.

### Configure

The first time you run your server with this plugin installed it will create its default configuration in:

`[server root]/plugins/StimulusPackage/config.yml`

After you edit your configuration this plugin will use your new configuration the next time you start your server.

## Implementation Details

- Configured variables:
    - `[economic interval]`: Time interval used to measure economic activity.
    - `[stimulus interval]`: Time interval for which players will receive stimulus payments after they last played.
    - `[payment interval]`: Time interval used to make stimulus payments to players.
    - `[desired volume]`: Desired economic activity per player over the last `[economic interval]`.
    - `[desired stimulus]`: Desired stimulus payment per player every `[payment interval]`.
    - `[minimum payment factor]`: Determines how much the wealthiest player gets paid.
- Track `[active economic players]` for the past `[economic interval]`.
- Track `[active stimulus players]` for the past `[stimulus interval]`.
- A player who spends any time at all on the server during the specified time interval is considered active.
- Track the `[actual volume]` of economic transactions for past `[economic interval]`, excluding stimulus payments.
- Every `[payment interval]` do the following:
    - Determine `[active economic players]` for the past `[economic interval]`.
    - Determine `[active stimulus players]` for the past `[stimulus interval]`.
    - Determine `[actual volume]` for the past `[economic interval]`.
    - Compute `[total desired volume]`: `[desired volume]` * `[active economic players]`
    - Compute `[volume delta]`: `[total desired volume]` - `[actual volume]`
    - If `[volume delta]` is less than or equal to 0, skip the rest.
    - Compute `[stimulus factor]`: `[volume delta]` / `[total desired volume]`
    - Compute `[total stimulus]`: `[stimulus factor]` * `[desired stimulus]` * `[active stimulus players]`
    - Assign the wealth of the wealthiest active stimulus player(s) to `[highest wealth]`.
    - Assign the wealth of the poorest active stimulus player(s) to `[lowest wealth]`.
    - Compute `[wealth delta]`: `[highest wealth]` - `[lowest wealth]`
    - Compute a `[payment factor]` for each active stimulus player:
        - If all active stimulus players have the same wealth, or there is only one active stimulus player, use 1.
        - Otherwise:
            - Assign the amount of wealth owned by a player to `[player's wealth]`.
            - Compute `[player's offset]`: `[player's wealth]` - `[lowest wealth]`
            - Compute `[raw payment factor]`: 1 - ( `[player's offset]` / `[wealth delta]` )
            - Compute `[payment factor]`:
              (( 1 - `[minimum payment factor]` ) * `[raw payment factor]` ) + `[minimum payment factor]`
    - Assign the sum of `[payment factor]` for all active stimulus players to `[payment factor sum]`.
    - Compute an `[adjusted payment factor]` for each active stimulus player:
      `[payment factor]` / `[payment factor sum]`
    - Compute a `[payment amount]` for each active stimulus player: `[adjusted payment factor]` * `[total stimulus]`
    - Pay each active stimulus player their `[payment amount]`.

## Default Configuration

- `[economic interval]`: 1 Week - 604,800 Seconds
- `[stimulus interval]`: 1 Week - 604,800 Seconds
- `[payment interval]`: 15 Minutes - 900 Seconds
- `[desired volume]`: 1,000
- `[desired stimulus]`: 1
- `[minimum payment factor]`: 0

With 1 player with no economic activity, that player would receive a payment of 1 currency every 15 minutes with a
total payment of 672 currency over one week.

## Dependencies

This plugin depends on the following plugins:

- [GriefPrevention](https://www.spigotmc.org/resources/griefprevention.1884/)
- [QuickShop-Reremake](https://www.spigotmc.org/resources/quickshop-reremake-1-16-ready-say-hello-with-rgb.62575/)
- [Vault](https://www.spigotmc.org/resources/vault.34315/)

## Compatibility

This plugin has been tested against the following versions of its dependencies:

- Spigot - Version 1.16.4
- GriefPrevention - Version 16.16.0
- QuickShop-Reremake - Version 4.0.4.14
- Vault - Version 1.7.3

## Contributing

Instructions for those wishing to contribute to this project are available in our
[contributing documentation](contributing.md).
