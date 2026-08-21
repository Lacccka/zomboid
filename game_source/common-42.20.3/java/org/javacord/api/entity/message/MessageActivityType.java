/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

public enum MessageActivityType {
    JOIN(1),
    SPECTATE(2),
    LISTEN(3),
    JOIN_REQUEST(5),
    UNKNOWN(-1);

    private final int id;

    private MessageActivityType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static MessageActivityType getMessageActivityTypeById(int id) {
        switch (id) {
            case 1: {
                return JOIN;
            }
            case 2: {
                return SPECTATE;
            }
            case 3: {
                return LISTEN;
            }
            case 5: {
                return JOIN_REQUEST;
            }
        }
        return UNKNOWN;
    }
}

