/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.bytecode;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.bytecode.SecureLoader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ClassTailor {
    private static final Logger logger = Util.getClassLogger();

    private ClassTailor() {
    }

    public static String toVMClassName(Class c) {
        assert (!c.isPrimitive());
        if (c.isArray()) {
            return ClassTailor.toVMTypeName(c);
        }
        return c.getName().replace('.', '/');
    }

    public static String toVMTypeName(Class c) {
        if (c.isArray()) {
            return '[' + ClassTailor.toVMTypeName(c.getComponentType());
        }
        if (c.isPrimitive()) {
            if (c == Boolean.TYPE) {
                return "Z";
            }
            if (c == Character.TYPE) {
                return "C";
            }
            if (c == Byte.TYPE) {
                return "B";
            }
            if (c == Double.TYPE) {
                return "D";
            }
            if (c == Float.TYPE) {
                return "F";
            }
            if (c == Integer.TYPE) {
                return "I";
            }
            if (c == Long.TYPE) {
                return "J";
            }
            if (c == Short.TYPE) {
                return "S";
            }
            throw new IllegalArgumentException(c.getName());
        }
        return 'L' + c.getName().replace('.', '/') + ';';
    }

    public static byte[] tailor(Class templateClass, String newClassName, String ... replacements) {
        String vmname = ClassTailor.toVMClassName(templateClass);
        return ClassTailor.tailor(SecureLoader.getClassClassLoader(templateClass).getResourceAsStream(vmname + ".class"), vmname, newClassName, replacements);
    }

    public static byte[] tailor(InputStream image, String templateClassName, String newClassName, String ... replacements) {
        DataInputStream in = new DataInputStream(image);
        try {
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            DataOutputStream out = new DataOutputStream(baos);
            long l = in.readLong();
            out.writeLong(l);
            int count = in.readShort();
            out.writeShort(count);
            block10: for (int i = 0; i < count; ++i) {
                byte tag = in.readByte();
                out.writeByte(tag);
                switch (tag) {
                    case 0: {
                        continue block10;
                    }
                    case 1: {
                        String value = in.readUTF();
                        if (value.equals(templateClassName)) {
                            value = newClassName;
                        } else {
                            for (int j = 0; j < replacements.length; j += 2) {
                                if (!value.equals(replacements[j])) continue;
                                value = replacements[j + 1];
                                break;
                            }
                        }
                        out.writeUTF(value);
                        continue block10;
                    }
                    case 3: 
                    case 4: {
                        out.writeInt(in.readInt());
                        continue block10;
                    }
                    case 5: 
                    case 6: {
                        ++i;
                        out.writeLong(in.readLong());
                        continue block10;
                    }
                    case 7: 
                    case 8: {
                        out.writeShort(in.readShort());
                        continue block10;
                    }
                    case 9: 
                    case 10: 
                    case 11: 
                    case 12: {
                        out.writeInt(in.readInt());
                        continue block10;
                    }
                    default: {
                        throw new IllegalArgumentException("Unknown constant type " + tag);
                    }
                }
            }
            byte[] buf = new byte[512];
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return baos.toByteArray();
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "failed to tailor", e);
            return null;
        }
    }
}

