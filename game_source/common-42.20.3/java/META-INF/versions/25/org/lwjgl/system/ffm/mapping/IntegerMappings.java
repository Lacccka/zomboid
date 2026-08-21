/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm.mapping;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import org.lwjgl.system.Platform;
import org.lwjgl.system.ffm.mapping.Mapping;
import org.lwjgl.system.ffm.mapping.Mappings;

final class IntegerMappings {
    private static final boolean CLONG32 = ValueLayout.ADDRESS.byteSize() == 4L || Platform.get() == Platform.WINDOWS;

    private IntegerMappings() {
    }

    static Mapping.Byte create(ValueLayout.OfByte layout, boolean signed) {
        Mapping.Pointer p = new Mapping.Pointer(layout);
        return signed ? new ByteS(layout, p) : new ByteU(layout, p);
    }

    static Mapping.Char create(ValueLayout.OfChar layout) {
        return new Mapping.Char(layout, new Mapping.Pointer(layout));
    }

    static Mapping.Short create(ValueLayout.OfShort layout, boolean signed) {
        Mapping.Pointer p = new Mapping.Pointer(layout);
        return signed ? new ShortS(layout, p) : new ShortU(layout, p);
    }

    static Mapping.Int create(ValueLayout.OfInt layout, boolean signed) {
        Mapping.Pointer p = new Mapping.Pointer(layout);
        return signed ? new IntS(layout, p) : new IntU(layout, p);
    }

    static Mapping.Long create(ValueLayout.OfLong layout, boolean signed) {
        Mapping.Pointer p = new Mapping.Pointer(layout);
        return signed ? new LongS(layout, p) : new LongU(layout, p);
    }

    static Mapping.CLong createCLong(String name, boolean signed) {
        if (CLONG32) {
            ValueLayout.OfInt layout = ValueLayout.JAVA_INT.withName(name);
            Mapping.Pointer p = new Mapping.Pointer(layout);
            return signed ? new CLong32S(layout, p) : new CLong32U(layout, p);
        }
        ValueLayout.OfLong layout = ValueLayout.JAVA_LONG.withName(name);
        Mapping.Pointer p = new Mapping.Pointer(layout);
        return new CLong64Impl(layout, p, signed);
    }

    static Mapping.Size create(AddressLayout layout, boolean signed) {
        Mapping.Pointer p = new Mapping.Pointer((MemoryLayout)layout);
        return signed ? new SizeS(layout, p) : new SizeU(layout, p);
    }

    static final class ByteS
    extends Record
    implements Mapping.Byte {
        private final ValueLayout.OfByte layout;
        private final Mapping.Pointer p;

        ByteS(ValueLayout.OfByte layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return true;
        }

        @Override
        public int toInt(byte value) {
            return value;
        }

        @Override
        public byte fromInt(int value) {
            return (byte)value;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ByteS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ByteS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ByteS.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfByte layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class ByteU
    extends Record
    implements Mapping.Byte {
        private final ValueLayout.OfByte layout;
        private final Mapping.Pointer p;

        ByteU(ValueLayout.OfByte layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return false;
        }

        @Override
        public int toInt(byte value) {
            return Byte.toUnsignedInt(value);
        }

        @Override
        public byte fromInt(int value) {
            return (byte)(value & 0xFF);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ByteU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ByteU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ByteU.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfByte layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class ShortS
    extends Record
    implements Mapping.Short {
        private final ValueLayout.OfShort layout;
        private final Mapping.Pointer p;

        ShortS(ValueLayout.OfShort layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return true;
        }

        @Override
        public int toInt(short value) {
            return value;
        }

        @Override
        public short fromInt(int value) {
            return (short)value;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ShortS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ShortS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ShortS.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfShort layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class ShortU
    extends Record
    implements Mapping.Short {
        private final ValueLayout.OfShort layout;
        private final Mapping.Pointer p;

        ShortU(ValueLayout.OfShort layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return false;
        }

        @Override
        public int toInt(short value) {
            return Short.toUnsignedInt(value);
        }

        @Override
        public short fromInt(int value) {
            return (short)(value & 0xFFFF);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ShortU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ShortU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ShortU.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfShort layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class IntS
    extends Record
    implements Mapping.Int {
        private final ValueLayout.OfInt layout;
        private final Mapping.Pointer p;

        IntS(ValueLayout.OfInt layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return true;
        }

        @Override
        public long toLong(int value) {
            return value;
        }

        @Override
        public int fromLong(long value) {
            return (int)value;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IntS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IntS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IntS.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfInt layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class IntU
    extends Record
    implements Mapping.Int {
        private final ValueLayout.OfInt layout;
        private final Mapping.Pointer p;

        IntU(ValueLayout.OfInt layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return false;
        }

        @Override
        public long toLong(int value) {
            return Integer.toUnsignedLong(value);
        }

        @Override
        public int fromLong(long value) {
            return (int)(value & 0xFFFFFFFFL);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IntU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IntU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IntU.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfInt layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class LongS
    extends Record
    implements Mapping.Long {
        private final ValueLayout.OfLong layout;
        private final Mapping.Pointer p;

        LongS(ValueLayout.OfLong layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return true;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LongS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LongS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LongS.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfLong layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class LongU
    extends Record
    implements Mapping.Long {
        private final ValueLayout.OfLong layout;
        private final Mapping.Pointer p;

        LongU(ValueLayout.OfLong layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return false;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LongU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LongU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LongU.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfLong layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class CLong32S
    extends Record
    implements CLong32 {
        private final ValueLayout.OfInt layout;
        private final Mapping.Pointer p;

        CLong32S(ValueLayout.OfInt layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return true;
        }

        @Override
        public Mapping.CLong withByteAlignment(long byteAlignment) {
            return CLong32.create(this.layout.withByteAlignment(byteAlignment), true);
        }

        @Override
        public Mapping.CLong typedef(String name) {
            return CLong32.create(this.layout.withName(name), true);
        }

        @Override
        public Mapping.CLong cconst() {
            return CLong32.create(this.layout.withName(Mappings.nameConst(this.layout)), true);
        }

        @Override
        public long get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_INT_UNALIGNED, offset);
        }

        @Override
        public long getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_INT_UNALIGNED, index);
        }

        @Override
        public long getAligned(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_INT, offset);
        }

        @Override
        public long getAtIndexAligned(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_INT, index);
        }

        @Override
        public Mapping.CLong set(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.JAVA_INT_UNALIGNED, offset, (int)value);
            return this;
        }

        @Override
        public Mapping.CLong setAtIndex(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.JAVA_INT_UNALIGNED, index, (int)value);
            return this;
        }

        @Override
        public Mapping.CLong setAligned(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.JAVA_INT, offset, (int)value);
            return this;
        }

        @Override
        public Mapping.CLong setAtIndexAligned(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.JAVA_INT, index, (int)value);
            return this;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CLong32S.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CLong32S.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CLong32S.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfInt layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class CLong32U
    extends Record
    implements CLong32 {
        private final ValueLayout.OfInt layout;
        private final Mapping.Pointer p;

        CLong32U(ValueLayout.OfInt layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return false;
        }

        @Override
        public Mapping.CLong withByteAlignment(long byteAlignment) {
            return CLong32.create(this.layout.withByteAlignment(byteAlignment), false);
        }

        @Override
        public Mapping.CLong typedef(String name) {
            return CLong32.create(this.layout.withName(name), false);
        }

        @Override
        public Mapping.CLong cconst() {
            return CLong32.create(this.layout.withName(Mappings.nameConst(this.layout)), false);
        }

        @Override
        public long get(MemorySegment segment, long offset) {
            return Integer.toUnsignedLong(segment.get(ValueLayout.JAVA_INT_UNALIGNED, offset));
        }

        @Override
        public long getAtIndex(MemorySegment segment, long index) {
            return Integer.toUnsignedLong(segment.getAtIndex(ValueLayout.JAVA_INT_UNALIGNED, index));
        }

        @Override
        public long getAligned(MemorySegment segment, long offset) {
            return Integer.toUnsignedLong(segment.get(ValueLayout.JAVA_INT, offset));
        }

        @Override
        public long getAtIndexAligned(MemorySegment segment, long index) {
            return Integer.toUnsignedLong(segment.getAtIndex(ValueLayout.JAVA_INT, index));
        }

        @Override
        public Mapping.CLong set(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.JAVA_INT_UNALIGNED, offset, (int)(value & 0xFFFFFFFFL));
            return this;
        }

        @Override
        public Mapping.CLong setAtIndex(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.JAVA_INT_UNALIGNED, index, (int)(value & 0xFFFFFFFFL));
            return this;
        }

        @Override
        public Mapping.CLong setAligned(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.JAVA_INT, offset, (int)(value & 0xFFFFFFFFL));
            return this;
        }

        @Override
        public Mapping.CLong setAtIndexAligned(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.JAVA_INT, index, (int)(value & 0xFFFFFFFFL));
            return this;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CLong32U.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CLong32U.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CLong32U.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfInt layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class CLong64Impl
    extends Record
    implements CLong64 {
        private final ValueLayout.OfLong layout;
        private final Mapping.Pointer p;
        private final boolean signed;

        CLong64Impl(ValueLayout.OfLong layout, Mapping.Pointer p, boolean signed) {
            this.layout = layout;
            this.p = p;
            this.signed = signed;
        }

        @Override
        public Mapping.CLong withByteAlignment(long byteAlignment) {
            return CLong64.create(this.layout.withByteAlignment(byteAlignment), this.signed);
        }

        @Override
        public Mapping.CLong typedef(String name) {
            return CLong64.create(this.layout.withName(name), this.signed);
        }

        @Override
        public Mapping.CLong cconst() {
            return CLong64.create(this.layout.withName(Mappings.nameConst(this.layout)), this.signed);
        }

        @Override
        public long get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_LONG_UNALIGNED, offset);
        }

        @Override
        public long getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_LONG_UNALIGNED, index);
        }

        @Override
        public long getAligned(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_LONG, offset);
        }

        @Override
        public long getAtIndexAligned(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_LONG, index);
        }

        @Override
        public Mapping.CLong set(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.JAVA_LONG_UNALIGNED, offset, value);
            return this;
        }

        @Override
        public Mapping.CLong setAtIndex(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.JAVA_LONG_UNALIGNED, index, value);
            return this;
        }

        @Override
        public Mapping.CLong setAligned(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.JAVA_LONG, offset, value);
            return this;
        }

        @Override
        public Mapping.CLong setAtIndexAligned(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.JAVA_LONG, index, value);
            return this;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CLong64Impl.class, "layout;p;signed", "layout", "p", "signed"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CLong64Impl.class, "layout;p;signed", "layout", "p", "signed"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CLong64Impl.class, "layout;p;signed", "layout", "p", "signed"}, this, o);
        }

        @Override
        public ValueLayout.OfLong layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }

        @Override
        public boolean signed() {
            return this.signed;
        }
    }

    static final class SizeS
    extends Record
    implements Mapping.Size {
        private final AddressLayout layout;
        private final Mapping.Pointer p;

        SizeS(AddressLayout layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return true;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SizeS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SizeS.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SizeS.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public AddressLayout layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static final class SizeU
    extends Record
    implements Mapping.Size {
        private final AddressLayout layout;
        private final Mapping.Pointer p;

        SizeU(AddressLayout layout, Mapping.Pointer p) {
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return false;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SizeU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SizeU.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SizeU.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public AddressLayout layout() {
            return this.layout;
        }

        @Override
        public Mapping.Pointer p() {
            return this.p;
        }
    }

    static sealed interface CLong64
    extends Mapping.CLong
    permits CLong64Impl {
        public static Mapping.CLong create(ValueLayout.OfLong layout, boolean signed) {
            return new CLong64Impl(layout, new Mapping.Pointer(layout), signed);
        }
    }

    static sealed interface CLong32
    extends Mapping.CLong
    permits CLong32S, CLong32U {
        public static Mapping.CLong create(ValueLayout.OfInt layout, boolean signed) {
            Mapping.Pointer p = new Mapping.Pointer(layout);
            return signed ? new CLong32S(layout, p) : new CLong32U(layout, p);
        }
    }
}

