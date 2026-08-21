/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

public enum ButtonStyle {
    UNKNOWN(-1),
    PRIMARY(1),
    SECONDARY(2),
    SUCCESS(3),
    DANGER(4),
    LINK(5);

    private final int data;

    private ButtonStyle(int i) {
        this.data = i;
    }

    public int getValue() {
        return this.data;
    }

    public String toString() {
        return String.valueOf(this.data);
    }

    public static ButtonStyle fromName(String name) {
        switch (name) {
            case "blurple": {
                return PRIMARY;
            }
            case "grey": {
                return SECONDARY;
            }
            case "green": {
                return SUCCESS;
            }
            case "red": {
                return DANGER;
            }
            case "url": {
                return LINK;
            }
        }
        return UNKNOWN;
    }

    public static ButtonStyle fromId(int colorId) {
        switch (colorId) {
            case 1: {
                return PRIMARY;
            }
            case 2: {
                return SECONDARY;
            }
            case 3: {
                return SUCCESS;
            }
            case 4: {
                return DANGER;
            }
            case 5: {
                return LINK;
            }
        }
        return UNKNOWN;
    }
}

