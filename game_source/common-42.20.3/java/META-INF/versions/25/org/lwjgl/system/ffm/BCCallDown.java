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
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.ffm.BCCall;
import org.lwjgl.system.ffm.BCDescriptors;
import org.lwjgl.system.ffm.BCReturnTransform;
import org.lwjgl.system.ffm.BCUtil;
import org.lwjgl.system.ffm.Binder;
import org.lwjgl.system.ffm.FFM;
import org.lwjgl.system.ffm.FFMBooleanInt;
import org.lwjgl.system.ffm.FFMByValue;
import org.lwjgl.system.ffm.FFMCaptureCallState;
import org.lwjgl.system.ffm.FFMConfig;
import org.lwjgl.system.ffm.FFMCritical;
import org.lwjgl.system.ffm.FFMFirstVariadicArg;
import org.lwjgl.system.ffm.FFMFunctionAddress;
import org.lwjgl.system.ffm.FFMJNI;
import org.lwjgl.system.ffm.FFMPointer;
import org.lwjgl.system.ffm.FFMReturn;
import org.lwjgl.system.ffm.GroupBinder;
import org.lwjgl.system.ffm.StackAllocator;
import org.lwjgl.system.ffm.UpcallBinder;

final class BCCallDown
extends BCCall {
    private final Method method;
    private final Parameter[] parameters;
    private final boolean hasFunctionAddress;
    private final @Nullable String nativeName;
    private final int featureFlags;
    private final int[] featureFlagOffsets;
    private final LinkedHashMap<Class<?>, Integer> binders;
    private final @Nullable Class<?> allocatorClass;
    private final @Nullable Linker.Option captureCallState;
    private final @Nullable Linker.Option firstVariadicArg;
    private final FunctionDescriptor descriptor;
    private final MethodHandle ffm;

    BCCallDown(FFMConfig config, Method method) {
        super(config);
        Binder<?> binder;
        this.method = method;
        this.parameters = method.getParameters();
        this.hasFunctionAddress = this.hasFunctionAddress();
        this.nativeName = this.hasFunctionAddress ? null : BCUtil.getNativeName(method);
        this.featureFlagOffsets = new int[BCCall.FeatureFlag.FF_LAST.ordinal()];
        this.binders = new LinkedHashMap();
        Class<?> allocatorClass = null;
        Linker.Option captureCallState = null;
        Linker.Option firstVariadicArg = method.isAnnotationPresent(FFMFirstVariadicArg.class) ? Linker.Option.firstVariadicArg(method.getAnnotation(FFMFirstVariadicArg.class).value()) : null;
        boolean hasTracing = config.traceConsumer != null && (config.tracingFilter == null || config.tracingFilter.test(method));
        int featureFlags = hasTracing ? BCCall.FeatureFlag.FF_TRACING.mask : 0;
        ArrayList<MemoryLayout> argLayouts = new ArrayList<MemoryLayout>(this.parameters.length);
        if (this.hasJNI()) {
            argLayouts.add(ValueLayout.ADDRESS);
            argLayouts.add(ValueLayout.ADDRESS);
            featureFlags |= BCCall.FeatureFlag.FF_JNI.mask;
        }
        for (int i = 0; i < this.parameters.length; ++i) {
            Parameter parameter = this.parameters[i];
            if (parameter.getType() == MemorySegment.class) {
                if (i == 0 && this.hasFunctionAddress) {
                    if (!Checks.DEBUG || !Arrays.stream(parameter.getAnnotations()).anyMatch(it -> "org.lwjgl.system.ffm".equals(it.annotationType().getPackage().getName()))) continue;
                    throw new IllegalStateException("FFMFunctionAddress parameters cannot have FFM annotations.");
                }
                FFMCaptureCallState ccs = parameter.getAnnotation(FFMCaptureCallState.class);
                if (ccs != null) {
                    if (i != (this.hasFunctionAddress ? 1 : 0) + (allocatorClass != null ? 1 : 0)) {
                        throw new IllegalStateException("Invalid position of FFMCaptureCallState parameter.");
                    }
                    captureCallState = Linker.Option.captureCallState(ccs.value());
                    continue;
                }
            } else if (i == 0 && this.hasFunctionAddress) {
                throw new IllegalStateException("Missing FFMFunctionAddress parameter.");
            }
            if (parameter.isAnnotationPresent(FFMFirstVariadicArg.class)) {
                if (firstVariadicArg != null) {
                    throw new IllegalStateException("Multiple FFMFirstVariadicArg annotations found.");
                }
                firstVariadicArg = Linker.Option.firstVariadicArg(i);
            }
            if (SegmentAllocator.class.isAssignableFrom(parameter.getType())) {
                if (i != (this.hasFunctionAddress ? 1 : 0)) {
                    throw new IllegalStateException("Invalid position of SegmentAllocator/Arena parameter.");
                }
                allocatorClass = parameter.getType();
                continue;
            }
            Class<?> type = parameter.getType();
            if (BCUtil.isPointerType(parameter, type)) {
                if (config.checks && !BCUtil.isNullable(config, parameter)) {
                    featureFlags |= BCCall.FeatureFlag.FF_CHECK.mask;
                }
                if (BITS32 && type == Long.TYPE) {
                    featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
                }
            } else if (type == String.class) {
                featureFlags |= BCCall.FeatureFlag.FF_STACK.mask;
            } else if (type == Boolean.TYPE) {
                if (parameter.isAnnotationPresent(FFMBooleanInt.class)) {
                    featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
                }
            } else if (this.needsBinder(type)) {
                binder = config.binders.get(type).binder();
                if (allocatorClass == null && binder instanceof UpcallBinder) {
                    throw new IllegalStateException("An Arena parameter is required for parameter #" + i + ": " + String.valueOf(type));
                }
                this.binders.putIfAbsent(type, this.binders.size());
                featureFlags |= BCCall.FeatureFlag.FF_BINDER.mask;
                if (binder instanceof GroupBinder) {
                    GroupBinder groupBinder = (GroupBinder)binder;
                    MemoryLayout groupLayout = groupBinder.layout();
                    argLayouts.add(parameter.isAnnotationPresent(FFMByValue.class) ? groupLayout : ValueLayout.ADDRESS.withTargetLayout(groupLayout));
                    continue;
                }
                argLayouts.add(ValueLayout.ADDRESS);
                continue;
            }
            argLayouts.add(BCCallDown.valueLayout(parameter));
        }
        this.allocatorClass = allocatorClass;
        this.captureCallState = captureCallState;
        this.firstVariadicArg = firstVariadicArg;
        MemoryLayout resLayout = null;
        Class<?> type = method.getReturnType();
        if (type != Void.TYPE) {
            FFMReturn returnAnnotation = method.getAnnotation(FFMReturn.class);
            if (type == String.class || returnAnnotation != null) {
                featureFlags |= BCCall.FeatureFlag.FF_STACK.mask;
            } else if (type == Boolean.TYPE) {
                if (method.isAnnotationPresent(FFMBooleanInt.class)) {
                    featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
                }
            } else if (BITS32 && type == Long.TYPE && method.isAnnotationPresent(FFMPointer.class)) {
                featureFlags |= BCCall.FeatureFlag.FF_TYPE_CONVERSION.mask;
            }
            if (returnAnnotation != null) {
                FFMReturn.SizeOut returnOutputAnnotation = method.getAnnotation(FFMReturn.SizeOut.class);
                if (returnOutputAnnotation == null) {
                    for (Parameter parameter : this.parameters) {
                        if (!parameter.isAnnotationPresent(FFMReturn.Size.class)) continue;
                        resLayout = BCCallDown.valueLayout(parameter);
                    }
                }
                BCCallDown.injectReturnParameters(argLayouts, returnAnnotation, returnOutputAnnotation);
            } else if (this.needsBinder(type)) {
                binder = config.binders.get(type).binder();
                if (binder instanceof UpcallBinder) {
                    throw new IllegalStateException("Upcalls can only be returned as raw pointer values");
                }
                this.binders.putIfAbsent(type, this.binders.size());
                featureFlags |= BCCall.FeatureFlag.FF_BINDER.mask;
                MemoryLayout groupLayout = ((GroupBinder)binder).layout();
                if (method.isAnnotationPresent(FFMByValue.class)) {
                    if (allocatorClass == null) {
                        throw new IllegalStateException("A SegmentAllocator/Arena parameter is required for return value");
                    }
                    resLayout = groupLayout;
                    featureFlags |= BCCall.FeatureFlag.FF_BY_VALUE.mask;
                } else {
                    resLayout = ValueLayout.ADDRESS.withTargetLayout(groupLayout);
                }
            } else {
                resLayout = BCCallDown.valueLayout(method, type);
            }
        }
        this.featureFlags = featureFlags;
        MemoryLayout[] argLayoutsArray = (MemoryLayout[])argLayouts.toArray(MemoryLayout[]::new);
        this.descriptor = resLayout == null ? FunctionDescriptor.ofVoid(argLayoutsArray) : FunctionDescriptor.of(resLayout, argLayoutsArray);
        this.ffm = Linker.nativeLinker().downcallHandle(this.descriptor, this.createOptions());
    }

    private boolean hasFunctionAddress() {
        return this.method.getAnnotation(FFMFunctionAddress.class) != null || this.method.getDeclaringClass().getAnnotation(FFMFunctionAddress.class) != null;
    }

    private boolean hasJNI() {
        return this.method.getAnnotation(FFMJNI.class) != null || this.method.getDeclaringClass().getAnnotation(FFMJNI.class) != null;
    }

    private static void injectReturnParameters(ArrayList<MemoryLayout> argLayouts, FFMReturn returnAnnotation, @Nullable FFMReturn.SizeOut returnSizeOutAnnotation) {
        if (returnSizeOutAnnotation != null && returnSizeOutAnnotation.value() < returnAnnotation.value()) {
            BCCallDown.injectReturnParameter(argLayouts, returnSizeOutAnnotation.value(), "Invalid @FFMReturn.SizeOut parameter index: ");
        }
        BCCallDown.injectReturnParameter(argLayouts, returnAnnotation.value(), "Invalid return parameter index: ");
        if (returnSizeOutAnnotation != null && returnAnnotation.value() < returnSizeOutAnnotation.value()) {
            BCCallDown.injectReturnParameter(argLayouts, returnSizeOutAnnotation.value(), "Invalid @FFMReturn.SizeOut parameter index: ");
        }
    }

    private static void injectReturnParameter(ArrayList<MemoryLayout> argLayouts, int injectIndex, String errorMessage) {
        if (injectIndex < 0 || argLayouts.size() < injectIndex) {
            throw new IllegalArgumentException(errorMessage + injectIndex);
        }
        argLayouts.add(injectIndex, FFM.C_POINTER);
    }

    private Linker.Option[] createOptions() {
        ArrayList<Linker.Option> options = new ArrayList<Linker.Option>(2);
        this.addCritical(options);
        if (this.captureCallState != null) {
            options.add(this.captureCallState);
        }
        if (this.firstVariadicArg != null) {
            options.add(this.firstVariadicArg);
        }
        if (options.isEmpty()) {
            return BCUtil.EMPTY_OPTIONS;
        }
        return (Linker.Option[])options.toArray(Linker.Option[]::new);
    }

    private void addCritical(ArrayList<Linker.Option> options) {
        Boolean override;
        Boolean bl = override = this.config.criticalOverride == null ? null : this.config.criticalOverride.apply(this.method);
        if (override != null && !override.booleanValue()) {
            return;
        }
        FFMCritical annotation = this.method.getAnnotation(FFMCritical.class);
        if (annotation == null) {
            annotation = this.method.getDeclaringClass().getAnnotation(FFMCritical.class);
        }
        if (override != null || annotation != null) {
            options.add(Linker.Option.critical(annotation != null && annotation.value()));
        }
    }

    MethodHandle bootstrap() {
        if (this.config.debugGenerator) {
            BCCallDown.printDebug(this.method, this.parameters, this.descriptor);
        }
        if (this.featureFlags != 0) {
            return this.bootstrapWrapper();
        }
        return this.hasFunctionAddress ? this.ffm : this.ffm.bindTo(Objects.requireNonNull(this.config.symbolLookup).find(this.nativeName).orElseThrow());
    }

    private MethodHandle bootstrapWrapper() {
        if (this.config.debugGenerator) {
            APIUtil.apiLog("\t-> generating wrapper method");
        }
        List<Object> classData = this.getClassData();
        ClassDesc thisClass = BCCallDown.getClassDescWrapper(this.method);
        byte[] bytecode = ClassFile.of().build(thisClass, classBuilder -> {
            BCUtil.startHiddenClass(classBuilder);
            MethodTypeDesc nativeMethodTypeDesc = this.ffm.type().describeConstable().orElseThrow();
            if (this.hasFeature(BCCall.FeatureFlag.FF_TRACING)) {
                this.trace((ClassBuilder)classBuilder, nativeMethodTypeDesc);
            }
            MethodTypeDesc methodTypeDesc = BCUtil.getMethodTypeDesc(this.method);
            classBuilder.withMethod(this.method.getName(), methodTypeDesc, 9, mb -> mb.withCode(cb -> {
                int virtualParameterCount = this.getVirtualParameterCount();
                if (this.config.checks) {
                    for (int p = virtualParameterCount; p < methodTypeDesc.parameterCount(); ++p) {
                        Opcode ifThenOpcode;
                        Parameter parameter = this.parameters[p];
                        Class<?> type = parameter.getType();
                        if (!BCUtil.isPointerType(parameter, type) && !this.needsBinder(type) || BCUtil.isNullable(this.config, parameter)) continue;
                        int slot = cb.parameterSlot(p);
                        if (type == MemorySegment.class) {
                            cb.getstatic(BCDescriptors.CD_MemorySegment, "NULL", BCDescriptors.CD_MemorySegment).aload(slot).invokeinterface(BCDescriptors.CD_MemorySegment, "equals", BCDescriptors.MTD_boolean_Object);
                            ifThenOpcode = Opcode.IFNE;
                        } else if (type == Long.TYPE) {
                            cb.lconst_0().lload(slot).lcmp();
                            ifThenOpcode = Opcode.IFEQ;
                        } else if (this.needsBinder(type)) {
                            cb.aload(slot);
                            ifThenOpcode = Opcode.IFNULL;
                        } else {
                            throw new UnsupportedOperationException();
                        }
                        String exceptionText = BCUtil.getExceptionTextNULL(parameter, p - virtualParameterCount);
                        cb.ifThen(ifThenOpcode, thenHandler -> thenHandler.new_(BCDescriptors.CD_IllegalArgumentException).dup().ldc((ConstantDesc)((Object)exceptionText)).invokespecial(BCDescriptors.CD_IllegalArgumentException, "<init>", BCDescriptors.MTD_void_String).athrow());
                    }
                }
                int allocatorSlot = this.hasFeature(BCCall.FeatureFlag.FF_STACK) ? this.getStackSlot((CodeBuilder)cb) : -1;
                this.buildMethodBody((CodeBuilder)cb, methodTypeDesc, allocatorSlot, bcb -> {
                    BCReturnTransform returnTransform = null;
                    FFMReturn returnAnnotation = this.method.getAnnotation(FFMReturn.class);
                    if (returnAnnotation != null) {
                        returnTransform = BCReturnTransform.create(bcb, methodTypeDesc, this.method, this.parameters, returnAnnotation, allocatorSlot);
                    }
                    if (!this.hasFeature(BCCall.FeatureFlag.FF_TRACING)) {
                        cb.ldc(BCUtil.condyCDataAt(ConstantDescs.CD_MethodHandle, 0));
                    }
                    if (!this.hasFunctionAddress) {
                        cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_MemorySegment, 1));
                    }
                    if (this.hasJNI()) {
                        cb.getstatic(BCDescriptors.CD_MemorySegment, "NULL", BCDescriptors.CD_MemorySegment).getstatic(BCDescriptors.CD_MemorySegment, "NULL", BCDescriptors.CD_MemorySegment);
                    }
                    for (int p = 0; p < methodTypeDesc.parameterCount(); ++p) {
                        Parameter parameter = this.parameters[p];
                        Class<?> type = parameter.getType();
                        int slot = bcb.parameterSlot(p);
                        if (SegmentAllocator.class.isAssignableFrom(type)) {
                            if (!this.hasFeature(BCCall.FeatureFlag.FF_BY_VALUE)) continue;
                            bcb.aload(slot);
                            continue;
                        }
                        if (returnTransform != null) {
                            returnTransform.loadParameters((CodeBuilder)bcb, virtualParameterCount, p);
                        }
                        if (type == String.class) {
                            if (BCUtil.isNullable(this.config, parameter)) {
                                bcb.aload(slot).ifThenElse(Opcode.IFNULL, thenHandler -> thenHandler.getstatic(BCDescriptors.CD_MemorySegment, "NULL", BCDescriptors.CD_MemorySegment), elseHandler -> BCCallDown.buildAllocateFrom(elseHandler, allocatorSlot, slot, parameter));
                                continue;
                            }
                            BCCallDown.buildAllocateFrom(bcb, allocatorSlot, slot, parameter);
                            continue;
                        }
                        if (BITS32 && type == Long.TYPE && parameter.isAnnotationPresent(FFMPointer.class)) {
                            bcb.lload(slot);
                            BCUtil.buildPointer64to32(bcb);
                            continue;
                        }
                        if (this.needsBinder(type)) {
                            Binder<?> selector0$temp;
                            Binder<?> binder = this.config.binders.get(type).binder();
                            Objects.requireNonNull(binder);
                            int index$1 = 0;
                            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{GroupBinder.class, UpcallBinder.class}, selector0$temp, index$1)) {
                                case 0: {
                                    if (BCUtil.isNullable(this.config, parameter)) {
                                        bcb.aload(slot).ifThenElse(Opcode.IFNULL, b0 -> b0.getstatic(BCDescriptors.CD_MemorySegment, "NULL", BCDescriptors.CD_MemorySegment), b1 -> this.buildGroupAsSegment(b1, type, slot));
                                        break;
                                    }
                                    this.buildGroupAsSegment(bcb, type, slot);
                                    break;
                                }
                                case 1: {
                                    if (BCUtil.isNullable(this.config, parameter)) {
                                        bcb.aload(slot).ifThenElse(Opcode.IFNULL, b0 -> b0.getstatic(BCDescriptors.CD_MemorySegment, "NULL", BCDescriptors.CD_MemorySegment), b1 -> this.buildUpcallBinderAllocation(b1, type, slot));
                                        break;
                                    }
                                    this.buildUpcallBinderAllocation(bcb, type, slot);
                                    break;
                                }
                                default: {
                                    throw new IllegalStateException("Unsupported binder type: " + String.valueOf(binder.getClass()));
                                }
                            }
                            continue;
                        }
                        bcb.loadLocal(TypeKind.from(methodTypeDesc.parameterType(p)), slot);
                    }
                    if (returnTransform != null) {
                        returnTransform.loadParametersTail((CodeBuilder)bcb, virtualParameterCount, methodTypeDesc.parameterCount());
                    }
                    if (this.hasFeature(BCCall.FeatureFlag.FF_TRACING)) {
                        bcb.invokestatic(thisClass, "trace", nativeMethodTypeDesc);
                    } else {
                        bcb.invokevirtual(ConstantDescs.CD_MethodHandle, "invokeExact", nativeMethodTypeDesc);
                    }
                    Class<?> type = this.method.getReturnType();
                    if (type != Void.TYPE) {
                        if (returnTransform != null) {
                            returnTransform.buildResult((CodeBuilder)bcb, methodTypeDesc, this.method);
                        } else if (type == String.class) {
                            BCUtil.buildGetString(bcb, this.method);
                        } else if (type == Boolean.TYPE) {
                            FFMBooleanInt booleanInt = this.method.getAnnotation(FFMBooleanInt.class);
                            if (booleanInt != null && !booleanInt.binary()) {
                                bcb.ifThenElse(Opcode.IFEQ, CodeBuilder::iconst_0, CodeBuilder::iconst_1);
                            }
                        } else if (BITS32 && type == Long.TYPE && this.method.isAnnotationPresent(FFMPointer.class)) {
                            BCUtil.buildPointer32to64(bcb);
                        } else if (this.needsBinder(type)) {
                            bcb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_GroupBinder, this.featureFlagOffsets[BCCall.FeatureFlag.FF_BINDER.ordinal()] + this.binders.get(type))).swap().invokeinterface(BCDescriptors.CD_GroupBinder, "get", BCDescriptors.MTD_Object_MemorySegment);
                        }
                    }
                });
            }));
        });
        if (this.config.debugGenerator) {
            BCUtil.printModel(ClassFile.of().parse(bytecode));
        }
        try {
            MethodHandles.Lookup wrapperLookup = this.config.lookup.defineHiddenClassWithClassData(bytecode, classData, true, new MethodHandles.Lookup.ClassOption[0]);
            return wrapperLookup.findStatic(wrapperLookup.lookupClass(), this.method.getName(), MethodType.methodType(this.method.getReturnType(), this.method.getParameterTypes()));
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

    private void buildMethodBody(CodeBuilder cb, MethodTypeDesc methodTypeDesc, int allocatorSlot, Consumer<CodeBuilder> bodyHandler) {
        TypeKind returnTK = TypeKind.from(methodTypeDesc.returnType());
        if (allocatorSlot != -1 && (this.allocatorClass == null || StackAllocator.class.isAssignableFrom(this.allocatorClass))) {
            this.buildStackBody(cb, returnTK, allocatorSlot, bodyHandler);
        } else {
            bodyHandler.accept(cb);
        }
        cb.return_(returnTK);
    }

    private void buildStackBody(CodeBuilder cb, TypeKind returnTK, int stackSlot, Consumer<CodeBuilder> arenaTryHandler) {
        int returnSlot;
        int n = returnSlot = returnTK == TypeKind.VOID ? -1 : cb.allocateLocal(returnTK);
        if (this.allocatorClass == null || !StackAllocator.class.isAssignableFrom(this.allocatorClass)) {
            cb.invokestatic(BCDescriptors.CD_SegmentStack, "stackPush", BCDescriptors.MTD_SegmentStack).astore(stackSlot);
        } else {
            cb.aload(cb.parameterSlot(this.hasFunctionAddress ? 1 : 0)).invokeinterface(BCDescriptors.CD_StackAllocator, "push", BCDescriptors.MTD_StackAllocator).pop();
        }
        cb.trying(tryingHandler -> {
            arenaTryHandler.accept((CodeBuilder)tryingHandler);
            if (returnTK != TypeKind.VOID) {
                tryingHandler.storeLocal(returnTK, returnSlot);
            }
        }, catchesHandler -> catchesHandler.catchingAll(bcb0 -> bcb0.astore(stackSlot + 1).trying(finallyTryHandler -> finallyTryHandler.aload(stackSlot).invokeinterface(BCDescriptors.CD_StackAllocator, "pop", BCDescriptors.MTD_StackAllocator).pop(), suppressedCatchesHandler -> suppressedCatchesHandler.catchingAll(bcb1 -> bcb1.astore(stackSlot + 2).aload(stackSlot + 1).aload(stackSlot + 2).invokevirtual(ConstantDescs.CD_Throwable, "addSuppressed", BCDescriptors.MTD_void_Throwable))).aload(stackSlot + 1).athrow())).aload(stackSlot).invokeinterface(BCDescriptors.CD_StackAllocator, "pop", BCDescriptors.MTD_StackAllocator).pop();
        if (returnTK != TypeKind.VOID) {
            cb.loadLocal(returnTK, returnSlot);
        }
    }

    private void trace(ClassBuilder classBuilder, MethodTypeDesc nativeMethodTypeDesc) {
        classBuilder.withMethod("trace", nativeMethodTypeDesc, 10, mb -> mb.withCode(cb -> {
            int returnSlot;
            cb.ldc(BCUtil.condyCDataAt(ConstantDescs.CD_MethodHandle, 0));
            for (int p = 0; p < nativeMethodTypeDesc.parameterCount(); ++p) {
                cb.loadLocal(TypeKind.from(nativeMethodTypeDesc.parameterType(p)), cb.parameterSlot(p));
            }
            cb.invokevirtual(ConstantDescs.CD_MethodHandle, "invokeExact", nativeMethodTypeDesc);
            TypeKind returnTK = TypeKind.from(nativeMethodTypeDesc.returnType());
            if (returnTK != TypeKind.VOID) {
                returnSlot = cb.allocateLocal(returnTK);
                cb.storeLocal(returnTK, returnSlot);
            } else {
                returnSlot = -1;
            }
            int consumerIndex = this.featureFlagOffsets[BCCall.FeatureFlag.FF_TRACING.ordinal()];
            cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_TraceConsumer, consumerIndex)).ldc(BCUtil.condyCDataAt(BCDescriptors.CD_Method, consumerIndex + 1));
            if (returnTK == TypeKind.VOID) {
                cb.aconst_null();
            } else {
                cb.loadLocal(returnTK, returnSlot);
                if (returnTK != TypeKind.REFERENCE) {
                    BCCallDown.boxPrimitiveValue(cb, returnTK);
                }
            }
            cb.loadConstant(nativeMethodTypeDesc.parameterCount()).anewarray(ConstantDescs.CD_Object);
            for (int p = 0; p < nativeMethodTypeDesc.parameterCount(); ++p) {
                TypeKind tk = TypeKind.from(nativeMethodTypeDesc.parameterType(p));
                cb.dup().loadConstant(p).loadLocal(tk, cb.parameterSlot(p));
                if (tk != TypeKind.REFERENCE) {
                    BCCallDown.boxPrimitiveValue(cb, tk);
                }
                cb.aastore();
            }
            cb.invokeinterface(BCDescriptors.CD_TraceConsumer, "accept", BCDescriptors.MTD_void_Method_Object_ObjectArray);
            if (returnTK != TypeKind.VOID) {
                cb.loadLocal(returnTK, returnSlot);
            }
            cb.return_(returnTK);
        }));
    }

    private boolean hasFeature(BCCall.FeatureFlag flag) {
        return flag.isSet(this.featureFlags);
    }

    private int getVirtualParameterCount() {
        int index = 0;
        if (this.hasFunctionAddress) {
            ++index;
        }
        if (this.allocatorClass != null) {
            ++index;
        }
        if (this.captureCallState != null) {
            ++index;
        }
        return index;
    }

    private int getFirstNativeParameterIndex() {
        int index = 0;
        if (this.hasFunctionAddress) {
            ++index;
        }
        if (this.allocatorClass != null) {
            // empty if block
        }
        if (this.captureCallState != null) {
            ++index;
        }
        return index;
    }

    private int getStackSlot(CodeBuilder cb) {
        return this.allocatorClass != null ? cb.parameterSlot(this.hasFunctionAddress ? 1 : 0) : cb.allocateLocal(TypeKind.REFERENCE);
    }

    private int getUpcallArenaSlot(CodeBuilder cb) {
        if (this.allocatorClass == null || !Arena.class.isAssignableFrom(this.allocatorClass)) {
            throw new IllegalStateException("Allocating upcalls requires an Arena parameter");
        }
        return cb.parameterSlot(this.hasFunctionAddress ? 1 : 0);
    }

    private List<Object> getClassData() {
        ArrayList<Object> list = new ArrayList<Object>(5);
        list.add(this.ffm);
        if (!this.hasFunctionAddress) {
            SymbolLookup lookup = this.config.symbolLookup;
            if (lookup == null) {
                throw new IllegalStateException("The registered FFMConfig does not define a SymbolLookup.");
            }
            list.add(lookup.find(this.nativeName).orElseThrow(() -> new IllegalStateException("Failed to resolve native function: " + this.nativeName)));
        }
        if (this.hasFeature(BCCall.FeatureFlag.FF_TRACING)) {
            this.featureFlagOffsets[BCCall.FeatureFlag.FF_TRACING.ordinal()] = list.size();
            list.add(this.config.traceConsumer);
            list.add(this.method);
        }
        if (this.hasFeature(BCCall.FeatureFlag.FF_BINDER)) {
            this.featureFlagOffsets[BCCall.FeatureFlag.FF_BINDER.ordinal()] = list.size();
            for (Class clazz : this.binders.sequencedKeySet()) {
                list.add(this.config.binders.get(clazz).binder());
            }
        }
        return list;
    }

    private static <T extends CodeBuilder> T buildAllocateFrom(T cb, int allocatorSlot, int slot, Parameter parameter) {
        cb.aload(allocatorSlot).aload(slot);
        BCUtil.buildCharsetInstance(cb, BCUtil.getCharset(parameter)).invokeinterface(BCDescriptors.CD_SegmentAllocator, "allocateFrom", BCDescriptors.MTD_MemorySegment_String_Charset);
        return cb;
    }

    private <T extends CodeBuilder> T buildGroupAsSegment(T cb, Class<?> type, int parameterSlot) {
        cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_GroupBinder, this.featureFlagOffsets[BCCall.FeatureFlag.FF_BINDER.ordinal()] + this.binders.get(type))).aload(parameterSlot).invokeinterface(BCDescriptors.CD_GroupBinder, "asSegment", BCDescriptors.MTD_MemorySegment_Object);
        return cb;
    }

    private <T extends CodeBuilder> T buildUpcallBinderAllocation(T cb, Class<?> type, int parameterSlot) {
        cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_UpcallBinder, this.featureFlagOffsets[BCCall.FeatureFlag.FF_BINDER.ordinal()] + this.binders.get(type))).aload(this.getUpcallArenaSlot(cb)).aload(parameterSlot).invokeinterface(BCDescriptors.CD_UpcallBinder, "allocate", BCDescriptors.MTD_MemorySegment_Arena_Object);
        return cb;
    }
}

