/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wininet;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class WininetUtil {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Map<String, String> getCache() {
        ArrayList<Wininet.INTERNET_CACHE_ENTRY_INFO> items = new ArrayList<Wininet.INTERNET_CACHE_ENTRY_INFO>();
        WinNT.HANDLE cacheHandle = null;
        Win32Exception we = null;
        int lastError = 0;
        LinkedHashMap<String, String> cacheItems = new LinkedHashMap<String, String>();
        try {
            IntByReference size = new IntByReference();
            cacheHandle = Wininet.INSTANCE.FindFirstUrlCacheEntry(null, null, size);
            lastError = Native.getLastError();
            if (lastError == 259) {
                LinkedHashMap<String, String> linkedHashMap = cacheItems;
                return linkedHashMap;
            }
            if (lastError != 0 && lastError != 122) {
                throw new Win32Exception(lastError);
            }
            Wininet.INTERNET_CACHE_ENTRY_INFO entry = new Wininet.INTERNET_CACHE_ENTRY_INFO(size.getValue());
            cacheHandle = Wininet.INSTANCE.FindFirstUrlCacheEntry(null, entry, size);
            if (cacheHandle == null) {
                throw new Win32Exception(Native.getLastError());
            }
            items.add(entry);
            while (true) {
                boolean result;
                if (!(result = Wininet.INSTANCE.FindNextUrlCacheEntry(cacheHandle, null, size = new IntByReference()))) {
                    lastError = Native.getLastError();
                    if (lastError == 259) break;
                    if (lastError != 0 && lastError != 122) {
                        throw new Win32Exception(lastError);
                    }
                }
                if (!(result = Wininet.INSTANCE.FindNextUrlCacheEntry(cacheHandle, entry = new Wininet.INTERNET_CACHE_ENTRY_INFO(size.getValue()), size))) {
                    lastError = Native.getLastError();
                    if (lastError == 259) break;
                    if (lastError != 0 && lastError != 122) {
                        throw new Win32Exception(lastError);
                    }
                }
                items.add(entry);
            }
            for (Wininet.INTERNET_CACHE_ENTRY_INFO item : items) {
                cacheItems.put(item.lpszSourceUrlName.getWideString(0L), item.lpszLocalFileName == null ? "" : item.lpszLocalFileName.getWideString(0L));
            }
        }
        catch (Win32Exception e) {
            we = e;
        }
        finally {
            if (cacheHandle != null && !Wininet.INSTANCE.FindCloseUrlCache(cacheHandle) && we != null) {
                e = new Win32Exception(Native.getLastError());
                e.addSuppressedReflected(we);
                we = e;
            }
        }
        if (we != null) {
            throw we;
        }
        return cacheItems;
    }
}

