/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.common;

import java.util.Arrays;

public final class BitArray
implements Cloneable {
    private int[] bits;
    private int size;

    public BitArray() {
        this.size = 0;
        this.bits = new int[1];
    }

    public BitArray(int size) {
        this.size = size;
        this.bits = BitArray.makeArray(size);
    }

    BitArray(int[] bits, int size) {
        this.bits = bits;
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public int getSizeInBytes() {
        return (this.size + 7) / 8;
    }

    private void ensureCapacity(int size) {
        if (size > this.bits.length * 32) {
            int[] newBits = BitArray.makeArray(size);
            System.arraycopy(this.bits, 0, newBits, 0, this.bits.length);
            this.bits = newBits;
        }
    }

    public boolean get(int i) {
        return (this.bits[i / 32] & 1 << (i & 0x1F)) != 0;
    }

    public void set(int i) {
        int n = i / 32;
        this.bits[n] = this.bits[n] | 1 << (i & 0x1F);
    }

    public void flip(int i) {
        int n = i / 32;
        this.bits[n] = this.bits[n] ^ 1 << (i & 0x1F);
    }

    public int getNextSet(int from) {
        if (from >= this.size) {
            return this.size;
        }
        int bitsOffset = from / 32;
        int currentBits = this.bits[bitsOffset];
        currentBits &= ~((1 << (from & 0x1F)) - 1);
        while (currentBits == 0) {
            if (++bitsOffset == this.bits.length) {
                return this.size;
            }
            currentBits = this.bits[bitsOffset];
        }
        int result = bitsOffset * 32 + Integer.numberOfTrailingZeros(currentBits);
        return result > this.size ? this.size : result;
    }

    public int getNextUnset(int from) {
        if (from >= this.size) {
            return this.size;
        }
        int bitsOffset = from / 32;
        int currentBits = ~this.bits[bitsOffset];
        currentBits &= ~((1 << (from & 0x1F)) - 1);
        while (currentBits == 0) {
            if (++bitsOffset == this.bits.length) {
                return this.size;
            }
            currentBits = ~this.bits[bitsOffset];
        }
        int result = bitsOffset * 32 + Integer.numberOfTrailingZeros(currentBits);
        return result > this.size ? this.size : result;
    }

    public void setBulk(int i, int newBits) {
        this.bits[i / 32] = newBits;
    }

    public void setRange(int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException();
        }
        if (end == start) {
            return;
        }
        int firstInt = start / 32;
        int lastInt = --end / 32;
        int i = firstInt;
        while (i <= lastInt) {
            int mask;
            int lastBit;
            int firstBit = i > firstInt ? 0 : start & 0x1F;
            int n = lastBit = i < lastInt ? 31 : end & 0x1F;
            if (firstBit == 0 && lastBit == 31) {
                mask = -1;
            } else {
                mask = 0;
                for (int j = firstBit; j <= lastBit; ++j) {
                    mask |= 1 << j;
                }
            }
            int n2 = i++;
            this.bits[n2] = this.bits[n2] | mask;
        }
    }

    public void clear() {
        int max = this.bits.length;
        for (int i = 0; i < max; ++i) {
            this.bits[i] = 0;
        }
    }

    public boolean isRange(int start, int end, boolean value) {
        if (end < start) {
            throw new IllegalArgumentException();
        }
        if (end == start) {
            return true;
        }
        int firstInt = start / 32;
        int lastInt = --end / 32;
        for (int i = firstInt; i <= lastInt; ++i) {
            int mask;
            int lastBit;
            int firstBit = i > firstInt ? 0 : start & 0x1F;
            int n = lastBit = i < lastInt ? 31 : end & 0x1F;
            if (firstBit == 0 && lastBit == 31) {
                mask = -1;
            } else {
                mask = 0;
                for (int j = firstBit; j <= lastBit; ++j) {
                    mask |= 1 << j;
                }
            }
            if ((this.bits[i] & mask) == (value ? mask : 0)) continue;
            return false;
        }
        return true;
    }

    public void appendBit(boolean bit) {
        this.ensureCapacity(this.size + 1);
        if (bit) {
            int n = this.size / 32;
            this.bits[n] = this.bits[n] | 1 << (this.size & 0x1F);
        }
        ++this.size;
    }

    public void appendBits(int value, int numBits) {
        if (numBits < 0 || numBits > 32) {
            throw new IllegalArgumentException("Num bits must be between 0 and 32");
        }
        this.ensureCapacity(this.size + numBits);
        for (int numBitsLeft = numBits; numBitsLeft > 0; --numBitsLeft) {
            this.appendBit((value >> numBitsLeft - 1 & 1) == 1);
        }
    }

    public void appendBitArray(BitArray other) {
        int otherSize = other.size;
        this.ensureCapacity(this.size + otherSize);
        for (int i = 0; i < otherSize; ++i) {
            this.appendBit(other.get(i));
        }
    }

    public void xor(BitArray other) {
        if (this.bits.length != other.bits.length) {
            throw new IllegalArgumentException("Sizes don't match");
        }
        for (int i = 0; i < this.bits.length; ++i) {
            int n = i;
            this.bits[n] = this.bits[n] ^ other.bits[i];
        }
    }

    public void toBytes(int bitOffset, byte[] array, int offset, int numBytes) {
        for (int i = 0; i < numBytes; ++i) {
            int theByte = 0;
            for (int j = 0; j < 8; ++j) {
                if (this.get(bitOffset)) {
                    theByte |= 1 << 7 - j;
                }
                ++bitOffset;
            }
            array[offset + i] = (byte)theByte;
        }
    }

    public int[] getBitArray() {
        return this.bits;
    }

    public void reverse() {
        int[] newBits = new int[this.bits.length];
        int len = (this.size - 1) / 32;
        int oldBitsLen = len + 1;
        for (int i = 0; i < oldBitsLen; ++i) {
            long x = this.bits[i];
            x = x >> 1 & 0x55555555L | (x & 0x55555555L) << 1;
            x = x >> 2 & 0x33333333L | (x & 0x33333333L) << 2;
            x = x >> 4 & 0xF0F0F0FL | (x & 0xF0F0F0FL) << 4;
            x = x >> 8 & 0xFF00FFL | (x & 0xFF00FFL) << 8;
            x = x >> 16 & 0xFFFFL | (x & 0xFFFFL) << 16;
            newBits[len - i] = (int)x;
        }
        if (this.size != oldBitsLen * 32) {
            int leftOffset = oldBitsLen * 32 - this.size;
            int mask = 1;
            for (int i = 0; i < 31 - leftOffset; ++i) {
                mask = mask << 1 | 1;
            }
            int currentInt = newBits[0] >> leftOffset & mask;
            for (int i = 1; i < oldBitsLen; ++i) {
                int nextInt = newBits[i];
                newBits[i - 1] = currentInt |= nextInt << 32 - leftOffset;
                currentInt = nextInt >> leftOffset & mask;
            }
            newBits[oldBitsLen - 1] = currentInt;
        }
        this.bits = newBits;
    }

    private static int[] makeArray(int size) {
        return new int[(size + 31) / 32];
    }

    public boolean equals(Object o) {
        if (!(o instanceof BitArray)) {
            return false;
        }
        BitArray other = (BitArray)o;
        return this.size == other.size && Arrays.equals(this.bits, other.bits);
    }

    public int hashCode() {
        return 31 * this.size + Arrays.hashCode(this.bits);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(this.size);
        for (int i = 0; i < this.size; ++i) {
            if ((i & 7) == 0) {
                result.append(' ');
            }
            result.append(this.get(i) ? (char)'X' : '.');
        }
        return result.toString();
    }

    public BitArray clone() {
        return new BitArray((int[])this.bits.clone(), this.size);
    }
}

