/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.rife2;

import com.uwyn.rife2.elements.*;
import com.uwyn.rife2.models.ApiKey;
import com.uwyn.rife2.models.TestBadge;
import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.elements.*;
import rife.authentication.sessionvalidators.MemorySessionValidator;
import rife.database.Datasource;
import rife.database.exceptions.ExecutionErrorException;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.*;
import rife.template.TemplateFactory;

public class TestsBadgeSite extends Site {
    static final String DEFAULT_ADMIN_USER = "admin";
    static final String DEFAULT_ADMIN_PASSWORD = "rife2";

    final MemorySessionValidator validator = new MemorySessionValidator();
    final AuthConfig config = new AuthConfig(validator);

    public final Datasource datasource;
    public final GenericQueryManager<TestBadge> badgeManager;
    public final GenericQueryManager<ApiKey> apiManager;

    public TestsBadgeSite() {
        this(new Datasource("org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/tests-badge", "sa", "", 20));
    }

    public TestsBadgeSite(Datasource ds) {
        datasource = ds;
        badgeManager = GenericQueryManagerFactory.instance(datasource, TestBadge.class);
        apiManager = GenericQueryManagerFactory.instance(datasource, ApiKey.class);
    }

    // create the routes
    public Route update = post("/update", PathInfoHandling.MAP(TestsBadgeSite::badgePathInfo), Update.class);
    public Route badge = get("/badge", PathInfoHandling.MAP(TestsBadgeSite::badgePathInfo), Badge.class);
    public Route info = get("/info", PathInfoHandling.MAP(TestsBadgeSite::badgePathInfo), Info.class);
    public Route api;

    private static void badgePathInfo(PathInfoMapping m) {
        m.p("groupId", "[\\w\\.\\-_]+").s().p("artifactId", "[\\w\\.\\-_]+");
    }

    // set up the backend at startup
    public void setup() {
        // install the database structure if it doesn't exist yet
        try {
            badgeManager.install();
        } catch (ExecutionErrorException e) {
            if (!e.getCause().getMessage().contains("already exists")) throw e;
        }
        try {
            apiManager.install();
        } catch (ExecutionErrorException e) {
            if (!e.getCause().getMessage().contains("already exists")) throw e;
        }

        // set up the protected API admin site section
        var login = route("/login", new Login(config, TemplateFactory.HTML.get("login")));
        group(new Router() {
            public void setup() {
                before(new Authenticated(config));
                api = route("/api", Api.class);
            }
        });

        // set up the authentication
        config
            .loginRoute(login)
            .landingRoute(api);

        var admin_user = System.getProperty("admin.username", DEFAULT_ADMIN_USER);
        var admin_password = System.getProperty("admin.password", DEFAULT_ADMIN_PASSWORD);
        validator.getCredentialsManager()
            .addUser(admin_user, new RoleUserAttributes().password(admin_password));
    }

    public static void main(String[] args) {
        new Server().start(new TestsBadgeSite());
    }
}
