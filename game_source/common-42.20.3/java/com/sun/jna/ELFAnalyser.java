/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ELFAnalyser {
    private static final byte[] ELF_MAGIC = new byte[]{127, 69, 76, 70};
    private static final int EF_ARM_ABI_FLOAT_HARD = 1024;
    private static final int EF_ARM_ABI_FLOAT_SOFT = 512;
    private static final int EI_DATA_BIG_ENDIAN = 2;
    private static final int E_MACHINE_ARM = 40;
    private static final int EI_CLASS_64BIT = 2;
    private static final int SHN_UNDEF = 0;
    private static final int SHN_XINDEX = 65535;
    private final String filename;
    private boolean ELF = false;
    private boolean _64Bit = false;
    private boolean bigEndian = false;
    private boolean armHardFloatFlag = false;
    private boolean armSoftFloatFlag = false;
    private boolean armEabiAapcsVfp = false;
    private boolean arm = false;

    public static ELFAnalyser analyse(String filename) throws IOException {
        ELFAnalyser res = new ELFAnalyser(filename);
        res.runDetection();
        return res;
    }

    public boolean isELF() {
        return this.ELF;
    }

    public boolean is64Bit() {
        return this._64Bit;
    }

    public boolean isBigEndian() {
        return this.bigEndian;
    }

    public String getFilename() {
        return this.filename;
    }

    public boolean isArmHardFloat() {
        return this.isArmEabiAapcsVfp() || this.isArmHardFloatFlag();
    }

    public boolean isArmEabiAapcsVfp() {
        return this.armEabiAapcsVfp;
    }

    public boolean isArmHardFloatFlag() {
        return this.armHardFloatFlag;
    }

    public boolean isArmSoftFloatFlag() {
        return this.armSoftFloatFlag;
    }

    public boolean isArm() {
        return this.arm;
    }

    private ELFAnalyser(String filename) {
        this.filename = filename;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void runDetection() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.filename, "r");
        try {
            if (raf.length() > 4L) {
                byte[] magic = new byte[4];
                raf.seek(0L);
                raf.read(magic);
                if (Arrays.equals(magic, ELF_MAGIC)) {
                    this.ELF = true;
                }
            }
            if (!this.ELF) {
                return;
            }
            raf.seek(4L);
            byte sizeIndicator = raf.readByte();
            byte endianessIndicator = raf.readByte();
            this._64Bit = sizeIndicator == 2;
            this.bigEndian = endianessIndicator == 2;
            raf.seek(0L);
            ByteBuffer headerData = ByteBuffer.allocate(this._64Bit ? 64 : 52);
            raf.getChannel().read(headerData, 0L);
            headerData.order(this.bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            boolean bl = this.arm = headerData.get(18) == 40;
            if (this.arm) {
                int flags = headerData.getInt(this._64Bit ? 48 : 36);
                this.armHardFloatFlag = (flags & 0x400) == 1024;
                this.armSoftFloatFlag = (flags & 0x200) == 512;
                this.parseEabiAapcsVfp(headerData, raf);
            }
        }
        finally {
            try {
                raf.close();
            }
            catch (IOException magic) {}
        }
    }

    private void parseEabiAapcsVfp(ByteBuffer headerData, RandomAccessFile raf) throws IOException {
        ELFSectionHeaders sectionHeaders = new ELFSectionHeaders(this._64Bit, this.bigEndian, headerData, raf);
        for (ELFSectionHeaderEntry eshe : sectionHeaders.getEntries()) {
            if (!".ARM.attributes".equals(eshe.getName())) continue;
            ByteBuffer armAttributesBuffer = ByteBuffer.allocate((int)eshe.getSize());
            armAttributesBuffer.order(this.bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            raf.getChannel().read(armAttributesBuffer, eshe.getOffset());
            armAttributesBuffer.rewind();
            Map<Integer, Map<ArmAeabiAttributesTag, Object>> armAttributes = ELFAnalyser.parseArmAttributes(armAttributesBuffer);
            Map<ArmAeabiAttributesTag, Object> fileAttributes = armAttributes.get(1);
            if (fileAttributes == null) continue;
            Object abiVFPargValue = fileAttributes.get(ArmAeabiAttributesTag.ABI_VFP_args);
            if (abiVFPargValue instanceof Integer && ((Integer)abiVFPargValue).equals(1)) {
                this.armEabiAapcsVfp = true;
                continue;
            }
            if (!(abiVFPargValue instanceof BigInteger) || ((BigInteger)abiVFPargValue).intValue() != 1) continue;
            this.armEabiAapcsVfp = true;
        }
    }

    private static Map<Integer, Map<ArmAeabiAttributesTag, Object>> parseArmAttributes(ByteBuffer bb) {
        byte format = bb.get();
        if (format != 65) {
            return Collections.EMPTY_MAP;
        }
        while (bb.position() < bb.limit()) {
            int posSectionStart = bb.position();
            int sectionLength = bb.getInt();
            if (sectionLength <= 0) break;
            String vendorName = ELFAnalyser.readNTBS(bb, null);
            if ("aeabi".equals(vendorName)) {
                return ELFAnalyser.parseAEABI(bb);
            }
            ((Buffer)bb).position(posSectionStart + sectionLength);
        }
        return Collections.EMPTY_MAP;
    }

    private static Map<Integer, Map<ArmAeabiAttributesTag, Object>> parseAEABI(ByteBuffer buffer) {
        HashMap<Integer, Map<ArmAeabiAttributesTag, Object>> data = new HashMap<Integer, Map<ArmAeabiAttributesTag, Object>>();
        while (buffer.position() < buffer.limit()) {
            int pos = buffer.position();
            int subsectionTag = ELFAnalyser.readULEB128(buffer).intValue();
            int length = buffer.getInt();
            if (subsectionTag == 1) {
                data.put(subsectionTag, ELFAnalyser.parseFileAttribute(buffer));
            }
            ((Buffer)buffer).position(pos + length);
        }
        return data;
    }

    private static Map<ArmAeabiAttributesTag, Object> parseFileAttribute(ByteBuffer bb) {
        HashMap<ArmAeabiAttributesTag, Object> result = new HashMap<ArmAeabiAttributesTag, Object>();
        while (bb.position() < bb.limit()) {
            int tagValue = ELFAnalyser.readULEB128(bb).intValue();
            ArmAeabiAttributesTag tag = ArmAeabiAttributesTag.getByValue(tagValue);
            switch (tag.getParameterType().ordinal()) {
                case 0: {
                    result.put(tag, bb.getInt());
                    break;
                }
                case 1: {
                    result.put(tag, ELFAnalyser.readNTBS(bb, null));
                    break;
                }
                case 2: {
                    result.put(tag, ELFAnalyser.readULEB128(bb));
                }
            }
        }
        return result;
    }

    private static String readNTBS(ByteBuffer buffer, Integer position) {
        byte currentByte;
        if (position != null) {
            ((Buffer)buffer).position(position);
        }
        int startingPos = buffer.position();
        while ((currentByte = buffer.get()) != 0 && buffer.position() <= buffer.limit()) {
        }
        int terminatingPosition = buffer.position();
        byte[] data = new byte[terminatingPosition - startingPos - 1];
        ((Buffer)buffer).position(startingPos);
        buffer.get(data);
        ((Buffer)buffer).position(buffer.position() + 1);
        try {
            return new String(data, "ASCII");
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static BigInteger readULEB128(ByteBuffer buffer) {
        BigInteger result = BigInteger.ZERO;
        int shift = 0;
        while (true) {
            byte b = buffer.get();
            result = result.or(BigInteger.valueOf(b & 0x7F).shiftLeft(shift));
            if ((b & 0x80) == 0) break;
            shift += 7;
        }
        return result;
    }

    static class ELFSectionHeaders {
        private final List<ELFSectionHeaderEntry> entries = new ArrayList<ELFSectionHeaderEntry>();

        public ELFSectionHeaders(boolean _64bit, boolean bigEndian, ByteBuffer headerData, RandomAccessFile raf) throws IOException {
            int tableLength;
            int shstrndx;
            int shnum;
            short shentsize;
            long shoff;
            if (_64bit) {
                shoff = headerData.getLong(40);
                shentsize = headerData.getShort(58);
                shnum = headerData.getShort(60);
                shstrndx = headerData.getShort(62);
            } else {
                shoff = headerData.getInt(32);
                shentsize = headerData.getShort(46);
                shnum = headerData.getShort(48);
                shstrndx = headerData.getShort(50);
            }
            ByteBuffer sectionBuffer = ByteBuffer.allocate(shentsize);
            raf.getChannel().read(sectionBuffer, shoff);
            ELFSectionHeaderEntry section0 = new ELFSectionHeaderEntry(_64bit, sectionBuffer);
            int sectionCount = shnum == 0 && shoff != 0L ? (int)section0.getSize() : shnum;
            if (shstrndx == 65535) {
                shstrndx = section0.getLink();
            }
            if ((tableLength = sectionCount * shentsize) == 0 || shstrndx == 0) {
                return;
            }
            ByteBuffer data = ByteBuffer.allocate(tableLength);
            data.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            raf.getChannel().read(data, shoff);
            for (int i = 0; i < shnum; ++i) {
                data.position(i * shentsize);
                ByteBuffer header = data.slice();
                header.order(data.order());
                header.limit(shentsize);
                this.entries.add(new ELFSectionHeaderEntry(_64bit, header));
            }
            ELFSectionHeaderEntry stringTable = this.entries.get(shstrndx);
            ByteBuffer stringBuffer = ByteBuffer.allocate((int)stringTable.getSize());
            stringBuffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            raf.getChannel().read(stringBuffer, stringTable.getOffset());
            stringBuffer.rewind();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(20);
            for (ELFSectionHeaderEntry eshe : this.entries) {
                byte b;
                baos.reset();
                ((Buffer)stringBuffer).position(eshe.getNameOffset());
                while (stringBuffer.position() < stringBuffer.limit() && (b = stringBuffer.get()) != 0) {
                    baos.write(b);
                }
                eshe.setName(baos.toString("ASCII"));
            }
        }

        public List<ELFSectionHeaderEntry> getEntries() {
            return this.entries;
        }
    }

    static class ELFSectionHeaderEntry {
        private final int nameOffset;
        private String name;
        private final int type;
        private final long flags;
        private final long addr;
        private final long offset;
        private final long size;
        private final int link;

        public ELFSectionHeaderEntry(boolean _64bit, ByteBuffer sectionHeaderData) {
            this.nameOffset = sectionHeaderData.getInt(0);
            this.type = sectionHeaderData.getInt(4);
            this.flags = _64bit ? sectionHeaderData.getLong(8) : (long)sectionHeaderData.getInt(8);
            this.addr = _64bit ? sectionHeaderData.getLong(16) : (long)sectionHeaderData.getInt(12);
            this.offset = _64bit ? sectionHeaderData.getLong(24) : (long)sectionHeaderData.getInt(16);
            this.size = _64bit ? sectionHeaderData.getLong(32) : (long)sectionHeaderData.getInt(20);
            this.link = sectionHeaderData.getInt(_64bit ? 40 : 24);
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNameOffset() {
            return this.nameOffset;
        }

        public int getType() {
            return this.type;
        }

        public long getFlags() {
            return this.flags;
        }

        public long getOffset() {
            return this.offset;
        }

        public long getSize() {
            return this.size;
        }

        public long getAddr() {
            return this.addr;
        }

        public int getLink() {
            return this.link;
        }

        public String toString() {
            return String.format("ELFSectionHeaderEntry{nameOffset=%1$d (0x%1$x), name=%2$s, type=%3$d (0x%3$x), flags=%4$d (0x%4$x), addr=%5$d (0x%5$x), offset=%6$d (0x%6$x), size=%7$d (0x%7$x), link=%8$d (0x%8$x)}", this.nameOffset, this.name, this.type, this.flags, this.addr, this.offset, this.size, this.link);
        }
    }

    static class ArmAeabiAttributesTag {
        private final int value;
        private final String name;
        private final ParameterType parameterType;
        private static final List<ArmAeabiAttributesTag> tags = new LinkedList<ArmAeabiAttributesTag>();
        private static final Map<Integer, ArmAeabiAttributesTag> valueMap = new HashMap<Integer, ArmAeabiAttributesTag>();
        private static final Map<String, ArmAeabiAttributesTag> nameMap = new HashMap<String, ArmAeabiAttributesTag>();
        public static final ArmAeabiAttributesTag File = ArmAeabiAttributesTag.addTag(1, "File", ParameterType.UINT32);
        public static final ArmAeabiAttributesTag Section = ArmAeabiAttributesTag.addTag(2, "Section", ParameterType.UINT32);
        public static final ArmAeabiAttributesTag Symbol = ArmAeabiAttributesTag.addTag(3, "Symbol", ParameterType.UINT32);
        public static final ArmAeabiAttributesTag CPU_raw_name = ArmAeabiAttributesTag.addTag(4, "CPU_raw_name", ParameterType.NTBS);
        public static final ArmAeabiAttributesTag CPU_name = ArmAeabiAttributesTag.addTag(5, "CPU_name", ParameterType.NTBS);
        public static final ArmAeabiAttributesTag CPU_arch = ArmAeabiAttributesTag.addTag(6, "CPU_arch", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag CPU_arch_profile = ArmAeabiAttributesTag.addTag(7, "CPU_arch_profile", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ARM_ISA_use = ArmAeabiAttributesTag.addTag(8, "ARM_ISA_use", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag THUMB_ISA_use = ArmAeabiAttributesTag.addTag(9, "THUMB_ISA_use", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag FP_arch = ArmAeabiAttributesTag.addTag(10, "FP_arch", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag WMMX_arch = ArmAeabiAttributesTag.addTag(11, "WMMX_arch", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag Advanced_SIMD_arch = ArmAeabiAttributesTag.addTag(12, "Advanced_SIMD_arch", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag PCS_config = ArmAeabiAttributesTag.addTag(13, "PCS_config", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_PCS_R9_use = ArmAeabiAttributesTag.addTag(14, "ABI_PCS_R9_use", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_PCS_RW_data = ArmAeabiAttributesTag.addTag(15, "ABI_PCS_RW_data", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_PCS_RO_data = ArmAeabiAttributesTag.addTag(16, "ABI_PCS_RO_data", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_PCS_GOT_use = ArmAeabiAttributesTag.addTag(17, "ABI_PCS_GOT_use", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_PCS_wchar_t = ArmAeabiAttributesTag.addTag(18, "ABI_PCS_wchar_t", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_FP_rounding = ArmAeabiAttributesTag.addTag(19, "ABI_FP_rounding", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_FP_denormal = ArmAeabiAttributesTag.addTag(20, "ABI_FP_denormal", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_FP_exceptions = ArmAeabiAttributesTag.addTag(21, "ABI_FP_exceptions", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_FP_user_exceptions = ArmAeabiAttributesTag.addTag(22, "ABI_FP_user_exceptions", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_FP_number_model = ArmAeabiAttributesTag.addTag(23, "ABI_FP_number_model", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_align_needed = ArmAeabiAttributesTag.addTag(24, "ABI_align_needed", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_align8_preserved = ArmAeabiAttributesTag.addTag(25, "ABI_align8_preserved", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_enum_size = ArmAeabiAttributesTag.addTag(26, "ABI_enum_size", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_HardFP_use = ArmAeabiAttributesTag.addTag(27, "ABI_HardFP_use", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_VFP_args = ArmAeabiAttributesTag.addTag(28, "ABI_VFP_args", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_WMMX_args = ArmAeabiAttributesTag.addTag(29, "ABI_WMMX_args", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_optimization_goals = ArmAeabiAttributesTag.addTag(30, "ABI_optimization_goals", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_FP_optimization_goals = ArmAeabiAttributesTag.addTag(31, "ABI_FP_optimization_goals", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag compatibility = ArmAeabiAttributesTag.addTag(32, "compatibility", ParameterType.NTBS);
        public static final ArmAeabiAttributesTag CPU_unaligned_access = ArmAeabiAttributesTag.addTag(34, "CPU_unaligned_access", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag FP_HP_extension = ArmAeabiAttributesTag.addTag(36, "FP_HP_extension", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag ABI_FP_16bit_format = ArmAeabiAttributesTag.addTag(38, "ABI_FP_16bit_format", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag MPextension_use = ArmAeabiAttributesTag.addTag(42, "MPextension_use", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag DIV_use = ArmAeabiAttributesTag.addTag(44, "DIV_use", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag nodefaults = ArmAeabiAttributesTag.addTag(64, "nodefaults", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag also_compatible_with = ArmAeabiAttributesTag.addTag(65, "also_compatible_with", ParameterType.NTBS);
        public static final ArmAeabiAttributesTag conformance = ArmAeabiAttributesTag.addTag(67, "conformance", ParameterType.NTBS);
        public static final ArmAeabiAttributesTag T2EE_use = ArmAeabiAttributesTag.addTag(66, "T2EE_use", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag Virtualization_use = ArmAeabiAttributesTag.addTag(68, "Virtualization_use", ParameterType.ULEB128);
        public static final ArmAeabiAttributesTag MPextension_use2 = ArmAeabiAttributesTag.addTag(70, "MPextension_use", ParameterType.ULEB128);

        public ArmAeabiAttributesTag(int value, String name, ParameterType parameterType) {
            this.value = value;
            this.name = name;
            this.parameterType = parameterType;
        }

        public int getValue() {
            return this.value;
        }

        public String getName() {
            return this.name;
        }

        public ParameterType getParameterType() {
            return this.parameterType;
        }

        public String toString() {
            return this.name + " (" + this.value + ")";
        }

        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + this.value;
            return hash;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            ArmAeabiAttributesTag other = (ArmAeabiAttributesTag)obj;
            return this.value == other.value;
        }

        private static ArmAeabiAttributesTag addTag(int value, String name, ParameterType type) {
            ArmAeabiAttributesTag tag = new ArmAeabiAttributesTag(value, name, type);
            if (!valueMap.containsKey(tag.getValue())) {
                valueMap.put(tag.getValue(), tag);
            }
            if (!nameMap.containsKey(tag.getName())) {
                nameMap.put(tag.getName(), tag);
            }
            tags.add(tag);
            return tag;
        }

        public static List<ArmAeabiAttributesTag> getTags() {
            return Collections.unmodifiableList(tags);
        }

        public static ArmAeabiAttributesTag getByName(String name) {
            return nameMap.get(name);
        }

        public static ArmAeabiAttributesTag getByValue(int value) {
            if (valueMap.containsKey(value)) {
                return valueMap.get(value);
            }
            ArmAeabiAttributesTag pseudoTag = new ArmAeabiAttributesTag(value, "Unknown " + value, ArmAeabiAttributesTag.getParameterType(value));
            return pseudoTag;
        }

        private static ParameterType getParameterType(int value) {
            ArmAeabiAttributesTag tag = ArmAeabiAttributesTag.getByValue(value);
            if (tag == null) {
                if (value % 2 == 0) {
                    return ParameterType.ULEB128;
                }
                return ParameterType.NTBS;
            }
            return tag.getParameterType();
        }

        public static enum ParameterType {
            UINT32,
            NTBS,
            ULEB128;

        }
    }
}

