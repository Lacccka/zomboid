/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.registry;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wtsapi32;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.software.os.OSSession;
import oshi.util.ParseUtil;

@ThreadSafe
public final class SessionWtsData {
    private static final int WTS_ACTIVE = 0;
    private static final int WTS_CLIENTADDRESS = 14;
    private static final int WTS_SESSIONINFO = 24;
    private static final int WTS_CLIENTPROTOCOLTYPE = 16;
    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    private static final Wtsapi32 WTS = Wtsapi32.INSTANCE;

    private SessionWtsData() {
    }

    public static List<OSSession> queryUserSessions() {
        ArrayList<OSSession> sessions = new ArrayList<OSSession>();
        if (IS_VISTA_OR_GREATER) {
            try (ByRef.CloseablePointerByReference ppSessionInfo = new ByRef.CloseablePointerByReference();
                 ByRef.CloseableIntByReference pCount = new ByRef.CloseableIntByReference();
                 ByRef.CloseablePointerByReference ppBuffer = new ByRef.CloseablePointerByReference();
                 ByRef.CloseableIntByReference pBytes = new ByRef.CloseableIntByReference();){
                if (WTS.WTSEnumerateSessions(Wtsapi32.WTS_CURRENT_SERVER_HANDLE, 0, 1, ppSessionInfo, pCount)) {
                    Pointer pSessionInfo = ppSessionInfo.getValue();
                    if (pCount.getValue() > 0) {
                        Wtsapi32.WTS_SESSION_INFO[] sessionInfo;
                        Wtsapi32.WTS_SESSION_INFO sessionInfoRef = new Wtsapi32.WTS_SESSION_INFO(pSessionInfo);
                        for (Wtsapi32.WTS_SESSION_INFO session : sessionInfo = (Wtsapi32.WTS_SESSION_INFO[])sessionInfoRef.toArray(pCount.getValue())) {
                            if (session.State != 0) continue;
                            WTS.WTSQuerySessionInformation(Wtsapi32.WTS_CURRENT_SERVER_HANDLE, session.SessionId, 16, ppBuffer, pBytes);
                            Pointer pBuffer = ppBuffer.getValue();
                            short protocolType = pBuffer.getShort(0L);
                            WTS.WTSFreeMemory(pBuffer);
                            if (protocolType <= 0) continue;
                            String device = session.pWinStationName;
                            WTS.WTSQuerySessionInformation(Wtsapi32.WTS_CURRENT_SERVER_HANDLE, session.SessionId, 24, ppBuffer, pBytes);
                            pBuffer = ppBuffer.getValue();
                            Wtsapi32.WTSINFO wtsInfo = new Wtsapi32.WTSINFO(pBuffer);
                            long logonTime = new WinBase.FILETIME(new WinNT.LARGE_INTEGER(wtsInfo.LogonTime.getValue())).toTime();
                            String userName = wtsInfo.getUserName();
                            WTS.WTSFreeMemory(pBuffer);
                            WTS.WTSQuerySessionInformation(Wtsapi32.WTS_CURRENT_SERVER_HANDLE, session.SessionId, 14, ppBuffer, pBytes);
                            pBuffer = ppBuffer.getValue();
                            Wtsapi32.WTS_CLIENT_ADDRESS addr = new Wtsapi32.WTS_CLIENT_ADDRESS(pBuffer);
                            WTS.WTSFreeMemory(pBuffer);
                            String host = "::";
                            if (addr.AddressFamily == 2) {
                                try {
                                    host = InetAddress.getByAddress(Arrays.copyOfRange(addr.Address, 2, 6)).getHostAddress();
                                }
                                catch (UnknownHostException e) {
                                    host = "Illegal length IP Array";
                                }
                            } else if (addr.AddressFamily == 23) {
                                int[] ipArray = SessionWtsData.convertBytesToInts(addr.Address);
                                host = ParseUtil.parseUtAddrV6toIP(ipArray);
                            }
                            sessions.add(new OSSession(userName, device, logonTime, host));
                        }
                    }
                    WTS.WTSFreeMemory(pSessionInfo);
                }
            }
        }
        return sessions;
    }

    private static int[] convertBytesToInts(byte[] address) {
        IntBuffer intBuf = ByteBuffer.wrap(Arrays.copyOfRange(address, 2, 18)).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        int[] array = new int[intBuf.remaining()];
        intBuf.get(array);
        return array;
    }
}

