/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import pl.mjaron.tinyloki.ILogEncoder;

public class GzipLogEncoder
implements ILogEncoder {
    @Override
    public String contentEncoding() {
        return "gzip";
    }

    @Override
    public byte[] encode(byte[] what) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(what.length);
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos);){
            gzipOutputStream.write(what);
        }
        return bos.toByteArray();
    }
}

