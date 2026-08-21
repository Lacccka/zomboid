/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.DsGetDC;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.LMAccess;
import com.sun.jna.platform.win32.Netapi32;
import com.sun.jna.platform.win32.Ole32Util;
import com.sun.jna.platform.win32.Secur32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.util.ArrayList;

public abstract class Netapi32Util {
    public static String getDCName() {
        return Netapi32Util.getDCName(null, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getDCName(String serverName, String domainName) {
        PointerByReference bufptr = new PointerByReference();
        try {
            int rc = Netapi32.INSTANCE.NetGetDCName(domainName, serverName, bufptr);
            if (0 != rc) {
                throw new Win32Exception(rc);
            }
            String string = bufptr.getValue().getWideString(0L);
            return string;
        }
        finally {
            if (0 != Netapi32.INSTANCE.NetApiBufferFree(bufptr.getValue())) {
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
            }
        }
    }

    public static int getJoinStatus() {
        return Netapi32Util.getJoinStatus(null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getJoinStatus(String computerName) {
        PointerByReference lpNameBuffer = new PointerByReference();
        IntByReference bufferType = new IntByReference();
        try {
            int rc = Netapi32.INSTANCE.NetGetJoinInformation(computerName, lpNameBuffer, bufferType);
            if (0 != rc) {
                throw new Win32Exception(rc);
            }
            int n = bufferType.getValue();
            return n;
        }
        finally {
            int rc;
            if (lpNameBuffer.getPointer() != null && 0 != (rc = Netapi32.INSTANCE.NetApiBufferFree(lpNameBuffer.getValue()))) {
                throw new Win32Exception(rc);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getDomainName(String computerName) {
        PointerByReference lpNameBuffer = new PointerByReference();
        IntByReference bufferType = new IntByReference();
        try {
            int rc = Netapi32.INSTANCE.NetGetJoinInformation(computerName, lpNameBuffer, bufferType);
            if (0 != rc) {
                throw new Win32Exception(rc);
            }
            String string = lpNameBuffer.getValue().getWideString(0L);
            return string;
        }
        finally {
            int rc;
            if (lpNameBuffer.getPointer() != null && 0 != (rc = Netapi32.INSTANCE.NetApiBufferFree(lpNameBuffer.getValue()))) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static LocalGroup[] getLocalGroups() {
        return Netapi32Util.getLocalGroups(null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static LocalGroup[] getLocalGroups(String serverName) {
        PointerByReference bufptr = new PointerByReference();
        IntByReference entriesRead = new IntByReference();
        IntByReference totalEntries = new IntByReference();
        try {
            int rc = Netapi32.INSTANCE.NetLocalGroupEnum(serverName, 1, bufptr, -1, entriesRead, totalEntries, null);
            if (0 != rc || bufptr.getValue() == Pointer.NULL) {
                throw new Win32Exception(rc);
            }
            ArrayList<LocalGroup> result = new ArrayList<LocalGroup>();
            if (entriesRead.getValue() > 0) {
                LMAccess.LOCALGROUP_INFO_1[] groups2;
                LMAccess.LOCALGROUP_INFO_1 group = new LMAccess.LOCALGROUP_INFO_1(bufptr.getValue());
                for (LMAccess.LOCALGROUP_INFO_1 lgpi : groups2 = (LMAccess.LOCALGROUP_INFO_1[])group.toArray(entriesRead.getValue())) {
                    LocalGroup lgp = new LocalGroup();
                    lgp.name = lgpi.lgrui1_name;
                    lgp.comment = lgpi.lgrui1_comment;
                    result.add(lgp);
                }
            }
            LocalGroup[] localGroupArray = result.toArray(new LocalGroup[0]);
            return localGroupArray;
        }
        finally {
            int rc;
            if (bufptr.getValue() != Pointer.NULL && 0 != (rc = Netapi32.INSTANCE.NetApiBufferFree(bufptr.getValue()))) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static Group[] getGlobalGroups() {
        return Netapi32Util.getGlobalGroups(null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Group[] getGlobalGroups(String serverName) {
        PointerByReference bufptr = new PointerByReference();
        IntByReference entriesRead = new IntByReference();
        IntByReference totalEntries = new IntByReference();
        try {
            int rc = Netapi32.INSTANCE.NetGroupEnum(serverName, 1, bufptr, -1, entriesRead, totalEntries, null);
            if (0 != rc || bufptr.getValue() == Pointer.NULL) {
                throw new Win32Exception(rc);
            }
            ArrayList<LocalGroup> result = new ArrayList<LocalGroup>();
            if (entriesRead.getValue() > 0) {
                LMAccess.GROUP_INFO_1[] groups2;
                LMAccess.GROUP_INFO_1 group = new LMAccess.GROUP_INFO_1(bufptr.getValue());
                for (LMAccess.GROUP_INFO_1 lgpi : groups2 = (LMAccess.GROUP_INFO_1[])group.toArray(entriesRead.getValue())) {
                    LocalGroup lgp = new LocalGroup();
                    lgp.name = lgpi.grpi1_name;
                    lgp.comment = lgpi.grpi1_comment;
                    result.add(lgp);
                }
            }
            Group[] groupArray = result.toArray(new LocalGroup[0]);
            return groupArray;
        }
        finally {
            int rc;
            if (bufptr.getValue() != Pointer.NULL && 0 != (rc = Netapi32.INSTANCE.NetApiBufferFree(bufptr.getValue()))) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static User[] getUsers() {
        return Netapi32Util.getUsers(null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static User[] getUsers(String serverName) {
        PointerByReference bufptr = new PointerByReference();
        IntByReference entriesRead = new IntByReference();
        IntByReference totalEntries = new IntByReference();
        try {
            int rc = Netapi32.INSTANCE.NetUserEnum(serverName, 1, 0, bufptr, -1, entriesRead, totalEntries, null);
            if (0 != rc || bufptr.getValue() == Pointer.NULL) {
                throw new Win32Exception(rc);
            }
            ArrayList<User> result = new ArrayList<User>();
            if (entriesRead.getValue() > 0) {
                LMAccess.USER_INFO_1[] users;
                LMAccess.USER_INFO_1 user = new LMAccess.USER_INFO_1(bufptr.getValue());
                for (LMAccess.USER_INFO_1 lu : users = (LMAccess.USER_INFO_1[])user.toArray(entriesRead.getValue())) {
                    User auser = new User();
                    if (lu.usri1_name != null) {
                        auser.name = lu.usri1_name;
                    }
                    result.add(auser);
                }
            }
            User[] userArray = result.toArray(new User[0]);
            return userArray;
        }
        finally {
            int rc;
            if (bufptr.getValue() != Pointer.NULL && 0 != (rc = Netapi32.INSTANCE.NetApiBufferFree(bufptr.getValue()))) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static Group[] getCurrentUserLocalGroups() {
        return Netapi32Util.getUserLocalGroups(Secur32Util.getUserNameEx(2));
    }

    public static Group[] getUserLocalGroups(String userName) {
        return Netapi32Util.getUserLocalGroups(userName, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Group[] getUserLocalGroups(String userName, String serverName) {
        PointerByReference bufptr = new PointerByReference();
        IntByReference entriesread = new IntByReference();
        IntByReference totalentries = new IntByReference();
        try {
            int rc = Netapi32.INSTANCE.NetUserGetLocalGroups(serverName, userName, 0, 0, bufptr, -1, entriesread, totalentries);
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
            ArrayList<LocalGroup> result = new ArrayList<LocalGroup>();
            if (entriesread.getValue() > 0) {
                LMAccess.LOCALGROUP_USERS_INFO_0[] lgroups;
                LMAccess.LOCALGROUP_USERS_INFO_0 lgroup = new LMAccess.LOCALGROUP_USERS_INFO_0(bufptr.getValue());
                for (LMAccess.LOCALGROUP_USERS_INFO_0 lgpi : lgroups = (LMAccess.LOCALGROUP_USERS_INFO_0[])lgroup.toArray(entriesread.getValue())) {
                    LocalGroup lgp = new LocalGroup();
                    if (lgpi.lgrui0_name != null) {
                        lgp.name = lgpi.lgrui0_name;
                    }
                    result.add(lgp);
                }
            }
            Group[] groupArray = result.toArray(new Group[0]);
            return groupArray;
        }
        finally {
            int rc;
            if (bufptr.getValue() != Pointer.NULL && 0 != (rc = Netapi32.INSTANCE.NetApiBufferFree(bufptr.getValue()))) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static Group[] getUserGroups(String userName) {
        return Netapi32Util.getUserGroups(userName, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Group[] getUserGroups(String userName, String serverName) {
        PointerByReference bufptr = new PointerByReference();
        IntByReference entriesread = new IntByReference();
        IntByReference totalentries = new IntByReference();
        try {
            int rc = Netapi32.INSTANCE.NetUserGetGroups(serverName, userName, 0, bufptr, -1, entriesread, totalentries);
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
            ArrayList<Group> result = new ArrayList<Group>();
            if (entriesread.getValue() > 0) {
                LMAccess.GROUP_USERS_INFO_0[] lgroups;
                LMAccess.GROUP_USERS_INFO_0 lgroup = new LMAccess.GROUP_USERS_INFO_0(bufptr.getValue());
                for (LMAccess.GROUP_USERS_INFO_0 lgpi : lgroups = (LMAccess.GROUP_USERS_INFO_0[])lgroup.toArray(entriesread.getValue())) {
                    Group lgp = new Group();
                    if (lgpi.grui0_name != null) {
                        lgp.name = lgpi.grui0_name;
                    }
                    result.add(lgp);
                }
            }
            Group[] groupArray = result.toArray(new Group[0]);
            return groupArray;
        }
        finally {
            int rc;
            if (bufptr.getValue() != Pointer.NULL && 0 != (rc = Netapi32.INSTANCE.NetApiBufferFree(bufptr.getValue()))) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static DomainController getDC() {
        DsGetDC.PDOMAIN_CONTROLLER_INFO pdci = new DsGetDC.PDOMAIN_CONTROLLER_INFO();
        int rc = Netapi32.INSTANCE.DsGetDcName(null, null, null, null, 0, pdci);
        if (0 != rc) {
            throw new Win32Exception(rc);
        }
        DomainController dc = new DomainController();
        dc.address = pdci.dci.DomainControllerAddress;
        dc.addressType = pdci.dci.DomainControllerAddressType;
        dc.clientSiteName = pdci.dci.ClientSiteName;
        dc.dnsForestName = pdci.dci.DnsForestName;
        dc.domainGuid = pdci.dci.DomainGuid;
        dc.domainName = pdci.dci.DomainName;
        dc.flags = pdci.dci.Flags;
        dc.name = pdci.dci.DomainControllerName;
        rc = Netapi32.INSTANCE.NetApiBufferFree(pdci.dci.getPointer());
        if (0 != rc) {
            throw new Win32Exception(rc);
        }
        return dc;
    }

    public static DomainTrust[] getDomainTrusts() {
        return Netapi32Util.getDomainTrusts(null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static DomainTrust[] getDomainTrusts(String serverName) {
        PointerByReference domainsPointerRef = new PointerByReference();
        IntByReference domainTrustCount = new IntByReference();
        int rc = Netapi32.INSTANCE.DsEnumerateDomainTrusts(serverName, 63, domainsPointerRef, domainTrustCount);
        if (0 != rc) {
            throw new Win32Exception(rc);
        }
        try {
            ArrayList<DomainTrust> trusts = new ArrayList<DomainTrust>(domainTrustCount.getValue());
            if (domainTrustCount.getValue() > 0) {
                DsGetDC.DS_DOMAIN_TRUSTS[] domainTrusts;
                DsGetDC.DS_DOMAIN_TRUSTS domainTrustRefs = new DsGetDC.DS_DOMAIN_TRUSTS(domainsPointerRef.getValue());
                for (DsGetDC.DS_DOMAIN_TRUSTS domainTrust : domainTrusts = (DsGetDC.DS_DOMAIN_TRUSTS[])domainTrustRefs.toArray(new DsGetDC.DS_DOMAIN_TRUSTS[domainTrustCount.getValue()])) {
                    DomainTrust t = new DomainTrust();
                    if (domainTrust.DnsDomainName != null) {
                        t.DnsDomainName = domainTrust.DnsDomainName;
                    }
                    if (domainTrust.NetbiosDomainName != null) {
                        t.NetbiosDomainName = domainTrust.NetbiosDomainName;
                    }
                    t.DomainSid = domainTrust.DomainSid;
                    if (domainTrust.DomainSid != null) {
                        t.DomainSidString = Advapi32Util.convertSidToStringSid(domainTrust.DomainSid);
                    }
                    t.DomainGuid = domainTrust.DomainGuid;
                    if (domainTrust.DomainGuid != null) {
                        t.DomainGuidString = Ole32Util.getStringFromGUID(domainTrust.DomainGuid);
                    }
                    t.flags = domainTrust.Flags;
                    trusts.add(t);
                }
            }
            DomainTrust[] domainTrustArray = trusts.toArray(new DomainTrust[0]);
            return domainTrustArray;
        }
        finally {
            rc = Netapi32.INSTANCE.NetApiBufferFree(domainsPointerRef.getValue());
            if (0 != rc) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static UserInfo getUserInfo(String accountName) {
        return Netapi32Util.getUserInfo(accountName, Netapi32Util.getDCName());
    }

    public static UserInfo getUserInfo(String accountName, String domainName) {
        PointerByReference bufptr = new PointerByReference();
        try {
            int rc = Netapi32.INSTANCE.NetUserGetInfo(domainName, accountName, 23, bufptr);
            if (rc == 0) {
                LMAccess.USER_INFO_23 info_23 = new LMAccess.USER_INFO_23(bufptr.getValue());
                UserInfo userInfo = new UserInfo();
                userInfo.comment = info_23.usri23_comment;
                userInfo.flags = info_23.usri23_flags;
                userInfo.fullName = info_23.usri23_full_name;
                userInfo.name = info_23.usri23_name;
                if (info_23.usri23_user_sid != null) {
                    userInfo.sidString = Advapi32Util.convertSidToStringSid(info_23.usri23_user_sid);
                }
                userInfo.sid = info_23.usri23_user_sid;
                UserInfo userInfo2 = userInfo;
                return userInfo2;
            }
            throw new Win32Exception(rc);
        }
        finally {
            if (bufptr.getValue() != Pointer.NULL) {
                Netapi32.INSTANCE.NetApiBufferFree(bufptr.getValue());
            }
        }
    }

    public static class LocalGroup
    extends Group {
        public String comment;
    }

    public static class Group {
        public String name;
    }

    public static class User {
        public String name;
        public String comment;
    }

    public static class DomainController {
        public String name;
        public String address;
        public int addressType;
        public Guid.GUID domainGuid;
        public String domainName;
        public String dnsForestName;
        public int flags;
        public String clientSiteName;
    }

    public static class DomainTrust {
        public String NetbiosDomainName;
        public String DnsDomainName;
        public WinNT.PSID DomainSid;
        public String DomainSidString;
        public Guid.GUID DomainGuid;
        public String DomainGuidString;
        private int flags;

        public boolean isInForest() {
            return (this.flags & 1) != 0;
        }

        public boolean isOutbound() {
            return (this.flags & 2) != 0;
        }

        public boolean isRoot() {
            return (this.flags & 4) != 0;
        }

        public boolean isPrimary() {
            return (this.flags & 8) != 0;
        }

        public boolean isNativeMode() {
            return (this.flags & 0x10) != 0;
        }

        public boolean isInbound() {
            return (this.flags & 0x20) != 0;
        }
    }

    public static class UserInfo
    extends User {
        public String fullName;
        public String sidString;
        public WinNT.PSID sid;
        public int flags;
    }
}

