/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Secur32;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.win32.W32APITypeMapper;

public interface Sspi {
    public static final int MAX_TOKEN_SIZE = VersionHelpers.IsWindows8OrGreater() ? 48000 : 12000;
    public static final int SECPKG_CRED_INBOUND = 1;
    public static final int SECPKG_CRED_OUTBOUND = 2;
    public static final int SECURITY_NATIVE_DREP = 16;
    public static final int SECURITY_NETWORK_DREP = 0;
    public static final int ISC_REQ_ALLOCATE_MEMORY = 256;
    public static final int ISC_REQ_CONFIDENTIALITY = 16;
    public static final int ISC_REQ_CONNECTION = 2048;
    public static final int ISC_REQ_DELEGATE = 1;
    public static final int ISC_REQ_EXTENDED_ERROR = 16384;
    public static final int ISC_REQ_INTEGRITY = 65536;
    public static final int ISC_REQ_MUTUAL_AUTH = 2;
    public static final int ISC_REQ_REPLAY_DETECT = 4;
    public static final int ISC_REQ_SEQUENCE_DETECT = 8;
    public static final int ISC_REQ_STREAM = 32768;
    public static final int SECBUFFER_VERSION = 0;
    public static final int SECBUFFER_EMPTY = 0;
    public static final int SECBUFFER_DATA = 1;
    public static final int SECBUFFER_TOKEN = 2;
    public static final int SECPKG_ATTR_SIZES = 0;
    public static final int SECPKG_ATTR_NAMES = 1;
    public static final int SECPKG_ATTR_LIFESPAN = 2;
    public static final int SECPKG_ATTR_DCE_INFO = 3;
    public static final int SECPKG_ATTR_STREAM_SIZES = 4;
    public static final int SECPKG_ATTR_KEY_INFO = 5;
    public static final int SECPKG_ATTR_AUTHORITY = 6;
    public static final int SECPKG_ATTR_PROTO_INFO = 7;
    public static final int SECPKG_ATTR_PASSWORD_EXPIRY = 8;
    public static final int SECPKG_ATTR_SESSION_KEY = 9;
    public static final int SECPKG_ATTR_PACKAGE_INFO = 10;
    public static final int SECPKG_ATTR_USER_FLAGS = 11;
    public static final int SECPKG_ATTR_NEGOTIATION_INFO = 12;
    public static final int SECPKG_ATTR_NATIVE_NAMES = 13;
    public static final int SECPKG_ATTR_FLAGS = 14;
    public static final int SECPKG_ATTR_USE_VALIDATED = 15;
    public static final int SECPKG_ATTR_CREDENTIAL_NAME = 16;
    public static final int SECPKG_ATTR_TARGET_INFORMATION = 17;
    public static final int SECPKG_ATTR_ACCESS_TOKEN = 18;
    public static final int SECPKG_ATTR_TARGET = 19;
    public static final int SECPKG_ATTR_AUTHENTICATION_ID = 20;
    public static final int SECPKG_ATTR_LOGOFF_TIME = 21;
    public static final int SECPKG_ATTR_NEGO_KEYS = 22;
    public static final int SECPKG_ATTR_PROMPTING_NEEDED = 24;
    public static final int SECPKG_ATTR_UNIQUE_BINDINGS = 25;
    public static final int SECPKG_ATTR_ENDPOINT_BINDINGS = 26;
    public static final int SECPKG_ATTR_CLIENT_SPECIFIED_TARGET = 27;
    public static final int SECPKG_ATTR_LAST_CLIENT_TOKEN_STATUS = 30;
    public static final int SECPKG_ATTR_NEGO_PKG_INFO = 31;
    public static final int SECPKG_ATTR_NEGO_STATUS = 32;
    public static final int SECPKG_ATTR_CONTEXT_DELETED = 33;
    public static final int SECPKG_ATTR_SUBJECT_SECURITY_ATTRIBUTES = 128;
    public static final int SECPKG_NEGOTIATION_COMPLETE = 0;
    public static final int SECPKG_NEGOTIATION_OPTIMISTIC = 1;
    public static final int SECPKG_NEGOTIATION_IN_PROGRESS = 2;
    public static final int SECPKG_NEGOTIATION_DIRECT = 3;
    public static final int SECPKG_NEGOTIATION_TRY_MULTICRED = 4;
    public static final int SECPKG_FLAG_INTEGRITY = 1;
    public static final int SECPKG_FLAG_PRIVACY = 2;
    public static final int SECPKG_FLAG_TOKEN_ONLY = 4;
    public static final int SECPKG_FLAG_DATAGRAM = 8;
    public static final int SECPKG_FLAG_CONNECTION = 16;
    public static final int SECPKG_FLAG_MULTI_REQUIRED = 32;
    public static final int SECPKG_FLAG_CLIENT_ONLY = 64;
    public static final int SECPKG_FLAG_EXTENDED_ERROR = 128;
    public static final int SECPKG_FLAG_IMPERSONATION = 256;
    public static final int SECPKG_FLAG_ACCEPT_WIN32_NAME = 512;
    public static final int SECPKG_FLAG_STREAM = 1024;
    public static final int SECPKG_FLAG_NEGOTIABLE = 2048;
    public static final int SECPKG_FLAG_GSS_COMPATIBLE = 4096;
    public static final int SECPKG_FLAG_LOGON = 8192;
    public static final int SECPKG_FLAG_ASCII_BUFFERS = 16384;
    public static final int SECPKG_FLAG_FRAGMENT = 32768;
    public static final int SECPKG_FLAG_MUTUAL_AUTH = 65536;
    public static final int SECPKG_FLAG_DELEGATION = 131072;
    public static final int SECPKG_FLAG_RESTRICTED_TOKENS = 524288;
    public static final int SECPKG_FLAG_NEGO_EXTENDER = 0x100000;
    public static final int SECPKG_FLAG_NEGOTIABLE2 = 0x200000;
    public static final int SECPKG_FLAG_APPCONTAINER_PASSTHROUGH = 0x400000;
    public static final int SECPKG_FLAG_APPCONTAINER_CHECKS = 0x800000;
    public static final int SECPKG_CRED_ATTR_NAMES = 1;
    public static final int SECQOP_WRAP_NO_ENCRYPT = -2147483647;
    public static final int SECQOP_WRAP_OOB_DATA = 0x40000000;
    public static final int SEC_WINNT_AUTH_IDENTITY_ANSI = 1;
    public static final int SEC_WINNT_AUTH_IDENTITY_UNICODE = 2;

    @Structure.FieldOrder(value={"User", "UserLength", "Domain", "DomainLength", "Password", "PasswordLength", "Flags"})
    public static class SEC_WINNT_AUTH_IDENTITY
    extends Structure {
        public String User;
        public int UserLength;
        public String Domain;
        public int DomainLength;
        public String Password;
        public int PasswordLength;
        public int Flags = 2;

        public SEC_WINNT_AUTH_IDENTITY() {
            super(W32APITypeMapper.UNICODE);
        }

        @Override
        public void write() {
            this.UserLength = this.User == null ? 0 : this.User.length();
            this.DomainLength = this.Domain == null ? 0 : this.Domain.length();
            this.PasswordLength = this.Password == null ? 0 : this.Password.length();
            super.write();
        }
    }

    @Structure.FieldOrder(value={"Flags"})
    public static class SecPkgContext_Flags
    extends Structure {
        public int Flags;

        public SecPkgContext_Flags() {
            super(W32APITypeMapper.DEFAULT);
        }

        public static class ByReference
        extends SecPkgContext_Flags
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"PackageInfo", "NegotiationState"})
    public static class SecPkgContext_NegotiationInfo
    extends Structure {
        public PSecPkgInfo PackageInfo;
        public int NegotiationState;

        public SecPkgContext_NegotiationInfo() {
            super(W32APITypeMapper.DEFAULT);
        }

        public synchronized void free() {
            if (this.PackageInfo != null) {
                Secur32.INSTANCE.FreeContextBuffer(this.PackageInfo.pPkgInfo.getPointer());
                this.PackageInfo = null;
            }
        }

        public static class ByReference
        extends SecPkgContext_NegotiationInfo
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"tsStart", "tsExpiry"})
    public static class SecPkgContext_Lifespan
    extends Structure {
        public TimeStamp tsStart;
        public TimeStamp tsExpiry;

        public SecPkgContext_Lifespan() {
            super(W32APITypeMapper.DEFAULT);
        }

        public static class ByReference
        extends SecPkgContext_Lifespan
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"sSignatureAlgorithmName", "sEncryptAlgorithmName", "KeySize", "SignatureAlgorithm", "EncryptAlgorithm"})
    public static class SecPkgContext_KeyInfo
    extends Structure {
        public Pointer sSignatureAlgorithmName;
        public Pointer sEncryptAlgorithmName;
        public int KeySize;
        public int SignatureAlgorithm;
        public int EncryptAlgorithm;

        public SecPkgContext_KeyInfo() {
            super(W32APITypeMapper.DEFAULT);
        }

        public synchronized String getSignatureAlgorithmName() {
            if (this.sSignatureAlgorithmName == null) {
                return null;
            }
            return Boolean.getBoolean("w32.ascii") ? this.sSignatureAlgorithmName.getString(0L) : this.sSignatureAlgorithmName.getWideString(0L);
        }

        public synchronized String getEncryptAlgorithmName() {
            if (this.sEncryptAlgorithmName == null) {
                return null;
            }
            return Boolean.getBoolean("w32.ascii") ? this.sEncryptAlgorithmName.getString(0L) : this.sEncryptAlgorithmName.getWideString(0L);
        }

        public synchronized void free() {
            if (this.sSignatureAlgorithmName != null) {
                Secur32.INSTANCE.FreeContextBuffer(this.sSignatureAlgorithmName);
                this.sSignatureAlgorithmName = null;
            }
            if (this.sEncryptAlgorithmName != null) {
                Secur32.INSTANCE.FreeContextBuffer(this.sEncryptAlgorithmName);
                this.sEncryptAlgorithmName = null;
            }
        }
    }

    @Structure.FieldOrder(value={"SessionKeyLength", "SessionKey"})
    public static class SecPkgContext_SessionKey
    extends Structure {
        public int SessionKeyLength;
        public Pointer SessionKey;

        public SecPkgContext_SessionKey() {
            super(W32APITypeMapper.DEFAULT);
        }

        public byte[] getSessionKey() {
            if (this.SessionKey == null) {
                return null;
            }
            return this.SessionKey.getByteArray(0L, this.SessionKeyLength);
        }

        public synchronized void free() {
            if (this.SessionKey != null) {
                Secur32.INSTANCE.FreeContextBuffer(this.SessionKey);
                this.SessionKey = null;
            }
        }

        public static class ByReference
        extends SecPkgContext_SessionKey
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"cbMaxToken", "cbMaxSignature", "cbBlockSize", "cbSecurityTrailer"})
    public static class SecPkgContext_Sizes
    extends Structure {
        public int cbMaxToken;
        public int cbMaxSignature;
        public int cbBlockSize;
        public int cbSecurityTrailer;

        public SecPkgContext_Sizes() {
            super(W32APITypeMapper.DEFAULT);
        }

        @Override
        public String toString() {
            return "SecPkgContext_Sizes{cbMaxToken=" + this.cbMaxToken + ", cbMaxSignature=" + this.cbMaxSignature + ", cbBlockSize=" + this.cbBlockSize + ", cbSecurityTrailer=" + this.cbSecurityTrailer + '}';
        }

        public static class ByReference
        extends SecPkgContext_Sizes
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"sUserName"})
    public static class SecPkgCredentials_Names
    extends Structure {
        public Pointer sUserName;

        public SecPkgCredentials_Names() {
            super(W32APITypeMapper.DEFAULT);
        }

        public synchronized String getUserName() {
            if (this.sUserName == null) {
                return null;
            }
            return Boolean.getBoolean("w32.ascii") ? this.sUserName.getString(0L) : this.sUserName.getWideString(0L);
        }

        public synchronized int free() {
            if (this.sUserName != null) {
                int result = Secur32.INSTANCE.FreeContextBuffer(this.sUserName);
                this.sUserName = null;
                return result;
            }
            return 0;
        }

        public static class ByReference
        extends SecPkgCredentials_Names
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"PackageInfo"})
    public static class SecPkgContext_PackageInfo
    extends Structure {
        public SecPkgInfo.ByReference PackageInfo;

        public SecPkgContext_PackageInfo() {
            super(W32APITypeMapper.DEFAULT);
        }

        public static class ByReference
        extends SecPkgContext_PackageInfo
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"fCapabilities", "wVersion", "wRPCID", "cbMaxToken", "Name", "Comment"})
    public static class SecPkgInfo
    extends Structure {
        public int fCapabilities;
        public short wVersion = 1;
        public short wRPCID;
        public int cbMaxToken;
        public String Name;
        public String Comment;

        public SecPkgInfo() {
            super(W32APITypeMapper.DEFAULT);
        }

        public static class ByReference
        extends SecPkgInfo
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"pPkgInfo"})
    public static class PSecPkgInfo
    extends Structure {
        public SecPkgInfo.ByReference pPkgInfo;

        public SecPkgInfo.ByReference[] toArray(int size) {
            return (SecPkgInfo.ByReference[])this.pPkgInfo.toArray(size);
        }

        public static class ByReference
        extends PSecPkgInfo
        implements Structure.ByReference {
        }
    }

    public static class TimeStamp
    extends SECURITY_INTEGER {
    }

    @Structure.FieldOrder(value={"dwLower", "dwUpper"})
    public static class SECURITY_INTEGER
    extends Structure {
        public int dwLower;
        public int dwUpper;
    }

    @Structure.FieldOrder(value={"ulVersion", "cBuffers", "pBuffers"})
    public static class SecBufferDesc
    extends Structure {
        public int ulVersion = 0;
        public int cBuffers = 1;
        public Pointer pBuffers;
    }

    @Structure.FieldOrder(value={"cbBuffer", "BufferType", "pvBuffer"})
    public static class SecBuffer
    extends Structure {
        public int cbBuffer;
        public int BufferType = 0;
        public Pointer pvBuffer;

        public SecBuffer() {
        }

        public SecBuffer(int type, int size) {
            this.cbBuffer = size;
            this.pvBuffer = new Memory(size);
            this.BufferType = type;
        }

        public SecBuffer(int type, byte[] token) {
            this.cbBuffer = token.length;
            this.pvBuffer = new Memory(token.length);
            this.pvBuffer.write(0L, token, 0, token.length);
            this.BufferType = type;
        }

        public byte[] getBytes() {
            return this.pvBuffer == null ? null : this.pvBuffer.getByteArray(0L, this.cbBuffer);
        }

        public static class ByReference
        extends SecBuffer
        implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(int type, int size) {
                super(type, size);
            }

            public ByReference(int type, byte[] token) {
                super(type, token);
            }
        }
    }

    public static class CtxtHandle
    extends SecHandle {
    }

    public static class CredHandle
    extends SecHandle {
    }

    @Structure.FieldOrder(value={"secHandle"})
    public static class PSecHandle
    extends Structure {
        public SecHandle.ByReference secHandle;

        public PSecHandle() {
        }

        public PSecHandle(SecHandle h) {
            super(h.getPointer());
            this.read();
        }

        public static class ByReference
        extends PSecHandle
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"dwLower", "dwUpper"})
    public static class SecHandle
    extends Structure {
        public Pointer dwLower;
        public Pointer dwUpper;

        public boolean isNull() {
            return this.dwLower == null && this.dwUpper == null;
        }

        public static class ByReference
        extends SecHandle
        implements Structure.ByReference {
        }
    }
}

