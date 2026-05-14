package com.company.untitled16.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@NamePattern("%s|name")
@Table(name = "UNTITLED16_SED_DOCUMENT_TYPE")
@Entity(name = "untitled16_SedDocumentType")
public class SedDocumentType extends StandardEntity {
    private static final long serialVersionUID = 7128174887520421927L;

    @Column(name = "CODE", nullable = false, unique = true, length = 50)
    protected String code;

    @Column(name = "NAME", nullable = false, length = 255)
    protected String name;

    @Lob
    @Column(name = "DESCRIPTION")
    protected String description;

    @Column(name = "RETENTION_PERIOD_DAYS")
    protected Integer retentionPeriodDays;

    @Column(name = "ACTIVE", nullable = false)
    protected Boolean active = true;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRetentionPeriodDays() {
        return retentionPeriodDays;
    }

    public void setRetentionPeriodDays(Integer retentionPeriodDays) {
        this.retentionPeriodDays = retentionPeriodDays;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
