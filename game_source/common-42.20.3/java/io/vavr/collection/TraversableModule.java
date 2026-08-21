/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import java.util.function.ToDoubleFunction;

interface TraversableModule {
    public static <T> double[] neumaierSum(Iterable<T> ts, ToDoubleFunction<T> toDouble) {
        double simpleSum = 0.0;
        double sum = 0.0;
        double compensation = 0.0;
        int size = 0;
        for (T t : ts) {
            double d = toDouble.applyAsDouble(t);
            double tmp = sum + d;
            compensation += Math.abs(sum) >= Math.abs(d) ? sum - tmp + d : d - tmp + sum;
            sum = tmp;
            simpleSum += d;
            ++size;
        }
        if (size > 0 && Double.isNaN(sum += compensation) && Double.isInfinite(simpleSum)) {
            sum = simpleSum;
        }
        return new double[]{sum, size};
    }
}

