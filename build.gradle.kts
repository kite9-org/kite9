plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
}

subprojects {

	group = "org.kite9"
    version = "3.0.0"
    
    repositories {
        mavenCentral()
    }
}
