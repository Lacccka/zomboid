/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.Arrays;

public enum ComponentType {
    UNKNOWN(-1, false),
    ACTION_ROW(1, false),
    BUTTON(2, false),
    SELECT_MENU_STRING(3, true),
    TEXT_INPUT(4, false),
    SELECT_MENU_USER(5, true),
    SELECT_MENU_ROLE(6, true),
    SELECT_MENU_MENTIONABLE(7, true),
    SELECT_MENU_CHANNEL(8, true);

    private static final ComponentType[] selectMenuTypes;
    private final int value;
    private final boolean selectMenu;

    private ComponentType(int i, boolean selectMenu) {
        this.value = i;
        this.selectMenu = selectMenu;
    }

    public int value() {
        return this.value;
    }

    public boolean isSelectMenuType() {
        return this.selectMenu;
    }

    public static ComponentType[] getSelectMenuTypes() {
        return selectMenuTypes;
    }

    public String toString() {
        return String.valueOf(this.value);
    }

    public static ComponentType fromId(int id) {
        for (ComponentType value : ComponentType.values()) {
            if (value.value != id) continue;
            return value;
        }
        return UNKNOWN;
    }

    static {
        selectMenuTypes = (ComponentType[])Arrays.stream(ComponentType.values()).filter(ComponentType::isSelectMenuType).toArray(ComponentType[]::new);
    }
}

