/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.pack200.AbstractStreamBridge;
import org.apache.commons.compress.compressors.pack200.Pack200Strategy;
import org.apache.commons.compress.java.util.jar.Pack200;

public class Pack200CompressorOutputStream
extends CompressorOutputStream<OutputStream> {
    private boolean finished;
    private final AbstractStreamBridge abstractStreamBridge;
    private final Map<String, String> properties;

    public Pack200CompressorOutputStream(OutputStream out) throws IOException {
        this(out, Pack200Strategy.IN_MEMORY);
    }

    public Pack200CompressorOutputStream(OutputStream out, Map<String, String> props) throws IOException {
        this(out, Pack200Strategy.IN_MEMORY, props);
    }

    public Pack200CompressorOutputStream(OutputStream out, Pack200Strategy mode) throws IOException {
        this(out, mode, null);
    }

    public Pack200CompressorOutputStream(OutputStream out, Pack200Strategy mode, Map<String, String> props) throws IOException {
        super(out);
        this.abstractStreamBridge = mode.newStreamBridge();
        this.properties = props;
    }

    @Override
    public void close() throws IOException {
        try {
            this.finish();
        }
        finally {
            try {
                this.abstractStreamBridge.stop();
            }
            finally {
                this.out.close();
            }
        }
    }

    public void finish() throws IOException {
        if (!this.finished) {
            this.finished = true;
            Pack200.Packer p = Pack200.newPacker();
            if (this.properties != null) {
                p.properties().putAll(this.properties);
            }
            try (JarInputStream ji = new JarInputStream(this.abstractStreamBridge.getInputStream());){
                p.pack(ji, this.out);
            }
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.abstractStreamBridge.write(b);
    }

    @Override
    public void write(byte[] b, int from, int length) throws IOException {
        this.abstractStreamBridge.write(b, from, length);
    }

    @Override
    public void write(int b) throws IOException {
        this.abstractStreamBridge.write(b);
    }
}

