# Leningrad AI Studio

Android application for generating images from text prompts with Pollinations.

## Features
- Prompt input and model selection
- Image generation through the official Pollinations API
- Preview generated image
- Save image using Android's system document picker
- API key is entered by the user and is not embedded in the APK or persisted by the app

## Setup
1. Install the APK from the GitHub Actions artifact.
2. Create a Pollinations API key at https://enter.pollinations.ai/keys.
3. Enter the key in the app, write a prompt, select a model, and tap Generate.
4. Use Save Image to choose where to store the PNG.

Image generation uses `https://gen.pollinations.ai/image/{prompt}` with an Authorization Bearer header. Generation availability, model access, pricing, and rate limits are controlled by Pollinations and may change. The API key may consume the account's available balance.

## Security notes
- No background services, device admin, accessibility service, SMS, contacts, microphone, camera, or location permissions.
- The only declared Android permission is INTERNET, required to call the image API.
- Cleartext HTTP traffic is disabled.
- The API key is not hardcoded in the source or APK and is not saved by this app.
- Image saving is performed through Android's Storage Access Framework; broad storage permissions are not requested.

A clean implementation does not guarantee that every antivirus engine will classify every build as safe. Review the APK and scan it with trusted security tools before distribution.
