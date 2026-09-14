/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge;

import com.uwyn.testsbadge.models.ApiKey;
import com.uwyn.testsbadge.models.TestBadge;
import org.junit.jupiter.api.*;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.exceptions.DatabaseException;
import rife.engine.RequestMethod;
import rife.json.Json;
import rife.test.MockConversation;
import rife.test.MockRequest;
import rife.test.MockResponse;
import rife.tools.UniqueIDGenerator;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TestsBadgeTest {
    Datasource testDatasource;
    TestsBadgeSite site;

    @BeforeEach
    void setup() {
        testDatasource = new Datasource("org.h2.Driver", "jdbc:h2:./test_dbs/h2/tests-badge", "sa", "", 5);
        site = new TestsBadgeSite(testDatasource);
    }

    @AfterEach
    void tearDown() {
        site.badgeManager.remove();
        site.apiManager.remove();
    }

    @Test
    void testInfoUnknown() {
        var conversation = new MockConversation(site);
        assertEquals(404, conversation.doRequest("/info/com.unknown/artifact").getStatus());
    }

    @Test
    void testInfo() {
        var conversation = new MockConversation(site);

        var api_key = UniqueIDGenerator.generate();
        var group_id = "com.uwyn.rife2";
        var artifact_id = "rife2";
        var passed = 2435;
        var failed = 1324;
        var skipped = 5364;

        site.apiManager.save(new ApiKey()
            .groupId(group_id)
            .artifactId(artifact_id)
            .apiKey(api_key.toString()));

        assertEquals(200, conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", api_key)
            .parameter("passed", passed)
            .parameter("failed", failed)
            .parameter("skipped", skipped)).getStatus());

        var response = conversation.doRequest("/info/" + group_id + "/" + artifact_id);
        assertEquals(200, response.getStatus());

        var json = Json.parseObject(response.getText());
        assertEquals(group_id, json.getString("groupId"));
        assertEquals(artifact_id, json.getString("artifactId"));
        assertEquals(passed, json.getInt("passed"));
        assertEquals(failed, json.getInt("failed"));
        assertEquals(skipped, json.getInt("skipped"));

        assertEquals(404, conversation.doRequest("/info/com.unknown/artifact").getStatus());
    }

    @Test
    void testBadgeRequiresUpdated() {
        new MockConversation(site);
        assertThrows(DatabaseException.class, () -> site.badgeManager.save(new TestBadge()
            .groupId("com.uwyn.rife2")
            .artifactId("rife2")));
    }

    @Test
    void testBadgeUnknown() {
        var conversation = new MockConversation(site);
        assertEquals(404, conversation.doRequest("/badge/com.unknown/artifact").getStatus());
    }

    @Test
    void testBadge() {
        var conversation = new MockConversation(site);

        var api_key = UniqueIDGenerator.generate();
        var group_id = "com.uwyn.rife2";
        var artifact_id = "rife2";
        var passed = 2435;
        var failed = 0;
        var skipped = 5364;

        site.apiManager.save(new ApiKey()
            .groupId(group_id)
            .artifactId(artifact_id)
            .apiKey(api_key.toString()));

        assertEquals(200, conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", api_key)
            .parameter("passed", passed)
            .parameter("failed", failed)
            .parameter("skipped", skipped)).getStatus());

        var response = conversation.doRequest("/badge/" + group_id + "/" + artifact_id);
        assertEquals(200, response.getStatus());
        assertEquals(group_id, response.getTemplate().getValue("groupId"));
        assertEquals(artifact_id, response.getTemplate().getValue("artifactId"));
        assertEquals(String.valueOf(passed), response.getTemplate().getValue("passed"));
        assertEquals(String.valueOf(failed), response.getTemplate().getValue("failed"));

        assertEquals(404, conversation.doRequest("/info/com.unknown/artifact").getStatus());
    }

    @Test
    void testUpdateMissingApiKey() {
        var conversation = new MockConversation(site);
        var api_key = UniqueIDGenerator.generate();
        var group_id = "group";
        var artifact_id = "artifact";

        site.apiManager.save(new ApiKey()
            .groupId(group_id)
            .artifactId(artifact_id)
            .apiKey(api_key.toString()));

        var response = conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("passed", 1)
            .parameter("failed", 2)
            .parameter("skipped", 3));
        assertEquals(401, response.getStatus());
    }

    @Test
    void testUpdateWrongApiKey() {
        var conversation = new MockConversation(site);
        var api_key = UniqueIDGenerator.generate();
        var group_id = "group";
        var artifact_id = "artifact";

        site.apiManager.save(new ApiKey()
            .groupId(group_id)
            .artifactId(artifact_id)
            .apiKey(api_key.toString()));

        var response = conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", UniqueIDGenerator.generate().toString())
            .parameter("passed", 1)
            .parameter("failed", 2)
            .parameter("skipped", 3));
        assertEquals(401, response.getStatus());
    }

    @Test
    void testUpdateDifferentApiKey() {
        var conversation = new MockConversation(site);
        var api_key = UniqueIDGenerator.generate();
        var group_id = "group";
        var artifact_id = "artifact";

        site.apiManager.save(new ApiKey()
            .groupId(group_id + "_different")
            .artifactId(artifact_id + "_different")
            .apiKey(api_key.toString()));

        var response = conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", api_key)
            .parameter("passed", 1)
            .parameter("failed", 2)
            .parameter("skipped", 3));
        assertEquals(401, response.getStatus());
    }

    @Test
    void testUpdate() {
        var conversation = new MockConversation(site);
        var api_key = UniqueIDGenerator.generate();
        var group_id = "com.uwyn.rife2";
        var artifact_id = "rife2";
        var passed = 111;
        var failed = 222;
        var skipped = 333;

        site.apiManager.save(new ApiKey()
            .groupId(group_id)
            .artifactId(artifact_id)
            .apiKey(api_key.toString()));

        var response1 = conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", api_key)
            .parameter("passed", passed)
            .parameter("failed", failed)
            .parameter("skipped", skipped));
        assertEquals(200, response1.getStatus());

        var json1 = Json.parseObject(response1.getText());
        assertEquals(group_id, json1.getString("groupId"));
        assertEquals(artifact_id, json1.getString("artifactId"));
        assertEquals(passed, json1.getInt("passed"));
        assertEquals(failed, json1.getInt("failed"));
        assertEquals(skipped, json1.getInt("skipped"));

        var passed2 = 444;
        var response2 = conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", api_key)
            .parameter("passed", passed2));
        assertEquals(200, response2.getStatus());

        var json2 = Json.parseObject(response2.getText());
        assertEquals(group_id, json2.getString("groupId"));
        assertEquals(artifact_id, json2.getString("artifactId"));
        assertEquals(passed2, json2.getInt("passed"));
        assertEquals(failed, json2.getInt("failed"));
        assertEquals(skipped, json2.getInt("skipped"));

        var failed2 = 555;
        var skipped2 = 666;
        var response3 = conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", api_key)
            .parameter("failed", failed2)
            .parameter("skipped", skipped2));
        assertEquals(200, response3.getStatus());

        var json3 = Json.parseObject(response3.getText());
        assertEquals(group_id, json3.getString("groupId"));
        assertEquals(artifact_id, json3.getString("artifactId"));
        assertEquals(passed2, json3.getInt("passed"));
        assertEquals(failed2, json3.getInt("failed"));
        assertEquals(skipped2, json3.getInt("skipped"));
    }

    @Test
    void testBadgeFailState() {
        var conversation = new MockConversation(site);
        site.badgeManager.save(new TestBadge()
            .groupId("com.uwyn.rife2")
            .artifactId("rife2")
            .updated(new Date())
            .passed(10));

        var passing = conversation.doRequest("/badge/com.uwyn.rife2/rife2").getTemplate();
        assertFalse(passing.isValueSet("state"));

        site.badgeManager.save(site.badgeManager.restoreFirst(site.badgeManager.getRestoreQuery()).failed(1));
        var failing = conversation.doRequest("/badge/com.uwyn.rife2/rife2").getTemplate();
        assertEquals("fail", failing.getValue("state"));
    }

    @Test
    void testFallback() {
        var conversation = new MockConversation(site);
        var response = conversation.doRequest("/unknown");
        assertEquals(302, response.getStatus());
        assertEquals("https://github.com/rife2/tests-badge", response.getHeader("Location"));
    }

    @Test
    void testLogin() {
        var conversation = new MockConversation(site);
        var response = login(conversation, TestsBadgeSite.DEFAULT_ADMIN_PASSWORD);
        assertEquals(302, response.getStatus());
        assertTrue(response.getNewCookieNames().contains(site.config.authCookieName()));

        assertNotNull(conversation.doRequest("/api").getParsedHtml().getFormWithName("api"));
    }

    @Test
    void testLoginInvalidCredentials() {
        var conversation = new MockConversation(site);
        var response = login(conversation, "wrong");
        assertEquals(200, response.getStatus());
        assertFalse(response.getNewCookieNames().contains(site.config.authCookieName()));
        assertEquals("These credentials are invalid", response.getParsedHtml().getDocument().select("div.errors").text());
    }

    @Test
    void testLoginRequiresCsrfToken() {
        var conversation = new MockConversation(site);
        assertTrue(conversation.doRequest("/login").getParsedHtml().getFormWithName("credentials")
            .hasParameter(RifeConfig.engine().getCsrfParameterName()));

        var response = conversation.doRequest("/login", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("login", TestsBadgeSite.DEFAULT_ADMIN_USER)
            .parameter("password", TestsBadgeSite.DEFAULT_ADMIN_PASSWORD));
        assertEquals(403, response.getStatus());
        assertFalse(response.getNewCookieNames().contains(site.config.authCookieName()));
    }

    @Test
    void testApiRequiresAuthentication() {
        var conversation = new MockConversation(site);
        var response = conversation.doRequest("/api");
        assertEquals(302, response.getStatus());
        assertEquals("http://localhost/login", response.getHeader("Location"));
    }

    @Test
    void testApiGeneratesKey() {
        var conversation = new MockConversation(site);
        login(conversation, TestsBadgeSite.DEFAULT_ADMIN_PASSWORD);

        var template = generateApiKey(conversation, "com.uwyn.rife2", "rife2").getTemplate();
        var api_key = template.getValue("apiKey");
        assertEquals(36, api_key.length());

        var info = Json.parseObject(conversation.doRequest("/info/com.uwyn.rife2/rife2").getText());
        assertEquals(0, info.getInt("passed"));
        assertEquals(0, info.getInt("failed"));
        assertEquals(0, info.getInt("skipped"));

        assertEquals(200, update(conversation, api_key).getStatus());
    }

    @Test
    void testApiRegeneratesKey() {
        var conversation = new MockConversation(site);
        login(conversation, TestsBadgeSite.DEFAULT_ADMIN_PASSWORD);

        var api_key1 = generateApiKey(conversation, "com.uwyn.rife2", "rife2").getTemplate().getValue("apiKey");
        var api_key2 = generateApiKey(conversation, "com.uwyn.rife2", "rife2").getTemplate().getValue("apiKey");
        assertNotEquals(api_key1, api_key2);
        assertEquals(1, site.apiManager.count());

        assertEquals(401, update(conversation, api_key1).getStatus());
        assertEquals(200, update(conversation, api_key2).getStatus());
    }

    @Test
    void testApiValidation() {
        var conversation = new MockConversation(site);
        login(conversation, TestsBadgeSite.DEFAULT_ADMIN_PASSWORD);

        var template = generateApiKey(conversation, "com.uwyn.rife2", "").getTemplate();
        assertFalse(template.isValueSet("apiKey"));
        assertTrue(template.isValueSet("errors:artifactId"));
        assertEquals(0, site.apiManager.count());
        assertEquals(0, site.badgeManager.count());
    }

    @Test
    void testApiRequiresCsrfToken() {
        var conversation = new MockConversation(site);
        login(conversation, TestsBadgeSite.DEFAULT_ADMIN_PASSWORD);
        assertTrue(conversation.doRequest("/api").getParsedHtml().getFormWithName("api")
            .hasParameter(RifeConfig.engine().getCsrfParameterName()));

        var response = conversation.doRequest("/api", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("groupId", "com.uwyn.rife2")
            .parameter("artifactId", "rife2"));
        assertEquals(403, response.getStatus());
        assertEquals(0, site.apiManager.count());
    }

    private MockResponse login(MockConversation conversation, String password) {
        return conversation.doRequest("/login").getParsedHtml().getFormWithName("credentials")
            .parameter("login", TestsBadgeSite.DEFAULT_ADMIN_USER)
            .parameter("password", password)
            .submit();
    }

    private MockResponse generateApiKey(MockConversation conversation, String groupId, String artifactId) {
        return conversation.doRequest("/api").getParsedHtml().getFormWithName("api")
            .parameter("groupId", groupId)
            .parameter("artifactId", artifactId)
            .submit();
    }

    private MockResponse update(MockConversation conversation, String apiKey) {
        return conversation.doRequest("/update/com.uwyn.rife2/rife2", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", apiKey)
            .parameter("passed", 1));
    }
}
