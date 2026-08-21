/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.compress.archivers.zip.DefaultBackingStoreSupplier;
import org.apache.commons.compress.archivers.zip.ScatterStatistics;
import org.apache.commons.compress.archivers.zip.ScatterZipOutputStream;
import org.apache.commons.compress.archivers.zip.StreamCompressor;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequestSupplier;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.compress.parallel.ScatterGatherBackingStore;
import org.apache.commons.compress.parallel.ScatterGatherBackingStoreSupplier;

public class ParallelScatterZipCreator {
    private final Deque<ScatterZipOutputStream> streams = new ConcurrentLinkedDeque<ScatterZipOutputStream>();
    private final ExecutorService executorService;
    private final ScatterGatherBackingStoreSupplier backingStoreSupplier;
    private final Deque<Future<? extends ScatterZipOutputStream>> futures = new ConcurrentLinkedDeque<Future<? extends ScatterZipOutputStream>>();
    private final long startedAt = System.currentTimeMillis();
    private long compressionDoneAt;
    private long scatterDoneAt;
    private final int compressionLevel;
    private final ThreadLocal<ScatterZipOutputStream> tlScatterStreams = new ThreadLocal<ScatterZipOutputStream>(){

        @Override
        protected ScatterZipOutputStream initialValue() {
            try {
                ScatterZipOutputStream scatterStream = ParallelScatterZipCreator.this.createDeferred(ParallelScatterZipCreator.this.backingStoreSupplier);
                ParallelScatterZipCreator.this.streams.add(scatterStream);
                return scatterStream;
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    };

    public ParallelScatterZipCreator() {
        this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    public ParallelScatterZipCreator(ExecutorService executorService) {
        this(executorService, new DefaultBackingStoreSupplier(null));
    }

    public ParallelScatterZipCreator(ExecutorService executorService, ScatterGatherBackingStoreSupplier backingStoreSupplier) {
        this(executorService, backingStoreSupplier, -1);
    }

    public ParallelScatterZipCreator(ExecutorService executorService, ScatterGatherBackingStoreSupplier backingStoreSupplier, int compressionLevel) throws IllegalArgumentException {
        if ((compressionLevel < 0 || compressionLevel > 9) && compressionLevel != -1) {
            throw new IllegalArgumentException("Compression level is expected between -1~9");
        }
        this.backingStoreSupplier = backingStoreSupplier;
        this.executorService = executorService;
        this.compressionLevel = compressionLevel;
    }

    public void addArchiveEntry(ZipArchiveEntry zipArchiveEntry, InputStreamSupplier source2) {
        this.submitStreamAwareCallable(this.createCallable(zipArchiveEntry, source2));
    }

    public void addArchiveEntry(ZipArchiveEntryRequestSupplier zipArchiveEntryRequestSupplier) {
        this.submitStreamAwareCallable(this.createCallable(zipArchiveEntryRequestSupplier));
    }

    private void closeAll() {
        for (ScatterZipOutputStream scatterStream : this.streams) {
            try {
                scatterStream.close();
            }
            catch (IOException iOException) {}
        }
    }

    public final Callable<ScatterZipOutputStream> createCallable(ZipArchiveEntry zipArchiveEntry, InputStreamSupplier source2) {
        int method = zipArchiveEntry.getMethod();
        if (method == -1) {
            throw new IllegalArgumentException("Method must be set on zipArchiveEntry: " + zipArchiveEntry);
        }
        ZipArchiveEntryRequest zipArchiveEntryRequest = ZipArchiveEntryRequest.createZipArchiveEntryRequest(zipArchiveEntry, source2);
        return () -> {
            ScatterZipOutputStream scatterStream = this.tlScatterStreams.get();
            scatterStream.addArchiveEntry(zipArchiveEntryRequest);
            return scatterStream;
        };
    }

    public final Callable<ScatterZipOutputStream> createCallable(ZipArchiveEntryRequestSupplier zipArchiveEntryRequestSupplier) {
        return () -> {
            ScatterZipOutputStream scatterStream = this.tlScatterStreams.get();
            scatterStream.addArchiveEntry(zipArchiveEntryRequestSupplier.get());
            return scatterStream;
        };
    }

    private ScatterZipOutputStream createDeferred(ScatterGatherBackingStoreSupplier scatterGatherBackingStoreSupplier) throws IOException {
        ScatterGatherBackingStore bs = scatterGatherBackingStoreSupplier.get();
        StreamCompressor sc = StreamCompressor.create(this.compressionLevel, bs);
        return new ScatterZipOutputStream(bs, sc);
    }

    public ScatterStatistics getStatisticsMessage() {
        return new ScatterStatistics(this.compressionDoneAt - this.startedAt, this.scatterDoneAt - this.compressionDoneAt);
    }

    public final void submit(Callable<? extends Object> callable) {
        this.submitStreamAwareCallable(() -> {
            callable.call();
            return this.tlScatterStreams.get();
        });
    }

    public final void submitStreamAwareCallable(Callable<? extends ScatterZipOutputStream> callable) {
        this.futures.add(this.executorService.submit(callable));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeTo(ZipArchiveOutputStream targetStream) throws IOException, InterruptedException, ExecutionException {
        try {
            try {
                for (Future<? extends ScatterZipOutputStream> future : this.futures) {
                    future.get();
                }
            }
            finally {
                this.executorService.shutdown();
            }
            this.executorService.awaitTermination(60000L, TimeUnit.SECONDS);
            this.compressionDoneAt = System.currentTimeMillis();
            for (Future<? extends ScatterZipOutputStream> future : this.futures) {
                ScatterZipOutputStream scatterStream = future.get();
                scatterStream.zipEntryWriter().writeNextZipEntry(targetStream);
            }
            for (ScatterZipOutputStream scatterStream : this.streams) {
                scatterStream.close();
            }
            this.scatterDoneAt = System.currentTimeMillis();
        }
        finally {
            this.closeAll();
        }
    }
}

