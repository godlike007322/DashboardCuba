package com.company.untitled16.service;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class RecentDocInfo implements Serializable {
    private String entityName;
    private UUID entityId;
    private String caption;
    private Date visitedTs;


    public RecentDocInfo() {
    }

    public RecentDocInfo(String entityName, UUID entityId, String caption, Date visitedTs) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.caption = caption;
        this.visitedTs = visitedTs;
    }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public Date getVisitedTs() { return visitedTs; }
    public void setVisitedTs(Date visitedTs) { this.visitedTs = visitedTs; }
}
