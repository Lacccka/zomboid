/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.common.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.detector.MathUtils;

public final class WhiteRectangleDetector {
    private static final int INIT_SIZE = 10;
    private static final int CORR = 1;
    private final BitMatrix image;
    private final int height;
    private final int width;
    private final int leftInit;
    private final int rightInit;
    private final int downInit;
    private final int upInit;

    public WhiteRectangleDetector(BitMatrix image) throws NotFoundException {
        this(image, 10, image.getWidth() / 2, image.getHeight() / 2);
    }

    public WhiteRectangleDetector(BitMatrix image, int initSize, int x, int y) throws NotFoundException {
        this.image = image;
        this.height = image.getHeight();
        this.width = image.getWidth();
        int halfsize = initSize / 2;
        this.leftInit = x - halfsize;
        this.rightInit = x + halfsize;
        this.upInit = y - halfsize;
        this.downInit = y + halfsize;
        if (this.upInit < 0 || this.leftInit < 0 || this.downInit >= this.height || this.rightInit >= this.width) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    public ResultPoint[] detect() throws NotFoundException {
        int left = this.leftInit;
        int right = this.rightInit;
        int up = this.upInit;
        int down = this.downInit;
        boolean sizeExceeded = false;
        boolean aBlackPointFoundOnBorder = true;
        boolean atLeastOneBlackPointFoundOnBorder = false;
        boolean atLeastOneBlackPointFoundOnRight = false;
        boolean atLeastOneBlackPointFoundOnBottom = false;
        boolean atLeastOneBlackPointFoundOnLeft = false;
        boolean atLeastOneBlackPointFoundOnTop = false;
        while (aBlackPointFoundOnBorder) {
            aBlackPointFoundOnBorder = false;
            boolean rightBorderNotWhite = true;
            while ((rightBorderNotWhite || !atLeastOneBlackPointFoundOnRight) && right < this.width) {
                rightBorderNotWhite = this.containsBlackPoint(up, down, right, false);
                if (rightBorderNotWhite) {
                    ++right;
                    aBlackPointFoundOnBorder = true;
                    atLeastOneBlackPointFoundOnRight = true;
                    continue;
                }
                if (atLeastOneBlackPointFoundOnRight) continue;
                ++right;
            }
            if (right >= this.width) {
                sizeExceeded = true;
                break;
            }
            boolean bottomBorderNotWhite = true;
            while ((bottomBorderNotWhite || !atLeastOneBlackPointFoundOnBottom) && down < this.height) {
                bottomBorderNotWhite = this.containsBlackPoint(left, right, down, true);
                if (bottomBorderNotWhite) {
                    ++down;
                    aBlackPointFoundOnBorder = true;
                    atLeastOneBlackPointFoundOnBottom = true;
                    continue;
                }
                if (atLeastOneBlackPointFoundOnBottom) continue;
                ++down;
            }
            if (down >= this.height) {
                sizeExceeded = true;
                break;
            }
            boolean leftBorderNotWhite = true;
            while ((leftBorderNotWhite || !atLeastOneBlackPointFoundOnLeft) && left >= 0) {
                leftBorderNotWhite = this.containsBlackPoint(up, down, left, false);
                if (leftBorderNotWhite) {
                    --left;
                    aBlackPointFoundOnBorder = true;
                    atLeastOneBlackPointFoundOnLeft = true;
                    continue;
                }
                if (atLeastOneBlackPointFoundOnLeft) continue;
                --left;
            }
            if (left < 0) {
                sizeExceeded = true;
                break;
            }
            boolean topBorderNotWhite = true;
            while ((topBorderNotWhite || !atLeastOneBlackPointFoundOnTop) && up >= 0) {
                topBorderNotWhite = this.containsBlackPoint(left, right, up, true);
                if (topBorderNotWhite) {
                    --up;
                    aBlackPointFoundOnBorder = true;
                    atLeastOneBlackPointFoundOnTop = true;
                    continue;
                }
                if (atLeastOneBlackPointFoundOnTop) continue;
                --up;
            }
            if (up < 0) {
                sizeExceeded = true;
                break;
            }
            if (!aBlackPointFoundOnBorder) continue;
            atLeastOneBlackPointFoundOnBorder = true;
        }
        if (!sizeExceeded && atLeastOneBlackPointFoundOnBorder) {
            int maxSize = right - left;
            ResultPoint z = null;
            for (int i = 1; i < maxSize && (z = this.getBlackPointOnSegment(left, down - i, left + i, down)) == null; ++i) {
            }
            if (z == null) {
                throw NotFoundException.getNotFoundInstance();
            }
            ResultPoint t = null;
            for (int i = 1; i < maxSize && (t = this.getBlackPointOnSegment(left, up + i, left + i, up)) == null; ++i) {
            }
            if (t == null) {
                throw NotFoundException.getNotFoundInstance();
            }
            ResultPoint x = null;
            for (int i = 1; i < maxSize && (x = this.getBlackPointOnSegment(right, up + i, right - i, up)) == null; ++i) {
            }
            if (x == null) {
                throw NotFoundException.getNotFoundInstance();
            }
            ResultPoint y = null;
            for (int i = 1; i < maxSize && (y = this.getBlackPointOnSegment(right, down - i, right - i, down)) == null; ++i) {
            }
            if (y == null) {
                throw NotFoundException.getNotFoundInstance();
            }
            return this.centerEdges(y, z, x, t);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private ResultPoint getBlackPointOnSegment(float aX, float aY, float bX, float bY) {
        int dist = MathUtils.round(MathUtils.distance(aX, aY, bX, bY));
        float xStep = (bX - aX) / (float)dist;
        float yStep = (bY - aY) / (float)dist;
        for (int i = 0; i < dist; ++i) {
            int y;
            int x = MathUtils.round(aX + (float)i * xStep);
            if (!this.image.get(x, y = MathUtils.round(aY + (float)i * yStep))) continue;
            return new ResultPoint(x, y);
        }
        return null;
    }

    private ResultPoint[] centerEdges(ResultPoint y, ResultPoint z, ResultPoint x, ResultPoint t) {
        float yi = y.getX();
        float yj = y.getY();
        float zi = z.getX();
        float zj = z.getY();
        float xi = x.getX();
        float xj = x.getY();
        float ti = t.getX();
        float tj = t.getY();
        if (yi < (float)this.width / 2.0f) {
            return new ResultPoint[]{new ResultPoint(ti - 1.0f, tj + 1.0f), new ResultPoint(zi + 1.0f, zj + 1.0f), new ResultPoint(xi - 1.0f, xj - 1.0f), new ResultPoint(yi + 1.0f, yj - 1.0f)};
        }
        return new ResultPoint[]{new ResultPoint(ti + 1.0f, tj + 1.0f), new ResultPoint(zi + 1.0f, zj - 1.0f), new ResultPoint(xi - 1.0f, xj + 1.0f), new ResultPoint(yi - 1.0f, yj - 1.0f)};
    }

    private boolean containsBlackPoint(int a, int b, int fixed, boolean horizontal) {
        if (horizontal) {
            for (int x = a; x <= b; ++x) {
                if (!this.image.get(x, fixed)) continue;
                return true;
            }
        } else {
            for (int y = a; y <= b; ++y) {
                if (!this.image.get(fixed, y)) continue;
                return true;
            }
        }
        return false;
    }
}

