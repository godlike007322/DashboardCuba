package com.company.untitled16.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Table(name = "UNTITLED16_RECENT_DOC")
@Entity(name = "untitled16_RecentDoc")
public class RecentDoc extends StandardEntity {
    private static final long serialVersionUID = 3957968931525807584L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "ENTITY_NAME", nullable = false, length = 100)
    private String entityName;

    @Column(name = "ENTITY_ID", nullable = false)
    private UUID entityId;

    @Column(name = "CAPTION", length = 255)
    private String caption;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "VISITED_TS", nullable = false)
    private Date visitedTs;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Date getVisitedTs() {
        return visitedTs;
    }

    public void setVisitedTs(Date visitedTs) {
        this.visitedTs = visitedTs;
    }
}