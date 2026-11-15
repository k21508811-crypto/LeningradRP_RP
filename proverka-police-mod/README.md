# Proverka Police Mod

RAGE Plugin Hook add-on for GTA V Legacy. Version 0.4.0.

## Controls
- F12: open/close the in-game menu.
- Up/Down: select a command.
- Enter: execute.
- Esc: close the menu.

## Implemented functions
- Create up to three local police partner peds; issue follow/hold commands.
- Order recruited partners into the player's current vehicle.
- Spawn POLICE, POLICE2 and POLICE3 vehicles.
- Dispatcher menu requests LocalUnit, SWAT, AirUnit, Ambulance, Firetruck and PrisonerTransport through the LSPDFR `Functions.RequestBackup` API using reflection, avoiding a compile-time dependency on LSPDFR.
- Enter custom Russian or English text; show it as an in-game subtitle and send it to Windows System.Speech.

## Compatibility notes
- Built for .NET Framework 4.8 and the RAGE Plugin Hook SDK package.
- Install compatible RAGE Plugin Hook and LSPDFR releases for GTA V Legacy separately.
- The dispatcher calls the standard LSPDFR backup API. If Ultimate Backup is installed and intercepts standard LSPDFR backup requests in the user's setup, it should handle those requests; this is not a direct Ultimate Backup proprietary API integration.
- The local "remove test backup" option only removes entities created by this plugin; use the normal LSPDFR/Ultimate Backup dismissal controls for dispatched units.
- Speech output depends on Windows voices installed on the user's system. Russian speech requires a Russian Windows speech voice.
- The in-game text-entry dialog temporarily switches focus to Windows.

## Build
GitHub Actions builds the plugin DLL and Windows installer. RAGE Plugin Hook itself is not redistributed.
