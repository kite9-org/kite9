plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm() {
        withJava()
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
