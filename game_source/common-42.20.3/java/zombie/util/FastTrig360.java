/*
 * Decompiled with CFR 0.152.
 */
package zombie.util;

import zombie.core.math.PZMath;

public final class FastTrig360 {
    private static final float[] sinTable = new float[360];

    public static float sin(float rad) {
        float degf = PZMath.wrap(rad * 57.295776f, 0.0f, 360.0f);
        int deg = PZMath.fastfloor(degf);
        return sinTable[deg % 360];
    }

    public static float cos(float rad) {
        return FastTrig360.sin(rad + 1.5707964f);
    }

    public static float approximateAtan2(float y, float x) {
        float atan;
        if (x == 0.0f) {
            if (y > 0.0f) {
                return 1.5707964f;
            }
            if (y < 0.0f) {
                return -1.5707964f;
            }
            return 0.0f;
        }
        float z = y / x;
        if (Math.abs(z) < 1.0f) {
            atan = z / (1.0f + 0.28086f * z * z);
            if (x < 0.0f) {
                if (y < 0.0f) {
                    return atan - (float)Math.PI;
                }
                return atan + (float)Math.PI;
            }
        } else {
            atan = 1.5707964f - z / (z * z + 0.28086f);
            if (y < 0.0f) {
                return atan - (float)Math.PI;
            }
        }
        return atan;
    }

    public static void test() {
        double maxDeviation = 0.0;
        for (int i = 0; i < 360; ++i) {
            double x = Math.cos((float)i * ((float)Math.PI / 180));
            double y = Math.sin((float)i * ((float)Math.PI / 180));
            double angleAccurate = Math.atan2(y, x) * 57.2957763671875;
            float angleApprox = FastTrig360.approximateAtan2((float)y, (float)x) * 57.295776f;
            double deviation = Math.abs(angleAccurate - (double)angleApprox);
            maxDeviation = Math.max(maxDeviation, deviation);
            System.out.printf("i=%d deviation=%.4f%n", i, deviation);
        }
        System.out.printf("maxDeviation=%.4f%n", maxDeviation);
    }

    static {
        for (int i = 0; i < sinTable.length; ++i) {
            FastTrig360.sinTable[i] = (float)Math.sin((float)i * ((float)Math.PI / 180));
        }
    }
}

