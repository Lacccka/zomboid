/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class LangCollectors {
    private static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    public static <T, R, A> R collect(Collector<? super T, A, R> collector, T ... array) {
        return Arrays.stream(array).collect(collector);
    }

    public static Collector<Object, ?, String> joining() {
        return new SimpleCollector(StringBuilder::new, StringBuilder::append, StringBuilder::append, StringBuilder::toString, CH_NOID);
    }

    public static Collector<Object, ?, String> joining(CharSequence delimiter) {
        return LangCollectors.joining(delimiter, "", "");
    }

    public static Collector<Object, ?, String> joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        return LangCollectors.joining(delimiter, prefix, suffix, Objects::toString);
    }

    public static Collector<Object, ?, String> joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix, Function<Object, String> toString2) {
        return new SimpleCollector(() -> new StringJoiner(delimiter, prefix, suffix), (a, t) -> a.add((CharSequence)toString2.apply(t)), StringJoiner::merge, StringJoiner::toString, CH_NOID);
    }

    private LangCollectors() {
    }

    private static final class SimpleCollector<T, A, R>
    implements Collector<T, A, R> {
        private final BiConsumer<A, T> accumulator;
        private final Set<Collector.Characteristics> characteristics;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Supplier<A> supplier;

        private SimpleCollector(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher, Set<Collector.Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return this.accumulator;
        }

        @Override
        public Set<Collector.Characteristics> characteristics() {
            return this.characteristics;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return this.combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return this.finisher;
        }

        @Override
        public Supplier<A> supplier() {
            return this.supplier;
        }
    }
}

