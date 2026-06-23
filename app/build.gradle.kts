import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

// Optional release signing. If a `keystore.properties` file exists at the
// project root, a release signing config is created from it. Otherwise the
// release build remains unsigned (still assembles), so the project compiles
// out of the box without secrets.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.universalwallet.loyalty"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.universalwallet.loyalty"
        minSdk = 26
        targetSdk = 35
        // versionCode/versionName can be overridden from CI (e.g. the GitHub run
        // number) so every release increments without editing this file. They
        // fall back to the committed values for local builds.
        versionCode = (project.findProperty("appVersionCode") as String?)?.toIntOrNull()
            ?: System.getenv("APP_VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = (project.findProperty("appVersionName") as String?)
            ?: System.getenv("APP_VERSION_NAME") ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Export Room schemas to a versioned directory (enables migration testing).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // --- Core / lifecycle / activity ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    // Material Components (XML) — provides the Theme.Material3.* parent used by
    // themes.xml. Compose's material3 artifact does not ship XML themes.
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.compose)

    // --- Compose (BOM-aligned) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose)

    // --- Dependency Injection ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Database (wired now; entities/DAOs arrive in a later phase) ---
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // --- Async ---
    implementation(libs.bundles.coroutines)

    // --- Preferences ---
    implementation(libs.androidx.datastore.preferences)

    // --- Serialization ---
    implementation(libs.kotlinx.serialization.json)

    // --- Image loading ---
    implementation(libs.coil.compose)

    // --- Barcode (scan + generate) ---
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.zxing.core)

    // --- Camera (CameraX) + image metadata for scanner / image import ---
    implementation(libs.bundles.camerax)
    implementation(libs.androidx.exifinterface)

    // --- Security ---
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.security.crypto)

    // --- Logging ---
    implementation(libs.timber)

    // --- Unit tests ---
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)

    // --- Instrumented tests ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// --- Static analysis (Part 6A) ---
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    parallel = true
}

ktlint {
    android.set(true)
    // Report-only while the codebase adopts the rules; CI surfaces issues
    // without blocking. Tighten to `false` once the baseline is clean.
    ignoreFailures.set(true)
}
