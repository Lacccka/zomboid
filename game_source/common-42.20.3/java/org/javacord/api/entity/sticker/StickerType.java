/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.sticker;

public enum StickerType {
    STANDARD(1),
    SERVER(2),
    UNKNOWN(-1);

    private final int id;

    private StickerType(int id) {
        this.id = id;
    }

    public static StickerType fromId(int id) {
        for (StickerType stickerType : StickerType.values()) {
            if (stickerType.getId() != id) continue;
            return stickerType;
        }
        return UNKNOWN;
    }

    public int getId() {
        return this.id;
    }
}

