plugins {
    id("org.kite9.java-conventions")
    kotlin("multiplatform")
}


kotlin {
    jvm()

    sourceSets {
        val visMain by creating
        val jvmMain by getting {
            dependsOn(visMain)
        }
    }
}

description = "Kite9 Visualization Common"
