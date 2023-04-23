plugins {
    id("org.kite9.java-conventions")
    kotlin("multiplatform")
}

kotlin {
    jvm() {
        withJava()
    }


    sourceSets {
        val jvmMain by getting
    }
}

description = "Kite9 Visualization Common Algorithms"


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
