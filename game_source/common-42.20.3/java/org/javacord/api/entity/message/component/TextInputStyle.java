/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

public enum TextInputStyle {
    UNKNOWN(-1),
    SHORT(1),
    PARAGRAPH(2);

    private final int data;

    private TextInputStyle(int i) {
        this.data = i;
    }

    public int getValue() {
        return this.data;
    }

    public String toString() {
        return String.valueOf(this.data);
    }

    public static TextInputStyle fromId(int style) {
        for (TextInputStyle value : TextInputStyle.values()) {
            if (style != value.getValue()) continue;
            return value;
        }
        return UNKNOWN;
    }
}

