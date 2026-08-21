/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.mac;

import java.lang.foreign.MemorySegment;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.ffm.mac.CoreFoundation;

@ThreadSafe
public final class CFUtilFFM {
    private CFUtilFFM() {
    }

    public static String cfPointerToString(MemorySegment segment) {
        return CFUtilFFM.cfPointerToString(segment, true);
    }

    public static String cfPointerToString(MemorySegment segment, boolean returnUnknown) {
        String s = "";
        if (segment != null && !segment.equals(MemorySegment.NULL)) {
            CoreFoundation.CFStringRef cfString = null;
            cfString = new CoreFoundation.CFStringRef(segment);
            s = cfString.stringValue();
        }
        if (returnUnknown && s.isEmpty()) {
            return "unknown";
        }
        return s;
    }

    public static CoreFoundation.CFStringRef stringToCFString(String str) {
        return CoreFoundation.CFStringRef.createCFString(str);
    }
}

