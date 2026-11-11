plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cybertron.recomplabs"
    compileSdk = 36
    // Pin the NDK used by CI so ARM64 output is reproducible.
    ndkVersion = "27.2.12479018"

    defaultConfig {
        applicationId = "com.cybertron.recomplabs"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = "0.6.0"
        ndk { abiFilters += listOf("arm64-v8a") }
        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17", "-Wall", "-Wextra")
                // Build native libraries with 16 KiB page-size compatibility for newer Android devices.
                arguments += listOf("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
            }
        }
    }
    externalNativeBuild {
        cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.22.1" }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
