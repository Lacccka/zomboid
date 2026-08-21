/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.compress.harmony.pack200.BandSet;
import org.apache.commons.compress.harmony.pack200.CPConstant;
import org.apache.commons.compress.harmony.pack200.CPSignature;
import org.apache.commons.compress.harmony.pack200.CPUTF8;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.CpBands;
import org.apache.commons.compress.harmony.pack200.IntList;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingUtils;
import org.apache.commons.compress.harmony.pack200.SegmentHeader;

public class MetadataBandGroup
extends BandSet {
    public static final int CONTEXT_CLASS = 0;
    public static final int CONTEXT_FIELD = 1;
    public static final int CONTEXT_METHOD = 2;
    private final String type;
    private int numBackwardsCalls;
    public IntList param_NB = new IntList();
    public IntList anno_N = new IntList();
    public List<CPSignature> type_RS = new ArrayList<CPSignature>();
    public IntList pair_N = new IntList();
    public List<CPUTF8> name_RU = new ArrayList<CPUTF8>();
    public List<String> T = new ArrayList<String>();
    public List<CPConstant<?>> caseI_KI = new ArrayList();
    public List<CPConstant<?>> caseD_KD = new ArrayList();
    public List<CPConstant<?>> caseF_KF = new ArrayList();
    public List<CPConstant<?>> caseJ_KJ = new ArrayList();
    public List<CPSignature> casec_RS = new ArrayList<CPSignature>();
    public List<CPSignature> caseet_RS = new ArrayList<CPSignature>();
    public List<CPUTF8> caseec_RU = new ArrayList<CPUTF8>();
    public List<CPUTF8> cases_RU = new ArrayList<CPUTF8>();
    public IntList casearray_N = new IntList();
    public List<CPSignature> nesttype_RS = new ArrayList<CPSignature>();
    public IntList nestpair_N = new IntList();
    public List<CPUTF8> nestname_RU = new ArrayList<CPUTF8>();
    private final CpBands cpBands;
    private final int context;

    public MetadataBandGroup(String type, int context, CpBands cpBands, SegmentHeader segmentHeader, int effort) {
        super(effort, segmentHeader);
        this.type = type;
        this.cpBands = cpBands;
        this.context = context;
    }

    public void addAnnotation(String desc, List<String> nameRU, List<String> tags, List<Object> values2, List<Integer> caseArrayN, List<String> nestTypeRS, List<String> nestNameRU, List<Integer> nestPairN) {
        this.type_RS.add(this.cpBands.getCPSignature(desc));
        this.pair_N.add(nameRU.size());
        nameRU.forEach(name -> this.name_RU.add(this.cpBands.getCPUtf8((String)name)));
        Iterator<Object> valuesIterator = values2.iterator();
        for (String tag : tags) {
            this.T.add(tag);
            switch (tag) {
                case "B": 
                case "C": 
                case "I": 
                case "S": 
                case "Z": {
                    this.caseI_KI.add(this.cpBands.getConstant(valuesIterator.next()));
                    break;
                }
                case "D": {
                    this.caseD_KD.add(this.cpBands.getConstant(valuesIterator.next()));
                    break;
                }
                case "F": {
                    this.caseF_KF.add(this.cpBands.getConstant(valuesIterator.next()));
                    break;
                }
                case "J": {
                    this.caseJ_KJ.add(this.cpBands.getConstant(valuesIterator.next()));
                    break;
                }
                case "c": {
                    this.casec_RS.add(this.cpBands.getCPSignature(this.nextString(valuesIterator)));
                    break;
                }
                case "e": {
                    this.caseet_RS.add(this.cpBands.getCPSignature(this.nextString(valuesIterator)));
                    this.caseec_RU.add(this.cpBands.getCPUtf8(this.nextString(valuesIterator)));
                    break;
                }
                case "s": {
                    this.cases_RU.add(this.cpBands.getCPUtf8(this.nextString(valuesIterator)));
                }
            }
        }
        for (Integer element2 : caseArrayN) {
            int arraySize = element2;
            this.casearray_N.add(arraySize);
            this.numBackwardsCalls += arraySize;
        }
        nestTypeRS.forEach(element -> this.nesttype_RS.add(this.cpBands.getCPSignature((String)element)));
        nestNameRU.forEach(element -> this.nestname_RU.add(this.cpBands.getCPUtf8((String)element)));
        for (Integer numPairs : nestPairN) {
            this.nestpair_N.add(numPairs);
            this.numBackwardsCalls += numPairs.intValue();
        }
    }

    public void addParameterAnnotation(int numParams, int[] annoN, IntList pairN, List<String> typeRS, List<String> nameRU, List<String> tags, List<Object> values2, List<Integer> caseArrayN, List<String> nestTypeRS, List<String> nestNameRU, List<Integer> nestPairN) {
        this.param_NB.add(numParams);
        for (int element : annoN) {
            this.anno_N.add(element);
        }
        this.pair_N.addAll(pairN);
        typeRS.forEach(desc -> this.type_RS.add(this.cpBands.getCPSignature((String)desc)));
        nameRU.forEach(name -> this.name_RU.add(this.cpBands.getCPUtf8((String)name)));
        Iterator<Object> valuesIterator = values2.iterator();
        for (String tag : tags) {
            this.T.add(tag);
            switch (tag) {
                case "B": 
                case "C": 
                case "I": 
                case "S": 
                case "Z": {
                    this.caseI_KI.add(this.cpBands.getConstant(valuesIterator.next()));
                    break;
                }
                case "D": {
                    this.caseD_KD.add(this.cpBands.getConstant(valuesIterator.next()));
                    break;
                }
                case "F": {
                    this.caseF_KF.add(this.cpBands.getConstant(valuesIterator.next()));
                    break;
                }
                case "J": {
                    this.caseJ_KJ.add(this.cpBands.getConstant(valuesIterator.next()));
                    break;
                }
                case "c": {
                    this.casec_RS.add(this.cpBands.getCPSignature(this.nextString(valuesIterator)));
                    break;
                }
                case "e": {
                    this.caseet_RS.add(this.cpBands.getCPSignature(this.nextString(valuesIterator)));
                    this.caseec_RU.add(this.cpBands.getCPUtf8(this.nextString(valuesIterator)));
                    break;
                }
                case "s": {
                    this.cases_RU.add(this.cpBands.getCPUtf8(this.nextString(valuesIterator)));
                }
            }
        }
        for (Integer element : caseArrayN) {
            int arraySize = element;
            this.casearray_N.add(arraySize);
            this.numBackwardsCalls += arraySize;
        }
        nestTypeRS.forEach(type -> this.nesttype_RS.add(this.cpBands.getCPSignature((String)type)));
        nestNameRU.forEach(name -> this.nestname_RU.add(this.cpBands.getCPUtf8((String)name)));
        for (Integer numPairs : nestPairN) {
            this.nestpair_N.add(numPairs);
            this.numBackwardsCalls += numPairs.intValue();
        }
    }

    public boolean hasContent() {
        return this.type_RS.size() > 0;
    }

    public void incrementAnnoN() {
        this.anno_N.increment(this.anno_N.size() - 1);
    }

    public void newEntryInAnnoN() {
        this.anno_N.add(1);
    }

    private String nextString(Iterator<Object> valuesIterator) {
        return (String)valuesIterator.next();
    }

    public int numBackwardsCalls() {
        return this.numBackwardsCalls;
    }

    @Override
    public void pack(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing metadata band group...");
        if (this.hasContent()) {
            byte[] encodedBand;
            String contextStr = this.context == 0 ? "Class" : (this.context == 1 ? "Field" : "Method");
            if (!this.type.equals("AD")) {
                if (this.type.indexOf(80) != -1) {
                    encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " param_NB", this.param_NB.toArray(), Codec.BYTE1);
                    out.write(encodedBand);
                    PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " anno_N[" + this.param_NB.size() + "]");
                }
                encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " anno_N", this.anno_N.toArray(), Codec.UNSIGNED5);
                out.write(encodedBand);
                PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " anno_N[" + this.anno_N.size() + "]");
                encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " type_RS", this.cpEntryListToArray(this.type_RS), Codec.UNSIGNED5);
                out.write(encodedBand);
                PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " type_RS[" + this.type_RS.size() + "]");
                encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " pair_N", this.pair_N.toArray(), Codec.UNSIGNED5);
                out.write(encodedBand);
                PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " pair_N[" + this.pair_N.size() + "]");
                encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " name_RU", this.cpEntryListToArray(this.name_RU), Codec.UNSIGNED5);
                out.write(encodedBand);
                PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " name_RU[" + this.name_RU.size() + "]");
            }
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " T", this.tagListToArray(this.T), Codec.BYTE1);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " T[" + this.T.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseI_KI", this.cpEntryListToArray(this.caseI_KI), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseI_KI[" + this.caseI_KI.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseD_KD", this.cpEntryListToArray(this.caseD_KD), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseD_KD[" + this.caseD_KD.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseF_KF", this.cpEntryListToArray(this.caseF_KF), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseF_KF[" + this.caseF_KF.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseJ_KJ", this.cpEntryListToArray(this.caseJ_KJ), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseJ_KJ[" + this.caseJ_KJ.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " casec_RS", this.cpEntryListToArray(this.casec_RS), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " casec_RS[" + this.casec_RS.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseet_RS", this.cpEntryListToArray(this.caseet_RS), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseet_RS[" + this.caseet_RS.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseec_RU", this.cpEntryListToArray(this.caseec_RU), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseec_RU[" + this.caseec_RU.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " cases_RU", this.cpEntryListToArray(this.cases_RU), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " cases_RU[" + this.cases_RU.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " casearray_N", this.casearray_N.toArray(), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " casearray_N[" + this.casearray_N.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " nesttype_RS", this.cpEntryListToArray(this.nesttype_RS), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " nesttype_RS[" + this.nesttype_RS.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " nestpair_N", this.nestpair_N.toArray(), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " nestpair_N[" + this.nestpair_N.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " nestname_RU", this.cpEntryListToArray(this.nestname_RU), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " nestname_RU[" + this.nestname_RU.size() + "]");
        }
    }

    public void removeLatest() {
        int latest = this.anno_N.remove(this.anno_N.size() - 1);
        for (int i = 0; i < latest; ++i) {
            this.type_RS.remove(this.type_RS.size() - 1);
            int pairs = this.pair_N.remove(this.pair_N.size() - 1);
            for (int j = 0; j < pairs; ++j) {
                this.removeOnePair();
            }
        }
    }

    private void removeOnePair() {
        String tag;
        switch (tag = this.T.remove(this.T.size() - 1)) {
            case "B": 
            case "C": 
            case "I": 
            case "S": 
            case "Z": {
                this.caseI_KI.remove(this.caseI_KI.size() - 1);
                break;
            }
            case "D": {
                this.caseD_KD.remove(this.caseD_KD.size() - 1);
                break;
            }
            case "F": {
                this.caseF_KF.remove(this.caseF_KF.size() - 1);
                break;
            }
            case "J": {
                this.caseJ_KJ.remove(this.caseJ_KJ.size() - 1);
                break;
            }
            case "e": {
                this.caseet_RS.remove(this.caseet_RS.size() - 1);
                this.caseec_RU.remove(this.caseet_RS.size() - 1);
                break;
            }
            case "s": {
                this.cases_RU.remove(this.cases_RU.size() - 1);
                break;
            }
            case "[": {
                int arraySize = this.casearray_N.remove(this.casearray_N.size() - 1);
                this.numBackwardsCalls -= arraySize;
                for (int k = 0; k < arraySize; ++k) {
                    this.removeOnePair();
                }
                break;
            }
            case "@": {
                this.nesttype_RS.remove(this.nesttype_RS.size() - 1);
                int numPairs = this.nestpair_N.remove(this.nestpair_N.size() - 1);
                this.numBackwardsCalls -= numPairs;
                for (int i = 0; i < numPairs; ++i) {
                    this.removeOnePair();
                }
                break;
            }
        }
    }

    private int[] tagListToArray(List<String> list) {
        return list.stream().mapToInt(s -> s.charAt(0)).toArray();
    }
}

