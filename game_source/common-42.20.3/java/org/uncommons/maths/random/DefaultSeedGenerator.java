/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.random.DevRandomSeedGenerator;
import org.uncommons.maths.random.RandomDotOrgSeedGenerator;
import org.uncommons.maths.random.SecureRandomSeedGenerator;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

public final class DefaultSeedGenerator
implements SeedGenerator {
    private static final String DEBUG_PROPERTY = "org.uncommons.maths.random.debug";
    private static final DefaultSeedGenerator INSTANCE = new DefaultSeedGenerator();
    private static final SeedGenerator[] GENERATORS = new SeedGenerator[]{new DevRandomSeedGenerator(), new RandomDotOrgSeedGenerator(), new SecureRandomSeedGenerator()};

    private DefaultSeedGenerator() {
    }

    public static DefaultSeedGenerator getInstance() {
        return INSTANCE;
    }

    public byte[] generateSeed(int length) {
        for (SeedGenerator generator : GENERATORS) {
            try {
                byte[] seed = generator.generateSeed(length);
                try {
                    boolean debug = System.getProperty(DEBUG_PROPERTY, "false").equals("true");
                    if (debug) {
                        String seedString = BinaryUtils.convertBytesToHexString(seed);
                        System.out.println(seed.length + " bytes of seed data acquired from " + generator + ":");
                        System.out.println("  " + seedString);
                    }
                }
                catch (SecurityException ex) {
                    // empty catch block
                }
                return seed;
            }
            catch (SeedException ex) {
            }
        }
        throw new IllegalStateException("All available seed generation strategies failed.");
    }
}

