/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.BCIRenumberedAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ExceptionTableEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.LocalVariableTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.LocalVariableTypeTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;

public class CodeAttribute
extends BCIRenumberedAttribute {
    private static CPUTF8 attributeName;
    public List<Attribute> attributes = new ArrayList<Attribute>();
    public List<Integer> byteCodeOffsets = new ArrayList<Integer>();
    public List<ByteCode> byteCodes = new ArrayList<ByteCode>();
    public int codeLength;
    public List<ExceptionTableEntry> exceptionTable;
    public int maxLocals;
    public int maxStack;

    public static void setAttributeName(CPUTF8 attributeName) {
        CodeAttribute.attributeName = attributeName;
    }

    public CodeAttribute(int maxStack, int maxLocals, byte[] codePacked, Segment segment, OperandManager operandManager, List<ExceptionTableEntry> exceptionTable) {
        super(attributeName);
        this.maxLocals = maxLocals;
        this.maxStack = maxStack;
        this.codeLength = 0;
        this.exceptionTable = exceptionTable;
        this.byteCodeOffsets.add(0);
        int byteCodeIndex = 0;
        for (int i = 0; i < codePacked.length; ++i) {
            ByteCode byteCode = ByteCode.getByteCode(codePacked[i] & 0xFF);
            byteCode.setByteCodeIndex(byteCodeIndex);
            ++byteCodeIndex;
            byteCode.extractOperands(operandManager, segment, this.codeLength);
            this.byteCodes.add(byteCode);
            this.codeLength += byteCode.getLength();
            int lastBytecodePosition = this.byteCodeOffsets.get(this.byteCodeOffsets.size() - 1);
            if (byteCode.hasMultipleByteCodes()) {
                this.byteCodeOffsets.add(lastBytecodePosition + 1);
                ++byteCodeIndex;
            }
            if (i < codePacked.length - 1) {
                this.byteCodeOffsets.add(lastBytecodePosition + byteCode.getLength());
            }
            if (byteCode.getOpcode() != 196) continue;
            ++i;
        }
        for (ByteCode byteCode : this.byteCodes) {
            byteCode.applyByteCodeTargetFixup(this);
        }
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        if (attribute instanceof LocalVariableTableAttribute) {
            ((LocalVariableTableAttribute)attribute).setCodeLength(this.codeLength);
        }
        if (attribute instanceof LocalVariableTypeTableAttribute) {
            ((LocalVariableTypeTableAttribute)attribute).setCodeLength(this.codeLength);
        }
    }

    @Override
    protected int getLength() {
        int attributesSize = 0;
        for (Attribute attribute : this.attributes) {
            attributesSize += attribute.getLengthIncludingHeader();
        }
        return 8 + this.codeLength + 2 + this.exceptionTable.size() * 8 + 2 + attributesSize;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        ArrayList<ClassFileEntry> nestedEntries = new ArrayList<ClassFileEntry>(this.attributes.size() + this.byteCodes.size() + 10);
        nestedEntries.add(this.getAttributeName());
        nestedEntries.addAll(this.byteCodes);
        nestedEntries.addAll(this.attributes);
        for (ExceptionTableEntry entry : this.exceptionTable) {
            CPClass catchType = entry.getCatchType();
            if (catchType == null) continue;
            nestedEntries.add(catchType);
        }
        return nestedEntries.toArray(NONE);
    }

    @Override
    protected int[] getStartPCs() {
        return null;
    }

    @Override
    public void renumber(List<Integer> byteCodeOffsets) {
        this.exceptionTable.forEach(entry -> entry.renumber(byteCodeOffsets));
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.attributes.forEach(attribute -> attribute.resolve(pool));
        this.byteCodes.forEach(byteCode -> byteCode.resolve(pool));
        this.exceptionTable.forEach(byteCode -> byteCode.resolve(pool));
    }

    @Override
    public String toString() {
        return "Code: " + this.getLength() + " bytes";
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.maxStack);
        dos.writeShort(this.maxLocals);
        dos.writeInt(this.codeLength);
        for (ByteCode byteCode : this.byteCodes) {
            byteCode.write(dos);
        }
        dos.writeShort(this.exceptionTable.size());
        for (ExceptionTableEntry entry : this.exceptionTable) {
            entry.write(dos);
        }
        dos.writeShort(this.attributes.size());
        for (Attribute attribute : this.attributes) {
            attribute.write(dos);
        }
    }
}

