package com.company.untitled16.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@NamePattern("%s: %s|assigneeRole,status")
@Table(name = "UNTITLED16_SED_DOCUMENT_ROUTE_TASK")
@Entity(name = "untitled16_SedDocumentRouteTask")
public class SedDocumentRouteTask extends StandardEntity {
    private static final long serialVersionUID = 8151525459848021649L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DOCUMENT_ID", nullable = false)
    protected SedDocument document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAGE_ID")
    protected SedRouteStage stage;

    @Column(name = "ASSIGNEE_ROLE", nullable = false, length = 100)
    protected String assigneeRole;

    @Column(name = "ASSIGNEE_NAME", length = 255)
    protected String assigneeName;

    @Column(name = "STATUS", nullable = false, length = 30)
    protected String status;

    @Column(name = "STARTED_AT")
    protected Date startedAt;

    @Column(name = "COMPLETED_AT")
    protected Date completedAt;

    @Lob
    @Column(name = "COMMENT_TEXT")
    protected String comment;

    public SedDocument getDocument() {
        return document;
    }

    public void setDocument(SedDocument document) {
        this.document = document;
    }

    public SedRouteStage getStage() {
        return stage;
    }

    public void setStage(SedRouteStage stage) {
        this.stage = stage;
    }

    public String getAssigneeRole() {
        return assigneeRole;
    }

    public void setAssigneeRole(String assigneeRole) {
        this.assigneeRole = assigneeRole;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public SedTaskStatus getStatus() {
        return status == null ? null : SedTaskStatus.fromId(status);
    }

    public void setStatus(SedTaskStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
