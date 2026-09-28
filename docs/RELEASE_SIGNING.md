# Release Signing

Release keystores and passwords must **never** be committed to git.

## Local release build

Keep a durable, secure backup of the keystore. Play App Signing can use an upload key, but an upload key still must be preserved and must not be committed to git.

1. Create a keystore on a trusted machine (skip this if you already have the correct upload/release key):
   ```bash
   keytool -genkey -v -keystore android/closeby-release.jks -alias closeby -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Create `android/keystore.properties` (git-ignored):
   ```properties
   storeFile=closeby-release.jks
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

Release artifact tasks fail if a real release keystore and all three signing values are not configured. They never fall back to the debug key.

## Replit Secrets / CI secrets

For headless CI builds that do not have `android/keystore.properties`, configure these as Replit Secrets or repository/CI secrets. For Replit, use the secure secrets form rather than putting values in chat or source files:

| Secret | Description |
|--------|-------------|
| `RELEASE_STORE_FILE_BASE64` | Base64-encoded `.keystore` or `.jks` file |
| `RELEASE_STORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias (e.g. `closeby`) |
| `RELEASE_KEY_PASSWORD` | Key password |

On Linux, encode the keystore as one line with `base64 -w 0 closeby-release.keystore`; on macOS, use `base64 < closeby-release.keystore | tr -d '\n'`. Put the resulting value directly into the secure secret field—do not print it in a build log or paste it into chat. The build script decodes it into `android/build/release.keystore` at build time, which is git-ignored.

Also supported via environment variables (same names without requiring `keystore.properties`):

- `RELEASE_STORE_FILE` — path to keystore file on the runner
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Do not echo secrets in build logs.

Use either `RELEASE_STORE_FILE_BASE64` or a runner-local `RELEASE_STORE_FILE`/`storeFile` path. Passwords and alias can come from environment variables or the git-ignored `android/keystore.properties`.
When `android/keystore.properties` exists, its complete local signing configuration takes precedence. CI environment secrets are used when that local file is absent.

## Verify signing

```bash
cd android
./gradlew bundleRelease
jarsigner -verify -certs app/build/outputs/bundle/release/app-release.aab
```

The build runs `verifyReleaseSigning` before release packaging and reports missing configuration without printing any supplied values.

## Git-ignored files

- `android/keystore.properties`
- `*.keystore`, `*.jks`
- `android/build/release.keystore` (CI decode target)
