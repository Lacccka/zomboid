/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.qrcode.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.detector.AlignmentPattern;
import java.util.ArrayList;
import java.util.List;

final class AlignmentPatternFinder {
    private final BitMatrix image;
    private final List<AlignmentPattern> possibleCenters;
    private final int startX;
    private final int startY;
    private final int width;
    private final int height;
    private final float moduleSize;
    private final int[] crossCheckStateCount;
    private final ResultPointCallback resultPointCallback;

    AlignmentPatternFinder(BitMatrix image, int startX, int startY, int width, int height, float moduleSize, ResultPointCallback resultPointCallback) {
        this.image = image;
        this.possibleCenters = new ArrayList<AlignmentPattern>(5);
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.moduleSize = moduleSize;
        this.crossCheckStateCount = new int[3];
        this.resultPointCallback = resultPointCallback;
    }

    AlignmentPattern find() throws NotFoundException {
        int startX = this.startX;
        int height = this.height;
        int maxJ = startX + this.width;
        int middleI = this.startY + height / 2;
        int[] stateCount = new int[3];
        for (int iGen = 0; iGen < height; ++iGen) {
            AlignmentPattern confirmed;
            int j;
            int i = middleI + ((iGen & 1) == 0 ? (iGen + 1) / 2 : -((iGen + 1) / 2));
            stateCount[0] = 0;
            stateCount[1] = 0;
            stateCount[2] = 0;
            for (j = startX; j < maxJ && !this.image.get(j, i); ++j) {
            }
            int currentState = 0;
            while (j < maxJ) {
                if (this.image.get(j, i)) {
                    if (currentState == 1) {
                        int n = currentState;
                        stateCount[n] = stateCount[n] + 1;
                    } else if (currentState == 2) {
                        if (this.foundPatternCross(stateCount) && (confirmed = this.handlePossibleCenter(stateCount, i, j)) != null) {
                            return confirmed;
                        }
                        stateCount[0] = stateCount[2];
                        stateCount[1] = 1;
                        stateCount[2] = 0;
                        currentState = 1;
                    } else {
                        int n = ++currentState;
                        stateCount[n] = stateCount[n] + 1;
                    }
                } else {
                    if (currentState == 1) {
                        // empty if block
                    }
                    int n = ++currentState;
                    stateCount[n] = stateCount[n] + 1;
                }
                ++j;
            }
            if (!this.foundPatternCross(stateCount) || (confirmed = this.handlePossibleCenter(stateCount, i, maxJ)) == null) continue;
            return confirmed;
        }
        if (!this.possibleCenters.isEmpty()) {
            return this.possibleCenters.get(0);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static float centerFromEnd(int[] stateCount, int end) {
        return (float)(end - stateCount[2]) - (float)stateCount[1] / 2.0f;
    }

    private boolean foundPatternCross(int[] stateCount) {
        float moduleSize = this.moduleSize;
        float maxVariance = moduleSize / 2.0f;
        for (int i = 0; i < 3; ++i) {
            if (!(Math.abs(moduleSize - (float)stateCount[i]) >= maxVariance)) continue;
            return false;
        }
        return true;
    }

    private float crossCheckVertical(int startI, int centerJ, int maxCount, int originalStateCountTotal) {
        int i;
        BitMatrix image = this.image;
        int maxI = image.getHeight();
        int[] stateCount = this.crossCheckStateCount;
        stateCount[0] = 0;
        stateCount[1] = 0;
        stateCount[2] = 0;
        for (i = startI; i >= 0 && image.get(centerJ, i) && stateCount[1] <= maxCount; --i) {
            stateCount[1] = stateCount[1] + 1;
        }
        if (i < 0 || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (i >= 0 && !image.get(centerJ, i) && stateCount[0] <= maxCount) {
            stateCount[0] = stateCount[0] + 1;
            --i;
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN;
        }
        for (i = startI + 1; i < maxI && image.get(centerJ, i) && stateCount[1] <= maxCount; ++i) {
            stateCount[1] = stateCount[1] + 1;
        }
        if (i == maxI || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (i < maxI && !image.get(centerJ, i) && stateCount[2] <= maxCount) {
            stateCount[2] = stateCount[2] + 1;
            ++i;
        }
        if (stateCount[2] > maxCount) {
            return Float.NaN;
        }
        int stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2];
        if (5 * Math.abs(stateCountTotal - originalStateCountTotal) >= 2 * originalStateCountTotal) {
            return Float.NaN;
        }
        return this.foundPatternCross(stateCount) ? AlignmentPatternFinder.centerFromEnd(stateCount, i) : Float.NaN;
    }

    private AlignmentPattern handlePossibleCenter(int[] stateCount, int i, int j) {
        int stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2];
        float centerJ = AlignmentPatternFinder.centerFromEnd(stateCount, j);
        float centerI = this.crossCheckVertical(i, (int)centerJ, 2 * stateCount[1], stateCountTotal);
        if (!Float.isNaN(centerI)) {
            float estimatedModuleSize = (float)(stateCount[0] + stateCount[1] + stateCount[2]) / 3.0f;
            for (AlignmentPattern center : this.possibleCenters) {
                if (!center.aboutEquals(estimatedModuleSize, centerI, centerJ)) continue;
                return center.combineEstimate(centerI, centerJ, estimatedModuleSize);
            }
            AlignmentPattern point = new AlignmentPattern(centerJ, centerI, estimatedModuleSize);
            this.possibleCenters.add(point);
            if (this.resultPointCallback != null) {
                this.resultPointCallback.foundPossibleResultPoint(point);
            }
        }
        return null;
    }
}

