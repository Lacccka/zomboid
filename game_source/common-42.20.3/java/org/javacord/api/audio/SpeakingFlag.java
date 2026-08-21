/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.audio;

public enum SpeakingFlag {
    SPEAKING(1),
    SOUNDSHARE(2),
    PRIORITY_SPEAKER(4);

    private int flag;

    private SpeakingFlag(int flag) {
        this.flag = flag;
    }

    public int asInt() {
        return this.flag;
    }
}

