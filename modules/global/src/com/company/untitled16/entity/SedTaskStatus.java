package com.company.untitled16.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;

public enum SedTaskStatus implements EnumClass<String> {
    PLANNED("PLANNED"),
    IN_PROGRESS("IN_PROGRESS"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    REVISION_REQUIRED("REVISION_REQUIRED"),
    SKIPPED("SKIPPED");

    private final String id;

    SedTaskStatus(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static SedTaskStatus fromId(String id) {
        for (SedTaskStatus at : SedTaskStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
