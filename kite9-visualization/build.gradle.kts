import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile;

plugins {
    kotlin("multiplatform")
    id("com.dorongold.task-tree") version "2.1.1"
    id("distribution")
}

kotlin {

    jvmToolchain(17)

    jvm()

    js(IR) {
        browser()
        binaries.library()
    }

    sourceSets {
        val commonMain by getting

        val jvmMain by getting {
            dependencies {
                implementation("org.apache.xmlgraphics:batik-svggen:1.14")
                implementation("org.apache.xmlgraphics:batik-transcoder:1.14")
                implementation("org.apache.xmlgraphics:batik-bridge:1.14")
                implementation("org.apache.xmlgraphics:batik-ext:1.14")
                implementation("org.apache.xmlgraphics:xmlgraphics-commons:2.7")
                implementation("org.apache.xmlgraphics:batik-rasterizer:1.14")
                implementation("org.apache.xmlgraphics:batik-codec:1.14")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation("org.xmlunit:xmlunit-core:2.9.0")
            }
        }

        val jsMain by getting 
    }

}

gradle.taskGraph.whenReady {
    // this is necessary because otherwise the metadata for the
    // common sourceSet fails to compile (due to DOM classes).
    tasks {
        getByName("compileKotlinMetadata") {
            enabled = false
        }
    }
}

tasks.withType<Kotlin2JsCompile>().configureEach {
    compilerOptions {
        // this means source maps work in safari but it blows out the time
        //sourceMapEmbedSources.set(org.jetbrains.kotlin.gradle.targets.js.dsl.JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
    }
}

description = "Kite9 Visualization"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val jvmMainSourceJar by tasks.registering(Jar::class) {
    from(kotlin.sourceSets["jvmMain"].kotlin)
    archiveClassifier.set("jvm-sources")
}