/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

interface CheckedPredicateModule {
    public static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
        throw t;
    }
}

