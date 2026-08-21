/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.ffm.BCCallDown;
import org.lwjgl.system.ffm.BCCallUp;
import org.lwjgl.system.ffm.BCDescriptors;
import org.lwjgl.system.ffm.FFM;
import org.lwjgl.system.ffm.FFMBooleanInt;
import org.lwjgl.system.ffm.FFMConfig;
import org.lwjgl.system.ffm.FFMDefinition;
import org.lwjgl.system.ffm.FFMPointer;

abstract sealed class BCCall
permits BCCallDown, BCCallUp {
    protected static final boolean BITS32 = ValueLayout.ADDRESS.byteSize() == 4L;
    protected final FFMConfig config;

    BCCall(FFMConfig config) {
        this.config = config;
    }

    protected static ClassDesc getClassDescWrapper(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        return ClassDesc.of(declaringClass.getPackageName(), declaringClass.getSimpleName() + "$" + method.getName());
    }

    protected boolean needsBinder(Class<?> type) {
        if (!type.isInterface()) {
            return false;
        }
        if (MemorySegment.class.isAssignableFrom(type)) {
            return false;
        }
        FFM.lookupBinder(this.config, type);
        return true;
    }

    protected static void boxPrimitiveValue(CodeBuilder cb, TypeKind tk) {
        switch (tk) {
            case BOOLEAN: {
                cb.invokestatic(ConstantDescs.CD_Boolean, "valueOf", BCDescriptors.MTD_Boolean_valueOf);
                break;
            }
            case BYTE: {
                cb.invokestatic(ConstantDescs.CD_Byte, "valueOf", BCDescriptors.MTD_Byte_valueOf);
                break;
            }
            case SHORT: {
                cb.invokestatic(ConstantDescs.CD_Short, "valueOf", BCDescriptors.MTD_Short_valueOf);
                break;
            }
            case INT: {
                cb.invokestatic(ConstantDescs.CD_Integer, "valueOf", BCDescriptors.MTD_Integer_valueOf);
                break;
            }
            case LONG: {
                cb.invokestatic(ConstantDescs.CD_Long, "valueOf", BCDescriptors.MTD_Long_valueOf);
                break;
            }
            case FLOAT: {
                cb.invokestatic(ConstantDescs.CD_Float, "valueOf", BCDescriptors.MTD_Float_valueOf);
                break;
            }
            case DOUBLE: {
                cb.invokestatic(ConstantDescs.CD_Double, "valueOf", BCDescriptors.MTD_Double_valueOf);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported primitive type: " + String.valueOf((Object)tk));
            }
        }
    }

    protected static MemoryLayout valueLayout(Parameter parameter) {
        return BCCall.valueLayout(parameter, parameter.getType());
    }

    protected static MemoryLayout valueLayout(AnnotatedElement element, Class<?> type) {
        if (type == String.class) {
            return FFM.C_POINTER;
        }
        if (type == MemorySegment.class) {
            return ValueLayout.ADDRESS;
        }
        if (type == Boolean.TYPE) {
            FFMBooleanInt booleanInt = element.getAnnotation(FFMBooleanInt.class);
            if (booleanInt != null) {
                return booleanInt.value().layout;
            }
            return ValueLayout.JAVA_BOOLEAN;
        }
        if (type == Byte.TYPE) {
            return ValueLayout.JAVA_BYTE;
        }
        if (type == Short.TYPE) {
            return ValueLayout.JAVA_SHORT;
        }
        if (type == Integer.TYPE) {
            return ValueLayout.JAVA_INT;
        }
        if (type == Long.TYPE) {
            return BITS32 && element.isAnnotationPresent(FFMPointer.class) ? ValueLayout.JAVA_INT : ValueLayout.JAVA_LONG;
        }
        if (type == Float.TYPE) {
            return ValueLayout.JAVA_FLOAT;
        }
        if (type == Double.TYPE) {
            return ValueLayout.JAVA_DOUBLE;
        }
        throw new IllegalArgumentException("Unsupported type: " + String.valueOf(type));
    }

    protected static void printDebug(Method method, Parameter[] parameters, FunctionDescriptor descriptor) {
        APIUtil.apiLog("\t-> J: " + String.valueOf(method.getReturnType()) + " " + method.getName() + "(" + Stream.of(parameters).map(it -> it.getType().getSimpleName()).collect(Collectors.joining(", ")) + ")");
        FFMDefinition signature = method.getAnnotation(FFMDefinition.class);
        if (signature != null) {
            APIUtil.apiLog("\t-> S: " + signature.value());
        }
        APIUtil.apiLog("\t-> N: " + String.valueOf(descriptor));
    }

    static enum FeatureFlag {
        FF_TRACING,
        FF_CHECK,
        FF_STACK,
        FF_BINDER,
        FF_BY_VALUE,
        FF_TYPE_CONVERSION,
        FF_JNI,
        FF_LAST;

        final int mask = 1 << this.ordinal();

        public boolean isSet(int flags) {
            return (flags & this.mask) != 0;
        }
    }
}

