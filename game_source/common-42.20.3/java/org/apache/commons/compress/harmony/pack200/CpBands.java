/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.Type
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.compress.harmony.pack200.BandSet;
import org.apache.commons.compress.harmony.pack200.CPClass;
import org.apache.commons.compress.harmony.pack200.CPConstant;
import org.apache.commons.compress.harmony.pack200.CPDouble;
import org.apache.commons.compress.harmony.pack200.CPFloat;
import org.apache.commons.compress.harmony.pack200.CPInt;
import org.apache.commons.compress.harmony.pack200.CPLong;
import org.apache.commons.compress.harmony.pack200.CPMethodOrField;
import org.apache.commons.compress.harmony.pack200.CPNameAndType;
import org.apache.commons.compress.harmony.pack200.CPSignature;
import org.apache.commons.compress.harmony.pack200.CPString;
import org.apache.commons.compress.harmony.pack200.CPUTF8;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.ConstantPoolEntry;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingUtils;
import org.apache.commons.compress.harmony.pack200.Segment;
import org.objectweb.asm.Type;

public class CpBands
extends BandSet {
    private final Set<String> defaultAttributeNames = new HashSet<String>();
    private final Set<CPUTF8> cp_Utf8 = new TreeSet<CPUTF8>();
    private final Set<CPInt> cp_Int = new TreeSet<CPInt>();
    private final Set<CPFloat> cp_Float = new TreeSet<CPFloat>();
    private final Set<CPLong> cp_Long = new TreeSet<CPLong>();
    private final Set<CPDouble> cp_Double = new TreeSet<CPDouble>();
    private final Set<CPString> cp_String = new TreeSet<CPString>();
    private final Set<CPClass> cp_Class = new TreeSet<CPClass>();
    private final Set<CPSignature> cp_Signature = new TreeSet<CPSignature>();
    private final Set<CPNameAndType> cp_Descr = new TreeSet<CPNameAndType>();
    private final Set<CPMethodOrField> cp_Field = new TreeSet<CPMethodOrField>();
    private final Set<CPMethodOrField> cp_Method = new TreeSet<CPMethodOrField>();
    private final Set<CPMethodOrField> cp_Imethod = new TreeSet<CPMethodOrField>();
    private final Map<String, CPUTF8> stringsToCpUtf8 = new HashMap<String, CPUTF8>();
    private final Map<String, CPNameAndType> stringsToCpNameAndType = new HashMap<String, CPNameAndType>();
    private final Map<String, CPClass> stringsToCpClass = new HashMap<String, CPClass>();
    private final Map<String, CPSignature> stringsToCpSignature = new HashMap<String, CPSignature>();
    private final Map<String, CPMethodOrField> stringsToCpMethod = new HashMap<String, CPMethodOrField>();
    private final Map<String, CPMethodOrField> stringsToCpField = new HashMap<String, CPMethodOrField>();
    private final Map<String, CPMethodOrField> stringsToCpIMethod = new HashMap<String, CPMethodOrField>();
    private final Map<Object, CPConstant<?>> objectsToCPConstant = new HashMap();
    private final Segment segment;

    public CpBands(Segment segment, int effort) {
        super(effort, segment.getSegmentHeader());
        this.segment = segment;
        this.defaultAttributeNames.add("AnnotationDefault");
        this.defaultAttributeNames.add("RuntimeVisibleAnnotations");
        this.defaultAttributeNames.add("RuntimeInvisibleAnnotations");
        this.defaultAttributeNames.add("RuntimeVisibleParameterAnnotations");
        this.defaultAttributeNames.add("RuntimeInvisibleParameterAnnotations");
        this.defaultAttributeNames.add("Code");
        this.defaultAttributeNames.add("LineNumberTable");
        this.defaultAttributeNames.add("LocalVariableTable");
        this.defaultAttributeNames.add("LocalVariableTypeTable");
        this.defaultAttributeNames.add("ConstantValue");
        this.defaultAttributeNames.add("Deprecated");
        this.defaultAttributeNames.add("EnclosingMethod");
        this.defaultAttributeNames.add("Exceptions");
        this.defaultAttributeNames.add("InnerClasses");
        this.defaultAttributeNames.add("Signature");
        this.defaultAttributeNames.add("SourceFile");
    }

    private void addCharacters(List<Character> chars, char[] charArray) {
        for (char element : charArray) {
            chars.add(Character.valueOf(element));
        }
    }

    public void addCPClass(String className) {
        this.getCPClass(className);
    }

    void addCPUtf8(String utf8) {
        this.getCPUtf8(utf8);
    }

    private void addIndices() {
        for (Set set : Arrays.asList(this.cp_Utf8, this.cp_Int, this.cp_Float, this.cp_Long, this.cp_Double, this.cp_String, this.cp_Class, this.cp_Signature, this.cp_Descr, this.cp_Field, this.cp_Method, this.cp_Imethod)) {
            int j = 0;
            for (ConstantPoolEntry entry : set) {
                entry.setIndex(j);
                ++j;
            }
        }
        HashMap classNameToIndex = new HashMap();
        this.cp_Field.forEach(mOrF -> {
            CPClass cpClassName = mOrF.getClassName();
            Integer index = (Integer)classNameToIndex.get(cpClassName);
            if (index == null) {
                classNameToIndex.put(cpClassName, 1);
                mOrF.setIndexInClass(0);
            } else {
                int theIndex = index;
                mOrF.setIndexInClass(theIndex);
                classNameToIndex.put(cpClassName, theIndex + 1);
            }
        });
        classNameToIndex.clear();
        HashMap classNameToConstructorIndex = new HashMap();
        this.cp_Method.forEach(mOrF -> {
            CPClass cpClassName = mOrF.getClassName();
            Integer index = (Integer)classNameToIndex.get(cpClassName);
            if (index == null) {
                classNameToIndex.put(cpClassName, 1);
                mOrF.setIndexInClass(0);
            } else {
                int theIndex = index;
                mOrF.setIndexInClass(theIndex);
                classNameToIndex.put(cpClassName, theIndex + 1);
            }
            if (mOrF.getDesc().getName().equals("<init>")) {
                Integer constructorIndex = (Integer)classNameToConstructorIndex.get(cpClassName);
                if (constructorIndex == null) {
                    classNameToConstructorIndex.put(cpClassName, 1);
                    mOrF.setIndexInClassForConstructor(0);
                } else {
                    int theIndex = constructorIndex;
                    mOrF.setIndexInClassForConstructor(theIndex);
                    classNameToConstructorIndex.put(cpClassName, theIndex + 1);
                }
            }
        });
    }

    public boolean existsCpClass(String className) {
        return this.stringsToCpClass.containsKey(className);
    }

    public void finaliseBands() {
        this.addCPUtf8("");
        this.removeSignaturesFromCpUTF8();
        this.addIndices();
        this.segmentHeader.setCp_Utf8_count(this.cp_Utf8.size());
        this.segmentHeader.setCp_Int_count(this.cp_Int.size());
        this.segmentHeader.setCp_Float_count(this.cp_Float.size());
        this.segmentHeader.setCp_Long_count(this.cp_Long.size());
        this.segmentHeader.setCp_Double_count(this.cp_Double.size());
        this.segmentHeader.setCp_String_count(this.cp_String.size());
        this.segmentHeader.setCp_Class_count(this.cp_Class.size());
        this.segmentHeader.setCp_Signature_count(this.cp_Signature.size());
        this.segmentHeader.setCp_Descr_count(this.cp_Descr.size());
        this.segmentHeader.setCp_Field_count(this.cp_Field.size());
        this.segmentHeader.setCp_Method_count(this.cp_Method.size());
        this.segmentHeader.setCp_Imethod_count(this.cp_Imethod.size());
    }

    public CPConstant<?> getConstant(Object value) {
        CPConstant constant = this.objectsToCPConstant.get(value);
        if (constant == null) {
            if (value instanceof Integer) {
                constant = new CPInt((Integer)value);
                this.cp_Int.add((CPInt)constant);
            } else if (value instanceof Long) {
                constant = new CPLong((Long)value);
                this.cp_Long.add((CPLong)constant);
            } else if (value instanceof Float) {
                constant = new CPFloat(((Float)value).floatValue());
                this.cp_Float.add((CPFloat)constant);
            } else if (value instanceof Double) {
                constant = new CPDouble((Double)value);
                this.cp_Double.add((CPDouble)constant);
            } else if (value instanceof String) {
                constant = new CPString(this.getCPUtf8((String)value));
                this.cp_String.add((CPString)constant);
            } else if (value instanceof Type) {
                String className = ((Type)value).getClassName();
                if (className.endsWith("[]")) {
                    className = "[L" + className.substring(0, className.length() - 2);
                    while (className.endsWith("[]")) {
                        className = "[" + className.substring(0, className.length() - 2);
                    }
                    className = className + ";";
                }
                constant = this.getCPClass(className);
            }
            this.objectsToCPConstant.put(value, constant);
        }
        return constant;
    }

    public CPClass getCPClass(String className) {
        if (className == null) {
            return null;
        }
        CPClass cpClass = this.stringsToCpClass.get(className = className.replace('.', '/'));
        if (cpClass == null) {
            CPUTF8 cpUtf8 = this.getCPUtf8(className);
            cpClass = new CPClass(cpUtf8);
            this.cp_Class.add(cpClass);
            this.stringsToCpClass.put(className, cpClass);
        }
        if (cpClass.isInnerClass()) {
            this.segment.getClassBands().currentClassReferencesInnerClass(cpClass);
        }
        return cpClass;
    }

    public CPMethodOrField getCPField(CPClass cpClass, String name, String desc) {
        String key = cpClass.toString() + ":" + name + ":" + desc;
        CPMethodOrField cpF = this.stringsToCpField.get(key);
        if (cpF == null) {
            CPNameAndType nAndT = this.getCPNameAndType(name, desc);
            cpF = new CPMethodOrField(cpClass, nAndT);
            this.cp_Field.add(cpF);
            this.stringsToCpField.put(key, cpF);
        }
        return cpF;
    }

    public CPMethodOrField getCPField(String owner, String name, String desc) {
        return this.getCPField(this.getCPClass(owner), name, desc);
    }

    public CPMethodOrField getCPIMethod(CPClass cpClass, String name, String desc) {
        String key = cpClass.toString() + ":" + name + ":" + desc;
        CPMethodOrField cpIM = this.stringsToCpIMethod.get(key);
        if (cpIM == null) {
            CPNameAndType nAndT = this.getCPNameAndType(name, desc);
            cpIM = new CPMethodOrField(cpClass, nAndT);
            this.cp_Imethod.add(cpIM);
            this.stringsToCpIMethod.put(key, cpIM);
        }
        return cpIM;
    }

    public CPMethodOrField getCPIMethod(String owner, String name, String desc) {
        return this.getCPIMethod(this.getCPClass(owner), name, desc);
    }

    public CPMethodOrField getCPMethod(CPClass cpClass, String name, String desc) {
        String key = cpClass.toString() + ":" + name + ":" + desc;
        CPMethodOrField cpM = this.stringsToCpMethod.get(key);
        if (cpM == null) {
            CPNameAndType nAndT = this.getCPNameAndType(name, desc);
            cpM = new CPMethodOrField(cpClass, nAndT);
            this.cp_Method.add(cpM);
            this.stringsToCpMethod.put(key, cpM);
        }
        return cpM;
    }

    public CPMethodOrField getCPMethod(String owner, String name, String desc) {
        return this.getCPMethod(this.getCPClass(owner), name, desc);
    }

    public CPNameAndType getCPNameAndType(String name, String signature) {
        String descr = name + ":" + signature;
        CPNameAndType nameAndType = this.stringsToCpNameAndType.get(descr);
        if (nameAndType == null) {
            nameAndType = new CPNameAndType(this.getCPUtf8(name), this.getCPSignature(signature));
            this.stringsToCpNameAndType.put(descr, nameAndType);
            this.cp_Descr.add(nameAndType);
        }
        return nameAndType;
    }

    public CPSignature getCPSignature(String signature) {
        if (signature == null) {
            return null;
        }
        CPSignature cpS = this.stringsToCpSignature.get(signature);
        if (cpS == null) {
            CPUTF8 signatureUTF8;
            ArrayList<CPClass> cpClasses = new ArrayList<CPClass>();
            if (signature.length() > 1 && signature.indexOf(76) != -1) {
                ArrayList<String> classes = new ArrayList<String>();
                char[] chars = signature.toCharArray();
                StringBuilder signatureString = new StringBuilder();
                block0: for (int i = 0; i < chars.length; ++i) {
                    signatureString.append(chars[i]);
                    if (chars[i] != 'L') continue;
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = i + 1; j < chars.length; ++j) {
                        char c = chars[j];
                        if (!Character.isLetter(c) && !Character.isDigit(c) && c != '/' && c != '$' && c != '_') {
                            classes.add(stringBuilder.toString());
                            i = j - 1;
                            continue block0;
                        }
                        stringBuilder.append(c);
                    }
                }
                this.removeCpUtf8(signature);
                for (String string : classes) {
                    String string2;
                    CPClass cpClass = null;
                    if (string != null && (cpClass = this.stringsToCpClass.get(string2 = string.replace('.', '/'))) == null) {
                        CPUTF8 cpUtf8 = this.getCPUtf8(string2);
                        cpClass = new CPClass(cpUtf8);
                        this.cp_Class.add(cpClass);
                        this.stringsToCpClass.put(string2, cpClass);
                    }
                    cpClasses.add(cpClass);
                }
                signatureUTF8 = this.getCPUtf8(signatureString.toString());
            } else {
                signatureUTF8 = this.getCPUtf8(signature);
            }
            cpS = new CPSignature(signature, signatureUTF8, cpClasses);
            this.cp_Signature.add(cpS);
            this.stringsToCpSignature.put(signature, cpS);
        }
        return cpS;
    }

    public CPUTF8 getCPUtf8(String utf8) {
        if (utf8 == null) {
            return null;
        }
        CPUTF8 cpUtf8 = this.stringsToCpUtf8.get(utf8);
        if (cpUtf8 == null) {
            cpUtf8 = new CPUTF8(utf8);
            this.cp_Utf8.add(cpUtf8);
            this.stringsToCpUtf8.put(utf8, cpUtf8);
        }
        return cpUtf8;
    }

    @Override
    public void pack(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing constant pool bands...");
        this.writeCpUtf8(out);
        this.writeCpInt(out);
        this.writeCpFloat(out);
        this.writeCpLong(out);
        this.writeCpDouble(out);
        this.writeCpString(out);
        this.writeCpClass(out);
        this.writeCpSignature(out);
        this.writeCpDescr(out);
        this.writeCpMethodOrField(this.cp_Field, out, "cp_Field");
        this.writeCpMethodOrField(this.cp_Method, out, "cp_Method");
        this.writeCpMethodOrField(this.cp_Imethod, out, "cp_Imethod");
    }

    private void removeCpUtf8(String string) {
        CPUTF8 utf8 = this.stringsToCpUtf8.get(string);
        if (utf8 != null && this.stringsToCpClass.get(string) == null) {
            this.stringsToCpUtf8.remove(string);
            this.cp_Utf8.remove(utf8);
        }
    }

    private void removeSignaturesFromCpUTF8() {
        this.cp_Signature.forEach(signature -> {
            CPUTF8 utf8;
            String form;
            String sigStr = signature.getUnderlyingString();
            if (!sigStr.equals(form = (utf8 = signature.getSignatureForm()).getUnderlyingString())) {
                this.removeCpUtf8(sigStr);
            }
        });
    }

    private void writeCpClass(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + this.cp_Class.size() + " Class entries...");
        int[] cpClass = new int[this.cp_Class.size()];
        int i = 0;
        for (CPClass cpCl : this.cp_Class) {
            cpClass[i] = cpCl.getIndexInCpUtf8();
            ++i;
        }
        byte[] encodedBand = this.encodeBandInt("cpClass", cpClass, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cpClass[" + cpClass.length + "]");
    }

    private void writeCpDescr(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + this.cp_Descr.size() + " Descriptor entries...");
        int[] cpDescrName = new int[this.cp_Descr.size()];
        int[] cpDescrType = new int[this.cp_Descr.size()];
        int i = 0;
        for (CPNameAndType nameAndType : this.cp_Descr) {
            cpDescrName[i] = nameAndType.getNameIndex();
            cpDescrType[i] = nameAndType.getTypeIndex();
            ++i;
        }
        byte[] encodedBand = this.encodeBandInt("cp_Descr_Name", cpDescrName, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cp_Descr_Name[" + cpDescrName.length + "]");
        encodedBand = this.encodeBandInt("cp_Descr_Type", cpDescrType, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cp_Descr_Type[" + cpDescrType.length + "]");
    }

    private void writeCpDouble(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + this.cp_Double.size() + " Double entries...");
        int[] highBits = new int[this.cp_Double.size()];
        int[] loBits = new int[this.cp_Double.size()];
        int i = 0;
        for (CPDouble dbl : this.cp_Double) {
            long l = Double.doubleToLongBits(dbl.getDouble());
            highBits[i] = (int)(l >> 32);
            loBits[i] = (int)l;
            ++i;
        }
        byte[] encodedBand = this.encodeBandInt("cp_Double_hi", highBits, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cp_Double_hi[" + highBits.length + "]");
        encodedBand = this.encodeBandInt("cp_Double_lo", loBits, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cp_Double_lo[" + loBits.length + "]");
    }

    private void writeCpFloat(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + this.cp_Float.size() + " Float entries...");
        int[] cpFloat = new int[this.cp_Float.size()];
        int i = 0;
        for (CPFloat fl : this.cp_Float) {
            cpFloat[i] = Float.floatToIntBits(fl.getFloat());
            ++i;
        }
        byte[] encodedBand = this.encodeBandInt("cp_Float", cpFloat, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cp_Float[" + cpFloat.length + "]");
    }

    private void writeCpInt(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + this.cp_Int.size() + " Integer entries...");
        int[] cpInt = new int[this.cp_Int.size()];
        int i = 0;
        for (CPInt integer : this.cp_Int) {
            cpInt[i] = integer.getInt();
            ++i;
        }
        byte[] encodedBand = this.encodeBandInt("cp_Int", cpInt, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cp_Int[" + cpInt.length + "]");
    }

    private void writeCpLong(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + this.cp_Long.size() + " Long entries...");
        int[] highBits = new int[this.cp_Long.size()];
        int[] loBits = new int[this.cp_Long.size()];
        int i = 0;
        for (CPLong lng : this.cp_Long) {
            long l = lng.getLong();
            highBits[i] = (int)(l >> 32);
            loBits[i] = (int)l;
            ++i;
        }
        byte[] encodedBand = this.encodeBandInt("cp_Long_hi", highBits, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cp_Long_hi[" + highBits.length + "]");
        encodedBand = this.encodeBandInt("cp_Long_lo", loBits, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cp_Long_lo[" + loBits.length + "]");
    }

    private void writeCpMethodOrField(Set<CPMethodOrField> cp, OutputStream out, String name) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + cp.size() + " Method and Field entries...");
        int[] cp_methodOrField_class = new int[cp.size()];
        int[] cp_methodOrField_desc = new int[cp.size()];
        int i = 0;
        for (CPMethodOrField mOrF : cp) {
            cp_methodOrField_class[i] = mOrF.getClassIndex();
            cp_methodOrField_desc[i] = mOrF.getDescIndex();
            ++i;
        }
        byte[] encodedBand = this.encodeBandInt(name + "_class", cp_methodOrField_class, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + name + "_class[" + cp_methodOrField_class.length + "]");
        encodedBand = this.encodeBandInt(name + "_desc", cp_methodOrField_desc, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + name + "_desc[" + cp_methodOrField_desc.length + "]");
    }

    private void writeCpSignature(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + this.cp_Signature.size() + " Signature entries...");
        int[] cpSignatureForm = new int[this.cp_Signature.size()];
        ArrayList<CPClass> classes = new ArrayList<CPClass>();
        int i = 0;
        for (CPSignature cpS : this.cp_Signature) {
            classes.addAll(cpS.getClasses());
            cpSignatureForm[i] = cpS.getIndexInCpUtf8();
            ++i;
        }
        int[] cpSignatureClasses = new int[classes.size()];
        Arrays.setAll(cpSignatureClasses, j -> ((CPClass)classes.get(j)).getIndex());
        byte[] encodedBand = this.encodeBandInt("cpSignatureForm", cpSignatureForm, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cpSignatureForm[" + cpSignatureForm.length + "]");
        encodedBand = this.encodeBandInt("cpSignatureClasses", cpSignatureClasses, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cpSignatureClasses[" + cpSignatureClasses.length + "]");
    }

    private void writeCpString(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + this.cp_String.size() + " String entries...");
        int[] cpString = new int[this.cp_String.size()];
        int i = 0;
        for (CPString cpStr : this.cp_String) {
            cpString[i] = cpStr.getIndexInCpUtf8();
            ++i;
        }
        byte[] encodedBand = this.encodeBandInt("cpString", cpString, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cpString[" + cpString.length + "]");
    }

    private void writeCpUtf8(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing " + this.cp_Utf8.size() + " UTF8 entries...");
        int[] cpUtf8Prefix = new int[this.cp_Utf8.size() - 2];
        int[] cpUtf8Suffix = new int[this.cp_Utf8.size() - 1];
        ArrayList<Character> chars = new ArrayList<Character>();
        ArrayList<Integer> bigSuffix = new ArrayList<Integer>();
        ArrayList<Character> bigChars = new ArrayList<Character>();
        Object[] cpUtf8Array = this.cp_Utf8.toArray();
        String first = ((CPUTF8)cpUtf8Array[1]).getUnderlyingString();
        cpUtf8Suffix[0] = first.length();
        this.addCharacters(chars, first.toCharArray());
        for (int i2 = 2; i2 < cpUtf8Array.length; ++i2) {
            char[] previous = ((CPUTF8)cpUtf8Array[i2 - 1]).getUnderlyingString().toCharArray();
            String currentStr = ((CPUTF8)cpUtf8Array[i2]).getUnderlyingString();
            char[] current = currentStr.toCharArray();
            int prefix = 0;
            for (int j2 = 0; j2 < previous.length && previous[j2] == current[j2]; ++j2) {
                ++prefix;
            }
            cpUtf8Prefix[i2 - 2] = prefix;
            char[] suffix = (currentStr = currentStr.substring(prefix)).toCharArray();
            if (suffix.length > 1000) {
                cpUtf8Suffix[i2 - 1] = 0;
                bigSuffix.add(suffix.length);
                this.addCharacters(bigChars, suffix);
                continue;
            }
            cpUtf8Suffix[i2 - 1] = suffix.length;
            this.addCharacters(chars, suffix);
        }
        int[] cpUtf8Chars = new int[chars.size()];
        int[] cpUtf8BigSuffix = new int[bigSuffix.size()];
        int[][] cpUtf8BigChars = new int[bigSuffix.size()][];
        Arrays.setAll(cpUtf8Chars, i -> ((Character)chars.get(i)).charValue());
        for (int i3 = 0; i3 < cpUtf8BigSuffix.length; ++i3) {
            int numBigChars;
            cpUtf8BigSuffix[i3] = numBigChars = ((Integer)bigSuffix.get(i3)).intValue();
            cpUtf8BigChars[i3] = new int[numBigChars];
            Arrays.setAll(cpUtf8BigChars[i3], j -> ((Character)bigChars.remove(0)).charValue());
        }
        byte[] encodedBand = this.encodeBandInt("cpUtf8Prefix", cpUtf8Prefix, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cpUtf8Prefix[" + cpUtf8Prefix.length + "]");
        encodedBand = this.encodeBandInt("cpUtf8Suffix", cpUtf8Suffix, Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cpUtf8Suffix[" + cpUtf8Suffix.length + "]");
        encodedBand = this.encodeBandInt("cpUtf8Chars", cpUtf8Chars, Codec.CHAR3);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cpUtf8Chars[" + cpUtf8Chars.length + "]");
        encodedBand = this.encodeBandInt("cpUtf8BigSuffix", cpUtf8BigSuffix, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from cpUtf8BigSuffix[" + cpUtf8BigSuffix.length + "]");
        for (int i4 = 0; i4 < cpUtf8BigChars.length; ++i4) {
            encodedBand = this.encodeBandInt("cpUtf8BigChars " + i4, cpUtf8BigChars[i4], Codec.DELTA5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from cpUtf8BigChars" + i4 + "[" + cpUtf8BigChars[i4].length + "]");
        }
    }
}

