/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.j2se;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.DecoderConfig;
import com.google.zxing.client.j2se.ImageReader;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;

final class DecodeWorker
implements Callable<Integer> {
    private static final int RED = -65536;
    private static final int BLACK = -16777216;
    private static final int WHITE = -1;
    private final DecoderConfig config;
    private final Queue<URI> inputs;
    private final Map<DecodeHintType, ?> hints;

    DecodeWorker(DecoderConfig config, Queue<URI> inputs) {
        this.config = config;
        this.inputs = inputs;
        this.hints = config.buildHints();
    }

    @Override
    public Integer call() throws IOException {
        URI input;
        int successful = 0;
        while ((input = this.inputs.poll()) != null) {
            Result[] results = this.decode(input, this.hints);
            if (results == null) continue;
            ++successful;
            if (!this.config.dumpResults) continue;
            DecodeWorker.dumpResult(input, results);
        }
        return successful;
    }

    private static Path buildOutputPath(URI input, String suffix) throws IOException {
        String inputFileName;
        Path outDir;
        if ("file".equals(input.getScheme())) {
            Path inputPath = Paths.get(input);
            outDir = inputPath.getParent();
            inputFileName = inputPath.getFileName().toString();
        } else {
            outDir = Paths.get(".", new String[0]).toRealPath(new LinkOption[0]);
            String[] pathElements = input.getPath().split("/");
            inputFileName = pathElements[pathElements.length - 1];
        }
        int pos = inputFileName.lastIndexOf(46);
        inputFileName = pos > 0 ? inputFileName.substring(0, pos) + suffix : inputFileName + suffix;
        return outDir.resolve(inputFileName);
    }

    private static void dumpResult(URI input, Result ... results) throws IOException {
        ArrayList<String> resultTexts = new ArrayList<String>();
        for (Result result : results) {
            resultTexts.add(result.getText());
        }
        Files.write(DecodeWorker.buildOutputPath(input, ".txt"), resultTexts, StandardCharsets.UTF_8, new OpenOption[0]);
    }

    private Result[] decode(URI uri, Map<DecodeHintType, ?> hints) throws IOException {
        Result[] results;
        BufferedImageLuminanceSource source2;
        BufferedImage image = ImageReader.readImage(uri);
        if (this.config.crop == null) {
            source2 = new BufferedImageLuminanceSource(image);
        } else {
            List<Integer> crop = this.config.crop;
            source2 = new BufferedImageLuminanceSource(image, crop.get(0), crop.get(1), crop.get(2), crop.get(3));
        }
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source2));
        if (this.config.dumpBlackPoint) {
            DecodeWorker.dumpBlackPoint(uri, image, bitmap);
        }
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        try {
            if (this.config.multi) {
                GenericMultipleBarcodeReader reader = new GenericMultipleBarcodeReader(multiFormatReader);
                results = reader.decodeMultiple(bitmap, hints);
            } else {
                results = new Result[]{multiFormatReader.decode(bitmap, hints)};
            }
        }
        catch (NotFoundException ignored) {
            System.out.println(uri + ": No barcode found");
            return null;
        }
        if (this.config.brief) {
            System.out.println(uri + ": Success");
        } else {
            for (Result result : results) {
                ParsedResult parsedResult = ResultParser.parseResult(result);
                System.out.println(uri + " (format: " + (Object)((Object)result.getBarcodeFormat()) + ", type: " + (Object)((Object)parsedResult.getType()) + "):\n" + "Raw result:\n" + result.getText() + "\n" + "Parsed result:\n" + parsedResult.getDisplayResult());
                System.out.println("Found " + result.getResultPoints().length + " result points.");
                for (int i = 0; i < result.getResultPoints().length; ++i) {
                    ResultPoint rp = result.getResultPoints()[i];
                    System.out.println("  Point " + i + ": (" + rp.getX() + ',' + rp.getY() + ')');
                }
            }
        }
        return results;
    }

    private static void dumpBlackPoint(URI uri, BufferedImage image, BinaryBitmap bitmap) throws IOException {
        int offset;
        int y;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int stride = width * 3;
        int[] pixels = new int[stride * height];
        int[] argb = new int[width];
        for (int y2 = 0; y2 < height; ++y2) {
            image.getRGB(0, y2, width, 1, argb, 0, width);
            System.arraycopy(argb, 0, pixels, y2 * stride, width);
        }
        BitArray row = new BitArray(width);
        for (y = 0; y < height; ++y) {
            try {
                row = bitmap.getBlackRow(y, row);
            }
            catch (NotFoundException nfe) {
                offset = y * stride + width;
                Arrays.fill(pixels, offset, offset + width, -65536);
                continue;
            }
            int offset2 = y * stride + width;
            for (int x = 0; x < width; ++x) {
                pixels[offset2 + x] = row.get(x) ? -16777216 : -1;
            }
        }
        try {
            for (y = 0; y < height; ++y) {
                BitMatrix matrix = bitmap.getBlackMatrix();
                offset = y * stride + width * 2;
                for (int x = 0; x < width; ++x) {
                    pixels[offset + x] = matrix.get(x, y) ? -16777216 : -1;
                }
            }
        }
        catch (NotFoundException notFoundException) {
            // empty catch block
        }
        DecodeWorker.writeResultImage(stride, height, pixels, uri, ".mono.png");
    }

    private static void writeResultImage(int stride, int height, int[] pixels, URI input, String suffix) throws IOException {
        BufferedImage result = new BufferedImage(stride, height, 2);
        result.setRGB(0, 0, stride, height, pixels, 0, stride);
        Path imagePath = DecodeWorker.buildOutputPath(input, suffix);
        try {
            if (!ImageIO.write((RenderedImage)result, "png", imagePath.toFile())) {
                System.err.println("Could not encode an image to " + imagePath);
            }
        }
        catch (IOException ignored) {
            System.err.println("Could not write to " + imagePath);
        }
    }
}

