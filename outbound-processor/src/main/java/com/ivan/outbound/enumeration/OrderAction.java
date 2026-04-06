package com.ivan.outbound.enumeration;

import lombok.Getter;

@Getter
public enum OrderAction {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private final String action;

    OrderAction(String action) {
        this.action = action;
    }
}
