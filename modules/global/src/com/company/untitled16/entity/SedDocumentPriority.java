package com.company.untitled16.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;

public enum SedDocumentPriority implements EnumClass<String> {
    LOW("LOW"),
    NORMAL("NORMAL"),
    HIGH("HIGH"),
    URGENT("URGENT");

    private final String id;

    SedDocumentPriority(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static SedDocumentPriority fromId(String id) {
        for (SedDocumentPriority at : SedDocumentPriority.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
