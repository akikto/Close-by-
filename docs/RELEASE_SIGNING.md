# Release Signing

Release keystores and passwords must **never** be committed to git.

## Local release build

1. Create a keystore:
   ```bash
   keytool -genkey -v -keystore closeby-release.keystore -alias closeby -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Create `android/keystore.properties` (git-ignored):
   ```properties
   storeFile=../closeby-release.keystore
   storePassword=<your-store-password>
   keyAlias=closeby
   keyPassword=<your-key-password>
   ```
3. Build release AAB:
   ```bash
   cd android
   ./gradlew bundleRelease
   ```

Output: `android/app/build/outputs/bundle/release/app-release.aab`

If `keystore.properties` is missing, `bundleRelease` falls back to the **debug** keystore so CI and local smoke builds can still produce an AAB. Production Play Store uploads must use a real release keystore.

## CI / GitHub Actions secrets

Configure these repository secrets:

| Secret | Description |
|--------|-------------|
| `RELEASE_STORE_FILE_BASE64` | Base64-encoded `.keystore` or `.jks` file |
| `RELEASE_STORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias (e.g. `closeby`) |
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
