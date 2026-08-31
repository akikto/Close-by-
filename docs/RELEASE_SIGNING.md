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
3. Wire signing in `app/build.gradle.kts` `release` block reading `keystore.properties`.
4. Build: `./gradlew assembleRelease`

## CI secrets

Configure in GitHub Actions (or your CI):

- `RELEASE_STORE_FILE` (base64-encoded keystore artifact)
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Do not echo secrets in build logs.
