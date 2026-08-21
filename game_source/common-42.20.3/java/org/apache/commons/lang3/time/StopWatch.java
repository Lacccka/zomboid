/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class StopWatch {
    private static final long NANO_2_MILLIS = 1000000L;
    private final String message;
    private State runningState = State.UNSTARTED;
    private SplitState splitState = SplitState.UNSPLIT;
    private long startTimeNanos;
    private Instant startInstant;
    private Instant stopInstant;
    private long stopTimeNanos;

    public static StopWatch create() {
        return new StopWatch();
    }

    public static StopWatch createStarted() {
        StopWatch sw = new StopWatch();
        sw.start();
        return sw;
    }

    public StopWatch() {
        this(null);
    }

    public StopWatch(String message) {
        this.message = message;
    }

    public String formatSplitTime() {
        return DurationFormatUtils.formatDurationHMS(this.getSplitDuration().toMillis());
    }

    public String formatTime() {
        return DurationFormatUtils.formatDurationHMS(this.getTime());
    }

    public Duration getDuration() {
        return Duration.ofNanos(this.getNanoTime());
    }

    public String getMessage() {
        return this.message;
    }

    public long getNanoTime() {
        if (this.runningState == State.STOPPED || this.runningState == State.SUSPENDED) {
            return this.stopTimeNanos - this.startTimeNanos;
        }
        if (this.runningState == State.UNSTARTED) {
            return 0L;
        }
        if (this.runningState == State.RUNNING) {
            return System.nanoTime() - this.startTimeNanos;
        }
        throw new IllegalStateException("Illegal running state has occurred.");
    }

    public Duration getSplitDuration() {
        return Duration.ofNanos(this.getSplitNanoTime());
    }

    public long getSplitNanoTime() {
        if (this.splitState != SplitState.SPLIT) {
            throw new IllegalStateException("Stopwatch must be split to get the split time.");
        }
        return this.stopTimeNanos - this.startTimeNanos;
    }

    @Deprecated
    public long getSplitTime() {
        return this.nanosToMillis(this.getSplitNanoTime());
    }

    public Instant getStartInstant() {
        return Instant.ofEpochMilli(this.getStartTime());
    }

    @Deprecated
    public long getStartTime() {
        if (this.runningState == State.UNSTARTED) {
            throw new IllegalStateException("Stopwatch has not been started");
        }
        return this.startInstant.toEpochMilli();
    }

    public Instant getStopInstant() {
        return Instant.ofEpochMilli(this.getStopTime());
    }

    @Deprecated
    public long getStopTime() {
        if (this.runningState == State.UNSTARTED) {
            throw new IllegalStateException("Stopwatch has not been started");
        }
        return this.stopInstant.toEpochMilli();
    }

    @Deprecated
    public long getTime() {
        return this.nanosToMillis(this.getNanoTime());
    }

    public long getTime(TimeUnit timeUnit) {
        return timeUnit.convert(this.getNanoTime(), TimeUnit.NANOSECONDS);
    }

    public boolean isStarted() {
        return this.runningState.isStarted();
    }

    public boolean isStopped() {
        return this.runningState.isStopped();
    }

    public boolean isSuspended() {
        return this.runningState.isSuspended();
    }

    private long nanosToMillis(long nanos) {
        return nanos / 1000000L;
    }

    public void reset() {
        this.runningState = State.UNSTARTED;
        this.splitState = SplitState.UNSPLIT;
    }

    public void resume() {
        if (this.runningState != State.SUSPENDED) {
            throw new IllegalStateException("Stopwatch must be suspended to resume. ");
        }
        this.startTimeNanos += System.nanoTime() - this.stopTimeNanos;
        this.runningState = State.RUNNING;
    }

    public void split() {
        if (this.runningState != State.RUNNING) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        this.stopTimeNanos = System.nanoTime();
        this.splitState = SplitState.SPLIT;
    }

    public void start() {
        if (this.runningState == State.STOPPED) {
            throw new IllegalStateException("Stopwatch must be reset before being restarted. ");
        }
        if (this.runningState != State.UNSTARTED) {
            throw new IllegalStateException("Stopwatch already started. ");
        }
        this.startTimeNanos = System.nanoTime();
        this.startInstant = Instant.now();
        this.runningState = State.RUNNING;
    }

    public void stop() {
        if (this.runningState != State.RUNNING && this.runningState != State.SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        if (this.runningState == State.RUNNING) {
            this.stopTimeNanos = System.nanoTime();
            this.stopInstant = Instant.now();
        }
        this.runningState = State.STOPPED;
    }

    public void suspend() {
        if (this.runningState != State.RUNNING) {
            throw new IllegalStateException("Stopwatch must be running to suspend. ");
        }
        this.stopTimeNanos = System.nanoTime();
        this.stopInstant = Instant.now();
        this.runningState = State.SUSPENDED;
    }

    public String toSplitString() {
        String msgStr = Objects.toString(this.message, "");
        String formattedTime = this.formatSplitTime();
        return msgStr.isEmpty() ? formattedTime : msgStr + " " + formattedTime;
    }

    public String toString() {
        String msgStr = Objects.toString(this.message, "");
        String formattedTime = this.formatTime();
        return msgStr.isEmpty() ? formattedTime : msgStr + " " + formattedTime;
    }

    public void unsplit() {
        if (this.splitState != SplitState.SPLIT) {
            throw new IllegalStateException("Stopwatch has not been split. ");
        }
        this.splitState = SplitState.UNSPLIT;
    }

    private static enum State {
        RUNNING{

            @Override
            boolean isStarted() {
                return true;
            }

            @Override
            boolean isStopped() {
                return false;
            }

            @Override
            boolean isSuspended() {
                return false;
            }
        }
        ,
        STOPPED{

            @Override
            boolean isStarted() {
                return false;
            }

            @Override
            boolean isStopped() {
                return true;
            }

            @Override
            boolean isSuspended() {
                return false;
            }
        }
        ,
        SUSPENDED{

            @Override
            boolean isStarted() {
                return true;
            }

            @Override
            boolean isStopped() {
                return false;
            }

            @Override
            boolean isSuspended() {
                return true;
            }
        }
        ,
        UNSTARTED{

            @Override
            boolean isStarted() {
                return false;
            }

            @Override
            boolean isStopped() {
                return true;
            }

            @Override
            boolean isSuspended() {
                return false;
            }
        };


        abstract boolean isStarted();

        abstract boolean isStopped();

        abstract boolean isSuspended();
    }

    private static enum SplitState {
        SPLIT,
        UNSPLIT;

    }
}

