// build.gradle.kts a nivel de proyecto

plugins {
    // Otros plugins que tengas
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1") // O la versión que estés usando
        classpath("com.google.gms:google-services:4.4.2")
    }
}
