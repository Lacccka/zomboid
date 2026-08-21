/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.common;

import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DefaultGridSampler;
import com.google.zxing.common.PerspectiveTransform;

public abstract class GridSampler {
    private static GridSampler gridSampler = new DefaultGridSampler();

    public static void setGridSampler(GridSampler newGridSampler) {
        gridSampler = newGridSampler;
    }

    public static GridSampler getInstance() {
        return gridSampler;
    }

    public abstract BitMatrix sampleGrid(BitMatrix var1, int var2, int var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16, float var17, float var18, float var19) throws NotFoundException;

    public abstract BitMatrix sampleGrid(BitMatrix var1, int var2, int var3, PerspectiveTransform var4) throws NotFoundException;

    protected static void checkAndNudgePoints(BitMatrix image, float[] points) throws NotFoundException {
        int y;
        int x;
        int offset;
        int width = image.getWidth();
        int height = image.getHeight();
        boolean nudged = true;
        for (offset = 0; offset < points.length && nudged; offset += 2) {
            x = (int)points[offset];
            y = (int)points[offset + 1];
            if (x < -1 || x > width || y < -1 || y > height) {
                throw NotFoundException.getNotFoundInstance();
            }
            nudged = false;
            if (x == -1) {
                points[offset] = 0.0f;
                nudged = true;
            } else if (x == width) {
                points[offset] = width - 1;
                nudged = true;
            }
            if (y == -1) {
                points[offset + 1] = 0.0f;
                nudged = true;
                continue;
            }
            if (y != height) continue;
            points[offset + 1] = height - 1;
            nudged = true;
        }
        nudged = true;
        for (offset = points.length - 2; offset >= 0 && nudged; offset -= 2) {
            x = (int)points[offset];
            y = (int)points[offset + 1];
            if (x < -1 || x > width || y < -1 || y > height) {
                throw NotFoundException.getNotFoundInstance();
            }
            nudged = false;
            if (x == -1) {
                points[offset] = 0.0f;
                nudged = true;
            } else if (x == width) {
                points[offset] = width - 1;
                nudged = true;
            }
            if (y == -1) {
                points[offset + 1] = 0.0f;
                nudged = true;
                continue;
            }
            if (y != height) continue;
            points[offset + 1] = height - 1;
            nudged = true;
        }
    }
}

