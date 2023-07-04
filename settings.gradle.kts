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
project(":kite9-visualization").projectDir = file("visualization/kite9-visualization")
project(":kite9-pipeline-common").projectDir = file("pipeline/kite9-pipeline-common")
project(":kite9-server-java").projectDir = file("server/kite9-server-java")
include("visualization:kite9-visualization:src:common:common")
findProject(":visualization:kite9-visualization:src:common:common")?.name = "common"
