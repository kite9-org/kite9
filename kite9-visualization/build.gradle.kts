import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile;
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import java.util.Collections

plugins {
    kotlin("multiplatform")
    id("com.dorongold.task-tree") version "2.1.1"
    id("distribution")
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.12"
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

kotlin {

    jvmToolchain(17)

    jvm()

    js(IR) {
        browser()
        binaries.library()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
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
                implementation("org.junit.jupiter:junit-jupiter:5.10.0")
                implementation("org.junit.vintage:junit-vintage-engine:5.10.0")
                implementation("org.junit.platform:junit-platform-suite:1.10.0")
                implementation("org.junit.platform:junit-platform-suite-engine:1.10.0")
                implementation("org.xmlunit:xmlunit-core:2.9.0")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }

}

gradle.taskGraph.whenReady {
    // this is necessary because otherwise the metadata for the
    // common sourceSet fails to compile (due to DOM classes).
    tasks {
        getByName("compileCommonMainKotlinMetadata") {
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

// Configure JUnit 5
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Create a custom jacocoTestReport task for Kotlin Multiplatform
tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn(tasks.named("jvmTest"))
    
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    executionData(tasks.named("jvmTest"))
    
    sourceSets {
        sourceDirectories.from(kotlin.sourceSets["jvmMain"].kotlin)
        classDirectories.from(kotlin.targets["jvm"].compilations["main"].output.allOutputs)
    }
}