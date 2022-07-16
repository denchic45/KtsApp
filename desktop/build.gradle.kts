import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.1.1"
}

group = "com.denchic45"
version = "1.0"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
//                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
//                implementation(compose.material3)
//                implementation("org.jetbrains.compose.material3:material3-desktop:1.1.0")
//                implementation("org.jetbrains.compose.material3:material3:1.1.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            modules("java.sql")
            packageName = "jvm"
            packageVersion = "1.0.0"
        }
    }
}