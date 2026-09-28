# Close by on Replit

This repository is a native Kotlin/Jetpack Compose Android app under `android/`, not a web app. Replit's browser preview cannot display it; build an APK here, then install it on an Android device or emulator. There is no long-running run workflow because Gradle builds are one-off commands.

## Build in this workspace

Java 17 is installed through the Replit Nix configuration. The Android command-line tools and SDK platform 36 / build-tools 35.0.0 are installed under the git-ignored `.android-sdk/` directory. From the repository root:

```bash
export ANDROID_HOME="$PWD/.android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
cd android
./gradlew test assembleDebug
```

The APK is `android/app/build/outputs/apk/debug/app-debug.apk` (relative to the repository root). `./gradlew lint` runs Android lint separately.

If `.android-sdk/` is missing in a fresh workspace, install the SDK before building:

```bash
mkdir -p .android-sdk/cmdline-tools
curl -fL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/android-commandline-tools.zip
unzip -q /tmp/android-commandline-tools.zip -d .android-sdk/cmdline-tools
mv .android-sdk/cmdline-tools/cmdline-tools .android-sdk/cmdline-tools/latest
export ANDROID_HOME="$PWD/.android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_HOME" 'platforms;android-36' 'build-tools;35.0.0'
```

Without a Supabase configuration, the app builds with demo auth (OTP code `123456`; no emails are sent). To connect a real Supabase project, follow `README.md` and `docs/supabase/MIGRATION_ORDER.md`, and add the project URL and **anon/public** key to git-ignored `android/local.properties`. Never put a service-role key in an Android app.