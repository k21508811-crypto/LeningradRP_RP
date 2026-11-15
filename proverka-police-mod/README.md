# Proverka Police Mod

RAGE Plugin Hook / LSPDFR plugin for GTA V Legacy. Version 0.5.0.

## Correct installation

This project is an **LSPDFR plugin**, not a standalone RAGE Plugin Hook plugin.

The installer places the DLL here:

```
GTA V Legacy/
  GTA5.exe
  RagePluginHook.exe
  Plugins/
    LSPD First Response.dll
    LSPDFR/
      ProverkaPoliceMod.dll
      ProverkaPoliceMod.json
```

Run `ProverkaPoliceMod-Setup.exe` separately. Select the GTA V root folder containing `GTA5.exe` if it is not detected. Do not copy the Setup executable into `Plugins/LSPDFR`.

Launch `RagePluginHook.exe`, wait for LSPDFR to load, then go on duty. LSPDFR loads its plugins from `Plugins/LSPDFR` when the player goes on duty. Press F12 after going on duty.

## Controls

- F12: open/close menu.
- Up/Down: select.
- Enter: execute.
- Esc: close.

## Implemented functions

- Recruit up to three local police partner peds; toggle follow/hold.
- Order recruited partners into the player's current vehicle.
- Spawn POLICE, POLICE2 and POLICE3.
- Request LocalUnit, SWAT, AirUnit, Ambulance, Firetruck and PrisonerTransport through LSPDFR's `Functions.RequestBackup` API.
- Enter custom Russian or English text, show it as an in-game subtitle and send it to Windows System.Speech.

## Ultimate Backup compatibility

The dispatcher calls the documented standard LSPDFR backup API from the LSPDFR plugin context. This allows Ultimate Backup to handle requests if the installed Ultimate Backup version intercepts standard LSPDFR backup requests. It is **not a direct call to a private/proprietary Ultimate Backup API**, and that behavior must be tested in-game with the user's installed versions.

## Troubleshooting

1. Confirm `Plugins/LSPDFR/ProverkaPoliceMod.dll` exists.
2. Start through `RagePluginHook.exe`.
3. Go on duty in LSPDFR; being in Story Mode alone is not enough to load an LSPDFR plugin.
4. If it still does not appear, inspect `RagePluginHook.log` in the GTA V root and search for `ProverkaPoliceMod` or a plugin load error.
5. Windows speech output depends on installed Windows voices. Russian speech requires a Russian voice.

Built for .NET Framework 4.8 and x64. RAGE Plugin Hook and LSPDFR must be installed separately; they are not redistributed.
