/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.expose.ClassDebugInformation;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.integration.expose.MethodArguments;
import se.krka.kahlua.integration.expose.MethodDebugInformation;
import se.krka.kahlua.integration.expose.ReturnValues;
import se.krka.kahlua.integration.expose.caller.Caller;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;

public class LuaJavaInvoker
implements JavaFunction {
    private final LuaJavaClassExposer exposer;
    private final KahluaConverterManager manager;
    private final Class<?> clazz;
    private final String name;
    private final Caller caller;
    private final Class<?>[] parameterTypes;
    private final int numMethodParams;
    private final Class<?> varargType;
    private final boolean hasSelf;
    private final boolean needsReturnValues;
    private final boolean hasVarargs;

    public LuaJavaInvoker(LuaJavaClassExposer exposer, KahluaConverterManager manager, Class<?> clazz, String name, Caller caller) {
        this.exposer = exposer;
        this.manager = manager;
        this.clazz = clazz;
        this.name = name;
        this.caller = caller;
        this.parameterTypes = caller.getParameterTypes();
        this.varargType = caller.getVarargType();
        this.hasSelf = caller.hasSelf();
        this.needsReturnValues = caller.needsMultipleReturnValues();
        this.hasVarargs = caller.hasVararg();
        this.numMethodParams = this.parameterTypes.length + this.toInt(this.needsReturnValues) + this.toInt(this.hasVarargs);
    }

    private int toInt(boolean b) {
        return b ? 1 : 0;
    }

    public MethodArguments prepareCall(LuaCallFrame callFrame, int nArguments) {
        MethodArguments methodArguments = MethodArguments.get(this.numMethodParams);
        int javaParamCounter = 0;
        int luaArgCounter = 0;
        int selfDecr = this.toInt(this.hasSelf);
        if (this.hasSelf) {
            Object self;
            Object object = self = nArguments <= 0 ? null : callFrame.get(0);
            if (self == null || !this.clazz.isInstance(self)) {
                methodArguments.fail(this.syntaxErrorMessage(this.name + ": Expected a method call but got a function call."));
                return methodArguments;
            }
            methodArguments.setSelf(self);
            ++luaArgCounter;
        }
        ReturnValues returnValues = ReturnValues.get(this.manager, callFrame);
        methodArguments.setReturnValues(returnValues);
        if (this.needsReturnValues) {
            methodArguments.getParams()[javaParamCounter] = returnValues;
            ++javaParamCounter;
        }
        if (nArguments - luaArgCounter < this.parameterTypes.length) {
            int expected = this.parameterTypes.length;
            int got = nArguments - selfDecr;
            methodArguments.fail("expected %d argument%s, got %d".formatted(expected, expected == 1 ? "" : "s", got));
            return methodArguments;
        }
        if (!this.hasVarargs && luaArgCounter != 0 && this.parameterTypes.length < nArguments - luaArgCounter) {
            int expected = this.parameterTypes.length;
            int got = nArguments - selfDecr;
            methodArguments.fail("expected %d argument%s, got %d".formatted(expected, expected == 1 ? "" : "s", got));
            return methodArguments;
        }
        for (int i = 0; i < this.parameterTypes.length; ++i) {
            Object o = callFrame.get(luaArgCounter + i);
            int parameterIndex = luaArgCounter + i - selfDecr;
            Class<?> parameterType = this.parameterTypes[i];
            Object obj = o;
            if (!parameterType.isInstance(o)) {
                obj = this.convert(o, parameterType);
            }
            if (o != null && obj == null) {
                methodArguments.fail("expected argument of type %s, got %s".formatted(parameterType.getSimpleName(), o.getClass().getSimpleName()));
                return methodArguments;
            }
            methodArguments.getParams()[javaParamCounter + i] = obj;
        }
        javaParamCounter += this.parameterTypes.length;
        luaArgCounter += this.parameterTypes.length;
        if (this.hasVarargs) {
            int numVarargs = nArguments - luaArgCounter;
            if (numVarargs < 0) {
                // empty if block
            }
            Object[] varargs = (Object[])Array.newInstance(this.varargType, numVarargs);
            for (int i = 0; i < numVarargs; ++i) {
                Object obj;
                Object o = callFrame.get(luaArgCounter + i);
                int parameterIndex = luaArgCounter + i - selfDecr;
                varargs[i] = obj = this.convert(o, this.varargType);
                if (o == null || obj != null) continue;
                methodArguments.fail("expected argument of type %s, got %s".formatted(this.varargType.getSimpleName(), o.getClass().getSimpleName()));
                return methodArguments;
            }
            methodArguments.getParams()[javaParamCounter] = varargs;
            ++javaParamCounter;
            luaArgCounter += numVarargs;
        } else {
            if (nArguments == 1 && luaArgCounter == 0) {
                return methodArguments;
            }
            if (luaArgCounter != nArguments) {
                int expected = luaArgCounter;
                int got = nArguments - selfDecr;
                methodArguments.fail("expected %d argument%s, got %d".formatted(expected, expected == 1 ? "" : "s", got));
                return methodArguments;
            }
        }
        return methodArguments;
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        MethodArguments methodArguments = this.prepareCall(callFrame, nArguments);
        methodArguments.assertValid();
        int val = this.call(methodArguments);
        ReturnValues.put(methodArguments.getReturnValues());
        MethodArguments.put(methodArguments);
        return val;
    }

    public int call(MethodArguments methodArguments) {
        try {
            ReturnValues returnValues = methodArguments.getReturnValues();
            this.caller.call(methodArguments.getSelf(), returnValues, methodArguments.getParams());
            return returnValues.getNArguments();
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private Object convert(Object o, Class<?> parameterType) {
        if (o == null) {
            return null;
        }
        Object value = this.manager.fromLuaToJava(o, parameterType);
        return value;
    }

    private String syntaxErrorMessage(String errorMessage) {
        String syntax = this.getFunctionSyntax();
        if (syntax != null) {
            errorMessage = (String)errorMessage + " Correct syntax: " + syntax;
        }
        return errorMessage;
    }

    private String newError(int i, String message) {
        int argumentIndex = i + 1;
        String errorMessage = message + " at argument #" + argumentIndex;
        String argumentName = this.getParameterName(i);
        if (argumentName != null) {
            errorMessage = errorMessage + ", " + argumentName;
        }
        return errorMessage;
    }

    private String getFunctionSyntax() {
        MethodDebugInformation methodDebug = this.getMethodDebugData();
        if (methodDebug != null) {
            return methodDebug.getLuaDescription();
        }
        return null;
    }

    public MethodDebugInformation getMethodDebugData() {
        ClassDebugInformation debugInformation = this.exposer.getDebugdata(this.clazz);
        if (debugInformation == null) {
            return null;
        }
        return debugInformation.getMethods().get(this.caller.getDescriptor());
    }

    private String getParameterName(int i) {
        MethodDebugInformation methodDebug = this.getMethodDebugData();
        if (methodDebug != null) {
            return methodDebug.getParameters().get(i).getName();
        }
        return null;
    }

    public String toString() {
        return this.name;
    }

    public int getNumMethodParams() {
        return this.numMethodParams;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        LuaJavaInvoker that = (LuaJavaInvoker)o;
        if (!this.caller.equals(that.caller)) {
            return false;
        }
        if (!this.clazz.equals(that.clazz)) {
            return false;
        }
        return this.name.equals(that.name);
    }

    public int hashCode() {
        int result = this.clazz.hashCode();
        result = 31 * result + this.name.hashCode();
        result = 31 * result + this.caller.hashCode();
        return result;
    }

    public boolean matchesArgumentTypes(LuaCallFrame callFrame, int nArguments) {
        int luaArgCounter = 0;
        if (this.hasSelf) {
            Object self;
            Object object = self = nArguments <= 0 ? null : callFrame.get(0);
            if (self == null || !this.clazz.isInstance(self)) {
                return false;
            }
            ++luaArgCounter;
        }
        if (this.parameterTypes.length != nArguments - luaArgCounter) {
            return false;
        }
        for (int i = 0; i < this.parameterTypes.length; ++i) {
            Class<?> parameterType = this.parameterTypes[i];
            Object o = callFrame.get(luaArgCounter + i);
            if (parameterType.isInstance(o)) continue;
            return false;
        }
        return true;
    }

    public boolean matchesArgumentTypesOrPrimitives(LuaCallFrame callFrame, int nArguments) {
        int luaArgCounter = 0;
        if (this.hasSelf) {
            Object self;
            Object object = self = nArguments <= 0 ? null : callFrame.get(0);
            if (self == null || !this.clazz.isInstance(self)) {
                return false;
            }
            ++luaArgCounter;
        }
        if (this.parameterTypes.length != nArguments - luaArgCounter) {
            return false;
        }
        for (int i = 0; i < this.parameterTypes.length; ++i) {
            Class<?> parameterType = this.parameterTypes[i];
            Object o = callFrame.get(luaArgCounter + i);
            if (parameterType.isInstance(o)) continue;
            if (parameterType.isPrimitive()) {
                if (o == null) {
                    return false;
                }
                if (o instanceof Double) {
                    if (parameterType != Void.TYPE && parameterType != Boolean.TYPE) continue;
                    return false;
                }
                if (o instanceof Boolean && parameterType == Boolean.TYPE) {
                    continue;
                }
            } else if (o == null) continue;
            return false;
        }
        return true;
    }

    public boolean isAllInt() {
        if (this.parameterTypes == null || this.parameterTypes.length == 0) {
            return false;
        }
        for (Class<?> parameterType : this.parameterTypes) {
            if (parameterType == Integer.TYPE) continue;
            return false;
        }
        return true;
    }
}

