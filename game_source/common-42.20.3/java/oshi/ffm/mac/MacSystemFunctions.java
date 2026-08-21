/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.mac;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import oshi.ffm.ForeignFunctions;

public final class MacSystemFunctions
extends ForeignFunctions {
    private static final SymbolLookup SYSTEM_LIBRARY = MacSystemFunctions.libraryLookup("System");
    public static final ValueLayout.OfLong SIZE_T = ValueLayout.JAVA_LONG;
    private static final MethodHandle proc_listpids = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("proc_listpids"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle proc_pidinfo = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("proc_pidinfo"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle proc_pidpath = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("proc_pidpath"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle proc_pid_rusage = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("proc_pid_rusage"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle proc_pidfdinfo = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("proc_pidfdinfo"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle getpwuid = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("getpwuid"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle getgrgid = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("getgrgid"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle getpid = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("getpid"), FunctionDescriptor.of(ValueLayout.JAVA_INT, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle sysctl = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("sysctl"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_T), CAPTURE_CALL_STATE);
    private static final MethodHandle sysctlbyname = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("sysctlbyname"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_T), CAPTURE_CALL_STATE);
    private static final MethodHandle getrlimit = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("getrlimit"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle mach_task_self = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("mach_task_self"), FunctionDescriptor.of(ValueLayout.JAVA_INT, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle mach_port_deallocate = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("mach_port_deallocate"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle getfsstat64 = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("getfsstat64"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), new Linker.Option[0]);
    private static final MethodHandle mach_host_self_handle = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("mach_host_self"), FunctionDescriptor.of(ValueLayout.JAVA_INT, new MemoryLayout[0]), new Linker.Option[0]);
    private static final MethodHandle host_page_size = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("host_page_size"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle host_statistics = LINKER.downcallHandle(SYSTEM_LIBRARY.findOrThrow("host_statistics"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS), CAPTURE_CALL_STATE);

    private MacSystemFunctions() {
    }

    public static int proc_listpids(int type, int typeinfo, MemorySegment pids, int bufferSize) throws Throwable {
        return proc_listpids.invokeExact(type, typeinfo, pids, bufferSize);
    }

    public static int proc_pidinfo(int pid, int flavor, long arg, MemorySegment buffer, int bufferSize) throws Throwable {
        return proc_pidinfo.invokeExact(pid, flavor, arg, buffer, bufferSize);
    }

    public static int proc_pidpath(int pid, MemorySegment buffer, int bufferSize) throws Throwable {
        return proc_pidpath.invokeExact(pid, buffer, bufferSize);
    }

    public static int proc_pid_rusage(int pid, int flavor, MemorySegment buffer) throws Throwable {
        return proc_pid_rusage.invokeExact(pid, flavor, buffer);
    }

    public static int proc_pidfdinfo(int pid, int fd, int flavor, MemorySegment buffer, int bufferSize) throws Throwable {
        return proc_pidfdinfo.invokeExact(pid, fd, flavor, buffer, bufferSize);
    }

    public static MemorySegment getpwuid(int uid) throws Throwable {
        MemorySegment result = getpwuid.invokeExact(uid);
        return result.equals(MemorySegment.NULL) ? null : result;
    }

    public static MemorySegment getgrgid(int gid) throws Throwable {
        MemorySegment result = getgrgid.invokeExact(gid);
        return result.equals(MemorySegment.NULL) ? null : result;
    }

    public static int getpid() throws Throwable {
        return getpid.invokeExact();
    }

    public static int sysctl(MemorySegment callState, MemorySegment name, int namelen, MemorySegment oldp, MemorySegment oldlenp, MemorySegment newp, long newlen) throws Throwable {
        return sysctl.invokeExact(callState, name, namelen, oldp, oldlenp, newp, newlen);
    }

    public static int sysctlbyname(MemorySegment callState, MemorySegment name, MemorySegment oldp, MemorySegment oldlenp, MemorySegment newp, long newlen) throws Throwable {
        return sysctlbyname.invokeExact(callState, name, oldp, oldlenp, newp, newlen);
    }

    public static int getrlimit(int resource, MemorySegment rlp) throws Throwable {
        return getrlimit.invokeExact(resource, rlp);
    }

    public static int mach_task_self() throws Throwable {
        return mach_task_self.invokeExact();
    }

    public static int mach_port_deallocate(int task, int name) throws Throwable {
        return mach_port_deallocate.invokeExact(task, name);
    }

    public static int getfsstat64(MemorySegment buffer, int bufsize, int flags) throws Throwable {
        return getfsstat64.invokeExact(buffer, bufsize, flags);
    }

    public static int mach_host_self() throws Throwable {
        return mach_host_self_handle.invokeExact();
    }

    public static int host_page_size(int hostPort, MemorySegment pPageSize) throws Throwable {
        return host_page_size.invokeExact(hostPort, pPageSize);
    }

    public static int host_statistics(MemorySegment callState, int hostPort, int hostStat, MemorySegment stats, MemorySegment count) throws Throwable {
        return host_statistics.invokeExact(callState, hostPort, hostStat, stats, count);
    }
}

