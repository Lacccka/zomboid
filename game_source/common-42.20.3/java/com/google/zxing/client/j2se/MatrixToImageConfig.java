/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.j2se;

public final class MatrixToImageConfig {
    public static final int BLACK = -16777216;
    public static final int WHITE = -1;
    private final int onColor;
    private final int offColor;

    public MatrixToImageConfig() {
        this(-16777216, -1);
    }

    public MatrixToImageConfig(int onColor, int offColor) {
        this.onColor = onColor;
        this.offColor = offColor;
    }

    public int getPixelOnColor() {
        return this.onColor;
    }

    public int getPixelOffColor() {
        return this.offColor;
    }

    int getBufferedImageColorModel() {
        if (this.onColor == -16777216 && this.offColor == -1) {
            return 12;
        }
        if (MatrixToImageConfig.hasTransparency(this.onColor) || MatrixToImageConfig.hasTransparency(this.offColor)) {
            return 2;
        }
        return 1;
    }

    private static boolean hasTransparency(int argb) {
        return (argb & 0xFF000000) != -16777216;
    }
}

