plugins {
    kotlin("jvm")
    id("org.kite9.java-conventions")
}

dependencies {
    implementation(project(":kite9-visualization"))
    implementation("org.xmlunit:xmlunit-core:2.9.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}


description = "Kite9 Pipeline Common"
