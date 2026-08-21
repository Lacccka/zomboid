/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.mac;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;
import oshi.ffm.ForeignFunctions;
import oshi.ffm.mac.CoreFoundationFunctions;

public interface CoreFoundation {
    public static final int kCFNotFound = -1;
    public static final int kCFStringEncodingASCII = 1536;
    public static final int kCFStringEncodingUTF8 = 0x8000100;
    public static final int kCFNumberSInt8Type = 1;
    public static final int kCFNumberSInt16Type = 2;
    public static final int kCFNumberSInt32Type = 3;
    public static final int kCFNumberSInt64Type = 4;
    public static final int kCFNumberFloat32Type = 5;
    public static final int kCFNumberFloat64Type = 6;
    public static final int kCFNumberCharType = 7;
    public static final int kCFNumberShortType = 8;
    public static final int kCFNumberIntType = 9;
    public static final int kCFNumberLongType = 10;
    public static final int kCFNumberLongLongType = 11;
    public static final int kCFNumberFloatType = 12;
    public static final int kCFNumberDoubleType = 13;
    public static final int kCFNumberCFIndexType = 14;
    public static final int kCFNumberNSIntegerType = 15;
    public static final int kCFNumberCGFloatType = 16;
    public static final int kCFDateFormatterNoStyle = 0;
    public static final int kCFDateFormatterShortStyle = 1;
    public static final int kCFDateFormatterMediumStyle = 2;
    public static final int kCFDateFormatterLongStyle = 3;
    public static final int kCFDateFormatterFullStyle = 4;

    public static class CFDateFormatter
    extends CFTypeRef {
        public CFDateFormatter(MemorySegment segment) {
            super(segment);
        }

        public static CFDateFormatter create(CFAllocatorRef allocator, CFLocale locale, int dateStyle, int timeStyle) {
            try {
                MemorySegment allocSeg = allocator != null ? allocator.segment() : MemorySegment.NULL;
                MemorySegment localeSeg = locale != null ? locale.segment() : MemorySegment.NULL;
                MemorySegment formatter = CoreFoundationFunctions.CFDateFormatterCreate(allocSeg, localeSeg, dateStyle, timeStyle);
                return new CFDateFormatter(formatter);
            }
            catch (Throwable e) {
                return new CFDateFormatter(MemorySegment.NULL);
            }
        }

        public CFStringRef getFormat() {
            if (this.isNull()) {
                return new CFStringRef(MemorySegment.NULL);
            }
            try {
                return new CFStringRef(CoreFoundationFunctions.CFDateFormatterGetFormat(this.segment()));
            }
            catch (Throwable e) {
                return new CFStringRef(MemorySegment.NULL);
            }
        }
    }

    public static class CFLocale
    extends CFTypeRef {
        public CFLocale(MemorySegment segment) {
            super(segment);
        }

        public static CFLocale copyCurrent() {
            try {
                return new CFLocale(CoreFoundationFunctions.CFLocaleCopyCurrent());
            }
            catch (Throwable e) {
                return new CFLocale(MemorySegment.NULL);
            }
        }
    }

    public static class CFStringRef
    extends CFTypeRef {
        public CFStringRef(MemorySegment segment) {
            super(segment);
            if (!this.isNull() && !this.isTypeID(CoreFoundationFunctions.STRING_TYPE_ID)) {
                throw new ClassCastException("Unable to cast to CFString. Type ID: " + this.getTypeID());
            }
        }

        public static CFStringRef createCFString(String s) {
            Arena arena = Arena.ofConfined();
            try {
                char[] chars = s.toCharArray();
                MemorySegment charsSeg = arena.allocateFrom(ValueLayout.JAVA_CHAR, chars);
                MemorySegment allocator = CoreFoundationFunctions.CFAllocatorGetDefault();
                MemorySegment stringRef = CoreFoundationFunctions.CFStringCreateWithCharacters(allocator, charsSeg, chars.length);
                CFStringRef cFStringRef = new CFStringRef(stringRef);
                if (arena != null) {
                    arena.close();
                }
                return cFStringRef;
            }
            catch (Throwable throwable) {
                try {
                    if (arena != null) {
                        try {
                            arena.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Throwable e) {
                    return new CFStringRef(MemorySegment.NULL);
                }
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public String stringValue() {
            if (this.isNull()) {
                return "";
            }
            try (Arena arena = Arena.ofConfined();){
                long length = CoreFoundationFunctions.CFStringGetLength(this.segment());
                if (length == 0L) {
                    String string = "";
                    return string;
                }
                long maxSize = CoreFoundationFunctions.CFStringGetMaximumSizeForEncoding(length, 0x8000100);
                if (maxSize == -1L) {
                    throw new StringIndexOutOfBoundsException("CFString maximum number of bytes exceeds LONG_MAX.");
                }
                MemorySegment buf = arena.allocate(maxSize + 1L);
                if (!CoreFoundationFunctions.CFStringGetCString(this.segment(), buf, maxSize + 1L, 0x8000100)) throw new IllegalArgumentException("CFString conversion failed or buffer too small.");
                String string = buf.getString(0L);
                return string;
            }
            catch (Throwable e) {
                return "";
            }
        }
    }

    public static class CFMutableDictionaryRef
    extends CFDictionaryRef {
        public CFMutableDictionaryRef(MemorySegment segment) {
            super(segment);
        }

        public void setValue(CFTypeRef key, CFTypeRef value) {
            if (this.isNull() || key == null || key.isNull() || value == null || value.isNull()) {
                return;
            }
            try {
                CoreFoundationFunctions.CFDictionarySetValue(this.segment(), key.segment, value.segment);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public static class CFDictionaryRef
    extends CFTypeRef {
        public CFDictionaryRef(MemorySegment segment) {
            super(segment);
            if (!this.isNull() && !this.isTypeID(CoreFoundationFunctions.DICTIONARY_TYPE_ID)) {
                throw new ClassCastException("Unable to cast to CFDictionary. Type ID: " + this.getTypeID());
            }
        }

        public MemorySegment getValue(CFTypeRef key) {
            if (this.isNull() || key == null || key.isNull()) {
                return MemorySegment.NULL;
            }
            try {
                return CoreFoundationFunctions.CFDictionaryGetValue(this.segment(), key.segment);
            }
            catch (Throwable e) {
                return MemorySegment.NULL;
            }
        }

        public long getCount() {
            if (this.isNull()) {
                return 0L;
            }
            try {
                return CoreFoundationFunctions.CFDictionaryGetCount(this.segment());
            }
            catch (Throwable e) {
                return 0L;
            }
        }

        public boolean getValueIfPresent(CFTypeRef key, MemorySegment value) {
            if (this.isNull() || key == null || key.isNull()) {
                return false;
            }
            try {
                return CoreFoundationFunctions.CFDictionaryGetValueIfPresent(this.segment(), key.segment, value) != 0;
            }
            catch (Throwable e) {
                return false;
            }
        }
    }

    public static class CFDataRef
    extends CFTypeRef {
        public CFDataRef(MemorySegment segment) {
            super(segment);
            if (!this.isNull() && !this.isTypeID(CoreFoundationFunctions.DATA_TYPE_ID)) {
                throw new ClassCastException("Unable to cast to CFData. Type ID: " + this.getTypeID());
            }
        }

        public int getLength() {
            if (this.isNull()) {
                return 0;
            }
            try {
                return (int)CoreFoundationFunctions.CFDataGetLength(this.segment());
            }
            catch (Throwable e) {
                return 0;
            }
        }

        public MemorySegment getBytePtr() {
            if (this.isNull()) {
                return MemorySegment.NULL;
            }
            try {
                return CoreFoundationFunctions.CFDataGetBytePtr(this.segment());
            }
            catch (Throwable e) {
                return MemorySegment.NULL;
            }
        }

        public byte[] getBytes() {
            if (this.isNull()) {
                return new byte[0];
            }
            Arena arena = Arena.ofConfined();
            try {
                int length = this.getLength();
                MemorySegment bytePtr = this.getBytePtr();
                byte[] byArray = ForeignFunctions.getByteArrayFromNativePointer(bytePtr, length, arena);
                if (arena != null) {
                    arena.close();
                }
                return byArray;
            }
            catch (Throwable throwable) {
                try {
                    if (arena != null) {
                        try {
                            arena.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Throwable e) {
                    return new byte[0];
                }
            }
        }
    }

    public static class CFArrayRef
    extends CFTypeRef {
        public CFArrayRef(MemorySegment segment) {
            super(segment);
            if (!this.isNull() && !this.isTypeID(CoreFoundationFunctions.ARRAY_TYPE_ID)) {
                throw new ClassCastException("Unable to cast to CFArray. Type ID: " + this.getTypeID());
            }
        }

        public int getCount() {
            if (this.isNull()) {
                return 0;
            }
            try {
                return (int)CoreFoundationFunctions.CFArrayGetCount(this.segment());
            }
            catch (Throwable e) {
                return 0;
            }
        }

        public MemorySegment getValueAtIndex(int idx) {
            if (this.isNull()) {
                return MemorySegment.NULL;
            }
            try {
                return CoreFoundationFunctions.CFArrayGetValueAtIndex(this.segment(), idx);
            }
            catch (Throwable e) {
                return MemorySegment.NULL;
            }
        }
    }

    public static class CFBooleanRef
    extends CFTypeRef {
        public CFBooleanRef(MemorySegment segment) {
            super(segment);
            if (!this.isNull() && !this.isTypeID(CoreFoundationFunctions.BOOLEAN_TYPE_ID)) {
                throw new ClassCastException("Unable to cast to CFBoolean. Type ID: " + this.getTypeID());
            }
        }

        public boolean booleanValue() {
            if (this.isNull()) {
                return false;
            }
            try {
                return CoreFoundationFunctions.CFBooleanGetValue(this.segment()) != 0;
            }
            catch (Throwable e) {
                return false;
            }
        }
    }

    public static class CFNumberRef
    extends CFTypeRef {
        public CFNumberRef(MemorySegment segment) {
            super(segment);
            if (!this.isNull() && !this.isTypeID(CoreFoundationFunctions.NUMBER_TYPE_ID)) {
                throw new ClassCastException("Unable to cast to CFNumber. Type ID: " + this.getTypeID());
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public long longValue() {
            if (this.isNull()) {
                return 0L;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment valuePtr = arena.allocate(ValueLayout.JAVA_LONG);
                if (CoreFoundationFunctions.CFNumberGetValue(this.segment(), 11, valuePtr)) {
                    long l = valuePtr.get(ValueLayout.JAVA_LONG, 0L);
                    return l;
                }
                long l = 0L;
                return l;
            }
            catch (Throwable e) {
                return 0L;
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public int intValue() {
            if (this.isNull()) {
                return 0;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment valuePtr = arena.allocate(ValueLayout.JAVA_INT);
                if (CoreFoundationFunctions.CFNumberGetValue(this.segment(), 9, valuePtr)) {
                    int n = valuePtr.get(ValueLayout.JAVA_INT, 0L);
                    return n;
                }
                int n = 0;
                return n;
            }
            catch (Throwable e) {
                return 0;
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public short shortValue() {
            if (this.isNull()) {
                return 0;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment valuePtr = arena.allocate(ValueLayout.JAVA_SHORT);
                if (CoreFoundationFunctions.CFNumberGetValue(this.segment(), 8, valuePtr)) {
                    short s = valuePtr.get(ValueLayout.JAVA_SHORT, 0L);
                    return s;
                }
                short s = 0;
                return s;
            }
            catch (Throwable e) {
                return 0;
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public byte byteValue() {
            if (this.isNull()) {
                return 0;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment valuePtr = arena.allocate(ValueLayout.JAVA_BYTE);
                if (CoreFoundationFunctions.CFNumberGetValue(this.segment(), 7, valuePtr)) {
                    byte by = valuePtr.get(ValueLayout.JAVA_BYTE, 0L);
                    return by;
                }
                byte by = 0;
                return by;
            }
            catch (Throwable e) {
                return 0;
            }
        }

        /*
         * Loose catch block
         */
        public double doubleValue() {
            Arena arena;
            block13: {
                if (this.isNull()) {
                    return 0.0;
                }
                arena = Arena.ofConfined();
                MemorySegment valuePtr = arena.allocate(ValueLayout.JAVA_DOUBLE);
                if (!CoreFoundationFunctions.CFNumberGetValue(this.segment(), 13, valuePtr)) break block13;
                double d = valuePtr.get(ValueLayout.JAVA_DOUBLE, 0L);
                if (arena != null) {
                    arena.close();
                }
                return d;
            }
            double d = 0.0;
            if (arena != null) {
                arena.close();
            }
            return d;
            {
                catch (Throwable throwable) {
                    try {
                        if (arena != null) {
                            try {
                                arena.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (Throwable e) {
                        return 0.0;
                    }
                }
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public float floatValue() {
            if (this.isNull()) {
                return 0.0f;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment valuePtr = arena.allocate(ValueLayout.JAVA_FLOAT);
                if (CoreFoundationFunctions.CFNumberGetValue(this.segment(), 12, valuePtr)) {
                    float f = valuePtr.get(ValueLayout.JAVA_FLOAT, 0L);
                    return f;
                }
                float f = 0.0f;
                return f;
            }
            catch (Throwable e) {
                return 0.0f;
            }
        }
    }

    public static class CFAllocatorRef
    extends CFTypeRef {
        public CFAllocatorRef(MemorySegment segment) {
            super(segment);
        }
    }

    public static class CFTypeRef {
        private final MemorySegment segment;

        public CFTypeRef(MemorySegment segment) {
            this.segment = segment;
        }

        public MemorySegment segment() {
            return this.segment;
        }

        public boolean isNull() {
            return this.segment == null || this.segment.equals(MemorySegment.NULL);
        }

        public long getTypeID() {
            if (this.isNull()) {
                return 0L;
            }
            try {
                return CoreFoundationFunctions.CFGetTypeID(this.segment());
            }
            catch (Throwable e) {
                return 0L;
            }
        }

        public boolean isTypeID(long typeID) {
            return this.getTypeID() == typeID;
        }

        public void retain() {
            if (!this.isNull()) {
                try {
                    CoreFoundationFunctions.CFRetain(this.segment());
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }

        public void release() {
            if (!this.isNull()) {
                try {
                    CoreFoundationFunctions.CFRelease(this.segment());
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            CFTypeRef cfTypeRef = (CFTypeRef)o;
            if (this.isNull() || cfTypeRef.isNull()) {
                return this.isNull() == cfTypeRef.isNull();
            }
            try {
                return CoreFoundationFunctions.CFEqual(this.segment(), cfTypeRef.segment);
            }
            catch (Throwable e) {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(this.segment());
        }
    }
}

