plugins {
    war
}

val testsBadgeVersion: String by rootProject.extra

base {
    archivesName.set("tests-badge")
    version = testsBadgeVersion
}

repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    implementation(project(":app"))
}

tasks.war {
    webAppDirectory.set(file("../app/src/main/webapp"))
    webXml = file("src/web.xml")
    rootSpec.exclude("**/jetty*.jar", "**/slf4j*.jar")
}