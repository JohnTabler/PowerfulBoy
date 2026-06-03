# 💪 Powerful Boy

Personal Android app for meal planning, macro tracking, meal prep, and AI-powered meal suggestions.

---

## Features

- **Dashboard** — Daily macro rings (Calories, Protein, Carbs, Fat) with meal-by-meal log
- **Food Logger** — Search Open Food Facts database or enter foods manually (all in grams)
- **Recipes** — Build recipes from ingredients with auto-calculated macros
- **Weekly Planner** — Assign recipes to Breakfast/Lunch/Dinner for a full week
- **Shopping List** — Auto-generated from your weekly plan, with checkboxes
- **Meal Prep Checklist** — One checkbox per recipe per day, with progress bar
- **AI Meal Suggestions** — Claude Haiku suggests meals based on your macro budget, restrictions, and pantry preferences (❤️ love / 🚫 avoid per ingredient)

---

## Setup

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 26+

### Steps

1. **Clone the repo**
   ```bash
   git clone https://github.com/YOUR_USERNAME/PowerfulBoy.git
   cd PowerfulBoy
   ```

2. **Open in Android Studio**
   File → Open → select the `PowerfulBoy` folder

3. **Let Gradle sync** (first sync downloads ~200MB of dependencies)

4. **Run on device or emulator**
   - Select a device (API 26+ required)
   - Click Run ▶

5. **On first launch**, the setup screen will prompt you for:
   - Macro targets
   - Dietary restrictions (pre-filled: no cheese, very minimal dairy, no beets)
   - Anthropic API key (for AI suggestions — get one at [console.anthropic.com](https://console.anthropic.com))

---

## Building the APK locally

### Debug APK (install directly)
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Install directly to connected device
```bash
./gradlew installDebug
```

### Release APK (unsigned)
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Signing the release APK (optional, for sideloading)
```bash
# 1. Generate a keystore (one-time)
keytool -genkey -v -keystore powerfulboy.jks -alias powerfulboy \
  -keyalg RSA -keysize 2048 -validity 10000

# 2. Sign the APK
apksigner sign --ks powerfulboy.jks \
  --out app-release-signed.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk

# 3. Transfer to phone and install
adb install app-release-signed.apk
```

---

## GitHub Actions

Every push to `main` automatically builds both debug and release APKs.
Download them from the **Actions** tab → latest run → **Artifacts**.

---

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Database | Room (local SQLite) |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Food search | Open Food Facts API (free, no key) |
| AI | Anthropic Claude Haiku (your key) |

---

## Data & Privacy

- All data lives **on your device only** — no accounts, no cloud sync
- Your Anthropic API key is stored in the local Room database, never transmitted anywhere except directly to `api.anthropic.com`
- AI suggestion calls send only: macro targets, restrictions list, loved/avoided ingredient names — no personal info
