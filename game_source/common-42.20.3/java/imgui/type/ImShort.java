/*
 * Decompiled with CFR 0.152.
 */
package imgui.type;

public final class ImShort
extends Number
implements Cloneable,
Comparable<ImShort> {
    private final short[] data = new short[]{0};

    public ImShort() {
    }

    public ImShort(ImShort imShort) {
        this.data[0] = imShort.data[0];
    }

    public ImShort(short value) {
        this.set(value);
    }

    public short get() {
        return this.data[0];
    }

    public short[] getData() {
        return this.data;
    }

    public void set(short value) {
        this.data[0] = value;
    }

    public void set(ImShort value) {
        this.set(value.get());
    }

    public String toString() {
        return String.valueOf(this.get());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ImShort imShort = (ImShort)o;
        return this.data[0] == imShort.data[0];
    }

    public int hashCode() {
        return Short.hashCode(this.data[0]);
    }

    public ImShort clone() {
        return new ImShort(this);
    }

    @Override
    public int compareTo(ImShort o) {
        return Short.compare(this.get(), o.get());
    }

    @Override
    public int intValue() {
        return this.get();
    }

    @Override
    public long longValue() {
        return this.get();
    }

    @Override
    public float floatValue() {
        return this.get();
    }

    @Override
    public double doubleValue() {
        return this.get();
    }
}

