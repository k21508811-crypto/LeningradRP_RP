# City Run Launcher — Android beta test

This is the single Android test launcher APK. It checks license status against a server endpoint and keeps game launch disabled unless the server returns HTTP 2xx with JSON `{"active":true,"expires_at":"YYYY-MM-DD"}`.

## License API contract

Configure Gradle property `LICENSE_API_BASE_URL` (HTTPS only). The app calls:

`GET /v1/license/status?device_id=<ANDROID_ID>`

Example response:

```json
{"active":true,"expires_at":"2029-11-14"}
```

The server must verify the payment provider's signed webhook and issue a three-year license. Never trust a client-side payment success screen. Do not collect, store, or charge bank-card details in this app. The app does not contain payment credentials or a fake QR code.

## Current limitation

The existing City Run game build is Windows-only. This Android APK is a launcher/license-check test shell; it does not run the Windows EXE on Android. The Play button remains informational until an Android game build or a real Android launch target is added.

## Build

GitHub Actions workflow: **Build City Run Android Test Launcher**. The artifact is `CityRun-Android-Test-Launcher`.
