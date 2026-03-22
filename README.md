# TorBox VLC Android App

Native Android app (Kotlin + Jetpack Compose) that streams TorBox downloads directly in VLC.

## Features

- 🎬 Browse all your TorBox downloads (torrents, usenet, web downloads)
- 🎯 Filter by content type (movies/series only) 
- 📱 Native Android interface with Material Design 3
- 🚀 Stream files directly in VLC (no local download needed)
- 🔐 Secure API key storage using DataStore

## Requirements

- Android 8.0+ (API 26+)
- VLC Media Player installed from Google Play Store
- TorBox account with API key

## Setup

1. Install the APK on your Android device
2. Get your TorBox API key from [torbox.app/settings](https://torbox.app/settings)
3. Enter the API key when prompted in the app
4. Browse your downloads and tap any video file to stream in VLC

## Building

This project uses GitHub Actions for automatic APK generation:

1. Fork this repository
2. Push changes to trigger a build
3. Download the APK from the "Actions" tab → Latest build → "TorBoxVLC-debug-apk"

For local builds:
```bash
./gradlew assembleDebug
```

## Tech Stack

- **Kotlin** - Primary language
- **Jetpack Compose** - Modern UI toolkit  
- **Material Design 3** - UI design system
- **Retrofit** - HTTP client for TorBox API
- **Kotlin Serialization** - JSON parsing
- **DataStore** - Secure preferences storage
- **Navigation Compose** - Screen navigation

## API Integration

Uses TorBox public API endpoints:
- `GET /api/v1/api/torrents/mylist` - List torrent downloads
- `GET /api/v1/api/usenet/mylist` - List usenet downloads  
- `GET /api/v1/api/webdl/mylist` - List web downloads
- `GET /api/v1/api/{type}/requestdl` - Get streaming URLs

## License

MIT License - see LICENSE file for details.
