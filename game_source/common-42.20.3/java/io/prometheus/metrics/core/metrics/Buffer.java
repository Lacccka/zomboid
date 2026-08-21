/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class Buffer {
    private static final long bufferActiveBit = Long.MIN_VALUE;
    private final AtomicLong observationCount = new AtomicLong(0L);
    private double[] observationBuffer = new double[0];
    private int bufferPos = 0;
    private boolean reset = false;
    ReentrantLock appendLock = new ReentrantLock();
    ReentrantLock runLock = new ReentrantLock();
    Condition bufferFilled = this.appendLock.newCondition();

    Buffer() {
    }

    boolean append(double value) {
        long count = this.observationCount.incrementAndGet();
        if ((count & Long.MIN_VALUE) == 0L) {
            return false;
        }
        this.doAppend(value);
        return true;
    }

    private void doAppend(double amount) {
        this.appendLock.lock();
        try {
            if (this.bufferPos >= this.observationBuffer.length) {
                this.observationBuffer = Arrays.copyOf(this.observationBuffer, this.observationBuffer.length + 128);
            }
            this.observationBuffer[this.bufferPos] = amount;
            ++this.bufferPos;
            this.bufferFilled.signalAll();
        }
        finally {
            this.appendLock.unlock();
        }
    }

    void reset() {
        this.reset = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    <T extends DataPointSnapshot> T run(Function<Long, Boolean> complete, Supplier<T> createResult, Consumer<Double> observeFunction) {
        int bufferSize;
        double[] buffer;
        DataPointSnapshot result;
        this.runLock.lock();
        try {
            int expectedBufferSize;
            Long expectedCount = this.observationCount.getAndAdd(Long.MIN_VALUE);
            while (!complete.apply(expectedCount).booleanValue()) {
                Thread.yield();
            }
            result = (DataPointSnapshot)createResult.get();
            if (this.reset) {
                expectedBufferSize = (int)((this.observationCount.getAndSet(0L) & Long.MAX_VALUE) - expectedCount);
                this.reset = false;
            } else {
                expectedBufferSize = (int)(this.observationCount.addAndGet(Long.MIN_VALUE) - expectedCount);
            }
            this.appendLock.lock();
            try {
                while (this.bufferPos < expectedBufferSize) {
                    this.bufferFilled.await();
                }
            }
            finally {
                this.appendLock.unlock();
            }
            buffer = this.observationBuffer;
            bufferSize = this.bufferPos;
            this.observationBuffer = new double[0];
            this.bufferPos = 0;
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            this.runLock.unlock();
        }
        for (int i = 0; i < bufferSize; ++i) {
            observeFunction.accept(buffer[i]);
        }
        return (T)result;
    }
}

