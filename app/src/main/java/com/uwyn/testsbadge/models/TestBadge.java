/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.testsbadge.models;

import rife.validation.*;

import java.util.Date;

public class TestBadge extends MetaData {
    private Integer id_;
    private String groupId_;
    private String artifactId_;
    private Date updated_;
    private int passed_;
    private int failed_;
    private int skipped_;

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

    public Integer getId()                         { return id_; }
    public void setId(Integer id)                  { id_ = id; }
    public String getGroupId()                     { return groupId_; }
    public void setGroupId(String groupId)         { groupId_ = groupId; }
    public TestBadge groupId(String groupId)       { setGroupId(groupId); return this; }
    public String getArtifactId()                  { return artifactId_; }
    public void setArtifactId(String artifactId)   { artifactId_ = artifactId; }
    public TestBadge artifactId(String artifactId) { setArtifactId(artifactId); return this; }
    public Date getUpdated()                       { return updated_; }
    public void setUpdated(Date updated)           { updated_ = updated; }
    public TestBadge updated(Date updated)         { setUpdated(updated); return this; }
    public int getPassed()                         { return passed_; }
    public void setPassed(int passed)              { passed_ = passed; }
    public TestBadge passed(int passed)            { setPassed(passed); return this; }
    public int getFailed()                         { return failed_; }
    public void setFailed(int failed)              { failed_ = failed; }
    public TestBadge failed(int failed)            { setFailed(failed); return this; }
    public int getSkipped()                        { return skipped_; }
    public void setSkipped(int skipped)            { skipped_ = skipped; }
    public TestBadge skipped(int skipped)          { setSkipped(skipped); return this; }
}
