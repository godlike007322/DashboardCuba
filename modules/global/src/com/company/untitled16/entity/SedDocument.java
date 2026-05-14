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
import java.math.BigDecimal;
import java.util.Date;

@NamePattern("%s %s|registrationNumber,subject")
@Table(name = "UNTITLED16_SED_DOCUMENT")
@Entity(name = "untitled16_SedDocument")
public class SedDocument extends StandardEntity {
    private static final long serialVersionUID = 6593682855750163577L;

    @Column(name = "REGISTRATION_NUMBER", nullable = false, unique = true, length = 50)
    protected String registrationNumber;

    @Column(name = "SUBJECT", nullable = false, length = 500)
    protected String subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DOCUMENT_TYPE_ID", nullable = false)
    protected SedDocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROUTE_ID")
    protected SedBusinessRoute route;

    @Column(name = "STATUS", nullable = false, length = 30)
    protected String status;

    @Column(name = "PRIORITY", nullable = false, length = 20)
    protected String priority;

    @Column(name = "INITIATOR", nullable = false, length = 255)
    protected String initiator;

    @Column(name = "COUNTERPARTY", length = 255)
    protected String counterparty;

    @Column(name = "AMOUNT", precision = 19, scale = 2)
    protected BigDecimal amount;

    @Column(name = "DUE_DATE")
    protected Date dueDate;

    @Column(name = "SIGNED_AT")
    protected Date signedAt;

    @Lob
    @Column(name = "SUMMARY")
    protected String summary;

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public SedDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(SedDocumentType documentType) {
        this.documentType = documentType;
    }

    public SedBusinessRoute getRoute() {
        return route;
    }

    public void setRoute(SedBusinessRoute route) {
        this.route = route;
    }

    public SedDocumentStatus getStatus() {
        return status == null ? null : SedDocumentStatus.fromId(status);
    }

    public void setStatus(SedDocumentStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public SedDocumentPriority getPriority() {
        return priority == null ? null : SedDocumentPriority.fromId(priority);
    }

    public void setPriority(SedDocumentPriority priority) {
        this.priority = priority == null ? null : priority.getId();
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(String counterparty) {
        this.counterparty = counterparty;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Date signedAt) {
        this.signedAt = signedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
