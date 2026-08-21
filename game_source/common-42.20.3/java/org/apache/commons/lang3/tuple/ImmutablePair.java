/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.tuple;

import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;

public class ImmutablePair<L, R>
extends Pair<L, R> {
    public static final ImmutablePair<?, ?>[] EMPTY_ARRAY = new ImmutablePair[0];
    private static final ImmutablePair NULL = new ImmutablePair<Object, Object>(null, null);
    private static final long serialVersionUID = 4954918890077093841L;
    public final L left;
    public final R right;

    public static <L, R> ImmutablePair<L, R>[] emptyArray() {
        return EMPTY_ARRAY;
    }

    public static <L, R> Pair<L, R> left(L left) {
        return ImmutablePair.of(left, null);
    }

    public static <L, R> ImmutablePair<L, R> nullPair() {
        return NULL;
    }

    public static <L, R> ImmutablePair<L, R> of(L left, R right) {
        return left != null || right != null ? new ImmutablePair<L, R>(left, right) : ImmutablePair.nullPair();
    }

    public static <L, R> ImmutablePair<L, R> of(Map.Entry<L, R> pair) {
        return pair != null ? new ImmutablePair<L, R>(pair.getKey(), pair.getValue()) : ImmutablePair.nullPair();
    }

    public static <L, R> ImmutablePair<L, R> ofNonNull(L left, R right) {
        return ImmutablePair.of(Objects.requireNonNull(left, "left"), Objects.requireNonNull(right, "right"));
    }

    public static <L, R> Pair<L, R> right(R right) {
        return ImmutablePair.of(null, right);
    }

    public ImmutablePair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return this.left;
    }

    @Override
    public R getRight() {
        return this.right;
    }

    @Override
    public R setValue(R value) {
        throw new UnsupportedOperationException();
    }
}

