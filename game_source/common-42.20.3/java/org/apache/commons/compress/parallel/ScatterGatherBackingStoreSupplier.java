/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.parallel;

import java.io.IOException;
import org.apache.commons.compress.parallel.ScatterGatherBackingStore;

public interface ScatterGatherBackingStoreSupplier {
    public ScatterGatherBackingStore get() throws IOException;
}

