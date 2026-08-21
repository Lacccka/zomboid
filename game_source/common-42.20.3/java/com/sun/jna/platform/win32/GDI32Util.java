/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class GDI32Util {
    private static final DirectColorModel SCREENSHOT_COLOR_MODEL = new DirectColorModel(24, 0xFF0000, 65280, 255);
    private static final int[] SCREENSHOT_BAND_MASKS = new int[]{SCREENSHOT_COLOR_MODEL.getRedMask(), SCREENSHOT_COLOR_MODEL.getGreenMask(), SCREENSHOT_COLOR_MODEL.getBlueMask()};

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static BufferedImage getScreenshot(WinDef.HWND target) {
        BufferedImage image;
        Win32Exception we;
        block27: {
            Win32Exception ex;
            WinNT.HANDLE result;
            WinDef.RECT rect = new WinDef.RECT();
            if (!User32.INSTANCE.GetWindowRect(target, rect)) {
                throw new Win32Exception(Native.getLastError());
            }
            Rectangle jRectangle = rect.toRectangle();
            int windowWidth = jRectangle.width;
            int windowHeight = jRectangle.height;
            if (windowWidth == 0 || windowHeight == 0) {
                throw new IllegalStateException("Window width and/or height were 0 even though GetWindowRect did not appear to fail.");
            }
            WinDef.HDC hdcTarget = User32.INSTANCE.GetDC(target);
            if (hdcTarget == null) {
                throw new Win32Exception(Native.getLastError());
            }
            we = null;
            WinDef.HDC hdcTargetMem = null;
            WinDef.HBITMAP hBitmap = null;
            WinNT.HANDLE hOriginal = null;
            image = null;
            try {
                hdcTargetMem = GDI32.INSTANCE.CreateCompatibleDC(hdcTarget);
                if (hdcTargetMem == null) {
                    throw new Win32Exception(Native.getLastError());
                }
                hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcTarget, windowWidth, windowHeight);
                if (hBitmap == null) {
                    throw new Win32Exception(Native.getLastError());
                }
                hOriginal = GDI32.INSTANCE.SelectObject(hdcTargetMem, hBitmap);
                if (hOriginal == null) {
                    throw new Win32Exception(Native.getLastError());
                }
                if (!GDI32.INSTANCE.BitBlt(hdcTargetMem, 0, 0, windowWidth, windowHeight, hdcTarget, 0, 0, 0xCC0020)) {
                    throw new Win32Exception(Native.getLastError());
                }
                WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
                bmi.bmiHeader.biWidth = windowWidth;
                bmi.bmiHeader.biHeight = -windowHeight;
                bmi.bmiHeader.biPlanes = 1;
                bmi.bmiHeader.biBitCount = (short)32;
                bmi.bmiHeader.biCompression = 0;
                Memory buffer = new Memory(windowWidth * windowHeight * 4);
                int resultOfDrawing = GDI32.INSTANCE.GetDIBits(hdcTarget, hBitmap, 0, windowHeight, buffer, bmi, 0);
                if (resultOfDrawing == 0 || resultOfDrawing == 87) {
                    throw new Win32Exception(Native.getLastError());
                }
                int bufferSize = windowWidth * windowHeight;
                DataBufferInt dataBuffer = new DataBufferInt(buffer.getIntArray(0L, bufferSize), bufferSize);
                WritableRaster raster = Raster.createPackedRaster(dataBuffer, windowWidth, windowHeight, windowWidth, SCREENSHOT_BAND_MASKS, null);
                image = new BufferedImage(SCREENSHOT_COLOR_MODEL, raster, false, null);
            }
            catch (Win32Exception e) {
                we = e;
                return we;
            }
            finally {
                Win32Exception ex2;
                WinNT.HANDLE result2;
                if (hOriginal != null && ((result2 = GDI32.INSTANCE.SelectObject(hdcTargetMem, hOriginal)) == null || WinGDI.HGDI_ERROR.equals(result2))) {
                    Win32Exception ex3 = new Win32Exception(Native.getLastError());
                    if (we != null) {
                        ex3.addSuppressedReflected(we);
                    }
                    we = ex3;
                }
                if (hBitmap != null && !GDI32.INSTANCE.DeleteObject(hBitmap)) {
                    ex2 = new Win32Exception(Native.getLastError());
                    if (we != null) {
                        ex2.addSuppressedReflected(we);
                    }
                    we = ex2;
                }
                if (hdcTargetMem != null && !GDI32.INSTANCE.DeleteDC(hdcTargetMem)) {
                    ex2 = new Win32Exception(Native.getLastError());
                    if (we != null) {
                        ex2.addSuppressedReflected(we);
                    }
                    we = ex2;
                }
                if (hdcTarget == null || 0 != User32.INSTANCE.ReleaseDC(target, hdcTarget)) break block27;
                throw new IllegalStateException("Device context did not release properly.");
            }
            if (hOriginal != null && ((result = GDI32.INSTANCE.SelectObject(hdcTargetMem, hOriginal)) == null || WinGDI.HGDI_ERROR.equals(result))) {
                Win32Exception ex4 = new Win32Exception(Native.getLastError());
                if (we != null) {
                    ex4.addSuppressedReflected(we);
                }
                we = ex4;
            }
            if (hBitmap != null && !GDI32.INSTANCE.DeleteObject(hBitmap)) {
                ex = new Win32Exception(Native.getLastError());
                if (we != null) {
                    ex.addSuppressedReflected(we);
                }
                we = ex;
            }
            if (hdcTargetMem != null && !GDI32.INSTANCE.DeleteDC(hdcTargetMem)) {
                ex = new Win32Exception(Native.getLastError());
                if (we != null) {
                    ex.addSuppressedReflected(we);
                }
                we = ex;
            }
            if (hdcTarget != null && 0 == User32.INSTANCE.ReleaseDC(target, hdcTarget)) {
                throw new IllegalStateException("Device context did not release properly.");
            }
        }
        if (we != null) {
            throw we;
        }
        return image;
    }
}

