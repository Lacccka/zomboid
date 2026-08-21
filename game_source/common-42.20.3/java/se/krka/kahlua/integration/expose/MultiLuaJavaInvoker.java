/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import se.krka.kahlua.integration.expose.LuaJavaInvoker;
import se.krka.kahlua.integration.expose.MethodArguments;
import se.krka.kahlua.integration.expose.ReturnValues;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;

public class MultiLuaJavaInvoker
implements JavaFunction {
    private final List<LuaJavaInvoker> invokers = new ArrayList<LuaJavaInvoker>();
    private static final Comparator<? super LuaJavaInvoker> COMPARATOR = new Comparator<LuaJavaInvoker>(){

        @Override
        public int compare(LuaJavaInvoker o1, LuaJavaInvoker o2) {
            if (o2.getNumMethodParams() == o1.getNumMethodParams()) {
                boolean allInt1 = o1.isAllInt();
                boolean allInt2 = o2.isAllInt();
                return allInt1 ? 1 : (allInt2 ? -1 : 0);
            }
            return o2.getNumMethodParams() - o1.getNumMethodParams();
        }
    };

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        boolean bValid;
        LuaJavaInvoker invoker;
        int n;
        MethodArguments methodArguments = null;
        int inS = this.invokers.size();
        int best = -1;
        for (n = 0; n < inS; ++n) {
            invoker = this.invokers.get(n);
            if (!invoker.matchesArgumentTypes(callFrame, nArguments)) continue;
            methodArguments = invoker.prepareCall(callFrame, nArguments);
            bValid = methodArguments.isValid();
            if (bValid) {
                int val = invoker.call(methodArguments);
                ReturnValues.put(methodArguments.getReturnValues());
                MethodArguments.put(methodArguments);
                return val;
            }
            best = n;
            break;
        }
        if (best == -1) {
            for (n = 0; n < inS; ++n) {
                invoker = this.invokers.get(n);
                if (!invoker.matchesArgumentTypesOrPrimitives(callFrame, nArguments)) continue;
                methodArguments = invoker.prepareCall(callFrame, nArguments);
                bValid = methodArguments.isValid();
                if (bValid) {
                    int val = invoker.call(methodArguments);
                    ReturnValues.put(methodArguments.getReturnValues());
                    MethodArguments.put(methodArguments);
                    return val;
                }
                best = n;
                break;
            }
        }
        for (n = 0; n < inS; ++n) {
            if (n == best) continue;
            invoker = this.invokers.get(n);
            methodArguments = invoker.prepareCall(callFrame, nArguments);
            bValid = methodArguments.isValid();
            if (bValid) {
                int val = invoker.call(methodArguments);
                ReturnValues.put(methodArguments.getReturnValues());
                MethodArguments.put(methodArguments);
                return val;
            }
            MethodArguments.put(methodArguments);
        }
        if (methodArguments != null) {
            methodArguments.assertValid();
            MethodArguments.put(methodArguments);
        }
        throw new RuntimeException("No implementation found for function: " + this.getMethodSignature(callFrame, nArguments) + ")");
    }

    public String getMethodSignature(LuaCallFrame callFrame, int nArguments) {
        String methodName = this.getMethodName();
        String methodArgs = this.getParametersString(callFrame, nArguments);
        return methodName + "(" + methodArgs + ")";
    }

    public String getMethodName() {
        String methodName = !this.invokers.isEmpty() ? this.invokers.get(0).toString() : "!noname!";
        return methodName;
    }

    public String getParametersString(LuaCallFrame callFrame, int nArguments) {
        StringBuilder returnVal = new StringBuilder();
        for (int i = 0; i < nArguments; ++i) {
            Class<?> parameterType;
            Object o = callFrame.get(i);
            Class<?> clazz = parameterType = o != null ? o.getClass() : null;
            if (!returnVal.isEmpty()) {
                returnVal.append(", ");
            }
            returnVal.append(parameterType);
            returnVal.append(' ');
            returnVal.append(o);
        }
        return returnVal.toString();
    }

    public void addInvoker(LuaJavaInvoker invoker) {
        if (!this.invokers.contains(invoker)) {
            this.invokers.add(invoker);
            Collections.sort(this.invokers, COMPARATOR);
        }
    }

    public List<LuaJavaInvoker> getInvokers() {
        return this.invokers;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MultiLuaJavaInvoker that = (MultiLuaJavaInvoker)o;
        return this.invokers.equals(that.invokers);
    }

    public int hashCode() {
        return this.invokers.hashCode();
    }

    public String toString() {
        return this.getMethodName();
    }
}

