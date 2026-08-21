/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.exemplars;

import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfig;
import io.prometheus.metrics.core.util.Scheduler;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.tracer.common.SpanContext;
import io.prometheus.metrics.tracer.initializer.SpanContextSupplier;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

public class ExemplarSampler {
    private final ExemplarSamplerConfig config;
    private final Exemplar[] exemplars;
    private final Exemplar[] customExemplars;
    private final AtomicBoolean acceptingNewExemplars = new AtomicBoolean(true);
    private final AtomicBoolean acceptingNewCustomExemplars = new AtomicBoolean(true);
    private final SpanContext spanContext;

    public ExemplarSampler(ExemplarSamplerConfig config) {
        this(config, null);
    }

    public ExemplarSampler(ExemplarSamplerConfig config, SpanContext spanContext) {
        this.config = config;
        this.exemplars = new Exemplar[config.getNumberOfExemplars()];
        this.customExemplars = new Exemplar[this.exemplars.length];
        this.spanContext = spanContext;
    }

    public Exemplars collect() {
        Exemplar exemplar;
        int i;
        long now = System.currentTimeMillis();
        ArrayList<Exemplar> result = new ArrayList<Exemplar>(this.exemplars.length);
        for (i = 0; i < this.customExemplars.length; ++i) {
            exemplar = this.customExemplars[i];
            if (exemplar == null) continue;
            if (now - exemplar.getTimestampMillis() > this.config.getMaxRetentionPeriodMillis()) {
                this.customExemplars[i] = null;
                continue;
            }
            result.add(exemplar);
        }
        for (i = 0; i < this.exemplars.length && result.size() < this.exemplars.length; ++i) {
            exemplar = this.exemplars[i];
            if (exemplar == null) continue;
            if (now - exemplar.getTimestampMillis() > this.config.getMaxRetentionPeriodMillis()) {
                this.exemplars[i] = null;
                continue;
            }
            result.add(exemplar);
        }
        return Exemplars.of(result);
    }

    public void reset() {
        for (int i = 0; i < this.exemplars.length; ++i) {
            this.exemplars[i] = null;
            this.customExemplars[i] = null;
        }
    }

    public void observe(double value) {
        if (!this.acceptingNewExemplars.get()) {
            return;
        }
        this.rateLimitedObserve(this.acceptingNewExemplars, value, () -> this.doObserve(value));
    }

    public void observeWithExemplar(double value, Labels labels) {
        if (!this.acceptingNewCustomExemplars.get()) {
            return;
        }
        this.rateLimitedObserve(this.acceptingNewCustomExemplars, value, () -> this.doObserveWithExemplar(value, labels));
    }

    private long doObserve(double value) {
        if (this.exemplars.length == 1) {
            return this.doObserveSingleExemplar(value);
        }
        if (this.config.getHistogramClassicUpperBounds() != null) {
            return this.doObserveWithUpperBounds(value);
        }
        return this.doObserveWithoutUpperBounds(value);
    }

    private long doObserveSingleExemplar(double value) {
        long now = System.currentTimeMillis();
        Exemplar current = this.exemplars[0];
        if (current == null || now - current.getTimestampMillis() > this.config.getMinRetentionPeriodMillis()) {
            return this.updateExemplar(0, value, now);
        }
        return 0L;
    }

    private long doObserveSingleExemplar(double amount, Labels labels) {
        long now = System.currentTimeMillis();
        Exemplar current = this.customExemplars[0];
        if (current == null || now - current.getTimestampMillis() > this.config.getMinRetentionPeriodMillis()) {
            return this.updateCustomExemplar(0, amount, labels, now);
        }
        return 0L;
    }

    private long doObserveWithUpperBounds(double value) {
        long now = System.currentTimeMillis();
        double[] upperBounds = this.config.getHistogramClassicUpperBounds();
        for (int i = 0; i < upperBounds.length; ++i) {
            if (!(value <= upperBounds[i])) continue;
            Exemplar previous = this.exemplars[i];
            if (previous == null || now - previous.getTimestampMillis() > this.config.getMinRetentionPeriodMillis()) {
                return this.updateExemplar(i, value, now);
            }
            return 0L;
        }
        return 0L;
    }

    private long doObserveWithoutUpperBounds(double value) {
        long now = System.currentTimeMillis();
        Exemplar smallest = null;
        int smallestIndex = -1;
        Exemplar largest = null;
        int largestIndex = -1;
        int nullIndex = -1;
        for (int i = this.exemplars.length - 1; i >= 0; --i) {
            Exemplar exemplar = this.exemplars[i];
            if (exemplar == null) {
                nullIndex = i;
                continue;
            }
            if (now - exemplar.getTimestampMillis() > this.config.getMaxRetentionPeriodMillis()) {
                this.exemplars[i] = null;
                nullIndex = i;
                continue;
            }
            if (smallest == null || exemplar.getValue() < smallest.getValue()) {
                smallest = exemplar;
                smallestIndex = i;
            }
            if (largest != null && !(exemplar.getValue() > largest.getValue())) continue;
            largest = exemplar;
            largestIndex = i;
        }
        if (nullIndex >= 0) {
            return this.updateExemplar(nullIndex, value, now);
        }
        if (now - smallest.getTimestampMillis() > this.config.getMinRetentionPeriodMillis() && value < smallest.getValue()) {
            return this.updateExemplar(smallestIndex, value, now);
        }
        if (now - largest.getTimestampMillis() > this.config.getMinRetentionPeriodMillis() && value > largest.getValue()) {
            return this.updateExemplar(largestIndex, value, now);
        }
        long oldestTimestamp = 0L;
        int oldestIndex = -1;
        for (int i = 0; i < this.exemplars.length; ++i) {
            Exemplar exemplar = this.exemplars[i];
            if (exemplar == null || exemplar == smallest || exemplar == largest || oldestTimestamp != 0L && exemplar.getTimestampMillis() >= oldestTimestamp) continue;
            oldestTimestamp = exemplar.getTimestampMillis();
            oldestIndex = i;
        }
        if (oldestIndex != -1 && now - oldestTimestamp > this.config.getMinRetentionPeriodMillis()) {
            return this.updateExemplar(oldestIndex, value, now);
        }
        return 0L;
    }

    private long doObserveWithExemplar(double amount, Labels labels) {
        if (this.customExemplars.length == 1) {
            return this.doObserveSingleExemplar(amount, labels);
        }
        if (this.config.getHistogramClassicUpperBounds() != null) {
            return this.doObserveWithExemplarWithUpperBounds(amount, labels);
        }
        return this.doObserveWithExemplarWithoutUpperBounds(amount, labels);
    }

    private long doObserveWithExemplarWithUpperBounds(double value, Labels labels) {
        long now = System.currentTimeMillis();
        double[] upperBounds = this.config.getHistogramClassicUpperBounds();
        for (int i = 0; i < upperBounds.length; ++i) {
            if (!(value <= upperBounds[i])) continue;
            Exemplar previous = this.customExemplars[i];
            if (previous == null || now - previous.getTimestampMillis() > this.config.getMinRetentionPeriodMillis()) {
                return this.updateCustomExemplar(i, value, labels, now);
            }
            return 0L;
        }
        return 0L;
    }

    private long doObserveWithExemplarWithoutUpperBounds(double amount, Labels labels) {
        long now = System.currentTimeMillis();
        int nullPos = -1;
        int oldestPos = -1;
        Exemplar oldest = null;
        for (int i = this.customExemplars.length - 1; i >= 0; --i) {
            Exemplar exemplar = this.customExemplars[i];
            if (exemplar == null) {
                nullPos = i;
                continue;
            }
            if (now - exemplar.getTimestampMillis() > this.config.getMaxRetentionPeriodMillis()) {
                this.customExemplars[i] = null;
                nullPos = i;
                continue;
            }
            if (oldest != null && exemplar.getTimestampMillis() >= oldest.getTimestampMillis()) continue;
            oldest = exemplar;
            oldestPos = i;
        }
        if (nullPos != -1) {
            return this.updateCustomExemplar(nullPos, amount, labels, now);
        }
        if (now - oldest.getTimestampMillis() > this.config.getMinRetentionPeriodMillis()) {
            return this.updateCustomExemplar(oldestPos, amount, labels, now);
        }
        return 0L;
    }

    private void rateLimitedObserve(AtomicBoolean accepting, double value, LongSupplier observeFunc) {
        if (Double.isNaN(value)) {
            return;
        }
        if (!accepting.compareAndSet(true, false)) {
            return;
        }
        long now = observeFunc.getAsLong();
        long sleepTime = now == 0L ? this.config.getSampleIntervalMillis() : this.durationUntilNextExemplarExpires(now);
        Scheduler.schedule(() -> accepting.compareAndSet(false, true), sleepTime, TimeUnit.MILLISECONDS);
    }

    private long durationUntilNextExemplarExpires(long now) {
        long oldestTimestamp = now;
        for (Exemplar exemplar : this.exemplars) {
            if (exemplar == null) {
                return this.config.getSampleIntervalMillis();
            }
            if (exemplar.getTimestampMillis() >= oldestTimestamp) continue;
            oldestTimestamp = exemplar.getTimestampMillis();
        }
        long oldestAge = now - oldestTimestamp;
        if (oldestAge < this.config.getMinRetentionPeriodMillis()) {
            return this.config.getMinRetentionPeriodMillis() - oldestAge;
        }
        return this.config.getSampleIntervalMillis();
    }

    private long updateCustomExemplar(int index, double value, Labels labels, long now) {
        if (!labels.contains("trace_id") && !labels.contains("span_id")) {
            labels = labels.merge(this.doSampleExemplar());
        }
        this.customExemplars[index] = Exemplar.builder().value(value).labels(labels).timestampMillis(now).build();
        return now;
    }

    private long updateExemplar(int index, double value, long now) {
        Labels traceLabels = this.doSampleExemplar();
        if (!traceLabels.isEmpty()) {
            this.exemplars[index] = Exemplar.builder().value(value).labels(traceLabels).timestampMillis(now).build();
            return now;
        }
        return 0L;
    }

    private Labels doSampleExemplar() {
        SpanContext spanContext = this.spanContext != null ? this.spanContext : SpanContextSupplier.getSpanContext();
        try {
            if (spanContext != null && spanContext.isCurrentSpanSampled()) {
                String spanId = spanContext.getCurrentSpanId();
                String traceId = spanContext.getCurrentTraceId();
                if (spanId != null && traceId != null) {
                    spanContext.markCurrentSpanAsExemplar();
                    return Labels.of("trace_id", traceId, "span_id", spanId);
                }
            }
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        return Labels.EMPTY;
    }
}

