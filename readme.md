# Stimulus Package

Stimulus Package is a Spigot plugin that gives money to players based on the number of active players and volume of
economic transactions.

## Under Development

This plugin does not currently match our proposed implementation.

## Proposed Implementation

- Configured variables:
    - `[economic interval]`: Time interval used to measure economic activity.
    - `[stimulus interval]`: Time interval for which players will receive stimulus payments after they last played.
    - `[payment interval]`: Time interval used to make stimulus payments to players.
    - `[desired volume]`: Desired economic activity per player over the last `[economic interval]`.
    - `[desired stimulus]`: Desired stimulus payment per player every `[payment interval]`.
    - `[minimum payment factor]`: Determines how much the richest player gets paid.
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
    - Assign the sum of `[payment factor]` for all active players to `[payment factor sum]`.
    - Compute an `[adjusted payment factor]` for each active player: `[payment factor]` / `[payment factor sum]`
    - Compute a `[payment amount]` for each active player: `[adjusted payment factor]` * `[total stimulus]`
    - Pay each active player their `[payment amount]`.

## Proposed Default Configuration

- `[economic interval]`: One Week - 604,800 seconds
- `[stimulus interval]`: One Day - 86,400 seconds
- `[payment interval]`: Ten Minutes - 600 seconds
- `[desired volume]`: 10,000
- `[desired stimulus]`: 10
- `[minimum payment factor]`: 0

With one player with no economic activity, that player would receive a payment of 10 currency every 10 minutes with a
total payment of 10,080 currency over one week.

## Dependencies

This plugin depends on the following plugins:

* [GriefPrevention](https://www.spigotmc.org/resources/griefprevention.1884/)
* [SaneEconomy](https://www.spigotmc.org/resources/saneeconomy-simple-but-featureful-economy.26223/)
* [Vault](https://www.spigotmc.org/resources/vault.34315/)

## Installation

This plugin is currently under development and a binary release is not available. To use this plugin you must compile it
yourself. We use [Apache Maven](https://maven.apache.org/) to build this plugin.

### Compile Plugin

Run `mvn package` to create a Jar for this plugin.

### Install Plugin

Copy `target/stimulus-package-1.0.0-SNAPSHOT.jar` into your server `plugins` directory.

## Usage

Use the `/stimulus` command to see debug information related to the plugin.

Use the `/wealth` command to see your total wealth.

## Contributing

Instructions for those wishing to contribute to this project are available in our
[contributing documentation](contributing.md).
