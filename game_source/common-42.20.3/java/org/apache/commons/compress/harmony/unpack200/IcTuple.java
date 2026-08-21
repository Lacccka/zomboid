/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200;

import java.util.ArrayList;
import java.util.Objects;

public class IcTuple {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final int NESTED_CLASS_FLAG = 65536;
    static final IcTuple[] EMPTY_ARRAY = new IcTuple[0];
    private final int cIndex;
    private final int c2Index;
    private final int nIndex;
    private final int tIndex;
    protected String C;
    protected int F;
    protected String C2;
    protected String N;
    private boolean predictSimple;
    private boolean predictOuter;
    private String cachedOuterClassString;
    private String cachedSimpleClassName;
    private boolean initialized;
    private boolean anonymous;
    private boolean outerIsAnonymous;
    private boolean member = true;
    private int cachedOuterClassIndex = -1;
    private int cachedSimpleClassNameIndex = -1;
    private boolean hashCodeComputed;
    private int cachedHashCode;

    public IcTuple(String C, int F, String C2, String N, int cIndex, int c2Index, int nIndex, int tIndex) {
        this.C = C;
        this.F = F;
        this.C2 = C2;
        this.N = N;
        this.cIndex = cIndex;
        this.c2Index = c2Index;
        this.nIndex = nIndex;
        this.tIndex = tIndex;
        if (null == N) {
            this.predictSimple = true;
        }
        if (null == C2) {
            this.predictOuter = true;
        }
        this.initializeClassStrings();
    }

    private boolean computeOuterIsAnonymous() {
        String[] result = this.innerBreakAtDollar(this.cachedOuterClassString);
        if (result.length == 0) {
            throw new Error("Should have an outer before checking if it's anonymous");
        }
        for (String element : result) {
            if (!this.isAllDigits(element)) continue;
            return true;
        }
        return false;
    }

    public boolean equals(Object object) {
        if (object == null || object.getClass() != this.getClass()) {
            return false;
        }
        IcTuple other = (IcTuple)object;
        return Objects.equals(this.C, other.C) && Objects.equals(this.C2, other.C2) && Objects.equals(this.N, other.N);
    }

    private void generateHashCode() {
        this.hashCodeComputed = true;
        this.cachedHashCode = 17;
        if (this.C != null) {
            this.cachedHashCode = this.C.hashCode();
        }
        if (this.C2 != null) {
            this.cachedHashCode = this.C2.hashCode();
        }
        if (this.N != null) {
            this.cachedHashCode = this.N.hashCode();
        }
    }

    public String getC() {
        return this.C;
    }

    public String getC2() {
        return this.C2;
    }

    public int getF() {
        return this.F;
    }

    public String getN() {
        return this.N;
    }

    public int getTupleIndex() {
        return this.tIndex;
    }

    public int hashCode() {
        if (!this.hashCodeComputed) {
            this.generateHashCode();
        }
        return this.cachedHashCode;
    }

    private void initializeClassStrings() {
        String[] nameComponents;
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        if (!this.predictSimple) {
            this.cachedSimpleClassName = this.N;
        }
        if (!this.predictOuter) {
            this.cachedOuterClassString = this.C2;
        }
        if ((nameComponents = this.innerBreakAtDollar(this.C)).length == 0) {
            // empty if block
        }
        if (nameComponents.length == 1) {
            // empty if block
        }
        if (nameComponents.length < 2) {
            return;
        }
        int lastPosition = nameComponents.length - 1;
        this.cachedSimpleClassName = nameComponents[lastPosition];
        this.cachedOuterClassString = "";
        for (int index = 0; index < lastPosition; ++index) {
            this.cachedOuterClassString = this.cachedOuterClassString + nameComponents[index];
            if (this.isAllDigits(nameComponents[index])) {
                this.member = false;
            }
            if (index + 1 == lastPosition) continue;
            this.cachedOuterClassString = this.cachedOuterClassString + '$';
        }
        if (!this.predictSimple) {
            this.cachedSimpleClassName = this.N;
            this.cachedSimpleClassNameIndex = this.nIndex;
        }
        if (!this.predictOuter) {
            this.cachedOuterClassString = this.C2;
            this.cachedOuterClassIndex = this.c2Index;
        }
        if (this.isAllDigits(this.cachedSimpleClassName)) {
            this.anonymous = true;
            this.member = false;
            if (this.nestedExplicitFlagSet()) {
                this.member = true;
            }
        }
        this.outerIsAnonymous = this.computeOuterIsAnonymous();
    }

    public String[] innerBreakAtDollar(String className) {
        ArrayList<String> resultList = new ArrayList<String>();
        int start = 0;
        int index = 0;
        while (index < className.length()) {
            if (className.charAt(index) <= '$') {
                resultList.add(className.substring(start, index));
                start = index + 1;
            }
            if (++index < className.length()) continue;
            resultList.add(className.substring(start));
        }
        return resultList.toArray(EMPTY_STRING_ARRAY);
    }

    private boolean isAllDigits(String nameString) {
        if (null == nameString) {
            return false;
        }
        for (int index = 0; index < nameString.length(); ++index) {
            if (Character.isDigit(nameString.charAt(index))) continue;
            return false;
        }
        return true;
    }

    public boolean isAnonymous() {
        return this.anonymous;
    }

    public boolean isMember() {
        return this.member;
    }

    public boolean nestedExplicitFlagSet() {
        return (this.F & 0x10000) == 65536;
    }

    public boolean nullSafeEquals(String stringOne, String stringTwo) {
        if (null == stringOne) {
            return null == stringTwo;
        }
        return stringOne.equals(stringTwo);
    }

    public int outerClassIndex() {
        return this.cachedOuterClassIndex;
    }

    public String outerClassString() {
        return this.cachedOuterClassString;
    }

    public boolean outerIsAnonymous() {
        return this.outerIsAnonymous;
    }

    public boolean predicted() {
        return this.predictOuter || this.predictSimple;
    }

    public String simpleClassName() {
        return this.cachedSimpleClassName;
    }

    public int simpleClassNameIndex() {
        return this.cachedSimpleClassNameIndex;
    }

    public int thisClassIndex() {
        if (this.predicted()) {
            return this.cIndex;
        }
        return -1;
    }

    public String thisClassString() {
        if (this.predicted()) {
            return this.C;
        }
        return this.C2 + "$" + this.N;
    }

    public String toString() {
        return "IcTuple " + '(' + this.simpleClassName() + " in " + this.outerClassString() + ')';
    }
}

