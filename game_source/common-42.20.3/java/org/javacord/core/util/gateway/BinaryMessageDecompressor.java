/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.apache.logging.log4j.Logger;
import org.javacord.core.util.logging.LoggerUtil;

public class BinaryMessageDecompressor {
    private static final Logger logger = LoggerUtil.getLogger(BinaryMessageDecompressor.class);

    private BinaryMessageDecompressor() {
        throw new UnsupportedOperationException("You cannot create an instance of this class");
    }

    public static String decompress(byte[] data) throws DataFormatException {
        Inflater decompressor = new Inflater();
        decompressor.setInput(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            int count = decompressor.inflate(buf);
            bos.write(buf, 0, count);
        }
        try {
            bos.close();
        }
        catch (IOException count) {
            // empty catch block
        }
        byte[] decompressedData = bos.toByteArray();
        return new String(decompressedData, StandardCharsets.UTF_8);
    }
}

