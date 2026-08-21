/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.function.FailableSupplier;

public interface ConcurrentInitializer<T>
extends FailableSupplier<T, ConcurrentException> {
}

