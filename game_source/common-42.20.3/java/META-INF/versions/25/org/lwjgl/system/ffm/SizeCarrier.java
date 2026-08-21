/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.ValueLayout;
import org.lwjgl.system.ffm.BCDescriptors;

public enum SizeCarrier {
    BYTE(ValueLayout.JAVA_BYTE, "JAVA_BYTE", BCDescriptors.CD_ValueLayout$OfByte, BCDescriptors.MTD_byte_ValueLayout$OfByte_long),
    SHORT(ValueLayout.JAVA_SHORT, "JAVA_SHORT", BCDescriptors.CD_ValueLayout$OfShort, BCDescriptors.MTD_short_ValueLayout$OfShort_long),
    INT(ValueLayout.JAVA_INT, "JAVA_INT", BCDescriptors.CD_ValueLayout$OfInt, BCDescriptors.MTD_int_ValueLayout$OfInt_long),
    LONG(ValueLayout.JAVA_LONG, "JAVA_LONG", BCDescriptors.CD_ValueLayout$OfLong, BCDescriptors.MTD_long_ValueLayout$OfLong_long),
    SIZE_T(ValueLayout.ADDRESS, "ADDRESS", BCDescriptors.CD_AddressLayout, BCDescriptors.MTD_MemorySegment_AddressLayout_long);

    private static final SizeCarrier[] values;
    public final ValueLayout layout;
    final String name;
    final ClassDesc type;
    final MethodTypeDesc getter;

    private SizeCarrier(ValueLayout layout, String name, ClassDesc type, MethodTypeDesc getter) {
        this.layout = layout;
        this.name = name;
        this.type = type;
        this.getter = getter;
    }

    CodeBuilder allocateSingle(CodeBuilder cb) {
        return cb.getstatic(BCDescriptors.CD_ValueLayout, this.name, this.type).lconst_1().invokeinterface(BCDescriptors.CD_SegmentAllocator, "allocate", BCDescriptors.MTD_MemorySegment_MemoryLayout_long);
    }

    static SizeCarrier get(Class<?> carrier) {
        for (SizeCarrier value : values) {
            if (value.layout.carrier() != carrier) continue;
            return value;
        }
        throw new IllegalArgumentException("Unsupported size carrier type: " + String.valueOf(carrier));
    }

    static {
        values = SizeCarrier.values();
    }
}

