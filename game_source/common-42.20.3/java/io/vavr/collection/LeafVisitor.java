/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

@FunctionalInterface
interface LeafVisitor<T> {
    public int visit(int var1, T var2, int var3, int var4);
}

