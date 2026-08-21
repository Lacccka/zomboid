/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.mac;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import oshi.ffm.ForeignFunctions;

public final class CoreFoundationFunctions
extends ForeignFunctions {
    private static final MethodHandle CFAllocatorGetDefault = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFAllocatorGetDefault"), FunctionDescriptor.of(ValueLayout.ADDRESS, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle CFRelease = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFRelease"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFRetain = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFRetain"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFGetRetainCount = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFGetRetainCount"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFEqual = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFEqual"), FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFCopyDescription = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFCopyDescription"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFStringCreateWithCharacters = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFStringCreateWithCharacters"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG), new Linker.Option[0]);
    private static final MethodHandle CFStringGetLength = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFStringGetLength"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFStringGetCString = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFStringGetCString"), FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle CFStringGetMaximumSizeForEncoding = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFStringGetMaximumSizeForEncoding"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle CFNumberGetValue = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFNumberGetValue"), FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFNumberGetType = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFNumberGetType"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFNumberCreate = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFNumberCreate"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFDataGetLength = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDataGetLength"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFDataGetBytePtr = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDataGetBytePtr"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFDataCreate = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDataCreate"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG), new Linker.Option[0]);
    private static final MethodHandle CFBooleanGetValue = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFBooleanGetValue"), FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFArrayGetCount = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFArrayGetCount"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFArrayGetValueAtIndex = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFArrayGetValueAtIndex"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG), new Linker.Option[0]);
    private static final MethodHandle CFArrayCreate = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFArrayCreate"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFDictionaryGetCount = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDictionaryGetCount"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFDictionaryGetValue = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDictionaryGetValue"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFDictionaryGetValueIfPresent = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDictionaryGetValueIfPresent"), FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFDictionarySetValue = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDictionarySetValue"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFDictionaryCreateMutable = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDictionaryCreateMutable"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFGetTypeID = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFGetTypeID"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFLocaleCopyCurrent = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFLocaleCopyCurrent"), FunctionDescriptor.of(ValueLayout.ADDRESS, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle CFDateFormatterCreate = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDateFormatterCreate"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG), new Linker.Option[0]);
    private static final MethodHandle CFDateFormatterGetFormat = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDateFormatterGetFormat"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle CFArrayGetTypeID = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFArrayGetTypeID"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle CFBooleanGetTypeID = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFBooleanGetTypeID"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle CFDataGetTypeID = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDataGetTypeID"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle CFDateGetTypeID = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDateGetTypeID"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle CFDictionaryGetTypeID = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFDictionaryGetTypeID"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle CFNumberGetTypeID = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFNumberGetTypeID"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle CFStringGetTypeID = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("CFStringGetTypeID"), FunctionDescriptor.of(ValueLayout.JAVA_LONG, new MemoryLayout[0]), new Linker.Option[0]);
    public static final long ARRAY_TYPE_ID;
    public static final long BOOLEAN_TYPE_ID;
    public static final long DATA_TYPE_ID;
    public static final long DATE_TYPE_ID;
    public static final long DICTIONARY_TYPE_ID;
    public static final long NUMBER_TYPE_ID;
    public static final long STRING_TYPE_ID;

    public static MemorySegment CFAllocatorGetDefault() throws Throwable {
        return CFAllocatorGetDefault.invokeExact();
    }

    public static void CFRelease(MemorySegment cf) throws Throwable {
        CFRelease.invokeExact(cf);
    }

    public static MemorySegment CFRetain(MemorySegment cf) throws Throwable {
        return CFRetain.invokeExact(cf);
    }

    public static long CFGetRetainCount(MemorySegment cf) throws Throwable {
        return CFGetRetainCount.invokeExact(cf);
    }

    public static boolean CFEqual(MemorySegment cf1, MemorySegment cf2) throws Throwable {
        return CFEqual.invokeExact(cf1, cf2);
    }

    public static MemorySegment CFCopyDescription(MemorySegment cf) throws Throwable {
        return CFCopyDescription.invokeExact(cf);
    }

    public static MemorySegment CFStringCreateWithCharacters(MemorySegment allocator, MemorySegment chars, long numChars) throws Throwable {
        return CFStringCreateWithCharacters.invokeExact(allocator, chars, numChars);
    }

    public static long CFStringGetLength(MemorySegment theString) throws Throwable {
        return CFStringGetLength.invokeExact(theString);
    }

    public static boolean CFStringGetCString(MemorySegment theString, MemorySegment buffer, long bufferSize, int encoding) throws Throwable {
        return CFStringGetCString.invokeExact(theString, buffer, bufferSize, encoding);
    }

    public static long CFStringGetMaximumSizeForEncoding(long length, int encoding) throws Throwable {
        return CFStringGetMaximumSizeForEncoding.invokeExact(length, encoding);
    }

    public static boolean CFNumberGetValue(MemorySegment number, int theType, MemorySegment valuePtr) throws Throwable {
        return CFNumberGetValue.invokeExact(number, theType, valuePtr);
    }

    public static long CFNumberGetType(MemorySegment number) throws Throwable {
        return CFNumberGetType.invokeExact(number);
    }

    public static MemorySegment CFNumberCreate(MemorySegment allocator, long theType, MemorySegment valuePtr) throws Throwable {
        return CFNumberCreate.invokeExact(allocator, theType, valuePtr);
    }

    public static long CFDataGetLength(MemorySegment dataRef) throws Throwable {
        return CFDataGetLength.invokeExact(dataRef);
    }

    public static MemorySegment CFDataGetBytePtr(MemorySegment dataRef) throws Throwable {
        return CFDataGetBytePtr.invokeExact(dataRef);
    }

    public static MemorySegment CFDataCreate(MemorySegment allocator, MemorySegment bytes, long length) throws Throwable {
        return CFDataCreate.invokeExact(allocator, bytes, length);
    }

    public static byte CFBooleanGetValue(MemorySegment bool) throws Throwable {
        return CFBooleanGetValue.invokeExact(bool);
    }

    public static long CFArrayGetCount(MemorySegment theArray) throws Throwable {
        return CFArrayGetCount.invokeExact(theArray);
    }

    public static MemorySegment CFArrayGetValueAtIndex(MemorySegment theArray, long idx) throws Throwable {
        return CFArrayGetValueAtIndex.invokeExact(theArray, idx);
    }

    public static MemorySegment CFArrayCreate(MemorySegment allocator, MemorySegment values2, long numValues, MemorySegment callbacks) throws Throwable {
        return CFArrayCreate.invokeExact(allocator, values2, numValues, callbacks);
    }

    public static long CFDictionaryGetCount(MemorySegment theDict) throws Throwable {
        return CFDictionaryGetCount.invokeExact(theDict);
    }

    public static MemorySegment CFDictionaryGetValue(MemorySegment theDict, MemorySegment key) throws Throwable {
        return CFDictionaryGetValue.invokeExact(theDict, key);
    }

    public static byte CFDictionaryGetValueIfPresent(MemorySegment theDict, MemorySegment key, MemorySegment value) throws Throwable {
        return CFDictionaryGetValueIfPresent.invokeExact(theDict, key, value);
    }

    public static void CFDictionarySetValue(MemorySegment theDict, MemorySegment key, MemorySegment value) throws Throwable {
        CFDictionarySetValue.invokeExact(theDict, key, value);
    }

    public static MemorySegment CFDictionaryCreateMutable(MemorySegment allocator, long capacity, MemorySegment keyCallBacks, MemorySegment valueCallBacks) throws Throwable {
        return CFDictionaryCreateMutable.invokeExact(allocator, capacity, keyCallBacks, valueCallBacks);
    }

    public static long CFGetTypeID(MemorySegment cf) throws Throwable {
        return CFGetTypeID.invokeExact(cf);
    }

    public static MemorySegment CFLocaleCopyCurrent() throws Throwable {
        return CFLocaleCopyCurrent.invokeExact();
    }

    public static MemorySegment CFDateFormatterCreate(MemorySegment allocator, MemorySegment locale, long dateStyle, long timeStyle) throws Throwable {
        return CFDateFormatterCreate.invokeExact(allocator, locale, dateStyle, timeStyle);
    }

    public static MemorySegment CFDateFormatterGetFormat(MemorySegment formatter) throws Throwable {
        return CFDateFormatterGetFormat.invokeExact(formatter);
    }

    public static long CFArrayGetTypeID() throws Throwable {
        return CFArrayGetTypeID.invokeExact();
    }

    public static long CFBooleanGetTypeID() throws Throwable {
        return CFBooleanGetTypeID.invokeExact();
    }

    public static long CFDataGetTypeID() throws Throwable {
        return CFDataGetTypeID.invokeExact();
    }

    public static long CFDateGetTypeID() throws Throwable {
        return CFDateGetTypeID.invokeExact();
    }

    public static long CFDictionaryGetTypeID() throws Throwable {
        return CFDictionaryGetTypeID.invokeExact();
    }

    public static long CFNumberGetTypeID() throws Throwable {
        return CFNumberGetTypeID.invokeExact();
    }

    public static long CFStringGetTypeID() throws Throwable {
        return CFStringGetTypeID.invokeExact();
    }

    static {
        try {
            ARRAY_TYPE_ID = CoreFoundationFunctions.CFArrayGetTypeID();
            BOOLEAN_TYPE_ID = CoreFoundationFunctions.CFBooleanGetTypeID();
            DATA_TYPE_ID = CoreFoundationFunctions.CFDataGetTypeID();
            DATE_TYPE_ID = CoreFoundationFunctions.CFDateGetTypeID();
            DICTIONARY_TYPE_ID = CoreFoundationFunctions.CFDictionaryGetTypeID();
            NUMBER_TYPE_ID = CoreFoundationFunctions.CFNumberGetTypeID();
            STRING_TYPE_ID = CoreFoundationFunctions.CFStringGetTypeID();
        }
        catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

