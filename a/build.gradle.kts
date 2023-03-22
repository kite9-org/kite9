import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm() {
        withJava()
    }

    sourceSets {
        val myMain by creating
        val jvmMain by getting {
            dependsOn(myMain)
        }
    }


}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
