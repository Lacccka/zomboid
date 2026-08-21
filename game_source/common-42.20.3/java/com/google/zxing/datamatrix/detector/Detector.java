/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.GridSampler;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.common.detector.WhiteRectangleDetector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class Detector {
    private final BitMatrix image;
    private final WhiteRectangleDetector rectangleDetector;

    public Detector(BitMatrix image) throws NotFoundException {
        this.image = image;
        this.rectangleDetector = new WhiteRectangleDetector(image);
    }

    public DetectorResult detect() throws NotFoundException {
        BitMatrix bits;
        ResultPoint correctedTopRight;
        ResultPoint[] cornerPoints = this.rectangleDetector.detect();
        ResultPoint pointA = cornerPoints[0];
        ResultPoint pointB = cornerPoints[1];
        ResultPoint pointC = cornerPoints[2];
        ResultPoint pointD = cornerPoints[3];
        ArrayList<ResultPointsAndTransitions> transitions = new ArrayList<ResultPointsAndTransitions>(4);
        transitions.add(this.transitionsBetween(pointA, pointB));
        transitions.add(this.transitionsBetween(pointA, pointC));
        transitions.add(this.transitionsBetween(pointB, pointD));
        transitions.add(this.transitionsBetween(pointC, pointD));
        Collections.sort(transitions, new ResultPointsAndTransitionsComparator());
        ResultPointsAndTransitions lSideOne = (ResultPointsAndTransitions)transitions.get(0);
        ResultPointsAndTransitions lSideTwo = (ResultPointsAndTransitions)transitions.get(1);
        HashMap<ResultPoint, Integer> pointCount = new HashMap<ResultPoint, Integer>();
        Detector.increment(pointCount, lSideOne.getFrom());
        Detector.increment(pointCount, lSideOne.getTo());
        Detector.increment(pointCount, lSideTwo.getFrom());
        Detector.increment(pointCount, lSideTwo.getTo());
        ResultPoint maybeTopLeft = null;
        ResultPoint bottomLeft = null;
        ResultPoint maybeBottomRight = null;
        for (Map.Entry entry : pointCount.entrySet()) {
            ResultPoint point = (ResultPoint)entry.getKey();
            Integer value = (Integer)entry.getValue();
            if (value == 2) {
                bottomLeft = point;
                continue;
            }
            if (maybeTopLeft == null) {
                maybeTopLeft = point;
                continue;
            }
            maybeBottomRight = point;
        }
        if (maybeTopLeft == null || bottomLeft == null || maybeBottomRight == null) {
            throw NotFoundException.getNotFoundInstance();
        }
        ResultPoint[] corners = new ResultPoint[]{maybeTopLeft, bottomLeft, maybeBottomRight};
        ResultPoint.orderBestPatterns(corners);
        ResultPoint bottomRight = corners[0];
        bottomLeft = corners[1];
        ResultPoint topLeft = corners[2];
        ResultPoint topRight = !pointCount.containsKey(pointA) ? pointA : (!pointCount.containsKey(pointB) ? pointB : (!pointCount.containsKey(pointC) ? pointC : pointD));
        int dimensionTop = this.transitionsBetween(topLeft, topRight).getTransitions();
        int dimensionRight = this.transitionsBetween(bottomRight, topRight).getTransitions();
        if ((dimensionTop & 1) == 1) {
            ++dimensionTop;
        }
        dimensionTop += 2;
        if ((dimensionRight & 1) == 1) {
            ++dimensionRight;
        }
        if (4 * dimensionTop >= 7 * (dimensionRight += 2) || 4 * dimensionRight >= 7 * dimensionTop) {
            correctedTopRight = this.correctTopRightRectangular(bottomLeft, bottomRight, topLeft, topRight, dimensionTop, dimensionRight);
            if (correctedTopRight == null) {
                correctedTopRight = topRight;
            }
            dimensionTop = this.transitionsBetween(topLeft, correctedTopRight).getTransitions();
            dimensionRight = this.transitionsBetween(bottomRight, correctedTopRight).getTransitions();
            if ((dimensionTop & 1) == 1) {
                ++dimensionTop;
            }
            if ((dimensionRight & 1) == 1) {
                ++dimensionRight;
            }
            bits = Detector.sampleGrid(this.image, topLeft, bottomLeft, bottomRight, correctedTopRight, dimensionTop, dimensionRight);
        } else {
            int dimension = Math.min(dimensionRight, dimensionTop);
            correctedTopRight = this.correctTopRight(bottomLeft, bottomRight, topLeft, topRight, dimension);
            if (correctedTopRight == null) {
                correctedTopRight = topRight;
            }
            int dimensionCorrected = Math.max(this.transitionsBetween(topLeft, correctedTopRight).getTransitions(), this.transitionsBetween(bottomRight, correctedTopRight).getTransitions());
            if ((++dimensionCorrected & 1) == 1) {
                ++dimensionCorrected;
            }
            bits = Detector.sampleGrid(this.image, topLeft, bottomLeft, bottomRight, correctedTopRight, dimensionCorrected, dimensionCorrected);
        }
        return new DetectorResult(bits, new ResultPoint[]{topLeft, bottomLeft, bottomRight, correctedTopRight});
    }

    private ResultPoint correctTopRightRectangular(ResultPoint bottomLeft, ResultPoint bottomRight, ResultPoint topLeft, ResultPoint topRight, int dimensionTop, int dimensionRight) {
        int l2;
        float corr = (float)Detector.distance(bottomLeft, bottomRight) / (float)dimensionTop;
        int norm = Detector.distance(topLeft, topRight);
        float cos = (topRight.getX() - topLeft.getX()) / (float)norm;
        float sin = (topRight.getY() - topLeft.getY()) / (float)norm;
        ResultPoint c1 = new ResultPoint(topRight.getX() + corr * cos, topRight.getY() + corr * sin);
        corr = (float)Detector.distance(bottomLeft, topLeft) / (float)dimensionRight;
        norm = Detector.distance(bottomRight, topRight);
        cos = (topRight.getX() - bottomRight.getX()) / (float)norm;
        sin = (topRight.getY() - bottomRight.getY()) / (float)norm;
        ResultPoint c2 = new ResultPoint(topRight.getX() + corr * cos, topRight.getY() + corr * sin);
        if (!this.isValid(c1)) {
            if (this.isValid(c2)) {
                return c2;
            }
            return null;
        }
        if (!this.isValid(c2)) {
            return c1;
        }
        int l1 = Math.abs(dimensionTop - this.transitionsBetween(topLeft, c1).getTransitions()) + Math.abs(dimensionRight - this.transitionsBetween(bottomRight, c1).getTransitions());
        if (l1 <= (l2 = Math.abs(dimensionTop - this.transitionsBetween(topLeft, c2).getTransitions()) + Math.abs(dimensionRight - this.transitionsBetween(bottomRight, c2).getTransitions()))) {
            return c1;
        }
        return c2;
    }

    private ResultPoint correctTopRight(ResultPoint bottomLeft, ResultPoint bottomRight, ResultPoint topLeft, ResultPoint topRight, int dimension) {
        int l2;
        float corr = (float)Detector.distance(bottomLeft, bottomRight) / (float)dimension;
        int norm = Detector.distance(topLeft, topRight);
        float cos = (topRight.getX() - topLeft.getX()) / (float)norm;
        float sin = (topRight.getY() - topLeft.getY()) / (float)norm;
        ResultPoint c1 = new ResultPoint(topRight.getX() + corr * cos, topRight.getY() + corr * sin);
        corr = (float)Detector.distance(bottomLeft, topLeft) / (float)dimension;
        norm = Detector.distance(bottomRight, topRight);
        cos = (topRight.getX() - bottomRight.getX()) / (float)norm;
        sin = (topRight.getY() - bottomRight.getY()) / (float)norm;
        ResultPoint c2 = new ResultPoint(topRight.getX() + corr * cos, topRight.getY() + corr * sin);
        if (!this.isValid(c1)) {
            if (this.isValid(c2)) {
                return c2;
            }
            return null;
        }
        if (!this.isValid(c2)) {
            return c1;
        }
        int l1 = Math.abs(this.transitionsBetween(topLeft, c1).getTransitions() - this.transitionsBetween(bottomRight, c1).getTransitions());
        return l1 <= (l2 = Math.abs(this.transitionsBetween(topLeft, c2).getTransitions() - this.transitionsBetween(bottomRight, c2).getTransitions())) ? c1 : c2;
    }

    private boolean isValid(ResultPoint p) {
        return p.getX() >= 0.0f && p.getX() < (float)this.image.getWidth() && p.getY() > 0.0f && p.getY() < (float)this.image.getHeight();
    }

    private static int distance(ResultPoint a, ResultPoint b) {
        return MathUtils.round(ResultPoint.distance(a, b));
    }

    private static void increment(Map<ResultPoint, Integer> table, ResultPoint key) {
        Integer value = table.get(key);
        table.put(key, value == null ? 1 : value + 1);
    }

    private static BitMatrix sampleGrid(BitMatrix image, ResultPoint topLeft, ResultPoint bottomLeft, ResultPoint bottomRight, ResultPoint topRight, int dimensionX, int dimensionY) throws NotFoundException {
        GridSampler sampler = GridSampler.getInstance();
        return sampler.sampleGrid(image, dimensionX, dimensionY, 0.5f, 0.5f, (float)dimensionX - 0.5f, 0.5f, (float)dimensionX - 0.5f, (float)dimensionY - 0.5f, 0.5f, (float)dimensionY - 0.5f, topLeft.getX(), topLeft.getY(), topRight.getX(), topRight.getY(), bottomRight.getX(), bottomRight.getY(), bottomLeft.getX(), bottomLeft.getY());
    }

    private ResultPointsAndTransitions transitionsBetween(ResultPoint from, ResultPoint to) {
        boolean steep;
        int fromX = (int)from.getX();
        int fromY = (int)from.getY();
        int toX = (int)to.getX();
        int toY = (int)to.getY();
        boolean bl = steep = Math.abs(toY - fromY) > Math.abs(toX - fromX);
        if (steep) {
            int temp = fromX;
            fromX = fromY;
            fromY = temp;
            temp = toX;
            toX = toY;
            toY = temp;
        }
        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);
        int error = -dx / 2;
        int ystep = fromY < toY ? 1 : -1;
        int xstep = fromX < toX ? 1 : -1;
        int transitions = 0;
        boolean inBlack = this.image.get(steep ? fromY : fromX, steep ? fromX : fromY);
        int y = fromY;
        for (int x = fromX; x != toX; x += xstep) {
            boolean isBlack = this.image.get(steep ? y : x, steep ? x : y);
            if (isBlack != inBlack) {
                ++transitions;
                inBlack = isBlack;
            }
            if ((error += dy) <= 0) continue;
            if (y == toY) break;
            y += ystep;
            error -= dx;
        }
        return new ResultPointsAndTransitions(from, to, transitions);
    }

    private static final class ResultPointsAndTransitionsComparator
    implements Comparator<ResultPointsAndTransitions>,
    Serializable {
        private ResultPointsAndTransitionsComparator() {
        }

        @Override
        public int compare(ResultPointsAndTransitions o1, ResultPointsAndTransitions o2) {
            return o1.getTransitions() - o2.getTransitions();
        }
    }

    private static final class ResultPointsAndTransitions {
        private final ResultPoint from;
        private final ResultPoint to;
        private final int transitions;

        private ResultPointsAndTransitions(ResultPoint from, ResultPoint to, int transitions) {
            this.from = from;
            this.to = to;
            this.transitions = transitions;
        }

        ResultPoint getFrom() {
            return this.from;
        }

        ResultPoint getTo() {
            return this.to;
        }

        public int getTransitions() {
            return this.transitions;
        }

        public String toString() {
            return this.from + "/" + this.to + '/' + this.transitions;
        }
    }
}

