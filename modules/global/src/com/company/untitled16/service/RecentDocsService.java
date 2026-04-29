package com.company.untitled16.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface RecentDocsService {
    String NAME = "untitled16_RecentDocsService";


    void register(String entityName, UUID entityId, String caption);
    void remove(String entityName, UUID entityId);


    List<RecentDocInfo> loadLast(int limit);
    class RecentDocInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String entityName;
        private UUID entityId;
        private String caption;
        private Date visitedTs;

        public RecentDocInfo(String entityName, UUID entityId, String caption, Date visitedTs) {
            this.entityName = entityName;
            this.entityId = entityId;
            this.caption = caption;
            this.visitedTs = visitedTs;
        }

        public String getEntityName() { return entityName; }
        public UUID getEntityId() { return entityId; }
        public String getCaption() { return caption; }
        public Date getVisitedTs() { return visitedTs; }
    }
}