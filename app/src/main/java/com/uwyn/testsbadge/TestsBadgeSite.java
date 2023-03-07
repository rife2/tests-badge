/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge;

import com.uwyn.testsbadge.elements.*;
import com.uwyn.testsbadge.models.ApiKey;
import com.uwyn.testsbadge.models.TestBadge;
import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.elements.*;
import rife.authentication.sessionvalidators.MemorySessionValidator;
import rife.config.RifeConfig;
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

    public Datasource datasource;
    public GenericQueryManager<TestBadge> badgeManager;
    public GenericQueryManager<ApiKey> apiManager;

    public TestsBadgeSite() {
    }

    public TestsBadgeSite(Datasource ds) {
        datasource = ds;
    }

    // create the routes
    public Route update = post("/update", PathInfoHandling.MAP(TestsBadgeSite::badgePathInfo), Update.class);
    public Route badge = get("/badge", PathInfoHandling.MAP(TestsBadgeSite::badgePathInfo), Badge.class);
    public Route info = get("/info", PathInfoHandling.MAP(TestsBadgeSite::badgePathInfo), Info.class);
    public Route api;
    public Route login;

    private static void badgePathInfo(PathInfoMapping m) {
        m.p("groupId").s().p("artifactId");
    }

    // set up the backend at startup
    public void setup() {
        setupDatasource();
        setupManagers();
        setupApiAdmin();
        setupAuthentication();
    }

    private void setupDatasource() {
        if (properties().contains("tests-badge.production.deployment")) {
            var proxy_root_url = properties().getValueString("tests-badge.proxy.root");
            if (proxy_root_url != null) {
                RifeConfig.engine().setProxyRootUrl(proxy_root_url);
            }

            datasource = new Datasource(
                "org.postgresql.Driver",
                "jdbc:postgresql://localhost:5432/" + properties().getValueString("tests-badge.database.name"),
                properties().getValueString("tests-badge.database.user"),
                properties().getValueString("tests-badge.database.password"),
                10);
        }
        else if (datasource == null) {
            datasource = new Datasource("org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/tests-badge", "sa", "", 20);
        }
    }

    private void setupManagers() {
        badgeManager = GenericQueryManagerFactory.instance(datasource, TestBadge.class);
        apiManager = GenericQueryManagerFactory.instance(datasource, ApiKey.class);

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
    }

    private void setupApiAdmin() {
        login = route("/login", new Login(config, TemplateFactory.HTML.get("login")));
        group(new Router() {
            public void setup() {
                before(new Authenticated(config));
                api = route("/api", Api.class);
            }
        });
        fallback(c -> c.redirect("https://github.com/rife2/tests-badge"));
    }

    private void setupAuthentication() {
        config
            .loginRoute(login)
            .landingRoute(api);

        var admin_user = properties().getValueString("tests-badge.admin.username", DEFAULT_ADMIN_USER);
        var admin_password = properties().getValueString("tests-badge.admin.password", DEFAULT_ADMIN_PASSWORD);
        validator.getCredentialsManager()
            .addUser(admin_user, new RoleUserAttributes().password(admin_password));
    }

    public static void main(String[] args) {
        new Server().start(new TestsBadgeSite());
    }
}
