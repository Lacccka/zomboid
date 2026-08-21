/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.common;

import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;

public class GlobalHistogramBinarizer
extends Binarizer {
    private static final int LUMINANCE_BITS = 5;
    private static final int LUMINANCE_SHIFT = 3;
    private static final int LUMINANCE_BUCKETS = 32;
    private static final byte[] EMPTY = new byte[0];
    private byte[] luminances = EMPTY;
    private final int[] buckets = new int[32];

    public GlobalHistogramBinarizer(LuminanceSource source2) {
        super(source2);
    }

    @Override
    public BitArray getBlackRow(int y, BitArray row) throws NotFoundException {
        LuminanceSource source2 = this.getLuminanceSource();
        int width = source2.getWidth();
        if (row == null || row.getSize() < width) {
            row = new BitArray(width);
        } else {
            row.clear();
        }
        this.initArrays(width);
        byte[] localLuminances = source2.getRow(y, this.luminances);
        int[] localBuckets = this.buckets;
        for (int x = 0; x < width; ++x) {
            int pixel = localLuminances[x] & 0xFF;
            int n = pixel >> 3;
            localBuckets[n] = localBuckets[n] + 1;
        }
        int blackPoint = GlobalHistogramBinarizer.estimateBlackPoint(localBuckets);
        int left = localLuminances[0] & 0xFF;
        int center = localLuminances[1] & 0xFF;
        for (int x = 1; x < width - 1; ++x) {
            int right = localLuminances[x + 1] & 0xFF;
            int luminance = (center * 4 - left - right) / 2;
            if (luminance < blackPoint) {
                row.set(x);
            }
            left = center;
            center = right;
        }
        return row;
    }

    @Override
    public BitMatrix getBlackMatrix() throws NotFoundException {
        int pixel;
        int x;
        LuminanceSource source2 = this.getLuminanceSource();
        int width = source2.getWidth();
        int height = source2.getHeight();
        BitMatrix matrix = new BitMatrix(width, height);
        this.initArrays(width);
        int[] localBuckets = this.buckets;
        for (int y = 1; y < 5; ++y) {
            int row = height * y / 5;
            byte[] localLuminances = source2.getRow(row, this.luminances);
            int right = width * 4 / 5;
            for (x = width / 5; x < right; ++x) {
                pixel = localLuminances[x] & 0xFF;
                int n = pixel >> 3;
                localBuckets[n] = localBuckets[n] + 1;
            }
        }
        int blackPoint = GlobalHistogramBinarizer.estimateBlackPoint(localBuckets);
        byte[] localLuminances = source2.getMatrix();
        for (int y = 0; y < height; ++y) {
            int offset = y * width;
            for (x = 0; x < width; ++x) {
                pixel = localLuminances[offset + x] & 0xFF;
                if (pixel >= blackPoint) continue;
                matrix.set(x, y);
            }
        }
        return matrix;
    }

    @Override
    public Binarizer createBinarizer(LuminanceSource source2) {
        return new GlobalHistogramBinarizer(source2);
    }

    private void initArrays(int luminanceSize) {
        if (this.luminances.length < luminanceSize) {
            this.luminances = new byte[luminanceSize];
        }
        for (int x = 0; x < 32; ++x) {
            this.buckets[x] = 0;
        }
    }

    private static int estimateBlackPoint(int[] buckets) throws NotFoundException {
        int numBuckets = buckets.length;
        int maxBucketCount = 0;
        int firstPeak = 0;
        int firstPeakSize = 0;
        for (int x = 0; x < numBuckets; ++x) {
            if (buckets[x] > firstPeakSize) {
                firstPeak = x;
                firstPeakSize = buckets[x];
            }
            if (buckets[x] <= maxBucketCount) continue;
            maxBucketCount = buckets[x];
        }
        int secondPeak = 0;
        int secondPeakScore = 0;
        for (int x = 0; x < numBuckets; ++x) {
            int distanceToBiggest = x - firstPeak;
            int score = buckets[x] * distanceToBiggest * distanceToBiggest;
            if (score <= secondPeakScore) continue;
            secondPeak = x;
            secondPeakScore = score;
        }
        if (firstPeak > secondPeak) {
            int temp = firstPeak;
            firstPeak = secondPeak;
            secondPeak = temp;
        }
        if (secondPeak - firstPeak <= numBuckets / 16) {
            throw NotFoundException.getNotFoundInstance();
        }
        int bestValley = secondPeak - 1;
        int bestValleyScore = -1;
        for (int x = secondPeak - 1; x > firstPeak; --x) {
            int fromFirst = x - firstPeak;
            int score = fromFirst * fromFirst * (secondPeak - x) * (maxBucketCount - buckets[x]);
            if (score <= bestValleyScore) continue;
            bestValley = x;
            bestValleyScore = score;
        }
        return bestValley << 3;
    }
}

