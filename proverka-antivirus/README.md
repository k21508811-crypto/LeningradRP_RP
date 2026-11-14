# Proverka Antivirus (Windows 10/11)

A Windows desktop prototype with a Windows Security-inspired interface.

## Features
- Launches Microsoft Defender scans for a folder selected by the user.
- Can request UAC elevation for a broader scan.
- Lists installed applications from standard Windows uninstall registry entries.
- Includes a clearly labeled local Pro trial demonstration screen.
- Does not disable Microsoft Defender or claim to be an independent antivirus engine.

## Build
GitHub Actions workflow: **Build Proverka Antivirus**.
Run it manually from Actions, then download the `ProverkaAntivirus-Windows-x64` artifact.

## Important limitations
- The trial period is a UI demonstration, not a secure commercial licensing system.
- The installed-app list is based on registry uninstall entries and is not guaranteed to include portable applications.
- Detection, remediation and quarantine are handled by Microsoft Defender. The prototype does not move or delete files itself.
- Folder scans are limited by the Windows account permissions and Defender configuration. UAC is requested only for the broader scan action.
