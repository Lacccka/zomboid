/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose.caller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import se.krka.kahlua.integration.expose.ReturnValues;
import se.krka.kahlua.integration.expose.caller.AbstractCaller;
import se.krka.kahlua.integration.processor.DescriptorUtil;
import zombie.core.logger.ExceptionLogger;
import zombie.ui.UIManager;

public class MethodCaller
extends AbstractCaller {
    private final Method method;
    private final Object owner;
    private final boolean hasSelf;
    private final boolean hasReturnValue;

    public MethodCaller(Method method, Object owner, boolean hasSelf) {
        super(method.getParameterTypes(), method.isVarArgs());
        this.method = method;
        this.owner = owner;
        this.hasSelf = hasSelf;
        method.setAccessible(true);
        boolean bl = this.hasReturnValue = !method.getReturnType().equals(Void.TYPE);
        if (this.hasReturnValue && this.needsMultipleReturnValues()) {
            throw new IllegalArgumentException("Must have a void return type if first argument is a ReturnValues: got: " + String.valueOf(method.getReturnType()));
        }
    }

    @Override
    public void call(Object self, ReturnValues rv, Object[] params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (!this.hasSelf) {
            self = this.owner;
        }
        try {
            Object ret = this.method.invoke(self, params);
            if (this.hasReturnValue) {
                rv.push(ret);
            }
        }
        catch (InvocationTargetException e) {
            MethodCaller.logException(e.getTargetException());
        }
        catch (Throwable e) {
            MethodCaller.logException(e);
        }
    }

    private static void logException(Throwable throwable) {
        UIManager.defaultthread.doStacktraceProper();
        UIManager.defaultthread.debugException(throwable);
        ExceptionLogger.logException(throwable);
    }

    @Override
    public boolean hasSelf() {
        return this.hasSelf;
    }

    @Override
    public String getDescriptor() {
        return DescriptorUtil.getDescriptor(this.method);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MethodCaller that = (MethodCaller)o;
        if (!this.method.equals(that.method)) {
            return false;
        }
        return !(this.owner != null ? !this.owner.equals(that.owner) : that.owner != null);
    }

    public int hashCode() {
        int result = this.method.hashCode();
        result = 31 * result + (this.owner != null ? this.owner.hashCode() : 0);
        return result;
    }
}

