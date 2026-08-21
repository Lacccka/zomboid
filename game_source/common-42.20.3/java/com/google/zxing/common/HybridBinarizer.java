/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.common;

import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;

public final class HybridBinarizer
extends GlobalHistogramBinarizer {
    private static final int BLOCK_SIZE_POWER = 3;
    private static final int BLOCK_SIZE = 8;
    private static final int BLOCK_SIZE_MASK = 7;
    private static final int MINIMUM_DIMENSION = 40;
    private static final int MIN_DYNAMIC_RANGE = 24;
    private BitMatrix matrix;

    public HybridBinarizer(LuminanceSource source2) {
        super(source2);
    }

    @Override
    public BitMatrix getBlackMatrix() throws NotFoundException {
        if (this.matrix != null) {
            return this.matrix;
        }
        LuminanceSource source2 = this.getLuminanceSource();
        int width = source2.getWidth();
        int height = source2.getHeight();
        if (width >= 40 && height >= 40) {
            byte[] luminances = source2.getMatrix();
            int subWidth = width >> 3;
            if ((width & 7) != 0) {
                ++subWidth;
            }
            int subHeight = height >> 3;
            if ((height & 7) != 0) {
                ++subHeight;
            }
            int[][] blackPoints = HybridBinarizer.calculateBlackPoints(luminances, subWidth, subHeight, width, height);
            BitMatrix newMatrix = new BitMatrix(width, height);
            HybridBinarizer.calculateThresholdForBlock(luminances, subWidth, subHeight, width, height, blackPoints, newMatrix);
            this.matrix = newMatrix;
        } else {
            this.matrix = super.getBlackMatrix();
        }
        return this.matrix;
    }

    @Override
    public Binarizer createBinarizer(LuminanceSource source2) {
        return new HybridBinarizer(source2);
    }

    private static void calculateThresholdForBlock(byte[] luminances, int subWidth, int subHeight, int width, int height, int[][] blackPoints, BitMatrix matrix) {
        for (int y = 0; y < subHeight; ++y) {
            int yoffset = y << 3;
            int maxYOffset = height - 8;
            if (yoffset > maxYOffset) {
                yoffset = maxYOffset;
            }
            for (int x = 0; x < subWidth; ++x) {
                int xoffset = x << 3;
                int maxXOffset = width - 8;
                if (xoffset > maxXOffset) {
                    xoffset = maxXOffset;
                }
                int left = HybridBinarizer.cap(x, 2, subWidth - 3);
                int top = HybridBinarizer.cap(y, 2, subHeight - 3);
                int sum = 0;
                for (int z = -2; z <= 2; ++z) {
                    int[] blackRow = blackPoints[top + z];
                    sum += blackRow[left - 2] + blackRow[left - 1] + blackRow[left] + blackRow[left + 1] + blackRow[left + 2];
                }
                int average = sum / 25;
                HybridBinarizer.thresholdBlock(luminances, xoffset, yoffset, average, width, matrix);
            }
        }
    }

    private static int cap(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }

    private static void thresholdBlock(byte[] luminances, int xoffset, int yoffset, int threshold, int stride, BitMatrix matrix) {
        int y = 0;
        int offset = yoffset * stride + xoffset;
        while (y < 8) {
            for (int x = 0; x < 8; ++x) {
                if ((luminances[offset + x] & 0xFF) > threshold) continue;
                matrix.set(xoffset + x, yoffset + y);
            }
            ++y;
            offset += stride;
        }
    }

    private static int[][] calculateBlackPoints(byte[] luminances, int subWidth, int subHeight, int width, int height) {
        int[][] blackPoints = new int[subHeight][subWidth];
        for (int y = 0; y < subHeight; ++y) {
            int yoffset = y << 3;
            int maxYOffset = height - 8;
            if (yoffset > maxYOffset) {
                yoffset = maxYOffset;
            }
            for (int x = 0; x < subWidth; ++x) {
                int xoffset = x << 3;
                int maxXOffset = width - 8;
                if (xoffset > maxXOffset) {
                    xoffset = maxXOffset;
                }
                int sum = 0;
                int min = 255;
                int max = 0;
                int yy = 0;
                int offset = yoffset * width + xoffset;
                while (yy < 8) {
                    int xx;
                    for (xx = 0; xx < 8; ++xx) {
                        int pixel = luminances[offset + xx] & 0xFF;
                        sum += pixel;
                        if (pixel < min) {
                            min = pixel;
                        }
                        if (pixel <= max) continue;
                        max = pixel;
                    }
                    if (max - min > 24) {
                        ++yy;
                        offset += width;
                        while (yy < 8) {
                            for (xx = 0; xx < 8; ++xx) {
                                sum += luminances[offset + xx] & 0xFF;
                            }
                            ++yy;
                            offset += width;
                        }
                    }
                    ++yy;
                    offset += width;
                }
                int average = sum >> 6;
                if (max - min <= 24) {
                    int averageNeighborBlackPoint;
                    average = min / 2;
                    if (y > 0 && x > 0 && min < (averageNeighborBlackPoint = (blackPoints[y - 1][x] + 2 * blackPoints[y][x - 1] + blackPoints[y - 1][x - 1]) / 4)) {
                        average = averageNeighborBlackPoint;
                    }
                }
                blackPoints[y][x] = average;
            }
        }
        return blackPoints;
    }
}

