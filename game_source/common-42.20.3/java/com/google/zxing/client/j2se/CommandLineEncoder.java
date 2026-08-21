/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.beust.jcommander.JCommander
 */
package com.google.zxing.client.j2se;

import com.beust.jcommander.JCommander;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.EncoderConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.nio.file.Paths;
import java.util.Locale;

public final class CommandLineEncoder {
    private CommandLineEncoder() {
    }

    public static void main(String[] args2) throws Exception {
        EncoderConfig config = new EncoderConfig();
        JCommander jCommander = new JCommander((Object)config, args2);
        jCommander.setProgramName(CommandLineEncoder.class.getSimpleName());
        if (config.help) {
            jCommander.usage();
            return;
        }
        String outFileString = config.outputFileBase;
        if ("out".equals(outFileString)) {
            outFileString = outFileString + '.' + config.imageFormat.toLowerCase(Locale.ENGLISH);
        }
        BitMatrix matrix = new MultiFormatWriter().encode(config.contents.get(0), config.barcodeFormat, config.width, config.height);
        MatrixToImageWriter.writeToPath(matrix, config.imageFormat, Paths.get(outFileString, new String[0]));
    }
}

