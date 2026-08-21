/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.OpenGL32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.User32Util;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

public abstract class OpenGL32Util {
    public static Function wglGetProcAddress(String procName) {
        Pointer funcPointer = OpenGL32.INSTANCE.wglGetProcAddress(procName);
        return funcPointer == null ? null : Function.getFunction(funcPointer);
    }

    public static int countGpusNV() {
        WinDef.HWND hWnd = User32Util.createWindow("Message", null, 0, 0, 0, 0, 0, null, null, null, null);
        WinDef.HDC hdc = User32.INSTANCE.GetDC(hWnd);
        WinGDI.PIXELFORMATDESCRIPTOR.ByReference pfd = new WinGDI.PIXELFORMATDESCRIPTOR.ByReference();
        pfd.nVersion = 1;
        pfd.dwFlags = 37;
        pfd.iPixelType = 0;
        pfd.cColorBits = (byte)24;
        pfd.cDepthBits = (byte)16;
        pfd.iLayerType = 0;
        GDI32.INSTANCE.SetPixelFormat(hdc, GDI32.INSTANCE.ChoosePixelFormat(hdc, pfd), pfd);
        WinDef.HGLRC hGLRC = OpenGL32.INSTANCE.wglCreateContext(hdc);
        OpenGL32.INSTANCE.wglMakeCurrent(hdc, hGLRC);
        Pointer funcPointer = OpenGL32.INSTANCE.wglGetProcAddress("wglEnumGpusNV");
        Function fncEnumGpusNV = funcPointer == null ? null : Function.getFunction(funcPointer);
        OpenGL32.INSTANCE.wglDeleteContext(hGLRC);
        User32.INSTANCE.ReleaseDC(hWnd, hdc);
        User32Util.destroyWindow(hWnd);
        if (fncEnumGpusNV == null) {
            return 0;
        }
        WinDef.HGLRCByReference hGPU = new WinDef.HGLRCByReference();
        for (int i = 0; i < 16; ++i) {
            Boolean ok = (Boolean)fncEnumGpusNV.invoke(Boolean.class, new Object[]{i, hGPU});
            if (ok.booleanValue()) continue;
            return i;
        }
        return 0;
    }
}

