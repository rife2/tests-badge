/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge.models;

import rife.validation.*;

public class ApiKey extends MetaData {
    private Integer id;
    private String groupId;
    private String artifactId;

    private String apiKey;

    public void activateMetaData() {
        addConstraint(new ConstrainedBean().unique("groupId", "artifactId"));

        addConstraint(new ConstrainedProperty("id")
            .identifier(true));
        addConstraint(new ConstrainedProperty("apiKey")
            .notNull(true)
            .unique(true)
            .maxLength(36));
        addGroup("form")
            .addConstraint(new ConstrainedProperty("groupId")
                .notNull(true)
                .notEmpty(true))
            .addConstraint(new ConstrainedProperty("artifactId")
                .notNull(true)
                .notEmpty(true));
    }

    public Integer getId()                       { return id; }
    public void setId(Integer id)                { this.id = id; }
    public String getGroupId()                   { return groupId; }
    public void setGroupId(String groupId)       { this.groupId = groupId; }
    public ApiKey groupId(String groupId)        { setGroupId(groupId); return this; }
    public String getArtifactId()                { return artifactId; }
    public void setArtifactId(String artifactId) { this.artifactId = artifactId; }
    public ApiKey artifactId(String artifactId)  { setArtifactId(artifactId); return this; }
    public String getApiKey()                    { return apiKey; }
    public void setApiKey(String apiKey)         { this.apiKey = apiKey; }
    public ApiKey apiKey(String apiKey)          { setApiKey(apiKey); return this;}
}
