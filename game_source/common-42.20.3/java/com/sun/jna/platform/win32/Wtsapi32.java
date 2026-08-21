/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.win32.W32APITypeMapper;

public interface Wtsapi32
extends StdCallLibrary {
    public static final Wtsapi32 INSTANCE = Native.load("Wtsapi32", Wtsapi32.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final int NOTIFY_FOR_ALL_SESSIONS = 1;
    public static final int NOTIFY_FOR_THIS_SESSION = 0;
    public static final int WTS_CONSOLE_CONNECT = 1;
    public static final int WTS_CONSOLE_DISCONNECT = 2;
    public static final int WTS_REMOTE_CONNECT = 3;
    public static final int WTS_REMOTE_DISCONNECT = 4;
    public static final int WTS_SESSION_LOGON = 5;
    public static final int WTS_SESSION_LOGOFF = 6;
    public static final int WTS_SESSION_LOCK = 7;
    public static final int WTS_SESSION_UNLOCK = 8;
    public static final int WTS_SESSION_REMOTE_CONTROL = 9;
    public static final WinNT.HANDLE WTS_CURRENT_SERVER_HANDLE = new WinNT.HANDLE(null);
    public static final int WTS_CURRENT_SESSION = -1;
    public static final int WTS_ANY_SESSION = -2;
    public static final int WTS_PROCESS_INFO_LEVEL_0 = 0;
    public static final int WTS_PROCESS_INFO_LEVEL_1 = 1;
    public static final int DOMAIN_LENGTH = 17;
    public static final int USERNAME_LENGTH = 20;
    public static final int WINSTATIONNAME_LENGTH = 32;

    public boolean WTSEnumerateSessions(WinNT.HANDLE var1, int var2, int var3, PointerByReference var4, IntByReference var5);

    public boolean WTSQuerySessionInformation(WinNT.HANDLE var1, int var2, int var3, PointerByReference var4, IntByReference var5);

    public void WTSFreeMemory(Pointer var1);

    public boolean WTSRegisterSessionNotification(WinDef.HWND var1, int var2);

    public boolean WTSUnRegisterSessionNotification(WinDef.HWND var1);

    public boolean WTSEnumerateProcessesEx(WinNT.HANDLE var1, IntByReference var2, int var3, PointerByReference var4, IntByReference var5);

    public boolean WTSFreeMemoryEx(int var1, Pointer var2, int var3);

    @Structure.FieldOrder(value={"SessionId", "ProcessId", "pProcessName", "pUserSid", "NumberOfThreads", "HandleCount", "PagefileUsage", "PeakPagefileUsage", "WorkingSetSize", "PeakWorkingSetSize", "UserTime", "KernelTime"})
    public static class WTS_PROCESS_INFO_EX
    extends Structure {
        public int SessionId;
        public int ProcessId;
        public String pProcessName;
        public WinNT.PSID pUserSid;
        public int NumberOfThreads;
        public int HandleCount;
        public int PagefileUsage;
        public int PeakPagefileUsage;
        public int WorkingSetSize;
        public int PeakWorkingSetSize;
        public WinNT.LARGE_INTEGER UserTime;
        public WinNT.LARGE_INTEGER KernelTime;

        public WTS_PROCESS_INFO_EX() {
            super(W32APITypeMapper.DEFAULT);
        }

        public WTS_PROCESS_INFO_EX(Pointer p) {
            super(p, 0, W32APITypeMapper.DEFAULT);
            this.read();
        }
    }

    @Structure.FieldOrder(value={"State", "SessionId", "IncomingBytes", "OutgoingBytes", "IncomingFrames", "OutgoingFrames", "IncomingCompressedBytes", "OutgoingCompressedBytes", "WinStationName", "Domain", "UserName", "ConnectTime", "DisconnectTime", "LastInputTime", "LogonTime", "CurrentTime"})
    public static class WTSINFO
    extends Structure {
        private static final int CHAR_WIDTH = Boolean.getBoolean("w32.ascii") ? 1 : 2;
        public int State;
        public int SessionId;
        public int IncomingBytes;
        public int OutgoingBytes;
        public int IncomingFrames;
        public int OutgoingFrames;
        public int IncomingCompressedBytes;
        public int OutgoingCompressedBytes;
        public final byte[] WinStationName = new byte[32 * CHAR_WIDTH];
        public final byte[] Domain = new byte[17 * CHAR_WIDTH];
        public final byte[] UserName = new byte[21 * CHAR_WIDTH];
        public WinNT.LARGE_INTEGER ConnectTime;
        public WinNT.LARGE_INTEGER DisconnectTime;
        public WinNT.LARGE_INTEGER LastInputTime;
        public WinNT.LARGE_INTEGER LogonTime;
        public WinNT.LARGE_INTEGER CurrentTime;

        public WTSINFO() {
        }

        public WTSINFO(Pointer p) {
            super(p);
            this.read();
        }

        public String getWinStationName() {
            return this.getStringAtOffset(this.fieldOffset("WinStationName"));
        }

        public String getDomain() {
            return this.getStringAtOffset(this.fieldOffset("Domain"));
        }

        public String getUserName() {
            return this.getStringAtOffset(this.fieldOffset("UserName"));
        }

        private String getStringAtOffset(int offset) {
            return CHAR_WIDTH == 1 ? this.getPointer().getString(offset) : this.getPointer().getWideString(offset);
        }
    }

    @Structure.FieldOrder(value={"AddressFamily", "Address"})
    public static class WTS_CLIENT_ADDRESS
    extends Structure {
        public int AddressFamily;
        public byte[] Address = new byte[20];

        public WTS_CLIENT_ADDRESS() {
        }

        public WTS_CLIENT_ADDRESS(Pointer p) {
            super(p);
            this.read();
        }
    }

    @Structure.FieldOrder(value={"SessionId", "pWinStationName", "State"})
    public static class WTS_SESSION_INFO
    extends Structure {
        public int SessionId;
        public String pWinStationName;
        public int State;

        public WTS_SESSION_INFO() {
            super(W32APITypeMapper.DEFAULT);
        }

        public WTS_SESSION_INFO(Pointer p) {
            super(p, 0, W32APITypeMapper.DEFAULT);
            this.read();
        }
    }

    public static interface WTS_INFO_CLASS {
        public static final int WTSInitialProgram = 0;
        public static final int WTSApplicationName = 1;
        public static final int WTSWorkingDirectory = 2;
        public static final int WTSOEMId = 3;
        public static final int WTSSessionId = 4;
        public static final int WTSUserName = 5;
        public static final int WTSWinStationName = 6;
        public static final int WTSDomainName = 7;
        public static final int WTSConnectState = 8;
        public static final int WTSClientBuildNumber = 9;
        public static final int WTSClientName = 10;
        public static final int WTSClientDirectory = 11;
        public static final int WTSClientProductId = 12;
        public static final int WTSClientHardwareId = 13;
        public static final int WTSClientAddress = 14;
        public static final int WTSClientDisplay = 15;
        public static final int WTSClientProtocolType = 16;
        public static final int WTSIdleTime = 17;
        public static final int WTSLogonTime = 18;
        public static final int WTSIncomingBytes = 19;
        public static final int WTSOutgoingBytes = 20;
        public static final int WTSIncomingFrames = 21;
        public static final int WTSOutgoingFrames = 22;
        public static final int WTSClientInfo = 23;
        public static final int WTSSessionInfo = 24;
        public static final int WTSSessionInfoEx = 25;
        public static final int WTSConfigInfo = 26;
        public static final int WTSValidationInfo = 27;
        public static final int WTSSessionAddressV4 = 28;
        public static final int WTSIsRemoteSession = 29;
    }

    public static interface WTS_CONNECTSTATE_CLASS {
        public static final int WTSActive = 0;
        public static final int WTSConnected = 1;
        public static final int WTSConnectQuery = 2;
        public static final int WTSShadow = 3;
        public static final int WTSDisconnected = 4;
        public static final int WTSIdle = 5;
        public static final int WTSListen = 6;
        public static final int WTSReset = 7;
        public static final int WTSDown = 8;
        public static final int WTSInit = 9;
    }
}

