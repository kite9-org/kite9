plugins {
    kotlin("multiplatform")
    id("eclipse")
}

kotlin {
    jvm() {
    }

    sourceSets {
        val jvmMain by getting
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

eclipse {
	classpath {
		defaultOutputDir = file("build/classes/kotlin/jvm/main")
	}
}