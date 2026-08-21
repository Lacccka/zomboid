/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.unix.aix;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.aix.Perfstat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class SharedObjectLoader {
    private SharedObjectLoader() {
    }

    static Perfstat getPerfstatInstance() {
        Map<String, Object> options = SharedObjectLoader.getOptions();
        try {
            return Native.load("/usr/lib/libperfstat.a(shr_64.o)", Perfstat.class, options);
        }
        catch (UnsatisfiedLinkError unsatisfiedLinkError) {
            return Native.load("/usr/lib/libperfstat.a(shr.o)", Perfstat.class, options);
        }
    }

    private static Map<String, Object> getOptions() {
        int RTLD_MEMBER = 262144;
        int RTLD_GLOBAL = 65536;
        int RTLD_LAZY = 4;
        HashMap<String, Integer> options = new HashMap<String, Integer>();
        options.put("open-flags", RTLD_MEMBER | RTLD_GLOBAL | RTLD_LAZY);
        return Collections.unmodifiableMap(options);
    }
}

