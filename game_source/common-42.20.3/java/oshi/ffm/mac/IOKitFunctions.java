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

public final class IOKitFunctions
extends ForeignFunctions {
    private static final MethodHandle IOMasterPort = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOMasterPort"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IORegistryGetRootEntry = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IORegistryGetRootEntry"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle IOServiceNameMatching = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOServiceNameMatching"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IOServiceMatching = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOServiceMatching"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IOServiceGetMatchingService = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOServiceGetMatchingService"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IOServiceGetMatchingServices = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOServiceGetMatchingServices"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IOBSDNameMatching = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOBSDNameMatching"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IOObjectRelease = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOObjectRelease"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IOObjectConformsTo = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOObjectConformsTo"), FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IOIteratorNext = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOIteratorNext"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IORegistryEntryGetRegistryEntryID = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IORegistryEntryGetRegistryEntryID"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IORegistryEntryGetName = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IORegistryEntryGetName"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IORegistryEntryGetChildIterator = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IORegistryEntryGetChildIterator"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IORegistryEntryGetChildEntry = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IORegistryEntryGetChildEntry"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IORegistryEntryGetParentEntry = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IORegistryEntryGetParentEntry"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IORegistryEntryCreateCFProperty = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IORegistryEntryCreateCFProperty"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle IORegistryEntryCreateCFProperties = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IORegistryEntryCreateCFProperties"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle IORegistryEntrySearchCFProperty = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IORegistryEntrySearchCFProperty"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle IOPSCopyPowerSourcesInfo = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOPSCopyPowerSourcesInfo"), FunctionDescriptor.of(ValueLayout.ADDRESS, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle IOPSCopyPowerSourcesList = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOPSCopyPowerSourcesList"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IOPSGetPowerSourceDescription = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOPSGetPowerSourceDescription"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle IOPSGetTimeRemainingEstimate = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("IOPSGetTimeRemainingEstimate"), FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, new MemoryLayout[0]), new Linker.Option[0]);

    private IOKitFunctions() {
    }

    public static int IOMasterPort(int bootstrapPort, MemorySegment port) throws Throwable {
        return IOMasterPort.invokeExact(bootstrapPort, port);
    }

    public static MemorySegment IORegistryGetRootEntry(int masterPort) throws Throwable {
        return IORegistryGetRootEntry.invokeExact(masterPort);
    }

    public static MemorySegment IOServiceNameMatching(MemorySegment name) throws Throwable {
        return IOServiceNameMatching.invokeExact(name);
    }

    public static MemorySegment IOServiceMatching(MemorySegment name) throws Throwable {
        return IOServiceMatching.invokeExact(name);
    }

    public static MemorySegment IOServiceGetMatchingService(int masterPort, MemorySegment matchingDict) throws Throwable {
        return IOServiceGetMatchingService.invokeExact(masterPort, matchingDict);
    }

    public static int IOServiceGetMatchingServices(int masterPort, MemorySegment matchingDict, MemorySegment iterator2) throws Throwable {
        return IOServiceGetMatchingServices.invokeExact(masterPort, matchingDict, iterator2);
    }

    public static MemorySegment IOBSDNameMatching(int masterPort, int options, MemorySegment bsdName) throws Throwable {
        return IOBSDNameMatching.invokeExact(masterPort, options, bsdName);
    }

    public static int IOObjectRelease(MemorySegment object) throws Throwable {
        return IOObjectRelease.invokeExact(object);
    }

    public static boolean IOObjectConformsTo(MemorySegment object, MemorySegment className) throws Throwable {
        return IOObjectConformsTo.invokeExact(object, className);
    }

    public static MemorySegment IOIteratorNext(MemorySegment iterator2) throws Throwable {
        return IOIteratorNext.invokeExact(iterator2);
    }

    public static int IORegistryEntryGetRegistryEntryID(MemorySegment entry, MemorySegment id) throws Throwable {
        return IORegistryEntryGetRegistryEntryID.invokeExact(entry, id);
    }

    public static int IORegistryEntryGetName(MemorySegment entry, MemorySegment name) throws Throwable {
        return IORegistryEntryGetName.invokeExact(entry, name);
    }

    public static int IORegistryEntryGetChildIterator(MemorySegment entry, MemorySegment plane, MemorySegment iter) throws Throwable {
        return IORegistryEntryGetChildIterator.invokeExact(entry, plane, iter);
    }

    public static int IORegistryEntryGetChildEntry(MemorySegment entry, MemorySegment plane, MemorySegment child) throws Throwable {
        return IORegistryEntryGetChildEntry.invokeExact(entry, plane, child);
    }

    public static int IORegistryEntryGetParentEntry(MemorySegment entry, MemorySegment plane, MemorySegment parent) throws Throwable {
        return IORegistryEntryGetParentEntry.invokeExact(entry, plane, parent);
    }

    public static MemorySegment IORegistryEntryCreateCFProperty(MemorySegment entry, MemorySegment key, MemorySegment allocator, int options) throws Throwable {
        return IORegistryEntryCreateCFProperty.invokeExact(entry, key, allocator, options);
    }

    public static int IORegistryEntryCreateCFProperties(MemorySegment entry, MemorySegment properties, MemorySegment allocator, int options) throws Throwable {
        return IORegistryEntryCreateCFProperties.invokeExact(entry, properties, allocator, options);
    }

    public static MemorySegment IORegistryEntrySearchCFProperty(MemorySegment entry, MemorySegment plane, MemorySegment key, MemorySegment allocator, int options) throws Throwable {
        return IORegistryEntrySearchCFProperty.invokeExact(entry, plane, key, allocator, options);
    }

    public static MemorySegment IOPSCopyPowerSourcesInfo() throws Throwable {
        return IOPSCopyPowerSourcesInfo.invokeExact();
    }

    public static MemorySegment IOPSCopyPowerSourcesList(MemorySegment blob) throws Throwable {
        return IOPSCopyPowerSourcesList.invokeExact(blob);
    }

    public static MemorySegment IOPSGetPowerSourceDescription(MemorySegment blob, MemorySegment ps) throws Throwable {
        return IOPSGetPowerSourceDescription.invokeExact(blob, ps);
    }

    public static double IOPSGetTimeRemainingEstimate() throws Throwable {
        return IOPSGetTimeRemainingEstimate.invokeExact();
    }
}

