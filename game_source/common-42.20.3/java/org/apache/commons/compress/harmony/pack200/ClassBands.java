/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.Label
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.harmony.pack200.AttributeDefinitionBands;
import org.apache.commons.compress.harmony.pack200.BandSet;
import org.apache.commons.compress.harmony.pack200.CPClass;
import org.apache.commons.compress.harmony.pack200.CPConstant;
import org.apache.commons.compress.harmony.pack200.CPNameAndType;
import org.apache.commons.compress.harmony.pack200.CPSignature;
import org.apache.commons.compress.harmony.pack200.CPUTF8;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.ConstantPoolEntry;
import org.apache.commons.compress.harmony.pack200.CpBands;
import org.apache.commons.compress.harmony.pack200.IcBands;
import org.apache.commons.compress.harmony.pack200.IntList;
import org.apache.commons.compress.harmony.pack200.MetadataBandGroup;
import org.apache.commons.compress.harmony.pack200.NewAttribute;
import org.apache.commons.compress.harmony.pack200.NewAttributeBands;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingUtils;
import org.apache.commons.compress.harmony.pack200.Segment;
import org.objectweb.asm.Label;

public class ClassBands
extends BandSet {
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private final CpBands cpBands;
    private final AttributeDefinitionBands attrBands;
    private final CPClass[] class_this;
    private final CPClass[] class_super;
    private final CPClass[][] class_interface;
    private final int[] class_interface_count;
    private final int[] major_versions;
    private final long[] class_flags;
    private int[] class_attr_calls;
    private final List<CPUTF8> classSourceFile = new ArrayList<CPUTF8>();
    private final List<ConstantPoolEntry> classEnclosingMethodClass = new ArrayList<ConstantPoolEntry>();
    private final List<ConstantPoolEntry> classEnclosingMethodDesc = new ArrayList<ConstantPoolEntry>();
    private final List<CPSignature> classSignature = new ArrayList<CPSignature>();
    private final IntList classFileVersionMinor = new IntList();
    private final IntList classFileVersionMajor = new IntList();
    private final int[] class_field_count;
    private final CPNameAndType[][] field_descr;
    private final long[][] field_flags;
    private int[] field_attr_calls;
    private final List<CPConstant<?>> fieldConstantValueKQ = new ArrayList();
    private final List<CPSignature> fieldSignature = new ArrayList<CPSignature>();
    private final int[] class_method_count;
    private final CPNameAndType[][] method_descr;
    private final long[][] method_flags;
    private int[] method_attr_calls;
    private final List<CPSignature> methodSignature = new ArrayList<CPSignature>();
    private final IntList methodExceptionNumber = new IntList();
    private final List<CPClass> methodExceptionClasses = new ArrayList<CPClass>();
    private int[] codeHeaders;
    private final IntList codeMaxStack = new IntList();
    private final IntList codeMaxLocals = new IntList();
    private final IntList codeHandlerCount = new IntList();
    private final List codeHandlerStartP = new ArrayList();
    private final List codeHandlerEndPO = new ArrayList();
    private final List codeHandlerCatchPO = new ArrayList();
    private final List<CPClass> codeHandlerClass = new ArrayList<CPClass>();
    private final List<Long> codeFlags = new ArrayList<Long>();
    private int[] code_attr_calls;
    private final IntList codeLineNumberTableN = new IntList();
    private final List codeLineNumberTableBciP = new ArrayList();
    private final IntList codeLineNumberTableLine = new IntList();
    private final IntList codeLocalVariableTableN = new IntList();
    private final List codeLocalVariableTableBciP = new ArrayList();
    private final List codeLocalVariableTableSpanO = new ArrayList();
    private final List<ConstantPoolEntry> codeLocalVariableTableNameRU = new ArrayList<ConstantPoolEntry>();
    private final List<ConstantPoolEntry> codeLocalVariableTableTypeRS = new ArrayList<ConstantPoolEntry>();
    private final IntList codeLocalVariableTableSlot = new IntList();
    private final IntList codeLocalVariableTypeTableN = new IntList();
    private final List codeLocalVariableTypeTableBciP = new ArrayList();
    private final List codeLocalVariableTypeTableSpanO = new ArrayList();
    private final List<ConstantPoolEntry> codeLocalVariableTypeTableNameRU = new ArrayList<ConstantPoolEntry>();
    private final List<ConstantPoolEntry> codeLocalVariableTypeTableTypeRS = new ArrayList<ConstantPoolEntry>();
    private final IntList codeLocalVariableTypeTableSlot = new IntList();
    private final MetadataBandGroup class_RVA_bands;
    private final MetadataBandGroup class_RIA_bands;
    private final MetadataBandGroup field_RVA_bands;
    private final MetadataBandGroup field_RIA_bands;
    private final MetadataBandGroup method_RVA_bands;
    private final MetadataBandGroup method_RIA_bands;
    private final MetadataBandGroup method_RVPA_bands;
    private final MetadataBandGroup method_RIPA_bands;
    private final MetadataBandGroup method_AD_bands;
    private final List<NewAttributeBands> classAttributeBands = new ArrayList<NewAttributeBands>();
    private final List<NewAttributeBands> methodAttributeBands = new ArrayList<NewAttributeBands>();
    private final List<NewAttributeBands> fieldAttributeBands = new ArrayList<NewAttributeBands>();
    private final List<NewAttributeBands> codeAttributeBands = new ArrayList<NewAttributeBands>();
    private final List<Long> tempFieldFlags = new ArrayList<Long>();
    private final List<CPNameAndType> tempFieldDesc = new ArrayList<CPNameAndType>();
    private final List<Long> tempMethodFlags = new ArrayList<Long>();
    private final List<CPNameAndType> tempMethodDesc = new ArrayList<CPNameAndType>();
    private TempParamAnnotation tempMethodRVPA;
    private TempParamAnnotation tempMethodRIPA;
    private boolean anySyntheticClasses;
    private boolean anySyntheticFields;
    private boolean anySyntheticMethods;
    private final Segment segment;
    private final Map<CPClass, Set<CPClass>> classReferencesInnerClass = new HashMap<CPClass, Set<CPClass>>();
    private final boolean stripDebug;
    private int index;
    private int numMethodArgs;
    private int[] class_InnerClasses_N;
    private CPClass[] class_InnerClasses_RC;
    private int[] class_InnerClasses_F;
    private List<CPClass> classInnerClassesOuterRCN;
    private List<CPUTF8> classInnerClassesNameRUN;

    protected static int countArgs(String descriptor) {
        int bra = descriptor.indexOf(40);
        int ket = descriptor.indexOf(41);
        if (bra == -1 || ket == -1 || ket < bra) {
            throw new IllegalArgumentException("No arguments");
        }
        boolean inType = false;
        boolean consumingNextType = false;
        int count = 0;
        for (int i = bra + 1; i < ket; ++i) {
            char charAt = descriptor.charAt(i);
            if (inType && charAt == ';') {
                inType = false;
                consumingNextType = false;
                continue;
            }
            if (!inType && charAt == 'L') {
                inType = true;
                ++count;
                continue;
            }
            if (charAt == '[') {
                consumingNextType = true;
                continue;
            }
            if (inType) continue;
            if (consumingNextType) {
                ++count;
                consumingNextType = false;
                continue;
            }
            if (charAt == 'D' || charAt == 'J') {
                count += 2;
                continue;
            }
            ++count;
        }
        return count;
    }

    public ClassBands(Segment segment, int numClasses, int effort, boolean stripDebug) throws IOException {
        super(effort, segment.getSegmentHeader());
        this.stripDebug = stripDebug;
        this.segment = segment;
        this.cpBands = segment.getCpBands();
        this.attrBands = segment.getAttrBands();
        this.class_this = new CPClass[numClasses];
        this.class_super = new CPClass[numClasses];
        this.class_interface_count = new int[numClasses];
        this.class_interface = new CPClass[numClasses][];
        this.class_field_count = new int[numClasses];
        this.class_method_count = new int[numClasses];
        this.field_descr = new CPNameAndType[numClasses][];
        this.field_flags = new long[numClasses][];
        this.method_descr = new CPNameAndType[numClasses][];
        this.method_flags = new long[numClasses][];
        for (int i = 0; i < numClasses; ++i) {
            this.field_flags[i] = EMPTY_LONG_ARRAY;
            this.method_flags[i] = EMPTY_LONG_ARRAY;
        }
        this.major_versions = new int[numClasses];
        this.class_flags = new long[numClasses];
        this.class_RVA_bands = new MetadataBandGroup("RVA", 0, this.cpBands, this.segmentHeader, effort);
        this.class_RIA_bands = new MetadataBandGroup("RIA", 0, this.cpBands, this.segmentHeader, effort);
        this.field_RVA_bands = new MetadataBandGroup("RVA", 1, this.cpBands, this.segmentHeader, effort);
        this.field_RIA_bands = new MetadataBandGroup("RIA", 1, this.cpBands, this.segmentHeader, effort);
        this.method_RVA_bands = new MetadataBandGroup("RVA", 2, this.cpBands, this.segmentHeader, effort);
        this.method_RIA_bands = new MetadataBandGroup("RIA", 2, this.cpBands, this.segmentHeader, effort);
        this.method_RVPA_bands = new MetadataBandGroup("RVPA", 2, this.cpBands, this.segmentHeader, effort);
        this.method_RIPA_bands = new MetadataBandGroup("RIPA", 2, this.cpBands, this.segmentHeader, effort);
        this.method_AD_bands = new MetadataBandGroup("AD", 2, this.cpBands, this.segmentHeader, effort);
        this.createNewAttributeBands();
    }

    public void addAnnotation(int context, String desc, boolean visible, List<String> nameRU, List<String> tags, List<Object> values2, List<Integer> caseArrayN, List<String> nestTypeRS, List<String> nestNameRU, List<Integer> nestPairN) {
        switch (context) {
            case 0: {
                if (visible) {
                    this.class_RVA_bands.addAnnotation(desc, nameRU, tags, values2, caseArrayN, nestTypeRS, nestNameRU, nestPairN);
                    if ((this.class_flags[this.index] & 0x200000L) != 0L) {
                        this.class_RVA_bands.incrementAnnoN();
                        break;
                    }
                    this.class_RVA_bands.newEntryInAnnoN();
                    this.class_flags[this.index] = this.class_flags[this.index] | 0x200000L;
                    break;
                }
                this.class_RIA_bands.addAnnotation(desc, nameRU, tags, values2, caseArrayN, nestTypeRS, nestNameRU, nestPairN);
                if ((this.class_flags[this.index] & 0x400000L) != 0L) {
                    this.class_RIA_bands.incrementAnnoN();
                    break;
                }
                this.class_RIA_bands.newEntryInAnnoN();
                this.class_flags[this.index] = this.class_flags[this.index] | 0x400000L;
                break;
            }
            case 1: {
                if (visible) {
                    this.field_RVA_bands.addAnnotation(desc, nameRU, tags, values2, caseArrayN, nestTypeRS, nestNameRU, nestPairN);
                    Long flag = this.tempFieldFlags.remove(this.tempFieldFlags.size() - 1);
                    if ((flag.intValue() & 0x200000) != 0) {
                        this.field_RVA_bands.incrementAnnoN();
                    } else {
                        this.field_RVA_bands.newEntryInAnnoN();
                    }
                    this.tempFieldFlags.add(Long.valueOf(flag.intValue() | 0x200000));
                    break;
                }
                this.field_RIA_bands.addAnnotation(desc, nameRU, tags, values2, caseArrayN, nestTypeRS, nestNameRU, nestPairN);
                Long flag = this.tempFieldFlags.remove(this.tempFieldFlags.size() - 1);
                if ((flag.intValue() & 0x400000) != 0) {
                    this.field_RIA_bands.incrementAnnoN();
                } else {
                    this.field_RIA_bands.newEntryInAnnoN();
                }
                this.tempFieldFlags.add(Long.valueOf(flag.intValue() | 0x400000));
                break;
            }
            case 2: {
                if (visible) {
                    this.method_RVA_bands.addAnnotation(desc, nameRU, tags, values2, caseArrayN, nestTypeRS, nestNameRU, nestPairN);
                    Long flag = this.tempMethodFlags.remove(this.tempMethodFlags.size() - 1);
                    if ((flag.intValue() & 0x200000) != 0) {
                        this.method_RVA_bands.incrementAnnoN();
                    } else {
                        this.method_RVA_bands.newEntryInAnnoN();
                    }
                    this.tempMethodFlags.add(Long.valueOf(flag.intValue() | 0x200000));
                    break;
                }
                this.method_RIA_bands.addAnnotation(desc, nameRU, tags, values2, caseArrayN, nestTypeRS, nestNameRU, nestPairN);
                Long flag = this.tempMethodFlags.remove(this.tempMethodFlags.size() - 1);
                if ((flag.intValue() & 0x400000) != 0) {
                    this.method_RIA_bands.incrementAnnoN();
                } else {
                    this.method_RIA_bands.newEntryInAnnoN();
                }
                this.tempMethodFlags.add(Long.valueOf(flag.intValue() | 0x400000));
            }
        }
    }

    public void addAnnotationDefault(List<String> nameRU, List<String> tags, List<Object> values2, List<Integer> caseArrayN, List<String> nestTypeRS, List<String> nestNameRU, List<Integer> nestPairN) {
        this.method_AD_bands.addAnnotation(null, nameRU, tags, values2, caseArrayN, nestTypeRS, nestNameRU, nestPairN);
        Long flag = this.tempMethodFlags.remove(this.tempMethodFlags.size() - 1);
        this.tempMethodFlags.add(flag | 0x2000000L);
    }

    public void addClass(int major, int flags, String className, String signature, String superName, String[] interfaces) {
        this.class_this[this.index] = this.cpBands.getCPClass(className);
        this.class_super[this.index] = this.cpBands.getCPClass(superName);
        this.class_interface_count[this.index] = interfaces.length;
        this.class_interface[this.index] = new CPClass[interfaces.length];
        Arrays.setAll(this.class_interface[this.index], i -> this.cpBands.getCPClass(interfaces[i]));
        this.major_versions[this.index] = major;
        this.class_flags[this.index] = flags;
        if (!this.anySyntheticClasses && (flags & 0x1000) != 0 && this.segment.getCurrentClassReader().hasSyntheticAttributes()) {
            this.cpBands.addCPUtf8("Synthetic");
            this.anySyntheticClasses = true;
        }
        if (signature != null) {
            int n = this.index;
            this.class_flags[n] = this.class_flags[n] | 0x80000L;
            this.classSignature.add(this.cpBands.getCPSignature(signature));
        }
    }

    public void addClassAttribute(NewAttribute attribute) {
        String attributeName = attribute.type;
        for (NewAttributeBands bands : this.classAttributeBands) {
            if (!bands.getAttributeName().equals(attributeName)) continue;
            bands.addAttribute(attribute);
            int flagIndex = bands.getFlagIndex();
            int n = this.index;
            this.class_flags[n] = this.class_flags[n] | (long)(1 << flagIndex);
            return;
        }
        throw new IllegalArgumentException("No suitable definition for " + attributeName);
    }

    public void addCode() {
        this.codeHandlerCount.add(0);
        if (!this.stripDebug) {
            this.codeFlags.add(4L);
            this.codeLocalVariableTableN.add(0);
        }
    }

    public void addCodeAttribute(NewAttribute attribute) {
        String attributeName = attribute.type;
        for (NewAttributeBands bands : this.codeAttributeBands) {
            if (!bands.getAttributeName().equals(attributeName)) continue;
            bands.addAttribute(attribute);
            int flagIndex = bands.getFlagIndex();
            Long flags = this.codeFlags.remove(this.codeFlags.size() - 1);
            this.codeFlags.add(flags | (long)(1 << flagIndex));
            return;
        }
        throw new IllegalArgumentException("No suitable definition for " + attributeName);
    }

    public void addEnclosingMethod(String owner, String name, String desc) {
        int n = this.index;
        this.class_flags[n] = this.class_flags[n] | 0x40000L;
        this.classEnclosingMethodClass.add(this.cpBands.getCPClass(owner));
        this.classEnclosingMethodDesc.add(name == null ? null : this.cpBands.getCPNameAndType(name, desc));
    }

    public void addField(int flags, String name, String desc, String signature, Object value) {
        flags &= 0xFFFF;
        this.tempFieldDesc.add(this.cpBands.getCPNameAndType(name, desc));
        if (signature != null) {
            this.fieldSignature.add(this.cpBands.getCPSignature(signature));
            flags |= 0x80000;
        }
        if ((flags & 0x20000) != 0) {
            flags &= 0xFFFDFFFF;
            flags |= 0x100000;
        }
        if (value != null) {
            this.fieldConstantValueKQ.add(this.cpBands.getConstant(value));
            flags |= 0x20000;
        }
        if (!this.anySyntheticFields && (flags & 0x1000) != 0 && this.segment.getCurrentClassReader().hasSyntheticAttributes()) {
            this.cpBands.addCPUtf8("Synthetic");
            this.anySyntheticFields = true;
        }
        this.tempFieldFlags.add(Long.valueOf(flags));
    }

    public void addFieldAttribute(NewAttribute attribute) {
        String attributeName = attribute.type;
        for (NewAttributeBands bands : this.fieldAttributeBands) {
            if (!bands.getAttributeName().equals(attributeName)) continue;
            bands.addAttribute(attribute);
            int flagIndex = bands.getFlagIndex();
            Long flags = this.tempFieldFlags.remove(this.tempFieldFlags.size() - 1);
            this.tempFieldFlags.add(flags | (long)(1 << flagIndex));
            return;
        }
        throw new IllegalArgumentException("No suitable definition for " + attributeName);
    }

    public void addHandler(Label start, Label end, Label handler, String type) {
        int handlers = this.codeHandlerCount.remove(this.codeHandlerCount.size() - 1);
        this.codeHandlerCount.add(handlers + 1);
        this.codeHandlerStartP.add(start);
        this.codeHandlerEndPO.add(end);
        this.codeHandlerCatchPO.add(handler);
        this.codeHandlerClass.add(type == null ? null : this.cpBands.getCPClass(type));
    }

    public void addLineNumber(int line, Label start) {
        Long latestCodeFlag = this.codeFlags.get(this.codeFlags.size() - 1);
        if ((latestCodeFlag.intValue() & 2) == 0) {
            this.codeFlags.remove(this.codeFlags.size() - 1);
            this.codeFlags.add(Long.valueOf(latestCodeFlag.intValue() | 2));
            this.codeLineNumberTableN.add(1);
        } else {
            this.codeLineNumberTableN.increment(this.codeLineNumberTableN.size() - 1);
        }
        this.codeLineNumberTableLine.add(line);
        this.codeLineNumberTableBciP.add(start);
    }

    public void addLocalVariable(String name, String desc, String signature, Label start, Label end, int indx) {
        if (signature != null) {
            Long latestCodeFlag = this.codeFlags.get(this.codeFlags.size() - 1);
            if ((latestCodeFlag.intValue() & 8) == 0) {
                this.codeFlags.remove(this.codeFlags.size() - 1);
                this.codeFlags.add(Long.valueOf(latestCodeFlag.intValue() | 8));
                this.codeLocalVariableTypeTableN.add(1);
            } else {
                this.codeLocalVariableTypeTableN.increment(this.codeLocalVariableTypeTableN.size() - 1);
            }
            this.codeLocalVariableTypeTableBciP.add(start);
            this.codeLocalVariableTypeTableSpanO.add(end);
            this.codeLocalVariableTypeTableNameRU.add(this.cpBands.getCPUtf8(name));
            this.codeLocalVariableTypeTableTypeRS.add(this.cpBands.getCPSignature(signature));
            this.codeLocalVariableTypeTableSlot.add(indx);
        }
        this.codeLocalVariableTableN.increment(this.codeLocalVariableTableN.size() - 1);
        this.codeLocalVariableTableBciP.add(start);
        this.codeLocalVariableTableSpanO.add(end);
        this.codeLocalVariableTableNameRU.add(this.cpBands.getCPUtf8(name));
        this.codeLocalVariableTableTypeRS.add(this.cpBands.getCPSignature(desc));
        this.codeLocalVariableTableSlot.add(indx);
    }

    public void addMaxStack(int maxStack, int maxLocals) {
        Long latestFlag = this.tempMethodFlags.remove(this.tempMethodFlags.size() - 1);
        Long newFlag = latestFlag.intValue() | 0x20000;
        this.tempMethodFlags.add(newFlag);
        this.codeMaxStack.add(maxStack);
        if ((newFlag & 8L) == 0L) {
            --maxLocals;
        }
        this.codeMaxLocals.add(maxLocals -= this.numMethodArgs);
    }

    public void addMethod(int flags, String name, String desc, String signature, String[] exceptions) {
        CPNameAndType nt = this.cpBands.getCPNameAndType(name, desc);
        this.tempMethodDesc.add(nt);
        if (signature != null) {
            this.methodSignature.add(this.cpBands.getCPSignature(signature));
            flags |= 0x80000;
        }
        if (exceptions != null) {
            this.methodExceptionNumber.add(exceptions.length);
            for (String exception : exceptions) {
                this.methodExceptionClasses.add(this.cpBands.getCPClass(exception));
            }
            flags |= 0x40000;
        }
        if ((flags & 0x20000) != 0) {
            flags &= 0xFFFDFFFF;
            flags |= 0x100000;
        }
        this.tempMethodFlags.add(Long.valueOf(flags));
        this.numMethodArgs = ClassBands.countArgs(desc);
        if (!this.anySyntheticMethods && (flags & 0x1000) != 0 && this.segment.getCurrentClassReader().hasSyntheticAttributes()) {
            this.cpBands.addCPUtf8("Synthetic");
            this.anySyntheticMethods = true;
        }
    }

    public void addMethodAttribute(NewAttribute attribute) {
        String attributeName = attribute.type;
        for (NewAttributeBands bands : this.methodAttributeBands) {
            if (!bands.getAttributeName().equals(attributeName)) continue;
            bands.addAttribute(attribute);
            int flagIndex = bands.getFlagIndex();
            Long flags = this.tempMethodFlags.remove(this.tempMethodFlags.size() - 1);
            this.tempMethodFlags.add(flags | (long)(1 << flagIndex));
            return;
        }
        throw new IllegalArgumentException("No suitable definition for " + attributeName);
    }

    public void addParameterAnnotation(int parameter, String desc, boolean visible, List<String> nameRU, List<String> tags, List<Object> values2, List<Integer> caseArrayN, List<String> nestTypeRS, List<String> nestNameRU, List<Integer> nestPairN) {
        if (visible) {
            if (this.tempMethodRVPA == null) {
                this.tempMethodRVPA = new TempParamAnnotation(this.numMethodArgs);
                this.tempMethodRVPA.addParameterAnnotation(parameter, desc, nameRU, tags, values2, caseArrayN, nestTypeRS, nestNameRU, nestPairN);
            }
            Long flag = this.tempMethodFlags.remove(this.tempMethodFlags.size() - 1);
            this.tempMethodFlags.add(flag | 0x800000L);
        } else {
            if (this.tempMethodRIPA == null) {
                this.tempMethodRIPA = new TempParamAnnotation(this.numMethodArgs);
                this.tempMethodRIPA.addParameterAnnotation(parameter, desc, nameRU, tags, values2, caseArrayN, nestTypeRS, nestNameRU, nestPairN);
            }
            Long flag = this.tempMethodFlags.remove(this.tempMethodFlags.size() - 1);
            this.tempMethodFlags.add(flag | 0x1000000L);
        }
    }

    public void addSourceFile(String source2) {
        String implicitSourceFileName = this.class_this[this.index].toString();
        if (implicitSourceFileName.indexOf(36) != -1) {
            implicitSourceFileName = implicitSourceFileName.substring(0, implicitSourceFileName.indexOf(36));
        }
        if (source2.equals(implicitSourceFileName = implicitSourceFileName.substring(implicitSourceFileName.lastIndexOf(47) + 1) + ".java")) {
            this.classSourceFile.add(null);
        } else {
            this.classSourceFile.add(this.cpBands.getCPUtf8(source2));
        }
        int n = this.index;
        this.class_flags[n] = this.class_flags[n] | 0x20000L;
    }

    private void createNewAttributeBands() throws IOException {
        for (AttributeDefinitionBands.AttributeDefinition def : this.attrBands.getClassAttributeLayouts()) {
            this.classAttributeBands.add(new NewAttributeBands(this.effort, this.cpBands, this.segment.getSegmentHeader(), def));
        }
        for (AttributeDefinitionBands.AttributeDefinition def : this.attrBands.getMethodAttributeLayouts()) {
            this.methodAttributeBands.add(new NewAttributeBands(this.effort, this.cpBands, this.segment.getSegmentHeader(), def));
        }
        for (AttributeDefinitionBands.AttributeDefinition def : this.attrBands.getFieldAttributeLayouts()) {
            this.fieldAttributeBands.add(new NewAttributeBands(this.effort, this.cpBands, this.segment.getSegmentHeader(), def));
        }
        for (AttributeDefinitionBands.AttributeDefinition def : this.attrBands.getCodeAttributeLayouts()) {
            this.codeAttributeBands.add(new NewAttributeBands(this.effort, this.cpBands, this.segment.getSegmentHeader(), def));
        }
    }

    public void currentClassReferencesInnerClass(CPClass inner) {
        CPClass currentClass;
        if (this.index < this.class_this.length && (currentClass = this.class_this[this.index]) != null && !currentClass.equals(inner) && !this.isInnerClassOf(currentClass.toString(), inner)) {
            this.classReferencesInnerClass.computeIfAbsent(currentClass, c -> new HashSet()).add(inner);
        }
    }

    public void doBciRenumbering(IntList bciRenumbering, Map<Label, Integer> labelsToOffsets) {
        this.renumberBci(this.codeLineNumberTableBciP, bciRenumbering, labelsToOffsets);
        this.renumberBci(this.codeLocalVariableTableBciP, bciRenumbering, labelsToOffsets);
        this.renumberOffsetBci(this.codeLocalVariableTableBciP, this.codeLocalVariableTableSpanO, bciRenumbering, labelsToOffsets);
        this.renumberBci(this.codeLocalVariableTypeTableBciP, bciRenumbering, labelsToOffsets);
        this.renumberOffsetBci(this.codeLocalVariableTypeTableBciP, this.codeLocalVariableTypeTableSpanO, bciRenumbering, labelsToOffsets);
        this.renumberBci(this.codeHandlerStartP, bciRenumbering, labelsToOffsets);
        this.renumberOffsetBci(this.codeHandlerStartP, this.codeHandlerEndPO, bciRenumbering, labelsToOffsets);
        this.renumberDoubleOffsetBci(this.codeHandlerStartP, this.codeHandlerEndPO, this.codeHandlerCatchPO, bciRenumbering, labelsToOffsets);
        for (NewAttributeBands newAttributeBandSet : this.classAttributeBands) {
            newAttributeBandSet.renumberBci(bciRenumbering, labelsToOffsets);
        }
        for (NewAttributeBands newAttributeBandSet : this.methodAttributeBands) {
            newAttributeBandSet.renumberBci(bciRenumbering, labelsToOffsets);
        }
        for (NewAttributeBands newAttributeBandSet : this.fieldAttributeBands) {
            newAttributeBandSet.renumberBci(bciRenumbering, labelsToOffsets);
        }
        for (NewAttributeBands newAttributeBandSet : this.codeAttributeBands) {
            newAttributeBandSet.renumberBci(bciRenumbering, labelsToOffsets);
        }
    }

    public void endOfClass() {
        int numMethods;
        int numFields;
        this.class_field_count[this.index] = numFields = this.tempFieldDesc.size();
        this.field_descr[this.index] = new CPNameAndType[numFields];
        this.field_flags[this.index] = new long[numFields];
        for (int i = 0; i < numFields; ++i) {
            this.field_descr[this.index][i] = this.tempFieldDesc.get(i);
            this.field_flags[this.index][i] = this.tempFieldFlags.get(i);
        }
        this.class_method_count[this.index] = numMethods = this.tempMethodDesc.size();
        this.method_descr[this.index] = new CPNameAndType[numMethods];
        this.method_flags[this.index] = new long[numMethods];
        for (int i = 0; i < numMethods; ++i) {
            this.method_descr[this.index][i] = this.tempMethodDesc.get(i);
            this.method_flags[this.index][i] = this.tempMethodFlags.get(i);
        }
        this.tempFieldDesc.clear();
        this.tempFieldFlags.clear();
        this.tempMethodDesc.clear();
        this.tempMethodFlags.clear();
        ++this.index;
    }

    public void endOfMethod() {
        if (this.tempMethodRVPA != null) {
            this.method_RVPA_bands.addParameterAnnotation(this.tempMethodRVPA.numParams, this.tempMethodRVPA.annoN, this.tempMethodRVPA.pairN, this.tempMethodRVPA.typeRS, this.tempMethodRVPA.nameRU, this.tempMethodRVPA.tags, this.tempMethodRVPA.values, this.tempMethodRVPA.caseArrayN, this.tempMethodRVPA.nestTypeRS, this.tempMethodRVPA.nestNameRU, this.tempMethodRVPA.nestPairN);
            this.tempMethodRVPA = null;
        }
        if (this.tempMethodRIPA != null) {
            this.method_RIPA_bands.addParameterAnnotation(this.tempMethodRIPA.numParams, this.tempMethodRIPA.annoN, this.tempMethodRIPA.pairN, this.tempMethodRIPA.typeRS, this.tempMethodRIPA.nameRU, this.tempMethodRIPA.tags, this.tempMethodRIPA.values, this.tempMethodRIPA.caseArrayN, this.tempMethodRIPA.nestTypeRS, this.tempMethodRIPA.nestNameRU, this.tempMethodRIPA.nestPairN);
            this.tempMethodRIPA = null;
        }
        if (this.codeFlags.size() > 0) {
            long latestCodeFlag = this.codeFlags.get(this.codeFlags.size() - 1);
            int latestLocalVariableTableN = this.codeLocalVariableTableN.get(this.codeLocalVariableTableN.size() - 1);
            if (latestCodeFlag == 4L && latestLocalVariableTableN == 0) {
                this.codeLocalVariableTableN.remove(this.codeLocalVariableTableN.size() - 1);
                this.codeFlags.remove(this.codeFlags.size() - 1);
                this.codeFlags.add(0L);
            }
        }
    }

    public void finaliseBands() {
        int i;
        int defaultMajorVersion = this.segmentHeader.getDefaultMajorVersion();
        for (int i2 = 0; i2 < this.class_flags.length; ++i2) {
            int major = this.major_versions[i2];
            if (major == defaultMajorVersion) continue;
            int n = i2;
            this.class_flags[n] = this.class_flags[n] | 0x1000000L;
            this.classFileVersionMajor.add(major);
            this.classFileVersionMinor.add(0);
        }
        this.codeHeaders = new int[this.codeHandlerCount.size()];
        int removed = 0;
        for (int i3 = 0; i3 < this.codeHeaders.length; ++i3) {
            int numHandlers = this.codeHandlerCount.get(i3 - removed);
            int maxLocals = this.codeMaxLocals.get(i3 - removed);
            int maxStack = this.codeMaxStack.get(i3 - removed);
            switch (numHandlers) {
                case 0: {
                    int header = maxLocals * 12 + maxStack + 1;
                    if (header >= 145 || maxStack >= 12) break;
                    this.codeHeaders[i3] = header;
                    break;
                }
                case 1: {
                    int header = maxLocals * 8 + maxStack + 145;
                    if (header >= 209 || maxStack >= 8) break;
                    this.codeHeaders[i3] = header;
                    break;
                }
                case 2: {
                    int header = maxLocals * 7 + maxStack + 209;
                    if (header >= 256 || maxStack >= 7) break;
                    this.codeHeaders[i3] = header;
                    break;
                }
            }
            if (this.codeHeaders[i3] != 0) {
                this.codeHandlerCount.remove(i3 - removed);
                this.codeMaxLocals.remove(i3 - removed);
                this.codeMaxStack.remove(i3 - removed);
                ++removed;
                continue;
            }
            if (this.segment.getSegmentHeader().have_all_code_flags()) continue;
            this.codeFlags.add(0L);
        }
        IntList innerClassesN = new IntList();
        ArrayList<IcBands.IcTuple> icLocal = new ArrayList<IcBands.IcTuple>();
        for (i = 0; i < this.class_this.length; ++i) {
            CPClass cpClass = this.class_this[i];
            Set<CPClass> referencedInnerClasses = this.classReferencesInnerClass.get(cpClass);
            if (referencedInnerClasses == null) continue;
            int innerN = 0;
            List<IcBands.IcTuple> innerClasses = this.segment.getIcBands().getInnerClassesForOuter(cpClass.toString());
            if (innerClasses != null) {
                for (IcBands.IcTuple element : innerClasses) {
                    referencedInnerClasses.remove(element.C);
                }
            }
            for (CPClass inner : referencedInnerClasses) {
                IcBands.IcTuple icTuple = this.segment.getIcBands().getIcTuple(inner);
                if (icTuple == null || icTuple.isAnonymous()) continue;
                icLocal.add(icTuple);
                ++innerN;
            }
            if (innerN == 0) continue;
            innerClassesN.add(innerN);
            int n = i;
            this.class_flags[n] = this.class_flags[n] | 0x800000L;
        }
        this.class_InnerClasses_N = innerClassesN.toArray();
        this.class_InnerClasses_RC = new CPClass[icLocal.size()];
        this.class_InnerClasses_F = new int[icLocal.size()];
        this.classInnerClassesOuterRCN = new ArrayList<CPClass>();
        this.classInnerClassesNameRUN = new ArrayList<CPUTF8>();
        for (i = 0; i < this.class_InnerClasses_RC.length; ++i) {
            IcBands.IcTuple icTuple = (IcBands.IcTuple)icLocal.get(i);
            this.class_InnerClasses_RC[i] = icTuple.C;
            if (icTuple.C2 == null && icTuple.N == null) {
                this.class_InnerClasses_F[i] = 0;
                continue;
            }
            this.class_InnerClasses_F[i] = icTuple.F == 0 ? 65536 : icTuple.F;
            this.classInnerClassesOuterRCN.add(icTuple.C2);
            this.classInnerClassesNameRUN.add(icTuple.N);
        }
        IntList classAttrCalls = new IntList();
        IntList fieldAttrCalls = new IntList();
        IntList methodAttrCalls = new IntList();
        IntList codeAttrCalls = new IntList();
        if (this.class_RVA_bands.hasContent()) {
            classAttrCalls.add(this.class_RVA_bands.numBackwardsCalls());
        }
        if (this.class_RIA_bands.hasContent()) {
            classAttrCalls.add(this.class_RIA_bands.numBackwardsCalls());
        }
        if (this.field_RVA_bands.hasContent()) {
            fieldAttrCalls.add(this.field_RVA_bands.numBackwardsCalls());
        }
        if (this.field_RIA_bands.hasContent()) {
            fieldAttrCalls.add(this.field_RIA_bands.numBackwardsCalls());
        }
        if (this.method_RVA_bands.hasContent()) {
            methodAttrCalls.add(this.method_RVA_bands.numBackwardsCalls());
        }
        if (this.method_RIA_bands.hasContent()) {
            methodAttrCalls.add(this.method_RIA_bands.numBackwardsCalls());
        }
        if (this.method_RVPA_bands.hasContent()) {
            methodAttrCalls.add(this.method_RVPA_bands.numBackwardsCalls());
        }
        if (this.method_RIPA_bands.hasContent()) {
            methodAttrCalls.add(this.method_RIPA_bands.numBackwardsCalls());
        }
        if (this.method_AD_bands.hasContent()) {
            methodAttrCalls.add(this.method_AD_bands.numBackwardsCalls());
        }
        Comparator comparator = (arg0, arg1) -> arg0.getFlagIndex() - arg1.getFlagIndex();
        this.classAttributeBands.sort(comparator);
        this.methodAttributeBands.sort(comparator);
        this.fieldAttributeBands.sort(comparator);
        this.codeAttributeBands.sort(comparator);
        for (NewAttributeBands bands : this.classAttributeBands) {
            if (!bands.isUsedAtLeastOnce()) continue;
            for (int backwardsCallCount : bands.numBackwardsCalls()) {
                classAttrCalls.add(backwardsCallCount);
            }
        }
        for (NewAttributeBands bands : this.methodAttributeBands) {
            if (!bands.isUsedAtLeastOnce()) continue;
            for (int backwardsCallCount : bands.numBackwardsCalls()) {
                methodAttrCalls.add(backwardsCallCount);
            }
        }
        for (NewAttributeBands bands : this.fieldAttributeBands) {
            if (!bands.isUsedAtLeastOnce()) continue;
            for (int backwardsCallCount : bands.numBackwardsCalls()) {
                fieldAttrCalls.add(backwardsCallCount);
            }
        }
        for (NewAttributeBands bands : this.codeAttributeBands) {
            if (!bands.isUsedAtLeastOnce()) continue;
            for (int backwardsCallCount : bands.numBackwardsCalls()) {
                codeAttrCalls.add(backwardsCallCount);
            }
        }
        this.class_attr_calls = classAttrCalls.toArray();
        this.field_attr_calls = fieldAttrCalls.toArray();
        this.method_attr_calls = methodAttrCalls.toArray();
        this.code_attr_calls = codeAttrCalls.toArray();
    }

    private int[] getInts(CPClass[] cpClasses) {
        int[] ints = new int[cpClasses.length];
        for (int i = 0; i < ints.length; ++i) {
            if (cpClasses[i] == null) continue;
            ints[i] = cpClasses[i].getIndex();
        }
        return ints;
    }

    public boolean isAnySyntheticClasses() {
        return this.anySyntheticClasses;
    }

    public boolean isAnySyntheticFields() {
        return this.anySyntheticFields;
    }

    public boolean isAnySyntheticMethods() {
        return this.anySyntheticMethods;
    }

    private boolean isInnerClass(String possibleInner) {
        return possibleInner.indexOf(36) != -1;
    }

    private boolean isInnerClassOf(String possibleInner, CPClass possibleOuter) {
        if (this.isInnerClass(possibleInner)) {
            String superClassName = possibleInner.substring(0, possibleInner.lastIndexOf(36));
            if (superClassName.equals(possibleOuter.toString())) {
                return true;
            }
            return this.isInnerClassOf(superClassName, possibleOuter);
        }
        return false;
    }

    public int numClassesProcessed() {
        return this.index;
    }

    @Override
    public void pack(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing class bands...");
        byte[] encodedBand = this.encodeBandInt("class_this", this.getInts(this.class_this), Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_this[" + this.class_this.length + "]");
        encodedBand = this.encodeBandInt("class_super", this.getInts(this.class_super), Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_super[" + this.class_super.length + "]");
        encodedBand = this.encodeBandInt("class_interface_count", this.class_interface_count, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_interface_count[" + this.class_interface_count.length + "]");
        int totalInterfaces = this.sum(this.class_interface_count);
        int[] classInterface = new int[totalInterfaces];
        int k = 0;
        for (CPClass[] element : this.class_interface) {
            if (element == null) continue;
            for (CPClass cpClass : element) {
                classInterface[k] = cpClass.getIndex();
                ++k;
            }
        }
        encodedBand = this.encodeBandInt("class_interface", classInterface, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_interface[" + classInterface.length + "]");
        encodedBand = this.encodeBandInt("class_field_count", this.class_field_count, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_field_count[" + this.class_field_count.length + "]");
        encodedBand = this.encodeBandInt("class_method_count", this.class_method_count, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_method_count[" + this.class_method_count.length + "]");
        int totalFields = this.sum(this.class_field_count);
        int[] fieldDescr = new int[totalFields];
        k = 0;
        for (int i = 0; i < this.index; ++i) {
            for (int j = 0; j < this.field_descr[i].length; ++j) {
                CPNameAndType descr = this.field_descr[i][j];
                fieldDescr[k] = descr.getIndex();
                ++k;
            }
        }
        encodedBand = this.encodeBandInt("field_descr", fieldDescr, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from field_descr[" + fieldDescr.length + "]");
        this.writeFieldAttributeBands(out);
        int totalMethods = this.sum(this.class_method_count);
        int[] methodDescr = new int[totalMethods];
        k = 0;
        for (int i = 0; i < this.index; ++i) {
            for (int j = 0; j < this.method_descr[i].length; ++j) {
                CPNameAndType descr = this.method_descr[i][j];
                methodDescr[k] = descr.getIndex();
                ++k;
            }
        }
        encodedBand = this.encodeBandInt("method_descr", methodDescr, Codec.MDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from method_descr[" + methodDescr.length + "]");
        this.writeMethodAttributeBands(out);
        this.writeClassAttributeBands(out);
        this.writeCodeBands(out);
    }

    public void removeCurrentClass() {
        long flags;
        if ((this.class_flags[this.index] & 0x20000L) != 0L) {
            this.classSourceFile.remove(this.classSourceFile.size() - 1);
        }
        if ((this.class_flags[this.index] & 0x40000L) != 0L) {
            this.classEnclosingMethodClass.remove(this.classEnclosingMethodClass.size() - 1);
            this.classEnclosingMethodDesc.remove(this.classEnclosingMethodDesc.size() - 1);
        }
        if ((this.class_flags[this.index] & 0x80000L) != 0L) {
            this.classSignature.remove(this.classSignature.size() - 1);
        }
        if ((this.class_flags[this.index] & 0x200000L) != 0L) {
            this.class_RVA_bands.removeLatest();
        }
        if ((this.class_flags[this.index] & 0x400000L) != 0L) {
            this.class_RIA_bands.removeLatest();
        }
        for (Long flagsL : this.tempFieldFlags) {
            flags = flagsL;
            if ((flags & 0x80000L) != 0L) {
                this.fieldSignature.remove(this.fieldSignature.size() - 1);
            }
            if ((flags & 0x20000L) != 0L) {
                this.fieldConstantValueKQ.remove(this.fieldConstantValueKQ.size() - 1);
            }
            if ((flags & 0x200000L) != 0L) {
                this.field_RVA_bands.removeLatest();
            }
            if ((flags & 0x400000L) == 0L) continue;
            this.field_RIA_bands.removeLatest();
        }
        for (Long flagsL : this.tempMethodFlags) {
            int i;
            flags = flagsL;
            if ((flags & 0x80000L) != 0L) {
                this.methodSignature.remove(this.methodSignature.size() - 1);
            }
            if ((flags & 0x40000L) != 0L) {
                int exceptions = this.methodExceptionNumber.remove(this.methodExceptionNumber.size() - 1);
                for (i = 0; i < exceptions; ++i) {
                    this.methodExceptionClasses.remove(this.methodExceptionClasses.size() - 1);
                }
            }
            if ((flags & 0x20000L) != 0L) {
                this.codeMaxLocals.remove(this.codeMaxLocals.size() - 1);
                this.codeMaxStack.remove(this.codeMaxStack.size() - 1);
                int handlers = this.codeHandlerCount.remove(this.codeHandlerCount.size() - 1);
                for (i = 0; i < handlers; ++i) {
                    int index = this.codeHandlerStartP.size() - 1;
                    this.codeHandlerStartP.remove(index);
                    this.codeHandlerEndPO.remove(index);
                    this.codeHandlerCatchPO.remove(index);
                    this.codeHandlerClass.remove(index);
                }
                if (!this.stripDebug) {
                    int location;
                    int i2;
                    long cdeFlags = this.codeFlags.remove(this.codeFlags.size() - 1);
                    int numLocalVariables = this.codeLocalVariableTableN.remove(this.codeLocalVariableTableN.size() - 1);
                    for (int i3 = 0; i3 < numLocalVariables; ++i3) {
                        int location2 = this.codeLocalVariableTableBciP.size() - 1;
                        this.codeLocalVariableTableBciP.remove(location2);
                        this.codeLocalVariableTableSpanO.remove(location2);
                        this.codeLocalVariableTableNameRU.remove(location2);
                        this.codeLocalVariableTableTypeRS.remove(location2);
                        this.codeLocalVariableTableSlot.remove(location2);
                    }
                    if ((cdeFlags & 8L) != 0L) {
                        int numLocalVariablesInTypeTable = this.codeLocalVariableTypeTableN.remove(this.codeLocalVariableTypeTableN.size() - 1);
                        for (i2 = 0; i2 < numLocalVariablesInTypeTable; ++i2) {
                            location = this.codeLocalVariableTypeTableBciP.size() - 1;
                            this.codeLocalVariableTypeTableBciP.remove(location);
                            this.codeLocalVariableTypeTableSpanO.remove(location);
                            this.codeLocalVariableTypeTableNameRU.remove(location);
                            this.codeLocalVariableTypeTableTypeRS.remove(location);
                            this.codeLocalVariableTypeTableSlot.remove(location);
                        }
                    }
                    if ((cdeFlags & 2L) != 0L) {
                        int numLineNumbers = this.codeLineNumberTableN.remove(this.codeLineNumberTableN.size() - 1);
                        for (i2 = 0; i2 < numLineNumbers; ++i2) {
                            location = this.codeLineNumberTableBciP.size() - 1;
                            this.codeLineNumberTableBciP.remove(location);
                            this.codeLineNumberTableLine.remove(location);
                        }
                    }
                }
            }
            if ((flags & 0x200000L) != 0L) {
                this.method_RVA_bands.removeLatest();
            }
            if ((flags & 0x400000L) != 0L) {
                this.method_RIA_bands.removeLatest();
            }
            if ((flags & 0x800000L) != 0L) {
                this.method_RVPA_bands.removeLatest();
            }
            if ((flags & 0x1000000L) != 0L) {
                this.method_RIPA_bands.removeLatest();
            }
            if ((flags & 0x2000000L) == 0L) continue;
            this.method_AD_bands.removeLatest();
        }
        this.class_this[this.index] = null;
        this.class_super[this.index] = null;
        this.class_interface_count[this.index] = 0;
        this.class_interface[this.index] = null;
        this.major_versions[this.index] = 0;
        this.class_flags[this.index] = 0L;
        this.tempFieldDesc.clear();
        this.tempFieldFlags.clear();
        this.tempMethodDesc.clear();
        this.tempMethodFlags.clear();
        if (this.index > 0) {
            --this.index;
        }
    }

    private void renumberBci(List<Integer> list, IntList bciRenumbering, Map<Label, Integer> labelsToOffsets) {
        Integer label;
        for (int i = list.size() - 1; i >= 0 && !((label = list.get(i)) instanceof Integer); --i) {
            if (!(label instanceof Label)) continue;
            list.remove(i);
            Integer bytecodeIndex = labelsToOffsets.get(label);
            list.add(i, bciRenumbering.get(bytecodeIndex));
        }
    }

    private void renumberDoubleOffsetBci(List<Integer> relative, List<Integer> firstOffset, List<Object> list, IntList bciRenumbering, Map<Label, Integer> labelsToOffsets) {
        Object label;
        for (int i = list.size() - 1; i >= 0 && !((label = list.get(i)) instanceof Integer); --i) {
            if (!(label instanceof Label)) continue;
            list.remove(i);
            Integer bytecodeIndex = labelsToOffsets.get(label);
            Integer renumberedOffset = bciRenumbering.get(bytecodeIndex) - relative.get(i) - firstOffset.get(i);
            list.add(i, renumberedOffset);
        }
    }

    private void renumberOffsetBci(List<Integer> relative, List<Integer> list, IntList bciRenumbering, Map<Label, Integer> labelsToOffsets) {
        Integer label;
        for (int i = list.size() - 1; i >= 0 && !((label = list.get(i)) instanceof Integer); --i) {
            if (!(label instanceof Label)) continue;
            list.remove(i);
            Integer bytecodeIndex = labelsToOffsets.get(label);
            Integer renumberedOffset = bciRenumbering.get(bytecodeIndex) - relative.get(i);
            list.add(i, renumberedOffset);
        }
    }

    private int sum(int[] ints) {
        int sum = 0;
        for (int j : ints) {
            sum += j;
        }
        return sum;
    }

    private void writeClassAttributeBands(OutputStream out) throws IOException, Pack200Exception {
        byte[] encodedBand = this.encodeFlags("class_flags", this.class_flags, Codec.UNSIGNED5, Codec.UNSIGNED5, this.segmentHeader.have_class_flags_hi());
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_flags[" + this.class_flags.length + "]");
        encodedBand = this.encodeBandInt("class_attr_calls", this.class_attr_calls, Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_attr_calls[" + this.class_attr_calls.length + "]");
        encodedBand = this.encodeBandInt("classSourceFile", this.cpEntryOrNullListToArray(this.classSourceFile), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from classSourceFile[" + this.classSourceFile.size() + "]");
        encodedBand = this.encodeBandInt("class_enclosing_method_RC", this.cpEntryListToArray(this.classEnclosingMethodClass), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_enclosing_method_RC[" + this.classEnclosingMethodClass.size() + "]");
        encodedBand = this.encodeBandInt("class_EnclosingMethod_RDN", this.cpEntryOrNullListToArray(this.classEnclosingMethodDesc), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_EnclosingMethod_RDN[" + this.classEnclosingMethodDesc.size() + "]");
        encodedBand = this.encodeBandInt("class_Signature_RS", this.cpEntryListToArray(this.classSignature), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_Signature_RS[" + this.classSignature.size() + "]");
        this.class_RVA_bands.pack(out);
        this.class_RIA_bands.pack(out);
        encodedBand = this.encodeBandInt("class_InnerClasses_N", this.class_InnerClasses_N, Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_InnerClasses_N[" + this.class_InnerClasses_N.length + "]");
        encodedBand = this.encodeBandInt("class_InnerClasses_RC", this.getInts(this.class_InnerClasses_RC), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_InnerClasses_RC[" + this.class_InnerClasses_RC.length + "]");
        encodedBand = this.encodeBandInt("class_InnerClasses_F", this.class_InnerClasses_F, Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_InnerClasses_F[" + this.class_InnerClasses_F.length + "]");
        encodedBand = this.encodeBandInt("class_InnerClasses_outer_RCN", this.cpEntryOrNullListToArray(this.classInnerClassesOuterRCN), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_InnerClasses_outer_RCN[" + this.classInnerClassesOuterRCN.size() + "]");
        encodedBand = this.encodeBandInt("class_InnerClasses_name_RUN", this.cpEntryOrNullListToArray(this.classInnerClassesNameRUN), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from class_InnerClasses_name_RUN[" + this.classInnerClassesNameRUN.size() + "]");
        encodedBand = this.encodeBandInt("classFileVersionMinor", this.classFileVersionMinor.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from classFileVersionMinor[" + this.classFileVersionMinor.size() + "]");
        encodedBand = this.encodeBandInt("classFileVersionMajor", this.classFileVersionMajor.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from classFileVersionMajor[" + this.classFileVersionMajor.size() + "]");
        for (NewAttributeBands classAttributeBand : this.classAttributeBands) {
            classAttributeBand.pack(out);
        }
    }

    private void writeCodeAttributeBands(OutputStream out) throws IOException, Pack200Exception {
        byte[] encodedBand = this.encodeFlags("codeFlags", this.longListToArray(this.codeFlags), Codec.UNSIGNED5, Codec.UNSIGNED5, this.segmentHeader.have_code_flags_hi());
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from codeFlags[" + this.codeFlags.size() + "]");
        encodedBand = this.encodeBandInt("code_attr_calls", this.code_attr_calls, Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_attr_calls[" + this.code_attr_calls.length + "]");
        encodedBand = this.encodeBandInt("code_LineNumberTable_N", this.codeLineNumberTableN.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LineNumberTable_N[" + this.codeLineNumberTableN.size() + "]");
        encodedBand = this.encodeBandInt("code_LineNumberTable_bci_P", this.integerListToArray(this.codeLineNumberTableBciP), Codec.BCI5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LineNumberTable_bci_P[" + this.codeLineNumberTableBciP.size() + "]");
        encodedBand = this.encodeBandInt("code_LineNumberTable_line", this.codeLineNumberTableLine.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LineNumberTable_line[" + this.codeLineNumberTableLine.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTable_N", this.codeLocalVariableTableN.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTable_N[" + this.codeLocalVariableTableN.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTable_bci_P", this.integerListToArray(this.codeLocalVariableTableBciP), Codec.BCI5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTable_bci_P[" + this.codeLocalVariableTableBciP.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTable_span_O", this.integerListToArray(this.codeLocalVariableTableSpanO), Codec.BRANCH5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTable_span_O[" + this.codeLocalVariableTableSpanO.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTable_name_RU", this.cpEntryListToArray(this.codeLocalVariableTableNameRU), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTable_name_RU[" + this.codeLocalVariableTableNameRU.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTable_type_RS", this.cpEntryListToArray(this.codeLocalVariableTableTypeRS), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTable_type_RS[" + this.codeLocalVariableTableTypeRS.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTable_slot", this.codeLocalVariableTableSlot.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTable_slot[" + this.codeLocalVariableTableSlot.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTypeTable_N", this.codeLocalVariableTypeTableN.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTypeTable_N[" + this.codeLocalVariableTypeTableN.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTypeTable_bci_P", this.integerListToArray(this.codeLocalVariableTypeTableBciP), Codec.BCI5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTypeTable_bci_P[" + this.codeLocalVariableTypeTableBciP.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTypeTable_span_O", this.integerListToArray(this.codeLocalVariableTypeTableSpanO), Codec.BRANCH5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTypeTable_span_O[" + this.codeLocalVariableTypeTableSpanO.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTypeTable_name_RU", this.cpEntryListToArray(this.codeLocalVariableTypeTableNameRU), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTypeTable_name_RU[" + this.codeLocalVariableTypeTableNameRU.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTypeTable_type_RS", this.cpEntryListToArray(this.codeLocalVariableTypeTableTypeRS), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTypeTable_type_RS[" + this.codeLocalVariableTypeTableTypeRS.size() + "]");
        encodedBand = this.encodeBandInt("code_LocalVariableTypeTable_slot", this.codeLocalVariableTypeTableSlot.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from code_LocalVariableTypeTable_slot[" + this.codeLocalVariableTypeTableSlot.size() + "]");
        for (NewAttributeBands bands : this.codeAttributeBands) {
            bands.pack(out);
        }
    }

    private void writeCodeBands(OutputStream out) throws IOException, Pack200Exception {
        byte[] encodedBand = this.encodeBandInt("codeHeaders", this.codeHeaders, Codec.BYTE1);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from codeHeaders[" + this.codeHeaders.length + "]");
        encodedBand = this.encodeBandInt("codeMaxStack", this.codeMaxStack.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from codeMaxStack[" + this.codeMaxStack.size() + "]");
        encodedBand = this.encodeBandInt("codeMaxLocals", this.codeMaxLocals.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from codeMaxLocals[" + this.codeMaxLocals.size() + "]");
        encodedBand = this.encodeBandInt("codeHandlerCount", this.codeHandlerCount.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from codeHandlerCount[" + this.codeHandlerCount.size() + "]");
        encodedBand = this.encodeBandInt("codeHandlerStartP", this.integerListToArray(this.codeHandlerStartP), Codec.BCI5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from codeHandlerStartP[" + this.codeHandlerStartP.size() + "]");
        encodedBand = this.encodeBandInt("codeHandlerEndPO", this.integerListToArray(this.codeHandlerEndPO), Codec.BRANCH5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from codeHandlerEndPO[" + this.codeHandlerEndPO.size() + "]");
        encodedBand = this.encodeBandInt("codeHandlerCatchPO", this.integerListToArray(this.codeHandlerCatchPO), Codec.BRANCH5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from codeHandlerCatchPO[" + this.codeHandlerCatchPO.size() + "]");
        encodedBand = this.encodeBandInt("codeHandlerClass", this.cpEntryOrNullListToArray(this.codeHandlerClass), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from codeHandlerClass[" + this.codeHandlerClass.size() + "]");
        this.writeCodeAttributeBands(out);
    }

    private void writeFieldAttributeBands(OutputStream out) throws IOException, Pack200Exception {
        byte[] encodedBand = this.encodeFlags("field_flags", this.field_flags, Codec.UNSIGNED5, Codec.UNSIGNED5, this.segmentHeader.have_field_flags_hi());
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from field_flags[" + this.field_flags.length + "]");
        encodedBand = this.encodeBandInt("field_attr_calls", this.field_attr_calls, Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from field_attr_calls[" + this.field_attr_calls.length + "]");
        encodedBand = this.encodeBandInt("fieldConstantValueKQ", this.cpEntryListToArray(this.fieldConstantValueKQ), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from fieldConstantValueKQ[" + this.fieldConstantValueKQ.size() + "]");
        encodedBand = this.encodeBandInt("fieldSignature", this.cpEntryListToArray(this.fieldSignature), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from fieldSignature[" + this.fieldSignature.size() + "]");
        this.field_RVA_bands.pack(out);
        this.field_RIA_bands.pack(out);
        for (NewAttributeBands bands : this.fieldAttributeBands) {
            bands.pack(out);
        }
    }

    private void writeMethodAttributeBands(OutputStream out) throws IOException, Pack200Exception {
        byte[] encodedBand = this.encodeFlags("method_flags", this.method_flags, Codec.UNSIGNED5, Codec.UNSIGNED5, this.segmentHeader.have_method_flags_hi());
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from method_flags[" + this.method_flags.length + "]");
        encodedBand = this.encodeBandInt("method_attr_calls", this.method_attr_calls, Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from method_attr_calls[" + this.method_attr_calls.length + "]");
        encodedBand = this.encodeBandInt("methodExceptionNumber", this.methodExceptionNumber.toArray(), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from methodExceptionNumber[" + this.methodExceptionNumber.size() + "]");
        encodedBand = this.encodeBandInt("methodExceptionClasses", this.cpEntryListToArray(this.methodExceptionClasses), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from methodExceptionClasses[" + this.methodExceptionClasses.size() + "]");
        encodedBand = this.encodeBandInt("methodSignature", this.cpEntryListToArray(this.methodSignature), Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from methodSignature[" + this.methodSignature.size() + "]");
        this.method_RVA_bands.pack(out);
        this.method_RIA_bands.pack(out);
        this.method_RVPA_bands.pack(out);
        this.method_RIPA_bands.pack(out);
        this.method_AD_bands.pack(out);
        for (NewAttributeBands bands : this.methodAttributeBands) {
            bands.pack(out);
        }
    }

    private static final class TempParamAnnotation {
        int numParams;
        int[] annoN;
        IntList pairN = new IntList();
        List<String> typeRS = new ArrayList<String>();
        List<String> nameRU = new ArrayList<String>();
        List<String> tags = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        List<Integer> caseArrayN = new ArrayList<Integer>();
        List<String> nestTypeRS = new ArrayList<String>();
        List<String> nestNameRU = new ArrayList<String>();
        List<Integer> nestPairN = new ArrayList<Integer>();

        TempParamAnnotation(int numParams) {
            this.numParams = numParams;
            this.annoN = new int[numParams];
        }

        void addParameterAnnotation(int parameter, String desc, List<String> nameRU, List<String> tags, List<Object> values2, List<Integer> caseArrayN, List<String> nestTypeRS, List<String> nestNameRU, List<Integer> nestPairN) {
            int n = parameter;
            this.annoN[n] = this.annoN[n] + 1;
            this.typeRS.add(desc);
            this.pairN.add(nameRU.size());
            this.nameRU.addAll(nameRU);
            this.tags.addAll(tags);
            this.values.addAll(values2);
            this.caseArrayN.addAll(caseArrayN);
            this.nestTypeRS.addAll(nestTypeRS);
            this.nestNameRU.addAll(nestNameRU);
            this.nestPairN.addAll(nestPairN);
        }
    }
}

