package com.company.untitled16.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;

public enum SedDocumentStatus implements EnumClass<String> {
    DRAFT("DRAFT"),
    ON_APPROVAL("ON_APPROVAL"),
    REVISION_REQUIRED("REVISION_REQUIRED"),
    APPROVED("APPROVED"),
    SIGNED("SIGNED"),
    REGISTERED("REGISTERED"),
    ARCHIVED("ARCHIVED"),
    REJECTED("REJECTED");

    private final String id;

    SedDocumentStatus(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static SedDocumentStatus fromId(String id) {
        for (SedDocumentStatus at : SedDocumentStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
