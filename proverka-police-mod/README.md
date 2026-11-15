# Proverka Police Mod

Initial RAGE Plugin Hook plugin + Windows installer for GTA V Legacy.

## Implemented in 0.2.0
- RAGE Plugin Hook plugin entry point.
- F12 opens and closes the in-game menu.
- ESC closes the menu.
- Windows installer copies `ProverkaPoliceMod.dll` to `GTA V/Plugins`.

## Not implemented yet
Partner management, Ultimate Backup integration, vehicle spawning, dispatcher actions and custom text-to-speech are planned modules. The current menu shell does not claim these features are functional.

## Test access
The installer accepts `MENUBUC2` as a temporary local test code. It is not payment verification or production licensing. The code is visible in this public source repository.

## Build
GitHub Actions restores the RAGE Plugin Hook SDK package from NuGet, builds the plugin against it, packages the DLL into the PyInstaller installer, and uploads the Windows installer artifact. RAGE Plugin Hook itself is not bundled; users must install it separately.

RAGE Plugin Hook SDK package: https://www.nuget.org/packages/RagePluginHook
