# Proverka Police Mod

RAGE Plugin Hook add-on for GTA V Legacy. Version 0.4.0.

## Controls
- F12: open/close the in-game menu.
- Up/Down: select a command.
- Enter: execute.
- Esc: close the menu.

## Included gameplay functions
- Create up to three local police partner peds; issue follow/hold commands.
- Order recruited partners into the player's current vehicle.
- Spawn POLICE, POLICE2 and POLICE3 vehicles.
- Create and remove two local backup units and their police vehicles.
- Enter custom Russian or English text; show it as an in-game subtitle and send it to Windows System.Speech.

## Compatibility notes
- Built for .NET Framework 4.8 and the RAGE Plugin Hook SDK package.
- Install a compatible RAGE Plugin Hook and LSPDFR release for GTA V Legacy separately.
- The backup command currently creates local RAGE Plugin Hook entities. It is NOT a call to LSPDFR's dispatch system and is NOT integrated with Ultimate Backup. That integration requires a compatible public API/SDK for the exact installed Ultimate Backup version.
- Speech output depends on Windows voices installed on the user's system. Russian speech requires a Russian Windows speech voice.
- The in-game text-entry dialog temporarily switches focus to Windows.

## Build
GitHub Actions builds the plugin DLL and a Windows installer. RAGE Plugin Hook itself is not redistributed.
