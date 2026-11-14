# SiteGuard — Android app

Native Android starter for website checks, Yandex ID sign-in, and an AI assistant.

## Current state
- App UI: website address, check action, assistant chat, Yandex sign-in entry point.
- Local check validates URL syntax only. It does not detect malware or phishing.
- Real threat-intelligence checks, Yandex ID OAuth, and AI responses require a backend.
- Backend contract:
  - `POST /api/scan` with `{"url":"https://example.com"}`, response `{"report":"..."}`
  - `POST /api/chat` with `{"message":"..."}`, response `{"answer":"..."}`

## Production setup
1. Register an OAuth app in Yandex ID and configure its redirect URI on a backend you control.
2. Implement OAuth authorization-code flow server-side, validate state, exchange the code on the server, and issue an app session. Never ask for or store a user's Yandex password.
3. Connect a supported Yandex AI API/model on the backend. Only call it Alice if the official service and its terms explicitly support that branding/capability.
4. Connect a reputable website reputation provider. Unknown/time-out results must remain unknown, not be labelled safe. Protect URL fetching against SSRF: block private, loopback, link-local and reserved IPs; validate DNS and redirects.
5. Set the HTTPS backend URL in `BuildConfigField.API_BASE_URL`. Never embed OAuth secrets or AI keys in the APK.

## Build
JDK 17 + Android SDK 35; run `gradle assembleDebug`. Output: `app/build/outputs/apk/debug/app-debug.apk`.
