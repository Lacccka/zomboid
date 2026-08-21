/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.EntryStreamOffsets;
import org.apache.commons.compress.archivers.zip.ExtraFieldParsingBehavior;
import org.apache.commons.compress.archivers.zip.ExtraFieldUtils;
import org.apache.commons.compress.archivers.zip.GeneralPurposeBit;
import org.apache.commons.compress.archivers.zip.UnparseableExtraFieldData;
import org.apache.commons.compress.archivers.zip.UnrecognizedExtraField;
import org.apache.commons.compress.archivers.zip.X000A_NTFS;
import org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipShort;
import org.apache.commons.compress.archivers.zip.ZipUtil;
import org.apache.commons.compress.utils.ByteUtils;
import org.apache.commons.io.file.attribute.FileTimes;

public class ZipArchiveEntry
extends ZipEntry
implements ArchiveEntry,
EntryStreamOffsets {
    private static final String ZIP_DIR_SEP = "/";
    static final ZipArchiveEntry[] EMPTY_ARRAY = new ZipArchiveEntry[0];
    static LinkedList<ZipArchiveEntry> EMPTY_LINKED_LIST = new LinkedList();
    public static final int PLATFORM_UNIX = 3;
    public static final int PLATFORM_FAT = 0;
    public static final int CRC_UNKNOWN = -1;
    private static final int SHORT_MASK = 65535;
    private static final int SHORT_SHIFT = 16;
    private int method = -1;
    private long size = -1L;
    private int internalAttributes;
    private int versionRequired;
    private int versionMadeBy;
    private int platform = 0;
    private int rawFlag;
    private long externalAttributes;
    private int alignment;
    private ZipExtraField[] extraFields;
    private UnparseableExtraFieldData unparseableExtra;
    private String name;
    private byte[] rawName;
    private GeneralPurposeBit generalPurposeBit = new GeneralPurposeBit();
    private long localHeaderOffset = -1L;
    private long dataOffset = -1L;
    private boolean isStreamContiguous;
    private NameSource nameSource = NameSource.NAME;
    private final Function<ZipShort, ZipExtraField> extraFieldFactory;
    private CommentSource commentSource = CommentSource.COMMENT;
    private long diskNumberStart;
    private boolean lastModifiedDateSet;
    private long time = -1L;

    private static boolean canConvertToInfoZipExtendedTimestamp(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime creationTime) {
        return FileTimes.isUnixTime(lastModifiedTime) && FileTimes.isUnixTime(lastAccessTime) && FileTimes.isUnixTime(creationTime);
    }

    private static boolean isDirectoryEntryName(String entryName) {
        return entryName.endsWith(ZIP_DIR_SEP);
    }

    private static String toDirectoryEntryName(String entryName) {
        return ZipArchiveEntry.isDirectoryEntryName(entryName) ? entryName : entryName + ZIP_DIR_SEP;
    }

    private static String toEntryName(File inputFile, String entryName) {
        return inputFile.isDirectory() ? ZipArchiveEntry.toDirectoryEntryName(entryName) : entryName;
    }

    private static String toEntryName(Path inputPath, String entryName, LinkOption ... options) {
        return Files.isDirectory(inputPath, options) ? ZipArchiveEntry.toDirectoryEntryName(entryName) : entryName;
    }

    protected ZipArchiveEntry() {
        this("");
    }

    public ZipArchiveEntry(File inputFile, String entryName) {
        this(null, inputFile, entryName);
    }

    private ZipArchiveEntry(Function<ZipShort, ZipExtraField> extraFieldFactory, File inputFile, String entryName) {
        this(extraFieldFactory, ZipArchiveEntry.toEntryName(inputFile, entryName));
        try {
            this.setAttributes(inputFile.toPath(), new LinkOption[0]);
        }
        catch (IOException e) {
            if (inputFile.isFile()) {
                this.setSize(inputFile.length());
            }
            this.setTime(inputFile.lastModified());
        }
    }

    private ZipArchiveEntry(Function<ZipShort, ZipExtraField> extraFieldFactory, Path inputPath, String entryName, LinkOption ... options) throws IOException {
        this(extraFieldFactory, ZipArchiveEntry.toEntryName(inputPath, entryName, options));
        this.setAttributes(inputPath, options);
    }

    private ZipArchiveEntry(Function<ZipShort, ZipExtraField> extraFieldFactory, String name) {
        super(name);
        this.extraFieldFactory = extraFieldFactory;
        this.setName(name);
    }

    private ZipArchiveEntry(Function<ZipShort, ZipExtraField> extraFieldFactory, ZipEntry entry) throws ZipException {
        super(entry);
        this.extraFieldFactory = extraFieldFactory;
        this.setName(entry.getName());
        byte[] extra = entry.getExtra();
        if (extra != null) {
            this.setExtraFields(this.parseExtraFields(extra, true, ExtraFieldParsingMode.BEST_EFFORT));
        } else {
            this.setExtra();
        }
        this.setMethod(entry.getMethod());
        this.size = entry.getSize();
    }

    public ZipArchiveEntry(Path inputPath, String entryName, LinkOption ... options) throws IOException {
        this(null, inputPath, entryName, options);
    }

    public ZipArchiveEntry(String name) {
        this((Function<ZipShort, ZipExtraField>)null, name);
    }

    public ZipArchiveEntry(ZipArchiveEntry entry) throws ZipException {
        this((ZipEntry)entry);
        this.setInternalAttributes(entry.getInternalAttributes());
        this.setExternalAttributes(entry.getExternalAttributes());
        this.setExtraFields(entry.getAllExtraFieldsNoCopy());
        this.setPlatform(entry.getPlatform());
        GeneralPurposeBit other = entry.getGeneralPurposeBit();
        this.setGeneralPurposeBit(other == null ? null : (GeneralPurposeBit)other.clone());
    }

    public ZipArchiveEntry(ZipEntry entry) throws ZipException {
        this(null, entry);
    }

    public void addAsFirstExtraField(ZipExtraField ze) {
        if (ze instanceof UnparseableExtraFieldData) {
            this.unparseableExtra = (UnparseableExtraFieldData)ze;
        } else {
            if (this.getExtraField(ze.getHeaderId()) != null) {
                this.internalRemoveExtraField(ze.getHeaderId());
            }
            ZipExtraField[] copy = this.extraFields;
            int newLen = this.extraFields != null ? this.extraFields.length + 1 : 1;
            this.extraFields = new ZipExtraField[newLen];
            this.extraFields[0] = ze;
            if (copy != null) {
                System.arraycopy(copy, 0, this.extraFields, 1, this.extraFields.length - 1);
            }
        }
        this.setExtra();
    }

    public void addExtraField(ZipExtraField ze) {
        this.internalAddExtraField(ze);
        this.setExtra();
    }

    private void addInfoZipExtendedTimestamp(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime creationTime) {
        X5455_ExtendedTimestamp infoZipTimestamp = new X5455_ExtendedTimestamp();
        if (lastModifiedTime != null) {
            infoZipTimestamp.setModifyFileTime(lastModifiedTime);
        }
        if (lastAccessTime != null) {
            infoZipTimestamp.setAccessFileTime(lastAccessTime);
        }
        if (creationTime != null) {
            infoZipTimestamp.setCreateFileTime(creationTime);
        }
        this.internalAddExtraField(infoZipTimestamp);
    }

    private void addNTFSTimestamp(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime creationTime) {
        X000A_NTFS ntfsTimestamp = new X000A_NTFS();
        if (lastModifiedTime != null) {
            ntfsTimestamp.setModifyFileTime(lastModifiedTime);
        }
        if (lastAccessTime != null) {
            ntfsTimestamp.setAccessFileTime(lastAccessTime);
        }
        if (creationTime != null) {
            ntfsTimestamp.setCreateFileTime(creationTime);
        }
        this.internalAddExtraField(ntfsTimestamp);
    }

    @Override
    public Object clone() {
        ZipArchiveEntry e = (ZipArchiveEntry)super.clone();
        e.setInternalAttributes(this.getInternalAttributes());
        e.setExternalAttributes(this.getExternalAttributes());
        e.setExtraFields(this.getAllExtraFieldsNoCopy());
        return e;
    }

    private ZipExtraField[] copyOf(ZipExtraField[] src, int length) {
        return Arrays.copyOf(src, length);
    }

    public boolean equals(Object obj) {
        String otherName;
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ZipArchiveEntry other = (ZipArchiveEntry)obj;
        String myName = this.getName();
        if (!Objects.equals(myName, otherName = other.getName())) {
            return false;
        }
        String myComment = this.getComment();
        String otherComment = other.getComment();
        if (myComment == null) {
            myComment = "";
        }
        if (otherComment == null) {
            otherComment = "";
        }
        return Objects.equals(this.getLastModifiedTime(), other.getLastModifiedTime()) && Objects.equals(this.getLastAccessTime(), other.getLastAccessTime()) && Objects.equals(this.getCreationTime(), other.getCreationTime()) && myComment.equals(otherComment) && this.getInternalAttributes() == other.getInternalAttributes() && this.getPlatform() == other.getPlatform() && this.getExternalAttributes() == other.getExternalAttributes() && this.getMethod() == other.getMethod() && this.getSize() == other.getSize() && this.getCrc() == other.getCrc() && this.getCompressedSize() == other.getCompressedSize() && Arrays.equals(this.getCentralDirectoryExtra(), other.getCentralDirectoryExtra()) && Arrays.equals(this.getLocalFileDataExtra(), other.getLocalFileDataExtra()) && this.localHeaderOffset == other.localHeaderOffset && this.dataOffset == other.dataOffset && this.generalPurposeBit.equals(other.generalPurposeBit);
    }

    private ZipExtraField findMatching(ZipShort headerId, List<ZipExtraField> fs) {
        return fs.stream().filter(f -> headerId.equals(f.getHeaderId())).findFirst().orElse(null);
    }

    private ZipExtraField findUnparseable(List<ZipExtraField> fs) {
        return fs.stream().filter(UnparseableExtraFieldData.class::isInstance).findFirst().orElse(null);
    }

    protected int getAlignment() {
        return this.alignment;
    }

    private ZipExtraField[] getAllExtraFields() {
        ZipExtraField[] allExtraFieldsNoCopy = this.getAllExtraFieldsNoCopy();
        return allExtraFieldsNoCopy == this.extraFields ? this.copyOf(allExtraFieldsNoCopy, allExtraFieldsNoCopy.length) : allExtraFieldsNoCopy;
    }

    private ZipExtraField[] getAllExtraFieldsNoCopy() {
        if (this.extraFields == null) {
            return this.getUnparseableOnly();
        }
        return this.unparseableExtra != null ? this.getMergedFields() : this.extraFields;
    }

    public byte[] getCentralDirectoryExtra() {
        return ExtraFieldUtils.mergeCentralDirectoryData(this.getAllExtraFieldsNoCopy());
    }

    public CommentSource getCommentSource() {
        return this.commentSource;
    }

    @Override
    public long getDataOffset() {
        return this.dataOffset;
    }

    public long getDiskNumberStart() {
        return this.diskNumberStart;
    }

    public long getExternalAttributes() {
        return this.externalAttributes;
    }

    public ZipExtraField getExtraField(ZipShort type) {
        if (this.extraFields != null) {
            for (ZipExtraField extraField : this.extraFields) {
                if (!type.equals(extraField.getHeaderId())) continue;
                return extraField;
            }
        }
        return null;
    }

    public ZipExtraField[] getExtraFields() {
        return this.getParseableExtraFields();
    }

    public ZipExtraField[] getExtraFields(boolean includeUnparseable) {
        return includeUnparseable ? this.getAllExtraFields() : this.getParseableExtraFields();
    }

    public ZipExtraField[] getExtraFields(ExtraFieldParsingBehavior parsingBehavior) throws ZipException {
        if (parsingBehavior == ExtraFieldParsingMode.BEST_EFFORT) {
            return this.getExtraFields(true);
        }
        if (parsingBehavior == ExtraFieldParsingMode.ONLY_PARSEABLE_LENIENT) {
            return this.getExtraFields(false);
        }
        byte[] local = this.getExtra();
        ArrayList<ZipExtraField> localFields = new ArrayList<ZipExtraField>(Arrays.asList(this.parseExtraFields(local, true, parsingBehavior)));
        byte[] central = this.getCentralDirectoryExtra();
        ArrayList<ZipExtraField> centralFields = new ArrayList<ZipExtraField>(Arrays.asList(this.parseExtraFields(central, false, parsingBehavior)));
        ArrayList<ZipExtraField> merged = new ArrayList<ZipExtraField>();
        for (ZipExtraField l : localFields) {
            ZipExtraField c = l instanceof UnparseableExtraFieldData ? this.findUnparseable(centralFields) : this.findMatching(l.getHeaderId(), centralFields);
            if (c != null) {
                byte[] cd = c.getCentralDirectoryData();
                if (cd != null && cd.length > 0) {
                    l.parseFromCentralDirectoryData(cd, 0, cd.length);
                }
                centralFields.remove(c);
            }
            merged.add(l);
        }
        merged.addAll(centralFields);
        return merged.toArray(ExtraFieldUtils.EMPTY_ZIP_EXTRA_FIELD_ARRAY);
    }

    public GeneralPurposeBit getGeneralPurposeBit() {
        return this.generalPurposeBit;
    }

    public int getInternalAttributes() {
        return this.internalAttributes;
    }

    @Override
    public Date getLastModifiedDate() {
        return new Date(this.getTime());
    }

    public byte[] getLocalFileDataExtra() {
        byte[] extra = this.getExtra();
        return extra != null ? extra : ByteUtils.EMPTY_BYTE_ARRAY;
    }

    public long getLocalHeaderOffset() {
        return this.localHeaderOffset;
    }

    private ZipExtraField[] getMergedFields() {
        ZipExtraField[] zipExtraFields = this.copyOf(this.extraFields, this.extraFields.length + 1);
        zipExtraFields[this.extraFields.length] = this.unparseableExtra;
        return zipExtraFields;
    }

    @Override
    public int getMethod() {
        return this.method;
    }

    @Override
    public String getName() {
        return this.name == null ? super.getName() : this.name;
    }

    public NameSource getNameSource() {
        return this.nameSource;
    }

    private ZipExtraField[] getParseableExtraFields() {
        ZipExtraField[] parseableExtraFields = this.getParseableExtraFieldsNoCopy();
        return parseableExtraFields == this.extraFields ? this.copyOf(parseableExtraFields, parseableExtraFields.length) : parseableExtraFields;
    }

    private ZipExtraField[] getParseableExtraFieldsNoCopy() {
        if (this.extraFields == null) {
            return ExtraFieldUtils.EMPTY_ZIP_EXTRA_FIELD_ARRAY;
        }
        return this.extraFields;
    }

    public int getPlatform() {
        return this.platform;
    }

    public int getRawFlag() {
        return this.rawFlag;
    }

    public byte[] getRawName() {
        if (this.rawName != null) {
            return Arrays.copyOf(this.rawName, this.rawName.length);
        }
        return null;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public long getTime() {
        if (this.lastModifiedDateSet) {
            return this.getLastModifiedTime().toMillis();
        }
        return this.time != -1L ? this.time : super.getTime();
    }

    public int getUnixMode() {
        return this.platform != 3 ? 0 : (int)(this.getExternalAttributes() >> 16 & 0xFFFFL);
    }

    public UnparseableExtraFieldData getUnparseableExtraFieldData() {
        return this.unparseableExtra;
    }

    private ZipExtraField[] getUnparseableOnly() {
        ZipExtraField[] zipExtraFieldArray;
        if (this.unparseableExtra == null) {
            zipExtraFieldArray = ExtraFieldUtils.EMPTY_ZIP_EXTRA_FIELD_ARRAY;
        } else {
            ZipExtraField[] zipExtraFieldArray2 = new ZipExtraField[1];
            zipExtraFieldArray = zipExtraFieldArray2;
            zipExtraFieldArray2[0] = this.unparseableExtra;
        }
        return zipExtraFieldArray;
    }

    public int getVersionMadeBy() {
        return this.versionMadeBy;
    }

    public int getVersionRequired() {
        return this.versionRequired;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    private void internalAddExtraField(ZipExtraField ze) {
        if (ze instanceof UnparseableExtraFieldData) {
            this.unparseableExtra = (UnparseableExtraFieldData)ze;
        } else if (this.extraFields == null) {
            this.extraFields = new ZipExtraField[]{ze};
        } else {
            if (this.getExtraField(ze.getHeaderId()) != null) {
                this.internalRemoveExtraField(ze.getHeaderId());
            }
            ZipExtraField[] zipExtraFields = this.copyOf(this.extraFields, this.extraFields.length + 1);
            zipExtraFields[zipExtraFields.length - 1] = ze;
            this.extraFields = zipExtraFields;
        }
    }

    private void internalRemoveExtraField(ZipShort type) {
        if (this.extraFields == null) {
            return;
        }
        ArrayList<ZipExtraField> newResult = new ArrayList<ZipExtraField>();
        for (ZipExtraField extraField : this.extraFields) {
            if (type.equals(extraField.getHeaderId())) continue;
            newResult.add(extraField);
        }
        if (this.extraFields.length == newResult.size()) {
            return;
        }
        this.extraFields = newResult.toArray(ExtraFieldUtils.EMPTY_ZIP_EXTRA_FIELD_ARRAY);
    }

    private void internalSetLastModifiedTime(FileTime time) {
        super.setLastModifiedTime(time);
        this.time = time.toMillis();
        this.lastModifiedDateSet = true;
    }

    @Override
    public boolean isDirectory() {
        return ZipArchiveEntry.isDirectoryEntryName(this.getName());
    }

    @Override
    public boolean isStreamContiguous() {
        return this.isStreamContiguous;
    }

    public boolean isUnixSymlink() {
        return (this.getUnixMode() & 0xF000) == 40960;
    }

    private void mergeExtraFields(ZipExtraField[] f, boolean local) {
        if (this.extraFields == null) {
            this.setExtraFields(f);
        } else {
            for (ZipExtraField element : f) {
                ZipExtraField existing = element instanceof UnparseableExtraFieldData ? this.unparseableExtra : this.getExtraField(element.getHeaderId());
                if (existing == null) {
                    this.internalAddExtraField(element);
                    continue;
                }
                byte[] b = local ? element.getLocalFileDataData() : element.getCentralDirectoryData();
                try {
                    if (local) {
                        existing.parseFromLocalFileData(b, 0, b.length);
                        continue;
                    }
                    existing.parseFromCentralDirectoryData(b, 0, b.length);
                }
                catch (ZipException ex) {
                    UnrecognizedExtraField u = new UnrecognizedExtraField();
                    u.setHeaderId(existing.getHeaderId());
                    if (local) {
                        u.setLocalFileDataData(b);
                        u.setCentralDirectoryData(existing.getCentralDirectoryData());
                    } else {
                        u.setLocalFileDataData(existing.getLocalFileDataData());
                        u.setCentralDirectoryData(b);
                    }
                    this.internalRemoveExtraField(existing.getHeaderId());
                    this.internalAddExtraField(u);
                }
            }
            this.setExtra();
        }
    }

    private ZipExtraField[] parseExtraFields(byte[] data, boolean local, final ExtraFieldParsingBehavior parsingBehavior) throws ZipException {
        if (this.extraFieldFactory != null) {
            return ExtraFieldUtils.parse(data, local, new ExtraFieldParsingBehavior(){

                @Override
                public ZipExtraField createExtraField(ZipShort headerId) throws ZipException, InstantiationException, IllegalAccessException {
                    ZipExtraField field = (ZipExtraField)ZipArchiveEntry.this.extraFieldFactory.apply(headerId);
                    return field == null ? parsingBehavior.createExtraField(headerId) : field;
                }

                @Override
                public ZipExtraField fill(ZipExtraField field, byte[] data, int off, int len, boolean local) throws ZipException {
                    return parsingBehavior.fill(field, data, off, len, local);
                }

                @Override
                public ZipExtraField onUnparseableExtraField(byte[] data, int off, int len, boolean local, int claimedLength) throws ZipException {
                    return parsingBehavior.onUnparseableExtraField(data, off, len, local, claimedLength);
                }
            });
        }
        return ExtraFieldUtils.parse(data, local, parsingBehavior);
    }

    public void removeExtraField(ZipShort type) {
        if (this.getExtraField(type) == null) {
            throw new NoSuchElementException();
        }
        this.internalRemoveExtraField(type);
        this.setExtra();
    }

    public void removeUnparseableExtraFieldData() {
        if (this.unparseableExtra == null) {
            throw new NoSuchElementException();
        }
        this.unparseableExtra = null;
        this.setExtra();
    }

    private boolean requiresExtraTimeFields() {
        if (this.getLastAccessTime() != null || this.getCreationTime() != null) {
            return true;
        }
        return this.lastModifiedDateSet;
    }

    public void setAlignment(int alignment) {
        if ((alignment & alignment - 1) != 0 || alignment > 65535) {
            throw new IllegalArgumentException("Invalid value for alignment, must be power of two and no bigger than 65535 but is " + alignment);
        }
        this.alignment = alignment;
    }

    private void setAttributes(Path inputPath, LinkOption ... options) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(inputPath, BasicFileAttributes.class, options);
        if (attributes.isRegularFile()) {
            this.setSize(attributes.size());
        }
        super.setLastModifiedTime(attributes.lastModifiedTime());
        super.setCreationTime(attributes.creationTime());
        super.setLastAccessTime(attributes.lastAccessTime());
        this.setExtraTimeFields();
    }

    public void setCentralDirectoryExtra(byte[] b) {
        try {
            this.mergeExtraFields(this.parseExtraFields(b, false, ExtraFieldParsingMode.BEST_EFFORT), false);
        }
        catch (ZipException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public void setCommentSource(CommentSource commentSource) {
        this.commentSource = commentSource;
    }

    @Override
    public ZipEntry setCreationTime(FileTime time) {
        super.setCreationTime(time);
        this.setExtraTimeFields();
        return this;
    }

    protected void setDataOffset(long dataOffset) {
        this.dataOffset = dataOffset;
    }

    public void setDiskNumberStart(long diskNumberStart) {
        this.diskNumberStart = diskNumberStart;
    }

    public void setExternalAttributes(long value) {
        this.externalAttributes = value;
    }

    protected void setExtra() {
        super.setExtra(ExtraFieldUtils.mergeLocalFileDataData(this.getAllExtraFieldsNoCopy()));
        this.updateTimeFieldsFromExtraFields();
    }

    @Override
    public void setExtra(byte[] extra) throws RuntimeException {
        try {
            this.mergeExtraFields(this.parseExtraFields(extra, true, ExtraFieldParsingMode.BEST_EFFORT), true);
        }
        catch (ZipException e) {
            throw new IllegalArgumentException("Error parsing extra fields for entry: " + this.getName() + " - " + e.getMessage(), e);
        }
    }

    public void setExtraFields(ZipExtraField[] fields) {
        this.unparseableExtra = null;
        ArrayList<ZipExtraField> newFields = new ArrayList<ZipExtraField>();
        if (fields != null) {
            for (ZipExtraField field : fields) {
                if (field instanceof UnparseableExtraFieldData) {
                    this.unparseableExtra = (UnparseableExtraFieldData)field;
                    continue;
                }
                newFields.add(field);
            }
        }
        this.extraFields = newFields.toArray(ExtraFieldUtils.EMPTY_ZIP_EXTRA_FIELD_ARRAY);
        this.setExtra();
    }

    private void setExtraTimeFields() {
        if (this.getExtraField(X5455_ExtendedTimestamp.HEADER_ID) != null) {
            this.internalRemoveExtraField(X5455_ExtendedTimestamp.HEADER_ID);
        }
        if (this.getExtraField(X000A_NTFS.HEADER_ID) != null) {
            this.internalRemoveExtraField(X000A_NTFS.HEADER_ID);
        }
        if (this.requiresExtraTimeFields()) {
            FileTime creationTime;
            FileTime lastAccessTime;
            FileTime lastModifiedTime = this.getLastModifiedTime();
            if (ZipArchiveEntry.canConvertToInfoZipExtendedTimestamp(lastModifiedTime, lastAccessTime = this.getLastAccessTime(), creationTime = this.getCreationTime())) {
                this.addInfoZipExtendedTimestamp(lastModifiedTime, lastAccessTime, creationTime);
            }
            this.addNTFSTimestamp(lastModifiedTime, lastAccessTime, creationTime);
        }
        this.setExtra();
    }

    public void setGeneralPurposeBit(GeneralPurposeBit generalPurposeBit) {
        this.generalPurposeBit = generalPurposeBit;
    }

    public void setInternalAttributes(int internalAttributes) {
        this.internalAttributes = internalAttributes;
    }

    @Override
    public ZipEntry setLastAccessTime(FileTime fileTime) {
        super.setLastAccessTime(fileTime);
        this.setExtraTimeFields();
        return this;
    }

    @Override
    public ZipEntry setLastModifiedTime(FileTime fileTime) {
        this.internalSetLastModifiedTime(fileTime);
        this.setExtraTimeFields();
        return this;
    }

    protected void setLocalHeaderOffset(long localHeaderOffset) {
        this.localHeaderOffset = localHeaderOffset;
    }

    @Override
    public void setMethod(int method) {
        if (method < 0) {
            throw new IllegalArgumentException("ZIP compression method can not be negative: " + method);
        }
        this.method = method;
    }

    protected void setName(String name) {
        if (name != null && this.getPlatform() == 0 && !name.contains(ZIP_DIR_SEP)) {
            name = name.replace('\\', '/');
        }
        this.name = name;
    }

    protected void setName(String name, byte[] rawName) {
        this.setName(name);
        this.rawName = rawName;
    }

    public void setNameSource(NameSource nameSource) {
        this.nameSource = nameSource;
    }

    protected void setPlatform(int platform) {
        this.platform = platform;
    }

    public void setRawFlag(int rawFlag) {
        this.rawFlag = rawFlag;
    }

    @Override
    public void setSize(long size) {
        if (size < 0L) {
            throw new IllegalArgumentException("Invalid entry size");
        }
        this.size = size;
    }

    protected void setStreamContiguous(boolean isStreamContiguous) {
        this.isStreamContiguous = isStreamContiguous;
    }

    public void setTime(FileTime fileTime) {
        this.setTime(fileTime.toMillis());
    }

    @Override
    public void setTime(long timeEpochMillis) {
        if (ZipUtil.isDosTime(timeEpochMillis)) {
            super.setTime(timeEpochMillis);
            this.time = timeEpochMillis;
            this.lastModifiedDateSet = false;
            this.setExtraTimeFields();
        } else {
            this.setLastModifiedTime(FileTime.fromMillis(timeEpochMillis));
        }
    }

    public void setUnixMode(int mode) {
        this.setExternalAttributes(mode << 16 | ((mode & 0x80) == 0 ? 1 : 0) | (this.isDirectory() ? 16 : 0));
        this.platform = 3;
    }

    public void setVersionMadeBy(int versionMadeBy) {
        this.versionMadeBy = versionMadeBy;
    }

    public void setVersionRequired(int versionRequired) {
        this.versionRequired = versionRequired;
    }

    private void updateTimeFieldsFromExtraFields() {
        this.updateTimeFromExtendedTimestampField();
        this.updateTimeFromNtfsField();
    }

    private void updateTimeFromExtendedTimestampField() {
        ZipExtraField extraField = this.getExtraField(X5455_ExtendedTimestamp.HEADER_ID);
        if (extraField instanceof X5455_ExtendedTimestamp) {
            FileTime creationTime;
            FileTime accessTime;
            FileTime modifyTime;
            X5455_ExtendedTimestamp extendedTimestamp = (X5455_ExtendedTimestamp)extraField;
            if (extendedTimestamp.isBit0_modifyTimePresent() && (modifyTime = extendedTimestamp.getModifyFileTime()) != null) {
                this.internalSetLastModifiedTime(modifyTime);
            }
            if (extendedTimestamp.isBit1_accessTimePresent() && (accessTime = extendedTimestamp.getAccessFileTime()) != null) {
                super.setLastAccessTime(accessTime);
            }
            if (extendedTimestamp.isBit2_createTimePresent() && (creationTime = extendedTimestamp.getCreateFileTime()) != null) {
                super.setCreationTime(creationTime);
            }
        }
    }

    private void updateTimeFromNtfsField() {
        ZipExtraField extraField = this.getExtraField(X000A_NTFS.HEADER_ID);
        if (extraField instanceof X000A_NTFS) {
            FileTime creationTime;
            FileTime accessTime;
            X000A_NTFS ntfsTimestamp = (X000A_NTFS)extraField;
            FileTime modifyTime = ntfsTimestamp.getModifyFileTime();
            if (modifyTime != null) {
                this.internalSetLastModifiedTime(modifyTime);
            }
            if ((accessTime = ntfsTimestamp.getAccessFileTime()) != null) {
                super.setLastAccessTime(accessTime);
            }
            if ((creationTime = ntfsTimestamp.getCreateFileTime()) != null) {
                super.setCreationTime(creationTime);
            }
        }
    }

    public static enum NameSource {
        NAME,
        NAME_WITH_EFS_FLAG,
        UNICODE_EXTRA_FIELD;

    }

    public static enum CommentSource {
        COMMENT,
        UNICODE_EXTRA_FIELD;

    }

    public static enum ExtraFieldParsingMode implements ExtraFieldParsingBehavior
    {
        BEST_EFFORT(ExtraFieldUtils.UnparseableExtraField.READ){

            @Override
            public ZipExtraField fill(ZipExtraField field, byte[] data, int off, int len, boolean local) {
                return ExtraFieldParsingMode.fillAndMakeUnrecognizedOnError(field, data, off, len, local);
            }
        }
        ,
        STRICT_FOR_KNOW_EXTRA_FIELDS(ExtraFieldUtils.UnparseableExtraField.READ),
        ONLY_PARSEABLE_LENIENT(ExtraFieldUtils.UnparseableExtraField.SKIP){

            @Override
            public ZipExtraField fill(ZipExtraField field, byte[] data, int off, int len, boolean local) {
                return ExtraFieldParsingMode.fillAndMakeUnrecognizedOnError(field, data, off, len, local);
            }
        }
        ,
        ONLY_PARSEABLE_STRICT(ExtraFieldUtils.UnparseableExtraField.SKIP),
        DRACONIC(ExtraFieldUtils.UnparseableExtraField.THROW);

        private final ExtraFieldUtils.UnparseableExtraField onUnparseableData;

        private static ZipExtraField fillAndMakeUnrecognizedOnError(ZipExtraField field, byte[] data, int off, int len, boolean local) {
            try {
                return ExtraFieldUtils.fillExtraField(field, data, off, len, local);
            }
            catch (ZipException ex) {
                UnrecognizedExtraField u = new UnrecognizedExtraField();
                u.setHeaderId(field.getHeaderId());
                if (local) {
                    u.setLocalFileDataData(Arrays.copyOfRange(data, off, off + len));
                } else {
                    u.setCentralDirectoryData(Arrays.copyOfRange(data, off, off + len));
                }
                return u;
            }
        }

        private ExtraFieldParsingMode(ExtraFieldUtils.UnparseableExtraField onUnparseableData) {
            this.onUnparseableData = onUnparseableData;
        }

        @Override
        public ZipExtraField createExtraField(ZipShort headerId) {
            return ExtraFieldUtils.createExtraField(headerId);
        }

        @Override
        public ZipExtraField fill(ZipExtraField field, byte[] data, int off, int len, boolean local) throws ZipException {
            return ExtraFieldUtils.fillExtraField(field, data, off, len, local);
        }

        @Override
        public ZipExtraField onUnparseableExtraField(byte[] data, int off, int len, boolean local, int claimedLength) throws ZipException {
            return this.onUnparseableData.onUnparseableExtraField(data, off, len, local, claimedLength);
        }
    }
}

