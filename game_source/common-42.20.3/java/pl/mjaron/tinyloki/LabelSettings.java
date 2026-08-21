/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

public class LabelSettings {
    public static final int DEFAULT_MAX_LABEL_NAME_LENGTH = 1024;
    public static final int DEFAULT_MAX_LABEL_VALUE_LENGTH = 2048;
    private int maxLabelNameLength = 1024;
    private int maxLabelValueLength = 2048;

    public LabelSettings(int maxLabelNameLength, int maxLabelValueLength) {
        this.maxLabelNameLength = maxLabelNameLength;
        this.maxLabelValueLength = maxLabelValueLength;
    }

    public LabelSettings() {
    }

    public int getMaxLabelNameLength() {
        return this.maxLabelNameLength;
    }

    public void setMaxLabelNameLength(int maxLabelNameLength) {
        this.maxLabelNameLength = maxLabelNameLength;
    }

    public int getMaxLabelValueLength() {
        return this.maxLabelValueLength;
    }

    public void setMaxLabelValueLength(int maxLabelValueLength) {
        this.maxLabelValueLength = maxLabelValueLength;
    }
}

