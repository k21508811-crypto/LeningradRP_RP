# Proverka Police Mod — test installer

Windows installer scaffold for GTA V Legacy + LSPDFR.

## Test access
The prototype accepts the test code `MENUBUC2`. This is a local test gate only. It is not connected to a payment provider, does not verify purchases, and must not be used as production licensing.

## What this build does
- asks for the test access code;
- locates the GTA V root folder or asks the user to select it;
- checks for GTA5.exe and an LSPDFR/RagePluginHook installation marker;
- creates `Plugins/LSPDFR/ProverkaPoliceMod`;
- writes a starter config and installation note.

## Important status
This is **not yet a working in-game RAGE Plugin Hook plugin**. It does not spawn vehicles, control partners, communicate with the dispatcher, or speak arbitrary entered text. Those features require a compiled plugin using the compatible RAGE Plugin Hook/LSPDFR APIs and, for Ultimate Backup integration, the installed mod's supported interfaces. The current build deliberately does not overwrite game files or existing plugins.

## Build
GitHub Actions builds a Windows x64 executable with PyInstaller. Download the artifact from the Actions run after it succeeds.
