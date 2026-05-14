package com.company.untitled16.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;

public enum SedDecisionPolicy implements EnumClass<String> {
    ALL("ALL"),
    ANY("ANY");

    private final String id;

    SedDecisionPolicy(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static SedDecisionPolicy fromId(String id) {
        for (SedDecisionPolicy at : SedDecisionPolicy.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
