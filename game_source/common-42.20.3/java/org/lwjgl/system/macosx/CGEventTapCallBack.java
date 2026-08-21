/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.macosx;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Callback;
import org.lwjgl.system.macosx.CGEventTapCallBackI;

public abstract class CGEventTapCallBack
extends Callback
implements CGEventTapCallBackI {
    public static CGEventTapCallBack create(long functionPointer) {
        CGEventTapCallBackI instance = (CGEventTapCallBackI)Callback.get(functionPointer);
        return instance instanceof CGEventTapCallBack ? (CGEventTapCallBack)instance : new Container(functionPointer, instance);
    }

    public static @Nullable CGEventTapCallBack createSafe(long functionPointer) {
        return functionPointer == 0L ? null : CGEventTapCallBack.create(functionPointer);
    }

    public static CGEventTapCallBack create(CGEventTapCallBackI instance) {
        return instance instanceof CGEventTapCallBack ? (CGEventTapCallBack)instance : new Container(instance.address(), instance);
    }

    protected CGEventTapCallBack() {
        super(DESCRIPTOR);
    }

    CGEventTapCallBack(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends CGEventTapCallBack {
        private final CGEventTapCallBackI delegate;

        Container(long functionPointer, CGEventTapCallBackI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public long invoke(long proxy, int type, long event, long userInfo) {
            return this.delegate.invoke(proxy, type, event, userInfo);
        }
    }
}

