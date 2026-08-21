/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned.rss;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.rss.AbstractRSSReader;
import com.google.zxing.oned.rss.DataCharacter;
import com.google.zxing.oned.rss.FinderPattern;
import com.google.zxing.oned.rss.Pair;
import com.google.zxing.oned.rss.RSSUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class RSS14Reader
extends AbstractRSSReader {
    private static final int[] OUTSIDE_EVEN_TOTAL_SUBSET = new int[]{1, 10, 34, 70, 126};
    private static final int[] INSIDE_ODD_TOTAL_SUBSET = new int[]{4, 20, 48, 81};
    private static final int[] OUTSIDE_GSUM = new int[]{0, 161, 961, 2015, 2715};
    private static final int[] INSIDE_GSUM = new int[]{0, 336, 1036, 1516};
    private static final int[] OUTSIDE_ODD_WIDEST = new int[]{8, 6, 4, 3, 1};
    private static final int[] INSIDE_ODD_WIDEST = new int[]{2, 4, 6, 8};
    private static final int[][] FINDER_PATTERNS = new int[][]{{3, 8, 2, 1}, {3, 5, 5, 1}, {3, 3, 7, 1}, {3, 1, 9, 1}, {2, 7, 4, 1}, {2, 5, 6, 1}, {2, 3, 8, 1}, {1, 5, 7, 1}, {1, 3, 9, 1}};
    private final List<Pair> possibleLeftPairs = new ArrayList<Pair>();
    private final List<Pair> possibleRightPairs = new ArrayList<Pair>();

    @Override
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException {
        Pair leftPair = this.decodePair(row, false, rowNumber, hints);
        RSS14Reader.addOrTally(this.possibleLeftPairs, leftPair);
        row.reverse();
        Pair rightPair = this.decodePair(row, true, rowNumber, hints);
        RSS14Reader.addOrTally(this.possibleRightPairs, rightPair);
        row.reverse();
        int lefSize = this.possibleLeftPairs.size();
        for (int i = 0; i < lefSize; ++i) {
            Pair left = this.possibleLeftPairs.get(i);
            if (left.getCount() <= 1) continue;
            int rightSize = this.possibleRightPairs.size();
            for (int j = 0; j < rightSize; ++j) {
                Pair right = this.possibleRightPairs.get(j);
                if (right.getCount() <= 1 || !RSS14Reader.checkChecksum(left, right)) continue;
                return RSS14Reader.constructResult(left, right);
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static void addOrTally(Collection<Pair> possiblePairs, Pair pair) {
        if (pair == null) {
            return;
        }
        boolean found = false;
        for (Pair other : possiblePairs) {
            if (other.getValue() != pair.getValue()) continue;
            other.incrementCount();
            found = true;
            break;
        }
        if (!found) {
            possiblePairs.add(pair);
        }
    }

    @Override
    public void reset() {
        this.possibleLeftPairs.clear();
        this.possibleRightPairs.clear();
    }

    private static Result constructResult(Pair leftPair, Pair rightPair) {
        long symbolValue = 4537077L * (long)leftPair.getValue() + (long)rightPair.getValue();
        String text = String.valueOf(symbolValue);
        StringBuilder buffer = new StringBuilder(14);
        for (int i = 13 - text.length(); i > 0; --i) {
            buffer.append('0');
        }
        buffer.append(text);
        int checkDigit = 0;
        for (int i = 0; i < 13; ++i) {
            int digit = buffer.charAt(i) - 48;
            checkDigit += (i & 1) == 0 ? 3 * digit : digit;
        }
        if ((checkDigit = 10 - checkDigit % 10) == 10) {
            checkDigit = 0;
        }
        buffer.append(checkDigit);
        ResultPoint[] leftPoints = leftPair.getFinderPattern().getResultPoints();
        ResultPoint[] rightPoints = rightPair.getFinderPattern().getResultPoints();
        return new Result(String.valueOf(buffer.toString()), null, new ResultPoint[]{leftPoints[0], leftPoints[1], rightPoints[0], rightPoints[1]}, BarcodeFormat.RSS_14);
    }

    private static boolean checkChecksum(Pair leftPair, Pair rightPair) {
        int checkValue = (leftPair.getChecksumPortion() + 16 * rightPair.getChecksumPortion()) % 79;
        int targetCheckValue = 9 * leftPair.getFinderPattern().getValue() + rightPair.getFinderPattern().getValue();
        if (targetCheckValue > 72) {
            --targetCheckValue;
        }
        if (targetCheckValue > 8) {
            --targetCheckValue;
        }
        return checkValue == targetCheckValue;
    }

    private Pair decodePair(BitArray row, boolean right, int rowNumber, Map<DecodeHintType, ?> hints) {
        try {
            ResultPointCallback resultPointCallback;
            int[] startEnd = this.findFinderPattern(row, 0, right);
            FinderPattern pattern = this.parseFoundFinderPattern(row, rowNumber, right, startEnd);
            ResultPointCallback resultPointCallback2 = resultPointCallback = hints == null ? null : (ResultPointCallback)hints.get((Object)DecodeHintType.NEED_RESULT_POINT_CALLBACK);
            if (resultPointCallback != null) {
                float center = (float)(startEnd[0] + startEnd[1]) / 2.0f;
                if (right) {
                    center = (float)(row.getSize() - 1) - center;
                }
                resultPointCallback.foundPossibleResultPoint(new ResultPoint(center, rowNumber));
            }
            DataCharacter outside = this.decodeDataCharacter(row, pattern, true);
            DataCharacter inside = this.decodeDataCharacter(row, pattern, false);
            return new Pair(1597 * outside.getValue() + inside.getValue(), outside.getChecksumPortion() + 4 * inside.getChecksumPortion(), pattern);
        }
        catch (NotFoundException ignored) {
            return null;
        }
    }

    private DataCharacter decodeDataCharacter(BitArray row, FinderPattern pattern, boolean outsideChar) throws NotFoundException {
        int[] counters = this.getDataCharacterCounters();
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        counters[4] = 0;
        counters[5] = 0;
        counters[6] = 0;
        counters[7] = 0;
        if (outsideChar) {
            RSS14Reader.recordPatternInReverse(row, pattern.getStartEnd()[0], counters);
        } else {
            RSS14Reader.recordPattern(row, pattern.getStartEnd()[1] + 1, counters);
            int i = 0;
            for (int j = counters.length - 1; i < j; ++i, --j) {
                int temp = counters[i];
                counters[i] = counters[j];
                counters[j] = temp;
            }
        }
        int numModules = outsideChar ? 16 : 15;
        float elementWidth = (float)RSS14Reader.count(counters) / (float)numModules;
        int[] oddCounts = this.getOddCounts();
        int[] evenCounts = this.getEvenCounts();
        float[] oddRoundingErrors = this.getOddRoundingErrors();
        float[] evenRoundingErrors = this.getEvenRoundingErrors();
        for (int i = 0; i < counters.length; ++i) {
            float value = (float)counters[i] / elementWidth;
            int count = (int)(value + 0.5f);
            if (count < 1) {
                count = 1;
            } else if (count > 8) {
                count = 8;
            }
            int offset = i / 2;
            if ((i & 1) == 0) {
                oddCounts[offset] = count;
                oddRoundingErrors[offset] = value - (float)count;
                continue;
            }
            evenCounts[offset] = count;
            evenRoundingErrors[offset] = value - (float)count;
        }
        this.adjustOddEvenCounts(outsideChar, numModules);
        int oddSum = 0;
        int oddChecksumPortion = 0;
        for (int i = oddCounts.length - 1; i >= 0; --i) {
            oddChecksumPortion *= 9;
            oddChecksumPortion += oddCounts[i];
            oddSum += oddCounts[i];
        }
        int evenChecksumPortion = 0;
        int evenSum = 0;
        for (int i = evenCounts.length - 1; i >= 0; --i) {
            evenChecksumPortion *= 9;
            evenChecksumPortion += evenCounts[i];
            evenSum += evenCounts[i];
        }
        int checksumPortion = oddChecksumPortion + 3 * evenChecksumPortion;
        if (outsideChar) {
            if ((oddSum & 1) != 0 || oddSum > 12 || oddSum < 4) {
                throw NotFoundException.getNotFoundInstance();
            }
            int group = (12 - oddSum) / 2;
            int oddWidest = OUTSIDE_ODD_WIDEST[group];
            int evenWidest = 9 - oddWidest;
            int vOdd = RSSUtils.getRSSvalue(oddCounts, oddWidest, false);
            int vEven = RSSUtils.getRSSvalue(evenCounts, evenWidest, true);
            int tEven = OUTSIDE_EVEN_TOTAL_SUBSET[group];
            int gSum = OUTSIDE_GSUM[group];
            return new DataCharacter(vOdd * tEven + vEven + gSum, checksumPortion);
        }
        if (evenSum & true || evenSum > 10 || evenSum < 4) {
            throw NotFoundException.getNotFoundInstance();
        }
        int group = (10 - evenSum) / 2;
        int oddWidest = INSIDE_ODD_WIDEST[group];
        int evenWidest = 9 - oddWidest;
        int vOdd = RSSUtils.getRSSvalue(oddCounts, oddWidest, true);
        int vEven = RSSUtils.getRSSvalue(evenCounts, evenWidest, false);
        int tOdd = INSIDE_ODD_TOTAL_SUBSET[group];
        int gSum = INSIDE_GSUM[group];
        return new DataCharacter(vEven * tOdd + vOdd + gSum, checksumPortion);
    }

    private int[] findFinderPattern(BitArray row, int rowOffset, boolean rightFinderPattern) throws NotFoundException {
        int[] counters = this.getDecodeFinderCounters();
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int width = row.getSize();
        boolean isWhite = false;
        while (rowOffset < width) {
            boolean bl = isWhite = !row.get(rowOffset);
            if (rightFinderPattern == isWhite) break;
            ++rowOffset;
        }
        int counterPosition = 0;
        int patternStart = rowOffset;
        for (int x = rowOffset; x < width; ++x) {
            if (row.get(x) ^ isWhite) {
                int n = counterPosition;
                counters[n] = counters[n] + 1;
                continue;
            }
            if (counterPosition == 3) {
                if (RSS14Reader.isFinderPattern(counters)) {
                    return new int[]{patternStart, x};
                }
                patternStart += counters[0] + counters[1];
                counters[0] = counters[2];
                counters[1] = counters[3];
                counters[2] = 0;
                counters[3] = 0;
                --counterPosition;
            } else {
                ++counterPosition;
            }
            counters[counterPosition] = 1;
            isWhite = !isWhite;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private FinderPattern parseFoundFinderPattern(BitArray row, int rowNumber, boolean right, int[] startEnd) throws NotFoundException {
        int firstElementStart;
        boolean firstIsBlack = row.get(startEnd[0]);
        for (firstElementStart = startEnd[0] - 1; firstElementStart >= 0 && firstIsBlack ^ row.get(firstElementStart); --firstElementStart) {
        }
        int firstCounter = startEnd[0] - ++firstElementStart;
        int[] counters = this.getDecodeFinderCounters();
        System.arraycopy(counters, 0, counters, 1, counters.length - 1);
        counters[0] = firstCounter;
        int value = RSS14Reader.parseFinderValue(counters, FINDER_PATTERNS);
        int start = firstElementStart;
        int end = startEnd[1];
        if (right) {
            start = row.getSize() - 1 - start;
            end = row.getSize() - 1 - end;
        }
        return new FinderPattern(value, new int[]{firstElementStart, startEnd[1]}, start, end, rowNumber);
    }

    private void adjustOddEvenCounts(boolean outsideChar, int numModules) throws NotFoundException {
        int oddSum = RSS14Reader.count(this.getOddCounts());
        int evenSum = RSS14Reader.count(this.getEvenCounts());
        int mismatch = oddSum + evenSum - numModules;
        boolean oddParityBad = (oddSum & 1) == (outsideChar ? 1 : 0);
        boolean evenParityBad = (evenSum & 1) == 1;
        boolean incrementOdd = false;
        boolean decrementOdd = false;
        boolean incrementEven = false;
        boolean decrementEven = false;
        if (outsideChar) {
            if (oddSum > 12) {
                decrementOdd = true;
            } else if (oddSum < 4) {
                incrementOdd = true;
            }
            if (evenSum > 12) {
                decrementEven = true;
            } else if (evenSum < 4) {
                incrementEven = true;
            }
        } else {
            if (oddSum > 11) {
                decrementOdd = true;
            } else if (oddSum < 5) {
                incrementOdd = true;
            }
            if (evenSum > 10) {
                decrementEven = true;
            } else if (evenSum < 4) {
                incrementEven = true;
            }
        }
        if (mismatch == 1) {
            if (oddParityBad) {
                if (evenParityBad) {
                    throw NotFoundException.getNotFoundInstance();
                }
                decrementOdd = true;
            } else {
                if (!evenParityBad) {
                    throw NotFoundException.getNotFoundInstance();
                }
                decrementEven = true;
            }
        } else if (mismatch == -1) {
            if (oddParityBad) {
                if (evenParityBad) {
                    throw NotFoundException.getNotFoundInstance();
                }
                incrementOdd = true;
            } else {
                if (!evenParityBad) {
                    throw NotFoundException.getNotFoundInstance();
                }
                incrementEven = true;
            }
        } else if (mismatch == 0) {
            if (oddParityBad) {
                if (!evenParityBad) {
                    throw NotFoundException.getNotFoundInstance();
                }
                if (oddSum < evenSum) {
                    incrementOdd = true;
                    decrementEven = true;
                } else {
                    decrementOdd = true;
                    incrementEven = true;
                }
            } else if (evenParityBad) {
                throw NotFoundException.getNotFoundInstance();
            }
        } else {
            throw NotFoundException.getNotFoundInstance();
        }
        if (incrementOdd) {
            if (decrementOdd) {
                throw NotFoundException.getNotFoundInstance();
            }
            RSS14Reader.increment(this.getOddCounts(), this.getOddRoundingErrors());
        }
        if (decrementOdd) {
            RSS14Reader.decrement(this.getOddCounts(), this.getOddRoundingErrors());
        }
        if (incrementEven) {
            if (decrementEven) {
                throw NotFoundException.getNotFoundInstance();
            }
            RSS14Reader.increment(this.getEvenCounts(), this.getOddRoundingErrors());
        }
        if (decrementEven) {
            RSS14Reader.decrement(this.getEvenCounts(), this.getEvenRoundingErrors());
        }
    }
}

