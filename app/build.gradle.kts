import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.net.*
import java.net.http.*

plugins {
    application
}

repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
}

base {
    archivesName.set("tests-badge")
    version = 1.0
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.json:json:20220924")
    implementation("com.uwyn.rife2:rife2:0.9.3-SNAPSHOT")
    runtimeOnly("org.eclipse.jetty:jetty-server:11.0.13")
    runtimeOnly("org.eclipse.jetty:jetty-servlet:11.0.13")
    runtimeOnly("org.postgresql:postgresql:42.5.1")
    runtimeOnly("com.h2database:h2:2.1.214")
}

sourceSets {
    main {
        runtimeClasspath = files(file("src/main/resources"), runtimeClasspath);
    }
}

application {
    mainClass.set("com.uwyn.rife2.TestsBadgeSite")
    if (project.properties["testsBadgeAdminUsername"] != null &&
        project.properties["testsBadgeAdminPassword"] != null
    ) {
        applicationDefaultJvmArgs = listOf(
            "-Dtests-badge.admin.username=${project.properties["testsBadgeAdminUsername"].toString()}",
            "-Dtests-badge.admin.password=${project.properties["testsBadgeAdminPassword"].toString()}"
        )
    }
}

tasks {
    register<JavaExec>("precompileHtmlTemplates") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.template.TemplateDeployer")
        args = listOf(
            "-verbose",
            "-t", "html",
            "-d", "${projectDir}/build/classes/java/main",
            "-encoding", "UTF-8", "${projectDir}/src/main/resources/templates"
        )
    }

    register<JavaExec>("precompileSvgTemplates") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.template.TemplateDeployer")
        args = listOf(
            "-verbose",
            "-t", "svg",
            "-d", "${projectDir}/build/classes/java/main",
            "-encoding", "UTF-8", "${projectDir}/src/main/resources/templates"
        )
    }

    register<JavaExec>("precompileJsonTemplates") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.template.TemplateDeployer")
        args = listOf(
            "-verbose",
            "-t", "json",
            "-d", "${projectDir}/build/classes/java/main",
            "-encoding", "UTF-8", "${projectDir}/src/main/resources/templates"
        )
    }

    register("precompileTemplates") {
        dependsOn("precompileHtmlTemplates")
        dependsOn("precompileJsonTemplates")
        dependsOn("precompileSvgTemplates")
    }

    jar {
        dependsOn("precompileTemplates")
        excludes.add("templates/**")
    }

    test {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
        addTestListener(object : TestListener {
            override fun beforeTest(p0: TestDescriptor?) = Unit
            override fun beforeSuite(p0: TestDescriptor?) = Unit
            override fun afterTest(desc: TestDescriptor, result: TestResult) = Unit
            override fun afterSuite(desc: TestDescriptor, result: TestResult) {
                if (desc.parent == null) {
                    val passed = result.successfulTestCount
                    val failed = result.failedTestCount
                    val skipped = result.skippedTestCount

                    if (project.properties["testsBadgeApiKey"] != null) {
                        val apiKey = project.properties["testsBadgeApiKey"]
                        println(apiKey)
                        val response: HttpResponse<String> = HttpClient.newHttpClient()
                            .send(
                                HttpRequest.newBuilder()
                                    .uri(
                                        URI(
                                            "https://rife2.com/tests-badge/update/com.uwyn.rife2/tests-badge?" +
                                                    "apiKey=$apiKey&" +
                                                    "passed=$passed&" +
                                                    "failed=$failed&" +
                                                    "skipped=$skipped"
                                        )
                                    )
                                    .POST(HttpRequest.BodyPublishers.noBody())
                                    .build(), HttpResponse.BodyHandlers.ofString()
                            )
                        println("RESPONSE: " + response.statusCode())
                        println(response.body())
                    }
                }
            }
        })
    }
}
