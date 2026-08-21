/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.tuple;

import java.util.Objects;
import org.apache.commons.lang3.tuple.Triple;

public class ImmutableTriple<L, M, R>
extends Triple<L, M, R> {
    public static final ImmutableTriple<?, ?, ?>[] EMPTY_ARRAY = new ImmutableTriple[0];
    private static final ImmutableTriple NULL = new ImmutableTriple<Object, Object, Object>(null, null, null);
    private static final long serialVersionUID = 1L;
    public final L left;
    public final M middle;
    public final R right;

    public static <L, M, R> ImmutableTriple<L, M, R>[] emptyArray() {
        return EMPTY_ARRAY;
    }

    public static <L, M, R> ImmutableTriple<L, M, R> nullTriple() {
        return NULL;
    }

    public static <L, M, R> ImmutableTriple<L, M, R> of(L left, M middle, R right) {
        return left != null | middle != null || right != null ? new ImmutableTriple<L, M, R>(left, middle, right) : ImmutableTriple.nullTriple();
    }

    public static <L, M, R> ImmutableTriple<L, M, R> ofNonNull(L left, M middle, R right) {
        return ImmutableTriple.of(Objects.requireNonNull(left, "left"), Objects.requireNonNull(middle, "middle"), Objects.requireNonNull(right, "right"));
    }

    public ImmutableTriple(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return this.left;
    }

    @Override
    public M getMiddle() {
        return this.middle;
    }

    @Override
    public R getRight() {
        return this.right;
    }
}

