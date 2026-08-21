/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.ArrayType;

@FunctionalInterface
interface NodeModifier {
    public static final NodeModifier COPY_NODE = (o, i) -> ArrayType.obj().copy(o, i + 1);
    public static final NodeModifier IDENTITY = (o, i) -> o;

    public Object apply(Object var1, int var2);
}

