# City Run Android — launcher + mobile test game

This branch builds two separate debug APKs:

- **CityRun-Launcher-Android** — redesigned dark/red launcher dashboard, HTTPS license status check, install detection and game launch.
- **CityRun-Mobile-Test** — standalone playable portrait driving prototype with touch steering, throttle/brake, traffic, city blocks and a one-mission delivery objective.

## Install and test

1. Install `game-debug.apk` first.
2. Install `app-debug.apk`.
3. Open City Run Launcher, check license, then tap Play.

The launcher requires a valid license API response before enabling Play. Without `LICENSE_API_BASE_URL`, it intentionally stays in test mode and blocks launch. Configure an HTTPS base URL in Gradle properties only when a real license backend is deployed.

License endpoint: `GET /v1/license/status?device_id=<ANDROID_ID>`
Expected response: `{"active":true,"expires_at":"YYYY-MM-DD"}`

A production license backend must verify a payment provider's signed webhook. The app does not collect or store bank-card details and does not simulate a successful payment. This test APK has no payment flow.

## Game prototype scope

The mobile game is an original compact top-down driving test, not a GTA 5 port. Hold the left/right touch zones to steer; use GAS and BRAKE on the right. Drive through the city to complete the pickup and garage delivery stages. It uses simple procedural Canvas graphics so the APK stays small and loads quickly.

## Build

GitHub Actions workflow: **Build City Run Android Apps**. It uploads both APKs as separate artifacts.
