/*
 * Decompiled with CFR 0.152.
 */
package oshi.jna.platform.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public interface NtDll
extends com.sun.jna.platform.win32.NtDll {
    public static final NtDll INSTANCE = Native.load("NtDll", NtDll.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final int PROCESS_BASIC_INFORMATION = 0;

    public int NtQueryInformationProcess(WinNT.HANDLE var1, int var2, Pointer var3, int var4, IntByReference var5);

    @Structure.FieldOrder(value={"Flags", "Length", "TimeStamp", "DosPath"})
    public static class RTL_DRIVE_LETTER_CURDIR
    extends Structure {
        public short Flags;
        public short Length;
        public int TimeStamp;
        public STRING DosPath;
    }

    @Structure.FieldOrder(value={"DosPath", "Handle"})
    public static class CURDIR
    extends Structure {
        public UNICODE_STRING DosPath;
        public Pointer Handle;
    }

    @Structure.FieldOrder(value={"Length", "MaximumLength", "Buffer"})
    public static class STRING
    extends Structure {
        public short Length;
        public short MaximumLength;
        public Pointer Buffer;
    }

    @Structure.FieldOrder(value={"Length", "MaximumLength", "Buffer"})
    public static class UNICODE_STRING
    extends Structure {
        public short Length;
        public short MaximumLength;
        public Pointer Buffer;
    }

    @Structure.FieldOrder(value={"MaximumLength", "Length", "Flags", "DebugFlags", "ConsoleHandle", "ConsoleFlags", "StandardInput", "StandardOutput", "StandardError", "CurrentDirectory", "DllPath", "ImagePathName", "CommandLine", "Environment", "StartingX", "StartingY", "CountX", "CountY", "CountCharsX", "CountCharsY", "FillAttribute", "WindowFlags", "ShowWindowFlags", "WindowTitle", "DesktopInfo", "ShellInfo", "RuntimeData", "CurrentDirectories", "EnvironmentSize", "EnvironmentVersion", "PackageDependencyData", "ProcessGroupId", "LoaderThreads", "RedirectionDllName", "HeapPartitionName", "DefaultThreadpoolCpuSetMasks", "DefaultThreadpoolCpuSetMaskCount"})
    public static class RTL_USER_PROCESS_PARAMETERS
    extends Structure {
        public int MaximumLength;
        public int Length;
        public int Flags;
        public int DebugFlags;
        public WinNT.HANDLE ConsoleHandle;
        public int ConsoleFlags;
        public WinNT.HANDLE StandardInput;
        public WinNT.HANDLE StandardOutput;
        public WinNT.HANDLE StandardError;
        public CURDIR CurrentDirectory;
        public UNICODE_STRING DllPath;
        public UNICODE_STRING ImagePathName;
        public UNICODE_STRING CommandLine;
        public Pointer Environment;
        public int StartingX;
        public int StartingY;
        public int CountX;
        public int CountY;
        public int CountCharsX;
        public int CountCharsY;
        public int FillAttribute;
        public int WindowFlags;
        public int ShowWindowFlags;
        public UNICODE_STRING WindowTitle;
        public UNICODE_STRING DesktopInfo;
        public UNICODE_STRING ShellInfo;
        public UNICODE_STRING RuntimeData;
        public RTL_DRIVE_LETTER_CURDIR[] CurrentDirectories = new RTL_DRIVE_LETTER_CURDIR[32];
        public BaseTSD.ULONG_PTR EnvironmentSize;
        public BaseTSD.ULONG_PTR EnvironmentVersion;
        public Pointer PackageDependencyData;
        public int ProcessGroupId;
        public int LoaderThreads;
        public UNICODE_STRING RedirectionDllName;
        public UNICODE_STRING HeapPartitionName;
        public BaseTSD.ULONG_PTR DefaultThreadpoolCpuSetMasks;
        public int DefaultThreadpoolCpuSetMaskCount;
    }

    @Structure.FieldOrder(value={"pad", "pad2", "ProcessParameters"})
    public static class PEB
    extends Structure {
        public byte[] pad = new byte[4];
        public Pointer[] pad2 = new Pointer[3];
        public Pointer ProcessParameters;
    }

    @Structure.FieldOrder(value={"Reserved1", "PebBaseAddress", "Reserved2"})
    public static class PROCESS_BASIC_INFORMATION
    extends Structure {
        public Pointer Reserved1;
        public Pointer PebBaseAddress;
        public Pointer[] Reserved2 = new Pointer[4];
    }
}

