/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Cfgmgr32;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APITypeMapper;

public abstract class Cfgmgr32Util {
    public static String CM_Get_Device_ID(int devInst) throws Cfgmgr32Exception {
        int charToBytes = Boolean.getBoolean("w32.ascii") ? 1 : Native.WCHAR_SIZE;
        IntByReference pulLen = new IntByReference();
        int ret = Cfgmgr32.INSTANCE.CM_Get_Device_ID_Size(pulLen, devInst, 0);
        if (ret != 0) {
            throw new Cfgmgr32Exception(ret);
        }
        Memory buffer = new Memory((pulLen.getValue() + 1) * charToBytes);
        buffer.clear();
        ret = Cfgmgr32.INSTANCE.CM_Get_Device_ID(devInst, buffer, pulLen.getValue(), 0);
        if (ret == 26) {
            ret = Cfgmgr32.INSTANCE.CM_Get_Device_ID_Size(pulLen, devInst, 0);
            if (ret != 0) {
                throw new Cfgmgr32Exception(ret);
            }
            buffer = new Memory((pulLen.getValue() + 1) * charToBytes);
            buffer.clear();
            ret = Cfgmgr32.INSTANCE.CM_Get_Device_ID(devInst, buffer, pulLen.getValue(), 0);
        }
        if (ret != 0) {
            throw new Cfgmgr32Exception(ret);
        }
        if (charToBytes == 1) {
            return buffer.getString(0L);
        }
        return buffer.getWideString(0L);
    }

    public static Object CM_Get_DevNode_Registry_Property(int devInst, int ulProperty) throws Cfgmgr32Exception {
        IntByReference type = new IntByReference();
        IntByReference size = new IntByReference();
        int ret = Cfgmgr32.INSTANCE.CM_Get_DevNode_Registry_Property(devInst, ulProperty, type, null, size, 0);
        if (ret == 37) {
            return null;
        }
        if (ret != 26) {
            throw new Cfgmgr32Exception(ret);
        }
        Memory buffer = null;
        if (size.getValue() > 0 && (ret = Cfgmgr32.INSTANCE.CM_Get_DevNode_Registry_Property(devInst, ulProperty, type, buffer = new Memory(size.getValue()), size, 0)) != 0) {
            throw new Cfgmgr32Exception(ret);
        }
        switch (type.getValue()) {
            case 1: {
                if (buffer == null) {
                    return "";
                }
                return W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE ? buffer.getWideString(0L) : buffer.getString(0L);
            }
            case 7: {
                if (buffer == null) {
                    return new String[0];
                }
                return Advapi32Util.regMultiSzBufferToStringArray(buffer);
            }
            case 4: {
                if (buffer == null) {
                    return 0;
                }
                return buffer.getInt(0L);
            }
            case 0: {
                return null;
            }
        }
        if (buffer == null) {
            return new byte[0];
        }
        return buffer.getByteArray(0L, (int)buffer.size());
    }

    public static class Cfgmgr32Exception
    extends RuntimeException {
        private final int errorCode;

        public Cfgmgr32Exception(int errorCode) {
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return this.errorCode;
        }

        @Override
        public String toString() {
            return super.toString() + String.format(" [errorCode: 0x%08x]", this.errorCode);
        }
    }
}

