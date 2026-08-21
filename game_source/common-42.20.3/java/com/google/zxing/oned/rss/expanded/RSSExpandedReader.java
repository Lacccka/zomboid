/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned.rss.expanded;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.rss.AbstractRSSReader;
import com.google.zxing.oned.rss.DataCharacter;
import com.google.zxing.oned.rss.FinderPattern;
import com.google.zxing.oned.rss.RSSUtils;
import com.google.zxing.oned.rss.expanded.BitArrayBuilder;
import com.google.zxing.oned.rss.expanded.ExpandedPair;
import com.google.zxing.oned.rss.expanded.ExpandedRow;
import com.google.zxing.oned.rss.expanded.decoders.AbstractExpandedDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RSSExpandedReader
extends AbstractRSSReader {
    private static final int[] SYMBOL_WIDEST = new int[]{7, 5, 4, 3, 1};
    private static final int[] EVEN_TOTAL_SUBSET = new int[]{4, 20, 52, 104, 204};
    private static final int[] GSUM = new int[]{0, 348, 1388, 2948, 3988};
    private static final int[][] FINDER_PATTERNS = new int[][]{{1, 8, 4, 1}, {3, 6, 4, 1}, {3, 4, 6, 1}, {3, 2, 8, 1}, {2, 6, 5, 1}, {2, 2, 9, 1}};
    private static final int[][] WEIGHTS = new int[][]{{1, 3, 9, 27, 81, 32, 96, 77}, {20, 60, 180, 118, 143, 7, 21, 63}, {189, 145, 13, 39, 117, 140, 209, 205}, {193, 157, 49, 147, 19, 57, 171, 91}, {62, 186, 136, 197, 169, 85, 44, 132}, {185, 133, 188, 142, 4, 12, 36, 108}, {113, 128, 173, 97, 80, 29, 87, 50}, {150, 28, 84, 41, 123, 158, 52, 156}, {46, 138, 203, 187, 139, 206, 196, 166}, {76, 17, 51, 153, 37, 111, 122, 155}, {43, 129, 176, 106, 107, 110, 119, 146}, {16, 48, 144, 10, 30, 90, 59, 177}, {109, 116, 137, 200, 178, 112, 125, 164}, {70, 210, 208, 202, 184, 130, 179, 115}, {134, 191, 151, 31, 93, 68, 204, 190}, {148, 22, 66, 198, 172, 94, 71, 2}, {6, 18, 54, 162, 64, 192, 154, 40}, {120, 149, 25, 75, 14, 42, 126, 167}, {79, 26, 78, 23, 69, 207, 199, 175}, {103, 98, 83, 38, 114, 131, 182, 124}, {161, 61, 183, 127, 170, 88, 53, 159}, {55, 165, 73, 8, 24, 72, 5, 15}, {45, 135, 194, 160, 58, 174, 100, 89}};
    private static final int FINDER_PAT_A = 0;
    private static final int FINDER_PAT_B = 1;
    private static final int FINDER_PAT_C = 2;
    private static final int FINDER_PAT_D = 3;
    private static final int FINDER_PAT_E = 4;
    private static final int FINDER_PAT_F = 5;
    private static final int[][] FINDER_PATTERN_SEQUENCES = new int[][]{{0, 0}, {0, 1, 1}, {0, 2, 1, 3}, {0, 4, 1, 3, 2}, {0, 4, 1, 3, 3, 5}, {0, 4, 1, 3, 4, 5, 5}, {0, 0, 1, 1, 2, 2, 3, 3}, {0, 0, 1, 1, 2, 2, 3, 4, 4}, {0, 0, 1, 1, 2, 2, 3, 4, 5, 5}, {0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5}};
    private static final int MAX_PAIRS = 11;
    private final List<ExpandedPair> pairs = new ArrayList<ExpandedPair>(11);
    private final List<ExpandedRow> rows = new ArrayList<ExpandedRow>();
    private final int[] startEnd = new int[2];
    private boolean startFromEven;

    @Override
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException, FormatException {
        this.pairs.clear();
        this.startFromEven = false;
        try {
            List<ExpandedPair> pairs = this.decodeRow2pairs(rowNumber, row);
            return RSSExpandedReader.constructResult(pairs);
        }
        catch (NotFoundException pairs) {
            this.pairs.clear();
            this.startFromEven = true;
            List<ExpandedPair> pairs2 = this.decodeRow2pairs(rowNumber, row);
            return RSSExpandedReader.constructResult(pairs2);
        }
    }

    @Override
    public void reset() {
        this.pairs.clear();
        this.rows.clear();
    }

    List<ExpandedPair> decodeRow2pairs(int rowNumber, BitArray row) throws NotFoundException {
        try {
            while (true) {
                ExpandedPair nextPair = this.retrieveNextPair(row, this.pairs, rowNumber);
                this.pairs.add(nextPair);
            }
        }
        catch (NotFoundException nfe) {
            if (this.pairs.isEmpty()) {
                throw nfe;
            }
            if (this.checkChecksum()) {
                return this.pairs;
            }
            boolean tryStackedDecode = !this.rows.isEmpty();
            boolean wasReversed = false;
            this.storeRow(rowNumber, wasReversed);
            if (tryStackedDecode) {
                List<ExpandedPair> ps = this.checkRows(false);
                if (ps != null) {
                    return ps;
                }
                ps = this.checkRows(true);
                if (ps != null) {
                    return ps;
                }
            }
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private List<ExpandedPair> checkRows(boolean reverse) {
        if (this.rows.size() > 25) {
            this.rows.clear();
            return null;
        }
        this.pairs.clear();
        if (reverse) {
            Collections.reverse(this.rows);
        }
        List<ExpandedPair> ps = null;
        try {
            ps = this.checkRows(new ArrayList<ExpandedRow>(), 0);
        }
        catch (NotFoundException notFoundException) {
            // empty catch block
        }
        if (reverse) {
            Collections.reverse(this.rows);
        }
        return ps;
    }

    private List<ExpandedPair> checkRows(List<ExpandedRow> collectedRows, int currentRow) throws NotFoundException {
        for (int i = currentRow; i < this.rows.size(); ++i) {
            ExpandedRow row = this.rows.get(i);
            this.pairs.clear();
            int size = collectedRows.size();
            for (int j = 0; j < size; ++j) {
                this.pairs.addAll(collectedRows.get(j).getPairs());
            }
            this.pairs.addAll(row.getPairs());
            if (!RSSExpandedReader.isValidSequence(this.pairs)) continue;
            if (this.checkChecksum()) {
                return this.pairs;
            }
            ArrayList<ExpandedRow> rs = new ArrayList<ExpandedRow>();
            rs.addAll(collectedRows);
            rs.add(row);
            try {
                return this.checkRows(rs, i + 1);
            }
            catch (NotFoundException notFoundException) {
                // empty catch block
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static boolean isValidSequence(List<ExpandedPair> pairs) {
        for (int[] sequence : FINDER_PATTERN_SEQUENCES) {
            if (pairs.size() > sequence.length) continue;
            boolean stop = true;
            for (int j = 0; j < pairs.size(); ++j) {
                if (pairs.get(j).getFinderPattern().getValue() == sequence[j]) continue;
                stop = false;
                break;
            }
            if (!stop) continue;
            return true;
        }
        return false;
    }

    private void storeRow(int rowNumber, boolean wasReversed) {
        int insertPos;
        boolean prevIsSame = false;
        boolean nextIsSame = false;
        for (insertPos = 0; insertPos < this.rows.size(); ++insertPos) {
            ExpandedRow erow = this.rows.get(insertPos);
            if (erow.getRowNumber() > rowNumber) {
                nextIsSame = erow.isEquivalent(this.pairs);
                break;
            }
            prevIsSame = erow.isEquivalent(this.pairs);
        }
        if (nextIsSame || prevIsSame) {
            return;
        }
        if (RSSExpandedReader.isPartialRow(this.pairs, this.rows)) {
            return;
        }
        this.rows.add(insertPos, new ExpandedRow(this.pairs, rowNumber, wasReversed));
        RSSExpandedReader.removePartialRows(this.pairs, this.rows);
    }

    private static void removePartialRows(List<ExpandedPair> pairs, List<ExpandedRow> rows) {
        Iterator<ExpandedRow> iterator2 = rows.iterator();
        while (iterator2.hasNext()) {
            ExpandedRow r = iterator2.next();
            if (r.getPairs().size() == pairs.size()) continue;
            boolean allFound = true;
            for (ExpandedPair p : r.getPairs()) {
                boolean found = false;
                for (ExpandedPair pp : pairs) {
                    if (!p.equals(pp)) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                allFound = false;
                break;
            }
            if (!allFound) continue;
            iterator2.remove();
        }
    }

    private static boolean isPartialRow(Iterable<ExpandedPair> pairs, Iterable<ExpandedRow> rows) {
        for (ExpandedRow r : rows) {
            boolean allFound = true;
            for (ExpandedPair p : pairs) {
                boolean found = false;
                for (ExpandedPair pp : r.getPairs()) {
                    if (!p.equals(pp)) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                allFound = false;
                break;
            }
            if (!allFound) continue;
            return true;
        }
        return false;
    }

    List<ExpandedRow> getRows() {
        return this.rows;
    }

    static Result constructResult(List<ExpandedPair> pairs) throws NotFoundException, FormatException {
        BitArray binary = BitArrayBuilder.buildBitArray(pairs);
        AbstractExpandedDecoder decoder = AbstractExpandedDecoder.createDecoder(binary);
        String resultingString = decoder.parseInformation();
        ResultPoint[] firstPoints = pairs.get(0).getFinderPattern().getResultPoints();
        ResultPoint[] lastPoints = pairs.get(pairs.size() - 1).getFinderPattern().getResultPoints();
        return new Result(resultingString, null, new ResultPoint[]{firstPoints[0], firstPoints[1], lastPoints[0], lastPoints[1]}, BarcodeFormat.RSS_EXPANDED);
    }

    private boolean checkChecksum() {
        ExpandedPair firstPair = this.pairs.get(0);
        DataCharacter checkCharacter = firstPair.getLeftChar();
        DataCharacter firstCharacter = firstPair.getRightChar();
        if (firstCharacter == null) {
            return false;
        }
        int checksum = firstCharacter.getChecksumPortion();
        int s = 2;
        for (int i = 1; i < this.pairs.size(); ++i) {
            ExpandedPair currentPair = this.pairs.get(i);
            checksum += currentPair.getLeftChar().getChecksumPortion();
            ++s;
            DataCharacter currentRightChar = currentPair.getRightChar();
            if (currentRightChar == null) continue;
            checksum += currentRightChar.getChecksumPortion();
            ++s;
        }
        int checkCharacterValue = 211 * (s - 4) + (checksum %= 211);
        return checkCharacterValue == checkCharacter.getValue();
    }

    private static int getNextSecondBar(BitArray row, int initialPos) {
        int currentPos;
        if (row.get(initialPos)) {
            currentPos = row.getNextUnset(initialPos);
            currentPos = row.getNextSet(currentPos);
        } else {
            currentPos = row.getNextSet(initialPos);
            currentPos = row.getNextUnset(currentPos);
        }
        return currentPos;
    }

    ExpandedPair retrieveNextPair(BitArray row, List<ExpandedPair> previousPairs, int rowNumber) throws NotFoundException {
        DataCharacter rightChar;
        FinderPattern pattern;
        boolean isOddPattern;
        boolean bl = isOddPattern = previousPairs.size() % 2 == 0;
        if (this.startFromEven) {
            isOddPattern = !isOddPattern;
        }
        boolean keepFinding = true;
        int forcedOffset = -1;
        do {
            this.findNextPair(row, previousPairs, forcedOffset);
            pattern = this.parseFoundFinderPattern(row, rowNumber, isOddPattern);
            if (pattern == null) {
                forcedOffset = RSSExpandedReader.getNextSecondBar(row, this.startEnd[0]);
                continue;
            }
            keepFinding = false;
        } while (keepFinding);
        DataCharacter leftChar = this.decodeDataCharacter(row, pattern, isOddPattern, true);
        if (!previousPairs.isEmpty() && previousPairs.get(previousPairs.size() - 1).mustBeLast()) {
            throw NotFoundException.getNotFoundInstance();
        }
        try {
            rightChar = this.decodeDataCharacter(row, pattern, isOddPattern, false);
        }
        catch (NotFoundException ignored) {
            rightChar = null;
        }
        boolean mayBeLast = true;
        return new ExpandedPair(leftChar, rightChar, pattern, mayBeLast);
    }

    private void findNextPair(BitArray row, List<ExpandedPair> previousPairs, int forcedOffset) throws NotFoundException {
        boolean searchingEvenPair;
        int rowOffset;
        int[] counters = this.getDecodeFinderCounters();
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int width = row.getSize();
        if (forcedOffset >= 0) {
            rowOffset = forcedOffset;
        } else if (previousPairs.isEmpty()) {
            rowOffset = 0;
        } else {
            ExpandedPair lastPair = previousPairs.get(previousPairs.size() - 1);
            rowOffset = lastPair.getFinderPattern().getStartEnd()[1];
        }
        boolean bl = searchingEvenPair = previousPairs.size() % 2 != 0;
        if (this.startFromEven) {
            searchingEvenPair = !searchingEvenPair;
        }
        boolean isWhite = false;
        while (rowOffset < width) {
            boolean bl2 = isWhite = !row.get(rowOffset);
            if (!isWhite) break;
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
                if (searchingEvenPair) {
                    RSSExpandedReader.reverseCounters(counters);
                }
                if (RSSExpandedReader.isFinderPattern(counters)) {
                    this.startEnd[0] = patternStart;
                    this.startEnd[1] = x;
                    return;
                }
                if (searchingEvenPair) {
                    RSSExpandedReader.reverseCounters(counters);
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

    private static void reverseCounters(int[] counters) {
        int length = counters.length;
        for (int i = 0; i < length / 2; ++i) {
            int tmp = counters[i];
            counters[i] = counters[length - i - 1];
            counters[length - i - 1] = tmp;
        }
    }

    private FinderPattern parseFoundFinderPattern(BitArray row, int rowNumber, boolean oddPattern) {
        int value;
        int end;
        int start;
        int firstCounter;
        if (oddPattern) {
            int firstElementStart;
            for (firstElementStart = this.startEnd[0] - 1; firstElementStart >= 0 && !row.get(firstElementStart); --firstElementStart) {
            }
            firstCounter = this.startEnd[0] - ++firstElementStart;
            start = firstElementStart;
            end = this.startEnd[1];
        } else {
            start = this.startEnd[0];
            end = row.getNextUnset(this.startEnd[1] + 1);
            firstCounter = end - this.startEnd[1];
        }
        int[] counters = this.getDecodeFinderCounters();
        System.arraycopy(counters, 0, counters, 1, counters.length - 1);
        counters[0] = firstCounter;
        try {
            value = RSSExpandedReader.parseFinderValue(counters, FINDER_PATTERNS);
        }
        catch (NotFoundException ignored) {
            return null;
        }
        return new FinderPattern(value, new int[]{start, end}, start, end, rowNumber);
    }

    DataCharacter decodeDataCharacter(BitArray row, FinderPattern pattern, boolean isOddPattern, boolean leftChar) throws NotFoundException {
        int[] counters = this.getDataCharacterCounters();
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        counters[4] = 0;
        counters[5] = 0;
        counters[6] = 0;
        counters[7] = 0;
        if (leftChar) {
            RSSExpandedReader.recordPatternInReverse(row, pattern.getStartEnd()[0], counters);
        } else {
            RSSExpandedReader.recordPattern(row, pattern.getStartEnd()[1], counters);
            int i = 0;
            for (int j = counters.length - 1; i < j; ++i, --j) {
                int temp = counters[i];
                counters[i] = counters[j];
                counters[j] = temp;
            }
        }
        int numModules = 17;
        float elementWidth = (float)RSSExpandedReader.count(counters) / (float)numModules;
        float expectedElementWidth = (float)(pattern.getStartEnd()[1] - pattern.getStartEnd()[0]) / 15.0f;
        if (Math.abs(elementWidth - expectedElementWidth) / expectedElementWidth > 0.3f) {
            throw NotFoundException.getNotFoundInstance();
        }
        int[] oddCounts = this.getOddCounts();
        int[] evenCounts = this.getEvenCounts();
        float[] oddRoundingErrors = this.getOddRoundingErrors();
        float[] evenRoundingErrors = this.getEvenRoundingErrors();
        for (int i = 0; i < counters.length; ++i) {
            float value = 1.0f * (float)counters[i] / elementWidth;
            int count = (int)(value + 0.5f);
            if (count < 1) {
                if (value < 0.3f) {
                    throw NotFoundException.getNotFoundInstance();
                }
                count = 1;
            } else if (count > 8) {
                if (value > 8.7f) {
                    throw NotFoundException.getNotFoundInstance();
                }
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
        this.adjustOddEvenCounts(numModules);
        int weightRowNumber = 4 * pattern.getValue() + (isOddPattern ? 0 : 2) + (leftChar ? 0 : 1) - 1;
        int oddSum = 0;
        int oddChecksumPortion = 0;
        for (int i = oddCounts.length - 1; i >= 0; --i) {
            if (RSSExpandedReader.isNotA1left(pattern, isOddPattern, leftChar)) {
                int weight = WEIGHTS[weightRowNumber][2 * i];
                oddChecksumPortion += oddCounts[i] * weight;
            }
            oddSum += oddCounts[i];
        }
        int evenChecksumPortion = 0;
        for (int i = evenCounts.length - 1; i >= 0; --i) {
            if (!RSSExpandedReader.isNotA1left(pattern, isOddPattern, leftChar)) continue;
            int weight = WEIGHTS[weightRowNumber][2 * i + 1];
            evenChecksumPortion += evenCounts[i] * weight;
        }
        int checksumPortion = oddChecksumPortion + evenChecksumPortion;
        if ((oddSum & 1) != 0 || oddSum > 13 || oddSum < 4) {
            throw NotFoundException.getNotFoundInstance();
        }
        int group = (13 - oddSum) / 2;
        int oddWidest = SYMBOL_WIDEST[group];
        int evenWidest = 9 - oddWidest;
        int vOdd = RSSUtils.getRSSvalue(oddCounts, oddWidest, true);
        int vEven = RSSUtils.getRSSvalue(evenCounts, evenWidest, false);
        int tEven = EVEN_TOTAL_SUBSET[group];
        int gSum = GSUM[group];
        int value = vOdd * tEven + vEven + gSum;
        return new DataCharacter(value, checksumPortion);
    }

    private static boolean isNotA1left(FinderPattern pattern, boolean isOddPattern, boolean leftChar) {
        return pattern.getValue() != 0 || !isOddPattern || !leftChar;
    }

    private void adjustOddEvenCounts(int numModules) throws NotFoundException {
        int oddSum = RSSExpandedReader.count(this.getOddCounts());
        int evenSum = RSSExpandedReader.count(this.getEvenCounts());
        int mismatch = oddSum + evenSum - numModules;
        boolean oddParityBad = (oddSum & 1) == 1;
        boolean evenParityBad = (evenSum & 1) == 0;
        boolean incrementOdd = false;
        boolean decrementOdd = false;
        if (oddSum > 13) {
            decrementOdd = true;
        } else if (oddSum < 4) {
            incrementOdd = true;
        }
        boolean incrementEven = false;
        boolean decrementEven = false;
        if (evenSum > 13) {
            decrementEven = true;
        } else if (evenSum < 4) {
            incrementEven = true;
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
            RSSExpandedReader.increment(this.getOddCounts(), this.getOddRoundingErrors());
        }
        if (decrementOdd) {
            RSSExpandedReader.decrement(this.getOddCounts(), this.getOddRoundingErrors());
        }
        if (incrementEven) {
            if (decrementEven) {
                throw NotFoundException.getNotFoundInstance();
            }
            RSSExpandedReader.increment(this.getEvenCounts(), this.getOddRoundingErrors());
        }
        if (decrementEven) {
            RSSExpandedReader.decrement(this.getEvenCounts(), this.getEvenRoundingErrors());
        }
    }
}

