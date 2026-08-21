/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaClosure;

public final class Prototype {
    public int[] code;
    public Object[] constants;
    public Prototype[] prototypes;
    public int numParams;
    public boolean isVararg;
    public String name;
    public int[] lines;
    public int numUpvalues;
    public int maxStacksize;
    public String file;
    public String filename;
    public String[] locvars;
    public int[] locvarlines;

    public Prototype() {
    }

    public Prototype(DataInputStream in, boolean littleEndian, String parentName, int size_t2) throws IOException {
        int i;
        this.name = Prototype.readLuaString(in, size_t2, littleEndian);
        if (this.name == null) {
            this.name = parentName;
        }
        if (this.name != null) {
            this.name = this.name.intern();
        }
        in.readInt();
        in.readInt();
        this.numUpvalues = in.read();
        this.numParams = in.read();
        int isVararg = in.read();
        this.isVararg = (isVararg & 2) != 0;
        this.maxStacksize = in.read();
        int codeLen = Prototype.toInt(in.readInt(), littleEndian);
        this.code = new int[codeLen];
        for (int i2 = 0; i2 < codeLen; ++i2) {
            int op;
            this.code[i2] = op = Prototype.toInt(in.readInt(), littleEndian);
        }
        int constantsLen = Prototype.toInt(in.readInt(), littleEndian);
        this.constants = new Object[constantsLen];
        for (int i3 = 0; i3 < constantsLen; ++i3) {
            Object o = null;
            int type = in.read();
            switch (type) {
                case 0: {
                    break;
                }
                case 1: {
                    int b = in.read();
                    o = b == 0 ? Boolean.FALSE : Boolean.TRUE;
                    break;
                }
                case 3: {
                    long bits = in.readLong();
                    if (littleEndian) {
                        bits = Prototype.rev(bits);
                    }
                    o = KahluaUtil.toDouble(Double.longBitsToDouble(bits));
                    break;
                }
                case 4: {
                    o = Prototype.readLuaString(in, size_t2, littleEndian);
                    break;
                }
                default: {
                    throw new IOException("unknown constant type: " + type);
                }
            }
            this.constants[i3] = o;
        }
        int prototypesLen = Prototype.toInt(in.readInt(), littleEndian);
        this.prototypes = new Prototype[prototypesLen];
        for (i = 0; i < prototypesLen; ++i) {
            this.prototypes[i] = new Prototype(in, littleEndian, this.name, size_t2);
        }
        int tmp = Prototype.toInt(in.readInt(), littleEndian);
        this.lines = new int[tmp];
        for (i = 0; i < tmp; ++i) {
            int tmp2;
            this.lines[i] = tmp2 = Prototype.toInt(in.readInt(), littleEndian);
        }
        tmp = Prototype.toInt(in.readInt(), littleEndian);
        for (i = 0; i < tmp; ++i) {
            Prototype.readLuaString(in, size_t2, littleEndian);
            in.readInt();
            in.readInt();
        }
        tmp = Prototype.toInt(in.readInt(), littleEndian);
        for (i = 0; i < tmp; ++i) {
            Prototype.readLuaString(in, size_t2, littleEndian);
        }
    }

    public String toString() {
        return this.name;
    }

    private static String readLuaString(DataInputStream in, int size_t2, boolean littleEndian) throws IOException {
        long len = 0L;
        if (size_t2 == 4) {
            int i = in.readInt();
            len = Prototype.toInt(i, littleEndian);
        } else if (size_t2 == 8) {
            len = Prototype.toLong(in.readLong(), littleEndian);
        } else {
            Prototype.loadAssert(false, "Bad string size");
        }
        if (len == 0L) {
            return null;
        }
        Prototype.loadAssert(--len < 65536L, "Too long string:" + len);
        int iLen = (int)len;
        byte[] stringData = new byte[3 + iLen];
        stringData[0] = (byte)(iLen >> 8 & 0xFF);
        stringData[1] = (byte)(iLen & 0xFF);
        in.readFully(stringData, 2, iLen + 1);
        Prototype.loadAssert(stringData[2 + iLen] == 0, "String loading");
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(stringData));
        String s = dis.readUTF();
        dis.close();
        return s.intern();
    }

    public static int rev(int v) {
        int a = v >>> 24 & 0xFF;
        int b = v >>> 16 & 0xFF;
        int c = v >>> 8 & 0xFF;
        int d = v & 0xFF;
        return d << 24 | c << 16 | b << 8 | a;
    }

    public static long rev(long v) {
        long a = v >>> 56 & 0xFFL;
        long b = v >>> 48 & 0xFFL;
        long c = v >>> 40 & 0xFFL;
        long d = v >>> 32 & 0xFFL;
        long e = v >>> 24 & 0xFFL;
        long f = v >>> 16 & 0xFFL;
        long g = v >>> 8 & 0xFFL;
        long h = v & 0xFFL;
        return h << 56 | g << 48 | f << 40 | e << 32 | d << 24 | c << 16 | b << 8 | a;
    }

    public static int toInt(int bits, boolean littleEndian) {
        return littleEndian ? Prototype.rev(bits) : bits;
    }

    public static long toLong(long bits, boolean littleEndian) {
        return littleEndian ? Prototype.rev(bits) : bits;
    }

    public static LuaClosure loadByteCode(DataInputStream in, KahluaTable env) throws IOException {
        int tmp = in.read();
        Prototype.loadAssert(tmp == 27, "Signature 1");
        tmp = in.read();
        Prototype.loadAssert(tmp == 76, "Signature 2");
        tmp = in.read();
        Prototype.loadAssert(tmp == 117, "Signature 3");
        tmp = in.read();
        Prototype.loadAssert(tmp == 97, "Signature 4");
        tmp = in.read();
        Prototype.loadAssert(tmp == 81, "Version");
        tmp = in.read();
        Prototype.loadAssert(tmp == 0, "Format");
        boolean littleEndian = in.read() == 1;
        tmp = in.read();
        Prototype.loadAssert(tmp == 4, "Size int");
        int size_t2 = in.read();
        Prototype.loadAssert(size_t2 == 4 || size_t2 == 8, "Size t");
        tmp = in.read();
        Prototype.loadAssert(tmp == 4, "Size instr");
        tmp = in.read();
        Prototype.loadAssert(tmp == 8, "Size number");
        tmp = in.read();
        Prototype.loadAssert(tmp == 0, "Integral");
        Prototype mainPrototype = new Prototype(in, littleEndian, null, size_t2);
        LuaClosure closure = new LuaClosure(mainPrototype, env);
        return closure;
    }

    private static void loadAssert(boolean c, String message) throws IOException {
        if (!c) {
            throw new IOException("Could not load bytecode:" + message);
        }
    }

    public static LuaClosure loadByteCode(InputStream in, KahluaTable env) throws IOException {
        if (!(in instanceof DataInputStream)) {
            in = new DataInputStream(in);
        }
        return Prototype.loadByteCode((DataInputStream)in, env);
    }

    public void dump(OutputStream os) throws IOException {
        DataOutputStream dataOutputStream;
        DataOutputStream dos = os instanceof DataOutputStream ? (dataOutputStream = (DataOutputStream)os) : new DataOutputStream(os);
        dos.write(27);
        dos.write(76);
        dos.write(117);
        dos.write(97);
        dos.write(81);
        dos.write(0);
        dos.write(0);
        dos.write(4);
        dos.write(4);
        dos.write(4);
        dos.write(8);
        dos.write(0);
        this.dumpPrototype(dos);
    }

    private void dumpPrototype(DataOutputStream dos) throws IOException {
        Prototype.dumpString(this.name, dos);
        dos.writeInt(0);
        dos.writeInt(0);
        dos.write(this.numUpvalues);
        dos.write(this.numParams);
        dos.write(this.isVararg ? 2 : 0);
        dos.write(this.maxStacksize);
        int codeLen = this.code.length;
        dos.writeInt(codeLen);
        for (int i = 0; i < codeLen; ++i) {
            dos.writeInt(this.code[i]);
        }
        int constantsLen = this.constants.length;
        dos.writeInt(constantsLen);
        for (int i = 0; i < constantsLen; ++i) {
            Object o = this.constants[i];
            if (o == null) {
                dos.write(0);
                continue;
            }
            if (o instanceof Boolean) {
                Boolean b = (Boolean)o;
                dos.write(1);
                dos.write(b != false ? 1 : 0);
                continue;
            }
            if (o instanceof Double) {
                Double d = (Double)o;
                dos.write(3);
                dos.writeLong(Double.doubleToLongBits(d));
                continue;
            }
            if (o instanceof String) {
                String s = (String)o;
                dos.write(4);
                Prototype.dumpString(s, dos);
                continue;
            }
            throw new RuntimeException("Bad type in constant pool");
        }
        int prototypesLen = this.prototypes.length;
        dos.writeInt(prototypesLen);
        for (int i = 0; i < prototypesLen; ++i) {
            this.prototypes[i].dumpPrototype(dos);
        }
        int linesLen = this.lines.length;
        dos.writeInt(linesLen);
        for (int i = 0; i < linesLen; ++i) {
            dos.writeInt(this.lines[i]);
        }
        dos.writeInt(0);
        dos.writeInt(0);
    }

    private static void dumpString(String name, DataOutputStream dos) throws IOException {
        if (name == null) {
            dos.writeShort(0);
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new DataOutputStream(baos).writeUTF(name);
        byte[] bytes = baos.toByteArray();
        int numBytes = bytes.length - 2;
        dos.writeInt(numBytes + 1);
        dos.write(bytes, 2, numBytes);
        dos.write(0);
    }
}

