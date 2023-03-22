plugins {
    id("org.kite9.java-conventions")
    kotlin("multiplatform")
}

kotlin {
    jvm() {
        withJava()
        dependencies {
            implementation(project(":a"))
            implementation("org.xmlunit:xmlunit-core:2.9.0")
            implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.2")
        }
    }

    sourceSets {
      //  val commonMain by getting
        val jvmMain by getting {
        //    dependsOn(commonMain)
        }
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
