/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.aztec.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.aztec.AztecDetectorResult;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GridSampler;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.common.detector.WhiteRectangleDetector;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;

public final class Detector {
    private final BitMatrix image;
    private boolean compact;
    private int nbLayers;
    private int nbDataBlocks;
    private int nbCenterLayers;
    private int shift;
    private static final int[] EXPECTED_CORNER_BITS = new int[]{3808, 476, 2107, 1799};

    public Detector(BitMatrix image) {
        this.image = image;
    }

    public AztecDetectorResult detect() throws NotFoundException {
        return this.detect(false);
    }

    public AztecDetectorResult detect(boolean isMirror) throws NotFoundException {
        Point pCenter = this.getMatrixCenter();
        ResultPoint[] bullsEyeCorners = this.getBullsEyeCorners(pCenter);
        if (isMirror) {
            ResultPoint temp = bullsEyeCorners[0];
            bullsEyeCorners[0] = bullsEyeCorners[2];
            bullsEyeCorners[2] = temp;
        }
        this.extractParameters(bullsEyeCorners);
        BitMatrix bits = this.sampleGrid(this.image, bullsEyeCorners[this.shift % 4], bullsEyeCorners[(this.shift + 1) % 4], bullsEyeCorners[(this.shift + 2) % 4], bullsEyeCorners[(this.shift + 3) % 4]);
        ResultPoint[] corners = this.getMatrixCornerPoints(bullsEyeCorners);
        return new AztecDetectorResult(bits, corners, this.compact, this.nbDataBlocks, this.nbLayers);
    }

    private void extractParameters(ResultPoint[] bullsEyeCorners) throws NotFoundException {
        if (!(this.isValid(bullsEyeCorners[0]) && this.isValid(bullsEyeCorners[1]) && this.isValid(bullsEyeCorners[2]) && this.isValid(bullsEyeCorners[3]))) {
            throw NotFoundException.getNotFoundInstance();
        }
        int length = 2 * this.nbCenterLayers;
        int[] sides = new int[]{this.sampleLine(bullsEyeCorners[0], bullsEyeCorners[1], length), this.sampleLine(bullsEyeCorners[1], bullsEyeCorners[2], length), this.sampleLine(bullsEyeCorners[2], bullsEyeCorners[3], length), this.sampleLine(bullsEyeCorners[3], bullsEyeCorners[0], length)};
        this.shift = Detector.getRotation(sides, length);
        long parameterData = 0L;
        for (int i = 0; i < 4; ++i) {
            int side = sides[(this.shift + i) % 4];
            if (this.compact) {
                parameterData <<= 7;
                parameterData += (long)(side >> 1 & 0x7F);
                continue;
            }
            parameterData <<= 10;
            parameterData += (long)((side >> 2 & 0x3E0) + (side >> 1 & 0x1F));
        }
        int correctedData = Detector.getCorrectedParameterData(parameterData, this.compact);
        if (this.compact) {
            this.nbLayers = (correctedData >> 6) + 1;
            this.nbDataBlocks = (correctedData & 0x3F) + 1;
        } else {
            this.nbLayers = (correctedData >> 11) + 1;
            this.nbDataBlocks = (correctedData & 0x7FF) + 1;
        }
    }

    private static int getRotation(int[] sides, int length) throws NotFoundException {
        int cornerBits = 0;
        for (int side : sides) {
            int t = (side >> length - 2 << 1) + (side & 1);
            cornerBits = (cornerBits << 3) + t;
        }
        cornerBits = ((cornerBits & 1) << 11) + (cornerBits >> 1);
        for (int shift = 0; shift < 4; ++shift) {
            if (Integer.bitCount(cornerBits ^ EXPECTED_CORNER_BITS[shift]) > 2) continue;
            return shift;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int getCorrectedParameterData(long parameterData, boolean compact) throws NotFoundException {
        int numDataCodewords;
        int numCodewords;
        if (compact) {
            numCodewords = 7;
            numDataCodewords = 2;
        } else {
            numCodewords = 10;
            numDataCodewords = 4;
        }
        int numECCodewords = numCodewords - numDataCodewords;
        int[] parameterWords = new int[numCodewords];
        for (int i = numCodewords - 1; i >= 0; --i) {
            parameterWords[i] = (int)parameterData & 0xF;
            parameterData >>= 4;
        }
        try {
            ReedSolomonDecoder rsDecoder = new ReedSolomonDecoder(GenericGF.AZTEC_PARAM);
            rsDecoder.decode(parameterWords, numECCodewords);
        }
        catch (ReedSolomonException ignored) {
            throw NotFoundException.getNotFoundInstance();
        }
        int result = 0;
        for (int i = 0; i < numDataCodewords; ++i) {
            result = (result << 4) + parameterWords[i];
        }
        return result;
    }

    private ResultPoint[] getBullsEyeCorners(Point pCenter) throws NotFoundException {
        Point pina = pCenter;
        Point pinb = pCenter;
        Point pinc = pCenter;
        Point pind = pCenter;
        boolean color = true;
        this.nbCenterLayers = 1;
        while (this.nbCenterLayers < 9) {
            float q;
            Point pouta = this.getFirstDifferent(pina, color, 1, -1);
            Point poutb = this.getFirstDifferent(pinb, color, 1, 1);
            Point poutc = this.getFirstDifferent(pinc, color, -1, 1);
            Point poutd = this.getFirstDifferent(pind, color, -1, -1);
            if (this.nbCenterLayers > 2 && ((double)(q = Detector.distance(poutd, pouta) * (float)this.nbCenterLayers / (Detector.distance(pind, pina) * (float)(this.nbCenterLayers + 2))) < 0.75 || (double)q > 1.25 || !this.isWhiteOrBlackRectangle(pouta, poutb, poutc, poutd))) break;
            pina = pouta;
            pinb = poutb;
            pinc = poutc;
            pind = poutd;
            color = !color;
            ++this.nbCenterLayers;
        }
        if (this.nbCenterLayers != 5 && this.nbCenterLayers != 7) {
            throw NotFoundException.getNotFoundInstance();
        }
        this.compact = this.nbCenterLayers == 5;
        ResultPoint pinax = new ResultPoint((float)pina.getX() + 0.5f, (float)pina.getY() - 0.5f);
        ResultPoint pinbx = new ResultPoint((float)pinb.getX() + 0.5f, (float)pinb.getY() + 0.5f);
        ResultPoint pincx = new ResultPoint((float)pinc.getX() - 0.5f, (float)pinc.getY() + 0.5f);
        ResultPoint pindx = new ResultPoint((float)pind.getX() - 0.5f, (float)pind.getY() - 0.5f);
        return Detector.expandSquare(new ResultPoint[]{pinax, pinbx, pincx, pindx}, 2 * this.nbCenterLayers - 3, 2 * this.nbCenterLayers);
    }

    private Point getMatrixCenter() {
        ResultPoint pointD;
        ResultPoint pointC;
        ResultPoint pointB;
        ResultPoint pointA;
        try {
            ResultPoint[] cornerPoints = new WhiteRectangleDetector(this.image).detect();
            pointA = cornerPoints[0];
            pointB = cornerPoints[1];
            pointC = cornerPoints[2];
            pointD = cornerPoints[3];
        }
        catch (NotFoundException e) {
            int cx = this.image.getWidth() / 2;
            int cy = this.image.getHeight() / 2;
            pointA = this.getFirstDifferent(new Point(cx + 7, cy - 7), false, 1, -1).toResultPoint();
            pointB = this.getFirstDifferent(new Point(cx + 7, cy + 7), false, 1, 1).toResultPoint();
            pointC = this.getFirstDifferent(new Point(cx - 7, cy + 7), false, -1, 1).toResultPoint();
            pointD = this.getFirstDifferent(new Point(cx - 7, cy - 7), false, -1, -1).toResultPoint();
        }
        int cx = MathUtils.round((pointA.getX() + pointD.getX() + pointB.getX() + pointC.getX()) / 4.0f);
        int cy = MathUtils.round((pointA.getY() + pointD.getY() + pointB.getY() + pointC.getY()) / 4.0f);
        try {
            ResultPoint[] cornerPoints = new WhiteRectangleDetector(this.image, 15, cx, cy).detect();
            pointA = cornerPoints[0];
            pointB = cornerPoints[1];
            pointC = cornerPoints[2];
            pointD = cornerPoints[3];
        }
        catch (NotFoundException e) {
            pointA = this.getFirstDifferent(new Point(cx + 7, cy - 7), false, 1, -1).toResultPoint();
            pointB = this.getFirstDifferent(new Point(cx + 7, cy + 7), false, 1, 1).toResultPoint();
            pointC = this.getFirstDifferent(new Point(cx - 7, cy + 7), false, -1, 1).toResultPoint();
            pointD = this.getFirstDifferent(new Point(cx - 7, cy - 7), false, -1, -1).toResultPoint();
        }
        cx = MathUtils.round((pointA.getX() + pointD.getX() + pointB.getX() + pointC.getX()) / 4.0f);
        cy = MathUtils.round((pointA.getY() + pointD.getY() + pointB.getY() + pointC.getY()) / 4.0f);
        return new Point(cx, cy);
    }

    private ResultPoint[] getMatrixCornerPoints(ResultPoint[] bullsEyeCorners) {
        return Detector.expandSquare(bullsEyeCorners, 2 * this.nbCenterLayers, this.getDimension());
    }

    private BitMatrix sampleGrid(BitMatrix image, ResultPoint topLeft, ResultPoint topRight, ResultPoint bottomRight, ResultPoint bottomLeft) throws NotFoundException {
        GridSampler sampler = GridSampler.getInstance();
        int dimension = this.getDimension();
        float low = (float)dimension / 2.0f - (float)this.nbCenterLayers;
        float high = (float)dimension / 2.0f + (float)this.nbCenterLayers;
        return sampler.sampleGrid(image, dimension, dimension, low, low, high, low, high, high, low, high, topLeft.getX(), topLeft.getY(), topRight.getX(), topRight.getY(), bottomRight.getX(), bottomRight.getY(), bottomLeft.getX(), bottomLeft.getY());
    }

    private int sampleLine(ResultPoint p1, ResultPoint p2, int size) {
        int result = 0;
        float d = Detector.distance(p1, p2);
        float moduleSize = d / (float)size;
        float px = p1.getX();
        float py = p1.getY();
        float dx = moduleSize * (p2.getX() - p1.getX()) / d;
        float dy = moduleSize * (p2.getY() - p1.getY()) / d;
        for (int i = 0; i < size; ++i) {
            if (!this.image.get(MathUtils.round(px + (float)i * dx), MathUtils.round(py + (float)i * dy))) continue;
            result |= 1 << size - i - 1;
        }
        return result;
    }

    private boolean isWhiteOrBlackRectangle(Point p1, Point p2, Point p3, Point p4) {
        int corr = 3;
        p1 = new Point(p1.getX() - corr, p1.getY() + corr);
        p2 = new Point(p2.getX() - corr, p2.getY() - corr);
        p3 = new Point(p3.getX() + corr, p3.getY() - corr);
        int cInit = this.getColor(p4 = new Point(p4.getX() + corr, p4.getY() + corr), p1);
        if (cInit == 0) {
            return false;
        }
        int c = this.getColor(p1, p2);
        if (c != cInit) {
            return false;
        }
        c = this.getColor(p2, p3);
        if (c != cInit) {
            return false;
        }
        c = this.getColor(p3, p4);
        return c == cInit;
    }

    private int getColor(Point p1, Point p2) {
        float d = Detector.distance(p1, p2);
        float dx = (float)(p2.getX() - p1.getX()) / d;
        float dy = (float)(p2.getY() - p1.getY()) / d;
        int error = 0;
        float px = p1.getX();
        float py = p1.getY();
        boolean colorModel = this.image.get(p1.getX(), p1.getY());
        int i = 0;
        while ((float)i < d) {
            if (this.image.get(MathUtils.round(px += dx), MathUtils.round(py += dy)) != colorModel) {
                ++error;
            }
            ++i;
        }
        float errRatio = (float)error / d;
        if (errRatio > 0.1f && errRatio < 0.9f) {
            return 0;
        }
        return errRatio <= 0.1f == colorModel ? 1 : -1;
    }

    private Point getFirstDifferent(Point init, boolean color, int dx, int dy) {
        int x = init.getX() + dx;
        int y = init.getY() + dy;
        while (this.isValid(x, y) && this.image.get(x, y) == color) {
            x += dx;
            y += dy;
        }
        x -= dx;
        y -= dy;
        while (this.isValid(x, y) && this.image.get(x, y) == color) {
            x += dx;
        }
        x -= dx;
        while (this.isValid(x, y) && this.image.get(x, y) == color) {
            y += dy;
        }
        return new Point(x, y -= dy);
    }

    private static ResultPoint[] expandSquare(ResultPoint[] cornerPoints, float oldSide, float newSide) {
        float ratio = newSide / (2.0f * oldSide);
        float dx = cornerPoints[0].getX() - cornerPoints[2].getX();
        float dy = cornerPoints[0].getY() - cornerPoints[2].getY();
        float centerx = (cornerPoints[0].getX() + cornerPoints[2].getX()) / 2.0f;
        float centery = (cornerPoints[0].getY() + cornerPoints[2].getY()) / 2.0f;
        ResultPoint result0 = new ResultPoint(centerx + ratio * dx, centery + ratio * dy);
        ResultPoint result2 = new ResultPoint(centerx - ratio * dx, centery - ratio * dy);
        dx = cornerPoints[1].getX() - cornerPoints[3].getX();
        dy = cornerPoints[1].getY() - cornerPoints[3].getY();
        centerx = (cornerPoints[1].getX() + cornerPoints[3].getX()) / 2.0f;
        centery = (cornerPoints[1].getY() + cornerPoints[3].getY()) / 2.0f;
        ResultPoint result1 = new ResultPoint(centerx + ratio * dx, centery + ratio * dy);
        ResultPoint result3 = new ResultPoint(centerx - ratio * dx, centery - ratio * dy);
        return new ResultPoint[]{result0, result1, result2, result3};
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < this.image.getWidth() && y > 0 && y < this.image.getHeight();
    }

    private boolean isValid(ResultPoint point) {
        int x = MathUtils.round(point.getX());
        int y = MathUtils.round(point.getY());
        return this.isValid(x, y);
    }

    private static float distance(Point a, Point b) {
        return MathUtils.distance(a.getX(), a.getY(), b.getX(), b.getY());
    }

    private static float distance(ResultPoint a, ResultPoint b) {
        return MathUtils.distance(a.getX(), a.getY(), b.getX(), b.getY());
    }

    private int getDimension() {
        if (this.compact) {
            return 4 * this.nbLayers + 11;
        }
        if (this.nbLayers <= 4) {
            return 4 * this.nbLayers + 15;
        }
        return 4 * this.nbLayers + 2 * ((this.nbLayers - 4) / 8 + 1) + 15;
    }

    static final class Point {
        private final int x;
        private final int y;

        ResultPoint toResultPoint() {
            return new ResultPoint(this.getX(), this.getY());
        }

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int getX() {
            return this.x;
        }

        int getY() {
            return this.y;
        }

        public String toString() {
            return "<" + this.x + ' ' + this.y + '>';
        }
    }
}

