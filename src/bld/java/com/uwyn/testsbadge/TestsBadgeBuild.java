/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge;

import rife.bld.WebProject;
import rife.bld.extension.TestsBadgeOperation;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;

import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.*;
import static rife.bld.operations.TemplateType.*;

public class TestsBadgeBuild extends WebProject {
    public TestsBadgeBuild() {
        pkg = "com.uwyn.testsbadge";
        name = "TestsBadge";
        mainClass = "com.uwyn.testsbadge.TestsBadgeSite";
        uberJarMainClass = "com.uwyn.testsbadge.TestsBadgeSiteUber";
        version = version(1,4,3);

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;

        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES);
        scope(compile)
            .include(dependency("com.uwyn.rife2", "rife2", version(1,5,22)));
        scope(runtime)
            .include(dependency("org.postgresql", "postgresql", version(42,6,0)))
            .include(dependency("com.h2database", "h2", version(2,1,214)));
        scope(test)
            .include(dependency("org.eclipse.jetty.toolchain", "jetty-jakarta-servlet-api", version(5,0,2)))
            .include(dependency("org.jsoup", "jsoup", version(1,15,4)))
            .include(dependency("org.junit.jupiter", "junit-jupiter", version(5,9,2)))
            .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1,9,2)))
            .include(dependency("org.json", "json", version(20230227)));
        scope(standalone)
            .include(dependency("org.eclipse.jetty", "jetty-server", version(11,0,15)))
            .include(dependency("org.eclipse.jetty", "jetty-servlet", version(11,0,15)))
            .include(dependency("org.slf4j", "slf4j-simple", version(2,0,7)));

        precompileOperation()
            .templateTypes(HTML, SVG, JSON);

        publishOperation()
            .repository(version.isSnapshot() ? repository("rife2-snapshots") : repository("rife2-releases"))
            .info()
                .groupId("com.uwyn")
                .artifactId("testsbadge")
                .description("Status badge that reports the number of passed and failed tests in your project.")
                .url("https://github.com/rife2/tests-badge")
                .developer(new PublishDeveloper()
                    .id("gbevin")
                    .name("Geert Bevin")
                    .email("gbevin@uwyn.com")
                    .url("https://github.com/gbevin"))
                .license(new PublishLicense()
                    .name("The Apache License, Version 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
                .scm(new PublishScm()
                    .connection("scm:git:https://github.com/rife2/tests-badge.git")
                    .developerConnection("scm:git:git@github.com:rife2/tests-badge.git")
                    .url("https://github.com/rife2/tests-badge"))
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase"));
    }

    private final TestsBadgeOperation testsBadgeOperation = new TestsBadgeOperation();
    public void test()
    throws Exception {
        testsBadgeOperation.executeOnce(() -> testsBadgeOperation
            .url(property("testsBadgeUrl"))
            .apiKey(property("testsBadgeApiKey"))
            .fromProject(this));
    }

    public static void main(String[] args) {
        new TestsBadgeBuild().start(args);
    }
}