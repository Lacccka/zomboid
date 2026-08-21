/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.pack200;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.jar.JarOutputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.pack200.AbstractStreamBridge;
import org.apache.commons.compress.compressors.pack200.Pack200Strategy;
import org.apache.commons.compress.java.util.jar.Pack200;
import org.apache.commons.io.IOUtils;

public class Pack200CompressorInputStream
extends CompressorInputStream {
    private static final byte[] CAFE_DOOD = new byte[]{-54, -2, -48, 13};
    private static final int SIG_LENGTH = CAFE_DOOD.length;
    private final InputStream originalInputStream;
    private final AbstractStreamBridge abstractStreamBridge;

    public static boolean matches(byte[] signature, int length) {
        if (length < SIG_LENGTH) {
            return false;
        }
        for (int i = 0; i < SIG_LENGTH; ++i) {
            if (signature[i] == CAFE_DOOD[i]) continue;
            return false;
        }
        return true;
    }

    public Pack200CompressorInputStream(File file) throws IOException {
        this(file, Pack200Strategy.IN_MEMORY);
    }

    public Pack200CompressorInputStream(File file, Map<String, String> properties) throws IOException {
        this(file, Pack200Strategy.IN_MEMORY, properties);
    }

    public Pack200CompressorInputStream(File file, Pack200Strategy mode) throws IOException {
        this(null, file, mode, null);
    }

    public Pack200CompressorInputStream(File file, Pack200Strategy mode, Map<String, String> properties) throws IOException {
        this(null, file, mode, properties);
    }

    public Pack200CompressorInputStream(InputStream inputStream2) throws IOException {
        this(inputStream2, Pack200Strategy.IN_MEMORY);
    }

    private Pack200CompressorInputStream(InputStream inputStream2, File file, Pack200Strategy mode, Map<String, String> properties) throws IOException {
        this.originalInputStream = inputStream2;
        this.abstractStreamBridge = mode.newStreamBridge();
        try (JarOutputStream jarOut = new JarOutputStream(this.abstractStreamBridge);){
            Pack200.Unpacker unpacker = Pack200.newUnpacker();
            if (properties != null) {
                unpacker.properties().putAll(properties);
            }
            if (file == null) {
                unpacker.unpack(inputStream2, jarOut);
            } else {
                unpacker.unpack(file, jarOut);
            }
        }
    }

    public Pack200CompressorInputStream(InputStream inputStream2, Map<String, String> properties) throws IOException {
        this(inputStream2, Pack200Strategy.IN_MEMORY, properties);
    }

    public Pack200CompressorInputStream(InputStream inputStream2, Pack200Strategy mode) throws IOException {
        this(inputStream2, null, mode, null);
    }

    public Pack200CompressorInputStream(InputStream inputStream2, Pack200Strategy mode, Map<String, String> properties) throws IOException {
        this(inputStream2, null, mode, properties);
    }

    @Override
    public int available() throws IOException {
        return this.getInputStream().available();
    }

    @Override
    public void close() throws IOException {
        try {
            this.abstractStreamBridge.stop();
        }
        finally {
            if (this.originalInputStream != null) {
                this.originalInputStream.close();
            }
        }
    }

    private InputStream getInputStream() throws IOException {
        return this.abstractStreamBridge.getInputStream();
    }

    @Override
    public synchronized void mark(int limit) {
        try {
            this.getInputStream().mark(limit);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public boolean markSupported() {
        try {
            return this.getInputStream().markSupported();
        }
        catch (IOException ex) {
            return false;
        }
    }

    @Override
    public int read() throws IOException {
        return this.getInputStream().read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.getInputStream().read(b);
    }

    @Override
    public int read(byte[] b, int off, int count) throws IOException {
        return this.getInputStream().read(b, off, count);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.getInputStream().reset();
    }

    @Override
    public long skip(long count) throws IOException {
        return IOUtils.skip(this.getInputStream(), count);
    }
}

