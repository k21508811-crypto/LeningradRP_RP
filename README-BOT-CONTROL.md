# LENINGRAD RP BOT CONTROL

Android app for the LENINGRAD RP Discord bot.

## Pydroid 3 integration (prototype)
- Detects the Pydroid 3 Android package (`ru.iiec.pydroid3`).
- Creates a small, fixed diagnostic Python script in the app cache.
- Opens that script in Pydroid 3 using Android's content-sharing permission.
- The user must review the script and tap Run in Pydroid 3. Android does not allow this app to silently control another app or read its private console output.
- The app has its own terminal-style event log showing detection and launch status. Python's actual stdout/stderr remains in Pydroid 3.

## Discord bridge
- Optional HTTPS bridge URL: the app sends a JSON POST request to `/activate` with network type and IP details.
- The Discord bot must keep running on the VDS and expose a separately implemented authenticated HTTPS API. This Android feature does not start or control the Discord bot.

## Security notes
- The temporary test login is embedded in the APK and is not secure authentication. Replace it with server-side authentication before public release.
- The sample Python script only prints device/Python diagnostic information. It does not access private data, run shell commands, or send credentials.
- Do not put Discord bot tokens or bridge secrets in the APK or GitHub repository.
- Public IP lookup contacts `api.ipify.org`; the public IP is disclosed to that service.

## Build
GitHub Actions workflow: `.github/workflows/build-discord-bot-apk.yml`.
Artifact: `LeningradRP-BotControl-debug-apk`.
