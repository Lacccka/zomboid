/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.j2se;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

public final class ImageReader {
    private static final String BASE64TOKEN = "base64,";

    private ImageReader() {
    }

    public static BufferedImage readImage(URI uri) throws IOException {
        BufferedImage result;
        if ("data".equals(uri.getScheme())) {
            return ImageReader.readDataURIImage(uri);
        }
        try {
            result = ImageIO.read(uri.toURL());
        }
        catch (IllegalArgumentException iae) {
            throw new IOException("Resource not found: " + uri, iae);
        }
        if (result == null) {
            throw new IOException("Could not load " + uri);
        }
        return result;
    }

    public static BufferedImage readDataURIImage(URI uri) throws IOException {
        String uriString = uri.getSchemeSpecificPart();
        if (!uriString.startsWith("image/")) {
            throw new IOException("Unsupported data URI MIME type");
        }
        int base64Start = uriString.indexOf(BASE64TOKEN);
        if (base64Start < 0) {
            throw new IOException("Unsupported data URI encoding");
        }
        String base64DataEncoded = uriString.substring(base64Start + BASE64TOKEN.length());
        String base64Data = URLDecoder.decode(base64DataEncoded, "UTF-8");
        byte[] imageBytes = DatatypeConverter.parseBase64Binary(base64Data);
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }
}

