# Release Signing

Release keystores and passwords must **never** be committed to git.

## Local release build

1. Create a keystore (one-time, keep the file and passwords safe):
   ```bash
   cd android
   keytool -genkeypair -v -storetype JKS -keyalg RSA -keysize 2048 -validity 10000 \
     -keystore closeby-release.jks -alias closeby-release
   ```
2. Copy `android/keystore.properties.example` → `android/keystore.properties` and set passwords.
3. Build release AAB:
   ```bash
   cd android
   ./gradlew bundleRelease
   ```

Output: `android/app/build/outputs/bundle/release/app-release.aab`

**Release builds require a production keystore.** There is no debug-keystore fallback for `release`. Configure `keystore.properties` locally or `RELEASE_STORE_*` in CI before running `assembleRelease` / `bundleRelease`.

## CI / GitHub Actions secrets

Configure these repository secrets:

| Secret | Description |
|--------|-------------|
| `RELEASE_STORE_FILE_BASE64` | Base64-encoded `.keystore` or `.jks` file |
| `RELEASE_STORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias (e.g. `closeby-release`) |
| `RELEASE_KEY_PASSWORD` | Key password |

The build script decodes `RELEASE_STORE_FILE_BASE64` into `android/build/release.keystore` at build time (not committed).

Also supported via environment variables (same names without requiring `keystore.properties`):

- `RELEASE_STORE_FILE` — path to keystore file on the runner
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Do not echo secrets in build logs.

## Verify signing

```bash
cd android
./gradlew bundleRelease
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

## Git-ignored files

- `android/keystore.properties`
- `*.keystore`, `*.jks`
- `android/build/release.keystore` (CI decode target)
