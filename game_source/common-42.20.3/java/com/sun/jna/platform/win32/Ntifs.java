/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APITypeMapper;

public interface Ntifs
extends WinDef,
BaseTSD {
    public static final int MAXIMUM_REPARSE_DATA_BUFFER_SIZE = 16384;
    public static final int REPARSE_BUFFER_HEADER_SIZE = 8;
    public static final int SYMLINK_FLAG_RELATIVE = 1;

    @Structure.FieldOrder(value={"ReparseTag", "ReparseDataLength", "Reserved", "u"})
    public static class REPARSE_DATA_BUFFER
    extends Structure {
        public int ReparseTag = 0;
        public short ReparseDataLength = 0;
        public short Reserved = 0;
        public REPARSE_UNION u;

        public static int sizeOf() {
            return Native.getNativeSize(REPARSE_DATA_BUFFER.class, null);
        }

        public int getSize() {
            return 8 + this.ReparseDataLength;
        }

        public REPARSE_DATA_BUFFER() {
        }

        public REPARSE_DATA_BUFFER(int ReparseTag, short Reserved) {
            this.ReparseTag = ReparseTag;
            this.Reserved = Reserved;
            this.ReparseDataLength = 0;
            this.write();
        }

        public REPARSE_DATA_BUFFER(int ReparseTag, short Reserved, SymbolicLinkReparseBuffer symLinkReparseBuffer) {
            this.ReparseTag = ReparseTag;
            this.Reserved = Reserved;
            this.ReparseDataLength = (short)symLinkReparseBuffer.size();
            this.u.setType(SymbolicLinkReparseBuffer.class);
            this.u.symLinkReparseBuffer = symLinkReparseBuffer;
            this.write();
        }

        public REPARSE_DATA_BUFFER(Pointer memory) {
            super(memory);
            this.read();
        }

        @Override
        public void read() {
            super.read();
            switch (this.ReparseTag) {
                default: {
                    this.u.setType(GenericReparseBuffer.class);
                    break;
                }
                case -1610612733: {
                    this.u.setType(MountPointReparseBuffer.class);
                    break;
                }
                case -1610612724: {
                    this.u.setType(SymbolicLinkReparseBuffer.class);
                }
            }
            this.u.read();
        }

        public static class REPARSE_UNION
        extends Union {
            public SymbolicLinkReparseBuffer symLinkReparseBuffer;
            public MountPointReparseBuffer mountPointReparseBuffer;
            public GenericReparseBuffer genericReparseBuffer;

            public REPARSE_UNION() {
            }

            public REPARSE_UNION(Pointer memory) {
                super(memory);
            }

            public static class ByReference
            extends REPARSE_UNION
            implements Structure.ByReference {
            }
        }

        public static class ByReference
        extends REPARSE_DATA_BUFFER
        implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(Pointer memory) {
                super(memory);
            }
        }
    }

    @Structure.FieldOrder(value={"DataBuffer"})
    public static class GenericReparseBuffer
    extends Structure {
        public byte[] DataBuffer = new byte[16384];

        public static int sizeOf() {
            return Native.getNativeSize(GenericReparseBuffer.class, null);
        }

        public GenericReparseBuffer() {
        }

        public GenericReparseBuffer(Pointer memory) {
            super(memory);
            this.read();
        }

        public GenericReparseBuffer(String DataBuffer2) {
            this.DataBuffer = DataBuffer2.getBytes();
            this.write();
        }

        public static class ByReference
        extends GenericReparseBuffer
        implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(Pointer memory) {
                super(memory);
            }
        }
    }

    @Structure.FieldOrder(value={"SubstituteNameOffset", "SubstituteNameLength", "PrintNameOffset", "PrintNameLength", "PathBuffer"})
    public static class MountPointReparseBuffer
    extends Structure {
        public short SubstituteNameOffset = 0;
        public short SubstituteNameLength = 0;
        public short PrintNameOffset = 0;
        public short PrintNameLength = 0;
        public char[] PathBuffer = new char[8192];

        public static int sizeOf() {
            return Native.getNativeSize(MountPointReparseBuffer.class, null);
        }

        public MountPointReparseBuffer() {
            super(W32APITypeMapper.UNICODE);
        }

        public MountPointReparseBuffer(Pointer memory) {
            super(memory, 0, W32APITypeMapper.UNICODE);
            this.read();
        }

        public MountPointReparseBuffer(String substituteName, String printName) {
            String bothNames = substituteName + printName;
            this.PathBuffer = bothNames.toCharArray();
            this.SubstituteNameOffset = 0;
            this.SubstituteNameLength = (short)substituteName.length();
            this.PrintNameOffset = (short)(substituteName.length() * 2);
            this.PrintNameLength = (short)(printName.length() * 2);
            this.write();
        }

        public MountPointReparseBuffer(short SubstituteNameOffset, short SubstituteNameLength, short PrintNameOffset, short PrintNameLength, String PathBuffer) {
            this.SubstituteNameOffset = SubstituteNameOffset;
            this.SubstituteNameLength = SubstituteNameLength;
            this.PrintNameOffset = PrintNameOffset;
            this.PrintNameLength = PrintNameLength;
            this.PathBuffer = PathBuffer.toCharArray();
            this.write();
        }

        public static class ByReference
        extends MountPointReparseBuffer
        implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(Pointer memory) {
                super(memory);
            }
        }
    }

    @Structure.FieldOrder(value={"SubstituteNameOffset", "SubstituteNameLength", "PrintNameOffset", "PrintNameLength", "Flags", "PathBuffer"})
    public static class SymbolicLinkReparseBuffer
    extends Structure {
        public short SubstituteNameOffset = 0;
        public short SubstituteNameLength = 0;
        public short PrintNameOffset = 0;
        public short PrintNameLength = 0;
        public int Flags = 0;
        public char[] PathBuffer = new char[8192];

        public static int sizeOf() {
            return Native.getNativeSize(MountPointReparseBuffer.class, null);
        }

        public SymbolicLinkReparseBuffer() {
            super(W32APITypeMapper.UNICODE);
        }

        public SymbolicLinkReparseBuffer(Pointer memory) {
            super(memory, 0, W32APITypeMapper.UNICODE);
            this.read();
        }

        public SymbolicLinkReparseBuffer(String substituteName, String printName, int Flags2) {
            String bothNames = substituteName + printName;
            this.PathBuffer = bothNames.toCharArray();
            this.SubstituteNameOffset = 0;
            this.SubstituteNameLength = (short)(substituteName.length() * 2);
            this.PrintNameOffset = (short)(substituteName.length() * 2);
            this.PrintNameLength = (short)(printName.length() * 2);
            this.Flags = Flags2;
            this.write();
        }

        public SymbolicLinkReparseBuffer(short SubstituteNameOffset, short SubstituteNameLength, short PrintNameOffset, short PrintNameLength, int Flags2, String PathBuffer) {
            this.SubstituteNameOffset = SubstituteNameOffset;
            this.SubstituteNameLength = SubstituteNameLength;
            this.PrintNameOffset = PrintNameOffset;
            this.PrintNameLength = PrintNameLength;
            this.Flags = Flags2;
            this.PathBuffer = PathBuffer.toCharArray();
            this.write();
        }

        public String getPrintName() {
            return String.copyValueOf(this.PathBuffer, this.PrintNameOffset / 2, this.PrintNameLength / 2);
        }

        public String getSubstituteName() {
            return String.copyValueOf(this.PathBuffer, this.SubstituteNameOffset / 2, this.SubstituteNameLength / 2);
        }

        public static class ByReference
        extends SymbolicLinkReparseBuffer
        implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(Pointer memory) {
                super(memory);
            }
        }
    }
}

