plugins {
    kotlin("multiplatform")
    id("eclipse")
    id("org.kite9.java-conventions")
}

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
	        dependencies {
	            implementation(project(":kite9-visualization-common"))
	            implementation("org.xmlunit:xmlunit-core:2.9.0")
	            implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.2")
	        }
        }
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


description = "Kite9 Pipeline Common"

