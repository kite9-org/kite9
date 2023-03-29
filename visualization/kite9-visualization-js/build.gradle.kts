plugins {
    id("org.kite9.java-conventions")
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        browser()
    }

    sourceSets {

        val jsMain by getting {
            kotlin.srcDir("src/main/kotlin")
            dependencies {
                api(project(":kite9-visualization-common"))
            }
            //kotlin.srcDir("../kite9-visualization-common/src/jvmMain/kotlin")
        }
    }
}


description = "Kite9 Visualization Javascript"

