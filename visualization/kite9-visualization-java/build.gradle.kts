plugins {
    id("org.kite9.java-conventions")
    kotlin("multiplatform")
    id("eclipse")
}

kotlin {
    jvm() {
        withJava()
        dependencies {
            api(project(":kite9-visualization-common"))
            implementation("org.apache.xmlgraphics:batik-svggen:1.14")
            implementation("org.apache.xmlgraphics:batik-transcoder:1.14") 
            implementation("org.apache.xmlgraphics:batik-bridge:1.14")
            implementation("org.apache.xmlgraphics:batik-ext:1.14")
            implementation("org.apache.xmlgraphics:xmlgraphics-commons:2.7")
            implementation("org.apache.xmlgraphics:batik-rasterizer:1.14")
            implementation("org.apache.xmlgraphics:batik-codec:1.14")
            testImplementation("junit:junit:4.13.2")
            testImplementation("org.xmlunit:xmlunit-core:2.9.0")
        }
        
    }

    sourceSets {
        val jvmMain by getting {
        
        }
        val jvmTest by getting
    }
}

description = "Kite9 Visualization Java Batik (Server-Side)"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
