/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.registry;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Netapi32;
import java.util.ArrayList;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.software.os.OSSession;

@ThreadSafe
public final class NetSessionData {
    private static final Netapi32 NET = Netapi32.INSTANCE;

    private NetSessionData() {
    }

    public static List<OSSession> queryUserSessions() {
        ArrayList<OSSession> sessions = new ArrayList<OSSession>();
        try (ByRef.CloseablePointerByReference bufptr = new ByRef.CloseablePointerByReference();
             ByRef.CloseableIntByReference entriesread = new ByRef.CloseableIntByReference();
             ByRef.CloseableIntByReference totalentries = new ByRef.CloseableIntByReference();){
            if (0 == NET.NetSessionEnum(null, null, null, 10, bufptr, -1, entriesread, totalentries, null)) {
                Pointer buf = bufptr.getValue();
                Netapi32.SESSION_INFO_10 si10 = new Netapi32.SESSION_INFO_10(buf);
                if (entriesread.getValue() > 0) {
                    Netapi32.SESSION_INFO_10[] sessionInfo;
                    for (Netapi32.SESSION_INFO_10 si : sessionInfo = (Netapi32.SESSION_INFO_10[])si10.toArray(entriesread.getValue())) {
                        long logonTime = System.currentTimeMillis() - 1000L * (long)si.sesi10_time;
                        sessions.add(new OSSession(si.sesi10_username, "Network session", logonTime, si.sesi10_cname));
                    }
                }
                NET.NetApiBufferFree(buf);
            }
        }
        return sessions;
    }
}

