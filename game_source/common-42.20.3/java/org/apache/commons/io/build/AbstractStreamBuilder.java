/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.IntUnaryOperator;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.build.AbstractOriginSupplier;
import org.apache.commons.io.file.PathUtils;

public abstract class AbstractStreamBuilder<T, B extends AbstractStreamBuilder<T, B>>
extends AbstractOriginSupplier<T, B> {
    private static final int DEFAULT_MAX_VALUE = Integer.MAX_VALUE;
    private static final OpenOption[] DEFAULT_OPEN_OPTIONS = PathUtils.EMPTY_OPEN_OPTION_ARRAY;
    private int bufferSize = 8192;
    private int bufferSizeDefault = 8192;
    private int bufferSizeMax = Integer.MAX_VALUE;
    private Charset charset = Charset.defaultCharset();
    private Charset charsetDefault = Charset.defaultCharset();
    private OpenOption[] openOptions = DEFAULT_OPEN_OPTIONS;
    private final IntUnaryOperator defaultSizeChecker;
    private IntUnaryOperator bufferSizeChecker = this.defaultSizeChecker = size -> size > this.bufferSizeMax ? this.throwIae(size, this.bufferSizeMax) : size;

    private int checkBufferSize(int size) {
        return this.bufferSizeChecker.applyAsInt(size);
    }

    protected int getBufferSize() {
        return this.bufferSize;
    }

    protected int getBufferSizeDefault() {
        return this.bufferSizeDefault;
    }

    protected CharSequence getCharSequence() throws IOException {
        return this.checkOrigin().getCharSequence(this.getCharset());
    }

    public Charset getCharset() {
        return this.charset;
    }

    protected Charset getCharsetDefault() {
        return this.charsetDefault;
    }

    protected InputStream getInputStream() throws IOException {
        return this.checkOrigin().getInputStream(this.getOpenOptions());
    }

    protected OpenOption[] getOpenOptions() {
        return this.openOptions;
    }

    protected OutputStream getOutputStream() throws IOException {
        return this.checkOrigin().getOutputStream(this.getOpenOptions());
    }

    protected Path getPath() {
        return this.checkOrigin().getPath();
    }

    protected Reader getReader() throws IOException {
        return this.checkOrigin().getReader(this.getCharset());
    }

    protected Writer getWriter() throws IOException {
        return this.checkOrigin().getWriter(this.getCharset(), this.getOpenOptions());
    }

    public B setBufferSize(int bufferSize) {
        this.bufferSize = this.checkBufferSize(bufferSize > 0 ? bufferSize : this.bufferSizeDefault);
        return (B)((AbstractStreamBuilder)this.asThis());
    }

    public B setBufferSize(Integer bufferSize) {
        this.setBufferSize(bufferSize != null ? bufferSize : this.bufferSizeDefault);
        return (B)((AbstractStreamBuilder)this.asThis());
    }

    public B setBufferSizeChecker(IntUnaryOperator bufferSizeChecker) {
        this.bufferSizeChecker = bufferSizeChecker != null ? bufferSizeChecker : this.defaultSizeChecker;
        return (B)((AbstractStreamBuilder)this.asThis());
    }

    protected B setBufferSizeDefault(int bufferSizeDefault) {
        this.bufferSizeDefault = bufferSizeDefault;
        return (B)((AbstractStreamBuilder)this.asThis());
    }

    public B setBufferSizeMax(int bufferSizeMax) {
        this.bufferSizeMax = bufferSizeMax > 0 ? bufferSizeMax : Integer.MAX_VALUE;
        return (B)((AbstractStreamBuilder)this.asThis());
    }

    public B setCharset(Charset charset) {
        this.charset = Charsets.toCharset(charset, this.charsetDefault);
        return (B)((AbstractStreamBuilder)this.asThis());
    }

    public B setCharset(String charset) {
        return this.setCharset(Charsets.toCharset(charset, this.charsetDefault));
    }

    protected B setCharsetDefault(Charset defaultCharset) {
        this.charsetDefault = defaultCharset;
        return (B)((AbstractStreamBuilder)this.asThis());
    }

    public B setOpenOptions(OpenOption ... openOptions) {
        this.openOptions = openOptions != null ? openOptions : DEFAULT_OPEN_OPTIONS;
        return (B)((AbstractStreamBuilder)this.asThis());
    }

    private int throwIae(int size, int max) {
        throw new IllegalArgumentException(String.format("Request %,d exceeds maximum %,d", size, max));
    }
}

