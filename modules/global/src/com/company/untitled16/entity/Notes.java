package com.company.untitled16.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Table(name = "UNTITLED16_NOTES")
@Entity(name = "untitled16_Notes")
public class Notes extends StandardEntity {
    private static final long serialVersionUID = 1310305884040975064L;


    @Column(name = "TEXT", length = 1000)
    protected String text;

    @Column(name = "NOTE_DATE")
    private LocalDate noteDate;

    public LocalDate getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(LocalDate noteDate) {
        this.noteDate = noteDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}