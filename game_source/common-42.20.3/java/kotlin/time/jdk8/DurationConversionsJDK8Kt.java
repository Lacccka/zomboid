/*
 * Decompiled with CFR 0.152.
 */
package kotlin.time.jdk8;

import kotlin.Metadata;
import kotlin.SinceKotlin;
import kotlin.internal.InlineOnly;
import kotlin.jvm.JvmName;
import kotlin.jvm.internal.Intrinsics;
import kotlin.time.Duration;
import kotlin.time.DurationKt;
import kotlin.time.ExperimentalTime;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
@Metadata(mv={1, 4, 0}, bv={1, 0, 3}, k=2, d1={"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u001a\u0017\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u0087\b\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0003\u0010\u0004\u001a\u0015\u0010\u0005\u001a\u00020\u0002*\u00020\u0001H\u0087\b\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0007"}, d2={"toJavaDuration", "Ljava/time/Duration;", "Lkotlin/time/Duration;", "toJavaDuration-LRDsOJo", "(D)Ljava/time/Duration;", "toKotlinDuration", "(Ljava/time/Duration;)D", "kotlin-stdlib-jdk8"}, pn="kotlin.time")
@JvmName(name="DurationConversionsJDK8Kt")
public final class DurationConversionsJDK8Kt {
    @SinceKotlin(version="1.3")
    @ExperimentalTime
    @InlineOnly
    private static final double toKotlinDuration(java.time.Duration $this$toKotlinDuration) {
        int $i$f$toKotlinDuration = 0;
        return Duration.plus-LRDsOJo(DurationKt.getSeconds($this$toKotlinDuration.getSeconds()), DurationKt.getNanoseconds($this$toKotlinDuration.getNano()));
    }

    /*
     * WARNING - void declaration
     */
    @SinceKotlin(version="1.3")
    @ExperimentalTime
    @InlineOnly
    private static final java.time.Duration toJavaDuration-LRDsOJo(double $this$toJavaDuration) {
        void nanoseconds;
        int n = 0;
        double $this$iv = $this$toJavaDuration;
        boolean $i$f$toComponents = false;
        boolean bl = false;
        int n2 = Duration.getNanosecondsComponent-impl($this$iv);
        long seconds = (long)Duration.getInSeconds-impl($this$iv);
        boolean bl2 = false;
        java.time.Duration duration = java.time.Duration.ofSeconds(seconds, (long)nanoseconds);
        Intrinsics.checkNotNullExpressionValue(duration, "toComponents { seconds, \u2026, nanoseconds.toLong()) }");
        return duration;
    }
}

