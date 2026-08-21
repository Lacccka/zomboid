/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.statistics;

import java.util.Arrays;
import org.uncommons.maths.statistics.EmptyDataSetException;

public class DataSet {
    private static final int DEFAULT_CAPACITY = 50;
    private static final double GROWTH_RATE = 1.5;
    private double[] dataSet;
    private int dataSetSize = 0;
    private double total = 0.0;
    private double product = 1.0;
    private double reciprocalSum = 0.0;
    private double minimum = Double.MAX_VALUE;
    private double maximum = Double.MIN_VALUE;

    public DataSet() {
        this(50);
    }

    public DataSet(int capacity) {
        this.dataSet = new double[capacity];
        this.dataSetSize = 0;
    }

    public DataSet(double[] dataSet) {
        this.dataSet = (double[])dataSet.clone();
        this.dataSetSize = dataSet.length;
        for (double value : this.dataSet) {
            this.updateStatsWithNewValue(value);
        }
    }

    public void addValue(double value) {
        if (this.dataSetSize == this.dataSet.length) {
            int newLength = (int)(1.5 * (double)this.dataSetSize);
            double[] newDataSet = new double[newLength];
            System.arraycopy(this.dataSet, 0, newDataSet, 0, this.dataSetSize);
            this.dataSet = newDataSet;
        }
        this.dataSet[this.dataSetSize] = value;
        this.updateStatsWithNewValue(value);
        ++this.dataSetSize;
    }

    private void updateStatsWithNewValue(double value) {
        this.total += value;
        this.product *= value;
        this.reciprocalSum += 1.0 / value;
        this.minimum = Math.min(this.minimum, value);
        this.maximum = Math.max(this.maximum, value);
    }

    private void assertNotEmpty() {
        if (this.getSize() == 0) {
            throw new EmptyDataSetException();
        }
    }

    public final int getSize() {
        return this.dataSetSize;
    }

    public final double getMinimum() {
        this.assertNotEmpty();
        return this.minimum;
    }

    public final double getMaximum() {
        this.assertNotEmpty();
        return this.maximum;
    }

    public final double getMedian() {
        this.assertNotEmpty();
        double[] dataCopy = new double[this.getSize()];
        System.arraycopy(this.dataSet, 0, dataCopy, 0, dataCopy.length);
        Arrays.sort(dataCopy);
        int midPoint = dataCopy.length / 2;
        if (dataCopy.length % 2 != 0) {
            return dataCopy[midPoint];
        }
        return dataCopy[midPoint - 1] + (dataCopy[midPoint] - dataCopy[midPoint - 1]) / 2.0;
    }

    public final double getAggregate() {
        this.assertNotEmpty();
        return this.total;
    }

    public final double getProduct() {
        this.assertNotEmpty();
        return this.product;
    }

    public final double getArithmeticMean() {
        this.assertNotEmpty();
        return this.total / (double)this.dataSetSize;
    }

    public final double getGeometricMean() {
        this.assertNotEmpty();
        return Math.pow(this.product, 1.0 / (double)this.dataSetSize);
    }

    public final double getHarmonicMean() {
        this.assertNotEmpty();
        return (double)this.dataSetSize / this.reciprocalSum;
    }

    public final double getMeanDeviation() {
        double mean = this.getArithmeticMean();
        double diffs = 0.0;
        for (int i = 0; i < this.dataSetSize; ++i) {
            diffs += Math.abs(mean - this.dataSet[i]);
        }
        return diffs / (double)this.dataSetSize;
    }

    public final double getVariance() {
        return this.sumSquaredDiffs() / (double)this.getSize();
    }

    private double sumSquaredDiffs() {
        double mean = this.getArithmeticMean();
        double squaredDiffs = 0.0;
        for (int i = 0; i < this.getSize(); ++i) {
            double diff = mean - this.dataSet[i];
            squaredDiffs += diff * diff;
        }
        return squaredDiffs;
    }

    public final double getStandardDeviation() {
        return Math.sqrt(this.getVariance());
    }

    public final double getSampleVariance() {
        return this.sumSquaredDiffs() / (double)(this.getSize() - 1);
    }

    public final double getSampleStandardDeviation() {
        return Math.sqrt(this.getSampleVariance());
    }
}

