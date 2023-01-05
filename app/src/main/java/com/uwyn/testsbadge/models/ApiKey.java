/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge.models;

import rife.validation.*;

public class ApiKey extends MetaData {
    private Integer id_;
    private String groupId_;
    private String artifactId_;

    private String apiKey_;

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

    public Integer getId()                       { return id_; }
    public void setId(Integer id)                { id_ = id; }
    public String getGroupId()                   { return groupId_; }
    public ApiKey groupId(String groupId)        { setGroupId(groupId); return this; }
    public void setGroupId(String groupId)       { groupId_ = groupId; }
    public String getArtifactId()                { return artifactId_; }
    public void setArtifactId(String artifactId) { artifactId_ = artifactId; }
    public ApiKey artifactId(String artifactId)  { setArtifactId(artifactId); return this; }
    public String getApiKey()                    { return apiKey_; }
    public void setApiKey(String apiKey)         { apiKey_ = apiKey; }
    public ApiKey apiKey(String apiKey)          { setApiKey(apiKey); return this;}
}
