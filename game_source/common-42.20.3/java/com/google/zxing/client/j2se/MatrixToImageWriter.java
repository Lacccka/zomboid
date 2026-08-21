/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.j2se;

import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class MatrixToImageWriter {
    private static final MatrixToImageConfig DEFAULT_CONFIG = new MatrixToImageConfig();

    private MatrixToImageWriter() {
    }

    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        return MatrixToImageWriter.toBufferedImage(matrix, DEFAULT_CONFIG);
    }

    public static BufferedImage toBufferedImage(BitMatrix matrix, MatrixToImageConfig config) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, config.getBufferedImageColorModel());
        int onColor = config.getPixelOnColor();
        int offColor = config.getPixelOffColor();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                image.setRGB(x, y, matrix.get(x, y) ? onColor : offColor);
            }
        }
        return image;
    }

    @Deprecated
    public static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
        MatrixToImageWriter.writeToPath(matrix, format, file.toPath());
    }

    public static void writeToPath(BitMatrix matrix, String format, Path file) throws IOException {
        MatrixToImageWriter.writeToPath(matrix, format, file, DEFAULT_CONFIG);
    }

    @Deprecated
    public static void writeToFile(BitMatrix matrix, String format, File file, MatrixToImageConfig config) throws IOException {
        MatrixToImageWriter.writeToPath(matrix, format, file.toPath(), config);
    }

    public static void writeToPath(BitMatrix matrix, String format, Path file, MatrixToImageConfig config) throws IOException {
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix, config);
        if (!ImageIO.write((RenderedImage)image, format, file.toFile())) {
            throw new IOException("Could not write an image of format " + format + " to " + file);
        }
    }

    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream) throws IOException {
        MatrixToImageWriter.writeToStream(matrix, format, stream, DEFAULT_CONFIG);
    }

    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream, MatrixToImageConfig config) throws IOException {
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix, config);
        if (!ImageIO.write((RenderedImage)image, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
    }
}

