/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned.rss;

import com.google.zxing.ResultPoint;

public final class FinderPattern {
    private final int value;
    private final int[] startEnd;
    private final ResultPoint[] resultPoints;

    public FinderPattern(int value, int[] startEnd, int start, int end, int rowNumber) {
        this.value = value;
        this.startEnd = startEnd;
        this.resultPoints = new ResultPoint[]{new ResultPoint(start, rowNumber), new ResultPoint(end, rowNumber)};
    }

    public int getValue() {
        return this.value;
    }

    public int[] getStartEnd() {
        return this.startEnd;
    }

    public ResultPoint[] getResultPoints() {
        return this.resultPoints;
    }

    public boolean equals(Object o) {
        if (!(o instanceof FinderPattern)) {
            return false;
        }
        FinderPattern that = (FinderPattern)o;
        return this.value == that.value;
    }

    public int hashCode() {
        return this.value;
    }
}

