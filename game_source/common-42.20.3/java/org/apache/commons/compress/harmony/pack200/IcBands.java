/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.compress.harmony.pack200.BandSet;
import org.apache.commons.compress.harmony.pack200.CPClass;
import org.apache.commons.compress.harmony.pack200.CPUTF8;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.CpBands;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingUtils;
import org.apache.commons.compress.harmony.pack200.SegmentHeader;

public class IcBands
extends BandSet {
    private final Set<IcTuple> innerClasses = new TreeSet<IcTuple>();
    private final CpBands cpBands;
    private int bit16Count;
    private final Map<String, List<IcTuple>> outerToInner = new HashMap<String, List<IcTuple>>();

    public IcBands(SegmentHeader segmentHeader, CpBands cpBands, int effort) {
        super(effort, segmentHeader);
        this.cpBands = cpBands;
    }

    public void addInnerClass(String name, String outerName, String innerName, int flags) {
        if (outerName != null || innerName != null) {
            if (this.namesArePredictable(name, outerName, innerName)) {
                IcTuple innerClass = new IcTuple(this.cpBands.getCPClass(name), flags, null, null);
                this.addToMap(outerName, innerClass);
                this.innerClasses.add(innerClass);
            } else {
                IcTuple icTuple = new IcTuple(this.cpBands.getCPClass(name), flags |= 0x10000, this.cpBands.getCPClass(outerName), this.cpBands.getCPUtf8(innerName));
                boolean added = this.innerClasses.add(icTuple);
                if (added) {
                    ++this.bit16Count;
                    this.addToMap(outerName, icTuple);
                }
            }
        } else {
            IcTuple innerClass = new IcTuple(this.cpBands.getCPClass(name), flags, null, null);
            this.addToMap(this.getOuter(name), innerClass);
            this.innerClasses.add(innerClass);
        }
    }

    private void addToMap(String outerName, IcTuple icTuple) {
        List<IcTuple> tuples = this.outerToInner.get(outerName);
        if (tuples == null) {
            tuples = new ArrayList<IcTuple>();
            this.outerToInner.put(outerName, tuples);
        } else {
            for (IcTuple tuple : tuples) {
                if (!icTuple.equals(tuple)) continue;
                return;
            }
        }
        tuples.add(icTuple);
    }

    public void finaliseBands() {
        this.segmentHeader.setIc_count(this.innerClasses.size());
    }

    public IcTuple getIcTuple(CPClass inner) {
        for (IcTuple icTuple : this.innerClasses) {
            if (!icTuple.C.equals(inner)) continue;
            return icTuple;
        }
        return null;
    }

    public List<IcTuple> getInnerClassesForOuter(String outerClassName) {
        return this.outerToInner.get(outerClassName);
    }

    private String getOuter(String name) {
        return name.substring(0, name.lastIndexOf(36));
    }

    private boolean namesArePredictable(String name, String outerName, String innerName) {
        return name.equals(outerName + '$' + innerName) && innerName.indexOf(36) == -1;
    }

    @Override
    public void pack(OutputStream outputStream2) throws IOException, Pack200Exception {
        PackingUtils.log("Writing internal class bands...");
        int[] ic_this_class = new int[this.innerClasses.size()];
        int[] ic_flags = new int[this.innerClasses.size()];
        int[] ic_outer_class = new int[this.bit16Count];
        int[] ic_name = new int[this.bit16Count];
        int index2 = 0;
        ArrayList<IcTuple> innerClassesList = new ArrayList<IcTuple>(this.innerClasses);
        for (int i = 0; i < ic_this_class.length; ++i) {
            IcTuple icTuple = (IcTuple)innerClassesList.get(i);
            ic_this_class[i] = icTuple.C.getIndex();
            ic_flags[i] = icTuple.F;
            if ((icTuple.F & 0x10000) == 0) continue;
            ic_outer_class[index2] = icTuple.C2 == null ? 0 : icTuple.C2.getIndex() + 1;
            ic_name[index2] = icTuple.N == null ? 0 : icTuple.N.getIndex() + 1;
            ++index2;
        }
        byte[] encodedBand = this.encodeBandInt("ic_this_class", ic_this_class, Codec.UDELTA5);
        outputStream2.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from ic_this_class[" + ic_this_class.length + "]");
        encodedBand = this.encodeBandInt("ic_flags", ic_flags, Codec.UNSIGNED5);
        outputStream2.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from ic_flags[" + ic_flags.length + "]");
        encodedBand = this.encodeBandInt("ic_outer_class", ic_outer_class, Codec.DELTA5);
        outputStream2.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from ic_outer_class[" + ic_outer_class.length + "]");
        encodedBand = this.encodeBandInt("ic_name", ic_name, Codec.DELTA5);
        outputStream2.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from ic_name[" + ic_name.length + "]");
    }

    static class IcTuple
    implements Comparable<IcTuple> {
        protected CPClass C;
        protected int F;
        protected CPClass C2;
        protected CPUTF8 N;

        IcTuple(CPClass C, int F, CPClass C2, CPUTF8 N) {
            this.C = C;
            this.F = F;
            this.C2 = C2;
            this.N = N;
        }

        @Override
        public int compareTo(IcTuple arg0) {
            return this.C.compareTo(arg0.C);
        }

        public boolean equals(Object o) {
            if (o instanceof IcTuple) {
                IcTuple icT = (IcTuple)o;
                return this.C.equals(icT.C) && this.F == icT.F && Objects.equals(this.C2, icT.C2) && Objects.equals(this.N, icT.N);
            }
            return false;
        }

        public boolean isAnonymous() {
            String className = this.C.toString();
            String innerName = className.substring(className.lastIndexOf(36) + 1);
            return Character.isDigit(innerName.charAt(0));
        }

        public String toString() {
            return this.C.toString();
        }
    }
}

