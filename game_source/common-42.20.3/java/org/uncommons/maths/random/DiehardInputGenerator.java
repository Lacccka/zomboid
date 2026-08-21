/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.util.Random;

public final class DiehardInputGenerator {
    private static final int INT_COUNT = 3000000;

    private DiehardInputGenerator() {
    }

    public static void main(String[] args2) throws Exception {
        if (args2.length != 2) {
            System.out.println("Expected arguments:");
            System.out.println("\t<Fully-qualified RNG class name> <Output file>");
            System.exit(1);
        }
        Class<?> rngClass = Class.forName(args2[0]);
        File outputFile = new File(args2[1]);
        DiehardInputGenerator.generateOutputFile((Random)rngClass.newInstance(), outputFile);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void generateOutputFile(Random rng, File outputFile) throws IOException {
        FilterOutputStream dataOutput = null;
        try {
            dataOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
            for (int i = 0; i < 3000000; ++i) {
                ((DataOutputStream)dataOutput).writeInt(rng.nextInt());
            }
            ((DataOutputStream)dataOutput).flush();
        }
        finally {
            if (dataOutput != null) {
                dataOutput.close();
            }
        }
    }
}

