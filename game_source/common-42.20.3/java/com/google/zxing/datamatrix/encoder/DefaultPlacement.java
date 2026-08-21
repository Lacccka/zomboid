/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.encoder;

import java.util.Arrays;

public class DefaultPlacement {
    private final CharSequence codewords;
    private final int numrows;
    private final int numcols;
    private final byte[] bits;

    public DefaultPlacement(CharSequence codewords, int numcols, int numrows) {
        this.codewords = codewords;
        this.numcols = numcols;
        this.numrows = numrows;
        this.bits = new byte[numcols * numrows];
        Arrays.fill(this.bits, (byte)-1);
    }

    final int getNumrows() {
        return this.numrows;
    }

    final int getNumcols() {
        return this.numcols;
    }

    final byte[] getBits() {
        return this.bits;
    }

    public final boolean getBit(int col, int row) {
        return this.bits[row * this.numcols + col] == 1;
    }

    final void setBit(int col, int row, boolean bit) {
        this.bits[row * this.numcols + col] = bit ? (byte)1 : 0;
    }

    final boolean hasBit(int col, int row) {
        return this.bits[row * this.numcols + col] >= 0;
    }

    public final void place() {
        int pos = 0;
        int row = 4;
        int col = 0;
        do {
            if (row == this.numrows && col == 0) {
                this.corner1(pos++);
            }
            if (row == this.numrows - 2 && col == 0 && this.numcols % 4 != 0) {
                this.corner2(pos++);
            }
            if (row == this.numrows - 2 && col == 0 && this.numcols % 8 == 4) {
                this.corner3(pos++);
            }
            if (row == this.numrows + 4 && col == 2 && this.numcols % 8 == 0) {
                this.corner4(pos++);
            }
            do {
                if (row >= this.numrows || col < 0 || this.hasBit(col, row)) continue;
                this.utah(row, col, pos++);
            } while ((row -= 2) >= 0 && (col += 2) < this.numcols);
            ++row;
            col += 3;
            do {
                if (row < 0 || col >= this.numcols || this.hasBit(col, row)) continue;
                this.utah(row, col, pos++);
            } while ((row += 2) < this.numrows && (col -= 2) >= 0);
        } while ((row += 3) < this.numrows || ++col < this.numcols);
        if (!this.hasBit(this.numcols - 1, this.numrows - 1)) {
            this.setBit(this.numcols - 1, this.numrows - 1, true);
            this.setBit(this.numcols - 2, this.numrows - 2, true);
        }
    }

    private void module(int row, int col, int pos, int bit) {
        if (row < 0) {
            row += this.numrows;
            col += 4 - (this.numrows + 4) % 8;
        }
        if (col < 0) {
            col += this.numcols;
            row += 4 - (this.numcols + 4) % 8;
        }
        int v = this.codewords.charAt(pos);
        this.setBit(col, row, (v &= 1 << 8 - bit) != 0);
    }

    private void utah(int row, int col, int pos) {
        this.module(row - 2, col - 2, pos, 1);
        this.module(row - 2, col - 1, pos, 2);
        this.module(row - 1, col - 2, pos, 3);
        this.module(row - 1, col - 1, pos, 4);
        this.module(row - 1, col, pos, 5);
        this.module(row, col - 2, pos, 6);
        this.module(row, col - 1, pos, 7);
        this.module(row, col, pos, 8);
    }

    private void corner1(int pos) {
        this.module(this.numrows - 1, 0, pos, 1);
        this.module(this.numrows - 1, 1, pos, 2);
        this.module(this.numrows - 1, 2, pos, 3);
        this.module(0, this.numcols - 2, pos, 4);
        this.module(0, this.numcols - 1, pos, 5);
        this.module(1, this.numcols - 1, pos, 6);
        this.module(2, this.numcols - 1, pos, 7);
        this.module(3, this.numcols - 1, pos, 8);
    }

    private void corner2(int pos) {
        this.module(this.numrows - 3, 0, pos, 1);
        this.module(this.numrows - 2, 0, pos, 2);
        this.module(this.numrows - 1, 0, pos, 3);
        this.module(0, this.numcols - 4, pos, 4);
        this.module(0, this.numcols - 3, pos, 5);
        this.module(0, this.numcols - 2, pos, 6);
        this.module(0, this.numcols - 1, pos, 7);
        this.module(1, this.numcols - 1, pos, 8);
    }

    private void corner3(int pos) {
        this.module(this.numrows - 3, 0, pos, 1);
        this.module(this.numrows - 2, 0, pos, 2);
        this.module(this.numrows - 1, 0, pos, 3);
        this.module(0, this.numcols - 2, pos, 4);
        this.module(0, this.numcols - 1, pos, 5);
        this.module(1, this.numcols - 1, pos, 6);
        this.module(2, this.numcols - 1, pos, 7);
        this.module(3, this.numcols - 1, pos, 8);
    }

    private void corner4(int pos) {
        this.module(this.numrows - 1, 0, pos, 1);
        this.module(this.numrows - 1, this.numcols - 1, pos, 2);
        this.module(0, this.numcols - 3, pos, 3);
        this.module(0, this.numcols - 2, pos, 4);
        this.module(0, this.numcols - 1, pos, 5);
        this.module(1, this.numcols - 3, pos, 6);
        this.module(1, this.numcols - 2, pos, 7);
        this.module(1, this.numcols - 1, pos, 8);
    }
}

