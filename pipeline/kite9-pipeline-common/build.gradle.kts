plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm() {
        withJava()
        val implementation by configurations
        dependencies {
            implementation(project(":kite9-visualization-common"))
            implementation("org.xmlunit:xmlunit-core:2.9.0")
            implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.2")
        }
    }

    sourceSets {
        val jvmMain by getting {
        }
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


description = "Kite9 Pipeline Common"

