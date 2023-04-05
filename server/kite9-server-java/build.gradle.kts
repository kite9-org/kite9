import com.github.gradle.node.npm.task.NpxTask

plugins {
    id("org.kite9.java-conventions")
    kotlin("multiplatform")
    id("org.springframework.boot").version("2.7.0")
    id("com.github.node-gradle.node").version("3.5.1")
    id("eclipse")
}

kotlin {
    jvm() {
        withJava()
        dependencies {
            api(project(":kite9-visualization-java"))
            api(project(":kite9-visualization-common"))
            api(project(":kite9-pipeline-common"))
            api("org.kohsuke:github-api:1.306")
            api("org.springframework.boot:spring-boot-starter-actuator:2.7.0")
            api("org.springframework.boot:spring-boot-starter-hateoas:2.7.0")
            api("org.springframework.boot:spring-boot-starter-web:2.7.0")
            api("org.springframework.boot:spring-boot-starter-oauth2-client:2.7.0")
            api("org.springframework.boot:spring-boot-starter-webflux:2.7.0")
            api("org.springframework.boot:spring-boot-starter-websocket:2.7.0")
            api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.3")
            implementation("org.apache.xmlgraphics:batik-util:1.14")
            implementation("org.apache.xmlgraphics:batik-bridge:1.14")
            implementation("org.apache.xmlgraphics:batik-transcoder:1.14") {
            	exclude(group = "xml-apis")
            }
            api("net.sf.saxon:Saxon-HE:10.5")
            api("org.xmlunit:xmlunit-core:2.9.0")
            api("org.webjars:bootstrap:4.3.1")
            api("org.webjars.npm:codemirror:5.58.3")
            api("org.webjars:highlightjs:9.6.0")
            api("org.webjars.npm:hint.css:2.3.2")
            api("org.webjars.npm:kotlin:1.4.30")
            //api(project(":kite9-visualization-js"))
            testImplementation("org.springframework.security:spring-security-test:5.7.1")
            testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.0")
        }
    }


    sourceSets {
        val jvmMain by getting
        val jvmTest by getting
    }
}

eclipse {
  classpath {
    containers("bob")
  }
}

node {
    download.set(true)
    workDir.set(file("${project.buildDir}/nodejs"))
    npmWorkDir.set(file("${project.buildDir}/npm"))
}

val compileTypescript = tasks.register<NpxTask>("compileTypescript") {
    dependsOn(tasks.npmInstall)
    command.set("tsc")
}

val compile = tasks.getByName("jvmMainClasses") {
    dependsOn(compileTypescript)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


description = "Kite9 Server (Spring-Boot)"
