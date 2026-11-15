# Feature plan

## Implemented in 0.5.0
- Installer validates GTA5.exe, RagePluginHook.exe and Plugins/LSPD First Response.dll.
- Installer puts ProverkaPoliceMod.dll into Plugins/LSPDFR (the LSPDFR plugin directory).
- F12 menu, ESC close, arrow navigation and Enter activation.
- Partner recruit/follow/hold/vehicle entry/removal.
- POLICE, POLICE2 and POLICE3 vehicle spawning.
- Dispatcher requests through the LSPDFR Functions.RequestBackup API.
- Free text subtitle and Windows System.Speech output.

## Compatibility boundary
Ultimate Backup support currently uses the standard LSPDFR backup API from the LSPDFR plugin context. A direct proprietary Ultimate Backup API integration is not implemented or claimed. Verify dispatch behavior in-game with the installed Ultimate Backup version.

## Test checklist
- Confirm the DLL appears in Plugins/LSPDFR.
- Start RagePluginHook.exe and go on duty.
- Confirm the plugin load message in RagePluginHook.log.
- Press F12 and test each menu item.
- Test each dispatcher unit with Ultimate Backup installed.
