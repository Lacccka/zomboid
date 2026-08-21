/*
 * Decompiled with CFR 0.152.
 */
package imgui.type;

public final class ImLong
extends Number
implements Cloneable,
Comparable<ImLong> {
    private final long[] data = new long[]{0L};

    public ImLong() {
    }

    public ImLong(ImLong imLong) {
        this.data[0] = imLong.data[0];
    }

    public ImLong(long value) {
        this.set(value);
    }

    public long get() {
        return this.data[0];
    }

    public long[] getData() {
        return this.data;
    }

    public void set(long value) {
        this.data[0] = value;
    }

    public void set(ImLong value) {
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
        ImLong imLong = (ImLong)o;
        return this.data[0] == imLong.data[0];
    }

    public int hashCode() {
        return Long.hashCode(this.data[0]);
    }

    public ImLong clone() {
        return new ImLong(this);
    }

    @Override
    public int compareTo(ImLong o) {
        return Long.compare(this.get(), o.get());
    }

    @Override
    public int intValue() {
        return (int)this.get();
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

