// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin_version = "1.6.21"
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.4.2")
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("com.google.gms:google-services:4.3.13")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath ("com.squareup.sqldelight:gradle-plugin:1.5.3")
    }
}

group = "com.denchic45.kts"
version = "1.0"

allprojects {
    repositories {
        google()
        maven("https://jitpack.io")
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}