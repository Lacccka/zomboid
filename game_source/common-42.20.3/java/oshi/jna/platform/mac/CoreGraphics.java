/*
 * Decompiled with CFR 0.152.
 */
package oshi.jna.platform.mac;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.mac.CoreFoundation;
import oshi.util.Util;

public interface CoreGraphics
extends Library {
    public static final CoreGraphics INSTANCE = Native.load("CoreGraphics", CoreGraphics.class);
    public static final int kCGNullWindowID = 0;
    public static final int kCGWindowListOptionAll = 0;
    public static final int kCGWindowListOptionOnScreenOnly = 1;
    public static final int kCGWindowListOptionOnScreenAboveWindow = 2;
    public static final int kCGWindowListOptionOnScreenBelowWindow = 4;
    public static final int kCGWindowListOptionIncludingWindow = 8;
    public static final int kCGWindowListExcludeDesktopElements = 16;

    public CoreFoundation.CFArrayRef CGWindowListCopyWindowInfo(int var1, int var2);

    public boolean CGRectMakeWithDictionaryRepresentation(CoreFoundation.CFDictionaryRef var1, CGRect var2);

    @Structure.FieldOrder(value={"origin", "size"})
    public static class CGRect
    extends Structure
    implements AutoCloseable {
        public CGPoint origin;
        public CGSize size;

        @Override
        public void close() {
            Util.freeMemory(this.getPointer());
        }
    }

    @Structure.FieldOrder(value={"width", "height"})
    public static class CGSize
    extends Structure {
        public double width;
        public double height;
    }

    @Structure.FieldOrder(value={"x", "y"})
    public static class CGPoint
    extends Structure {
        public double x;
        public double y;
    }
}

