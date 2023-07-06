import org.jetbrains.kotlin.gradle.tasks.KotlinCompile;

plugins {
    id("org.kite9.java-conventions")
    kotlin("multiplatform")
    id("com.dorongold.task-tree") version "2.1.1"
}

kotlin {

    jvm {
        withJava()
        dependencies {
            implementation("org.apache.xmlgraphics:batik-svggen:1.14")
            implementation("org.apache.xmlgraphics:batik-transcoder:1.14")
            implementation("org.apache.xmlgraphics:batik-bridge:1.14")
            implementation("org.apache.xmlgraphics:batik-ext:1.14")
            implementation("org.apache.xmlgraphics:xmlgraphics-commons:2.7")
            implementation("org.apache.xmlgraphics:batik-rasterizer:1.14")
            implementation("org.apache.xmlgraphics:batik-codec:1.14")
            testImplementation("junit:junit:4.13.2")
            testImplementation("org.xmlunit:xmlunit-core:2.9.0")
        }


    }

    js(IR) {
        browser()
        binaries.library()
    }

    sourceSets {
        val common by creating

        getByName("jvmMain") {
            dependsOn(common)
        }

        getByName("jsMain") {
            dependsOn(common)
        }
    }
}

gradle.taskGraph.whenReady {
    // this is necessary because otherwise the metadata for the
    // common sourceSet fails to compile
    tasks {
        getByName("compileCommonKotlinMetadata") {
            enabled = false
        }
    }
}


description = "Kite9 Visualization"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
