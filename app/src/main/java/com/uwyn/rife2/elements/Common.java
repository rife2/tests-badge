/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.rife2.elements;

import com.uwyn.rife2.TestsBadgeSite;
import com.uwyn.rife2.models.TestBadge;
import rife.engine.Context;
import rife.engine.Element;
import rife.engine.annotations.ActiveSite;
import rife.engine.annotations.Parameter;

public abstract class Common implements Element {
    @ActiveSite TestsBadgeSite site;
    @Parameter String groupId;
    @Parameter String artifactId;

    TestBadge findBadge() {
        return site.badgeManager.restoreFirst(site.badgeManager.getRestoreQuery()
            .where("groupId", "=", groupId)
            .whereAnd("artifactId", "=", artifactId));
    }

    TestBadge findExistingBadge(Context c) {
        var badge = findBadge();
        if (badge == null) {
            c.setStatus(404);
            c.respond();
        }
        return badge;
    }
}
