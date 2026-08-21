/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.common.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;

public final class MonochromeRectangleDetector {
    private static final int MAX_MODULES = 32;
    private final BitMatrix image;

    public MonochromeRectangleDetector(BitMatrix image) {
        this.image = image;
    }

    public ResultPoint[] detect() throws NotFoundException {
        int height = this.image.getHeight();
        int width = this.image.getWidth();
        int halfHeight = height / 2;
        int halfWidth = width / 2;
        int deltaY = Math.max(1, height / 256);
        int deltaX = Math.max(1, width / 256);
        int top = 0;
        int bottom = height;
        int left = 0;
        int right = width;
        ResultPoint pointA = this.findCornerFromCenter(halfWidth, 0, left, right, halfHeight, -deltaY, top, bottom, halfWidth / 2);
        top = (int)pointA.getY() - 1;
        ResultPoint pointB = this.findCornerFromCenter(halfWidth, -deltaX, left, right, halfHeight, 0, top, bottom, halfHeight / 2);
        left = (int)pointB.getX() - 1;
        ResultPoint pointC = this.findCornerFromCenter(halfWidth, deltaX, left, right, halfHeight, 0, top, bottom, halfHeight / 2);
        right = (int)pointC.getX() + 1;
        ResultPoint pointD = this.findCornerFromCenter(halfWidth, 0, left, right, halfHeight, deltaY, top, bottom, halfWidth / 2);
        bottom = (int)pointD.getY() + 1;
        pointA = this.findCornerFromCenter(halfWidth, 0, left, right, halfHeight, -deltaY, top, bottom, halfWidth / 4);
        return new ResultPoint[]{pointA, pointB, pointC, pointD};
    }

    private ResultPoint findCornerFromCenter(int centerX, int deltaX, int left, int right, int centerY, int deltaY, int top, int bottom, int maxWhiteRun) throws NotFoundException {
        int[] lastRange = null;
        int y = centerY;
        for (int x = centerX; y < bottom && y >= top && x < right && x >= left; y += deltaY, x += deltaX) {
            int[] range = deltaX == 0 ? this.blackWhiteRange(y, maxWhiteRun, left, right, true) : this.blackWhiteRange(x, maxWhiteRun, top, bottom, false);
            if (range == null) {
                if (lastRange == null) {
                    throw NotFoundException.getNotFoundInstance();
                }
                if (deltaX == 0) {
                    int lastY = y - deltaY;
                    if (lastRange[0] < centerX) {
                        if (lastRange[1] > centerX) {
                            return new ResultPoint(deltaY > 0 ? (float)lastRange[0] : (float)lastRange[1], lastY);
                        }
                        return new ResultPoint(lastRange[0], lastY);
                    }
                    return new ResultPoint(lastRange[1], lastY);
                }
                int lastX = x - deltaX;
                if (lastRange[0] < centerY) {
                    if (lastRange[1] > centerY) {
                        return new ResultPoint(lastX, deltaX < 0 ? (float)lastRange[0] : (float)lastRange[1]);
                    }
                    return new ResultPoint(lastX, lastRange[0]);
                }
                return new ResultPoint(lastX, lastRange[1]);
            }
            lastRange = range;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private int[] blackWhiteRange(int fixedDimension, int maxWhiteRun, int minDim, int maxDim, boolean horizontal) {
        int[] nArray;
        int center;
        int start = center = (minDim + maxDim) / 2;
        while (start >= minDim) {
            if (horizontal ? this.image.get(start, fixedDimension) : this.image.get(fixedDimension, start)) {
                --start;
                continue;
            }
            int whiteRunStart = start;
            while (--start >= minDim && !(horizontal ? this.image.get(start, fixedDimension) : this.image.get(fixedDimension, start))) {
            }
            int whiteRunSize = whiteRunStart - start;
            if (start >= minDim && whiteRunSize <= maxWhiteRun) continue;
            start = whiteRunStart;
            break;
        }
        ++start;
        int end = center;
        while (end < maxDim) {
            if (horizontal ? this.image.get(end, fixedDimension) : this.image.get(fixedDimension, end)) {
                ++end;
                continue;
            }
            int whiteRunStart = end;
            while (++end < maxDim && !(horizontal ? this.image.get(end, fixedDimension) : this.image.get(fixedDimension, end))) {
            }
            int whiteRunSize = end - whiteRunStart;
            if (end < maxDim && whiteRunSize <= maxWhiteRun) continue;
            end = whiteRunStart;
            break;
        }
        if (--end > start) {
            int[] nArray2 = new int[2];
            nArray2[0] = start;
            nArray = nArray2;
            nArray2[1] = end;
        } else {
            nArray = null;
        }
        return nArray;
    }
}

