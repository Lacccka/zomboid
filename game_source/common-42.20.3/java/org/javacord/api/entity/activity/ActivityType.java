/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.activity;

public enum ActivityType {
    PLAYING(0),
    STREAMING(1),
    LISTENING(2),
    WATCHING(3),
    CUSTOM(4),
    COMPETING(5);

    private final int id;

    private ActivityType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static ActivityType getActivityTypeById(int id) {
        switch (id) {
            case 1: {
                return STREAMING;
            }
            case 2: {
                return LISTENING;
            }
            case 3: {
                return WATCHING;
            }
            case 4: {
                return CUSTOM;
            }
            case 5: {
                return COMPETING;
            }
        }
        return PLAYING;
    }
}

