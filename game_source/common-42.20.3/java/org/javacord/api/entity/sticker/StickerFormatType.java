/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.sticker;

public enum StickerFormatType {
    PNG(1),
    APNG(2),
    LOTTIE(3),
    UNKNOWN(-1);

    private final int id;

    private StickerFormatType(int id) {
        this.id = id;
    }

    public static StickerFormatType fromId(int id) {
        for (StickerFormatType stickerFormatType : StickerFormatType.values()) {
            if (stickerFormatType.getId() != id) continue;
            return stickerFormatType;
        }
        return UNKNOWN;
    }

    public int getId() {
        return this.id;
    }
}

