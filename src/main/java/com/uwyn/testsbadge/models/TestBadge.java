/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge.models;

import rife.validation.*;

import java.util.Date;

public class TestBadge extends MetaData {
    private Integer id;
    private String groupId;
    private String artifactId;
    private Date updated;
    private int passed;
    private int failed;
    private int skipped;

    public void activateMetaData() {
        addConstraint(new ConstrainedBean().unique("groupId", "artifactId"));

        addConstraint(new ConstrainedProperty("id")
            .identifier(true));
        addConstraint(new ConstrainedProperty("groupId")
            .notNull(true)
            .notEmpty(true));
        addConstraint(new ConstrainedProperty("artifactId")
            .notNull(true)
            .notEmpty(true));
        addConstraint(new ConstrainedProperty("date")
            .notNull(true));
        addConstraint(new ConstrainedProperty("passed")
            .notNull(true)
            .defaultValue(0));
        addConstraint(new ConstrainedProperty("failed")
            .notNull(true)
            .defaultValue(0));
        addConstraint(new ConstrainedProperty("skipped")
            .notNull(true)
            .defaultValue(0));
    }

    public Integer getId()                         { return id; }
    public void setId(Integer id)                  { this.id = id; }
    public String getGroupId()                     { return groupId; }
    public void setGroupId(String groupId)         { this.groupId = groupId; }
    public TestBadge groupId(String groupId)       { setGroupId(groupId); return this; }
    public String getArtifactId()                  { return artifactId; }
    public void setArtifactId(String artifactId)   { this.artifactId = artifactId; }
    public TestBadge artifactId(String artifactId) { setArtifactId(artifactId); return this; }
    public Date getUpdated()                       { return updated; }
    public void setUpdated(Date updated)           { this.updated = updated; }
    public TestBadge updated(Date updated)         { setUpdated(updated); return this; }
    public int getPassed()                         { return passed; }
    public void setPassed(int passed)              { this.passed = passed; }
    public TestBadge passed(int passed)            { setPassed(passed); return this; }
    public int getFailed()                         { return failed; }
    public void setFailed(int failed)              { this.failed = failed; }
    public TestBadge failed(int failed)            { setFailed(failed); return this; }
    public int getSkipped()                        { return skipped; }
    public void setSkipped(int skipped)            { this.skipped = skipped; }
    public TestBadge skipped(int skipped)          { setSkipped(skipped); return this; }
}
