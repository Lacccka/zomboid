/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.common.detector;

public final class MathUtils {
    private MathUtils() {
    }

    public static int round(float d) {
        return (int)(d + (d < 0.0f ? -0.5f : 0.5f));
    }

    public static float distance(float aX, float aY, float bX, float bY) {
        float xDiff = aX - bX;
        float yDiff = aY - bY;
        return (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public static float distance(int aX, int aY, int bX, int bY) {
        int xDiff = aX - bX;
        int yDiff = aY - bY;
        return (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
}

