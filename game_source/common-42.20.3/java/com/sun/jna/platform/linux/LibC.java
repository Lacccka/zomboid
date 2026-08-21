/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.LibCAPI;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface LibC
extends LibCAPI,
Library {
    public static final String NAME = "c";
    public static final LibC INSTANCE = Native.load("c", LibC.class);

    public int sysinfo(Sysinfo var1);

    public int statvfs(String var1, Statvfs var2);

    @Structure.FieldOrder(value={"f_bsize", "f_frsize", "f_blocks", "f_bfree", "f_bavail", "f_files", "f_ffree", "f_favail", "f_fsid", "_f_unused", "f_flag", "f_namemax", "_f_spare"})
    public static class Statvfs
    extends Structure {
        public NativeLong f_bsize;
        public NativeLong f_frsize;
        public NativeLong f_blocks;
        public NativeLong f_bfree;
        public NativeLong f_bavail;
        public NativeLong f_files;
        public NativeLong f_ffree;
        public NativeLong f_favail;
        public NativeLong f_fsid;
        public int _f_unused;
        public NativeLong f_flag;
        public NativeLong f_namemax;
        public int[] _f_spare = new int[6];

        @Override
        protected List<Field> getFieldList() {
            ArrayList<Field> fields = new ArrayList<Field>(super.getFieldList());
            if (NativeLong.SIZE > 4) {
                Iterator fieldIterator = fields.iterator();
                while (fieldIterator.hasNext()) {
                    Field field = (Field)fieldIterator.next();
                    if (!"_f_unused".equals(field.getName())) continue;
                    fieldIterator.remove();
                }
            }
            return fields;
        }

        @Override
        protected List<String> getFieldOrder() {
            ArrayList<String> fieldOrder = new ArrayList<String>(super.getFieldOrder());
            if (NativeLong.SIZE > 4) {
                fieldOrder.remove("_f_unused");
            }
            return fieldOrder;
        }
    }

    @Structure.FieldOrder(value={"uptime", "loads", "totalram", "freeram", "sharedram", "bufferram", "totalswap", "freeswap", "procs", "totalhigh", "freehigh", "mem_unit", "_f"})
    public static class Sysinfo
    extends Structure {
        private static final int PADDING_SIZE = 20 - 2 * NativeLong.SIZE - 4;
        public NativeLong uptime;
        public NativeLong[] loads = new NativeLong[3];
        public NativeLong totalram;
        public NativeLong freeram;
        public NativeLong sharedram;
        public NativeLong bufferram;
        public NativeLong totalswap;
        public NativeLong freeswap;
        public short procs;
        public NativeLong totalhigh;
        public NativeLong freehigh;
        public int mem_unit;
        public byte[] _f = new byte[PADDING_SIZE];

        @Override
        protected List<Field> getFieldList() {
            ArrayList<Field> fields = new ArrayList<Field>(super.getFieldList());
            if (PADDING_SIZE == 0) {
                Iterator fieldIterator = fields.iterator();
                while (fieldIterator.hasNext()) {
                    Field field = (Field)fieldIterator.next();
                    if (!"_f".equals(field.getName())) continue;
                    fieldIterator.remove();
                }
            }
            return fields;
        }

        @Override
        protected List<String> getFieldOrder() {
            ArrayList<String> fieldOrder = new ArrayList<String>(super.getFieldOrder());
            if (PADDING_SIZE == 0) {
                fieldOrder.remove("_f");
            }
            return fieldOrder;
        }
    }
}

