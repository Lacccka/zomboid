/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.qrcode.detector;

import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.detector.FinderPattern;
import com.google.zxing.qrcode.detector.FinderPatternInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FinderPatternFinder {
    private static final int CENTER_QUORUM = 2;
    protected static final int MIN_SKIP = 3;
    protected static final int MAX_MODULES = 57;
    private final BitMatrix image;
    private final List<FinderPattern> possibleCenters;
    private boolean hasSkipped;
    private final int[] crossCheckStateCount;
    private final ResultPointCallback resultPointCallback;

    public FinderPatternFinder(BitMatrix image) {
        this(image, null);
    }

    public FinderPatternFinder(BitMatrix image, ResultPointCallback resultPointCallback) {
        this.image = image;
        this.possibleCenters = new ArrayList<FinderPattern>();
        this.crossCheckStateCount = new int[5];
        this.resultPointCallback = resultPointCallback;
    }

    protected final BitMatrix getImage() {
        return this.image;
    }

    protected final List<FinderPattern> getPossibleCenters() {
        return this.possibleCenters;
    }

    final FinderPatternInfo find(Map<DecodeHintType, ?> hints) throws NotFoundException {
        boolean tryHarder = hints != null && hints.containsKey((Object)DecodeHintType.TRY_HARDER);
        boolean pureBarcode = hints != null && hints.containsKey((Object)DecodeHintType.PURE_BARCODE);
        int maxI = this.image.getHeight();
        int maxJ = this.image.getWidth();
        int iSkip = 3 * maxI / 228;
        if (iSkip < 3 || tryHarder) {
            iSkip = 3;
        }
        boolean done = false;
        int[] stateCount = new int[5];
        for (int i = iSkip - 1; i < maxI && !done; i += iSkip) {
            boolean confirmed;
            stateCount[0] = 0;
            stateCount[1] = 0;
            stateCount[2] = 0;
            stateCount[3] = 0;
            stateCount[4] = 0;
            int currentState = 0;
            for (int j = 0; j < maxJ; ++j) {
                block5: {
                    block6: {
                        block7: {
                            block10: {
                                block8: {
                                    block9: {
                                        if (this.image.get(j, i)) {
                                            if (currentState & true) {
                                                // empty if block
                                            }
                                            int n = ++currentState;
                                            stateCount[n] = stateCount[n] + 1;
                                            continue;
                                        }
                                        if (currentState & true) break block5;
                                        if (currentState != 4) break block6;
                                        if (!FinderPatternFinder.foundPatternCross(stateCount)) break block7;
                                        boolean confirmed2 = this.handlePossibleCenter(stateCount, i, j, pureBarcode);
                                        if (!confirmed2) break block8;
                                        iSkip = 2;
                                        if (!this.hasSkipped) break block9;
                                        done = this.haveMultiplyConfirmedCenters();
                                        break block10;
                                    }
                                    int rowSkip = this.findRowSkip();
                                    if (rowSkip <= stateCount[2]) break block10;
                                    i += rowSkip - stateCount[2] - iSkip;
                                    j = maxJ - 1;
                                    break block10;
                                }
                                stateCount[0] = stateCount[2];
                                stateCount[1] = stateCount[3];
                                stateCount[2] = stateCount[4];
                                stateCount[3] = 1;
                                stateCount[4] = 0;
                                currentState = 3;
                                continue;
                            }
                            currentState = 0;
                            stateCount[0] = 0;
                            stateCount[1] = 0;
                            stateCount[2] = 0;
                            stateCount[3] = 0;
                            stateCount[4] = 0;
                            continue;
                        }
                        stateCount[0] = stateCount[2];
                        stateCount[1] = stateCount[3];
                        stateCount[2] = stateCount[4];
                        stateCount[3] = 1;
                        stateCount[4] = 0;
                        currentState = 3;
                        continue;
                    }
                    int n = ++currentState;
                    stateCount[n] = stateCount[n] + 1;
                    continue;
                }
                int n = currentState;
                stateCount[n] = stateCount[n] + 1;
            }
            if (!FinderPatternFinder.foundPatternCross(stateCount) || !(confirmed = this.handlePossibleCenter(stateCount, i, maxJ, pureBarcode))) continue;
            iSkip = stateCount[0];
            if (!this.hasSkipped) continue;
            done = this.haveMultiplyConfirmedCenters();
        }
        ResultPoint[] patternInfo = this.selectBestPatterns();
        ResultPoint.orderBestPatterns(patternInfo);
        return new FinderPatternInfo((FinderPattern[])patternInfo);
    }

    private static float centerFromEnd(int[] stateCount, int end) {
        return (float)(end - stateCount[4] - stateCount[3]) - (float)stateCount[2] / 2.0f;
    }

    protected static boolean foundPatternCross(int[] stateCount) {
        int totalModuleSize = 0;
        for (int i = 0; i < 5; ++i) {
            int count = stateCount[i];
            if (count == 0) {
                return false;
            }
            totalModuleSize += count;
        }
        if (totalModuleSize < 7) {
            return false;
        }
        float moduleSize = (float)totalModuleSize / 7.0f;
        float maxVariance = moduleSize / 2.0f;
        return Math.abs(moduleSize - (float)stateCount[0]) < maxVariance && Math.abs(moduleSize - (float)stateCount[1]) < maxVariance && Math.abs(3.0f * moduleSize - (float)stateCount[2]) < 3.0f * maxVariance && Math.abs(moduleSize - (float)stateCount[3]) < maxVariance && Math.abs(moduleSize - (float)stateCount[4]) < maxVariance;
    }

    private int[] getCrossCheckStateCount() {
        this.crossCheckStateCount[0] = 0;
        this.crossCheckStateCount[1] = 0;
        this.crossCheckStateCount[2] = 0;
        this.crossCheckStateCount[3] = 0;
        this.crossCheckStateCount[4] = 0;
        return this.crossCheckStateCount;
    }

    private boolean crossCheckDiagonal(int startI, int centerJ, int maxCount, int originalStateCountTotal) {
        int i;
        int[] stateCount = this.getCrossCheckStateCount();
        for (i = 0; startI >= i && centerJ >= i && this.image.get(centerJ - i, startI - i); ++i) {
            stateCount[2] = stateCount[2] + 1;
        }
        if (startI < i || centerJ < i) {
            return false;
        }
        while (startI >= i && centerJ >= i && !this.image.get(centerJ - i, startI - i) && stateCount[1] <= maxCount) {
            stateCount[1] = stateCount[1] + 1;
            ++i;
        }
        if (startI < i || centerJ < i || stateCount[1] > maxCount) {
            return false;
        }
        while (startI >= i && centerJ >= i && this.image.get(centerJ - i, startI - i) && stateCount[0] <= maxCount) {
            stateCount[0] = stateCount[0] + 1;
            ++i;
        }
        if (stateCount[0] > maxCount) {
            return false;
        }
        int maxI = this.image.getHeight();
        int maxJ = this.image.getWidth();
        i = 1;
        while (startI + i < maxI && centerJ + i < maxJ && this.image.get(centerJ + i, startI + i)) {
            stateCount[2] = stateCount[2] + 1;
            ++i;
        }
        if (startI + i >= maxI || centerJ + i >= maxJ) {
            return false;
        }
        while (startI + i < maxI && centerJ + i < maxJ && !this.image.get(centerJ + i, startI + i) && stateCount[3] < maxCount) {
            stateCount[3] = stateCount[3] + 1;
            ++i;
        }
        if (startI + i >= maxI || centerJ + i >= maxJ || stateCount[3] >= maxCount) {
            return false;
        }
        while (startI + i < maxI && centerJ + i < maxJ && this.image.get(centerJ + i, startI + i) && stateCount[4] < maxCount) {
            stateCount[4] = stateCount[4] + 1;
            ++i;
        }
        if (stateCount[4] >= maxCount) {
            return false;
        }
        int stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2] + stateCount[3] + stateCount[4];
        return Math.abs(stateCountTotal - originalStateCountTotal) < 2 * originalStateCountTotal && FinderPatternFinder.foundPatternCross(stateCount);
    }

    private float crossCheckVertical(int startI, int centerJ, int maxCount, int originalStateCountTotal) {
        int i;
        BitMatrix image = this.image;
        int maxI = image.getHeight();
        int[] stateCount = this.getCrossCheckStateCount();
        for (i = startI; i >= 0 && image.get(centerJ, i); --i) {
            stateCount[2] = stateCount[2] + 1;
        }
        if (i < 0) {
            return Float.NaN;
        }
        while (i >= 0 && !image.get(centerJ, i) && stateCount[1] <= maxCount) {
            stateCount[1] = stateCount[1] + 1;
            --i;
        }
        if (i < 0 || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (i >= 0 && image.get(centerJ, i) && stateCount[0] <= maxCount) {
            stateCount[0] = stateCount[0] + 1;
            --i;
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN;
        }
        for (i = startI + 1; i < maxI && image.get(centerJ, i); ++i) {
            stateCount[2] = stateCount[2] + 1;
        }
        if (i == maxI) {
            return Float.NaN;
        }
        while (i < maxI && !image.get(centerJ, i) && stateCount[3] < maxCount) {
            stateCount[3] = stateCount[3] + 1;
            ++i;
        }
        if (i == maxI || stateCount[3] >= maxCount) {
            return Float.NaN;
        }
        while (i < maxI && image.get(centerJ, i) && stateCount[4] < maxCount) {
            stateCount[4] = stateCount[4] + 1;
            ++i;
        }
        if (stateCount[4] >= maxCount) {
            return Float.NaN;
        }
        int stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2] + stateCount[3] + stateCount[4];
        if (5 * Math.abs(stateCountTotal - originalStateCountTotal) >= 2 * originalStateCountTotal) {
            return Float.NaN;
        }
        return FinderPatternFinder.foundPatternCross(stateCount) ? FinderPatternFinder.centerFromEnd(stateCount, i) : Float.NaN;
    }

    private float crossCheckHorizontal(int startJ, int centerI, int maxCount, int originalStateCountTotal) {
        int j;
        BitMatrix image = this.image;
        int maxJ = image.getWidth();
        int[] stateCount = this.getCrossCheckStateCount();
        for (j = startJ; j >= 0 && image.get(j, centerI); --j) {
            stateCount[2] = stateCount[2] + 1;
        }
        if (j < 0) {
            return Float.NaN;
        }
        while (j >= 0 && !image.get(j, centerI) && stateCount[1] <= maxCount) {
            stateCount[1] = stateCount[1] + 1;
            --j;
        }
        if (j < 0 || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (j >= 0 && image.get(j, centerI) && stateCount[0] <= maxCount) {
            stateCount[0] = stateCount[0] + 1;
            --j;
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN;
        }
        for (j = startJ + 1; j < maxJ && image.get(j, centerI); ++j) {
            stateCount[2] = stateCount[2] + 1;
        }
        if (j == maxJ) {
            return Float.NaN;
        }
        while (j < maxJ && !image.get(j, centerI) && stateCount[3] < maxCount) {
            stateCount[3] = stateCount[3] + 1;
            ++j;
        }
        if (j == maxJ || stateCount[3] >= maxCount) {
            return Float.NaN;
        }
        while (j < maxJ && image.get(j, centerI) && stateCount[4] < maxCount) {
            stateCount[4] = stateCount[4] + 1;
            ++j;
        }
        if (stateCount[4] >= maxCount) {
            return Float.NaN;
        }
        int stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2] + stateCount[3] + stateCount[4];
        if (5 * Math.abs(stateCountTotal - originalStateCountTotal) >= originalStateCountTotal) {
            return Float.NaN;
        }
        return FinderPatternFinder.foundPatternCross(stateCount) ? FinderPatternFinder.centerFromEnd(stateCount, j) : Float.NaN;
    }

    protected final boolean handlePossibleCenter(int[] stateCount, int i, int j, boolean pureBarcode) {
        int stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2] + stateCount[3] + stateCount[4];
        float centerJ = FinderPatternFinder.centerFromEnd(stateCount, j);
        float centerI = this.crossCheckVertical(i, (int)centerJ, stateCount[2], stateCountTotal);
        if (!(Float.isNaN(centerI) || Float.isNaN(centerJ = this.crossCheckHorizontal((int)centerJ, (int)centerI, stateCount[2], stateCountTotal)) || pureBarcode && !this.crossCheckDiagonal((int)centerI, (int)centerJ, stateCount[2], stateCountTotal))) {
            float estimatedModuleSize = (float)stateCountTotal / 7.0f;
            boolean found = false;
            for (int index = 0; index < this.possibleCenters.size(); ++index) {
                FinderPattern center = this.possibleCenters.get(index);
                if (!center.aboutEquals(estimatedModuleSize, centerI, centerJ)) continue;
                this.possibleCenters.set(index, center.combineEstimate(centerI, centerJ, estimatedModuleSize));
                found = true;
                break;
            }
            if (!found) {
                FinderPattern point = new FinderPattern(centerJ, centerI, estimatedModuleSize);
                this.possibleCenters.add(point);
                if (this.resultPointCallback != null) {
                    this.resultPointCallback.foundPossibleResultPoint(point);
                }
            }
            return true;
        }
        return false;
    }

    private int findRowSkip() {
        int max = this.possibleCenters.size();
        if (max <= 1) {
            return 0;
        }
        FinderPattern firstConfirmedCenter = null;
        for (FinderPattern center : this.possibleCenters) {
            if (center.getCount() < 2) continue;
            if (firstConfirmedCenter == null) {
                firstConfirmedCenter = center;
                continue;
            }
            this.hasSkipped = true;
            return (int)(Math.abs(firstConfirmedCenter.getX() - center.getX()) - Math.abs(firstConfirmedCenter.getY() - center.getY())) / 2;
        }
        return 0;
    }

    private boolean haveMultiplyConfirmedCenters() {
        int confirmedCount = 0;
        float totalModuleSize = 0.0f;
        int max = this.possibleCenters.size();
        for (FinderPattern pattern : this.possibleCenters) {
            if (pattern.getCount() < 2) continue;
            ++confirmedCount;
            totalModuleSize += pattern.getEstimatedModuleSize();
        }
        if (confirmedCount < 3) {
            return false;
        }
        float average = totalModuleSize / (float)max;
        float totalDeviation = 0.0f;
        for (FinderPattern pattern : this.possibleCenters) {
            totalDeviation += Math.abs(pattern.getEstimatedModuleSize() - average);
        }
        return totalDeviation <= 0.05f * totalModuleSize;
    }

    private FinderPattern[] selectBestPatterns() throws NotFoundException {
        float totalModuleSize;
        int startSize = this.possibleCenters.size();
        if (startSize < 3) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (startSize > 3) {
            totalModuleSize = 0.0f;
            float square = 0.0f;
            for (FinderPattern center : this.possibleCenters) {
                float size = center.getEstimatedModuleSize();
                totalModuleSize += size;
                square += size * size;
            }
            float average = totalModuleSize / (float)startSize;
            float stdDev = (float)Math.sqrt(square / (float)startSize - average * average);
            Collections.sort(this.possibleCenters, new FurthestFromAverageComparator(average));
            float limit = Math.max(0.2f * average, stdDev);
            for (int i = 0; i < this.possibleCenters.size() && this.possibleCenters.size() > 3; ++i) {
                FinderPattern pattern = this.possibleCenters.get(i);
                if (!(Math.abs(pattern.getEstimatedModuleSize() - average) > limit)) continue;
                this.possibleCenters.remove(i);
                --i;
            }
        }
        if (this.possibleCenters.size() > 3) {
            totalModuleSize = 0.0f;
            for (FinderPattern possibleCenter : this.possibleCenters) {
                totalModuleSize += possibleCenter.getEstimatedModuleSize();
            }
            float average = totalModuleSize / (float)this.possibleCenters.size();
            Collections.sort(this.possibleCenters, new CenterComparator(average));
            this.possibleCenters.subList(3, this.possibleCenters.size()).clear();
        }
        return new FinderPattern[]{this.possibleCenters.get(0), this.possibleCenters.get(1), this.possibleCenters.get(2)};
    }

    private static final class CenterComparator
    implements Comparator<FinderPattern>,
    Serializable {
        private final float average;

        private CenterComparator(float f) {
            this.average = f;
        }

        @Override
        public int compare(FinderPattern center1, FinderPattern center2) {
            if (center2.getCount() == center1.getCount()) {
                float dB;
                float dA = Math.abs(center2.getEstimatedModuleSize() - this.average);
                return dA < (dB = Math.abs(center1.getEstimatedModuleSize() - this.average)) ? 1 : (dA == dB ? 0 : -1);
            }
            return center2.getCount() - center1.getCount();
        }
    }

    private static final class FurthestFromAverageComparator
    implements Comparator<FinderPattern>,
    Serializable {
        private final float average;

        private FurthestFromAverageComparator(float f) {
            this.average = f;
        }

        @Override
        public int compare(FinderPattern center1, FinderPattern center2) {
            float dB;
            float dA = Math.abs(center2.getEstimatedModuleSize() - this.average);
            return dA < (dB = Math.abs(center1.getEstimatedModuleSize() - this.average)) ? -1 : (dA == dB ? 0 : 1);
        }
    }
}

