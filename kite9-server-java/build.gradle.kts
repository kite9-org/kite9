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
    id("org.springframework.boot").version("2.7.0")
    id("com.github.node-gradle.node").version("3.5.1")
    id("eclipse")
}

dependencies {
    api(project(":kite9-visualization"))
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
    testImplementation("org.springframework.security:spring-security-test:5.7.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.0")
    
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.59.Final:osx-x86_64")
    
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
    into("build/resources/main/static/public/external")
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


eclipse {

    this.pathVariables(mapOf(
        "GRADLE_CACHE" to File("/Users/rob/.gradle/caches/modules-2/files-2.1")
    ))

    project {
        buildCommand(
            mapOf("LaunchConfigHandle" to "<project>/.externalToolBuilders/Typescript Builder.launch"),
            "org.eclipse.ui.externaltools.ExternalToolBuilder"
        )
    }

    classpath {
        defaultOutputDir = file("build/classes/java/main")

       file {
           whenMerged {
               var cpEntries = (this as Classpath).entries
               cpEntries.forEach { entry ->
                   if (entry.kind == "src" && entry is SourceFolder) {
                       if (entry.output != null) {
                           if (entry.path == "src/main/java") {
                               entry.output = "build/classes/java/main"
                           } else if (entry.path == "src/main/resources") {
                               entry.output = "build/resources/main"
                           } else if (entry.path == "src/test/java") {
                               entry.output = "build/classes/java/test"
                           } else if (entry.path == "src/test/resources") {
                               entry.output = "build/resources/test"
                           }
                       }
                   }
               }

               // remove references to other subprojects and replace with jars
               cpEntries.removeAll { it ->
                   if (it is ProjectDependency) {
                       println("Checking ${it.path}")
                       (it.path == "/kite9-visualization") || (it.path == "/kite9-pipeline-common")
                   } else {
                       false
                   }
               }
               var vis = FileReferenceFactory().fromPath("KITE9_HOME/kite9-visualization/build/libs/kite9-visualization-jvm-0.1-SNAPSHOT.jar")
               var visSource = FileReferenceFactory().fromPath("KITE9_HOME/kite9-visualization/src/jvmMain/java")
               var pipe = FileReferenceFactory().fromPath("KITE9_HOME/kite9-pipeline-common/build/libs/kite9-pipeline-common-0.1-SNAPSHOT.jar")
               var pipeSource = FileReferenceFactory().fromPath("KITE9_HOME/kite9-pipeline-common/src/main/kotlin")
               var visLib = Variable(vis)
               var pipeLib = Variable(pipe)
               visLib.sourcePath = visSource
               pipeLib.sourcePath = pipeSource
               cpEntries.add(visLib)
               cpEntries.add(pipeLib)

               // remove xml-apis (should already have happened from dependencies but hasn't)
               cpEntries.removeAll { it ->
                   if (it is Variable) {
                       (it.path.indexOf("/xml-apis/xml-apis/") > -1)
                   } else {
                       false
                   }
               }
           }
       }
    }
}

