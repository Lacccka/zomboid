/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.lwjgl.system.ffm.BCDescriptors;
import org.lwjgl.system.ffm.BCUtil;
import org.lwjgl.system.ffm.FFMCharset;
import org.lwjgl.system.ffm.FFMReturn;
import org.lwjgl.system.ffm.SizeCarrier;

record BCReturnTransform(int bufferIndex, int bufferSlot, int sizeIndex, SizeCarrier sizeCarrier, int sizeOutputIndex, int sizeOutputSlot) {
    static BCReturnTransform create(CodeBuilder cb, MethodTypeDesc methodTypeDesc, Method method, Parameter[] parameters, FFMReturn returnAnnotation, int allocatorSlot) {
        int bufferIndex = returnAnnotation.value();
        int sizeInputIndex = -1;
        for (int p = 0; p < methodTypeDesc.parameterCount(); ++p) {
            if (!parameters[p].isAnnotationPresent(FFMReturn.Size.class)) continue;
            sizeInputIndex = p;
            break;
        }
        if (sizeInputIndex == -1 && (BCDescriptors.CD_MemorySegment.equals(methodTypeDesc.returnType()) || ConstantDescs.CD_String.equals(methodTypeDesc.returnType()))) {
            throw new IllegalStateException("Missing @FFMReturn.Size annotation");
        }
        SizeCarrier sizeCarrier = sizeInputIndex == -1 ? SizeCarrier.get(method.getReturnType()) : SizeCarrier.get(parameters[sizeInputIndex].getType());
        FFMReturn.SizeOut returnOutputAnnotation = method.getAnnotation(FFMReturn.SizeOut.class);
        int sizeOutputIndex = returnOutputAnnotation == null ? -1 : returnOutputAnnotation.value();
        int sizeOutputSlot = -1;
        if (returnOutputAnnotation != null && sizeOutputIndex < bufferIndex) {
            sizeOutputSlot = BCReturnTransform.allocateOutputSlot(cb, sizeCarrier, allocatorSlot);
        }
        int bufferSlot = cb.allocateLocal(TypeKind.REFERENCE);
        if (sizeInputIndex == -1) {
            cb.aload(allocatorSlot);
            SizeCarrier.get(method.getReturnType()).allocateSingle(cb).astore(bufferSlot);
        } else {
            int slot = cb.parameterSlot(sizeInputIndex);
            TypeKind kind = TypeKind.from(methodTypeDesc.parameterType(sizeInputIndex));
            cb.aload(allocatorSlot).loadLocal(kind, slot);
            if (method.getReturnType() == String.class) {
                BCUtil.buildCharsetShift(cb, BCUtil.getCharset(method), kind);
            }
            if (kind != TypeKind.LONG) {
                cb.i2l();
            }
            cb.invokeinterface(BCDescriptors.CD_SegmentAllocator, "allocate", BCDescriptors.MTD_MemorySegment_long).astore(bufferSlot);
        }
        if (returnOutputAnnotation != null && bufferIndex < sizeOutputIndex) {
            sizeOutputSlot = BCReturnTransform.allocateOutputSlot(cb, sizeCarrier, allocatorSlot);
        }
        return new BCReturnTransform(bufferIndex, bufferSlot, sizeInputIndex, sizeCarrier, sizeOutputIndex, sizeOutputSlot);
    }

    private static int allocateOutputSlot(CodeBuilder cb, SizeCarrier sizeCarrier, int allocatorSlot) {
        int slot = cb.allocateLocal(TypeKind.REFERENCE);
        cb.aload(allocatorSlot);
        sizeCarrier.allocateSingle(cb).astore(slot);
        return slot;
    }

    void loadParameters(CodeBuilder bcb, int virtualParameterCount, int p) {
        if (p == virtualParameterCount + this.bufferIndex) {
            bcb.aload(this.bufferSlot);
        }
        if (this.sizeOutputIndex() != -1 && p == virtualParameterCount + this.sizeOutputIndex) {
            bcb.aload(this.sizeOutputSlot);
        }
    }

    void loadParametersTail(CodeBuilder bcb, int virtualParameterCount, int parameterCount) {
        if (this.sizeOutputSlot != -1 && parameterCount <= virtualParameterCount + this.sizeOutputIndex && this.sizeOutputSlot < this.bufferSlot) {
            bcb.aload(this.sizeOutputSlot);
        }
        if (parameterCount <= virtualParameterCount + this.bufferIndex) {
            bcb.aload(this.bufferSlot);
        }
        if (this.sizeOutputSlot != -1 && parameterCount <= virtualParameterCount + this.sizeOutputIndex && this.bufferSlot < this.sizeOutputSlot) {
            bcb.aload(this.sizeOutputSlot);
        }
    }

    void buildResult(CodeBuilder bcb, MethodTypeDesc methodTypeDesc, Method method) {
        if (method.getReturnType().isPrimitive()) {
            bcb.aload(this.bufferSlot).getstatic(BCDescriptors.CD_ValueLayout, this.sizeCarrier.name, this.sizeCarrier.type).lconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "get", this.sizeCarrier.getter);
        } else {
            FFMCharset.Type charsetType;
            TypeKind sizeKind = TypeKind.from(methodTypeDesc.parameterType(this.sizeIndex));
            int sizeSlot = bcb.allocateLocal(sizeKind);
            if (this.sizeOutputSlot != -1) {
                bcb.aload(this.sizeOutputSlot).getstatic(BCDescriptors.CD_ValueLayout, this.sizeCarrier.name, this.sizeCarrier.type).lconst_0().invokeinterface(BCDescriptors.CD_MemorySegment, "get", this.sizeCarrier.getter);
            }
            bcb.storeLocal(sizeKind, sizeSlot).aload(this.bufferSlot).lconst_0().loadLocal(sizeKind, sizeSlot);
            FFMCharset.Type type = charsetType = method.getReturnType() == String.class ? BCUtil.getCharset(method) : null;
            if (charsetType != null) {
                BCUtil.buildCharsetShift(bcb, charsetType, sizeKind);
            }
            if (sizeKind != TypeKind.LONG) {
                bcb.i2l();
            }
            bcb.invokeinterface(BCDescriptors.CD_MemorySegment, "asSlice", BCDescriptors.MTD_MemorySegment_long_long);
            if (charsetType != null) {
                int arraySlot = bcb.allocateLocal(TypeKind.REFERENCE);
                bcb.getstatic(BCDescriptors.CD_ValueLayout, "JAVA_BYTE", BCDescriptors.CD_ValueLayout$OfByte).invokeinterface(BCDescriptors.CD_MemorySegment, "toArray", BCDescriptors.MTD_byteArray_ValueLayout$OfByte).astore(arraySlot).new_(ConstantDescs.CD_String).dup().aload(arraySlot);
                BCUtil.buildCharsetInstance(bcb, charsetType).invokespecial(ConstantDescs.CD_String, "<init>", BCDescriptors.MTD_void_byteArray_Charset);
            }
        }
    }
}

