/*
 * Decompiled with CFR 0.152.
 */
package imgui.type;

public final class ImDouble
extends Number
implements Cloneable,
Comparable<ImDouble> {
    private final double[] data = new double[]{0.0};

    public ImDouble() {
    }

    public ImDouble(ImDouble imDouble) {
        this.data[0] = imDouble.data[0];
    }

    public ImDouble(double value) {
        this.set(value);
    }

    public double get() {
        return this.data[0];
    }

    public double[] getData() {
        return this.data;
    }

    public void set(double value) {
        this.data[0] = value;
    }

    public void set(ImDouble value) {
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
        ImDouble imDouble = (ImDouble)o;
        return this.data[0] == imDouble.data[0];
    }

    public int hashCode() {
        return Double.hashCode(this.data[0]);
    }

    public ImDouble clone() {
        return new ImDouble(this);
    }

    @Override
    public int compareTo(ImDouble o) {
        return Double.compare(this.get(), o.get());
    }

    @Override
    public int intValue() {
        return (int)this.get();
    }

    @Override
    public long longValue() {
        return (long)this.get();
    }

    @Override
    public float floatValue() {
        return (float)this.get();
    }

    @Override
    public double doubleValue() {
        return this.get();
    }
}

