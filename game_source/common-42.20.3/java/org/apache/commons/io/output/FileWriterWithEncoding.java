/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Objects;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.output.ProxyWriter;

public class FileWriterWithEncoding
extends ProxyWriter {
    public static Builder builder() {
        return new Builder();
    }

    private static OutputStreamWriter initWriter(File file, Object encoding, boolean append) throws IOException {
        Objects.requireNonNull(file, "file");
        OutputStream outputStream2 = null;
        boolean fileExistedAlready = file.exists();
        try {
            outputStream2 = FileUtils.newOutputStream(file, append);
            if (encoding == null || encoding instanceof Charset) {
                return new OutputStreamWriter(outputStream2, Charsets.toCharset((Charset)encoding));
            }
            if (encoding instanceof CharsetEncoder) {
                return new OutputStreamWriter(outputStream2, (CharsetEncoder)encoding);
            }
            return new OutputStreamWriter(outputStream2, (String)encoding);
        }
        catch (IOException | RuntimeException ex) {
            try {
                IOUtils.close((Closeable)outputStream2);
            }
            catch (IOException e) {
                ex.addSuppressed(e);
            }
            if (!fileExistedAlready) {
                FileUtils.deleteQuietly(file);
            }
            throw ex;
        }
    }

    @Deprecated
    public FileWriterWithEncoding(File file, Charset charset) throws IOException {
        this(file, charset, false);
    }

    @Deprecated
    public FileWriterWithEncoding(File file, Charset encoding, boolean append) throws IOException {
        this(FileWriterWithEncoding.initWriter(file, encoding, append));
    }

    @Deprecated
    public FileWriterWithEncoding(File file, CharsetEncoder charsetEncoder) throws IOException {
        this(file, charsetEncoder, false);
    }

    @Deprecated
    public FileWriterWithEncoding(File file, CharsetEncoder charsetEncoder, boolean append) throws IOException {
        this(FileWriterWithEncoding.initWriter(file, charsetEncoder, append));
    }

    @Deprecated
    public FileWriterWithEncoding(File file, String charsetName) throws IOException {
        this(file, charsetName, false);
    }

    @Deprecated
    public FileWriterWithEncoding(File file, String charsetName, boolean append) throws IOException {
        this(FileWriterWithEncoding.initWriter(file, charsetName, append));
    }

    private FileWriterWithEncoding(OutputStreamWriter outputStreamWriter) {
        super(outputStreamWriter);
    }

    @Deprecated
    public FileWriterWithEncoding(String fileName, Charset charset) throws IOException {
        this(new File(fileName), charset, false);
    }

    @Deprecated
    public FileWriterWithEncoding(String fileName, Charset charset, boolean append) throws IOException {
        this(new File(fileName), charset, append);
    }

    @Deprecated
    public FileWriterWithEncoding(String fileName, CharsetEncoder encoding) throws IOException {
        this(new File(fileName), encoding, false);
    }

    @Deprecated
    public FileWriterWithEncoding(String fileName, CharsetEncoder charsetEncoder, boolean append) throws IOException {
        this(new File(fileName), charsetEncoder, append);
    }

    @Deprecated
    public FileWriterWithEncoding(String fileName, String charsetName) throws IOException {
        this(new File(fileName), charsetName, false);
    }

    @Deprecated
    public FileWriterWithEncoding(String fileName, String charsetName, boolean append) throws IOException {
        this(new File(fileName), charsetName, append);
    }

    public static class Builder
    extends AbstractStreamBuilder<FileWriterWithEncoding, Builder> {
        private boolean append;
        private CharsetEncoder charsetEncoder = super.getCharset().newEncoder();

        @Override
        public FileWriterWithEncoding get() throws IOException {
            if (this.charsetEncoder != null && this.getCharset() != null && !this.charsetEncoder.charset().equals(this.getCharset())) {
                throw new IllegalStateException(String.format("Mismatched Charset(%s) and CharsetEncoder(%s)", this.getCharset(), this.charsetEncoder.charset()));
            }
            Object encoder = this.charsetEncoder != null ? this.charsetEncoder : this.getCharset();
            return new FileWriterWithEncoding(FileWriterWithEncoding.initWriter(this.checkOrigin().getFile(), encoder, this.append));
        }

        public Builder setAppend(boolean append) {
            this.append = append;
            return this;
        }

        public Builder setCharsetEncoder(CharsetEncoder charsetEncoder) {
            this.charsetEncoder = charsetEncoder;
            return this;
        }
    }
}

