# Stillness

Android app that helps you use selected apps with intention. Before opening a tracked app (e.g. Instagram), Stillness asks you to state your purpose. When you leave that app, it prompts you to reflect on whether you accomplished it.

## Requirements

- Android 10+ (API 29)
- Target: Android 15 (API 35)

## Setup

1. Open the project in Android Studio (Ladybug or newer recommended).
2. Sync Gradle and run on a device or emulator.
3. On first launch, select the apps you want Stillness to monitor.
4. Enable **Stillness App Monitor** in **Settings → Accessibility**.

## How it works

| Moment | Screen |
|--------|--------|
| First launch | Select apps to track |
| Opening a tracked app | Purpose prompt ("What is your purpose?") |
| Leaving a tracked app | Reflection ("Have you accomplished your purpose?") |

Stillness uses an **Accessibility Service** to detect when foreground apps change. This is the supported approach on modern Android (10–15) for apps in this category.

## Build

```bash
./gradlew assembleDebug
```

Install the APK from `app/build/outputs/apk/debug/`.

Design specs live in `specs/`.
