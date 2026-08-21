/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.windows;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Callback;
import org.lwjgl.system.windows.WindowProcI;

public abstract class WindowProc
extends Callback
implements WindowProcI {
    public static WindowProc create(long functionPointer) {
        WindowProcI instance = (WindowProcI)Callback.get(functionPointer);
        return instance instanceof WindowProc ? (WindowProc)instance : new Container(functionPointer, instance);
    }

    public static @Nullable WindowProc createSafe(long functionPointer) {
        return functionPointer == 0L ? null : WindowProc.create(functionPointer);
    }

    public static WindowProc create(WindowProcI instance) {
        return instance instanceof WindowProc ? (WindowProc)instance : new Container(instance.address(), instance);
    }

    protected WindowProc() {
        super(DESCRIPTOR);
    }

    WindowProc(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends WindowProc {
        private final WindowProcI delegate;

        Container(long functionPointer, WindowProcI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public long invoke(long hwnd, int uMsg, long wParam, long lParam) {
            return this.delegate.invoke(hwnd, uMsg, wParam, lParam);
        }
    }
}

