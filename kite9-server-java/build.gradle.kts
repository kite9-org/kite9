import com.github.gradle.node.npm.task.NpxTask
import org.gradle.api.tasks.Copy
import org.gradle.plugins.ide.eclipse.model.Library
import org.gradle.plugins.ide.eclipse.model.SourceFolder
import org.gradle.plugins.ide.eclipse.model.internal.FileReferenceFactory
import org.gradle.plugins.ide.eclipse.model.Classpath

import org.gradle.plugins.ide.eclipse.model.ProjectDependency
import org.gradle.plugins.ide.eclipse.model.Variable

plugins {
    id("org.kite9.java-conventions")
    id("org.springframework.boot").version("3.2.0")
    id("com.github.node-gradle.node").version("4.0.0")
}

dependencies {
    api(project(":kite9-visualization"))
    api(project(":kite9-pipeline-common"))
    api("org.kohsuke:github-api:1.306")
    api("org.springframework.boot:spring-boot-starter-actuator:3.2.0")
    api("org.springframework.boot:spring-boot-starter-hateoas:3.2.0")
    api("org.springframework.boot:spring-boot-starter-web:3.2.0")
    api("org.springframework.boot:spring-boot-starter-oauth2-client:3.2.0")
    api("org.springframework.boot:spring-boot-starter-webflux:3.2.0")
    api("org.springframework.boot:spring-boot-starter-websocket:3.2.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.2")
    implementation("org.apache.xmlgraphics:batik-util:1.14")
    implementation("org.apache.xmlgraphics:batik-bridge:1.14")
    implementation("org.apache.xmlgraphics:batik-transcoder:1.14") {
        exclude(group = "xml-apis")
    }
    api("net.sf.saxon:Saxon-HE:12.3")
    api("org.xmlunit:xmlunit-core:2.9.0")
    api("org.webjars:bootstrap:4.3.1")
    api("org.webjars.npm:codemirror:5.58.3")
    api("org.webjars:highlightjs:9.6.0")
    api("org.webjars.npm:hint.css:2.3.2")
    api("org.webjars.npm:kotlin:1.4.30")
    testImplementation("org.springframework.security:spring-security-test:6.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.0")
    
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.100.Final:osx-x86_64")
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    
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

val copyVisualization = tasks.register<Copy>("copyVisualization") {
    from("../kite9-visualization/build/productionLibrary/")
    into("src/generated/resources/static/public/external")
    include("kite9-parent-kite9-visualization.js")
    include("kite9-parent-kite9-visualization.js.map")
}

val compile = tasks.getByName("classes") {
    dependsOn(compileTypescript)
    dependsOn(copyVisualization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

description = "Kite9 Server (Spring-Boot)"

