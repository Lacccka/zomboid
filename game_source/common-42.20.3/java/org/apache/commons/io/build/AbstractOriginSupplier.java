/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.build;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractSupplier;

public abstract class AbstractOriginSupplier<T, B extends AbstractOriginSupplier<T, B>>
extends AbstractSupplier<T, B> {
    private AbstractOrigin<?, ?> origin;

    protected static AbstractOrigin.ByteArrayOrigin newByteArrayOrigin(byte[] origin) {
        return new AbstractOrigin.ByteArrayOrigin(origin);
    }

    protected static AbstractOrigin.CharSequenceOrigin newCharSequenceOrigin(CharSequence origin) {
        return new AbstractOrigin.CharSequenceOrigin(origin);
    }

    protected static AbstractOrigin.FileOrigin newFileOrigin(File origin) {
        return new AbstractOrigin.FileOrigin(origin);
    }

    protected static AbstractOrigin.FileOrigin newFileOrigin(String origin) {
        return new AbstractOrigin.FileOrigin(new File(origin));
    }

    protected static AbstractOrigin.InputStreamOrigin newInputStreamOrigin(InputStream origin) {
        return new AbstractOrigin.InputStreamOrigin(origin);
    }

    protected static AbstractOrigin.OutputStreamOrigin newOutputStreamOrigin(OutputStream origin) {
        return new AbstractOrigin.OutputStreamOrigin(origin);
    }

    protected static AbstractOrigin.PathOrigin newPathOrigin(Path origin) {
        return new AbstractOrigin.PathOrigin(origin);
    }

    protected static AbstractOrigin.PathOrigin newPathOrigin(String origin) {
        return new AbstractOrigin.PathOrigin(Paths.get(origin, new String[0]));
    }

    protected static AbstractOrigin.ReaderOrigin newReaderOrigin(Reader origin) {
        return new AbstractOrigin.ReaderOrigin(origin);
    }

    protected static AbstractOrigin.URIOrigin newURIOrigin(URI origin) {
        return new AbstractOrigin.URIOrigin(origin);
    }

    protected static AbstractOrigin.WriterOrigin newWriterOrigin(Writer origin) {
        return new AbstractOrigin.WriterOrigin(origin);
    }

    protected AbstractOrigin<?, ?> checkOrigin() {
        if (this.origin == null) {
            throw new IllegalStateException("origin == null");
        }
        return this.origin;
    }

    protected AbstractOrigin<?, ?> getOrigin() {
        return this.origin;
    }

    protected boolean hasOrigin() {
        return this.origin != null;
    }

    public B setByteArray(byte[] origin) {
        return this.setOrigin(AbstractOriginSupplier.newByteArrayOrigin(origin));
    }

    public B setCharSequence(CharSequence origin) {
        return this.setOrigin(AbstractOriginSupplier.newCharSequenceOrigin(origin));
    }

    public B setFile(File origin) {
        return this.setOrigin(AbstractOriginSupplier.newFileOrigin(origin));
    }

    public B setFile(String origin) {
        return this.setOrigin(AbstractOriginSupplier.newFileOrigin(origin));
    }

    public B setInputStream(InputStream origin) {
        return this.setOrigin(AbstractOriginSupplier.newInputStreamOrigin(origin));
    }

    protected B setOrigin(AbstractOrigin<?, ?> origin) {
        this.origin = origin;
        return (B)((AbstractOriginSupplier)this.asThis());
    }

    public B setOutputStream(OutputStream origin) {
        return this.setOrigin(AbstractOriginSupplier.newOutputStreamOrigin(origin));
    }

    public B setPath(Path origin) {
        return this.setOrigin(AbstractOriginSupplier.newPathOrigin(origin));
    }

    public B setPath(String origin) {
        return this.setOrigin(AbstractOriginSupplier.newPathOrigin(origin));
    }

    public B setReader(Reader origin) {
        return this.setOrigin(AbstractOriginSupplier.newReaderOrigin(origin));
    }

    public B setURI(URI origin) {
        return this.setOrigin(AbstractOriginSupplier.newURIOrigin(origin));
    }

    public B setWriter(Writer origin) {
        return this.setOrigin(AbstractOriginSupplier.newWriterOrigin(origin));
    }
}

