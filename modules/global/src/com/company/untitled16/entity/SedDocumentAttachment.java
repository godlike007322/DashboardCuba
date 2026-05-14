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

@NamePattern("%s|fileName")
@Table(name = "UNTITLED16_SED_DOCUMENT_ATTACHMENT")
@Entity(name = "untitled16_SedDocumentAttachment")
public class SedDocumentAttachment extends StandardEntity {
    private static final long serialVersionUID = -6867871542643710629L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DOCUMENT_ID", nullable = false)
    protected SedDocument document;

    @Column(name = "FILE_NAME", nullable = false, length = 255)
    protected String fileName;

    @Column(name = "MIME_TYPE", nullable = false, length = 100)
    protected String mimeType;

    @Column(name = "SIZE_BYTES")
    protected Long sizeBytes;

    @Lob
    @Column(name = "DESCRIPTION")
    protected String description;

    public SedDocument getDocument() {
        return document;
    }

    public void setDocument(SedDocument document) {
        this.document = document;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
