import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Base64
import java.util.Properties
import org.gradle.api.GradleException

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Load Supabase config from local.properties (git-ignored) if present.
val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        load(FileInputStream(localPropsFile))
    }
}

val keystoreProperties = Properties().apply {
    val keystorePropsFile = rootProject.file("keystore.properties")
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

fun releaseSigningValue(environmentName: String, propertyName: String): String? =
    keystoreProperties.getProperty(propertyName)?.takeIf { it.isNotBlank() }
        ?: System.getenv(environmentName)?.takeIf { it.isNotBlank() }

fun resolveSigningStoreFile(): File? {
    val localPath = keystoreProperties.getProperty("storeFile")?.takeIf { it.isNotBlank() }
    if (localPath != null) {
        val localFile = rootProject.file(localPath)
        if (!localFile.isFile) {
            throw GradleException("The keystore configured in android/keystore.properties does not exist.")
        }
        return localFile
    }

    val envBase64 = System.getenv("RELEASE_STORE_FILE_BASE64")?.takeIf { it.isNotBlank() }
    if (envBase64 != null) {
        val decoded = try {
            Base64.getDecoder().decode(envBase64)
        } catch (_: IllegalArgumentException) {
            throw GradleException("RELEASE_STORE_FILE_BASE64 must contain a valid base64-encoded keystore.")
        }
        val out = File(rootProject.layout.buildDirectory.get().asFile, "release.keystore")
        out.parentFile.mkdirs()
        FileOutputStream(out).use { it.write(decoded) }
        return out
    }
    val path = System.getenv("RELEASE_STORE_FILE")?.takeIf { it.isNotBlank() } ?: return null
    val file = rootProject.file(path)
    return file.takeIf { it.exists() }
}

android {
    namespace = "com.closeby.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.closeby.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.0-rc1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String", "SUPABASE_URL",
            "\"${localProperties.getProperty("SUPABASE_URL", "")}\""
        )
        buildConfigField(
            "String", "SUPABASE_ANON_KEY",
            "\"${localProperties.getProperty("SUPABASE_ANON_KEY", "")}\""
        )
    }

    signingConfigs {
        create("release") {
            val store = resolveSigningStoreFile()
            if (store != null) {
                storeFile = store
                storePassword = releaseSigningValue("RELEASE_STORE_PASSWORD", "storePassword")
                keyAlias = releaseSigningValue("RELEASE_KEY_ALIAS", "keyAlias")
                keyPassword = releaseSigningValue("RELEASE_KEY_PASSWORD", "keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

val verifyReleaseSigning = tasks.register("verifyReleaseSigning") {
    group = "verification"
    description = "Ensures release artifacts use a configured release keystore, never the debug key."
    doLast {
        val signing = android.signingConfigs.getByName("release")
        val missing = buildList {
            if (signing.storeFile?.isFile != true) add("keystore file")
            if (signing.storePassword.isNullOrBlank()) add("store password")
            if (signing.keyAlias.isNullOrBlank()) add("key alias")
            if (signing.keyPassword.isNullOrBlank()) add("key password")
        }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Release signing is required for release artifacts. Missing: ${missing.joinToString()}. " +
                    "Configure RELEASE_* secrets or android/keystore.properties; see docs/RELEASE_SIGNING.md. " +
                    "Debug signing is not used for release builds."
            )
        }
    }
}

tasks.configureEach {
    if (name in setOf(
            "assembleRelease",
            "bundleRelease",
            "packageRelease",
            "packageReleaseBundle",
            "signReleaseBundle",
            "validateSigningRelease"
        )
    ) {
        dependsOn(verifyReleaseSigning)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.github.jan-tennert.supabase:supabase-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.4")
    implementation("io.ktor:ktor-client-android:2.3.12")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("io.coil-kt:coil-compose:2.6.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
