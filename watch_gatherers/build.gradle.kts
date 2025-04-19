plugins {
    id("com.android.application")
    id("com.google.gms.google-services")  // Add this for Firebase
}

android {
    namespace = "com.example.watch_gatherers"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.watch_gatherers"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.22")
        }
    }
}

dependencies {
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.wear:wear:1.3.0")
    implementation("com.google.android.gms:play-services-wearable:18.1.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // UI components for Wear OS
    implementation("androidx.wear:wear-input:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // For curved layouts and better UX on wear
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}