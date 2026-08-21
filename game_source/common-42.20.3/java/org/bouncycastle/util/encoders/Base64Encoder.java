/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.util.encoders;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.util.encoders.Encoder;

public class Base64Encoder
implements Encoder {
    protected final byte[] encodingTable = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    protected byte padding = (byte)61;
    protected final byte[] decodingTable = new byte[128];

    protected void initialiseDecodingTable() {
        int n;
        for (n = 0; n < this.decodingTable.length; ++n) {
            this.decodingTable[n] = -1;
        }
        for (n = 0; n < this.encodingTable.length; ++n) {
            this.decodingTable[this.encodingTable[n]] = (byte)n;
        }
    }

    public Base64Encoder() {
        this.initialiseDecodingTable();
    }

    public int encode(byte[] byArray, int n, int n2, OutputStream outputStream2) throws IOException {
        int n3;
        int n4 = n2 % 3;
        int n5 = n2 - n4;
        for (n3 = n; n3 < n + n5; n3 += 3) {
            int n6 = byArray[n3] & 0xFF;
            int n7 = byArray[n3 + 1] & 0xFF;
            int n8 = byArray[n3 + 2] & 0xFF;
            outputStream2.write(this.encodingTable[n6 >>> 2 & 0x3F]);
            outputStream2.write(this.encodingTable[(n6 << 4 | n7 >>> 4) & 0x3F]);
            outputStream2.write(this.encodingTable[(n7 << 2 | n8 >>> 6) & 0x3F]);
            outputStream2.write(this.encodingTable[n8 & 0x3F]);
        }
        switch (n4) {
            case 0: {
                break;
            }
            case 1: {
                int n9 = byArray[n + n5] & 0xFF;
                n3 = n9 >>> 2 & 0x3F;
                int n10 = n9 << 4 & 0x3F;
                outputStream2.write(this.encodingTable[n3]);
                outputStream2.write(this.encodingTable[n10]);
                outputStream2.write(this.padding);
                outputStream2.write(this.padding);
                break;
            }
            case 2: {
                int n11 = byArray[n + n5] & 0xFF;
                int n12 = byArray[n + n5 + 1] & 0xFF;
                n3 = n11 >>> 2 & 0x3F;
                int n13 = (n11 << 4 | n12 >>> 4) & 0x3F;
                int n14 = n12 << 2 & 0x3F;
                outputStream2.write(this.encodingTable[n3]);
                outputStream2.write(this.encodingTable[n13]);
                outputStream2.write(this.encodingTable[n14]);
                outputStream2.write(this.padding);
            }
        }
        return n5 / 3 * 4 + (n4 == 0 ? 0 : 4);
    }

    private boolean ignore(char c) {
        return c == '\n' || c == '\r' || c == '\t' || c == ' ';
    }

    public int decode(byte[] byArray, int n, int n2, OutputStream outputStream2) throws IOException {
        int n3;
        int n4;
        int n5 = 0;
        for (n4 = n + n2; n4 > n && this.ignore((char)byArray[n4 - 1]); --n4) {
        }
        if (n4 == 0) {
            return 0;
        }
        int n6 = 0;
        for (n3 = n4; n3 > n && n6 != 4; --n3) {
            if (this.ignore((char)byArray[n3 - 1])) continue;
            ++n6;
        }
        n6 = this.nextI(byArray, n, n3);
        while (n6 < n3) {
            byte by;
            byte by2 = this.decodingTable[byArray[n6++]];
            n6 = this.nextI(byArray, n6, n3);
            byte by3 = this.decodingTable[byArray[n6++]];
            n6 = this.nextI(byArray, n6, n3);
            byte by4 = this.decodingTable[byArray[n6++]];
            n6 = this.nextI(byArray, n6, n3);
            if ((by2 | by3 | by4 | (by = this.decodingTable[byArray[n6++]])) < 0) {
                throw new IOException("invalid characters encountered in base64 data");
            }
            outputStream2.write(by2 << 2 | by3 >> 4);
            outputStream2.write(by3 << 4 | by4 >> 2);
            outputStream2.write(by4 << 6 | by);
            n5 += 3;
            n6 = this.nextI(byArray, n6, n3);
        }
        int n7 = this.nextI(byArray, n6, n4);
        int n8 = this.nextI(byArray, n7 + 1, n4);
        int n9 = this.nextI(byArray, n8 + 1, n4);
        int n10 = this.nextI(byArray, n9 + 1, n4);
        return n5 += this.decodeLastBlock(outputStream2, (char)byArray[n7], (char)byArray[n8], (char)byArray[n9], (char)byArray[n10]);
    }

    private int nextI(byte[] byArray, int n, int n2) {
        while (n < n2 && this.ignore((char)byArray[n])) {
            ++n;
        }
        return n;
    }

    public int decode(String string, OutputStream outputStream2) throws IOException {
        int n;
        int n2;
        int n3 = 0;
        for (n2 = string.length(); n2 > 0 && this.ignore(string.charAt(n2 - 1)); --n2) {
        }
        if (n2 == 0) {
            return 0;
        }
        int n4 = 0;
        for (n = n2; n > 0 && n4 != 4; --n) {
            if (this.ignore(string.charAt(n - 1))) continue;
            ++n4;
        }
        n4 = this.nextI(string, 0, n);
        while (n4 < n) {
            byte by;
            byte by2 = this.decodingTable[string.charAt(n4++)];
            n4 = this.nextI(string, n4, n);
            byte by3 = this.decodingTable[string.charAt(n4++)];
            n4 = this.nextI(string, n4, n);
            byte by4 = this.decodingTable[string.charAt(n4++)];
            n4 = this.nextI(string, n4, n);
            if ((by2 | by3 | by4 | (by = this.decodingTable[string.charAt(n4++)])) < 0) {
                throw new IOException("invalid characters encountered in base64 data");
            }
            outputStream2.write(by2 << 2 | by3 >> 4);
            outputStream2.write(by3 << 4 | by4 >> 2);
            outputStream2.write(by4 << 6 | by);
            n3 += 3;
            n4 = this.nextI(string, n4, n);
        }
        int n5 = this.nextI(string, n4, n2);
        int n6 = this.nextI(string, n5 + 1, n2);
        int n7 = this.nextI(string, n6 + 1, n2);
        int n8 = this.nextI(string, n7 + 1, n2);
        return n3 += this.decodeLastBlock(outputStream2, string.charAt(n5), string.charAt(n6), string.charAt(n7), string.charAt(n8));
    }

    private int decodeLastBlock(OutputStream outputStream2, char c, char c2, char c3, char c4) throws IOException {
        if (c3 == this.padding) {
            if (c4 != this.padding) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            byte by = this.decodingTable[c];
            byte by2 = this.decodingTable[c2];
            if ((by | by2) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            outputStream2.write(by << 2 | by2 >> 4);
            return 1;
        }
        if (c4 == this.padding) {
            byte by = this.decodingTable[c];
            byte by3 = this.decodingTable[c2];
            byte by4 = this.decodingTable[c3];
            if ((by | by3 | by4) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            outputStream2.write(by << 2 | by3 >> 4);
            outputStream2.write(by3 << 4 | by4 >> 2);
            return 2;
        }
        byte by = this.decodingTable[c];
        byte by5 = this.decodingTable[c2];
        byte by6 = this.decodingTable[c3];
        byte by7 = this.decodingTable[c4];
        if ((by | by5 | by6 | by7) < 0) {
            throw new IOException("invalid characters encountered at end of base64 data");
        }
        outputStream2.write(by << 2 | by5 >> 4);
        outputStream2.write(by5 << 4 | by6 >> 2);
        outputStream2.write(by6 << 6 | by7);
        return 3;
    }

    private int nextI(String string, int n, int n2) {
        while (n < n2 && this.ignore(string.charAt(n))) {
            ++n;
        }
        return n;
    }
}

