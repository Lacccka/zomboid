/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200;

import org.apache.commons.compress.harmony.unpack200.IMatcher;

public final class SegmentUtils {
    public static int countArgs(String descriptor) {
        return SegmentUtils.countArgs(descriptor, 1);
    }

    protected static int countArgs(String descriptor, int widthOfLongsAndDoubles) {
        int bra = descriptor.indexOf(40);
        int ket = descriptor.indexOf(41);
        if (bra == -1 || ket == -1 || ket < bra) {
            throw new IllegalArgumentException("No arguments");
        }
        boolean inType = false;
        boolean consumingNextType = false;
        int count = 0;
        for (int i = bra + 1; i < ket; ++i) {
            char charAt = descriptor.charAt(i);
            if (inType && charAt == ';') {
                inType = false;
                consumingNextType = false;
                continue;
            }
            if (!inType && charAt == 'L') {
                inType = true;
                ++count;
                continue;
            }
            if (charAt == '[') {
                consumingNextType = true;
                continue;
            }
            if (inType) continue;
            if (consumingNextType) {
                ++count;
                consumingNextType = false;
                continue;
            }
            if (charAt == 'D' || charAt == 'J') {
                count += widthOfLongsAndDoubles;
                continue;
            }
            ++count;
        }
        return count;
    }

    public static int countBit16(int[] flags) {
        int count = 0;
        for (int flag : flags) {
            if ((flag & 0x10000) == 0) continue;
            ++count;
        }
        return count;
    }

    public static int countBit16(long[] flags) {
        int count = 0;
        for (long flag : flags) {
            if ((flag & 0x10000L) == 0L) continue;
            ++count;
        }
        return count;
    }

    public static int countBit16(long[][] flags) {
        int count = 0;
        long[][] lArray = flags;
        int n = lArray.length;
        for (int i = 0; i < n; ++i) {
            long[] flag;
            for (long element : flag = lArray[i]) {
                if ((element & 0x10000L) == 0L) continue;
                ++count;
            }
        }
        return count;
    }

    public static int countInvokeInterfaceArgs(String descriptor) {
        return SegmentUtils.countArgs(descriptor, 2);
    }

    public static int countMatches(long[] flags, IMatcher matcher) {
        int count = 0;
        for (long flag : flags) {
            if (!matcher.matches(flag)) continue;
            ++count;
        }
        return count;
    }

    public static int countMatches(long[][] flags, IMatcher matcher) {
        int count = 0;
        for (long[] flag : flags) {
            count += SegmentUtils.countMatches(flag, matcher);
        }
        return count;
    }
}

