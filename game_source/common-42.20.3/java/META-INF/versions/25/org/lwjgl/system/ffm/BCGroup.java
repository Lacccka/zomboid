/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.classfile.Attribute;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.RecordAttribute;
import java.lang.classfile.attribute.RecordComponentInfo;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.ffm.BCCall;
import org.lwjgl.system.ffm.BCDescriptors;
import org.lwjgl.system.ffm.BCUtil;
import org.lwjgl.system.ffm.Binder;
import org.lwjgl.system.ffm.FFM;
import org.lwjgl.system.ffm.FFMCanonical;
import org.lwjgl.system.ffm.FFMCharset;
import org.lwjgl.system.ffm.FFMConfig;
import org.lwjgl.system.ffm.FFMPointer;
import org.lwjgl.system.ffm.FFMSize;
import org.lwjgl.system.ffm.Group;
import org.lwjgl.system.ffm.GroupBinder;
import org.lwjgl.system.ffm.StructBinder;
import org.lwjgl.system.ffm.UnionBinder;

final class BCGroup {
    private static final Collector<CharSequence, ?, String> SEMI_COLON = Collectors.joining(";");
    private static final MethodHandle CHECK_ADDRESS;

    private BCGroup() {
    }

    private static RuntimeException memberException(String message, Class<?> groupInterface, String member) {
        return new IllegalStateException(String.format("%s (%s::%s)", message, groupInterface.getSimpleName(), member));
    }

    private static RuntimeException methodException(String message, Method method) {
        return new IllegalStateException(String.format("%s (%s::%s)", message, method.getDeclaringClass(), method.getName()));
    }

    private static MethodHandles.Lookup bootstrapImplementation(FFMConfig config, Class<?> groupInterface, GroupLayout layout, FFM.GroupBinderBuilder<?, ?, ?, ?> builder) {
        ClassDesc thisClass = ClassDesc.of(groupInterface.getPackageName(), groupInterface.getSimpleName() + "Impl");
        byte[] bytecode = ClassFile.of().build(thisClass, classBuilder -> {
            ClassDesc groupDesc = groupInterface.describeConstable().orElseThrow();
            classBuilder.withVersion(ClassFile.latestMajorVersion(), ClassFile.latestMinorVersion()).withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL).withSuperclass(BCDescriptors.CD_Record).withInterfaceSymbols(groupDesc).withField("address", ConstantDescs.CD_long, 18).withMethod("<init>", BCDescriptors.MTD_void_long, 0, mb -> mb.withCode(cb -> cb.aload(cb.receiverSlot()).lload(cb.parameterSlot(0)).putfield(thisClass, "address", ConstantDescs.CD_long).aload(cb.receiverSlot()).invokespecial(BCDescriptors.CD_Record, "<init>", ConstantDescs.MTD_void, false).return_()));
            SequencedMap<String, List<Method>> memberMap = BCGroup.compileMemberAccessors(groupInterface, layout);
            boolean hasPrivateGetters = builder.equals == null || builder.hashCode == null || builder.toString == null;
            LinkedHashMap<String, Method> getters = new LinkedHashMap<String, Method>(memberMap.size());
            for (Map.Entry member : memberMap.entrySet()) {
                String memberName = (String)member.getKey();
                List methods = (List)member.getValue();
                int getterCount = 0;
                int nonCanonicalCount = 0;
                for (Method method : methods) {
                    if (method.getParameterCount() != 0) continue;
                    if (method.getReturnType() == Void.TYPE) {
                        throw BCGroup.methodException("Group getter returns void", method);
                    }
                    ++getterCount;
                    nonCanonicalCount += BCGroup.registerCanonicalGetter(groupInterface, method, getters, memberName);
                    MethodTypeDesc descriptor = BCUtil.getMethodTypeDesc(method);
                    classBuilder.withMethod(method.getName(), descriptor, 17, mb -> mb.withCode(cb -> {
                        MemoryLayout.PathElement memberPath = MemoryLayout.PathElement.groupElement(memberName);
                        MemoryLayout memberLayout = Objects.requireNonNull(layout.select(memberPath));
                        long memberOffset = layout.byteOffset(memberPath);
                        Class<?> returnType = method.getReturnType();
                        if (returnType == Boolean.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset);
                            MemoryLayout memoryLayout = memberLayout;
                            Objects.requireNonNull(memoryLayout);
                            MemoryLayout selector0$temp = memoryLayout;
                            int index$1 = 0;
                            block22: while (true) {
                                switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ValueLayout.OfBoolean.class, ValueLayout.OfByte.class, ValueLayout.OfShort.class, ValueLayout.OfInt.class}, (MemoryLayout)selector0$temp, index$1)) {
                                    case 0: 
                                    case 1: {
                                        if (!(selector0$temp instanceof ValueLayout.OfBoolean) && !(selector0$temp instanceof ValueLayout.OfByte)) {
                                            index$1 = 2;
                                            continue block22;
                                        }
                                        cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetByte", BCDescriptors.MTD_byte_long);
                                        break block22;
                                    }
                                    case 2: {
                                        cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetShort", BCDescriptors.MTD_short_long);
                                        break block22;
                                    }
                                    case 3: {
                                        cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetInt", BCDescriptors.MTD_int_long);
                                        break block22;
                                    }
                                    default: {
                                        throw BCGroup.methodException("Unsupported boolean getter layout: " + String.valueOf(memberLayout), method);
                                    }
                                }
                                break;
                            }
                            cb.ireturn();
                        } else if (returnType == Byte.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).invokestatic(BCDescriptors.CD_MemoryUtil, "memGetByte", BCDescriptors.MTD_byte_long).ireturn();
                        } else if (returnType == Short.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).invokestatic(BCDescriptors.CD_MemoryUtil, "memGetShort", BCDescriptors.MTD_short_long).ireturn();
                        } else if (returnType == Integer.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).invokestatic(BCDescriptors.CD_MemoryUtil, "memGetInt", BCDescriptors.MTD_int_long).ireturn();
                        } else if (returnType == Long.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset);
                            MemoryLayout memoryLayout = memberLayout;
                            Objects.requireNonNull(memoryLayout);
                            MemoryLayout selector0$temp = memoryLayout;
                            int index$1 = 0;
                            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AddressLayout.class, ValueLayout.OfInt.class, ValueLayout.OfLong.class}, (MemoryLayout)selector0$temp, index$1)) {
                                case 0: {
                                    cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetAddress", BCDescriptors.MTD_long_long);
                                    break;
                                }
                                case 1: {
                                    cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetInt", BCDescriptors.MTD_int_long);
                                    if (method.isAnnotationPresent(FFMPointer.class)) {
                                        BCUtil.buildPointer32to64(cb);
                                        break;
                                    }
                                    cb.i2l();
                                    break;
                                }
                                case 2: {
                                    cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetLong", BCDescriptors.MTD_long_long);
                                    break;
                                }
                                default: {
                                    throw BCGroup.methodException("Unsupported long getter layout: " + String.valueOf(memberLayout), method);
                                }
                            }
                            cb.lreturn();
                        } else if (returnType == Float.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).invokestatic(BCDescriptors.CD_MemoryUtil, "memGetFloat", BCDescriptors.MTD_float_long).freturn();
                        } else if (returnType == Double.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).invokestatic(BCDescriptors.CD_MemoryUtil, "memGetDouble", BCDescriptors.MTD_double_long).dreturn();
                        } else if (returnType == MemorySegment.class) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset);
                            MemoryLayout memoryLayout = memberLayout;
                            Objects.requireNonNull(memoryLayout);
                            MemoryLayout selector0$temp = memoryLayout;
                            int index$1 = 0;
                            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AddressLayout.class, SequenceLayout.class}, (MemoryLayout)selector0$temp, index$1)) {
                                case 0: {
                                    AddressLayout addressLayout = (AddressLayout)selector0$temp;
                                    cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetAddress", BCDescriptors.MTD_long_long).dup2().invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).dup_x2().pop().lconst_0().lcmp().ifThen(Opcode.IFNE, bcb -> BCGroup.buildMemorySegmentReinterpret(bcb, groupDesc, memberMap, method, addressLayout));
                                    break;
                                }
                                case 1: {
                                    SequenceLayout sequenceLayout = (SequenceLayout)selector0$temp;
                                    cb.invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true);
                                    FFMSize autoSize = method.getAnnotation(FFMSize.class);
                                    if (autoSize != null) {
                                        BCGroup.buildAutoSize(cb, groupDesc, memberMap, method, autoSize, sequenceLayout.elementLayout()).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long);
                                        break;
                                    }
                                    cb.loadConstant(sequenceLayout.byteSize()).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long);
                                    break;
                                }
                                default: {
                                    throw BCGroup.methodException("Unsupported MemorySegment getter layout: " + String.valueOf(memberLayout), method);
                                }
                            }
                        } else if (returnType == String.class) {
                            FFMCharset.Type charset = BCUtil.getCharset(method);
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset);
                            MemoryLayout memoryLayout = memberLayout;
                            Objects.requireNonNull(memoryLayout);
                            MemoryLayout selector0$temp = memoryLayout;
                            int index$1 = 0;
                            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AddressLayout.class, SequenceLayout.class}, (MemoryLayout)selector0$temp, index$1)) {
                                case 0: {
                                    cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetAddress", BCDescriptors.MTD_long_long);
                                    if (BCUtil.isNullable(config, method)) {
                                        cb.dup2().lconst_0().lcmp().ifThenElse(Opcode.IFEQ, bcb -> bcb.pop2().aconst_null(), bcb -> BCGroup.buildStringGetter(bcb, groupDesc, memberMap, method, charset)).areturn();
                                        break;
                                    }
                                    BCGroup.buildNullPointerCheck(cb);
                                    BCGroup.buildStringGetter(cb, groupDesc, memberMap, method, charset);
                                    break;
                                }
                                case 1: {
                                    SequenceLayout sequenceLayout = (SequenceLayout)selector0$temp;
                                    cb.invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true);
                                    FFMSize autoSize = method.getAnnotation(FFMSize.class);
                                    if (autoSize != null) {
                                        int arraySlot = cb.allocateLocal(TypeKind.REFERENCE);
                                        BCGroup.buildAutoSize(cb, groupDesc, memberMap, method, autoSize, sequenceLayout.elementLayout()).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_BYTE", BCDescriptors.CD_ValueLayout$OfByte).invokeinterface(BCDescriptors.CD_MemorySegment, "toArray", BCDescriptors.MTD_byteArray_ValueLayout$OfByte).astore(arraySlot).new_(ConstantDescs.CD_String).dup().aload(arraySlot);
                                        BCUtil.buildCharsetInstance(cb, charset).invokespecial(ConstantDescs.CD_String, "<init>", BCDescriptors.MTD_void_byteArray_Charset);
                                        break;
                                    }
                                    cb.loadConstant(sequenceLayout.byteSize()).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).lconst_0();
                                    BCUtil.buildCharsetInstance(cb, charset).invokeinterface(BCDescriptors.CD_MemorySegment, "getString", BCDescriptors.MTD_String_long_Charset);
                                    break;
                                }
                                default: {
                                    throw BCGroup.methodException("Unsupported String getter layout: " + String.valueOf(memberLayout), method);
                                }
                            }
                        } else {
                            ClassDesc type;
                            String name;
                            if (returnType == groupInterface) {
                                name = builder.binderField.getName();
                                type = builder.kind().binderDesc();
                            } else {
                                FFMConfig.BinderField binderField = FFM.lookupBinder(config, returnType);
                                name = binderField.name();
                                type = BCGroup.groupDesc(binderField);
                            }
                            ClassDesc returnTypeDesc = returnType.describeConstable().orElseThrow();
                            MemoryLayout memoryLayout = memberLayout;
                            Objects.requireNonNull(memoryLayout);
                            MemoryLayout selector0$temp = memoryLayout;
                            int index$1 = 0;
                            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AddressLayout.class, GroupLayout.class}, (MemoryLayout)selector0$temp, index$1)) {
                                case 0: {
                                    cb.getstatic(returnTypeDesc, name, type);
                                    BCGroup.buildMemberAddress(cb, thisClass, memberOffset).invokestatic(BCDescriptors.CD_MemoryUtil, "memGetAddress", BCDescriptors.MTD_long_long).invokeinterface(BCDescriptors.CD_GroupBinder, BCUtil.isNullable(config, method) ? "ofAddressSafe" : "ofAddress", BCDescriptors.MTD_Object_long);
                                    break;
                                }
                                case 1: {
                                    if (Checks.DEBUG && BCUtil.isNullable(config, method)) {
                                        throw BCGroup.methodException("Nested group members cannot be nullable", method);
                                    }
                                    cb.getstatic(returnTypeDesc, name, type);
                                    BCGroup.buildMemberAddress(cb, thisClass, memberOffset).invokeinterface(BCDescriptors.CD_GroupBinder, "ofAddress", BCDescriptors.MTD_Object_long);
                                    break;
                                }
                                default: {
                                    throw BCGroup.methodException("Unsupported getter layout: " + String.valueOf(memberLayout), method);
                                }
                            }
                        }
                        cb.return_(TypeKind.from(descriptor.returnType()));
                    }));
                }
                if (!hasPrivateGetters || true >= getterCount || getterCount != nonCanonicalCount) continue;
                throw BCGroup.memberException("Failed to find canonical getter for layout member", groupInterface, memberName);
            }
            classBuilder.with(RecordAttribute.of((RecordComponentInfo[])getters.sequencedValues().stream().map(getter -> RecordComponentInfo.of(getter.getName(), getter.getReturnType().describeConstable().orElseThrow(), new Attribute[0])).toArray(RecordComponentInfo[]::new)));
            if (hasPrivateGetters) {
                for (Map.Entry member : getters.sequencedEntrySet()) {
                    Method method;
                    MemoryLayout.PathElement memberPath = MemoryLayout.PathElement.groupElement((String)member.getKey());
                    MemoryLayout memberLayout = layout.select(memberPath);
                    if (!(memberLayout instanceof AddressLayout) || (method = (Method)member.getValue()).getReturnType() == Long.TYPE) continue;
                    long memberOffset = layout.byteOffset(memberPath);
                    if (builder.equals == null || builder.hashCode == null) {
                        classBuilder.withMethod("__address__" + method.getName(), BCDescriptors.MTD_long, 18, mb -> mb.withCode(cb -> {
                            cb.aload(cb.receiverSlot()).getfield(thisClass, "address", ConstantDescs.CD_long);
                            if (memberOffset != 0L) {
                                cb.loadConstant(memberOffset).ladd();
                            }
                            cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetAddress", BCDescriptors.MTD_long_long).lreturn();
                        }));
                    }
                    if (builder.toString != null) continue;
                    classBuilder.withMethod("__toString__" + method.getName(), BCDescriptors.MTD_String, 18, mb -> mb.withCode(cb -> {
                        cb.aload(cb.receiverSlot()).getfield(thisClass, "address", ConstantDescs.CD_long);
                        if (memberOffset != 0L) {
                            cb.loadConstant(memberOffset).ladd();
                        }
                        cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memGetAddress", BCDescriptors.MTD_long_long).invokestatic(ConstantDescs.CD_Long, "toHexString", BCDescriptors.MTD_String_long).invokedynamic(BCDescriptors.DCSD_StringConcatFactory_makeConcatWithConstants_AddressToHexString).areturn();
                    }));
                }
            }
            for (Map.Entry member : memberMap.entrySet()) {
                for (Method method : (List)member.getValue()) {
                    if (method.getParameterCount() == 0) continue;
                    if (method.getParameterCount() != 1) {
                        throw BCGroup.methodException("Setter must accept a single parameter", method);
                    }
                    if (method.getReturnType() != method.getDeclaringClass()) {
                        throw BCGroup.methodException("Setter return type must be its declaring interface", method);
                    }
                    MethodTypeDesc descriptor = BCUtil.getMethodTypeDesc(method);
                    classBuilder.withMethod(method.getName(), descriptor, 17, mb -> mb.withCode(cb -> {
                        MemoryLayout.PathElement memberPath = MemoryLayout.PathElement.groupElement((String)member.getKey());
                        MemoryLayout memberLayout = layout.select(memberPath);
                        long memberOffset = layout.byteOffset(memberPath);
                        int param0 = cb.parameterSlot(0);
                        Parameter parameter = method.getParameters()[0];
                        Class<?> parameterType = parameter.getType();
                        if (parameterType == Boolean.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).iload(param0);
                            MemoryLayout memoryLayout = memberLayout;
                            Objects.requireNonNull(memoryLayout);
                            MemoryLayout selector0$temp = memoryLayout;
                            int index$1 = 0;
                            block14: while (true) {
                                switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ValueLayout.OfBoolean.class, ValueLayout.OfByte.class, ValueLayout.OfShort.class, ValueLayout.OfInt.class}, (MemoryLayout)selector0$temp, index$1)) {
                                    case 0: 
                                    case 1: {
                                        if (!(selector0$temp instanceof ValueLayout.OfBoolean) && !(selector0$temp instanceof ValueLayout.OfByte)) {
                                            index$1 = 2;
                                            continue block14;
                                        }
                                        cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memPutByte", BCDescriptors.MTD_void_long_byte);
                                        break block14;
                                    }
                                    case 2: {
                                        cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memPutShort", BCDescriptors.MTD_void_long_short);
                                        break block14;
                                    }
                                    case 3: {
                                        cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memPutInt", BCDescriptors.MTD_void_long_int);
                                        break block14;
                                    }
                                    default: {
                                        throw BCGroup.methodException("Unsupported boolean setter layout: " + String.valueOf(memberLayout), method);
                                    }
                                }
                                break;
                            }
                        } else if (parameterType == Byte.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).iload(param0).invokestatic(BCDescriptors.CD_MemoryUtil, "memPutByte", BCDescriptors.MTD_void_long_byte);
                        } else if (parameterType == Short.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).iload(param0).invokestatic(BCDescriptors.CD_MemoryUtil, "memPutShort", BCDescriptors.MTD_void_long_short);
                        } else if (parameterType == Integer.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).iload(param0).invokestatic(BCDescriptors.CD_MemoryUtil, "memPutInt", BCDescriptors.MTD_void_long_int);
                        } else if (parameterType == Long.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).lload(param0);
                            MemoryLayout memoryLayout = memberLayout;
                            Objects.requireNonNull(memoryLayout);
                            MemoryLayout selector0$temp = memoryLayout;
                            int index$1 = 0;
                            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AddressLayout.class, ValueLayout.OfInt.class, ValueLayout.OfLong.class}, (MemoryLayout)selector0$temp, index$1)) {
                                case 0: {
                                    cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memPutAddress", BCDescriptors.MTD_void_long_long);
                                    break;
                                }
                                case 1: {
                                    cb.l2i().invokestatic(BCDescriptors.CD_MemoryUtil, "memPutInt", BCDescriptors.MTD_void_long_int);
                                    break;
                                }
                                case 2: {
                                    cb.invokestatic(BCDescriptors.CD_MemoryUtil, "memPutLong", BCDescriptors.MTD_void_long_long);
                                    break;
                                }
                                default: {
                                    throw BCGroup.methodException("Unsupported long setter layout: " + String.valueOf(memberLayout), method);
                                }
                            }
                        } else if (parameterType == Float.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).fload(param0).invokestatic(BCDescriptors.CD_MemoryUtil, "memPutFloat", BCDescriptors.MTD_void_long_float);
                        } else if (parameterType == Double.TYPE) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).dload(param0).invokestatic(BCDescriptors.CD_MemoryUtil, "memPutDouble", BCDescriptors.MTD_void_long_double);
                        } else if (parameterType == MemorySegment.class) {
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).aload(param0).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long).invokestatic(BCDescriptors.CD_MemoryUtil, "memPutAddress", BCDescriptors.MTD_void_long_long);
                        } else if (parameterType == String.class) {
                            if (!(memberLayout instanceof SequenceLayout)) {
                                throw BCGroup.methodException("Unsupported String setter layout: " + String.valueOf(memberLayout), method);
                            }
                            SequenceLayout sequenceLayout = (SequenceLayout)memberLayout;
                            FFMCharset.Type charset = BCUtil.getCharset(method);
                            BCGroup.buildMemberAddress(cb, thisClass, memberOffset).invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(sequenceLayout.byteSize()).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long);
                            if (parameter.isAnnotationPresent(FFMSize.class)) {
                                int segment = cb.allocateLocal(TypeKind.REFERENCE);
                                int array = cb.allocateLocal(TypeKind.REFERENCE);
                                cb.astore(segment).aload(param0);
                                BCUtil.buildCharsetInstance(cb, charset).invokevirtual(ConstantDescs.CD_String, "getBytes", BCDescriptors.MTD_byteArray_Charset).dup().astore(array).iconst_0().aload(segment).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_BYTE", BCDescriptors.CD_ValueLayout$OfByte).lconst_0().aload(array).arraylength().invokestatic(BCDescriptors.CD_MemorySegment, "copy", BCDescriptors.MTD_void_Object_int_MemorySegment_ValueLayout_long_int, true);
                            } else {
                                cb.lconst_0().aload(param0);
                                BCUtil.buildCharsetInstance(cb, charset).invokeinterface(BCDescriptors.CD_MemorySegment, "setString", BCDescriptors.MTD_void_long_String_Charset);
                            }
                        } else {
                            ClassDesc type;
                            String name;
                            if (parameterType == groupInterface) {
                                name = builder.binderField.getName();
                                type = builder.kind().binderDesc();
                            } else {
                                FFMConfig.BinderField binderField = FFM.lookupBinder(config, parameterType);
                                name = binderField.name();
                                type = BCGroup.groupDesc(binderField);
                            }
                            ClassDesc parameterTypeDesc = parameterType.describeConstable().orElseThrow();
                            MemoryLayout memoryLayout = memberLayout;
                            Objects.requireNonNull(memoryLayout);
                            MemoryLayout selector0$temp = memoryLayout;
                            int index$1 = 0;
                            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AddressLayout.class, GroupLayout.class}, (MemoryLayout)selector0$temp, index$1)) {
                                case 0: {
                                    BCGroup.buildMemberAddress(cb, thisClass, memberOffset).getstatic(parameterTypeDesc, name, type).aload(param0).invokeinterface(BCDescriptors.CD_GroupBinder, BCUtil.isNullable(config, parameter) ? "addressOfSafe" : "addressOf", BCDescriptors.MTD_long_Object).invokestatic(BCDescriptors.CD_MemoryUtil, "memPutAddress", BCDescriptors.MTD_void_long_long);
                                    break;
                                }
                                case 1: {
                                    if (Checks.DEBUG && BCUtil.isNullable(config, parameter)) {
                                        throw BCGroup.methodException("Nested group members cannot be nullable", method);
                                    }
                                    cb.getstatic(parameterTypeDesc, name, type).dup();
                                    BCGroup.buildMemberAddress(cb, thisClass, memberOffset).invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).invokeinterface(BCDescriptors.CD_GroupBinder, "reinterpret", BCDescriptors.MTD_MemorySegment_MemorySegment).aload(param0).invokeinterface(BCDescriptors.CD_GroupBinder, "set", BCDescriptors.MTD_GroupBinder_MemorySegment_Object).pop();
                                    break;
                                }
                                default: {
                                    throw BCGroup.methodException("Unsupported setter layout: " + String.valueOf(memberLayout), method);
                                }
                            }
                        }
                        cb.aload(cb.receiverSlot()).areturn();
                    }));
                }
            }
            String[] bootstrapArgs = hasPrivateGetters ? BCGroup.getBootstrapArgs(layout, getters) : null;
            classBuilder.withMethod("equals", BCDescriptors.MTD_boolean_Object, 17, mb -> mb.withCode(cb -> {
                int receiverSlot = cb.receiverSlot();
                int param0Slot = cb.parameterSlot(0);
                if (builder.equals == null) {
                    Objects.requireNonNull(bootstrapArgs);
                    cb.aload(receiverSlot).aload(param0Slot).invokedynamic(DynamicCallSiteDesc.of(BCDescriptors.DMHD_GroupBinder_bootstrapRecord, "equals", MethodTypeDesc.of(ConstantDescs.CD_boolean, groupDesc, ConstantDescs.CD_Object), (ConstantDesc[])bootstrapArgs));
                } else {
                    cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_BiPredicate, 1)).aload(receiverSlot).aload(param0Slot).invokeinterface(BCDescriptors.CD_BiPredicate, "test", BCDescriptors.MTD_boolean_Object_Object);
                }
                cb.ireturn();
            })).withMethod("hashCode", BCDescriptors.MTD_int, 17, mb -> mb.withCode(cb -> {
                int receiverSlot = cb.receiverSlot();
                if (builder.hashCode == null) {
                    Objects.requireNonNull(bootstrapArgs);
                    cb.aload(receiverSlot).invokedynamic(DynamicCallSiteDesc.of(BCDescriptors.DMHD_GroupBinder_bootstrapRecord, "hashCode", MethodTypeDesc.of(ConstantDescs.CD_int, groupDesc), (ConstantDesc[])bootstrapArgs));
                } else {
                    cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_ToIntFunction, 2)).aload(receiverSlot).invokeinterface(BCDescriptors.CD_ToIntFunction, "applyAsInt", BCDescriptors.MTD_int_Object);
                }
                cb.ireturn();
            })).withMethod("toString", BCDescriptors.MTD_String, 17, mb -> mb.withCode(cb -> {
                int receiverSlot = cb.receiverSlot();
                if (builder.toString == null) {
                    Objects.requireNonNull(bootstrapArgs);
                    for (int i = 0; i < bootstrapArgs.length; ++i) {
                        if (!bootstrapArgs[i].startsWith("__address__")) continue;
                        bootstrapArgs[i] = "__toString__" + bootstrapArgs[i].substring(11);
                    }
                    cb.aload(receiverSlot).invokedynamic(DynamicCallSiteDesc.of(BCDescriptors.DMHD_GroupBinder_bootstrapRecord, "toString", MethodTypeDesc.of(ConstantDescs.CD_String, groupDesc), (ConstantDesc[])bootstrapArgs));
                } else {
                    cb.ldc(BCUtil.condyCDataAt(BCDescriptors.CD_Function, 3)).aload(receiverSlot).invokeinterface(BCDescriptors.CD_Function, "apply", BCDescriptors.MTD_Object_Object).checkcast(ConstantDescs.CD_String);
                }
                cb.areturn();
            }));
            if (Group.class.isAssignableFrom(groupInterface)) {
                ClassDesc layoutDesc = layout instanceof StructLayout ? BCDescriptors.CD_StructLayout : BCDescriptors.CD_UnionLayout;
                classBuilder.withMethod("layout", BCDescriptors.MTD_GroupLayout, 17, mb -> mb.withCode(cb -> cb.ldc(BCUtil.condyCDataAt(layoutDesc, 0)).areturn())).withMethod("copyFrom", BCDescriptors.MTD_Group_Group, 17, mb -> mb.withCode(cb -> BCGroup.buildCopy(cb, layout, rcb -> rcb.aload(rcb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_Group, "address", BCDescriptors.MTD_long), rcb -> rcb.aload(rcb.receiverSlot()).getfield(thisClass, "address", ConstantDescs.CD_long), rcb -> rcb.aload(rcb.receiverSlot())))).withMethod("clear", BCDescriptors.MTD_Group, 17, mb -> mb.withCode(cb -> BCGroup.buildClear(cb, layout, rcb -> rcb.aload(rcb.receiverSlot()).getfield(thisClass, "address", ConstantDescs.CD_long), rcb -> rcb.aload(rcb.receiverSlot())))).withMethod("get", BCDescriptors.MTD_Group_MemorySegment, 17, mb -> mb.withCode(cb -> BCGroup.buildGetMemorySegment(cb, config, layoutDesc, ccb -> BCGroup.buildGetFromMemorySegment(ccb, thisClass, acb -> acb.aload(ccb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long))))).withMethod("get", BCDescriptors.MTD_Group_MemorySegment_long, 17, mb -> mb.withCode(cb -> BCGroup.buildGetMemorySegmentAtOffset(cb, config, layoutDesc, ccb -> BCGroup.buildGetFromMemorySegment(ccb, thisClass, acb -> acb.aload(ccb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long).lload(ccb.parameterSlot(1)).ladd())))).withMethod("getAtIndex", BCDescriptors.MTD_Group_MemorySegment_long, 17, mb -> mb.withCode(cb -> BCGroup.buildGetMemorySegmentAtIndex(cb, config, layoutDesc, ccb -> BCGroup.buildGetFromMemorySegment(ccb, thisClass, acb -> acb.aload(ccb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long).lload(cb.parameterSlot(1)).ldc(BCUtil.condyCDataAt(layoutDesc, 0)).invokeinterface(BCDescriptors.CD_GroupLayout, "byteSize", BCDescriptors.MTD_long).lmul().ladd())))).withMethod("set", BCDescriptors.MTD_Group_MemorySegment, 17, mb -> mb.withCode(cb -> BCGroup.buildGetMemorySegment(cb, config, layoutDesc, ccb -> BCGroup.buildSetFromMemorySegment(ccb, thisClass, acb -> acb.aload(ccb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long))))).withMethod("set", BCDescriptors.MTD_Group_MemorySegment_long, 17, mb -> mb.withCode(cb -> BCGroup.buildGetMemorySegmentAtOffset(cb, config, layoutDesc, ccb -> BCGroup.buildSetFromMemorySegment(ccb, thisClass, acb -> acb.aload(ccb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long).lload(ccb.parameterSlot(1)).ladd())))).withMethod("setAtIndex", BCDescriptors.MTD_Group_MemorySegment_long, 17, mb -> mb.withCode(cb -> BCGroup.buildGetMemorySegmentAtIndex(cb, config, layoutDesc, ccb -> BCGroup.buildSetFromMemorySegment(ccb, thisClass, acb -> acb.aload(ccb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long).lload(cb.parameterSlot(1)).ldc(BCUtil.condyCDataAt(layoutDesc, 0)).invokeinterface(BCDescriptors.CD_GroupLayout, "byteSize", BCDescriptors.MTD_long).lmul().ladd()))));
            }
            if (Group.class.isAssignableFrom(groupInterface) || Pointer.class.isAssignableFrom(groupInterface)) {
                classBuilder.withMethod("address", BCDescriptors.MTD_long, 17, mb -> mb.withCode(cb -> cb.aload(cb.receiverSlot()).getfield(thisClass, "address", ConstantDescs.CD_long).lreturn()));
            }
            if (NativeResource.class.isAssignableFrom(groupInterface)) {
                classBuilder.withMethod("free", ConstantDescs.MTD_void, 17, mb -> mb.withCode(cb -> cb.aload(cb.receiverSlot()).getfield(thisClass, "address", ConstantDescs.CD_long).invokestatic(BCDescriptors.CD_MemoryUtil, "nmemFree", BCDescriptors.MTD_void_long).return_()));
            }
        });
        if (config.debugGenerator) {
            BCUtil.printModel(ClassFile.of().parse(bytecode));
        }
        try {
            return config.lookup.defineHiddenClassWithClassData(bytecode, List.of(layout, builder.equals != null ? builder.equals : BCUtil.EMPTY_SLOT, builder.hashCode != null ? builder.hashCode : BCUtil.EMPTY_SLOT, builder.toString != null ? builder.toString : BCUtil.EMPTY_SLOT), true, new MethodHandles.Lookup.ClassOption[0]);
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

    private static SequencedMap<String, List<Method>> compileMemberAccessors(Class<?> groupInterface, GroupLayout layout) {
        LinkedHashMap<String, List<Method>> memberMap = new LinkedHashMap<String, List<Method>>(layout.memberLayouts().size());
        HashMap<String, List> methods = new HashMap<String, List>(layout.memberLayouts().size());
        for (Method method : groupInterface.getMethods()) {
            Class<?> declaringClass;
            if (Modifier.isStatic(method.getModifiers()) || method.isDefault() || (declaringClass = method.getDeclaringClass()) == Object.class || declaringClass == Group.class || declaringClass == Pointer.class || declaringClass == NativeResource.class) continue;
            BCGroup.checkAccessorAliasing(groupInterface, method);
            String name = BCUtil.getNativeName(method);
            methods.computeIfAbsent(name, string -> new ArrayList(4)).add(method);
        }
        for (MemoryLayout memoryLayout : layout.memberLayouts()) {
            List memberAccessors;
            String name = memoryLayout.name().orElse(null);
            if (name == null || (memberAccessors = (List)methods.get(name)) == null) continue;
            memberMap.put(name, memberAccessors);
        }
        for (Map.Entry entry : methods.entrySet()) {
            if (memberMap.containsKey(entry.getKey())) continue;
            throw BCGroup.memberException("No layout member found with this name", groupInterface, (String)entry.getKey());
        }
        return memberMap;
    }

    private static int registerCanonicalGetter(Class<?> groupInterface, Method method, LinkedHashMap<String, Method> getters, String memberName) {
        if (method.isAnnotationPresent(FFMCanonical.class)) {
            Method canonical = getters.get(memberName);
            if (canonical != null && canonical.isAnnotationPresent(FFMCanonical.class)) {
                throw BCGroup.memberException("Multiple canonical getters found", groupInterface, memberName);
            }
            getters.put(memberName, method);
        } else if (memberName.equals(method.getName())) {
            Method canonical = getters.get(memberName);
            if (canonical == null || !canonical.isAnnotationPresent(FFMCanonical.class)) {
                getters.put(memberName, method);
            }
        } else {
            getters.putIfAbsent(memberName, method);
            return 1;
        }
        return 0;
    }

    private static void checkAccessorAliasing(Class<?> groupInterface, Method method) {
        block18: {
            switch (method.getName()) {
                case "equals": 
                case "hashCode": 
                case "toString": {
                    break;
                }
                case "address": {
                    if (Group.class.isAssignableFrom(groupInterface) || Pointer.class.isAssignableFrom(groupInterface)) {
                        break;
                    }
                    break block18;
                }
                case "layout": 
                case "clear": 
                case "sizeof": 
                case "alignof": 
                case "asSegment": {
                    if (Group.class.isAssignableFrom(groupInterface)) {
                        break;
                    }
                    break block18;
                }
                default: {
                    break block18;
                }
            }
            throw BCGroup.methodException("Group accessor name aliases supertype method and must be changed with @FFMName", method);
        }
    }

    private static ClassDesc groupDesc(FFMConfig.BinderField binderField) {
        Binder<?> binder;
        Binder<?> binder2 = binder = binderField.binder();
        Objects.requireNonNull(binder2);
        Binder<?> binder3 = binder2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{StructBinder.class, UnionBinder.class}, binder3, n)) {
            case 0 -> BCDescriptors.CD_StructBinder;
            case 1 -> BCDescriptors.CD_UnionBinder;
            default -> throw new UnsupportedOperationException("Unsupported binder type: " + String.valueOf(binder.getClass()));
        };
    }

    private static CodeBuilder buildMemberAddress(CodeBuilder cb, ClassDesc thisClass, long memberOffset) {
        cb.aload(cb.receiverSlot()).getfield(thisClass, "address", ConstantDescs.CD_long);
        if (memberOffset != 0L) {
            cb.loadConstant(memberOffset).ladd();
        }
        return cb;
    }

    private static void buildNullPointerCheck(CodeBuilder cb) {
        if (Checks.DEBUG) {
            cb.dup2().lconst_0().lcmp().ifThen(Opcode.IFEQ, bcb -> bcb.new_(BCDescriptors.CD_NullPointerException).dup().ldc((ConstantDesc)((Object)"Pointer value is NULL")).invokespecial(BCDescriptors.CD_NullPointerException, "<init>", BCDescriptors.MTD_void_String).athrow());
        }
    }

    private static <T extends CodeBuilder> T buildAutoSize(T cb, ClassDesc groupDesc, SequencedMap<String, List<Method>> memberMap, Method method, FFMSize autoSize, MemoryLayout elementLayout) {
        long byteSize;
        Method sizeGetter = ((List)memberMap.get(autoSize.value())).stream().filter(it -> it.getReturnType().isPrimitive()).findFirst().orElseThrow(() -> new IllegalStateException("The FFMSize reference \"" + autoSize.value() + "\" not found for " + String.valueOf(method)));
        MethodTypeDesc mtd = BCUtil.getMethodTypeDesc(sizeGetter);
        cb.aload(cb.receiverSlot()).invokeinterface(groupDesc, sizeGetter.getName(), mtd);
        if (mtd.returnType() != ConstantDescs.CD_long) {
            if (mtd.returnType() == ConstantDescs.CD_int) {
                cb.i2l().loadConstant(0xFFFFFFFFL).land();
            } else if (mtd.returnType() == ConstantDescs.CD_short || mtd.returnType() == ConstantDescs.CD_char) {
                cb.loadConstant(65535).iand().i2l();
            } else if (mtd.returnType() == ConstantDescs.CD_byte) {
                cb.loadConstant(255).iand().i2l();
            } else {
                throw BCGroup.methodException("Unsupported FFMSize getter type: " + String.valueOf(sizeGetter), method);
            }
        }
        if ((byteSize = elementLayout.byteSize()) != 1L) {
            cb.loadConstant(byteSize).lmul();
        }
        return cb;
    }

    private static <T extends CodeBuilder> T buildMemorySegmentReinterpret(T cb, ClassDesc groupDesc, SequencedMap<String, List<Method>> memberMap, Method method, AddressLayout addressLayout) {
        MemoryLayout targetLayout = addressLayout.targetLayout().orElseThrow();
        FFMSize autoSize = method.getAnnotation(FFMSize.class);
        if (autoSize != null) {
            BCGroup.buildAutoSize(cb, groupDesc, memberMap, method, autoSize, targetLayout).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long);
        } else {
            cb.loadConstant(targetLayout.byteSize()).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long);
        }
        return cb;
    }

    private static <T extends CodeBuilder> T buildStringGetter(T cb, ClassDesc groupDesc, SequencedMap<String, List<Method>> memberMap, Method method, FFMCharset.Type charset) {
        cb.invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true);
        FFMSize autoSize = method.getAnnotation(FFMSize.class);
        if (autoSize != null) {
            int arraySlot = cb.allocateLocal(TypeKind.REFERENCE);
            BCGroup.buildAutoSize(cb, groupDesc, memberMap, method, autoSize, charset.layout).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_BYTE", BCDescriptors.CD_ValueLayout$OfByte).invokeinterface(BCDescriptors.CD_MemorySegment, "toArray", BCDescriptors.MTD_byteArray_ValueLayout$OfByte).astore(arraySlot).new_(ConstantDescs.CD_String).dup().aload(arraySlot);
            BCUtil.buildCharsetInstance(cb, charset).invokespecial(ConstantDescs.CD_String, "<init>", BCDescriptors.MTD_void_byteArray_Charset);
        } else {
            cb.loadConstant(Long.MAX_VALUE).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).lconst_0();
            BCUtil.buildCharsetInstance(cb, charset).invokeinterface(BCDescriptors.CD_MemorySegment, "getString", BCDescriptors.MTD_String_long_Charset);
        }
        return cb;
    }

    private static <T extends CodeBuilder> T buildGetFromMemorySegment(T cb, ClassDesc thisClass, Function<T, T> codeAddress) {
        cb.aload(cb.receiverSlot()).dup().new_(thisClass).dup();
        ((CodeBuilder)codeAddress.apply(cb)).invokespecial(thisClass, "<init>", BCDescriptors.MTD_void_long).invokevirtual(thisClass, "copyFrom", BCDescriptors.MTD_Group_Group).areturn();
        return cb;
    }

    private static <T extends CodeBuilder> T buildSetFromMemorySegment(T cb, ClassDesc thisClass, Function<T, T> codeAddress) {
        cb.new_(thisClass).dup();
        ((CodeBuilder)codeAddress.apply(cb)).invokespecial(thisClass, "<init>", BCDescriptors.MTD_void_long).aload(cb.receiverSlot()).invokevirtual(thisClass, "copyFrom", BCDescriptors.MTD_Group_Group).areturn();
        return cb;
    }

    static <T, L extends GroupLayout, M extends GroupBinder<L, T>> M bootstrap(FFM.GroupBinderBuilder<T, L, M, ?> builder, long byteAlignment) {
        MethodHandle implementationAddress;
        MethodHandle implementationConstructor;
        int featureFlags;
        Class groupInterface = builder.groupInterface;
        SequencedMap<String, MemoryLayout> members = builder.members;
        Kind kind = builder.kind();
        GroupLayout tmp = kind.layout(members.values().toArray(new MemoryLayout[0])).withName(BCUtil.getNativeName(groupInterface));
        if (tmp.byteAlignment() < byteAlignment) {
            tmp = tmp.withByteAlignment(byteAlignment);
        }
        GroupLayout layout = tmp;
        FFMConfig config = FFM.getConfig(groupInterface);
        boolean hasTracing = config.traceConsumer != null;
        int n = featureFlags = hasTracing ? BCCall.FeatureFlag.FF_TRACING.mask : 0;
        if (config.debugGenerator) {
            APIUtil.apiLog("BOOTSTRAPPING " + kind.name() + " " + String.valueOf(groupInterface));
        }
        MethodHandles.Lookup implementationLookup = BCGroup.bootstrapImplementation(config, groupInterface, layout, builder);
        try {
            implementationConstructor = implementationLookup.findConstructor(implementationLookup.lookupClass(), MethodType.methodType(Void.TYPE, Long.TYPE)).asType(MethodType.methodType(groupInterface, Long.TYPE));
            if (Checks.DEBUG) {
                implementationConstructor = MethodHandles.filterArguments(implementationConstructor, 0, MethodHandles.insertArguments(CHECK_ADDRESS, 1, layout.byteAlignment()));
            }
            implementationAddress = implementationLookup.findGetter(implementationLookup.lookupClass(), "address", Long.TYPE).asType(MethodType.methodType(Long.TYPE, groupInterface));
        }
        catch (Error | RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        ClassDesc binderClass = ClassDesc.of(groupInterface.getPackageName(), groupInterface.getSimpleName() + "Binder");
        byte[] bytecode = ClassFile.of().build(binderClass, classBuilder -> {
            BCUtil.startHiddenClass(classBuilder).withInterfaceSymbols(kind.binderDesc());
            ClassDesc groupDesc = groupInterface.describeConstable().orElseThrow();
            MethodTypeDesc constructorDesc = MethodTypeDesc.of(groupDesc, ConstantDescs.CD_long);
            classBuilder.withMethod("layout", BCDescriptors.MTD_GroupLayout, 17, mb -> mb.withCode(cb -> cb.ldc(BCUtil.condyCDataAt(kind.layoutDesc(), 0)).areturn())).withMethod("addressOf", BCDescriptors.MTD_long_Object, 17, mb -> mb.withCode(cb -> cb.ldc(BCUtil.condyCDataAt(ConstantDescs.CD_MethodHandle, 1)).aload(cb.parameterSlot(0)).invokevirtual(ConstantDescs.CD_MethodHandle, "invokeExact", MethodTypeDesc.of(ConstantDescs.CD_long, groupDesc)).lreturn())).withMethod("ofAddress", BCDescriptors.MTD_Object_long, 17, mb -> mb.withCode(cb -> BCGroup.buildConstructor(cb, constructorDesc, acb -> acb.lload(cb.parameterSlot(0))))).withMethod("copy", BCDescriptors.MTD_Object_Object_Object, 17, mb -> mb.withCode(cb -> BCGroup.buildCopy(cb, layout, rcb -> rcb.aload(rcb.receiverSlot()).aload(rcb.parameterSlot(0)).invokevirtual(binderClass, "addressOf", BCDescriptors.MTD_long_Object), rcb -> rcb.aload(rcb.receiverSlot()).aload(rcb.parameterSlot(1)).invokevirtual(binderClass, "addressOf", BCDescriptors.MTD_long_Object), rcb -> rcb.aload(rcb.parameterSlot(1))))).withMethod("clear", BCDescriptors.MTD_Object_Object, 17, mb -> mb.withCode(cb -> BCGroup.buildClear(cb, layout, rcb -> rcb.aload(rcb.receiverSlot()).aload(rcb.parameterSlot(0)).invokevirtual(binderClass, "addressOf", BCDescriptors.MTD_long_Object), rcb -> rcb.aload(rcb.parameterSlot(0))))).withMethod("get", BCDescriptors.MTD_Object_MemorySegment, 17, mb -> mb.withCode(cb -> BCGroup.buildGetMemorySegment(cb, config, kind.layoutDesc(), ccb -> BCGroup.buildConstructor(ccb, constructorDesc, acb -> acb.aload(cb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long))))).withMethod("get", BCDescriptors.MTD_Object_MemorySegment_long, 17, mb -> mb.withCode(cb -> BCGroup.buildGetMemorySegmentAtOffset(cb, config, kind.layoutDesc(), ccb -> BCGroup.buildConstructor(ccb, constructorDesc, acb -> acb.aload(ccb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long).lload(ccb.parameterSlot(1)).ladd())))).withMethod("getAtIndex", BCDescriptors.MTD_Object_MemorySegment_long, 17, mb -> mb.withCode(cb -> BCGroup.buildGetMemorySegmentAtIndex(cb, config, kind.layoutDesc(), ccb -> BCGroup.buildConstructor(ccb, constructorDesc, acb -> acb.aload(cb.parameterSlot(0)).invokeinterface(BCDescriptors.CD_MemorySegment, "address", BCDescriptors.MTD_long).lload(cb.parameterSlot(1)).ldc(BCUtil.condyCDataAt(kind.layoutDesc(), 0)).invokeinterface(BCDescriptors.CD_GroupLayout, "byteSize", BCDescriptors.MTD_long).lmul().ladd()))));
        });
        if (config.debugGenerator) {
            BCUtil.printModel(ClassFile.of().parse(bytecode));
        }
        try {
            MethodHandles.Lookup wrapperLookup = config.lookup.defineHiddenClassWithClassData(bytecode, List.of(layout, implementationAddress, implementationConstructor), true, new MethodHandles.Lookup.ClassOption[0]);
            return (M)((GroupBinder)wrapperLookup.lookupClass().getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
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

    private static String[] getBootstrapArgs(GroupLayout layout, LinkedHashMap<String, Method> getters) {
        String bootstrapNames = getters.sequencedKeySet().stream().collect(SEMI_COLON);
        Stream.Builder<String> bootstrapArgsBuilder = Stream.builder().add(bootstrapNames);
        getters.sequencedEntrySet().forEach(member -> {
            Method method = (Method)member.getValue();
            MemoryLayout.PathElement memberPath = MemoryLayout.PathElement.groupElement((String)member.getKey());
            MemoryLayout memberLayout = layout.select(memberPath);
            bootstrapArgsBuilder.add((String)(memberLayout instanceof AddressLayout && method.getReturnType() != Long.TYPE ? "__address__" + method.getName() : method.getName()));
        });
        return (String[])bootstrapArgsBuilder.build().toArray(String[]::new);
    }

    private static <T extends CodeBuilder> T buildConstructor(T cb, MethodTypeDesc constructorDesc, Function<T, T> codeAddress) {
        cb.ldc(BCUtil.condyCDataAt(ConstantDescs.CD_MethodHandle, 2));
        ((CodeBuilder)codeAddress.apply(cb)).invokevirtual(ConstantDescs.CD_MethodHandle, "invokeExact", constructorDesc).areturn();
        return cb;
    }

    private static <T extends CodeBuilder> void buildCopy(T cb, GroupLayout layout, Function<T, T> codeSrc, Function<T, T> codeDst, Function<T, T> codeRet) {
        codeDst.apply(cb);
        long byteSize = layout.byteSize();
        if (512L < byteSize || BCUtil.JAVA_VERSION == 25) {
            if (byteSize < BCUtil.NATIVE_THRESHOLD_COPY || (byteSize & 1L) != 0L) {
                cb.invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(byteSize).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long);
                ((CodeBuilder)codeSrc.apply(cb)).invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(byteSize).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long);
                cb.invokeinterface(BCDescriptors.CD_MemorySegment, "copyFrom", BCDescriptors.MTD_MemorySegment_MemorySegment).pop();
            } else {
                int dstSlot = cb.allocateLocal(TypeKind.LONG);
                cb.dup2().lstore(dstSlot).invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(byteSize - 1L).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long);
                int srcSlot = cb.allocateLocal(TypeKind.LONG);
                ((CodeBuilder)codeSrc.apply(cb)).dup2().lstore(srcSlot).invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(byteSize - 1L).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long);
                cb.invokeinterface(BCDescriptors.CD_MemorySegment, "copyFrom", BCDescriptors.MTD_MemorySegment_MemorySegment).pop();
                cb.lload(dstSlot).loadConstant(byteSize - 1L).ladd().lload(srcSlot).loadConstant(byteSize - 1L).ladd().invokestatic(BCDescriptors.CD_MemoryUtil, "memGetByte", BCDescriptors.MTD_byte_long).invokestatic(BCDescriptors.CD_MemoryUtil, "memPutByte", BCDescriptors.MTD_void_long_byte);
            }
        } else {
            int dstSlot = cb.allocateLocal(TypeKind.REFERENCE);
            cb.invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(byteSize).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).astore(dstSlot);
            int srcSlot = cb.allocateLocal(TypeKind.REFERENCE);
            ((CodeBuilder)codeSrc.apply(cb)).invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(byteSize).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).astore(srcSlot);
            if (16L <= byteSize) {
                int offsetSlot = cb.allocateLocal(TypeKind.LONG);
                cb.lconst_0().lstore(offsetSlot);
                Label loopStart = cb.newBoundLabel();
                Label loopEnd = cb.newLabel();
                cb.lload(offsetSlot).loadConstant(byteSize & 0xFFFFFFFFFFFFFFF8L).lcmp().ifge(loopEnd).aload(dstSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_LONG_UNALIGNED", BCDescriptors.CD_ValueLayout$OfLong).lload(offsetSlot).aload(srcSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_LONG_UNALIGNED", BCDescriptors.CD_ValueLayout$OfLong).lload(offsetSlot).invokeinterface(BCDescriptors.CD_MemorySegment, "get", BCDescriptors.MTD_long_ValueLayout$OfLong_long).invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfLong_long_long).lload(offsetSlot).loadConstant(8L).ladd().lstore(offsetSlot).goto_(loopStart).labelBinding(loopEnd);
            } else if (8L <= byteSize) {
                cb.aload(dstSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_LONG_UNALIGNED", BCDescriptors.CD_ValueLayout$OfLong).lconst_0().aload(srcSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_LONG_UNALIGNED", BCDescriptors.CD_ValueLayout$OfLong).lconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "get", BCDescriptors.MTD_long_ValueLayout$OfLong_long).invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfLong_long_long);
            }
            long offset = byteSize & 0xFFFFFFFFFFFFFFF8L;
            if (offset < (byteSize & 0xFFFFFFFFFFFFFFFCL)) {
                cb.aload(dstSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_INT_UNALIGNED", BCDescriptors.CD_ValueLayout$OfInt).loadConstant(offset).aload(srcSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_INT_UNALIGNED", BCDescriptors.CD_ValueLayout$OfInt).loadConstant(offset).invokeinterface(BCDescriptors.CD_MemorySegment, "get", BCDescriptors.MTD_int_ValueLayout$OfInt_long).invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfInt_long_int);
                offset += 4L;
            }
            if (offset < (byteSize & 0xFFFFFFFFFFFFFFFEL)) {
                cb.aload(dstSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_SHORT_UNALIGNED", BCDescriptors.CD_ValueLayout$OfShort).loadConstant(offset).aload(srcSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_SHORT_UNALIGNED", BCDescriptors.CD_ValueLayout$OfShort).loadConstant(offset).invokeinterface(BCDescriptors.CD_MemorySegment, "get", BCDescriptors.MTD_short_ValueLayout$OfShort_long).invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfShort_long_short);
                offset += 2L;
            }
            if (offset < byteSize) {
                cb.aload(dstSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_BYTE", BCDescriptors.CD_ValueLayout$OfByte).loadConstant(offset).aload(srcSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_BYTE", BCDescriptors.CD_ValueLayout$OfByte).loadConstant(offset).invokeinterface(BCDescriptors.CD_MemorySegment, "get", BCDescriptors.MTD_byte_ValueLayout$OfByte_long).invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfByte_long_byte);
            }
        }
        ((CodeBuilder)codeRet.apply(cb)).areturn();
    }

    private static <T extends CodeBuilder> void buildClear(T cb, GroupLayout layout, Function<T, T> codeReceiver, Function<T, T> codeReturn) {
        codeReceiver.apply(cb);
        long byteSize = layout.byteSize();
        if (1024L < byteSize || BCUtil.JAVA_VERSION == 25 && (byteSize <= BCUtil.NATIVE_THRESHOLD_FILL || 64L < byteSize)) {
            if (byteSize < BCUtil.NATIVE_THRESHOLD_FILL || (byteSize & 1L) != 0L) {
                cb.invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(byteSize).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).iconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "fill", BCDescriptors.MTD_MemorySegment_byte).pop();
            } else {
                int addressSlot = cb.allocateLocal(TypeKind.LONG);
                cb.lstore(addressSlot);
                cb.lload(addressSlot).invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(byteSize - 1L).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).iconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "fill", BCDescriptors.MTD_MemorySegment_byte).pop();
                cb.lload(addressSlot).loadConstant(byteSize - 1L).ladd().invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).lconst_1().invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_BYTE", BCDescriptors.CD_ValueLayout$OfByte).lconst_0().iconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfByte_long_byte);
            }
        } else {
            int segmentSlot = cb.allocateLocal(TypeKind.REFERENCE);
            cb.invokestatic(BCDescriptors.CD_MemorySegment, "ofAddress", BCDescriptors.MTD_MemorySegment_long, true).loadConstant(byteSize).invokeinterface(BCDescriptors.CD_MemorySegment, "reinterpret", BCDescriptors.MTD_MemorySegment_long).astore(segmentSlot);
            if (16L <= byteSize) {
                int offsetSlot = cb.allocateLocal(TypeKind.LONG);
                cb.lconst_0().lstore(offsetSlot);
                Label loopStart = cb.newBoundLabel();
                Label loopEnd = cb.newLabel();
                cb.lload(offsetSlot).loadConstant(byteSize & 0xFFFFFFFFFFFFFFF8L).lcmp().ifge(loopEnd).aload(segmentSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_LONG_UNALIGNED", BCDescriptors.CD_ValueLayout$OfLong).lload(offsetSlot).lconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfLong_long_long).lload(offsetSlot).loadConstant(8L).ladd().lstore(offsetSlot).goto_(loopStart).labelBinding(loopEnd);
            } else if (8L <= byteSize) {
                cb.aload(segmentSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_LONG_UNALIGNED", BCDescriptors.CD_ValueLayout$OfLong).lconst_0().lconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfLong_long_long);
            }
            long offset = byteSize & 0xFFFFFFFFFFFFFFF8L;
            if (offset < (byteSize & 0xFFFFFFFFFFFFFFFCL)) {
                cb.aload(segmentSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_INT_UNALIGNED", BCDescriptors.CD_ValueLayout$OfInt).loadConstant(offset).iconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfInt_long_int);
                offset += 4L;
            }
            if (offset < (byteSize & 0xFFFFFFFFFFFFFFFEL)) {
                cb.aload(segmentSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_SHORT_UNALIGNED", BCDescriptors.CD_ValueLayout$OfShort).loadConstant(offset).iconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfShort_long_short);
                offset += 2L;
            }
            if (offset < byteSize) {
                cb.aload(segmentSlot).getstatic(BCDescriptors.CD_ValueLayout, "JAVA_BYTE", BCDescriptors.CD_ValueLayout$OfByte).loadConstant(offset).iconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "set", BCDescriptors.MTD_void_ValueLayout$OfByte_long_byte);
            }
        }
        ((CodeBuilder)codeReturn.apply(cb)).areturn();
    }

    public static <T extends CodeBuilder> void buildGetMemorySegment(T cb, FFMConfig config, ClassDesc layoutDesc, Function<T, T> constructorCode) {
        if (config.checks) {
            cb.aload(cb.parameterSlot(0)).lconst_0().ldc(BCUtil.condyCDataAt(layoutDesc, 0)).invokeinterface(BCDescriptors.CD_MemorySegment, "asSlice", BCDescriptors.MTD_MemorySegment_long_MemoryLayout).pop();
        }
        constructorCode.apply(cb);
    }

    public static <T extends CodeBuilder> void buildGetMemorySegmentAtOffset(T cb, FFMConfig config, ClassDesc layoutDesc, Function<T, T> constructorCode) {
        if (config.checks) {
            cb.aload(cb.parameterSlot(0)).lload(cb.parameterSlot(1)).ldc(BCUtil.condyCDataAt(layoutDesc, 0)).invokeinterface(BCDescriptors.CD_MemorySegment, "asSlice", BCDescriptors.MTD_MemorySegment_long_MemoryLayout).pop();
        }
        constructorCode.apply(cb);
    }

    public static <T extends CodeBuilder> void buildGetMemorySegmentAtIndex(T cb, FFMConfig config, ClassDesc layoutDesc, Function<T, T> constructorCode) {
        if (config.checks) {
            cb.aload(cb.parameterSlot(0)).lload(cb.parameterSlot(1)).ldc(BCUtil.condyCDataAt(layoutDesc, 0)).invokeinterface(BCDescriptors.CD_GroupLayout, "byteSize", BCDescriptors.MTD_long).lmul().ldc(BCUtil.condyCDataAt(layoutDesc, 0)).invokeinterface(BCDescriptors.CD_MemorySegment, "asSlice", BCDescriptors.MTD_MemorySegment_long_MemoryLayout).pop();
        }
        constructorCode.apply(cb);
    }

    private static long checkAddress(long address, long alignment) {
        if (address == 0L) {
            throw new NullPointerException("Group instance cannot be instantiated with a NULL address");
        }
        if ((address & alignment - 1L) != 0L) {
            throw new IllegalArgumentException("Group instance address is not properly aligned to " + alignment + " bytes: 0x" + Long.toHexString(address));
        }
        return address;
    }

    static {
        try {
            CHECK_ADDRESS = MethodHandles.lookup().findStatic(BCGroup.class, "checkAddress", MethodType.methodType(Long.TYPE, Long.TYPE, Long.TYPE));
        }
        catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static enum Kind {
        STRUCT{

            @Override
            GroupLayout layout(MemoryLayout ... members) {
                return MemoryLayout.structLayout(members);
            }

            @Override
            ClassDesc layoutDesc() {
                return BCDescriptors.CD_StructLayout;
            }

            @Override
            ClassDesc binderDesc() {
                return BCDescriptors.CD_StructBinder;
            }
        }
        ,
        UNION{

            @Override
            GroupLayout layout(MemoryLayout ... members) {
                return MemoryLayout.unionLayout(members);
            }

            @Override
            ClassDesc layoutDesc() {
                return BCDescriptors.CD_UnionLayout;
            }

            @Override
            ClassDesc binderDesc() {
                return BCDescriptors.CD_UnionBinder;
            }
        };


        abstract GroupLayout layout(MemoryLayout ... var1);

        abstract ClassDesc layoutDesc();

        abstract ClassDesc binderDesc();
    }
}

