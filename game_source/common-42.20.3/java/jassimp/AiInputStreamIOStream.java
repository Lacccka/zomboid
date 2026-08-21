/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiIOStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;

public class AiInputStreamIOStream
implements AiIOStream {
    private final ByteArrayOutputStream os = new ByteArrayOutputStream();

    public AiInputStreamIOStream(URI uRI) throws IOException {
        this(uRI.toURL());
    }

    public AiInputStreamIOStream(URL uRL) throws IOException {
        this(uRL.openStream());
    }

    public AiInputStreamIOStream(InputStream inputStream2) throws IOException {
        int n;
        byte[] byArray = new byte[1024];
        while ((n = inputStream2.read(byArray, 0, byArray.length)) != -1) {
            this.os.write(byArray, 0, n);
        }
        this.os.flush();
        inputStream2.close();
    }

    @Override
    public int getFileSize() {
        return this.os.size();
    }

    @Override
    public boolean read(ByteBuffer byteBuffer) {
        ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(byteBuffer);
        try {
            this.os.writeTo(byteBufferOutputStream);
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
            return false;
        }
        return true;
    }

    private static class ByteBufferOutputStream
    extends OutputStream {
        private final ByteBuffer buffer;

        public ByteBufferOutputStream(ByteBuffer byteBuffer) {
            this.buffer = byteBuffer;
        }

        @Override
        public void write(int n) throws IOException {
            this.buffer.put((byte)n);
        }

        @Override
        public void write(byte[] byArray, int n, int n2) throws IOException {
            this.buffer.put(byArray, n, n2);
        }
    }
}

