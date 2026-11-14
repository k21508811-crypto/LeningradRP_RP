# LENINGRAD RP BOT CONTROL

Android app for the LENINGRAD RP Discord bot.

## Current prototype
- Login screen appears on every fresh app launch.
- Temporary test login: `NewBot` / `NewBot`.
- After login, the app detects Wi-Fi/mobile network, reads the local IP address, and requests the public IP from ipify.
- Optional HTTPS bridge URL: the app sends a JSON POST request to `/activate` with network type and IP details.

## Important limitations
- The test credentials are embedded in the APK (the password is stored as a SHA-256 digest, but a client-side check is not secure authentication). Do not reuse this password for important accounts. Replace it with server-side authentication before public release.
- The app does not start the Discord bot process. A running bot/bridge service must expose an HTTPS `POST /activate` endpoint and implement the connection logic.
- IP lookup contacts `api.ipify.org`; the public IP is therefore disclosed to that service. Local network details are sent to your configured bridge only after you provide its HTTPS URL and tap activation.
- No VDS is included or required by this Android client, but the Discord bot still needs a process running somewhere with internet access (for example, another always-on device or a suitable hosting environment).

## Build
GitHub Actions workflow: `.github/workflows/build-discord-bot-apk.yml`.
Artifact: `LeningradRP-BotControl-debug-apk`.
