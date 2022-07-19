import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.1.1"
    id("com.android.library")
    id("com.squareup.sqldelight")
}

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    sourceSets {
        val ktorVersion = "2.0.3"
        val koinVersion = "3.2.0-beta-1"
        val sqlDelightVersion = "1.5.3"

        val commonJvmMain by creating {
            dependencies {

                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")

                implementation("com.squareup.sqldelight:runtime-jvm:$sqlDelightVersion")
                implementation("com.squareup.sqldelight:coroutines-extensions-jvm:$sqlDelightVersion")

                implementation("net.harawata:appdirs:1.2.1")

                api("org.jetbrains.kotlin:kotlin-reflect:1.7.0")
            }
        }

        val commonMain by getting {
            dependencies {

                api(compose.runtime)
                api(compose.foundation)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.material3)
//                api(compose.material)


                api("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                api("io.ktor:ktor-client-logging:$ktorVersion")

                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                implementation("com.squareup.sqldelight:runtime:$sqlDelightVersion")
                implementation("com.squareup.sqldelight:coroutines-extensions:$sqlDelightVersion")

                implementation("io.insert-koin:koin-core:$koinVersion")

                api("com.russhwolf:multiplatform-settings:0.9")
                api("com.russhwolf:multiplatform-settings-coroutines:0.9")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(kotlin("test-junit"))
            }
        }
        val androidMain by getting {
            dependencies {
                dependsOn(commonJvmMain)

                api("androidx.appcompat:appcompat:1.4.2")
                api("androidx.core:core-ktx:1.8.0")

                implementation("io.ktor:ktor-client-android:$ktorVersion")
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")

                // Firebase
                api(project.dependencies.platform("com.google.firebase:firebase-bom:30.2.0"))
                api("com.google.firebase:firebase-firestore") {
                    exclude("com.squareup.okhttp")
                }
                api("com.google.firebase:firebase-storage")
                api("com.google.firebase:firebase-auth")
                api("com.google.firebase:firebase-analytics")

                implementation("io.grpc:grpc-okhttp:1.44.1") {
                    exclude("com.squareup.okhttp")
                }

                api("com.google.android.play:core:1.10.3")
                api("com.google.android.play:core-ktx:1.8.1")

                api("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)

                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

                api("com.squareup.sqldelight:sqlite-driver:$sqlDelightVersion")
                api("com.squareup.sqldelight:coroutines-extensions-jvm:$sqlDelightVersion")

                dependsOn(commonJvmMain)
            }
        }
        val desktopTest by getting {
            dependencies {


            }
        }
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 31
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }
}

sqldelight {
    database("AppDatabase") {
        packageName = "com.denchic45.kts"
        sourceFolders = listOf("sqldelight")
        dialect = "sqlite:3.25"
    }
}