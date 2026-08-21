/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.ByReference;
import java.io.UnsupportedEncodingException;

public interface WTypes {
    public static final int CLSCTX_INPROC_SERVER = 1;
    public static final int CLSCTX_INPROC_HANDLER = 2;
    public static final int CLSCTX_LOCAL_SERVER = 4;
    public static final int CLSCTX_INPROC_SERVER16 = 8;
    public static final int CLSCTX_REMOTE_SERVER = 16;
    public static final int CLSCTX_INPROC_HANDLER16 = 32;
    public static final int CLSCTX_RESERVED1 = 64;
    public static final int CLSCTX_RESERVED2 = 128;
    public static final int CLSCTX_RESERVED3 = 256;
    public static final int CLSCTX_RESERVED4 = 512;
    public static final int CLSCTX_NO_CODE_DOWNLOAD = 1024;
    public static final int CLSCTX_RESERVED5 = 2048;
    public static final int CLSCTX_NO_CUSTOM_MARSHAL = 4096;
    public static final int CLSCTX_ENABLE_CODE_DOWNLOAD = 8192;
    public static final int CLSCTX_NO_FAILURE_LOG = 16384;
    public static final int CLSCTX_DISABLE_AAA = 32768;
    public static final int CLSCTX_ENABLE_AAA = 65536;
    public static final int CLSCTX_FROM_DEFAULT_CONTEXT = 131072;
    public static final int CLSCTX_ACTIVATE_32_BIT_SERVER = 262144;
    public static final int CLSCTX_ACTIVATE_64_BIT_SERVER = 524288;
    public static final int CLSCTX_ENABLE_CLOAKING = 0x100000;
    public static final int CLSCTX_APPCONTAINER = 0x400000;
    public static final int CLSCTX_ACTIVATE_AAA_AS_IU = 0x800000;
    public static final int CLSCTX_PS_DLL = Integer.MIN_VALUE;
    public static final int CLSCTX_SERVER = 21;
    public static final int CLSCTX_ALL = 7;

    public static class VARTYPEByReference
    extends ByReference {
        public VARTYPEByReference() {
            super(2);
        }

        public VARTYPEByReference(VARTYPE type) {
            super(2);
            this.setValue(type);
        }

        public VARTYPEByReference(short type) {
            super(2);
            this.getPointer().setShort(0L, type);
        }

        public void setValue(VARTYPE value) {
            this.getPointer().setShort(0L, value.shortValue());
        }

        public VARTYPE getValue() {
            return new VARTYPE(this.getPointer().getShort(0L));
        }
    }

    public static class VARTYPE
    extends WinDef.USHORT {
        private static final long serialVersionUID = 1L;

        public VARTYPE() {
            this(0);
        }

        public VARTYPE(int value) {
            super((long)value);
        }
    }

    public static class LPOLESTR
    extends PointerType {
        public LPOLESTR() {
            super(Pointer.NULL);
        }

        public LPOLESTR(Pointer pointer) {
            super(pointer);
        }

        public LPOLESTR(String value) {
            super(new Memory(((long)value.length() + 1L) * (long)Native.WCHAR_SIZE));
            this.setValue(value);
        }

        public void setValue(String value) {
            this.getPointer().setWideString(0L, value);
        }

        public String getValue() {
            Pointer pointer = this.getPointer();
            String str = null;
            if (pointer != null) {
                str = pointer.getWideString(0L);
            }
            return str;
        }

        @Override
        public String toString() {
            return this.getValue();
        }

        public static class ByReference
        extends LPOLESTR
        implements Structure.ByReference {
        }
    }

    public static class LPWSTR
    extends PointerType {
        public LPWSTR() {
            super(Pointer.NULL);
        }

        public LPWSTR(Pointer pointer) {
            super(pointer);
        }

        public LPWSTR(String value) {
            this(new Memory(((long)value.length() + 1L) * (long)Native.WCHAR_SIZE));
            this.setValue(value);
        }

        public void setValue(String value) {
            this.getPointer().setWideString(0L, value);
        }

        public String getValue() {
            Pointer pointer = this.getPointer();
            String str = null;
            if (pointer != null) {
                str = pointer.getWideString(0L);
            }
            return str;
        }

        @Override
        public String toString() {
            return this.getValue();
        }

        public static class ByReference
        extends LPWSTR
        implements Structure.ByReference {
        }
    }

    public static class LPSTR
    extends PointerType {
        public LPSTR() {
            super(Pointer.NULL);
        }

        public LPSTR(Pointer pointer) {
            super(pointer);
        }

        public LPSTR(String value) {
            this(new Memory((long)value.length() + 1L));
            this.setValue(value);
        }

        public void setValue(String value) {
            this.getPointer().setString(0L, value);
        }

        public String getValue() {
            Pointer pointer = this.getPointer();
            String str = null;
            if (pointer != null) {
                str = pointer.getString(0L);
            }
            return str;
        }

        @Override
        public String toString() {
            return this.getValue();
        }

        public static class ByReference
        extends LPSTR
        implements Structure.ByReference {
        }
    }

    public static class BSTRByReference
    extends ByReference {
        public BSTRByReference() {
            super(Native.POINTER_SIZE);
        }

        public BSTRByReference(BSTR value) {
            this();
            this.setValue(value);
        }

        public void setValue(BSTR value) {
            this.getPointer().setPointer(0L, value.getPointer());
        }

        public BSTR getValue() {
            return new BSTR(this.getPointer().getPointer(0L));
        }

        public String getString() {
            BSTR b = this.getValue();
            return b == null ? null : b.getValue();
        }
    }

    public static class BSTR
    extends PointerType {
        public BSTR() {
            super(Pointer.NULL);
        }

        public BSTR(Pointer pointer) {
            super(pointer);
        }

        @Deprecated
        public BSTR(String value) {
            this.setValue(value);
        }

        @Deprecated
        public void setValue(String value) {
            if (value == null) {
                value = "";
            }
            try {
                byte[] encodedValue = value.getBytes("UTF-16LE");
                Memory mem = new Memory(4 + encodedValue.length + 2);
                mem.clear();
                mem.setInt(0L, encodedValue.length);
                mem.write(4L, encodedValue, 0, encodedValue.length);
                this.setPointer(mem.share(4L));
            }
            catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("UTF-16LE charset is not supported", ex);
            }
        }

        public String getValue() {
            try {
                Pointer pointer = this.getPointer();
                if (pointer == null) {
                    return "";
                }
                int stringLength = pointer.getInt(-4L);
                return new String(pointer.getByteArray(0L, stringLength), "UTF-16LE");
            }
            catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("UTF-16LE charset is not supported", ex);
            }
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }
}

