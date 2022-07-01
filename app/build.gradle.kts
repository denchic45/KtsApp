plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin") apply false
    id("com.google.gms.google-services")
}

apply(plugin = "androidx.navigation.safeargs.kotlin")

android {

    testOptions.apply {
        unitTests.isIncludeAndroidResources = true
//        useJUnitPlatform()
    }
    compileSdk = 32
    buildToolsVersion = "30.0.3"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


    defaultConfig {
        applicationId = "com.denchic45.kts"
        minSdk = 24
        targetSdk = 32
        versionCode = 83
        versionName = "1.0.7"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
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
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=compatibility")
    }
    kapt {
        correctErrorTypes = true
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
    }
    namespace = "com.denchic45.kts"

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }
}


dependencies {
    implementation(project((":common")))
    //Compose
    val compose_version = "1.1.1"
    implementation("androidx.compose.runtime:runtime:$compose_version")
    implementation("androidx.compose.compiler:compiler:$compose_version")
    implementation("androidx.compose.ui:ui:$compose_version")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:$compose_version")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:$compose_version")
    // Material Design
    implementation("androidx.compose.material:material:$compose_version")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:$compose_version")
    implementation("androidx.compose.material:material-icons-extended:$compose_version")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.0")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.7.0-alpha02")
    implementation("androidx.preference:preference-ktx:1.2.0")

    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.play:core-ktx:1.8.1")

    // Firebase
    implementation("com.google.firebase:firebase-storage:20.0.1")
    implementation("com.google.firebase:firebase-firestore:24.1.2") {
        exclude("com.squareup.okhttp")
    }
    implementation("io.grpc:grpc-okhttp:1.44.1") {
        exclude("com.squareup.okhttp")
    }
    implementation("com.google.firebase:firebase-analytics:21.0.0")
    implementation(platform("com.google.firebase:firebase-bom:25.12.0"))
    implementation("com.google.firebase:firebase-auth")

    implementation(project(":appbarcontroller"))
    implementation("com.github.denchic45:SearchBar:1.1")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:26.0.0"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.1")

    implementation("androidx.test:core-ktx:1.4.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.1")

    // support new language API
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation("com.github.alexmamo:FirestoreDocument-Android:0.1.5")

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")

    implementation("com.github.CanHub:Android-Image-Cropper:3.3.5")

    implementation("com.github.bumptech.glide:glide:4.13.0")
    kapt("com.github.bumptech.glide:compiler:4.13.0")

    implementation("org.apache.commons:commons-lang3:3.12.0")

    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation("io.reactivex.rxjava3:rxjava:3.1.0")
    implementation("com.jakewharton.rxbinding4:rxbinding-material:4.0.0")


//    debugImplementation "com.squareup.leakcanary:leakcanary-android:2.8.1"

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.0-rc01")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.0-rc01")
    implementation("com.github.tarek360.RichPath:animator:0.1.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("com.airbnb.android:lottie:4.0.0")

    implementation("androidx.room:room-rxjava3:2.5.0-alpha02")
    
    implementation("androidx.room:room-ktx:2.5.0-alpha02")
    implementation("androidx.room:room-runtime:2.5.0-alpha02")
    kapt("androidx.room:room-compiler:2.5.0-alpha02")

//    implementation "android.arch.persistence.room:runtime:1.1.1"
//    kapt "android.arch.persistence.room:compiler:1.1.1"

    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // dagger
    implementation("com.google.dagger:dagger:2.38.1")
    kapt("com.google.dagger:dagger-compiler:2.38.1")
    implementation("com.google.dagger:dagger-android:2.38.1")
    implementation("com.google.dagger:dagger-android-support:2.38.1")
    kapt("com.google.dagger:dagger-android-processor:2.38.1")

    implementation("org.mapstruct:mapstruct:1.5.0.Beta1")
    kapt("org.mapstruct:mapstruct-processor:1.5.0.Beta1")

    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.fasterxml.jackson.core", "jackson-core", "2.13.1")
    implementation("com.caverock:androidsvg-aar:1.4")

    // junit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("android.arch.core:core-testing:1.1.1")

    // Only needed to run tests in an IntelliJ IDEA that bundles an older version
    testImplementation("org.junit.platform:junit-platform-launcher:1.7.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.7.2")

    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("com.github.kirich1409:viewbindingpropertydelegate-noreflection:1.5.3")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // https://mvnrepository.com/artifact/com.sun.mail/javax.mail
    implementation("com.sun.mail:javax.mail:1.6.2")

}
