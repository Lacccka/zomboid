/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.binary;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public final class BitString
implements Cloneable,
Serializable {
    private static final int WORD_LENGTH = 32;
    private final int length;
    private int[] data;

    public BitString(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be non-negative.");
        }
        this.length = length;
        this.data = new int[(length + 32 - 1) / 32];
    }

    public BitString(int length, Random rng) {
        this(length);
        for (int i = 0; i < this.data.length; ++i) {
            this.data[i] = rng.nextInt();
        }
        int bitsUsed = length % 32;
        if (bitsUsed < 32) {
            int unusedBits = 32 - bitsUsed;
            int mask = -1 >>> unusedBits;
            int n = this.data.length - 1;
            this.data[n] = this.data[n] & mask;
        }
    }

    public BitString(String value) {
        this(value.length());
        for (int i = 0; i < value.length(); ++i) {
            if (value.charAt(i) == '1') {
                this.setBit(value.length() - (i + 1), true);
                continue;
            }
            if (value.charAt(i) == '0') continue;
            throw new IllegalArgumentException("Illegal character at position " + i);
        }
    }

    public int getLength() {
        return this.length;
    }

    public boolean getBit(int index) {
        this.assertValidIndex(index);
        int word = index / 32;
        int offset = index % 32;
        return (this.data[word] & 1 << offset) != 0;
    }

    public void setBit(int index, boolean set) {
        this.assertValidIndex(index);
        int word = index / 32;
        int offset = index % 32;
        if (set) {
            int n = word;
            this.data[n] = this.data[n] | 1 << offset;
        } else {
            int n = word;
            this.data[n] = this.data[n] & ~(1 << offset);
        }
    }

    public void flipBit(int index) {
        this.assertValidIndex(index);
        int word = index / 32;
        int offset = index % 32;
        int n = word;
        this.data[n] = this.data[n] ^ 1 << offset;
    }

    private void assertValidIndex(int index) {
        if (index >= this.length || index < 0) {
            throw new IndexOutOfBoundsException("Invalid index: " + index + " (length: " + this.length + ")");
        }
    }

    public int countSetBits() {
        int count = 0;
        for (int x : this.data) {
            while (x != 0) {
                x &= x - 1;
                ++count;
            }
        }
        return count;
    }

    public int countUnsetBits() {
        return this.length - this.countSetBits();
    }

    public BigInteger toNumber() {
        return new BigInteger(this.toString(), 2);
    }

    public void swapSubstring(BitString other, int start, int length) {
        this.assertValidIndex(start);
        other.assertValidIndex(start);
        int word = start / 32;
        int partialWordSize = (32 - start) % 32;
        if (partialWordSize > 0) {
            this.swapBits(other, word, -1 << 32 - partialWordSize);
            ++word;
        }
        int remainingBits = length - partialWordSize;
        int stop = remainingBits / 32;
        for (int i = word; i < stop; ++i) {
            int temp = this.data[i];
            this.data[i] = other.data[i];
            other.data[i] = temp;
        }
        if ((remainingBits %= 32) > 0) {
            this.swapBits(other, word, -1 >>> 32 - remainingBits);
        }
    }

    private void swapBits(BitString other, int word, int swapMask) {
        int preserveMask = ~swapMask;
        int preservedThis = this.data[word] & preserveMask;
        int preservedThat = other.data[word] & preserveMask;
        int swapThis = this.data[word] & swapMask;
        int swapThat = other.data[word] & swapMask;
        this.data[word] = preservedThis | swapThat;
        other.data[word] = preservedThat | swapThis;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (int i = this.length - 1; i >= 0; --i) {
            buffer.append(this.getBit(i) ? (char)'1' : '0');
        }
        return buffer.toString();
    }

    public BitString clone() {
        try {
            BitString clone = (BitString)super.clone();
            clone.data = (int[])this.data.clone();
            return clone;
        }
        catch (CloneNotSupportedException ex) {
            throw (Error)new InternalError("Cloning failed.").initCause(ex);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BitString bitString = (BitString)o;
        return this.length == bitString.length && Arrays.equals(this.data, bitString.data);
    }

    public int hashCode() {
        int result = this.length;
        result = 31 * result + Arrays.hashCode(this.data);
        return result;
    }
}

