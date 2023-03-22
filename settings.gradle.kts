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
//include(":kite9-visualization-common")
//include(":kite9-visualization-java")
//include(":kite9-pipeline-common")
////include(":kite9-server-java")
////include(":kite9-visualization-js")
//project(":kite9-visualization-common").projectDir = file("visualization/kite9-visualization-common")
//project(":kite9-visualization-java").projectDir = file("visualization/kite9-visualization-java")
//project(":kite9-pipeline-common").projectDir = file("pipeline/kite9-pipeline-common")
////project(":kite9-server-java").projectDir = file("server/kite9-server-java")
//project(":kite9-visualization-js").projectDir = file("visualization/kite9-visualization-js")
include("a")
include("b")
include("c")
