/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows;

import com.sun.jna.Pointer;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.software.os.OSDesktopWindow;

@ThreadSafe
public final class EnumWindows {
    private static final WinDef.DWORD GW_HWNDNEXT = new WinDef.DWORD(2L);

    private EnumWindows() {
    }

    public static List<OSDesktopWindow> queryDesktopWindows(boolean visibleOnly) {
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        ArrayList<OSDesktopWindow> windowList = new ArrayList<OSDesktopWindow>();
        HashMap<WinDef.HWND, Integer> zOrderMap = new HashMap<WinDef.HWND, Integer>();
        for (DesktopWindow window : windows) {
            WinDef.HWND hWnd = window.getHWND();
            if (hWnd == null) continue;
            boolean visible = User32.INSTANCE.IsWindowVisible(hWnd);
            if (visibleOnly && !visible) continue;
            if (!zOrderMap.containsKey(hWnd)) {
                EnumWindows.updateWindowZOrderMap(hWnd, zOrderMap);
            }
            try (ByRef.CloseableIntByReference pProcessId = new ByRef.CloseableIntByReference();){
                User32.INSTANCE.GetWindowThreadProcessId(hWnd, pProcessId);
                windowList.add(new OSDesktopWindow(Pointer.nativeValue(hWnd.getPointer()), window.getTitle(), window.getFilePath(), window.getLocAndSize(), pProcessId.getValue(), (Integer)zOrderMap.get(hWnd), visible));
            }
        }
        return windowList;
    }

    private static void updateWindowZOrderMap(WinDef.HWND hWnd, Map<WinDef.HWND, Integer> zOrderMap) {
        if (hWnd != null) {
            int zOrder = 1;
            WinDef.HWND h = new WinDef.HWND(hWnd.getPointer());
            do {
                zOrderMap.put(h, --zOrder);
            } while ((h = User32.INSTANCE.GetWindow(h, GW_HWNDNEXT)) != null);
            int offset = zOrder * -1;
            zOrderMap.replaceAll((k, v) -> v + offset);
        }
    }
}

