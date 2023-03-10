import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import com.uwyn.rife2.gradle.TemplateType.*
import java.net.*
import java.net.http.*

plugins {
    application
    id("com.uwyn.rife2") version "1.0.8"
    `maven-publish`
    id("org.graalvm.buildtools.native") version "0.9.20"
}

repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
}

val testsBadgeVersion by rootProject.extra { "1.4.0" }
version = testsBadgeVersion
group = "com.uwyn"

base {
    archivesName.set("tests-badge")
}

rife2 {
    version.set("1.4.1-SNAPSHOT")
    uberMainClass.set("com.uwyn.testsbadge.TestsBadgeSiteUber")
    precompiledTemplateTypes.addAll(HTML, SVG, JSON)
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.json:json:20220924")
    runtimeOnly("org.postgresql:postgresql:42.5.1")
    runtimeOnly("com.h2database:h2:2.1.214")
}

application {
    mainClass.set("com.uwyn.testsbadge.TestsBadgeSite")
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
    test {
        val apiKey = project.properties["testsBadgeApiKey"]

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

                    if (apiKey != null) {
                        val response: HttpResponse<String> = HttpClient.newHttpClient()
                            .send(
                                HttpRequest.newBuilder()
                                    .uri(
                                        URI(
                                            "https://rife2.com/tests-badge/update/com.uwyn/tests-badge?" +
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

publishing {
    repositories {
        maven {
            name = "Build"
            url = uri(rootProject.layout.buildDirectory.dir("repo"))
        }
    }
    publications {
        create<MavenPublication>("maven") {
            artifactId = rootProject.name
            from(components["java"])
        }
    }
}

graalvmNative.binaries.all {
    buildArgs.add("--enable-preview") // support for Jetty virtual threads with JDK 19
    imageName.set("tests-badge-$version")
}
