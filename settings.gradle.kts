pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

}

rootProject.name = "KtsApp"

include (":app")
rootProject.name = "KTS"
include (":appbarcontroller")
include(":common")
include(":desktop")
