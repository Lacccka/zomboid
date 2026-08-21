/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import org.apache.commons.io.function.IOBiConsumer;
import org.apache.commons.io.function.IOBiFunction;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.IOPredicate;
import org.apache.commons.io.function.IORunnable;
import org.apache.commons.io.function.IOTriConsumer;

final class Constants {
    static final IOBiConsumer IO_BI_CONSUMER = (t, u) -> {};
    static final IORunnable IO_RUNNABLE = () -> {};
    static final IOBiFunction IO_BI_FUNCTION = (t, u) -> null;
    static final IOFunction IO_FUNCTION_ID = t -> t;
    static final IOPredicate<Object> IO_PREDICATE_FALSE = t -> false;
    static final IOPredicate<Object> IO_PREDICATE_TRUE = t -> true;
    static final IOTriConsumer IO_TRI_CONSUMER = (t, u, v) -> {};

    private Constants() {
    }
}

