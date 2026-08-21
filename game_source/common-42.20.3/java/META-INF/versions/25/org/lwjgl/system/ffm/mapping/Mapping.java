/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm.mapping;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import org.lwjgl.system.ffm.mapping.DataMapping;
import org.lwjgl.system.ffm.mapping.GroupMapping;
import org.lwjgl.system.ffm.mapping.IntegerMapping;
import org.lwjgl.system.ffm.mapping.IntegerMappings;
import org.lwjgl.system.ffm.mapping.Mappings;
import org.lwjgl.system.ffm.mapping.PrimitiveMapping;
import org.lwjgl.system.ffm.mapping.PrimitiveMappings;

public interface Mapping<L extends MemoryLayout> {
    public L layout();

    public Mapping<L> typedef(String var1);

    public Mapping<L> cconst();

    default public Pointer p() {
        return new Pointer((MemoryLayout)this.layout());
    }

    public static Opaque createOpaque(String name) {
        return new Opaque(name);
    }

    public static Boolean createBoolean(String name) {
        return PrimitiveMappings.create(ValueLayout.JAVA_BOOLEAN.withName(name));
    }

    public static Byte createByte(String name, boolean signed) {
        return IntegerMappings.create(ValueLayout.JAVA_BYTE.withName(name), signed);
    }

    public static Char createChar(String name) {
        return IntegerMappings.create(ValueLayout.JAVA_CHAR.withName(name));
    }

    public static Short createShort(String name, boolean signed) {
        return IntegerMappings.create(ValueLayout.JAVA_SHORT.withName(name), signed);
    }

    public static Int createInt(String name, boolean signed) {
        return IntegerMappings.create(ValueLayout.JAVA_INT.withName(name), signed);
    }

    public static Long createLong(String name, boolean signed) {
        return IntegerMappings.create(ValueLayout.JAVA_LONG.withName(name), signed);
    }

    public static CLong createCLong(String name, boolean signed) {
        return IntegerMappings.createCLong(name, signed);
    }

    public static Size createSize(String name, boolean signed) {
        return IntegerMappings.create(ValueLayout.ADDRESS.withName(name), signed);
    }

    public static Float createFloat(String name) {
        return PrimitiveMappings.create(ValueLayout.JAVA_FLOAT.withName(name));
    }

    public static Double createDouble(String name) {
        return PrimitiveMappings.create(ValueLayout.JAVA_DOUBLE.withName(name));
    }

    public static final class Pointer
    extends Record
    implements DataMapping<AddressLayout> {
        private final AddressLayout layout;

        public Pointer(AddressLayout layout) {
            Mappings.check(layout);
            if (layout.targetLayout().isEmpty()) {
                throw new IllegalArgumentException("Pointer layout must have a target layout");
            }
            this.layout = layout;
        }

        Pointer(MemoryLayout targetLayout) {
            this(name + ((name = targetLayout.name().orElseThrow()).endsWith("*") ? "*" : " *"), targetLayout);
            String name;
        }

        private Pointer(String name, MemoryLayout targetLayout) {
            this(ValueLayout.ADDRESS.withTargetLayout(targetLayout).withName(name));
        }

        public Pointer withByteAlignment(long byteAlignment) {
            return new Pointer(this.layout.withByteAlignment(byteAlignment));
        }

        public Pointer typedef(String name) {
            return new Pointer(this.layout.withName(name));
        }

        @Override
        public Mapping<AddressLayout> cconst() {
            return new Pointer(this.layout.withName(Mappings.nameConst(this.layout)));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Pointer.class, "layout", "layout"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Pointer.class, "layout", "layout"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Pointer.class, "layout", "layout"}, this, o);
        }

        @Override
        public AddressLayout layout() {
            return this.layout;
        }
    }

    public static final class Opaque
    extends Record
    implements Mapping<AddressLayout> {
        private final AddressLayout layout;

        public Opaque(AddressLayout layout) {
            Mappings.check(layout);
            this.layout = layout;
        }

        Opaque(String name) {
            this(ValueLayout.ADDRESS.withName(name));
        }

        public Opaque typedef(String name) {
            return new Opaque(name);
        }

        public Opaque cconst() {
            return new Opaque(this.layout().withName(Mappings.nameConst(this.layout())));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Opaque.class, "layout", "layout"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Opaque.class, "layout", "layout"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Opaque.class, "layout", "layout"}, this, o);
        }

        @Override
        public AddressLayout layout() {
            return this.layout;
        }
    }

    public static final class Boolean
    extends Record
    implements PrimitiveMapping<ValueLayout.OfBoolean> {
        private final ValueLayout.OfBoolean layout;
        private final Pointer p;

        public Boolean(ValueLayout.OfBoolean layout, Pointer p) {
            Mappings.check(layout);
            this.layout = layout;
            this.p = p;
        }

        private Boolean(ValueLayout.OfBoolean layout) {
            this(layout, new Pointer(layout));
        }

        public Boolean(String name) {
            this(ValueLayout.JAVA_BOOLEAN.withName(name));
        }

        @Override
        public Boolean withByteAlignment(long byteAlignment) {
            return new Boolean(this.layout.withByteAlignment(byteAlignment));
        }

        @Override
        public Boolean typedef(String name) {
            return new Boolean(this.layout.withName(name));
        }

        @Override
        public Mapping<ValueLayout.OfBoolean> cconst() {
            return new Boolean(this.layout.withName(Mappings.nameConst(this.layout)));
        }

        public boolean get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_BOOLEAN, offset);
        }

        public boolean getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_BOOLEAN, index);
        }

        public Boolean set(MemorySegment segment, long offset, boolean value) {
            segment.set(ValueLayout.JAVA_BOOLEAN, offset, value);
            return this;
        }

        public Boolean setAtIndex(MemorySegment segment, long index, boolean value) {
            segment.setAtIndex(ValueLayout.JAVA_BOOLEAN, index, value);
            return this;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Boolean.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Boolean.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Boolean.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfBoolean layout() {
            return this.layout;
        }

        @Override
        public Pointer p() {
            return this.p;
        }
    }

    public static sealed interface Byte
    extends IntegerMapping<ValueLayout.OfByte>
    permits IntegerMappings.ByteS, IntegerMappings.ByteU {
        @Override
        default public Byte withByteAlignment(long byteAlignment) {
            return IntegerMappings.create(((ValueLayout.OfByte)this.layout()).withByteAlignment(byteAlignment), this.signed());
        }

        @Override
        default public Byte typedef(String name) {
            return IntegerMappings.create(((ValueLayout.OfByte)this.layout()).withName(name), this.signed());
        }

        default public Byte cconst() {
            return IntegerMappings.create(((ValueLayout.OfByte)this.layout()).withName(Mappings.nameConst(this.layout())), this.signed());
        }

        default public byte get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_BYTE, offset);
        }

        default public byte getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_BYTE, index);
        }

        default public Byte set(MemorySegment segment, long offset, byte value) {
            segment.set(ValueLayout.JAVA_BYTE, offset, value);
            return this;
        }

        default public Byte setAtIndex(MemorySegment segment, long index, byte value) {
            segment.setAtIndex(ValueLayout.JAVA_BYTE, index, value);
            return this;
        }

        public int toInt(byte var1);

        public byte fromInt(int var1);

        default public int get32(MemorySegment segment, long offset) {
            return this.toInt(this.get(segment, offset));
        }

        default public int get32AtIndex(MemorySegment segment, long index) {
            return this.toInt(this.getAtIndex(segment, index));
        }

        default public Byte set(MemorySegment segment, long offset, int value) {
            return this.set(segment, offset, this.fromInt(value));
        }

        default public Byte setAtIndex(MemorySegment segment, long index, int value) {
            return this.setAtIndex(segment, index, this.fromInt(value));
        }
    }

    public static final class Char
    extends Record
    implements IntegerMapping<ValueLayout.OfChar> {
        private final ValueLayout.OfChar layout;
        private final Pointer p;

        public Char(ValueLayout.OfChar layout, Pointer p) {
            Mappings.check(layout);
            this.layout = layout;
            this.p = p;
        }

        @Override
        public boolean signed() {
            return false;
        }

        @Override
        public Char withByteAlignment(long byteAlignment) {
            return IntegerMappings.create(this.layout.withByteAlignment(byteAlignment));
        }

        @Override
        public Char typedef(String name) {
            return IntegerMappings.create(this.layout.withName(name));
        }

        public Char cconst() {
            return IntegerMappings.create(this.layout.withName(Mappings.nameConst(this.layout)));
        }

        public char get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_CHAR_UNALIGNED, offset);
        }

        public char getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_CHAR_UNALIGNED, index);
        }

        public char getAligned(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_CHAR, offset);
        }

        public char getAtIndexAligned(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_CHAR, index);
        }

        public Char set(MemorySegment segment, long offset, char value) {
            segment.set(ValueLayout.JAVA_CHAR_UNALIGNED, offset, value);
            return this;
        }

        public Char setAtIndex(MemorySegment segment, long index, char value) {
            segment.setAtIndex(ValueLayout.JAVA_CHAR_UNALIGNED, index, value);
            return this;
        }

        public Char setAligned(MemorySegment segment, long offset, char value) {
            segment.set(ValueLayout.JAVA_CHAR, offset, value);
            return this;
        }

        public Char setAtIndexAligned(MemorySegment segment, long index, char value) {
            segment.setAtIndex(ValueLayout.JAVA_CHAR, index, value);
            return this;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Char.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Char.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Char.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfChar layout() {
            return this.layout;
        }

        @Override
        public Pointer p() {
            return this.p;
        }
    }

    public static sealed interface Short
    extends IntegerMapping<ValueLayout.OfShort>
    permits IntegerMappings.ShortS, IntegerMappings.ShortU {
        @Override
        default public Short withByteAlignment(long byteAlignment) {
            return IntegerMappings.create(((ValueLayout.OfShort)this.layout()).withByteAlignment(byteAlignment), this.signed());
        }

        @Override
        default public Short typedef(String name) {
            return IntegerMappings.create(((ValueLayout.OfShort)this.layout()).withName(name), this.signed());
        }

        default public Short cconst() {
            return IntegerMappings.create(((ValueLayout.OfShort)this.layout()).withName(Mappings.nameConst(this.layout())), this.signed());
        }

        default public short get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_SHORT_UNALIGNED, offset);
        }

        default public short getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_SHORT_UNALIGNED, index);
        }

        default public short getAligned(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_SHORT, offset);
        }

        default public short getAtIndexAligned(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_SHORT, index);
        }

        default public Short set(MemorySegment segment, long offset, short value) {
            segment.set(ValueLayout.JAVA_SHORT_UNALIGNED, offset, value);
            return this;
        }

        default public Short setAtIndex(MemorySegment segment, long index, short value) {
            segment.setAtIndex(ValueLayout.JAVA_SHORT_UNALIGNED, index, value);
            return this;
        }

        default public Short setAligned(MemorySegment segment, long offset, short value) {
            segment.set(ValueLayout.JAVA_SHORT, offset, value);
            return this;
        }

        default public Short setAtIndexAligned(MemorySegment segment, long index, short value) {
            segment.setAtIndex(ValueLayout.JAVA_SHORT, index, value);
            return this;
        }

        public int toInt(short var1);

        public short fromInt(int var1);

        default public int get32(MemorySegment segment, long offset) {
            return this.toInt(this.get(segment, offset));
        }

        default public int get32AtIndex(MemorySegment segment, long index) {
            return this.toInt(this.getAtIndex(segment, index));
        }

        default public Short set(MemorySegment segment, long offset, int value) {
            return this.set(segment, offset, this.fromInt(value));
        }

        default public Short setAtIndex(MemorySegment segment, long index, int value) {
            return this.setAtIndex(segment, index, this.fromInt(value));
        }

        default public int get32Aligned(MemorySegment segment, long offset) {
            return this.toInt(this.getAligned(segment, offset));
        }

        default public int get32AtIndexAligned(MemorySegment segment, long index) {
            return this.toInt(this.getAtIndexAligned(segment, index));
        }

        default public Short setAligned(MemorySegment segment, long offset, int value) {
            return this.setAligned(segment, offset, this.fromInt(value));
        }

        default public Short setAtIndexAligned(MemorySegment segment, long index, int value) {
            return this.setAtIndexAligned(segment, index, this.fromInt(value));
        }
    }

    public static sealed interface Int
    extends IntegerMapping<ValueLayout.OfInt>
    permits IntegerMappings.IntS, IntegerMappings.IntU {
        @Override
        default public Int withByteAlignment(long byteAlignment) {
            return IntegerMappings.create(((ValueLayout.OfInt)this.layout()).withByteAlignment(byteAlignment), this.signed());
        }

        @Override
        default public Int typedef(String name) {
            return IntegerMappings.create(((ValueLayout.OfInt)this.layout()).withName(name), this.signed());
        }

        default public Int cconst() {
            return IntegerMappings.create(((ValueLayout.OfInt)this.layout()).withName(Mappings.nameConst(this.layout())), this.signed());
        }

        default public int get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_INT_UNALIGNED, offset);
        }

        default public int getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_INT_UNALIGNED, index);
        }

        default public int getAligned(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_INT, offset);
        }

        default public int getAtIndexAligned(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_INT, index);
        }

        default public Int set(MemorySegment segment, long offset, int value) {
            segment.set(ValueLayout.JAVA_INT_UNALIGNED, offset, value);
            return this;
        }

        default public Int setAtIndex(MemorySegment segment, long index, int value) {
            segment.setAtIndex(ValueLayout.JAVA_INT_UNALIGNED, index, value);
            return this;
        }

        default public Int setAligned(MemorySegment segment, long offset, int value) {
            segment.set(ValueLayout.JAVA_INT, offset, value);
            return this;
        }

        default public Int setAtIndexAligned(MemorySegment segment, long index, int value) {
            segment.setAtIndex(ValueLayout.JAVA_INT, index, value);
            return this;
        }

        public long toLong(int var1);

        public int fromLong(long var1);

        default public long get64(MemorySegment segment, long offset) {
            return this.toLong(this.get(segment, offset));
        }

        default public long get64AtIndex(MemorySegment segment, long index) {
            return this.toLong(this.getAtIndex(segment, index));
        }

        default public Int set(MemorySegment segment, long offset, long value) {
            return this.set(segment, offset, this.fromLong(value));
        }

        default public Int setAtIndex(MemorySegment segment, long index, long value) {
            return this.setAtIndex(segment, index, this.fromLong(value));
        }

        default public long get64Aligned(MemorySegment segment, long offset) {
            return this.toLong(this.getAligned(segment, offset));
        }

        default public long get64AtIndexAligned(MemorySegment segment, long index) {
            return this.toLong(this.getAtIndexAligned(segment, index));
        }

        default public Int setAligned(MemorySegment segment, long offset, long value) {
            return this.setAligned(segment, offset, this.fromLong(value));
        }

        default public Int setAtIndexAligned(MemorySegment segment, long index, long value) {
            return this.setAtIndexAligned(segment, index, this.fromLong(value));
        }
    }

    public static sealed interface Long
    extends IntegerMapping<ValueLayout.OfLong>
    permits IntegerMappings.LongS, IntegerMappings.LongU {
        @Override
        default public Long withByteAlignment(long byteAlignment) {
            return IntegerMappings.create(((ValueLayout.OfLong)this.layout()).withByteAlignment(byteAlignment), this.signed());
        }

        @Override
        default public Long typedef(String name) {
            return IntegerMappings.create(((ValueLayout.OfLong)this.layout()).withName(name), this.signed());
        }

        default public Long cconst() {
            return IntegerMappings.create(((ValueLayout.OfLong)this.layout()).withName(Mappings.nameConst(this.layout())), this.signed());
        }

        default public long get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_LONG_UNALIGNED, offset);
        }

        default public long getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_LONG_UNALIGNED, index);
        }

        default public long getAligned(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_LONG, offset);
        }

        default public long getAtIndexAligned(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_LONG, index);
        }

        default public Long set(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.JAVA_LONG_UNALIGNED, offset, value);
            return this;
        }

        default public Long setAtIndex(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.JAVA_LONG_UNALIGNED, index, value);
            return this;
        }

        default public Long setAligned(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.JAVA_LONG, offset, value);
            return this;
        }

        default public Long setAtIndexAligned(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.JAVA_LONG, index, value);
            return this;
        }
    }

    public static sealed interface CLong
    extends IntegerMapping<ValueLayout>
    permits IntegerMappings.CLong32, IntegerMappings.CLong64 {
        @Override
        public CLong withByteAlignment(long var1);

        @Override
        public CLong typedef(String var1);

        public CLong cconst();

        public long get(MemorySegment var1, long var2);

        public long getAtIndex(MemorySegment var1, long var2);

        public long getAligned(MemorySegment var1, long var2);

        public long getAtIndexAligned(MemorySegment var1, long var2);

        public CLong set(MemorySegment var1, long var2, long var4);

        public CLong setAtIndex(MemorySegment var1, long var2, long var4);

        public CLong setAligned(MemorySegment var1, long var2, long var4);

        public CLong setAtIndexAligned(MemorySegment var1, long var2, long var4);
    }

    public static sealed interface Size
    extends IntegerMapping<AddressLayout>
    permits IntegerMappings.SizeS, IntegerMappings.SizeU {
        @Override
        default public Size withByteAlignment(long byteAlignment) {
            return IntegerMappings.create(((AddressLayout)this.layout()).withByteAlignment(byteAlignment), this.signed());
        }

        @Override
        default public Size typedef(String name) {
            return IntegerMappings.create(((AddressLayout)this.layout()).withName(name), this.signed());
        }

        default public Size cconst() {
            return IntegerMappings.create(((AddressLayout)this.layout()).withName(Mappings.nameConst(this.layout())), this.signed());
        }

        default public long get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.ADDRESS_UNALIGNED, offset).address();
        }

        default public long getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.ADDRESS_UNALIGNED, index).address();
        }

        default public long getAligned(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.ADDRESS, offset).address();
        }

        default public long getAtIndexAligned(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.ADDRESS, index).address();
        }

        default public Size set(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.ADDRESS_UNALIGNED, offset, MemorySegment.ofAddress(value));
            return this;
        }

        default public Size setAtIndex(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.ADDRESS_UNALIGNED, index, MemorySegment.ofAddress(value));
            return this;
        }

        default public Size setAligned(MemorySegment segment, long offset, long value) {
            segment.set(ValueLayout.ADDRESS, offset, MemorySegment.ofAddress(value));
            return this;
        }

        default public Size setAtIndexAligned(MemorySegment segment, long index, long value) {
            segment.setAtIndex(ValueLayout.ADDRESS, index, MemorySegment.ofAddress(value));
            return this;
        }
    }

    public static final class Float
    extends Record
    implements PrimitiveMapping<ValueLayout.OfFloat> {
        private final ValueLayout.OfFloat layout;
        private final Pointer p;

        public Float(ValueLayout.OfFloat layout, Pointer p) {
            Mappings.check(layout);
            this.layout = layout;
            this.p = p;
        }

        @Override
        public Float withByteAlignment(long byteAlignment) {
            return PrimitiveMappings.create(this.layout.withByteAlignment(byteAlignment));
        }

        @Override
        public Float typedef(String name) {
            return PrimitiveMappings.create(this.layout.withName(name));
        }

        public Float cconst() {
            return PrimitiveMappings.create(this.layout.withName(Mappings.nameConst(this.layout)));
        }

        public float get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_FLOAT_UNALIGNED, offset);
        }

        public float getAligned(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_FLOAT, offset);
        }

        public float getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_FLOAT_UNALIGNED, index);
        }

        public float getAtIndexAligned(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_FLOAT, index);
        }

        public Float set(MemorySegment segment, long offset, float value) {
            segment.set(ValueLayout.JAVA_FLOAT_UNALIGNED, offset, value);
            return this;
        }

        public Float setAligned(MemorySegment segment, long offset, float value) {
            segment.set(ValueLayout.JAVA_FLOAT, offset, value);
            return this;
        }

        public Float setAtIndex(MemorySegment segment, long index, float value) {
            segment.setAtIndex(ValueLayout.JAVA_FLOAT_UNALIGNED, index, value);
            return this;
        }

        public Float setAtIndexAligned(MemorySegment segment, long index, float value) {
            segment.setAtIndex(ValueLayout.JAVA_FLOAT, index, value);
            return this;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Float.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Float.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Float.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfFloat layout() {
            return this.layout;
        }

        @Override
        public Pointer p() {
            return this.p;
        }
    }

    public static final class Double
    extends Record
    implements PrimitiveMapping<ValueLayout.OfDouble> {
        private final ValueLayout.OfDouble layout;
        private final Pointer p;

        public Double(ValueLayout.OfDouble layout, Pointer p) {
            Mappings.check(layout);
            this.layout = layout;
            this.p = p;
        }

        @Override
        public Double withByteAlignment(long byteAlignment) {
            return PrimitiveMappings.create(this.layout.withByteAlignment(byteAlignment));
        }

        @Override
        public Double typedef(String name) {
            return PrimitiveMappings.create(this.layout.withName(name));
        }

        public Double cconst() {
            return PrimitiveMappings.create(this.layout().withName(Mappings.nameConst(this.layout())));
        }

        public double get(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, offset);
        }

        public double getAligned(MemorySegment segment, long offset) {
            return segment.get(ValueLayout.JAVA_DOUBLE, offset);
        }

        public double getAtIndex(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_DOUBLE_UNALIGNED, index);
        }

        public double getAtIndexAligned(MemorySegment segment, long index) {
            return segment.getAtIndex(ValueLayout.JAVA_DOUBLE, index);
        }

        public Double set(MemorySegment segment, long offset, double value) {
            segment.set(ValueLayout.JAVA_DOUBLE_UNALIGNED, offset, value);
            return this;
        }

        public Double setAligned(MemorySegment segment, long offset, double value) {
            segment.set(ValueLayout.JAVA_DOUBLE, offset, value);
            return this;
        }

        public Double setAtIndex(MemorySegment segment, long index, double value) {
            segment.setAtIndex(ValueLayout.JAVA_DOUBLE_UNALIGNED, index, value);
            return this;
        }

        public Double setAtIndexAligned(MemorySegment segment, long index, double value) {
            segment.setAtIndex(ValueLayout.JAVA_DOUBLE, index, value);
            return this;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Double.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Double.class, "layout;p", "layout", "p"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Double.class, "layout;p", "layout", "p"}, this, o);
        }

        @Override
        public ValueLayout.OfDouble layout() {
            return this.layout;
        }

        @Override
        public Pointer p() {
            return this.p;
        }
    }

    public static final class Union
    extends Record
    implements GroupMapping<UnionLayout> {
        private final UnionLayout layout;

        public Union(UnionLayout layout) {
            Mappings.check(layout);
            this.layout = layout;
        }

        public Union(String name, UnionLayout layout) {
            this(layout.withName(name));
        }

        @Override
        public Union withByteAlignment(long byteAlignment) {
            return new Union(this.layout.withByteAlignment(byteAlignment));
        }

        @Override
        public Union typedef(String name) {
            return new Union(this.layout.withName(name));
        }

        public Union cconst() {
            return new Union(this.layout.withName(Mappings.nameConst(this.layout)));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Union.class, "layout", "layout"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Union.class, "layout", "layout"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Union.class, "layout", "layout"}, this, o);
        }

        @Override
        public UnionLayout layout() {
            return this.layout;
        }
    }

    public static final class Struct
    extends Record
    implements GroupMapping<StructLayout> {
        private final StructLayout layout;

        public Struct(StructLayout layout) {
            Mappings.check(layout);
            this.layout = layout;
        }

        public Struct(String name, StructLayout layout) {
            this(layout.withName(name));
        }

        @Override
        public Struct withByteAlignment(long byteAlignment) {
            return new Struct(this.layout.withByteAlignment(byteAlignment));
        }

        @Override
        public Struct typedef(String name) {
            return new Struct(this.layout.withName(name));
        }

        public Struct cconst() {
            return new Struct(this.layout.withName(Mappings.nameConst(this.layout)));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Struct.class, "layout", "layout"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Struct.class, "layout", "layout"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Struct.class, "layout", "layout"}, this, o);
        }

        @Override
        public StructLayout layout() {
            return this.layout;
        }
    }

    public static final class Sequence
    extends Record
    implements DataMapping<SequenceLayout> {
        private final SequenceLayout layout;

        public Sequence(SequenceLayout layout) {
            Mappings.check(layout);
            this.layout = layout;
        }

        public Sequence(DataMapping<?> elementMapping, long elementCount) {
            String name = elementMapping.layout().name().orElseThrow();
            this(name + "[" + elementCount + "]", elementMapping, elementCount);
        }

        public Sequence(String name, DataMapping<?> elementMapping, long elementCount) {
            this(name, (MemoryLayout)elementMapping.layout(), elementCount);
        }

        private Sequence(String name, MemoryLayout elementLayout, long elementCount) {
            this(MemoryLayout.sequenceLayout(elementCount, elementLayout).withName(name));
        }

        public Sequence withByteAlignment(long byteAlignment) {
            return new Sequence(this.layout.withByteAlignment(byteAlignment));
        }

        public Sequence typedef(String name) {
            return new Sequence(this.layout.withName(name));
        }

        public Sequence cconst() {
            return new Sequence(this.layout.withName(Mappings.nameConst(this.layout)));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Sequence.class, "layout", "layout"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Sequence.class, "layout", "layout"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Sequence.class, "layout", "layout"}, this, o);
        }

        @Override
        public SequenceLayout layout() {
            return this.layout;
        }
    }
}

