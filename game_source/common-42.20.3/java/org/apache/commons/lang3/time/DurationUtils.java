/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.time;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.LongRange;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.math.NumberUtils;

public class DurationUtils {
    static final LongRange LONG_TO_INT_RANGE = LongRange.of(NumberUtils.LONG_INT_MIN_VALUE, NumberUtils.LONG_INT_MAX_VALUE);

    public static <T extends Throwable> void accept(FailableBiConsumer<Long, Integer, T> consumer, Duration duration) throws T {
        if (consumer != null && duration != null) {
            consumer.accept(duration.toMillis(), DurationUtils.getNanosOfMilli(duration));
        }
    }

    @Deprecated
    public static int getNanosOfMiili(Duration duration) {
        return DurationUtils.getNanosOfMilli(duration);
    }

    public static int getNanosOfMilli(Duration duration) {
        return DurationUtils.zeroIfNull(duration).getNano() % 1000000;
    }

    public static boolean isPositive(Duration duration) {
        return !duration.isNegative() && !duration.isZero();
    }

    private static <E extends Throwable> Instant now(FailableConsumer<Instant, E> nowConsumer) throws E {
        Instant start = Instant.now();
        nowConsumer.accept(start);
        return start;
    }

    public static <E extends Throwable> Duration of(FailableConsumer<Instant, E> consumer) throws E {
        return DurationUtils.since(DurationUtils.now(consumer::accept));
    }

    public static <E extends Throwable> Duration of(FailableRunnable<E> runnable2) throws E {
        return DurationUtils.of((Instant start) -> runnable2.run());
    }

    public static Duration since(Temporal startInclusive) {
        return Duration.between(startInclusive, Instant.now());
    }

    static ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        switch (Objects.requireNonNull(timeUnit)) {
            case NANOSECONDS: {
                return ChronoUnit.NANOS;
            }
            case MICROSECONDS: {
                return ChronoUnit.MICROS;
            }
            case MILLISECONDS: {
                return ChronoUnit.MILLIS;
            }
            case SECONDS: {
                return ChronoUnit.SECONDS;
            }
            case MINUTES: {
                return ChronoUnit.MINUTES;
            }
            case HOURS: {
                return ChronoUnit.HOURS;
            }
            case DAYS: {
                return ChronoUnit.DAYS;
            }
        }
        throw new IllegalArgumentException(timeUnit.toString());
    }

    public static Duration toDuration(long amount, TimeUnit timeUnit) {
        return Duration.of(amount, DurationUtils.toChronoUnit(timeUnit));
    }

    public static int toMillisInt(Duration duration) {
        Objects.requireNonNull(duration, "duration");
        return LONG_TO_INT_RANGE.fit(duration.toMillis()).intValue();
    }

    public static Duration zeroIfNull(Duration duration) {
        return ObjectUtils.defaultIfNull(duration, Duration.ZERO);
    }

    @Deprecated
    public DurationUtils() {
    }
}

