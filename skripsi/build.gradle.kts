plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    kotlin("kapt") version "2.1.0" apply false // Ubah ke 2.1.0
}
