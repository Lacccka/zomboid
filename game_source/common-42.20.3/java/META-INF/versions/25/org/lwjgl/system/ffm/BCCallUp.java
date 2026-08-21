/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Struct;
import org.lwjgl.system.ffm.BCCall;
import org.lwjgl.system.ffm.BCDescriptors;
import org.lwjgl.system.ffm.BCUtil;
import org.lwjgl.system.ffm.Binder;
import org.lwjgl.system.ffm.FFMBooleanInt;
import org.lwjgl.system.ffm.FFMByValue;
import org.lwjgl.system.ffm.FFMConfig;
import org.lwjgl.system.ffm.FFMPointer;
import org.lwjgl.system.ffm.GroupBinder;
import org.lwjgl.system.ffm.UpcallBinder;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.FFIType;

final class BCCallUp
extends BCCall {
    private static final int FF_RETURNS_STRUCT_BY_VALUE = Integer.MIN_VALUE;
    private final Class<?> upcallInterface;
    private final Method method;
    private final Parameter[] parameters;
    private final int featureFlags;
    private final int[] featureFlagOffsets;
    private final LinkedHashMap<Class<?>, Integer> binders;
    final FunctionDescriptor descriptor;

    BCCallUp(FFMConfig config, Class<?> upcallInterface, @Nullable FFICIF cif) {
        super(config);
        this.upcallInterface = upcallInterface;
        if (!upcallInterface.isInterface()) {
            throw new UnsupportedOperationException("The binder must be parameterized with an interface");
        }
        if (upcallInterface.getDeclaredAnnotation(FunctionalInterface.class) == null) {
            throw new UnsupportedOperationException("The upcall interface must be annotated with @FunctionalInterface");
        }
        this.method = Arrays.stream(upcallInterface.getDeclaredMethods()).filter(m -> !m.isDefault()).findFirst().orElseThrow();
        this.parameters = this.method.getParameters();
        this.featureFlagOffsets = new int[BCCall.FeatureFlag.FF_LAST.ordinal()];
        this.binders = new LinkedHashMap();
        boolean hasTracing = config.traceConsumer != null && (config.tracingFilter == null || config.tracingFilter.test(this.method));
        int featureFlags = hasTracing ? BCCall.FeatureFlag.FF_TRACING.mask : 0;
        ArrayList<MemoryLayout> argLayouts = new ArrayList<MemoryLayout>(this.parameters.length);
        for (int i = 0; i < this.parameters.length; ++i) {
            Parameter parameter = this.parameters[i];
            Class<?> type = parameter.getType();
            if (BCUtil.isPointerType(parameter, type)) {
                if (BITS32 && type == Long.TYPE) {
                    featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
                }
            } else if (type == String.class) {
                featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
            } else if (type == Boolean.TYPE) {
                MemoryLayout layout;
                FFMBooleanInt booleanInt = parameter.getAnnotation(FFMBooleanInt.class);
                if (booleanInt != null) {
                    featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
                    argLayouts.add(booleanInt.value().layout);
                    continue;
                }
                if (cif != null && (layout = BCCallUp.memoryLayoutFrom(FFIType.create(cif.arg_types().get(i)))) != ValueLayout.JAVA_BYTE) {
                    featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
                    argLayouts.add(layout);
                    continue;
                }
            } else {
                if (this.needsBinder(type)) {
                    featureFlags |= BCCall.FeatureFlag.FF_BINDER.mask;
                    argLayouts.add(ValueLayout.ADDRESS);
                    continue;
                }
                if (Struct.class.isAssignableFrom(type)) {
                    if (parameter != this.parameters[this.parameters.length - 1]) {
                        throw new IllegalStateException("Group result parameter must be the last parameter");
                    }
                    if (this.method.getReturnType() != Void.TYPE) {
                        throw new IllegalStateException("Group result parameter requires a void return type");
                    }
                    featureFlags |= Integer.MIN_VALUE;
                    continue;
                }
            }
            argLayouts.add(BCCallUp.valueLayout(parameter));
        }
        MemoryLayout resLayout = null;
        Class<?> type = this.method.getReturnType();
        if (type != Void.TYPE) {
            if (type == String.class) {
                throw new IllegalStateException("String return types are not supported in upcalls: " + String.valueOf(this.method));
            }
            if (type == Boolean.TYPE) {
                if (this.method.isAnnotationPresent(FFMBooleanInt.class)) {
                    featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
                } else if (cif != null) {
                    resLayout = BCCallUp.memoryLayoutFrom(cif.rtype());
                    featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
                }
            } else if (BITS32 && type == Long.TYPE && this.method.isAnnotationPresent(FFMPointer.class)) {
                featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
                resLayout = ValueLayout.JAVA_INT;
            } else if (this.needsBinder(type)) {
                Binder<?> binder = config.binders.get(type).binder();
                if (binder instanceof UpcallBinder) {
                    throw new IllegalStateException("Upcalls can only be returned as raw pointer values");
                }
                this.binders.putIfAbsent(type, this.binders.size());
                featureFlags |= BCCall.FeatureFlag.FF_BINDER.mask;
                MemoryLayout groupLayout = ((GroupBinder)binder).layout();
                if (this.method.isAnnotationPresent(FFMByValue.class)) {
                    featureFlags |= BCCall.FeatureFlag.FF_BY_VALUE.mask;
                    resLayout = groupLayout;
                } else {
                    resLayout = ValueLayout.ADDRESS.withTargetLayout(groupLayout);
                }
            } else {
                resLayout = BCCallUp.valueLayout(this.method, this.method.getReturnType());
            }
        } else if ((featureFlags & Integer.MIN_VALUE) != 0) {
            resLayout = BCCallUp.groupLayoutFrom(Objects.requireNonNull(cif).rtype());
        }
        this.featureFlags = featureFlags;
        MemoryLayout[] argLayoutsArray = (MemoryLayout[])argLayouts.toArray(MemoryLayout[]::new);
        this.descriptor = resLayout == null ? FunctionDescriptor.ofVoid(argLayoutsArray) : FunctionDescriptor.of(resLayout, argLayoutsArray);
    }

    <T> UpcallBinder<T> bootstrap() {
        MethodHandle methodHandle;
        BCCallUp bCCallUp;
        if (this.config.debugGenerator) {
            BCCallUp.printDebug(this.method, this.parameters, this.descriptor);
        }
        MethodHandle bridgeDescriptor = switch (this.featureFlags) {
            case 0 -> null;
            default -> {
                MethodType type = this.descriptor.toMethodType().insertParameterTypes(0, this.upcallInterface);
                if ((this.featureFlags & Integer.MIN_VALUE) != 0) {
                    type = type.insertParameterTypes(1, MemorySegment.class);
                }
                yield type;
            }
        };
        BCCallUp bCCallUp2 = this;
        switch (this.featureFlags) {
            case 0: {
                MethodHandle methodHandle2;
                try {
                    methodHandle2 = this.config.lookup.unreflect(this.method);
                    bCCallUp = bCCallUp2;
                    methodHandle = methodHandle2;
                    break;
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            default: {
                MethodHandle methodHandle2 = bridgeDescriptor;
                bCCallUp = bCCallUp2;
                methodHandle = methodHandle2;
            }
        }
        List<Object> classData = bCCallUp.getClassData(methodHandle);
        ClassDesc thisClass = BCCallUp.getClassDescWrapper(this.method);
        byte[] bytecode = ClassFile.of().build(thisClass, arg_0 -> this.lambda$bootstrap$0(thisClass, (MethodType)((Object)bridgeDescriptor), arg_0));
        if (this.config.debugGenerator) {
            BCUtil.printModel(ClassFile.of().parse(bytecode));
        }
        try {
            MethodHandles.Lookup wrapperLookup = this.config.lookup.defineHiddenClassWithClassData(bytecode, classData, true, new MethodHandles.Lookup.ClassOption[0]);
            return (UpcallBinder)wrapperLookup.lookupClass().getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (Error | RuntimeException e) {
            BCUtil.printModel(ClassFile.of().parse(bytecode));
            throw e;
        }
        catch (Exception e) {
            BCUtil.printModel(ClassFile.of().parse(bytecode));
            throw new RuntimeException(e);
        }
    }

    private List<Object> getClassData(Object method) {
        ArrayList<Object> list = new ArrayList<Object>(4);
        list.add(this.descriptor);
        list.add(method);
        if ((this.featureFlags & Integer.MIN_VALUE) != 0) {
            list.add(this.descriptor.returnLayout().orElseThrow());
        }
        if (BCCall.FeatureFlag.FF_BINDER.isSet(this.featureFlags)) {
            this.featureFlagOffsets[BCCall.FeatureFlag.FF_BINDER.ordinal()] = list.size();
            for (Class clazz : this.binders.sequencedKeySet()) {
                list.add(this.config.binders.get(clazz).binder());
            }
        }
        return list;
    }

    private static GroupLayout groupLayoutFrom(FFIType groupType) {
        MemorySegment element;
        MemorySegment elements = MemorySegment.ofAddress(groupType.address() + (long)FFIType.ELEMENTS).reinterpret(ValueLayout.ADDRESS.byteSize()).get(ValueLayout.ADDRESS, 0L).reinterpret(Long.MAX_VALUE);
        ArrayList<MemoryLayout> members = new ArrayList<MemoryLayout>();
        int index = 0;
        while (!MemorySegment.NULL.equals(element = elements.getAtIndex(ValueLayout.ADDRESS, (long)index++))) {
            FFIType elementType = FFIType.create(element.address());
            members.add(BCCallUp.memoryLayoutFrom(elementType));
        }
        return MemoryLayout.structLayout((MemoryLayout[])members.toArray(MemoryLayout[]::new));
    }

    private static MemoryLayout memoryLayoutFrom(FFIType type) {
        return switch (type.type()) {
            case 5, 6 -> ValueLayout.JAVA_BYTE;
            case 7, 8 -> ValueLayout.JAVA_SHORT;
            case 1, 9, 10 -> ValueLayout.JAVA_INT;
            case 11, 12 -> ValueLayout.JAVA_LONG;
            case 2 -> ValueLayout.JAVA_FLOAT;
            case 3 -> ValueLayout.JAVA_DOUBLE;
            case 13 -> BCCallUp.groupLayoutFrom(type);
            case 14 -> ValueLayout.ADDRESS;
            default -> throw new IllegalStateException("Unsupported libffi type: " + type.type());
        };
    }

    private /* synthetic */ void lambda$bootstrap$0(ClassDesc thisClass, MethodType bridgeDescriptor, ClassBuilder classBuilder) {
        BCUtil.startHiddenClass(classBuilder).withInterfaceSymbols(BCDescriptors.CD_UpcallBinder);
        classBuilder.withField("DESCRIPTOR", BCDescriptors.CD_FunctionDescriptor, 26).withField("HANDLE", ConstantDescs.CD_MethodHandle, 26).withMethod("<clinit>", ConstantDescs.MTD_void, 8, mb -> mb.withCode(cb -> {
            cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_FunctionDescriptor, 0)).putstatic(thisClass, "DESCRIPTOR", BCDescriptors.CD_FunctionDescriptor);
            if (this.featureFlags == 0) {
                cb.ldc(BCUtil.condyCDataAt(ConstantDescs.CD_MethodHandle, 1));
            } else {
                cb.invokestatic(ConstantDescs.CD_MethodHandles, "lookup", BCDescriptors.MTD_MethodHandles$Lookup).ldc(cb.constantPool().classEntry(thisClass)).ldc((ConstantDesc)((Object)"bridge")).ldc(BCUtil.condyCDataAt(ConstantDescs.CD_MethodType, 1)).invokevirtual(ConstantDescs.CD_MethodHandles_Lookup, "findStatic", BCDescriptors.MTD_MethodHandle_Class_String_MethodType);
            }
            cb.putstatic(thisClass, "HANDLE", ConstantDescs.CD_MethodHandle).return_();
        })).withMethod("descriptor", BCDescriptors.MTD_FunctionDescriptor, 17, mb -> mb.withCode(cb -> cb.getstatic(thisClass, "DESCRIPTOR", BCDescriptors.CD_FunctionDescriptor).areturn())).withMethod("handle", BCDescriptors.MTD_MethodHandle, 17, mb -> mb.withCode(cb -> cb.getstatic(thisClass, "HANDLE", ConstantDescs.CD_MethodHandle).areturn()));
        classBuilder.withMethod("stack", BCDescriptors.MTD_MemoryLayout, 17, mb -> mb.withCode(cb -> {
            if ((this.featureFlags & Integer.MIN_VALUE) == 0) {
                cb.aconst_null().areturn();
            } else {
                cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_MemoryLayout, 2)).areturn();
            }
        }));
        if (this.featureFlags != 0) {
            MethodTypeDesc methodTypeDesc = BCUtil.getMethodTypeDesc(this.method);
            classBuilder.withMethod("bridge", bridgeDescriptor.describeConstable().orElseThrow(), 9, mb -> mb.withCode(cb -> {
                cb.aload(cb.parameterSlot(0));
                int paramOffset = (this.featureFlags & Integer.MIN_VALUE) == 0 ? 1 : 2;
                for (int p = 0; p < methodTypeDesc.parameterCount(); ++p) {
                    Parameter parameter = this.parameters[p];
                    Class<?> type = parameter.getType();
                    if (Struct.class.isAssignableFrom(type)) continue;
                    int slot = cb.parameterSlot(p + paramOffset);
                    if (type == String.class) {
                        cb.aload(slot);
                        if (BCUtil.isNullable(this.config, parameter)) {
                            cb.invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long).lconst_0().lcmp().ifThenElse(Opcode.IFEQ, CodeBuilder::aconst_null, bcb -> BCUtil.buildGetString(bcb.aload(slot), this.method));
                            continue;
                        }
                        BCUtil.buildGetString(cb, this.method);
                        continue;
                    }
                    if (type == Boolean.TYPE) {
                        cb.iload(slot);
                        FFMBooleanInt booleanInt = parameter.getAnnotation(FFMBooleanInt.class);
                        if (booleanInt == null || booleanInt.binary()) continue;
                        cb.ifThenElse(Opcode.IFEQ, CodeBuilder::iconst_0, CodeBuilder::iconst_1);
                        continue;
                    }
                    if (BITS32 && type == Long.TYPE && parameter.isAnnotationPresent(FFMPointer.class)) {
                        cb.iload(slot);
                        BCUtil.buildPointer32to64(cb);
                        continue;
                    }
                    if (this.needsBinder(type)) {
                        cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_GroupBinder, this.featureFlagOffsets[BCCall.FeatureFlag.FF_BINDER.ordinal()] + this.binders.get(type))).swap().invokeinterface(BCDescriptors.CD_GroupBinder, "get", BCDescriptors.MTD_Object_MemorySegment);
                        continue;
                    }
                    cb.loadLocal(TypeKind.from(methodTypeDesc.parameterType(p)), slot);
                }
                if ((this.featureFlags & Integer.MIN_VALUE) != 0) {
                    Class<?> groupType = this.parameters[this.parameters.length - 1].getType();
                    ClassDesc groupDesc = groupType.describeConstable().orElseThrow();
                    cb.aload(cb.parameterSlot(1)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long).invokestatic(groupDesc, "create", MethodTypeDesc.of(groupDesc, ConstantDescs.CD_long));
                }
                cb.invokeinterface(this.upcallInterface.describeConstable().orElseThrow(), this.method.getName(), methodTypeDesc);
                if ((this.featureFlags & Integer.MIN_VALUE) != 0) {
                    cb.aload(cb.parameterSlot(1));
                } else {
                    Class<?> type = this.method.getReturnType();
                    if (type != Void.TYPE) {
                        if (BITS32 && type == Long.TYPE && this.method.isAnnotationPresent(FFMPointer.class)) {
                            BCUtil.buildPointer64to32(cb);
                        } else if (this.needsBinder(type)) {
                            cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_GroupBinder, this.featureFlagOffsets[BCCall.FeatureFlag.FF_BINDER.ordinal()] + this.binders.get(type))).swap().invokeinterface(BCDescriptors.CD_GroupBinder, "asSegment", BCDescriptors.MTD_MemorySegment_Object);
                        }
                    }
                }
                cb.return_(TypeKind.from(bridgeDescriptor.returnType()));
            }));
        }
    }
}

