This project does not include the Gradle wrapper binary (gradle/wrapper/gradle-wrapper.jar),
since it's a binary file that can't be generated in this environment.

To open the project:
1. Open the `android/` folder in Android Studio (Koala/2024.1+ recommended).
2. Let Android Studio regenerate the Gradle wrapper automatically, OR run:
     gradle wrapper --gradle-version 8.7
   from the `android/` directory if you have a local Gradle install.
3. Add your Supabase credentials to `android/local.properties` (see /.env.example
   at the repo root) before building.
