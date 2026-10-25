# AyushMusic Android

A runnable Android Studio starter app for local audio files and authorized direct HTTPS audio streams. Includes a dark Compose UI and Media3 playback service for background playback / media-session integration.

## Requirements
- Android Studio (current stable)
- JDK 17
- Android SDK Platform 35
- Internet connection for Gradle dependency downloads

## Open and run
1. Extract `AyushMusicAndroid.zip`.
2. In Android Studio choose **Open** and select the extracted `AyushMusicAndroid` folder.
3. Let Gradle sync and download dependencies.
4. Connect an Android phone with USB debugging enabled, or start an emulator (Android 8.0+).
5. Press **Run**.

## Using it
- Tap **Add MP3** and select audio files using Android's system file picker. The app does not need broad storage permission.
- Paste a direct HTTPS audio file/stream URL and tap **Add URL**.
- Tap a track in the library to play it. Playback uses Android Media3 and is designed to continue in the background with media-session integration.

## Important limitations
- The URL field expects a direct audio resource (e.g. MP3/AAC/M4A/Ogg or a compatible stream URL), not a web page URL.
- The starter's simple URL validation is intentionally conservative; some valid streaming endpoints do not include a recognizable extension. Adjust validation if your provider uses such endpoints.
- This is not a Spotify client and does not bypass DRM, paywalls, login, regional restrictions, or copyright controls. Spotify playback requires Spotify's own supported SDK/API and account/platform eligibility.
- Album art, lyrics, synced queue UI, offline caching of online tracks, and a catalog/search backend are not implemented yet. This is a functional starter implementation rather than a production streaming service.
- For reliable playback, use stable HTTPS URLs with appropriate CORS-free direct media responses and correct audio content types. The server must permit access to the audio stream.


## Build an APK using GitHub Actions (no local Android SDK needed)
1. Create a GitHub repository and upload the contents of this folder to its `main` branch.
2. Open the repository's **Actions** tab.
3. Select **Build AyushMusic APK** and click **Run workflow** (or wait for the push build).
4. Open the completed workflow run and download the `AyushMusic-debug-apk` artifact.
5. Extract the artifact ZIP to get `app-debug.apk`, then transfer it to your Android phone and install it. Android may ask you to allow installation from that source.

This produces a debug APK. It is not a Play Store release-signed APK.
