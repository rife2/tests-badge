/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge;

import rife.bld.WebProject;

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
        version = version(1,4,1,"SNAPSHOT");

        precompiledTemplateTypes = List.of(HTML, SVG, JSON);

        downloadSources = true;
        repositories = List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS);
        scope(compile)
            .include(dependency("com.uwyn.rife2", "rife2", version(1,5,6)));
        scope(runtime)
            .include(dependency("org.postgresql", "postgresql", version(42,6,0)))
            .include(dependency("com.h2database", "h2", version(2,1,214)));
        scope(test)
            .include(dependency("org.jsoup", "jsoup", version(1,15,4)))
            .include(dependency("org.junit.jupiter", "junit-jupiter", version(5,9,2)))
            .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1,9,2)))
            .include(dependency("org.json", "json", version(20230227)));
        scope(standalone)
            .include(dependency("org.eclipse.jetty", "jetty-server", version(11,0,14)))
            .include(dependency("org.eclipse.jetty", "jetty-servlet", version(11,0,14)))
            .include(dependency("org.slf4j", "slf4j-simple", version(2,0,7)));
    }

    @Override
    public void test()
    throws Exception {
        new BadgeTestOperation(properties().getValueString("testsBadgeApiKey"))
            .fromProject(this)
            .execute();
    }

    public static void main(String[] args) {
        new TestsBadgeBuild().start(args);
    }
}