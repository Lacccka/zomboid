/*
 * Decompiled with CFR 0.152.
 */
package kotlin.streams.jdk8;

import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import kotlin.Metadata;
import kotlin.SinceKotlin;
import kotlin.collections.ArraysKt;
import kotlin.jvm.JvmName;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;

@Metadata(mv={1, 4, 0}, bv={1, 0, 3}, k=2, d1={"\u0000.\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\u001a\u0012\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001*\u00020\u0003H\u0007\u001a\u0012\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00040\u0001*\u00020\u0005H\u0007\u001a\u0012\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00060\u0001*\u00020\u0007H\u0007\u001a\u001e\u0010\u0000\u001a\b\u0012\u0004\u0012\u0002H\b0\u0001\"\u0004\b\u0000\u0010\b*\b\u0012\u0004\u0012\u0002H\b0\tH\u0007\u001a\u001e\u0010\n\u001a\b\u0012\u0004\u0012\u0002H\b0\t\"\u0004\b\u0000\u0010\b*\b\u0012\u0004\u0012\u0002H\b0\u0001H\u0007\u001a\u0012\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00020\f*\u00020\u0003H\u0007\u001a\u0012\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00040\f*\u00020\u0005H\u0007\u001a\u0012\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00060\f*\u00020\u0007H\u0007\u001a\u001e\u0010\u000b\u001a\b\u0012\u0004\u0012\u0002H\b0\f\"\u0004\b\u0000\u0010\b*\b\u0012\u0004\u0012\u0002H\b0\tH\u0007\u00a8\u0006\r"}, d2={"asSequence", "Lkotlin/sequences/Sequence;", "", "Ljava/util/stream/DoubleStream;", "", "Ljava/util/stream/IntStream;", "", "Ljava/util/stream/LongStream;", "T", "Ljava/util/stream/Stream;", "asStream", "toList", "", "kotlin-stdlib-jdk8"}, pn="kotlin.streams")
@JvmName(name="StreamsKt")
public final class StreamsKt {
    @SinceKotlin(version="1.2")
    @NotNull
    public static final <T> Sequence<T> asSequence(@NotNull Stream<T> $this$asSequence) {
        Intrinsics.checkNotNullParameter($this$asSequence, "$this$asSequence");
        boolean bl = false;
        return new Sequence<T>($this$asSequence){
            final /* synthetic */ Stream $this_asSequence$inlined;
            {
                this.$this_asSequence$inlined = stream;
            }

            @NotNull
            public Iterator<T> iterator() {
                boolean bl = false;
                Iterator<T> iterator2 = this.$this_asSequence$inlined.iterator();
                Intrinsics.checkNotNullExpressionValue(iterator2, "iterator()");
                return iterator2;
            }
        };
    }

    @SinceKotlin(version="1.2")
    @NotNull
    public static final Sequence<Integer> asSequence(@NotNull IntStream $this$asSequence) {
        Intrinsics.checkNotNullParameter($this$asSequence, "$this$asSequence");
        boolean bl = false;
        return new Sequence<Integer>($this$asSequence){
            final /* synthetic */ IntStream $this_asSequence$inlined;
            {
                this.$this_asSequence$inlined = intStream;
            }

            @NotNull
            public Iterator<Integer> iterator() {
                boolean bl = false;
                PrimitiveIterator.OfInt ofInt = this.$this_asSequence$inlined.iterator();
                Intrinsics.checkNotNullExpressionValue(ofInt, "iterator()");
                return ofInt;
            }
        };
    }

    @SinceKotlin(version="1.2")
    @NotNull
    public static final Sequence<Long> asSequence(@NotNull LongStream $this$asSequence) {
        Intrinsics.checkNotNullParameter($this$asSequence, "$this$asSequence");
        boolean bl = false;
        return new Sequence<Long>($this$asSequence){
            final /* synthetic */ LongStream $this_asSequence$inlined;
            {
                this.$this_asSequence$inlined = longStream;
            }

            @NotNull
            public Iterator<Long> iterator() {
                boolean bl = false;
                PrimitiveIterator.OfLong ofLong = this.$this_asSequence$inlined.iterator();
                Intrinsics.checkNotNullExpressionValue(ofLong, "iterator()");
                return ofLong;
            }
        };
    }

    @SinceKotlin(version="1.2")
    @NotNull
    public static final Sequence<Double> asSequence(@NotNull DoubleStream $this$asSequence) {
        Intrinsics.checkNotNullParameter($this$asSequence, "$this$asSequence");
        boolean bl = false;
        return new Sequence<Double>($this$asSequence){
            final /* synthetic */ DoubleStream $this_asSequence$inlined;
            {
                this.$this_asSequence$inlined = doubleStream;
            }

            @NotNull
            public Iterator<Double> iterator() {
                boolean bl = false;
                PrimitiveIterator.OfDouble ofDouble = this.$this_asSequence$inlined.iterator();
                Intrinsics.checkNotNullExpressionValue(ofDouble, "iterator()");
                return ofDouble;
            }
        };
    }

    @SinceKotlin(version="1.2")
    @NotNull
    public static final <T> Stream<T> asStream(@NotNull Sequence<? extends T> $this$asStream) {
        Intrinsics.checkNotNullParameter($this$asStream, "$this$asStream");
        Stream stream = StreamSupport.stream(new Supplier<Spliterator<T>>($this$asStream){
            final /* synthetic */ Sequence $this_asStream;

            public final Spliterator<T> get() {
                return Spliterators.spliteratorUnknownSize(this.$this_asStream.iterator(), 16);
            }
            {
                this.$this_asStream = sequence;
            }
        }, 16, false);
        Intrinsics.checkNotNullExpressionValue(stream, "StreamSupport.stream({ S\u2026literator.ORDERED, false)");
        return stream;
    }

    @SinceKotlin(version="1.2")
    @NotNull
    public static final <T> List<T> toList(@NotNull Stream<T> $this$toList) {
        Intrinsics.checkNotNullParameter($this$toList, "$this$toList");
        List list = $this$toList.collect(Collectors.toList());
        Intrinsics.checkNotNullExpressionValue(list, "collect(Collectors.toList<T>())");
        return list;
    }

    @SinceKotlin(version="1.2")
    @NotNull
    public static final List<Integer> toList(@NotNull IntStream $this$toList) {
        Intrinsics.checkNotNullParameter($this$toList, "$this$toList");
        int[] nArray = $this$toList.toArray();
        Intrinsics.checkNotNullExpressionValue(nArray, "toArray()");
        return ArraysKt.asList(nArray);
    }

    @SinceKotlin(version="1.2")
    @NotNull
    public static final List<Long> toList(@NotNull LongStream $this$toList) {
        Intrinsics.checkNotNullParameter($this$toList, "$this$toList");
        long[] lArray = $this$toList.toArray();
        Intrinsics.checkNotNullExpressionValue(lArray, "toArray()");
        return ArraysKt.asList(lArray);
    }

    @SinceKotlin(version="1.2")
    @NotNull
    public static final List<Double> toList(@NotNull DoubleStream $this$toList) {
        Intrinsics.checkNotNullParameter($this$toList, "$this$toList");
        double[] dArray = $this$toList.toArray();
        Intrinsics.checkNotNullExpressionValue(dArray, "toArray()");
        return ArraysKt.asList(dArray);
    }
}

