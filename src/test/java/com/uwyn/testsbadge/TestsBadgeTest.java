/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge;

import com.uwyn.testsbadge.models.ApiKey;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import rife.database.Datasource;
import rife.engine.RequestMethod;
import rife.test.MockConversation;
import rife.test.MockRequest;
import rife.tools.UniqueIDGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        var json = new JSONObject(response.getText());
        assertEquals(group_id, json.optQuery("/groupId"));
        assertEquals(artifact_id, json.optQuery("/artifactId"));
        assertEquals(passed, json.optQuery("/passed"));
        assertEquals(failed, json.optQuery("/failed"));
        assertEquals(skipped, json.optQuery("/skipped"));

        assertEquals(404, conversation.doRequest("/info/com.unknown/artifact").getStatus());
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

        var json1 = new JSONObject(response1.getText());
        assertEquals(group_id, json1.optQuery("/groupId"));
        assertEquals(artifact_id, json1.optQuery("/artifactId"));
        assertEquals(passed, json1.optQuery("/passed"));
        assertEquals(failed, json1.optQuery("/failed"));
        assertEquals(skipped, json1.optQuery("/skipped"));

        var passed2 = 444;
        var response2 = conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", api_key)
            .parameter("passed", passed2));
        assertEquals(200, response2.getStatus());

        var json2 = new JSONObject(response2.getText());
        assertEquals(group_id, json2.optQuery("/groupId"));
        assertEquals(artifact_id, json2.optQuery("/artifactId"));
        assertEquals(passed2, json2.optQuery("/passed"));
        assertEquals(failed, json2.optQuery("/failed"));
        assertEquals(skipped, json2.optQuery("/skipped"));

        var failed2 = 555;
        var skipped2 = 666;
        var response3 = conversation.doRequest("/update/" + group_id + "/" + artifact_id, new MockRequest()
            .method(RequestMethod.POST)
            .parameter("apiKey", api_key)
            .parameter("failed", failed2)
            .parameter("skipped", skipped2));
        assertEquals(200, response3.getStatus());

        var json3 = new JSONObject(response3.getText());
        assertEquals(group_id, json3.optQuery("/groupId"));
        assertEquals(artifact_id, json3.optQuery("/artifactId"));
        assertEquals(passed2, json3.optQuery("/passed"));
        assertEquals(failed2, json3.optQuery("/failed"));
        assertEquals(skipped2, json3.optQuery("/skipped"));
    }
}
