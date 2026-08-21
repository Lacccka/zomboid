/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.compress.parallel.FileBasedScatterGatherBackingStore;
import org.apache.commons.compress.parallel.ScatterGatherBackingStore;
import org.apache.commons.compress.parallel.ScatterGatherBackingStoreSupplier;

public class DefaultBackingStoreSupplier
implements ScatterGatherBackingStoreSupplier {
    private static final String PREFIX = "parallelscatter";
    private final AtomicInteger storeNum = new AtomicInteger();
    private final Path dir;

    public DefaultBackingStoreSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    public ScatterGatherBackingStore get() throws IOException {
        String suffix = "n" + this.storeNum.incrementAndGet();
        Path tempFile = this.dir == null ? Files.createTempFile(PREFIX, suffix, new FileAttribute[0]) : Files.createTempFile(this.dir, PREFIX, suffix, new FileAttribute[0]);
        return new FileBasedScatterGatherBackingStore(tempFile);
    }
}

