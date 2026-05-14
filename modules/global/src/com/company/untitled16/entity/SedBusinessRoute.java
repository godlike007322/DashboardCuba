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

@NamePattern("%s|name")
@Table(name = "UNTITLED16_SED_BUSINESS_ROUTE")
@Entity(name = "untitled16_SedBusinessRoute")
public class SedBusinessRoute extends StandardEntity {
    private static final long serialVersionUID = -7937252047645823214L;

    @Column(name = "CODE", nullable = false, unique = true, length = 50)
    protected String code;

    @Column(name = "NAME", nullable = false, length = 255)
    protected String name;

    @Lob
    @Column(name = "DESCRIPTION")
    protected String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOCUMENT_TYPE_ID")
    protected SedDocumentType documentType;

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

    public SedDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(SedDocumentType documentType) {
        this.documentType = documentType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
