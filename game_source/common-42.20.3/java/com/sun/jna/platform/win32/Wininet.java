/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Wininet
extends StdCallLibrary {
    public static final Wininet INSTANCE = Native.load("wininet", Wininet.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final int NORMAL_CACHE_ENTRY = 1;
    public static final int STICKY_CACHE_ENTRY = 4;
    public static final int EDITED_CACHE_ENTRY = 8;
    public static final int TRACK_OFFLINE_CACHE_ENTRY = 16;
    public static final int TRACK_ONLINE_CACHE_ENTRY = 32;
    public static final int SPARSE_CACHE_ENTRY = 65536;
    public static final int COOKIE_CACHE_ENTRY = 0x100000;
    public static final int URLHISTORY_CACHE_ENTRY = 0x200000;

    public boolean FindCloseUrlCache(WinNT.HANDLE var1);

    public boolean DeleteUrlCacheEntry(String var1);

    public WinNT.HANDLE FindFirstUrlCacheEntry(String var1, INTERNET_CACHE_ENTRY_INFO var2, IntByReference var3);

    public boolean FindNextUrlCacheEntry(WinNT.HANDLE var1, INTERNET_CACHE_ENTRY_INFO var2, IntByReference var3);

    @Structure.FieldOrder(value={"dwStructSize", "lpszSourceUrlName", "lpszLocalFileName", "CacheEntryType", "dwUseCount", "dwHitRate", "dwSizeLow", "dwSizeHigh", "LastModifiedTime", "ExpireTime", "LastAccessTime", "LastSyncTime", "lpHeaderInfo", "dwHeaderInfoSize", "lpszFileExtension", "u", "additional"})
    public static class INTERNET_CACHE_ENTRY_INFO
    extends Structure {
        public int dwStructSize;
        public Pointer lpszSourceUrlName;
        public Pointer lpszLocalFileName;
        public int CacheEntryType;
        public int dwUseCount;
        public int dwHitRate;
        public int dwSizeLow;
        public int dwSizeHigh;
        public WinBase.FILETIME LastModifiedTime;
        public WinBase.FILETIME ExpireTime;
        public WinBase.FILETIME LastAccessTime;
        public WinBase.FILETIME LastSyncTime;
        public Pointer lpHeaderInfo;
        public int dwHeaderInfoSize;
        public Pointer lpszFileExtension;
        public UNION u;
        public byte[] additional;

        public INTERNET_CACHE_ENTRY_INFO(int size) {
            this.additional = new byte[size];
        }

        @Override
        public String toString() {
            return (this.lpszLocalFileName == null ? "" : this.lpszLocalFileName.getWideString(0L) + " => ") + (this.lpszSourceUrlName == null ? "null" : this.lpszSourceUrlName.getWideString(0L));
        }

        public static class UNION
        extends Union {
            public int dwReserved;
            public int dwExemptDelta;
        }
    }
}

