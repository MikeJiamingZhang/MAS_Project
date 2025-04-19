plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    kotlin("android") version "1.8.22" apply false  // Add this line
}