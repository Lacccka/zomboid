/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.windows;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

public interface WinNTFFM {
    public static final int EVENTLOG_BACKWARDS_READ = 8;
    public static final int EVENTLOG_SEQUENTIAL_READ = 1;
    public static final int KEY_READ = 131097;
    public static final int KEY_WOW64_64KEY = 256;
    public static final int KEY_WOW64_32KEY = 512;
    public static final int REG_SZ = 1;
    public static final int REG_EXPAND_SZ = 2;
    public static final int REG_DWORD = 4;
    public static final int SE_PRIVILEGE_ENABLED = 2;
    public static final int TOKEN_QUERY = 8;
    public static final int TOKEN_ADJUST_PRIVILEGES = 32;
    public static final int TokenElevation = 20;
    public static final StructLayout EVENTLOGRECORD = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("Length"), ValueLayout.JAVA_INT.withName("Reserved"), ValueLayout.JAVA_INT.withName("RecordNumber"), ValueLayout.JAVA_INT.withName("TimeGenerated"), ValueLayout.JAVA_INT.withName("TimeWritten"), ValueLayout.JAVA_INT.withName("EventID"), ValueLayout.JAVA_SHORT.withName("EventType"), ValueLayout.JAVA_SHORT.withName("NumStrings"), ValueLayout.JAVA_SHORT.withName("EventCategory"), ValueLayout.JAVA_SHORT.withName("ReservedFlags"), MemoryLayout.paddingLayout(4L), ValueLayout.JAVA_INT.withName("ClosingRecordNumber"), ValueLayout.JAVA_INT.withName("StringOffset"), ValueLayout.JAVA_INT.withName("UserSidLength"), ValueLayout.JAVA_INT.withName("UserSidOffset"), ValueLayout.JAVA_INT.withName("DataLength"), ValueLayout.JAVA_INT.withName("DataOffset"));
    public static final StructLayout LUID = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("LowPart"), ValueLayout.JAVA_INT.withName("HighPart"));
    public static final StructLayout LUID_AND_ATTRIBUTES = MemoryLayout.structLayout(LUID.withName("Luid"), ValueLayout.JAVA_INT.withName("Attributes"));
    public static final StructLayout PERFORMANCE_INFORMATION = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("cb"), MemoryLayout.paddingLayout(4L), ValueLayout.JAVA_LONG.withName("CommitTotal"), ValueLayout.JAVA_LONG.withName("CommitLimit"), ValueLayout.JAVA_LONG.withName("CommitPeak"), ValueLayout.JAVA_LONG.withName("PhysicalTotal"), ValueLayout.JAVA_LONG.withName("PhysicalAvailable"), ValueLayout.JAVA_LONG.withName("SystemCache"), ValueLayout.JAVA_LONG.withName("KernelTotal"), ValueLayout.JAVA_LONG.withName("KernelPaged"), ValueLayout.JAVA_LONG.withName("KernelNonpaged"), ValueLayout.JAVA_LONG.withName("PageSize"), ValueLayout.JAVA_INT.withName("HandleCount"), ValueLayout.JAVA_INT.withName("ProcessCount"), ValueLayout.JAVA_INT.withName("ThreadCount"), MemoryLayout.paddingLayout(4L));
    public static final StructLayout TOKEN_PRIVILEGES = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("PrivilegeCount"), LUID_AND_ATTRIBUTES.withName("Privileges"));
    public static final StructLayout TOKEN_ELEVATION = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("TokenIsElevated"));
    public static final long OFFSET_TIME_GENERATED = EVENTLOGRECORD.byteOffset(MemoryLayout.PathElement.groupElement("TimeGenerated"));
    public static final long OFFSET_EVENTID = EVENTLOGRECORD.byteOffset(MemoryLayout.PathElement.groupElement("EventID"));
    public static final long OFFSET_LENGTH = EVENTLOGRECORD.byteOffset(MemoryLayout.PathElement.groupElement("Length"));
    public static final long TOKEN_PRIVILEGES_PRIVILEGE_COUNT_OFFSET = TOKEN_PRIVILEGES.byteOffset(MemoryLayout.PathElement.groupElement("PrivilegeCount"));
    public static final long TOKEN_PRIVILEGES_LUID_OFFSET = TOKEN_PRIVILEGES.byteOffset(MemoryLayout.PathElement.groupElement("Privileges"), MemoryLayout.PathElement.groupElement("Luid"));
    public static final long TOKEN_PRIVILEGES_ATTRIBUTES_OFFSET = TOKEN_PRIVILEGES.byteOffset(MemoryLayout.PathElement.groupElement("Privileges"), MemoryLayout.PathElement.groupElement("Attributes"));
}

