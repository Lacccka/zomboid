/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.RandomAccessFiles;
import org.apache.commons.io.filefilter.AbstractFileFilter;

public class MagicNumberFileFilter
extends AbstractFileFilter
implements Serializable {
    private static final long serialVersionUID = -547733176983104172L;
    private final byte[] magicNumbers;
    private final long byteOffset;

    public MagicNumberFileFilter(byte[] magicNumber) {
        this(magicNumber, 0L);
    }

    public MagicNumberFileFilter(byte[] magicNumbers, long offset) {
        Objects.requireNonNull(magicNumbers, "magicNumbers");
        if (magicNumbers.length == 0) {
            throw new IllegalArgumentException("The magic number must contain at least one byte");
        }
        if (offset < 0L) {
            throw new IllegalArgumentException("The offset cannot be negative");
        }
        this.magicNumbers = (byte[])magicNumbers.clone();
        this.byteOffset = offset;
    }

    public MagicNumberFileFilter(String magicNumber) {
        this(magicNumber, 0L);
    }

    public MagicNumberFileFilter(String magicNumber, long offset) {
        Objects.requireNonNull(magicNumber, "magicNumber");
        if (magicNumber.isEmpty()) {
            throw new IllegalArgumentException("The magic number must contain at least one byte");
        }
        if (offset < 0L) {
            throw new IllegalArgumentException("The offset cannot be negative");
        }
        this.magicNumbers = magicNumber.getBytes(Charset.defaultCharset());
        this.byteOffset = offset;
    }

    @Override
    public boolean accept(File file) {
        if (file != null && file.isFile() && file.canRead()) {
            boolean bl;
            block9: {
                RandomAccessFile randomAccessFile = RandomAccessFileMode.READ_ONLY.create(file);
                try {
                    bl = Arrays.equals(this.magicNumbers, RandomAccessFiles.read(randomAccessFile, this.byteOffset, this.magicNumbers.length));
                    if (randomAccessFile == null) break block9;
                }
                catch (Throwable throwable) {
                    try {
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
                randomAccessFile.close();
            }
            return bl;
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
        if (file == null) return FileVisitResult.TERMINATE;
        if (!Files.isRegularFile(file, new LinkOption[0])) return FileVisitResult.TERMINATE;
        if (!Files.isReadable(file)) return FileVisitResult.TERMINATE;
        try (FileChannel fileChannel = FileChannel.open(file, new OpenOption[0]);){
            ByteBuffer byteBuffer = ByteBuffer.allocate(this.magicNumbers.length);
            fileChannel.position(this.byteOffset);
            int read = fileChannel.read(byteBuffer);
            if (read != this.magicNumbers.length) {
                FileVisitResult fileVisitResult2 = FileVisitResult.TERMINATE;
                return fileVisitResult2;
            }
            FileVisitResult fileVisitResult = this.toFileVisitResult(Arrays.equals(this.magicNumbers, byteBuffer.array()));
            return fileVisitResult;
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return FileVisitResult.TERMINATE;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("(");
        builder.append(new String(this.magicNumbers, Charset.defaultCharset()));
        builder.append(",");
        builder.append(this.byteOffset);
        builder.append(")");
        return builder.toString();
    }
}

