package com.company.untitled16.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "UNTITLED16_NEWS")
@Entity(name = "untitled16_News")
public class News extends StandardEntity {
    private static final long serialVersionUID = 7933006252195117601L;


    @Column(name = "TITLE", nullable = false, length = 255)
    protected String title;

    @Lob
    @Column(name = "SHORT_TEXT")
    protected String shortText;


    @Lob
    @Column(name = "FULL_TEXT")
    protected String fullText;

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    @Column(name = "PUBLISH_DATE")
    protected Date publishDate;
}