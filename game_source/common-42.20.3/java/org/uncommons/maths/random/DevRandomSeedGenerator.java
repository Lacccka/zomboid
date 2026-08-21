/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

public class DevRandomSeedGenerator
implements SeedGenerator {
    private static final File DEV_RANDOM = new File("/dev/random");

    public byte[] generateSeed(int length) throws SeedException {
        FileInputStream file = null;
        try {
            int bytesRead;
            file = new FileInputStream(DEV_RANDOM);
            byte[] randomSeed = new byte[length];
            for (int count = 0; count < length; count += bytesRead) {
                bytesRead = file.read(randomSeed, count, length - count);
                if (bytesRead != -1) continue;
                throw new SeedException("EOF encountered reading random data.");
            }
            byte[] byArray = randomSeed;
            return byArray;
        }
        catch (IOException ex) {
            throw new SeedException("Failed reading from " + DEV_RANDOM.getName(), ex);
        }
        catch (SecurityException ex) {
            throw new SeedException("SecurityManager prevented access to " + DEV_RANDOM.getName(), ex);
        }
        finally {
            if (file != null) {
                try {
                    file.close();
                }
                catch (IOException ex) {}
            }
        }
    }

    public String toString() {
        return "/dev/random";
    }
}

