pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String

        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)

    }
}

rootProject.name = "kite9-parent"
include(":kite9-visualization")
include(":kite9-pipeline-common")
include(":kite9-server-java")
