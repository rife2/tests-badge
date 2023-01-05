/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge.elements;

import com.uwyn.testsbadge.models.ApiKey;
import com.uwyn.testsbadge.models.TestBadge;
import rife.engine.Context;
import rife.engine.annotations.Parameter;

import java.util.Date;

public class Update extends Common {
    @Parameter String apiKey;
    @Parameter Integer passed;
    @Parameter Integer failed;
    @Parameter Integer skipped;

    public void process(Context c) {
        // ensure there's the api key is provided
        if (apiKey == null) {
            c.setStatus(401);
            return;
        }
        // ensure that the API key corresponds to the group ID and artifact ID
        var api_key = site.apiManager.restoreFirst(site.apiManager.getRestoreQuery()
            .where(new ApiKey()
                .groupId(groupId).artifactId(artifactId).apiKey(apiKey)));
        if (api_key == null || !api_key.validate()) {
            c.setStatus(401);
            return;
        }

        // retrieve the test badge
        var badge = findBadge();

        // create a new entry if none exists
        if (badge == null) {
            badge = new TestBadge().groupId(groupId).artifactId(artifactId);
        }

        // update the test badge and save it
        badge.updated(new Date());
        if (passed != null) badge.setPassed(passed);
        if (failed != null) badge.setFailed(failed);
        if (skipped != null) badge.setSkipped(skipped);
        site.badgeManager.save(badge);

        // report the test badge info
        var t = c.templateJson("info");
        t.setBean(badge);
        c.print(t);
    }
}
