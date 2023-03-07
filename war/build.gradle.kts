plugins {
    war
}

val testsBadgeVersion: String by rootProject.extra
version = testsBadgeVersion
group = "com.uwyn"

base {
    archivesName.set("tests-badge")
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
}