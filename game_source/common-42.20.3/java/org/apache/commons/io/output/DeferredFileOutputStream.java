/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.ThresholdingOutputStream;

public class DeferredFileOutputStream
extends ThresholdingOutputStream {
    private ByteArrayOutputStream memoryOutputStream;
    private OutputStream currentOutputStream;
    private Path outputPath;
    private final String prefix;
    private final String suffix;
    private final Path directory;
    private boolean closed;

    public static Builder builder() {
        return new Builder();
    }

    private static int checkBufferSize(int initialBufferSize) {
        if (initialBufferSize < 0) {
            throw new IllegalArgumentException("Initial buffer size must be at least 0.");
        }
        return initialBufferSize;
    }

    private static Path toPath(File file, Supplier<Path> defaultPathSupplier) {
        return file != null ? file.toPath() : (defaultPathSupplier == null ? null : defaultPathSupplier.get());
    }

    private static Path toPath(Path file, Supplier<Path> defaultPathSupplier) {
        return file != null ? file : (defaultPathSupplier == null ? null : defaultPathSupplier.get());
    }

    @Deprecated
    public DeferredFileOutputStream(int threshold, File outputFile) {
        this(threshold, outputFile, null, null, null, 1024);
    }

    private DeferredFileOutputStream(int threshold, File outputFile, String prefix, String suffix, File directory, int initialBufferSize) {
        super(threshold);
        this.outputPath = DeferredFileOutputStream.toPath(outputFile, null);
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = DeferredFileOutputStream.toPath(directory, PathUtils::getTempDirectory);
        this.memoryOutputStream = new ByteArrayOutputStream(DeferredFileOutputStream.checkBufferSize(initialBufferSize));
        this.currentOutputStream = this.memoryOutputStream;
    }

    @Deprecated
    public DeferredFileOutputStream(int threshold, int initialBufferSize, File outputFile) {
        this(threshold, outputFile, null, null, null, initialBufferSize);
    }

    @Deprecated
    public DeferredFileOutputStream(int threshold, int initialBufferSize, String prefix, String suffix, File directory) {
        this(threshold, null, Objects.requireNonNull(prefix, "prefix"), suffix, directory, initialBufferSize);
    }

    private DeferredFileOutputStream(int threshold, Path outputFile, String prefix, String suffix, Path directory, int initialBufferSize) {
        super(threshold);
        this.outputPath = DeferredFileOutputStream.toPath(outputFile, null);
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = DeferredFileOutputStream.toPath(directory, PathUtils::getTempDirectory);
        this.memoryOutputStream = new ByteArrayOutputStream(DeferredFileOutputStream.checkBufferSize(initialBufferSize));
        this.currentOutputStream = this.memoryOutputStream;
    }

    @Deprecated
    public DeferredFileOutputStream(int threshold, String prefix, String suffix, File directory) {
        this(threshold, null, Objects.requireNonNull(prefix, "prefix"), suffix, directory, 1024);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.closed = true;
    }

    public byte[] getData() {
        return this.memoryOutputStream != null ? this.memoryOutputStream.toByteArray() : null;
    }

    public File getFile() {
        return this.outputPath != null ? this.outputPath.toFile() : null;
    }

    public Path getPath() {
        return this.outputPath;
    }

    @Override
    protected OutputStream getStream() throws IOException {
        return this.currentOutputStream;
    }

    public boolean isInMemory() {
        return !this.isThresholdExceeded();
    }

    @Override
    protected void thresholdReached() throws IOException {
        if (this.prefix != null) {
            this.outputPath = Files.createTempFile(this.directory, this.prefix, this.suffix, new FileAttribute[0]);
        }
        PathUtils.createParentDirectories(this.outputPath, null, PathUtils.EMPTY_FILE_ATTRIBUTE_ARRAY);
        OutputStream fos = Files.newOutputStream(this.outputPath, new OpenOption[0]);
        try {
            this.memoryOutputStream.writeTo(fos);
        }
        catch (IOException e) {
            fos.close();
            throw e;
        }
        this.currentOutputStream = fos;
        this.memoryOutputStream = null;
    }

    public InputStream toInputStream() throws IOException {
        if (!this.closed) {
            throw new IOException("Stream not closed");
        }
        if (this.isInMemory()) {
            return this.memoryOutputStream.toInputStream();
        }
        return Files.newInputStream(this.outputPath, new OpenOption[0]);
    }

    public void writeTo(OutputStream outputStream2) throws IOException {
        if (!this.closed) {
            throw new IOException("Stream not closed");
        }
        if (this.isInMemory()) {
            this.memoryOutputStream.writeTo(outputStream2);
        } else {
            Files.copy(this.outputPath, outputStream2);
        }
    }

    public static class Builder
    extends AbstractStreamBuilder<DeferredFileOutputStream, Builder> {
        private int threshold;
        private Path outputFile;
        private String prefix;
        private String suffix;
        private Path directory;

        public Builder() {
            this.setBufferSizeDefault(1024);
            this.setBufferSize(1024);
        }

        @Override
        public DeferredFileOutputStream get() {
            return new DeferredFileOutputStream(this.threshold, this.outputFile, this.prefix, this.suffix, this.directory, this.getBufferSize());
        }

        public Builder setDirectory(File directory) {
            this.directory = DeferredFileOutputStream.toPath(directory, (Supplier<Path>)null);
            return this;
        }

        public Builder setDirectory(Path directory) {
            this.directory = DeferredFileOutputStream.toPath(directory, (Supplier<Path>)null);
            return this;
        }

        public Builder setOutputFile(File outputFile) {
            this.outputFile = DeferredFileOutputStream.toPath(outputFile, (Supplier<Path>)null);
            return this;
        }

        public Builder setOutputFile(Path outputFile) {
            this.outputFile = DeferredFileOutputStream.toPath(outputFile, (Supplier<Path>)null);
            return this;
        }

        public Builder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder setThreshold(int threshold) {
            this.threshold = threshold;
            return this;
        }
    }
}

