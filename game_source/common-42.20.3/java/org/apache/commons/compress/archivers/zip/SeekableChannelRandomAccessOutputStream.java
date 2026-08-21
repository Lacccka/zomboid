/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.apache.commons.compress.archivers.zip.RandomAccessOutputStream;
import org.apache.commons.compress.archivers.zip.ZipIoUtil;

class SeekableChannelRandomAccessOutputStream
extends RandomAccessOutputStream {
    private final SeekableByteChannel channel;

    SeekableChannelRandomAccessOutputStream(SeekableByteChannel channel) {
        this.channel = channel;
    }

    @Override
    public synchronized void close() throws IOException {
        this.channel.close();
    }

    @Override
    public synchronized long position() throws IOException {
        return this.channel.position();
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        ZipIoUtil.writeFully(this.channel, ByteBuffer.wrap(b, off, len));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void writeFully(byte[] b, int off, int len, long position) throws IOException {
        long saved = this.channel.position();
        try {
            this.channel.position(position);
            ZipIoUtil.writeFully(this.channel, ByteBuffer.wrap(b, off, len));
        }
        finally {
            this.channel.position(saved);
        }
    }
}

