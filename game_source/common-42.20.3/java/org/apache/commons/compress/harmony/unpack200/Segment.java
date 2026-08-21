/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.AttrDefinitionBands;
import org.apache.commons.compress.harmony.unpack200.AttributeLayout;
import org.apache.commons.compress.harmony.unpack200.BcBands;
import org.apache.commons.compress.harmony.unpack200.ClassBands;
import org.apache.commons.compress.harmony.unpack200.CpBands;
import org.apache.commons.compress.harmony.unpack200.FileBands;
import org.apache.commons.compress.harmony.unpack200.IcBands;
import org.apache.commons.compress.harmony.unpack200.IcTuple;
import org.apache.commons.compress.harmony.unpack200.Pack200UnpackerAdapter;
import org.apache.commons.compress.harmony.unpack200.SegmentConstantPool;
import org.apache.commons.compress.harmony.unpack200.SegmentHeader;
import org.apache.commons.compress.harmony.unpack200.SegmentOptions;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPField;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPMethod;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFile;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.InnerClassesAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.SourceFileAttribute;
import org.apache.commons.io.input.BoundedInputStream;

public class Segment {
    public static final int LOG_LEVEL_VERBOSE = 2;
    public static final int LOG_LEVEL_STANDARD = 1;
    public static final int LOG_LEVEL_QUIET = 0;
    private SegmentHeader header;
    private CpBands cpBands;
    private AttrDefinitionBands attrDefinitionBands;
    private IcBands icBands;
    private ClassBands classBands;
    private BcBands bcBands;
    private FileBands fileBands;
    private boolean overrideDeflateHint;
    private boolean deflateHint;
    private boolean doPreRead;
    private int logLevel;
    private PrintWriter logStream;
    private byte[][] classFilesContents;
    private boolean[] fileDeflate;
    private boolean[] fileIsClass;
    private InputStream internalBuffer;

    private ClassFile buildClassFile(int classNum) {
        ClassFile classFile = new ClassFile();
        int[] major = this.classBands.getClassVersionMajor();
        int[] minor = this.classBands.getClassVersionMinor();
        if (major != null) {
            classFile.major = major[classNum];
            classFile.minor = minor[classNum];
        } else {
            classFile.major = this.header.getDefaultClassMajorVersion();
            classFile.minor = this.header.getDefaultClassMinorVersion();
        }
        ClassConstantPool cp = classFile.pool;
        int fullNameIndexInCpClass = this.classBands.getClassThisInts()[classNum];
        String fullName = this.cpBands.getCpClass()[fullNameIndexInCpClass];
        int i = fullName.lastIndexOf("/") + 1;
        ArrayList<Attribute> classAttributes = this.classBands.getClassAttributes()[classNum];
        SourceFileAttribute sourceFileAttribute = null;
        for (Attribute classAttribute : classAttributes) {
            if (!classAttribute.isSourceFileAttribute()) continue;
            sourceFileAttribute = (SourceFileAttribute)classAttribute;
        }
        if (sourceFileAttribute == null) {
            AttributeLayout SOURCE_FILE = this.attrDefinitionBands.getAttributeDefinitionMap().getAttributeLayout("SourceFile", 0);
            if (SOURCE_FILE.matches(this.classBands.getRawClassFlags()[classNum])) {
                int firstDollar = -1;
                for (int index = 0; index < fullName.length(); ++index) {
                    if (fullName.charAt(index) > '$') continue;
                    firstDollar = index;
                }
                String fileName = firstDollar > -1 && i <= firstDollar ? fullName.substring(i, firstDollar) + ".java" : fullName.substring(i) + ".java";
                sourceFileAttribute = new SourceFileAttribute(this.cpBands.cpUTF8Value(fileName, false));
                classFile.attributes = new Attribute[]{(Attribute)cp.add(sourceFileAttribute)};
            } else {
                classFile.attributes = new Attribute[0];
            }
        } else {
            classFile.attributes = new Attribute[]{(Attribute)cp.add(sourceFileAttribute)};
        }
        ArrayList<Attribute> classAttributesWithoutSourceFileAttribute = new ArrayList<Attribute>(classAttributes.size());
        for (int index = 0; index < classAttributes.size(); ++index) {
            Attribute attrib = (Attribute)classAttributes.get(index);
            if (attrib.isSourceFileAttribute()) continue;
            classAttributesWithoutSourceFileAttribute.add(attrib);
        }
        Attribute[] originalAttributes = classFile.attributes;
        classFile.attributes = new Attribute[originalAttributes.length + classAttributesWithoutSourceFileAttribute.size()];
        System.arraycopy(originalAttributes, 0, classFile.attributes, 0, originalAttributes.length);
        for (int index = 0; index < classAttributesWithoutSourceFileAttribute.size(); ++index) {
            Attribute attrib = (Attribute)classAttributesWithoutSourceFileAttribute.get(index);
            cp.add(attrib);
            classFile.attributes[originalAttributes.length + index] = attrib;
        }
        ClassFileEntry cfThis = cp.add(this.cpBands.cpClassValue(fullNameIndexInCpClass));
        ClassFileEntry cfSuper = cp.add(this.cpBands.cpClassValue(this.classBands.getClassSuperInts()[classNum]));
        ClassFileEntry[] cfInterfaces = new ClassFileEntry[this.classBands.getClassInterfacesInts()[classNum].length];
        for (i = 0; i < cfInterfaces.length; ++i) {
            cfInterfaces[i] = cp.add(this.cpBands.cpClassValue(this.classBands.getClassInterfacesInts()[classNum][i]));
        }
        ClassFileEntry[] cfFields = new ClassFileEntry[this.classBands.getClassFieldCount()[classNum]];
        for (i = 0; i < cfFields.length; ++i) {
            int descriptorIndex = this.classBands.getFieldDescrInts()[classNum][i];
            int nameIndex = this.cpBands.getCpDescriptorNameInts()[descriptorIndex];
            int typeIndex = this.cpBands.getCpDescriptorTypeInts()[descriptorIndex];
            CPUTF8 name = this.cpBands.cpUTF8Value(nameIndex);
            CPUTF8 descriptor = this.cpBands.cpSignatureValue(typeIndex);
            cfFields[i] = cp.add(new CPField(name, descriptor, this.classBands.getFieldFlags()[classNum][i], this.classBands.getFieldAttributes()[classNum][i]));
        }
        ClassFileEntry[] cfMethods = new ClassFileEntry[this.classBands.getClassMethodCount()[classNum]];
        for (i = 0; i < cfMethods.length; ++i) {
            int descriptorIndex = this.classBands.getMethodDescrInts()[classNum][i];
            int nameIndex = this.cpBands.getCpDescriptorNameInts()[descriptorIndex];
            int typeIndex = this.cpBands.getCpDescriptorTypeInts()[descriptorIndex];
            CPUTF8 name = this.cpBands.cpUTF8Value(nameIndex);
            CPUTF8 descriptor = this.cpBands.cpSignatureValue(typeIndex);
            cfMethods[i] = cp.add(new CPMethod(name, descriptor, this.classBands.getMethodFlags()[classNum][i], this.classBands.getMethodAttributes()[classNum][i]));
        }
        cp.addNestedEntries();
        boolean addInnerClassesAttr = false;
        IcTuple[] icLocal = this.getClassBands().getIcLocal()[classNum];
        boolean icLocalSent = icLocal != null;
        InnerClassesAttribute innerClassesAttribute = new InnerClassesAttribute("InnerClasses");
        IcTuple[] icRelevant = this.getIcBands().getRelevantIcTuples(fullName, cp);
        List<IcTuple> ic_stored = this.computeIcStored(icLocal, icRelevant);
        for (IcTuple icStored : ic_stored) {
            CPClass innerClass;
            int innerClassIndex = icStored.thisClassIndex();
            int outerClassIndex = icStored.outerClassIndex();
            int simpleClassNameIndex = icStored.simpleClassNameIndex();
            String innerClassString = icStored.thisClassString();
            String outerClassString = icStored.outerClassString();
            String simpleClassName = icStored.simpleClassName();
            CPUTF8 innerName = null;
            CPClass outerClass = null;
            CPClass cPClass = innerClass = innerClassIndex != -1 ? this.cpBands.cpClassValue(innerClassIndex) : this.cpBands.cpClassValue(innerClassString);
            if (!icStored.isAnonymous()) {
                CPUTF8 cPUTF8 = innerName = simpleClassNameIndex != -1 ? this.cpBands.cpUTF8Value(simpleClassNameIndex) : this.cpBands.cpUTF8Value(simpleClassName);
            }
            if (icStored.isMember()) {
                outerClass = outerClassIndex != -1 ? this.cpBands.cpClassValue(outerClassIndex) : this.cpBands.cpClassValue(outerClassString);
            }
            int flags = icStored.F;
            innerClassesAttribute.addInnerClassesEntry(innerClass, outerClass, innerName, flags);
            addInnerClassesAttr = true;
        }
        if (icLocalSent && icLocal.length == 0) {
            addInnerClassesAttr = false;
        }
        if (!icLocalSent && icRelevant.length == 0) {
            addInnerClassesAttr = false;
        }
        if (addInnerClassesAttr) {
            Attribute[] originalAttrs = classFile.attributes;
            Attribute[] newAttrs = new Attribute[originalAttrs.length + 1];
            System.arraycopy(originalAttrs, 0, newAttrs, 0, originalAttrs.length);
            newAttrs[newAttrs.length - 1] = innerClassesAttribute;
            classFile.attributes = newAttrs;
            cp.addWithNestedEntries(innerClassesAttribute);
        }
        cp.resolve(this);
        classFile.accessFlags = (int)this.classBands.getClassFlags()[classNum];
        classFile.thisClass = cp.indexOf(cfThis);
        classFile.superClass = cp.indexOf(cfSuper);
        classFile.interfaces = new int[cfInterfaces.length];
        for (i = 0; i < cfInterfaces.length; ++i) {
            classFile.interfaces[i] = cp.indexOf(cfInterfaces[i]);
        }
        classFile.fields = cfFields;
        classFile.methods = cfMethods;
        return classFile;
    }

    private List<IcTuple> computeIcStored(IcTuple[] icLocal, IcTuple[] icRelevant) {
        ArrayList<IcTuple> result = new ArrayList<IcTuple>(icRelevant.length);
        ArrayList<IcTuple> duplicates = new ArrayList<IcTuple>(icRelevant.length);
        HashSet<IcTuple> isInResult = new HashSet<IcTuple>(icRelevant.length);
        if (icLocal != null) {
            for (IcTuple element : icLocal) {
                if (!isInResult.add(element)) continue;
                result.add(element);
            }
        }
        for (IcTuple element : icRelevant) {
            if (isInResult.add(element)) {
                result.add(element);
                continue;
            }
            duplicates.add(element);
        }
        duplicates.forEach(result::remove);
        return result;
    }

    protected AttrDefinitionBands getAttrDefinitionBands() {
        return this.attrDefinitionBands;
    }

    protected ClassBands getClassBands() {
        return this.classBands;
    }

    public SegmentConstantPool getConstantPool() {
        return this.cpBands.getConstantPool();
    }

    protected CpBands getCpBands() {
        return this.cpBands;
    }

    protected IcBands getIcBands() {
        return this.icBands;
    }

    public SegmentHeader getSegmentHeader() {
        return this.header;
    }

    public void log(int logLevel, String message) {
        if (this.logLevel >= logLevel) {
            this.logStream.println(message);
        }
    }

    public void overrideDeflateHint(boolean deflateHint) {
        this.overrideDeflateHint = true;
        this.deflateHint = deflateHint;
    }

    private void parseSegment() throws IOException, Pack200Exception {
        this.header.unpack();
        this.cpBands.unpack();
        this.attrDefinitionBands.unpack();
        this.icBands.unpack();
        this.classBands.unpack();
        this.bcBands.unpack();
        this.fileBands.unpack();
        int classNum = 0;
        int numberOfFiles = this.header.getNumberOfFiles();
        String[] fileName = this.fileBands.getFileName();
        int[] fileOptions = this.fileBands.getFileOptions();
        SegmentOptions options = this.header.getOptions();
        this.classFilesContents = new byte[numberOfFiles][];
        this.fileDeflate = new boolean[numberOfFiles];
        this.fileIsClass = new boolean[numberOfFiles];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        for (int i = 0; i < numberOfFiles; ++i) {
            boolean isClass;
            String name = fileName[i];
            boolean nameIsEmpty = name == null || name.isEmpty();
            boolean bl = isClass = (fileOptions[i] & 2) == 2 || nameIsEmpty;
            if (isClass && nameIsEmpty) {
                fileName[i] = name = this.cpBands.getCpClass()[this.classBands.getClassThisInts()[classNum]] + ".class";
            }
            this.fileDeflate[i] = !this.overrideDeflateHint ? (fileOptions[i] & 1) == 1 || options.shouldDeflate() : this.deflateHint;
            this.fileIsClass[i] = isClass;
            if (!isClass) continue;
            ClassFile classFile = this.buildClassFile(classNum);
            classFile.write(dos);
            dos.flush();
            this.classFilesContents[classNum] = bos.toByteArray();
            bos.reset();
            ++classNum;
        }
    }

    private void readSegment(InputStream in) throws IOException, Pack200Exception {
        this.log(2, "-------");
        this.cpBands = new CpBands(this);
        this.cpBands.read(in);
        this.attrDefinitionBands = new AttrDefinitionBands(this);
        this.attrDefinitionBands.read(in);
        this.icBands = new IcBands(this);
        this.icBands.read(in);
        this.classBands = new ClassBands(this);
        this.classBands.read(in);
        this.bcBands = new BcBands(this);
        this.bcBands.read(in);
        this.fileBands = new FileBands(this);
        this.fileBands.read(in);
        this.fileBands.processFileBits();
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public void setLogStream(OutputStream logStream) {
        this.logStream = new PrintWriter((Writer)new OutputStreamWriter(logStream, Charset.defaultCharset()), false);
    }

    public void setPreRead(boolean value) {
        this.doPreRead = value;
    }

    public void unpack(InputStream inputStream2, JarOutputStream out) throws IOException, Pack200Exception {
        this.unpackRead(inputStream2);
        this.unpackProcess();
        this.unpackWrite(out);
    }

    void unpackProcess() throws IOException, Pack200Exception {
        if (this.internalBuffer != null) {
            this.readSegment(this.internalBuffer);
        }
        this.parseSegment();
    }

    void unpackRead(InputStream inputStream2) throws IOException, Pack200Exception {
        BoundedInputStream in = Pack200UnpackerAdapter.newBoundedInputStream(inputStream2);
        this.header = new SegmentHeader(this);
        this.header.read(in);
        int size = (int)this.header.getArchiveSize() - this.header.getArchiveSizeOffset();
        if (this.doPreRead && this.header.getArchiveSize() != 0L) {
            byte[] data = new byte[size];
            ((InputStream)in).read(data);
            this.internalBuffer = new BufferedInputStream(new ByteArrayInputStream(data));
        } else {
            this.readSegment(in);
        }
    }

    void unpackWrite(JarOutputStream out) throws IOException {
        this.writeJar(out);
        if (this.logStream != null) {
            this.logStream.close();
        }
    }

    public void writeJar(JarOutputStream out) throws IOException {
        String[] fileName = this.fileBands.getFileName();
        int[] fileModtime = this.fileBands.getFileModtime();
        long[] fileSize = this.fileBands.getFileSize();
        byte[][] fileBits = this.fileBands.getFileBits();
        int classNum = 0;
        int numberOfFiles = this.header.getNumberOfFiles();
        long archiveModtime = this.header.getArchiveModtime();
        for (int i = 0; i < numberOfFiles; ++i) {
            String name = fileName[i];
            long modtime = 1000L * (archiveModtime + (long)fileModtime[i]);
            boolean deflate = this.fileDeflate[i];
            JarEntry entry = new JarEntry(name);
            if (deflate) {
                entry.setMethod(8);
            } else {
                entry.setMethod(0);
                CRC32 crc = new CRC32();
                if (this.fileIsClass[i]) {
                    crc.update(this.classFilesContents[classNum]);
                    entry.setSize(this.classFilesContents[classNum].length);
                } else {
                    crc.update(fileBits[i]);
                    entry.setSize(fileSize[i]);
                }
                entry.setCrc(crc.getValue());
            }
            entry.setTime(modtime - (long)TimeZone.getDefault().getRawOffset());
            out.putNextEntry(entry);
            if (this.fileIsClass[i]) {
                entry.setSize(this.classFilesContents[classNum].length);
                out.write(this.classFilesContents[classNum]);
                ++classNum;
                continue;
            }
            entry.setSize(fileSize[i]);
            out.write(fileBits[i]);
        }
    }
}

